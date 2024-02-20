package dat.serverAndClient;

import java.io.IOException;
import java.util.Objects;

public interface ChatIF
{
    
    boolean connect() throws IOException;
    
    boolean sendMessage( Message message );
    
    Message receiveMessage() throws IOException;
    
    void close();
    
    //Getters Only-------------------------------------
    boolean isRunning();
    
    
    //Getters and Setters-------------------------------------
    String getName();
    
    void setName( String name );
    
    int getPort();
    
    void setPort( int port );
    
    String getIp();
    
    void setIp( String ip ); //Does nothing for a server
    
    
    
    
    
    static String getNameChangeMessage( String formerName, String newName, boolean shouldBroadcast )
    {
        if (shouldBroadcast) {
            return "Changed their name from \"" + Message.dyeName( formerName ) + "\" to \"" + Message.dyeName( newName ) + "\"";
        }
        
        return "Changed name from \"" + Message.dyeName( formerName ) + "\" to \"" + Message.dyeName( newName ) + "\"";
    }
    
    static String setName( ChatIF chatIF, String newName, boolean shouldBroadcast )
    {
        String oldName = chatIF.getName();
        
        if ( newName == null ) {
            return oldName;
        }
        
        if ( newName.isEmpty() || newName.isBlank() ) {
            return oldName;
        }
        
        if ( Objects.equals( oldName, newName ) ) {
            return oldName;
        }
        
        String nameChangeMessage = ChatIF.getNameChangeMessage( oldName, newName, shouldBroadcast );
        
        if ( shouldBroadcast ) {
            Message message = new Message( nameChangeMessage, newName, Message.ALL );
            chatIF.sendMessage( message );
        }
//        else {
//            System.out.println( nameChangeMessage);
//        }
        
        return newName;
    }
    
    
}
