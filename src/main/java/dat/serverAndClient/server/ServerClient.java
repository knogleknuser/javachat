package dat.serverAndClient.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ServerClient
{
    
    private final Socket clientSocket;
    
    private final PrintWriter outputStream;
    private final BufferedReader inputStream;
    
    private String lastInput;
    
    public ServerClient( Socket clientSocket ) throws IOException
    {
        this.clientSocket = clientSocket;
        
        this.outputStream = new PrintWriter( clientSocket.getOutputStream(), true );
        this.inputStream = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
    }
    
    
    
    public void sendMessage( String message )
    {
        this.outputStream.println( message );
    }
    
    public void receiveMessage() throws IOException
    {
        this.lastInput = this.inputStream.readLine();
    }
    
    private void closeResources()
    {
        try {
            System.out.println( "Closing connection and resources." );
            
            if ( this.outputStream != null ) {
                this.outputStream.close();
            }
            
            if ( this.inputStream != null ) {
                this.inputStream.close();
            }
            
        } catch ( IOException e ) {
            System.err.println( "SERVERCLIENT: EXCEPTION IO: Failed to close resources: " );
            e.printStackTrace();
        }
    }
    
    public String getLastInput()
    {
        return this.lastInput;
    }
    
    public boolean isRunning()
    {
        if ( this.clientSocket.isBound() && this.clientSocket.isConnected() && !this.clientSocket.isClosed() ) {
            return true;
        }
        return false;
    }
    
}
