package dat.serverAndClient;

public interface ChatIF
{
    boolean connect();
    boolean sendMessage(Message message);
    Message receiveMessage();
    
    
    //Getters and Setters
    String getName();
    void setName(String name);
    
    int getPort();
    void setPort(int port);
    
    String getIp();
    void setIp(String ip); //Does nothing for a server
    

    
}
