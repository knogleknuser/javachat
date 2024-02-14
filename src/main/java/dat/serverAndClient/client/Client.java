package dat.serverAndClient.client;

import dat.serverAndClient.Message;
import dat.executeWith.ExecuteWithIF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Client implements Runnable, ExecuteWithIF
{
    
    public static final int THREADS_MINIMUM = 2;
    
    private final int PORT;
    private final String IP;
    private final Scanner scanner;
    private final String name;
    
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    
    
    public Client( String IP, int PORT, String name, Scanner scanner )
    {
        this.IP = IP;
        this.PORT = PORT;
        this.name = name;
        this.scanner = scanner;
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
    
    @Override
    public void run()
    {
        ExecutorService executorService = Executors.newFixedThreadPool( THREADS_MINIMUM );
        
        this.executeWith( executorService );
    }
    
    @Override
    public void executeWith( ExecutorService executorService )
    {
        this.connect();
        executorService.submit( () -> this.receiveMessage() );
        executorService.submit( () -> this.sendMessage() );
        //And then go die
    }
    
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
        System.out.println( "Send messages to chat server:" );
        
        try {
            String lineToSend;
            Message message;
            
            // send messages to server. The ressources will be close by receiver
            while ( ( lineToSend = this.scanner.nextLine() ) != null ) {
                message = new Message( lineToSend, this.name, "all" );
                this.outputStream.println( message );
            }
            
        } catch ( Exception e ) {
            System.err.println( "Error in sendMessages: " + e.getMessage() );
            
        } finally {
            this.closeResources();
        }
    }
    
    
    public void receiveMessage()
    {
        System.out.println( "Message Receiver started." );
        try {
            String recievedLine;
            
            while ( ( recievedLine = this.inputStream.readLine() ) != null ) {
                
                System.out.println(recievedLine  );
                
                if ( "exit".equals( recievedLine ) ) {
                    System.out.println( "Client sent 'exit' - closing connection." );
                    break; // Break out of the loop once "exit" is received
                }
                
            }
            
        } catch ( IOException e ) {
            System.err.println( "Error in Client Receive Message: " + e.getMessage() );
            
        } finally {
            this.closeResources();
        }
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
            System.err.println( "Failed to close resources: " + e.getMessage() );
        }
    }
    
    
    
}
