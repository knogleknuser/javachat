package dat;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.Socket;

public class MessageReceiver implements Runnable
{
    
    private final Socket clientSocket;
    private final BufferedReader inputStream;
    
    public MessageReceiver( Socket clientSocket, BufferedReader inputStream )
    {
        this.clientSocket = clientSocket;
        this.inputStream = inputStream;
    }
    
    @Override
    public void run()
    {
        System.out.println( "Message Receiver started." );
        try {
            String inputLine;
            
            while ( ( inputLine = this.inputStream.readLine() ) != null ) {
                
                System.out.println( "From server message queue: " + inputLine );
                
                if ( "exit".equals( inputLine ) ) {
                    System.out.println( "Client sent 'exit' - closing connection." );
                    break; // Break out of the loop once "exit" is received
                }
                
            }
            
        } catch ( IOException e ) {
            System.err.println( "Error in MessageReceiver: " + e.getMessage() );
            
        } finally {
            this.closeResources();
        }
    }
    
    private void closeResources()
    {
        try {
            System.out.println( "Closing connection and resources." );
            
            if ( this.inputStream != null ) {
                this.inputStream.close();
            }
            
            if ( this.clientSocket != null ) {
                this.clientSocket.close();
            }
            
        } catch ( IOException e ) {
            System.err.println( "Failed to close resources: " + e.getMessage() );
        }
    }
    
}
