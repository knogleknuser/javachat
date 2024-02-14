package dat.commandPatterns;

public class CommandClientSend implements Runnable
{
    
    private OnlineSendIF onlineSendIF;
    public CommandClientSend(OnlineSendIF onlineSendIF) {
        this.onlineSendIF = onlineSendIF;
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
        this.onlineSendIF.sendMessages();
        
    }
    
}
