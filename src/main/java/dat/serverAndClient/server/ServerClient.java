package dat.serverAndClient.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ServerClient //TODO: unit tests and integration tests
{
    
    private final Socket clientSocket;
    
    private final PrintWriter outputStream;
    private final BufferedReader inputStream;
    
    private String lastInput;
    private String name;
    
    
    
    //Constructor---------------------------------------------------------------------------
    public ServerClient( Socket clientSocket ) throws IOException
    {
        this.clientSocket = clientSocket;
        
        this.outputStream = new PrintWriter( clientSocket.getOutputStream(), true );
        this.inputStream = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
    }
    
    
    
    
    //Send, Receive, Close-----------------------------------------------------------------------
    public void sendMessage( String message )
    {
        if ( !this.isRunning() ) {
            this.close();
        }
        
        this.outputStream.println( message );
        
    }
    
    public void receiveMessage() throws IOException
    {
        if ( !this.isRunning() ) {
            this.close();
        }
        
        this.lastInput = this.inputStream.readLine();
    }
    
    public void close()   //TODO: don't spam the console with repeat and errors when closing
    {
        System.out.println( "SERVERCLIENT: Closing down..." );
        try {
            
            if ( this.outputStream != null ) {
                this.outputStream.close();
            }
            
            if ( this.inputStream != null ) {
                this.inputStream.close();
            }
            
            if ( this.clientSocket != null ) {
                this.clientSocket.close();
            }
            
        } catch ( IOException e ) {
            System.err.println( "SERVERCLIENT: EXCEPTION IO: Failed to close resources: " );
            e.printStackTrace();
        }
        
        System.out.println( "SERVERCLIENT: Finished shutdown" );
    }
    
    
    
    //Getters------------------------------------------------------------------
    public String getLastInput()
    {
        return this.lastInput;
    }
    
    public boolean isRunning()
    {
        if ( this.clientSocket.isBound() && this.clientSocket.isConnected() && !this.clientSocket.isClosed() ) { //TODO: make this work, cannot currently detect clients who have disconnected. Will also free up the thread, currently it is lost forever(until server restart)
            return true;
        }
        return false;
    }
    
    
    
    //Set and Get Name----------------------------------------------------------
    public void setName( String name )
    {
        this.name = name;
    }
    
    public String getName()
    {
        return this.name;
    }
    

}
