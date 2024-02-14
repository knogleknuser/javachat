package dat.serverAndClient.server;

import dat.serverAndClient.Message;
import dat.executeWith.ExecuteWithIF;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.*;

public class Server implements Runnable, ExecuteWithIF
{
    
    public static final int QUEUE_MAX_MESSAGES = 50;
    public static final int THREADS_MAX_FOR_CHAT_MEMBERS = 100;
    public static final int THREADS_MINIMUM_FOR_SERVER = 3;
    
    private final int port;
    private final String name;
    private final Scanner scanner;
    
    private ServerSocket serverSocket;
    
    private final ConcurrentMap< String, ServerClient > clientMap = new ConcurrentHashMap<>();
    private final BlockingQueue< Message > messageQueue = new ArrayBlockingQueue<>( QUEUE_MAX_MESSAGES );
    
    private final ServerClientListener serverClientListener = new ServerClientListener( THREADS_MAX_FOR_CHAT_MEMBERS );
    
    
    
    public Server( int port, String name, Scanner scanner )
    {
        this.port = port;
        this.name = name;
        this.scanner = scanner; //For making it testable
    }
    
    public Server( int port, String name )
    {
        this(
                port,
                name,
                new Scanner( System.in )
        );
    }
    
    @Override
    public void run()
    {
        ExecutorService executorService = Executors.newFixedThreadPool( THREADS_MINIMUM_FOR_SERVER );
        
        this.executeWith( executorService );
    }
    
    @Override
    public void executeWith( ExecutorService executorService )
    {
        
        
        try {
            this.setupSocket();
            
            executorService.submit( () -> this.connect() );
            executorService.submit( () -> this.broadcastMessages() );
            
            executorService.submit( () -> this.serverConsole() );
            
        } catch ( IOException e ) {
            this.close();
            throw new RuntimeException( e );
        }
        //Then go die
    }
    
    public void setupSocket() throws IOException
    {
        this.serverSocket = new ServerSocket( this.port );
    }
    
    public void connect()
    {
        do {
            
            try {
                Socket clientSocket = this.serverSocket.accept();  // blocking call
                
                this.addClient( clientSocket );
                
            } catch ( IOException e ) {
                System.out.println( "SERVER: EXCEPTION IO: On .accept()" );
                e.printStackTrace();
            }
            
        } while ( !this.serverSocket.isClosed() );
        
        System.out.println( "SERVER: Stopping Server Connect Thread" );
    }
    
    private void broadcastMessages()
    {
        try {
            
            Message message = new Message( "Server started", this.name, "all" );
            this.broadcastMessage( message.toString() );
            
            
            while ( !Thread.currentThread().isInterrupted() ) {
                
                message = this.messageQueue.take();
                
                this.broadcastMessage( message.toString() );
                
            }
            
        } catch ( InterruptedException e ) {
            System.err.println( "SERVER: was interrupted: " + e.getMessage() );
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        
        System.out.println( "SERVER: Stopping Server Broadcast Thread" );
    }
    
    public void serverConsole()
    {
        String inputLine;
        Message message;
        
        // send messages to server. The ressources will be close by receiver
        while ( ( inputLine = this.scanner.nextLine() ) != null ) {
            message = new Message( inputLine, this.name, "all" );
            this.messageQueue.add( message );
        }
        
        System.out.println( "SERVER: Stopping Server Console Thread" );
    }
    
    public void addClient( Socket clientSocket )
    {
        try {
            ServerClient serverClient = new ServerClient( clientSocket );
            Server.this.clientMap.put( serverClient.toString(), serverClient );
            
            this.serverClientListener.listenToClient( serverClient );
            
        } catch ( IOException e ) {
            System.out.println( "SERVER: IO EXCEPTION: On add Client" );
            e.printStackTrace();
        }
        
    }
    
    private void broadcastMessage( String message )
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
    
    public void close()
    {
        try {
            if ( this.serverClientListener != null  ) {
                this.serverClientListener.close();
            }
            
            if ( this.serverSocket != null  ) {
                this.serverSocket.close();
            }
            
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        
    }
    
    
    
    public class ServerClientListener
    {
        
        private final ExecutorService executorService;
        
        public ServerClientListener( int maxActiveChatMembers )
        {
            this.executorService = Executors.newFixedThreadPool( maxActiveChatMembers );
        }
        
        public void listenToClient( ServerClient serverClient )
        {
            this.executorService.submit( () -> this.receiveThisClientsMessages( serverClient ) );
        }
        
        private void receiveThisClientsMessages( ServerClient serverClient )
        {
            try {
                
                do {
                    serverClient.receiveMessage();
                    String rawMessage = serverClient.getLastInput();
                    
                    Message message = Message.createMessage( rawMessage );
                    
                    Server.this.messageQueue.add( message );
                    
                } while ( serverClient.isRunning() );
                
            } catch ( IOException e ) {
                Server.this.clientMap.remove( serverClient.toString() );
                throw new RuntimeException( e );
            }
        }
        
        public void close()
        {
            this.executorService.shutdownNow();
        }
        
    }
    
}
