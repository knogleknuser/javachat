package dat.serverAndClient.client;

import dat.serverAndClient.ChatIF;
import dat.serverAndClient.Message;
import dat.serverAndClient.server.Server;
import dat.executeWith.ExecuteWithIF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Client implements Runnable, ExecuteWithIF, ChatIF //TODO: unit tests and integration tests
{
    
    public static final int THREADS_MINIMUM = 2;
    
    private int port;
    private String ip;
    private String name;
    private final Scanner scanner;
    
    private final Socket clientSocket = new Socket();
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    
    private String lastInput = null;
    
    private final ArrayList< Future< ? > > clientThreads = new ArrayList<>();
    private ExecutorService localExecutorService = null;
    
    
    
    
    
    
    
    //Constructors-------------------------------------------------------------------------------
    public Client( String ip, int port, String name, Scanner scanner )
    {
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.scanner = scanner; //For making it testable
    }
    
    public Client( String ip, int port, String name )
    {
        this(
                ip,
                port,
                name,
                new Scanner( System.in )
        );
    }
    
    
    
    //Run Client--------------------------------------------------------------------------------
    @Override
    public void run()
    {
        this.localExecutorService = Executors.newFixedThreadPool( THREADS_MINIMUM );
        
        this.executeWith( this.localExecutorService );
    }
    
    @Override
    public void executeWith( ExecutorService executorService )
    {
        try {
            this.connect();
            
            Future< ? > threadSender = executorService.submit( () -> this.sendMessageUI() );
            Future< ? > threadReceiver = executorService.submit( () -> this.receiveMessageUI() );
            
            this.clientThreads.add( threadSender );
            this.clientThreads.add( threadReceiver );
            
        } catch ( IOException e ) {
            System.out.println( "CLIENT: IO EXCEPTION ON CONNECT!" );
            e.printStackTrace();
        }
        
        //And then go die
    }
    
    
    
    //Connect, Send, Recieve------------------------------------------------------------------------------------------------------
    @Override
    public boolean connect() throws IOException
    {
        
        this.clientSocket.connect( new InetSocketAddress( this.ip, this.port ) );
        
        this.outputStream = new PrintWriter( this.clientSocket.getOutputStream(), true );
        this.inputStream = new BufferedReader( new InputStreamReader( this.clientSocket.getInputStream() ) );
        
        
        return true;
    }
    
    public void sendMessageUI()
    {
        System.out.println( "CLIENT: Message Sender started" );
        
        try {
            this.firstConnectedMessages();
            
            String inputLine;
            Message message;
            
            //Send messages to server.
            while ( ( inputLine = this.scanner.nextLine() ) != null ) {
                
                if ( !Server.isCommand( inputLine ) ) {
                    message = new Message( inputLine, this.name, Message.ALL );
                    this.sendMessage( message );
                    
                } else {
                    this.runCommand( inputLine );
                }
                
            }
            
        } catch ( Exception e ) {
            System.err.println( "CLIENT: EXCEPTION: Error in sendMessages: " );
            e.printStackTrace();
            
        } finally {
            this.close();
        }
        System.out.println( "CLIENT: Message Sender shutdown" );
    }
    
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
    
    private void firstConnectedMessages()
    {
        //Connected! now send name!
        Message message = new Message( Server.COMMAND_COMPUTER_MYNAME, this.name, Message.ALL );  //TODO: select recipient
        this.outputStream.println( message );
        
        //Other firstConnectedMessages!
        //Currently, none!
    }
    
    public void receiveMessageUI()
    {
        
        System.out.println( "CLIENT: Message Receiver started" );
        try {
            //START of actual code that receives messages ----------------------------------
            Message message;
            
            do {
                message = this.receiveMessage();
                
                if ( message != null ) {
                    System.out.println( message.toString() );
                }
                
            } while ( this.isRunning() && message != null );
            //END of actual code that receives messages ----------------------------------
            
        } catch ( SocketException e ) {
            System.err.println( "CLIENT: EXCEPTION SOCKET: Receive Message: " );
            e.printStackTrace();
            
        } catch ( IOException e ) {
            System.err.println( "CLIENT: EXCEPTION IO: Receive Message: " );
            e.printStackTrace();
            
        } finally {
            this.close();
        }
        System.out.println( "CLIENT: Message Receiver shutdown" );
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
    
    
    
    
    //Close----------------------------------------------------------------------------------
    @Override
    public synchronized void close()  //TODO: don't spam the console with repeat prints and errors when closing
    {
        System.out.println( "CLIENT: Closing down..." );
        try {
            
            if ( this.outputStream != null ) {
                this.outputStream.close();
            }
            
            if ( this.inputStream != null ) {
                this.inputStream.close();
            }
            
            if ( this.localExecutorService != null ) {
                this.localExecutorService.shutdownNow();
                this.localExecutorService = null;
            }
            
            this.threadsServerClose();
            
            this.scanner.close();
            
        } catch ( IOException e ) {
            System.err.println( "CLIENT: EXCEPTION IO: Failed to close resources: " );
            e.printStackTrace();
            return;
        }
        
        System.out.println( "CLIENT: Done Closing" );
    }
    
    private void threadsServerClose()
    {
        for ( Future< ? > serverThread : this.clientThreads ) {
            
            if ( !Thread.currentThread().equals( serverThread ) ) {
                serverThread.cancel( true );
            }
            
        }
        this.clientThreads.clear();
    }
    
    
    
    
    //Commands--------------------------------------------------------------------------------------  TODO: Separate class maybe, shared with client commands?
    private void runCommand( String inputLine )
    {
        if ( !Server.isCommand( inputLine ) ) {
            System.err.println( "ERROR: CLIENT-CONSOLE THOUGHT NON-COMMAND WAS A COMMAND?" );
        }
        
        switch ( inputLine ) {
            
            case Server.COMMAND_HELP:
                Server.printCommandHelp();
                return;
            
            case Server.COMMAND_EXIT:  //TODO: tell server we are leaving
                this.close();
                return;
            
            default:
                System.out.println( "\"" + inputLine + "\" is not a recognized command" );
                return;
            
        }
    }
    
    
    
    
    public boolean isRunning()
    {
        if ( this.clientSocket.isBound() && this.clientSocket.isConnected() && !this.clientSocket.isClosed() ) {
            return true;
        }
        return false;
    }
    
    //Getters and Setters--------------------------------------------------------------------------------------------
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
        return this.port;
    }
    
    @Override
    public void setPort( int port )//TODO: Check this is a valid port
    {
        if ( port < 0 ) {
            return;
        }
        
        this.port = port;
        System.out.println( "CLIENT: port set, remember to reconnect!" );
        return;
    }
    
    @Override
    public String getIp()
    {
        return this.ip;
    }
    
    @Override
    public void setIp( String ip )  //TODO: Check this is a valid ip
    {
        this.ip = ip;
        
        System.out.println( "CLIENT:ip set, remember to reconnect!" );
        return;
    }
    
    
    
}
