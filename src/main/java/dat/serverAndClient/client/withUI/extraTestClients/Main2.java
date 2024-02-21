package dat.serverAndClient.client.withUI.extraTestClients;

import dat.serverAndClient.client.withUI.ClientWithUI;
import dat.serverAndClient.client.withUI.Main;
import jdk.jfr.Label;
import jdk.jfr.Name;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dat.util.ConnectionMaster.LOCAL_HOST;
import static dat.util.ConnectionMaster.PORT_DEFAULT;
//@Name( "STUDENT 2" )
//@Label( "STUDENT 2" )
public class Main2 //TODO: make into test
{
    private static final String NAME = "STUDENT 2"; //Set this!
    
    public static void main( String[] args )
    {
        Main.startClientMain( NAME,null,-1 );
        
    }
    
}
