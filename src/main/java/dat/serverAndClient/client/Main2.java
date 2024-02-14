package dat.serverAndClient.client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dat.util.ConnectionMaster.LOCAL_HOST;
import static dat.util.ConnectionMaster.PORT_DEFAULT;

public class Main2
{
    
    private static final String IP = LOCAL_HOST; //Set this!
    private static final String NAME = "STUDENT"; //Set this!
    
    public static void main( String[] args )
    {
        ExecutorService executorService = Executors.newFixedThreadPool( Client.THREADS_MINIMUM );
        
        System.out.println( "Starting extra ChatClient" );
        Client client = new Client( IP, PORT_DEFAULT, NAME );
        
        client.executeWith( executorService );
        
        
        //Or we just do this
//        client.run();
    }
    
}
