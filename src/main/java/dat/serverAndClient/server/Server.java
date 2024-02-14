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
    
    private final String name;
    
    private final int port;
    private ServerSocket serverSocket;
    
    private final ConcurrentMap< String, ServerClient > clientMap = new ConcurrentHashMap<>();
    private final BlockingQueue< Message > messageQueue = new ArrayBlockingQueue<>( QUEUE_MAX_MESSAGES );
    
    private final ServerClientManager serverClientManager = new ServerClientManager( THREADS_MAX_FOR_CHAT_MEMBERS );
    
    
    
    public Server( int port, String name )
    {
        this.port = port;
        this.name = name;
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
        try {
            
            do {
                Socket clientSocket = this.serverSocket.accept();  // blocking call
                
                this.serverClientManager.addClient( clientSocket );
                
            } while ( !this.serverSocket.isClosed() );
            
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        
        System.out.println( "Stopping Server Connect Thread" );
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
            System.err.println( "Server was interrupted: " + e.getMessage() );
            Thread.currentThread().interrupt();
        }
        
        System.out.println( "Stopping Server Broadcast Thread" );
    }
    
    private void broadcastMessage( String message )
    {
        System.out.println( message );
        
        for ( ServerClient serverClient : this.clientMap.values() ) {
            serverClient.sendMessage( message );
        }
    }
    
    
    public void serverConsole()
    {
        Scanner scanner = new Scanner( System.in );
        
        String inputLine;
        Message message;
        
        // send messages to server. The ressources will be close by receiver
        while ( ( inputLine = scanner.nextLine() ) != null ) {
            message = new Message( inputLine, this.name, "all" );
            this.messageQueue.add( message );
        }
        
        System.out.println( "Stopping Server Console Thread" );
    }
    
    public void close()
    {
        try {
            this.serverClientManager.close();
            this.serverSocket.close();
            
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        
    }
    
    
    
    public class ServerClientManager
    {
        
        private final ExecutorService executorService;
        
        public ServerClientManager( int maxActiveChatMembers )
        {
            this.executorService = Executors.newFixedThreadPool( maxActiveChatMembers );
        }
        
        public void addClient( Socket clientSocket )
        {
            try {
                ServerClient serverClient = new ServerClient( clientSocket );
                Server.this.clientMap.put( serverClient.toString(), serverClient );
                
                this.executorService.submit( () -> this.receiveThisClientsMessages( serverClient ) );
                
            } catch ( IOException ignored ) {
            
            }
            
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
                throw new RuntimeException( e );
            }
        }
        
        public void close()
        {
            this.executorService.shutdownNow();
        }
        
    }
    
}
