package dat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentMap;

public class ClientHandler implements Runnable
{
    
    private final Socket clientSocket;
    private final int clientId;
    
    private BufferedReader clientInputStream;
    private PrintWriter clientOutputStream;
    
    private final ConcurrentMap< String, ClientHandler > clientMap;
    private final BlockingQueue< Message > messageQueue;
    
    public ClientHandler( Socket clientSocket, ConcurrentMap< String, ClientHandler > clientMap, BlockingQueue< Message > messageQueue )
    {
        this.clientMap = clientMap;
        this.messageQueue = messageQueue;
        this.clientSocket = clientSocket;
        this.clientId = clientSocket.getLocalPort();
    }
    
    @Override
    public void run()
    {
        try {
            this.clientInputStream = new BufferedReader( new InputStreamReader( this.clientSocket.getInputStream() ) );
            this.clientOutputStream = new PrintWriter( this.clientSocket.getOutputStream(), true );
            
            String inputLine;
            do {
                
                inputLine = this.clientInputStream.readLine();
                
                if ( "exit".equals( inputLine ) ) {
                    Message message = new Message( inputLine, this.toString(), "all" );
                    this.messageQueue.add( message );
                    System.out.println( "Good bye ... closing down" );
                    this.closeResources();
                    
                } else if ( inputLine != null ) {
                    Message message = new Message( inputLine, this.toString(), "all" );
                    System.out.println( "Adding to messsage queue in clienthandler: " + message.message() );
                    this.messageQueue.add( message );
                }
                
            } while ( inputLine != null && !inputLine.equals( "exit" ) );
            
        } catch ( IOException e ) {
            throw new RuntimeException( e );
            
        } finally {
            this.closeResources();
        }
        
    }
    
    private void closeResources()
    {
        try {
            System.out.println( "Closing connection and resources." );
            
            if ( this.clientInputStream != null ) {
                this.clientInputStream.close();
            }
            
            if ( this.clientOutputStream != null ) {
                this.clientOutputStream.close();
            }
            
            if ( this.clientSocket != null ) {
                this.clientSocket.close();
            }
            
        } catch ( IOException e ) {
            System.err.println( "Failed to close resources: " + e.getMessage() );
        }
    }
    
    public void addMessage( Message message )
    {
        this.messageQueue.add( message );
    }
    
    public void sendMessage( String message )
    {
        this.clientOutputStream.println( message );
    }
    
    public int getClientId()
    {
        return this.clientId;
    }
    
    public Socket getClientSocket()
    {
        return this.clientSocket;
    }
    
    @Override
    public String toString()
    {
        return this.clientSocket.getInetAddress().toString() + ":" + this.clientSocket.getPort();
    }
    
}
