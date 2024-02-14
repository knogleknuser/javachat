package dat.client;

import dat.commandPatterns.CommandClientReceive;
import dat.commandPatterns.CommandClientSend;
import dat.commandPatterns.OnlineReceiveIF;
import dat.commandPatterns.OnlineSendIF;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;

public class Client implements Runnable, OnlineSendIF, OnlineReceiveIF
{
    
    private final int PORT;
    private final String IP;
    
    private Socket clientSocket;
    private PrintWriter outputStream;
    private BufferedReader inputStream;
    private final ExecutorService executorService;
    
    public Client( String IP, int PORT, ExecutorService executorService )
    {
        this.IP = IP;
        this.PORT = PORT;
        this.executorService = executorService;
    }
    
    @Override
    public void run()
    {
        this.connect();
        this.executorService.submit( new CommandClientReceive( this ) );
        this.executorService.submit( new CommandClientSend( this ) );
        //And then go die
    }
    
    public void connect()
    {
        try {
            this.clientSocket = new Socket( this.IP, this.PORT );
            this.outputStream = new PrintWriter( this.clientSocket.getOutputStream(), true );
            this.inputStream = new BufferedReader( new InputStreamReader( this.clientSocket.getInputStream() ) );
            
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        }
        
    }
    
    @Override
    public void sendMessages()
    {
        System.out.println( "Send messages to chat server:" );
        Scanner scanner = new Scanner( System.in );
        
        try {
            String inputLine;
            
            // send messages to server. The ressources will be close by receiver
            while ( ( inputLine = scanner.nextLine() ) != null ) {
                this.outputStream.println( inputLine );
            }
            
        } catch ( Exception e ) {
            System.err.println( "Error in sendMessages: " + e.getMessage() );
            
        } finally {
            this.closeResources();
        }
    }
    @Override
    public void receiveMessage()
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
            
            if ( this.clientSocket != null ) {
                this.clientSocket.close();
            }
            
        } catch ( IOException e ) {
            System.err.println( "Failed to close resources: " + e.getMessage() );
        }
    }
    
}
