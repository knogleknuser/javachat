package dat.serverAndClient.client.extraTestClients;

import dat.serverAndClient.client.Client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dat.util.ConnectionMaster.LOCAL_HOST;
import static dat.util.ConnectionMaster.PORT_DEFAULT;

public class Main2 //TODO: make into test
{
    
    private static final String IP = LOCAL_HOST; //Set this!                    //TODO: select this via. console 'UI', also make it none final when we have done it
    private static final int PORT = PORT_DEFAULT; //Set this!                     //TODO: same for this one
    private static final String NAME = "STUDENT"; //Set this!                       //TODO: and this one
    
    public static void main( String[] args )
    {
        ExecutorService executorService = Executors.newFixedThreadPool( Client.THREADS_MINIMUM );
        
        System.out.println( "Starting extra ChatClient" );
        Client client = new Client( IP, PORT, NAME );
        
        client.executeWith( executorService );
        
        
        //Or we just do this
//        client.run();
        
        executorService.shutdown();
        
    }
    
}
