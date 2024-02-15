package dat.serverAndClient.server;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static dat.util.ConnectionMaster.PORT_DEFAULT;

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
    
    
    }
    
}