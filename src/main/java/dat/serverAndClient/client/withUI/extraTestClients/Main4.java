package dat.serverAndClient.client.withUI.extraTestClients;

import dat.serverAndClient.client.withUI.ClientWithUI;
import dat.serverAndClient.client.withUI.Main;
import jdk.jfr.Label;
import jdk.jfr.Name;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dat.util.ConnectionMaster.LOCAL_HOST;
import static dat.util.ConnectionMaster.PORT_DEFAULT;
//@Name( "STUDENT 4" )
//@Label( "STUDENT 4" ) //TODO: Display custom console name at top of console
public class Main4 //TODO: make into test
{
    
    private static final String NAME = "STUDENT 4";
    
    public static void main( String[] args )
    {
        Main.startClientMain( NAME,null,-1 );
        
    }
    
}
