package dat;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;

public class ClientConnector implements Runnable
{
    
    private final ServerSocket serverSocket;
    private final ExecutorService executorService;
    private final ConcurrentMap< String, ClientHandler > clientMap;
    private final BlockingQueue< Message > messageQueue;
    private boolean running = true;
    
    public ClientConnector( ServerSocket serverSocket, ConcurrentMap< String, ClientHandler > clientMap, BlockingQueue< Message > messageQueue, ExecutorService executorService )
    {
        this.serverSocket = serverSocket;
        this.clientMap = clientMap;
        this.messageQueue = messageQueue;
        this.executorService = executorService;
    }
    
    @Override
    public void run()
    {
        System.out.println( "Client Connector started." );
        try {
            do {
                
                Socket clientSocket = this.serverSocket.accept(); // blocking call
                ClientHandler clientHandler = new ClientHandler( clientSocket, this.clientMap, this.messageQueue );
                this.registerClient( clientHandler );
                this.executorService.submit( clientHandler );
                
            } while ( this.running );
            
        } catch ( Exception e ) {
            System.err.println( "Error in ClientConnector: " + e.getMessage() );
            
        } finally {
            this.executorService.shutdown();
        }
    }
    
    private void registerClient( ClientHandler clientHandler )
    {
        String clientInfo = clientHandler.toString();
        this.clientMap.put( clientInfo, clientHandler );
    }
    
    public void stop()
    {
        this.running = false;
    }
    
}
