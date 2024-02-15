package dat.serverAndClient.client;

import dat.serverAndClient.Message;
import dat.serverAndClient.server.Server;
import dat.executeWith.ExecuteWithIF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class Client implements Runnable, ExecuteWithIF //TODO: unit tests and integration tests
{
    
    public static final int THREADS_MINIMUM = 2;
    
    private final int PORT;
    private final String IP;
    private final Scanner scanner;
    private final String name;
    
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    
    private final ArrayList< Future< ? > > clientThreads = new ArrayList<>();
    private ExecutorService localExecutorService = null;
    
    
    
    
    
    //Constructors-------------------------------------------------------------------------------
    public Client( String IP, int PORT, String name, Scanner scanner )
    {
        this.IP = IP;
        this.PORT = PORT;
        this.name = name;
        this.scanner = scanner; //For making it testable
    }
    
    public Client( String IP, int PORT, String name )
    {
        this(
                IP,
                PORT,
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
        this.connect();
        Future< ? > threadSender = executorService.submit( () -> this.sendMessage() );
        Future< ? > threadReceiver = executorService.submit( () -> this.receiveMessage() );
        
        this.clientThreads.add( threadSender );
        this.clientThreads.add( threadReceiver );
        
        //And then go die
    }
    
    
    
    //Connect, Send, Recieve------------------------------------------------------------------------------------------------------
    public void connect()
    {
        try {
            Socket clientSocket = new Socket( this.IP, this.PORT );
            this.outputStream = new PrintWriter( clientSocket.getOutputStream(), true );
            this.inputStream = new BufferedReader( new InputStreamReader( clientSocket.getInputStream() ) );
            
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        
    }
    
    
    public void sendMessage()
    {
        System.out.println( "CLIENT: Message Sender started" );
        
        try {
            this.firstConnectedMessages();
            
            String inputLine;
            Message message;
            
            // send messages to server. The ressources will be close by receiver
            while ( ( inputLine = this.scanner.nextLine() ) != null ) {
                
                if ( !Server.isCommand( inputLine ) ) {
                    message = new Message( inputLine, this.name, Message.ALL );
                    this.outputStream.println( message );
                    
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
    
    private void firstConnectedMessages()
    {
        //Connected, send name!
        Message message = new Message( Server.COMMAND_COMPUTER_MYNAME, this.name, Message.ALL );  //TODO: select recipient
        this.outputStream.println( message );
    }
    
    
    public void receiveMessage()
    {
        System.out.println( "CLIENT: Message Receiver started" );
        try {
            String recievedLine;
            
            while ( ( recievedLine = this.inputStream.readLine() ) != null ) {
                System.out.println( recievedLine );
            }
            
        } catch ( SocketException e ) {
            System.err.println( "CLIENT: EXCEPTION SOCKET: Receive Message: " );
            e.printStackTrace();
            
        } catch ( IOException e ) {
            System.err.println( "CLIENT: EXCEPTION IO: Receive Message: " );
            e.printStackTrace();
            
        } finally {
            this.close();
        }
        System.out.println("CLIENT: Message Receiver shutdown");
    }
    
    
    
    
    //Close----------------------------------------------------------------------------------
    public void close()  //TODO: don't spam the console with repeat and errors when closing
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
            serverThread.cancel( true );
        }
        this.clientThreads.clear();
    }
    
    
    
    
    //Commands--------------------------------------------------------------------------------------
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
    
    
}
