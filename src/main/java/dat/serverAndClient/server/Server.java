package dat.serverAndClient.server;

import dat.serverAndClient.ChatIF;
import dat.serverAndClient.Message;
import dat.executeWith.ExecuteWithIF;
import dat.util.Colors;

import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.*;

public class Server implements Runnable, ExecuteWithIF, ChatIF //TODO: unit tests and integration tests
{
    
    public static final String COMMAND_START = "/";
    public static final String COMMAND_HELP = COMMAND_START + "help";
    public static final String COMMAND_EXIT = COMMAND_START + "exit";
    
    public static final String COMMAND_COMPUTER_START = Colors.RESET_ANSI;
    public static final String COMMAND_COMPUTER_MYNAME = COMMAND_COMPUTER_START + "myname";
    
    public static final int MESSAGE_QUEUE_SIZE_DEFAULT = 50;
    public static final int MAX_ACTIVE_CLIENTS_DEFAULT = 100; //_AND_MAX_THREADS_FOR_CLIENTS_DEFAULT
    public static final int MIN_SERVER_THREADS = 3;
    public static final int MIN_CONNECT_THREADS = 1;
    
    private int port;
    private String name;
    private final int messageQueueSize;
    private final int maxActiveClients;
    private final Scanner scanner;
    
    private ServerSocket serverSocket;
    
    private final BlockingQueue< Message > messageQueue;
    private final ConcurrentMap< String, ServerClient > clientMap = new ConcurrentHashMap<>();
    
    private final ArrayList< Future< ? > > serverThreads = new ArrayList<>();
    private final ArrayList< Future< ? > > clientsThreads = new ArrayList<>();
    private ExecutorService localExecutorService = null;
    private Message lastMessageReceived = null;
    
    
    
    
    
    
    
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
        this.localExecutorService = this.newLocalExecutor();
        
        this.executeWith( this.localExecutorService );
    }
    
    @Override
    public void executeWith( ExecutorService executorService )
    {
        
        
        try {
            this.setupSocket();
            
            Future< ? > threadConnect = executorService.submit( () -> this.connect( executorService ) ); //I hate that we are throwing the service down the method hirachy TODO: Don't do that
            Future< ? > threadBroadcaster = executorService.submit( () -> this.sendMessagesFromQueue() );
            
            Future< ? > threadServerConsole = executorService.submit( () -> this.serverConsole() );
            
            this.serverThreads.add( threadConnect );
            this.serverThreads.add( threadBroadcaster );
            this.serverThreads.add( threadServerConsole );
            
        } catch ( BindException e ) {
            System.err.println("SERVER: EXCEPTION BIND: Never even got to connect");
            e.printStackTrace();
            this.close();
            
        } catch ( SocketException e ) {
            System.err.println("SERVER: EXCEPTION SOCKET: Close down maybe?");
            e.printStackTrace();
            
        } catch ( IOException e ) {
            System.err.println("SERVER: EXCEPTION IO: Close down maybe?");
            e.printStackTrace();
        }
        //Then go die
    }
    
    private ExecutorService newLocalExecutor()
    {
        int totalThreads = MIN_SERVER_THREADS + this.maxActiveClients;
        
        System.out.println( "SERVER: Starting local executor service with " + totalThreads + " Threads!" );
        
        return Executors.newFixedThreadPool( totalThreads );
    }
    
    
    
    
    
    
    
    //Setup, Connect, Send, Console--------------------------------------------------------------------------------------------------
    public void setupSocket() throws IOException  //ServerSocket
    {
        this.serverSocket = new ServerSocket( this.port );
    }
    
    @Override
    public boolean connect() //AKA run(); if not running
    {
        if ( !this.serverSocket.isBound() || this.serverSocket.isClosed() ) {
            this.run();
            return true;
        }
        
        System.out.println( "SERVER: Server is already connecting!" );
        return false;
    }
    
    public boolean connect( ExecutorService executorService )  //.accept() forever....      //TODO: ExecutorService executorService <- I hate this, fix it!
    {
        boolean hadNoErrors = true;
        
        do {
            
            try {
                Socket clientSocket = this.serverSocket.accept();  // blocking call
                
                executorService.submit( () -> this.addClient( clientSocket, executorService ) ); //We wanna listen again ASAP, so some other thread can finish this
                
            } catch ( IOException e ) {
                System.out.println( "SERVER: EXCEPTION IO: On .accept()" );
                e.printStackTrace();
                hadNoErrors = false;
            }
            
        } while ( !this.serverSocket.isClosed() );
        
        System.out.println( "SERVER: Stopping Server Connect Thread" );
        return hadNoErrors;
    }
    
    private void sendMessagesFromQueue() //SEND ALL THE MESSAGES WE RECEIVE----------------
    {
        try {
            
            Message message = new Message( "Server started", this.name, Message.ALL );
            this.sendMessage( message );
            
            
            while ( !Thread.currentThread().isInterrupted() ) {
                
                message = this.messageQueue.take();
                
                this.lastMessageReceived = message;
                
                if ( !message.message().isEmpty() ) {
                    this.sendMessage( message ); //TODO: send only to recipients
                }
                
            }
            
        } catch ( InterruptedException e ) {
            System.err.println( "SERVER: was interrupted: " + e.getMessage() );
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        
        System.out.println( "SERVER: Stopping Server Broadcast Thread" );
    }
    
    @Override
    public boolean sendMessage( Message message ) //SEND A SINGLE MESSAGE-----------------------
    {
        boolean hadNoErrors = true;
        
        System.out.println( message.toString() );
        
        for ( ServerClient serverClient : this.clientMap.values() ) {
            
            try {
                
                if ( serverClient != null ) {
                    serverClient.sendMessage( message );
                }
                
            } catch ( Exception e ) {
                System.out.println( "SERVER: EXCEPTION: On sendMessage" );
                e.printStackTrace();
                hadNoErrors = false;
            }
        }
        return hadNoErrors;
    }
    
    @Override
    public Message receiveMessage()
    {
        return this.lastMessageReceived;
    }
    
    public void serverConsole()
    {
        String inputLine;
        Message message;
        
        // send messages to server. The ressources will be close by receiver
        while ( ( inputLine = this.scanner.nextLine() ) != null ) {
            
            if ( !inputLine.isEmpty() ) {
                
                if ( !isCommand( inputLine ) ) {
                    message = new Message( inputLine, this.name, Message.ALL ); //TODO: select recipient
                    this.messageQueue.add( message );
                    
                } else {
                    this.runCommand( inputLine );
                }
                
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
            
            this.clientConnected( serverClient );
            
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
            Message message;
            
            do {
                message = serverClient.receiveMessage();
                
                if ( message != null ) {
                    Server.this.messageQueue.add( message );
                }
                
            } while ( serverClient.isRunning() && message != null );
            
        } catch ( SocketException e ) {
            System.err.println( "SERVER - SERVERCLIENT: EXCEPTION SOCKET: on listenToClient: " );
            e.printStackTrace();
            
        } catch ( IOException e ) {
            System.err.println( "SERVER - SERVERCLIENT: EXCEPTION IO: on listenToClient: " );
            e.printStackTrace();
            
        } finally {
            serverClient.close();
            this.clientDisconnected( serverClient );
            Server.this.clientMap.remove( serverClient.toString() );
        }
        
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
        Message message = new Message( Colors.BLUE_ANSI + serverClient.getName() + Colors.RESET_ANSI + " has connected.", this.name, Message.ALL );
        
        this.sendMessage( message );
    }
    
    private void clientDisconnected( ServerClient serverClient )
    {
        Message message = new Message( Colors.BLUE_ANSI + serverClient.getName() + Colors.RESET_ANSI + " has disconnected.", this.name, Message.ALL );
        
        this.sendMessage( message );
    }
    
    
    
    
    
    //ServerConsoleCommands-------------------------------------------------------------------------------------------------------- TODO: Separate class maybe, shared with client commands?
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
    @Override
    public synchronized void close()   //TODO: don't spam the console with repeat and errors when closing
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
            
            System.out.println( "Amount of clients at shutdown: " + this.clientMap.size() );
            
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
            
            if ( !Thread.currentThread().equals( serverThread ) ) {
                serverThread.cancel( true );
            }
            
        }
        this.serverThreads.clear();
    }
    
    private void threadsClientsClose()
    {
        for ( Future< ? > clientsThread : this.clientsThreads ) {
            
            if ( !Thread.currentThread().equals( clientsThread ) ) {
                clientsThread.cancel( true );
            }
            
        }
        this.clientsThreads.clear();
    }
    
    
    //Unique Getters------------------------------------------------------------------------------------
    public boolean isRunning()
    {
        if ( this.serverSocket.isBound() && !this.serverSocket.isClosed() ) {
            return true;
        }
        
        return false;
    }
    
    
    
    
    
    
    //Getters and Setters------------------------------------------------------------------------------------
    @Override
    public String getName()
    {
        return this.name;
    }
    
    @Override
    public void setName( String name )
    {
        this.name = ChatIF.setName( this, name );
    }
    
    @Override
    public int getPort()
    {
        return this.port;
    }
    
    @Override
    public void setPort( int port )  //TODO: Check this is a valid port
    {
        if ( port < 0 ) {
            return;
        }
        
        this.port = port;
        
        System.out.println( "SERVER: Port updated, remember to re-host for changes to take effect!" );
    }
    
    @Override
    public String getIp() //WAN IP
    {
        return String.valueOf( this.serverSocket.getInetAddress() );
    }
    
    @Override
    public void setIp( String ip )
    {
        System.out.println( "SERVER: You cannot set the IP for a server, but nice try!" );
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
