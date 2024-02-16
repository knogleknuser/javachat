package dat.util;

import java.util.ArrayList;
import java.util.concurrent.Future;

public class ThreadsUtil
{
    public static void closeThreads( ArrayList< Future<?> > arrayOfThreads)
    {
        for ( Future< ? > serverThread : arrayOfThreads ) {
            
            if ( !Thread.currentThread().equals( serverThread ) ) {
                serverThread.cancel( true );
            }
            
        }
        arrayOfThreads.clear();
    }
    
}
