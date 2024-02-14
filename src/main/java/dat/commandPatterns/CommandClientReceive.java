package dat.commandPatterns;

public class CommandClientReceive implements Runnable
{
    private OnlineReceiveIF recieveIF;
    public CommandClientReceive(OnlineReceiveIF recieveIF) {
        this.recieveIF = recieveIF;
    }
    
    
    
    /**
     * When an object implementing interface {@code Runnable} is used
     * to create a thread, starting the thread causes the object's
     * {@code run} method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method {@code run} is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run()
    {
        this.recieveIF.receiveMessage();
        
    }
    
    
    
}
