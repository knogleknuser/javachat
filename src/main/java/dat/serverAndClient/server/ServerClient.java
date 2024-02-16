package dat.serverAndClient.server;

import dat.serverAndClient.ChatIF;
import dat.serverAndClient.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;


public class ServerClient implements ChatIF //TODO: unit tests and integration tests
{
    
    private final Socket clientSocket;
    
    private final PrintWriter outputStream;
    private final BufferedReader inputStream;
    
    private String lastInput;
    private String name;
    private int port;
    private String ip;
    
    
    
    //Constructor---------------------------------------------------------------------------
    public ServerClient( Socket clientSocket ) throws IOException
    {
        this.clientSocket = clientSocket;
        
        this.outputStream = new PrintWriter( clientSocket.getOutputStream(), true );
        this.inputStream = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
        
        //Info
        this.port = clientSocket.getPort();
        this.ip = String.valueOf( clientSocket.getInetAddress() );
    }
    
    
    
    
    @Override
    public boolean connect() throws IOException
    {
        if ( this.isRunning() ) {
            return false;
        }
        
        this.clientSocket.connect( new InetSocketAddress( this.ip, this.port ) );
        return true;
    }
    
    //Send, Receive, Close-----------------------------------------------------------------------
    @Override
    public boolean sendMessage( Message message )
    {
        if ( !this.isRunning() ) {
            this.close();
            return false;
        }
        
        this.outputStream.println( message.toString() );
        return true;
    }
    
    @Override
    public Message receiveMessage() throws IOException
    {
        if ( !this.isRunning() ) {
            this.close();
            return null;
        }
        
        String rawMessage = this.inputStream.readLine();
        
        this.lastInput = rawMessage;
        
        return Message.createMessage( rawMessage );
    }
    
    @Override
    public synchronized void close()   //TODO: don't spam the console with repeat and errors when closing
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
        if ( this.clientSocket.isBound() && this.clientSocket.isConnected() && !this.clientSocket.isClosed() ) {
            return true;
        }
        return false;
    }
    
    
    
    //Getters and Setters----------------------------------------------------------
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
        return this.clientSocket.getPort();
    }
    
    @Override
    public void setPort( int port ) //TODO: Check this is a valid port
    {
        if ( port < 0 ) {
            return;
        }
        
        this.port = port;
        System.out.println("SERVERCLIENT: port set, remember to reconnect!");
        return;
    }
    
    @Override
    public String getIp()
    {
        return String.valueOf( this.clientSocket.getInetAddress() );
    }
    
    @Override
    public void setIp( String ip )  //TODO: Check this is a valid ip
    {
        this.ip = ip;
        
        System.out.println("SERVERCLIENT:ip set, remember to reconnect!");
        return;
    }
    
}
