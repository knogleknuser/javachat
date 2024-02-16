package dat.serverAndClient;

import java.io.IOException;
import java.util.Objects;

public interface ChatIF
{
    
    boolean connect() throws IOException;
    boolean sendMessage(Message message);
    Message receiveMessage() throws IOException;
    void close();
    
    //Getters Only-------------------------------------
    boolean isRunning();
    
    
    //Getters and Setters-------------------------------------
    String getName();
    void setName(String name);
    
    int getPort();
    void setPort(int port);
    
    String getIp();
    void setIp(String ip); //Does nothing for a server
    
    
    
    
    
    static String getNameChangeMessage(String formerName, String newName){
        return "Changed their name from \"" + formerName + "\" to \"" + newName + "\"";
    }
    static String setName(ChatIF chatIF, String newName){
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
        
        String nameChangeMessage = ChatIF.getNameChangeMessage( oldName, newName );
        
        Message message = new Message( nameChangeMessage, newName, Message.ALL );
        chatIF.sendMessage( message );
        
        return newName;
    }
    
    
}
