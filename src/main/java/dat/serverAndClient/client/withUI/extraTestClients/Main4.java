package dat.serverAndClient.client.withUI.extraTestClients;

import dat.serverAndClient.client.withUI.ClientWithUI;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dat.util.ConnectionMaster.LOCAL_HOST;
import static dat.util.ConnectionMaster.PORT_DEFAULT;

public class Main4 //TODO: make into test
{
    
    private static final String IP = LOCAL_HOST; //Set this!                    //TODO: select this via. console 'UI', also make it none final when we have done it
    private static final int PORT = PORT_DEFAULT; //Set this!                     //TODO: same for this one
    private static final String NAME = "STUDENT 4"; //Set this!                       //TODO: and this one
    
    public static void main( String[] args )
    {
        ExecutorService executorService = Executors.newFixedThreadPool( ClientWithUI.THREADS_MINIMUM );
        
        System.out.println( "Starting extra ChatClient" );
        ClientWithUI clientWithUI = new ClientWithUI( IP, PORT, NAME );
        
        clientWithUI.executeWith( executorService );
        
        
        //Or we just do this
//        clientWithUI.run();
        
        executorService.shutdown();
        
    }
    
}
