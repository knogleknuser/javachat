package dat.serverAndClient.server;

import jdk.jfr.Label;
import jdk.jfr.Name;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static dat.util.ConnectionMaster.PORT_DEFAULT;
//@Name( "SERVER" )
//@Label( "SERVER" )
public class Main
{
    
    private static final String NAME = "SERVER";
    
    public static void main( String[] args )
    {
        ExecutorService executorService = Executors.newFixedThreadPool( 30 );
        
        System.out.println( "Starting ChatServer" );
        Server server = new Server( PORT_DEFAULT, NAME );
        
        server.executeWith( executorService );
        
        //Or we just do this
//        server.run();
        
        //TODO: Shutdown executorService once server stops, we cannot use the same trick as the clients as the server actually needs more threads
        
    }
    
}