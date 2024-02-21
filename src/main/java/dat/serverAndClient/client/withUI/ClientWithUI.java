package dat.serverAndClient.client.withUI;

import dat.serverAndClient.ConsoleCommands;
import dat.serverAndClient.Message;
import dat.executeWith.ExecuteWithIF;
import dat.serverAndClient.client.Client;
import dat.util.ThreadsUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class ClientWithUI extends Client implements Runnable, ExecuteWithIF //TODO: unit tests and integration tests
{
    
    public static final int THREADS_MINIMUM = 2;
    
    private int port;
    private String ip;
    private String name;
    private final Scanner scanner;
    
    
    private String lastInput = null;
    
    private final ArrayList< Future< ? > > clientThreads = new ArrayList<>();
    private ExecutorService localExecutorService = null;
    
    
    
    
    
    
    
    //Constructors-------------------------------------------------------------------------------
    public ClientWithUI( String ip, int port, String name, Scanner scanner )
    {
        super( new Socket(), TYPE_INDEPEDENTCLIENT, name );
        
        this.ip = ip;
        this.port = port;
        this.name = name;
        this.scanner = scanner; //For making it testable
    }
    
    public ClientWithUI( String ip, int port, String name )
    {
        this(
                ip,
                port,
                name,
                new Scanner( System.in )
        );
    }
    
    
    
    //Run ClientWithUI--------------------------------------------------------------------------------
    @Override
    public void run()
    {
        this.localExecutorService = Executors.newFixedThreadPool( THREADS_MINIMUM );
        
        this.executeWith( this.localExecutorService );
    }
    
    @Override
    public void executeWith( ExecutorService executorService )
    {
        System.out.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - Starting threads ..." );
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
        System.out.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - Starting threads ... FINISHED!" );
        //And then go die
    }
    
    
    
    //Connect, Send, Recieve------------------------------------------------------------------------------------------------------
    @Override
    public boolean connect() throws IOException
    {
        System.out.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - Connecting to IP:PORT: " + this.ip + ":" + this.port + " ..." );
        this.clientSocket.connect( new InetSocketAddress( this.ip, this.port ) );
        
        super.connect();
        
        System.out.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - Successfully connected! to " + this.ip + ":" + this.port + " ... FINISHED!" );
        return true;
    }
    
    public void sendMessageUI()
    {
        System.out.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - Message Sender ... started" );
        
        try {
            this.firstConnectedMessages();
            
            String inputLine;
            Message message;
            
            //Send messages to server.
            while ( ( inputLine = this.scanner.nextLine() ) != null ) {
                
                if ( !ConsoleCommands.isCommand( inputLine ) ) {
                    message = new Message( inputLine, this.name, Message.ALL );
                    super.sendMessage( message );
                    
                } else {
                    ConsoleCommands.runCommand( inputLine, this );
                }
                
            }
            
        } catch ( Exception e ) {
            System.err.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - EXCEPTION: Error in sendMessages: " );
            e.printStackTrace();
            
        } finally {
            this.close();
        }
        System.out.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - Message Sender ... shutdown" );
    }
    
    private void firstConnectedMessages()
    {
        //Connected! now send name!
        Message message = new Message( ConsoleCommands.COMMAND_COMPUTER_MYNAME, this.name, Message.ALL );  //TODO: select recipient
        super.sendMessage( message );
        
        //Other firstConnectedMessages!
        //Currently, none!
    }
    
    public void receiveMessageUI()
    {
        
        System.out.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - Message Receiver ... started" );
        try {
            //START of actual code that receives messages ----------------------------------
            Message message;
            
            do {
                message = super.receiveMessage();
                
                if ( message != null ) {
                    System.out.println( message.toString() );
                }
                
            } while ( this.isRunning() && message != null );
            //END of actual code that receives messages ----------------------------------
            
        } catch ( SocketException e ) {
            System.err.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - EXCEPTION SOCKET: Receive Message: " );
            e.printStackTrace();
            
        } catch ( IOException e ) {
            System.err.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - EXCEPTION IO: Receive Message: " );
            e.printStackTrace();
            
        } finally {
            this.close();
        }
        System.out.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - Message Receiver ... shutdown" );
    }
    
    
    
    
    //Close----------------------------------------------------------------------------------
    @Override
    public synchronized void close()  //TODO: don't spam the console with repeat prints and errors when closing
    {
        System.out.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - Closing down localExecutorService, threads and scanner ..." );
        
        super.close();
        
        if ( this.localExecutorService != null ) {
            this.localExecutorService.shutdownNow();
            this.localExecutorService = null;
        }
        
        ThreadsUtil.closeThreads( this.clientThreads );
        
        this.scanner.close();
        
        System.out.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - Closing down localExecutorService, threads and scanner ... FINISHED!" );
    }
    
    
    
    //Getters and Setters--------------------------------------------------------------------------------------------
    
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
        System.out.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - PORT set, remember to reconnect!" );
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
        
        System.out.println( ConsoleCommands.consolePrefix( this.type, this.name ) + " - IP set, remember to reconnect!" );
        return;
    }
    
    
    
}
