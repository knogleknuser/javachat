package dat;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.*;

public class Server implements Runnable
{
    
    public static void main( String[] args )
    {
        ExecutorService executorService = Executors.newFixedThreadPool( 30 );
        
        System.out.println( "Starting ChatServer" );
        Server server = new Server( 9090, executorService );
        executorService.submit( server, "Server" );
        
    }
    
    private final int port;
    private final ExecutorService executorService;
    private final ConcurrentMap< String, ClientHandler > clientMap = new ConcurrentHashMap<>();
    private final BlockingQueue< Message > messageQueue = new ArrayBlockingQueue<>( 50 );
    private ClientConnector clientConnector;
    
    public Server( int port, ExecutorService executorService )
    {
        this.port = port;
        this.executorService = executorService;
    }
    
    @Override
    public void run()
    {
        try ( ServerSocket serverSocket = new ServerSocket( this.port ) ) {
            
            System.out.println( "Server started on port: " + this.port );
            
            this.clientConnector = new ClientConnector( serverSocket, this.clientMap, this.messageQueue, this.executorService );
            this.executorService.submit( this.clientConnector );
            
            Message message = new Message( "Server started", "Server", "all" );
            
            while ( !Thread.currentThread().isInterrupted() && !message.message().equals( "exit" ) ) {
                
                message = this.messageQueue.take();
                System.out.println( "Message received: " + message.message() );
                
                for ( ClientHandler clientHandler : this.clientMap.values() ) {
                    clientHandler.sendMessage( message.message() );
                }
                
            }
            
        } catch ( IOException e ) {
            System.err.println( "Server encountered an IOException: " + e.getMessage() );
            
        } catch ( InterruptedException e ) {
            System.err.println( "Server was interrupted: " + e.getMessage() );
            Thread.currentThread().interrupt();
            
        } finally {
            System.out.println( "Server shutting down." );
            this.clientConnector.stop();
            this.executorService.shutdownNow();
            // Close resources, if any, here
        }
        
    }
    
}
