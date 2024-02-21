package dat.serverAndClient.client.withUI;

import jdk.jfr.Label;
import jdk.jfr.Name;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static dat.util.ConnectionMaster.LOCAL_HOST;
import static dat.util.ConnectionMaster.PORT_DEFAULT;

//@Name( "STUDENT" )
//@Label( "STUDENT" ) //TODO: Display custom console name at top of console
public class Main
{
    
    public static final int NAME_INDEX = 0;
    public static final int IP_INDEX = 1;
    public static final int PORT_INDEX = 2;
    
    private static String IP = LOCAL_HOST; //Set this!                    //TODO: select this via. console 'UI', also make it none final when we have done it
    private static int PORT = PORT_DEFAULT; //Set this!                     //TODO: same for this one
    private static String NAME = "STUDENT"; //Set this!                       //TODO: and this one
    
    public static void main( String[] args )
    {
        if ( args != null && args.length != 0 ) {
            
            int i = NAME_INDEX;
            if ( args[ i ] != null && !args[ i ].isEmpty() && !args[ i ].isBlank() ) {
                NAME = args[ i ];
            }
            
            i = IP_INDEX;
            if ( args.length >= 2 && args[ i ] != null && !args[ i ].isEmpty() && !args[ i ].isBlank() ) {
                IP = args[ i ];
            }
            
            i = PORT_INDEX;
            if ( args.length >= 3 && args[ i ] != null && !args[ i ].isEmpty() && !args[ i ].isBlank() ) {
                int rawPort = Integer.parseInt( args[ i ] );
                
                if ( rawPort > 1023 && rawPort < 65536 ) {
                    PORT = rawPort;
                }
            }
            
        }
        
        ExecutorService executorService = Executors.newFixedThreadPool( ClientWithUI.THREADS_MINIMUM );
        
        ClientWithUI clientWithUI = new ClientWithUI( IP, PORT, NAME );
        
        clientWithUI.executeWith( executorService );
        
        
        //Or we just do this
//        clientWithUI.run();
        
        executorService.shutdown();
        
    }
    
    public static void startClientMain( String name, String ip, int port )
    {
        String[] nameIpPort = arrayForClientMain( name, ip, port );
        
        main( nameIpPort );
    }
    
    public static String[] arrayForClientMain( String name, String ip, int port )
    {
        String[] nameIpPort = new String[ 3 ];
        
        nameIpPort[ NAME_INDEX ] = name;
        nameIpPort[ IP_INDEX ] = ip;
        nameIpPort[ PORT_INDEX ] = String.valueOf( port );
        
        return nameIpPort;
    }
    
}
