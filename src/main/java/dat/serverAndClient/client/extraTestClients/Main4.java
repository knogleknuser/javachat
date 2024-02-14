package dat.serverAndClient.client.extraTestClients;

import dat.serverAndClient.client.Client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dat.util.ConnectionMaster.LOCAL_HOST;
import static dat.util.ConnectionMaster.PORT_DEFAULT;

public class Main4
{
    
    private static final String IP = LOCAL_HOST; //Set this!
    private static final String NAME = "STUDENT TEST 3"; //Set this!
    
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
