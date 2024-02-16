package dat.serverAndClient.client;

import dat.serverAndClient.ChatIF;
import dat.serverAndClient.Message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;


public class Client implements ChatIF //TODO: unit tests and integration tests
{
    
    public static final String TYPE_SERVERBITCH = "CONNECTED CLIENT";
    public static final String TYPE_INDEPEDENTCLIENT = "CLIENT";
    
    protected final Socket clientSocket;
    
    private PrintWriter outputStream = null;
    private BufferedReader inputStream = null;
    
    private String lastInput;
    private String name;
    
    protected final String type;
    
    
    
    //Constructor---------------------------------------------------------------------------
    public Client( Socket clientSocket, String type, String name )
    {
        this.clientSocket = Objects.requireNonNull( clientSocket );
        
        this.type = Objects.requireNonNullElse( type, TYPE_INDEPEDENTCLIENT );
        this.name = Objects.requireNonNullElse( name, this.type );
    }
    
    public Client( Socket clientSocket )
    {
        this( clientSocket, TYPE_SERVERBITCH, TYPE_SERVERBITCH );
    }
    
    
    
    
    @Override
    public boolean connect() throws IOException
    {
        this.outputStream = new PrintWriter( this.clientSocket.getOutputStream(), true );
        this.inputStream = new BufferedReader( new InputStreamReader( this.clientSocket.getInputStream() ) );
        
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
        System.out.println( this.type + ": Closing down socket and streams..." );
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
            System.err.println( this.type + ": EXCEPTION IO: Failed to close down socket and streams! " );
            e.printStackTrace();
        }
        
        System.out.println( this.type + ": Closing down socket and streams... FINISHED!" );
    }
    
    
    //Getters Only-------------------------------------
    @Override
    public boolean isRunning()
    {
        if ( this.clientSocket.isBound() && this.clientSocket.isConnected() && !this.clientSocket.isClosed() ) {
            return true;
        }
        return false;
    }
    
    public String getLastInput()
    {
        return this.lastInput;
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
        this.setName( name,true );
    }
    
    public void setName( String name, boolean shouldBroadcast )
    {
        this.name = ChatIF.setName( this, name, shouldBroadcast );
    }
    
    @Override
    public int getPort()
    {
        return this.clientSocket.getPort();
    }
    
    @Override
    public void setPort( int port ) //TODO: Check this is a valid port
    {
        System.out.println( "BASE-CLIENT: PORT cannot be set for base client, overwrite setPort method!" );
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
        System.out.println( "BASE-CLIENT: IP cannot be set for base client, overwrite setIp method!" );
        return;
    }
    
}
