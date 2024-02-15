package dat.serverAndClient.server;

import dat.serverAndClient.Message;
import dat.executeWith.ExecuteWithIF;
import dat.util.Colors;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.*;

public class Server implements Runnable, ExecuteWithIF //TODO: unit tests and integration tests
{
    
    public static final String COMMAND_START = "/";
    public static final String COMMAND_HELP = COMMAND_START + "help";
    public static final String COMMAND_EXIT = COMMAND_START + "exit";
    
    public static final String COMMAND_COMPUTER_START = Colors.RESET_ANSI;
    public static final String COMMAND_COMPUTER_MYNAME = COMMAND_COMPUTER_START + "myname";
    
    public static final int MESSAGE_QUEUE_SIZE_DEFAULT = 50;
    public static final int MAX_ACTIVE_CLIENTS_DEFAULT = 100; //_AND_MAX_THREADS_FOR_CLIENTS_DEFAULT
    public static final int MIN_SERVER_THREADS = 3;
    
    private final int port;
    private final String name;
    private final int messageQueueSize;
    private final int maxActiveClients;
    private final Scanner scanner;
    
    private ServerSocket serverSocket;
    
    private final BlockingQueue< Message > messageQueue;
    private final ConcurrentMap< String, ServerClient > clientMap = new ConcurrentHashMap<>();
    
    private final ArrayList< Future< ? > > serverThreads = new ArrayList<>();
    private final ArrayList< Future< ? > > clientsThreads = new ArrayList<>();
    private ExecutorService localExecutorService = null;
    
    
    
    
    
    
    
    //Constructors---------------------------------------------------------------------------------------
    public Server( int port, String name, int messageQueueSize, int maxActiveClients, Scanner scanner )
    {
        //Assign Input--------------------------
        this.port = port;
        this.name = name;
        this.messageQueueSize = checkMessageQueueSize( messageQueueSize );
        this.maxActiveClients = checkMaxActiveClients( maxActiveClients );
        this.scanner = scanner; //For making it testable
        
        //Final Setup------------------------------
        this.messageQueue = new ArrayBlockingQueue<>( this.messageQueueSize );
    }
    
    public Server( int port, String name, int messageQueueSize, int maxActiveClients )
    {
        this(
                port,
                name,
                messageQueueSize,
                maxActiveClients,
                new Scanner( System.in )
        );
    }
    
    public Server( int port, String name )
    {
        this(
                port,
                name,
                MESSAGE_QUEUE_SIZE_DEFAULT,
                MAX_ACTIVE_CLIENTS_DEFAULT,
                new Scanner( System.in )
        );
    }
    
    
    
    
    
    
    
    //Constructor helpers--------------------------------------------------------------------------------
    private static int checkMessageQueueSize( int messageQueueSize )
    {
        if ( messageQueueSize > 1 ) {
            return messageQueueSize;
        }
        return MESSAGE_QUEUE_SIZE_DEFAULT;
    }
    
    private static int checkMaxActiveClients( int maxActiveClients )
    {
        if ( maxActiveClients > 1 ) {
            return maxActiveClients;
        }
        return MAX_ACTIVE_CLIENTS_DEFAULT;
    }
    
    
    
    
    
    
    //Run Server-------------------------------------------------------------------------------------------
    @Override
    public void run()
    {
        this.localExecutorService = Executors.newFixedThreadPool( MIN_SERVER_THREADS + this.maxActiveClients );
        
        this.executeWith( this.localExecutorService );
    }
    
    @Override
    public void executeWith( ExecutorService executorService )
    {
        
        
        try {
            this.setupSocket();
            
            Future< ? > threadConnect = executorService.submit( () -> this.connect( executorService ) ); //I hate that we are throwing the service down the method hirachy TODO: Don't do that
            Future< ? > threadBroadcaster = executorService.submit( () -> this.broadcastMessagesFromQueue() );
            
            Future< ? > threadServerConsole = executorService.submit( () -> this.serverConsole() );
            
            this.serverThreads.add( threadConnect );
            this.serverThreads.add( threadBroadcaster );
            this.serverThreads.add( threadServerConsole );
            
        } catch ( IOException e ) {
            this.close();
            throw new RuntimeException( e );
        }
        //Then go die
    }
    
    
    
    
    
    
    
    
    //Setup, Connect, Send, Console--------------------------------------------------------------------------------------------------
    public void setupSocket() throws IOException  //ServerSocket
    {
        this.serverSocket = new ServerSocket( this.port );
    }
    
    public void connect( ExecutorService executorService )  //.accept() forever....      //TODO: ExecutorService executorService <- I hate this, fix it!
    {
        do {
            
            try {
                Socket clientSocket = this.serverSocket.accept();  // blocking call
                
                executorService.submit( () -> this.addClient( clientSocket, executorService ) ); //We wanna listen again ASAP, so some other thread can finish this
                
            } catch ( IOException e ) {
                System.out.println( "SERVER: EXCEPTION IO: On .accept()" );
                e.printStackTrace();
            }
            
        } while ( !this.serverSocket.isClosed() );
        
        System.out.println( "SERVER: Stopping Server Connect Thread" );
    }
    
    private void broadcastMessagesFromQueue() //sendMessage
    {
        try {
            
            Message message = new Message( "Server started", this.name, Message.ALL );
            this.broadcastAMessage( message.toString() );
            
            
            while ( !Thread.currentThread().isInterrupted() ) {
                
                message = this.messageQueue.take();
                
                this.broadcastAMessage( message.toString() ); //TODO: send only to recipients
                
            }
            
        } catch ( InterruptedException e ) {
            System.err.println( "SERVER: was interrupted: " + e.getMessage() );
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        
        System.out.println( "SERVER: Stopping Server Broadcast Thread" );
    }
    
    private void broadcastAMessage( String message )
    {
        System.out.println( message );
        
        for ( ServerClient serverClient : this.clientMap.values() ) {
            
            try {
                
                if ( serverClient != null ) {
                    serverClient.sendMessage( message );
                }
                
            } catch ( Exception e ) {
                System.out.println( "SERVER: EXCEPTION: On sendMessage" );
                e.printStackTrace();
            }
        }
    }
    
    public void serverConsole()
    {
        String inputLine;
        Message message;
        
        // send messages to server. The ressources will be close by receiver
        while ( ( inputLine = this.scanner.nextLine() ) != null ) {
            
            if ( !isCommand( inputLine ) ) {
                message = new Message( inputLine, this.name, Message.ALL ); //TODO: select recipient
                this.messageQueue.add( message );
                
            } else {
                this.runCommand( inputLine );
            }
            
        }
        
        System.out.println( "SERVER: Stopping Server Console Thread" );
    }
    
    
    
    
    
    
    
    
    
    
    //Add Client and Listen to their messages----------------------------------------------------------------------
    private void addClient( Socket clientSocket, ExecutorService executorService ) //TODO: ExecutorService executorService <- I hate this, fix it!
    {
        try {
            ServerClient serverClient = new ServerClient( clientSocket );
            Server.this.clientMap.put( serverClient.toString(), serverClient );
            
            //First message is always name
            setServerClientName( serverClient );
            
            clientConnected(serverClient);
            
            this.listenToClientOnNewThread( serverClient, executorService );
            
        } catch ( IOException e ) {
            System.out.println( "SERVER: IO EXCEPTION: On add Client" );
            e.printStackTrace();
        }
        
    }
    
    private void listenToClientOnNewThread( ServerClient serverClient, ExecutorService executorService ) //TODO: ExecutorService executorService <- I hate this, fix it!
    {
        Future< ? > threadAClient = executorService.submit( () -> this.listenToClient( serverClient ) );
        
        this.clientsThreads.add( threadAClient ); //Does not remove clients who disconnect -TO-DO fix this!
    }
    
    private void listenToClient( ServerClient serverClient )
    {
        try {
            
            do {
                serverClient.receiveMessage();
                String rawMessage = serverClient.getLastInput();
                
                Message message = Message.createMessage( rawMessage );
                
                Server.this.messageQueue.add( message );
                
            } while ( serverClient.isRunning() );
            
        } catch ( IOException e ) {
            e.printStackTrace();
        }
        
        serverClient.close();
        this.clientDisconnected(serverClient);
        Server.this.clientMap.remove( serverClient.toString() );
    }
    
    private static void setServerClientName( ServerClient serverClient ) throws IOException
    {
        serverClient.receiveMessage();
        String rawMessage = serverClient.getLastInput();
        
        Message message = Message.createMessage( rawMessage );
        
        if ( !Objects.equals( message.message(), COMMAND_COMPUTER_MYNAME ) ) {  //Check it actually is the name setter request
            System.err.println( "SERVER: CLient's first message was wrong?" );
        }
        
        serverClient.setName( message.sender() );
    }
    
    private void clientConnected( ServerClient serverClient )
    {
        Message message = new Message( Colors.BLUE_ANSI+serverClient.getName()+Colors.RESET_ANSI+" has connected.", this.name,Message.ALL );
        
        this.broadcastAMessage( message.toString() );
    }
    
    private void clientDisconnected( ServerClient serverClient )
    {
        Message message = new Message( Colors.BLUE_ANSI+serverClient.getName()+Colors.RESET_ANSI+" has disconnected.", this.name,Message.ALL );
        
        this.broadcastAMessage( message.toString() );
    }
    
    
    
    
    
    //ServerConsoleCommands
    public static boolean isCommand( String inputLine )
    {
        if ( inputLine.startsWith( COMMAND_START ) ) {
            return true;
        }
        return false;
    }
    
    public static void printCommandHelp()
    {
        System.out.println( COMMAND_HELP );
        System.out.println( COMMAND_EXIT );
    }
    
    private void runCommand( String inputLine )
    {
        if ( !isCommand( inputLine ) ) {
            System.err.println( "ERROR: SERVER-CONSOLE THOUGHT NON-COMMAND WAS A COMMAND?" );
        }
        
        switch ( inputLine ) {
            
            case COMMAND_HELP:
                printCommandHelp();
                return;
            
            case COMMAND_EXIT:
                this.close();
                return;
            
            default:
                System.out.println( "\"" + inputLine + "\" is not a recognized command" );
                return;
        }
    }
    
    
    
    
    
    
    
    
    //Close--------------------------------------------------------------------------------------------
    public void close()   //TODO: don't spam the console with repeat and errors when closing
    {
        System.out.println( "SERVER: Closing down....." );
        
        try {
            
            if ( this.serverSocket != null ) {
                this.serverSocket.close();
                this.serverSocket = null;
            }
            
            if ( this.localExecutorService != null ) {
                this.localExecutorService.shutdownNow();
                this.localExecutorService = null;
            }
            
            System.out.println("Amount of clients at shutdown: "+this.clientMap.size());
            
            this.closeClients();
            
            this.threadsServerClose();
            
            this.threadsClientsClose();
            
            this.scanner.close();
            
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        System.out.println( "SERVER: finished closing!" );
    }
    
    private void closeClients()
    {
        for ( ServerClient serverClient : this.clientMap.values() ) {
            serverClient.close();
        }
        this.clientMap.clear();
    }
    
    private void threadsServerClose()
    {
        for ( Future< ? > serverThread : this.serverThreads ) {
            serverThread.cancel( true );
        }
        this.serverThreads.clear();
    }
    
    private void threadsClientsClose()
    {
        for ( Future< ? > clientsThread : this.clientsThreads ) {
            clientsThread.cancel( true );
        }
        this.clientsThreads.clear();
    }
    
    
    
    
    
    
    //Getters------------------------------------------------------------------------------------
    public int getPort()
    {
        return this.port;
    }
    
    public String getName()
    {
        return this.name;
    }
    
    public int getMessageQueueSize()
    {
        return this.messageQueueSize;
    }
    
    public int getMaxActiveClients()
    {
        return this.maxActiveClients;
    }
    
}
