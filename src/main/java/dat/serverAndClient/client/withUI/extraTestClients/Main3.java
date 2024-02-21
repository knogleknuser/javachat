package dat.serverAndClient.client.withUI.extraTestClients;

import dat.serverAndClient.client.withUI.ClientWithUI;
import dat.serverAndClient.client.withUI.Main;
import jdk.jfr.Label;
import jdk.jfr.Name;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dat.util.ConnectionMaster.LOCAL_HOST;
import static dat.util.ConnectionMaster.PORT_DEFAULT;
//@Name( "STUDENT 3" )
//@Label( "STUDENT 3" )
public class Main3 //TODO: make into test
{
    
    private static final String NAME = "STUDENT 3"; //Set this!
    
    public static void main( String[] args )
    {
        Main.startClientMain( NAME,null,-1 );
        
    }
    
}
