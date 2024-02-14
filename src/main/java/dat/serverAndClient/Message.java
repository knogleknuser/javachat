package dat.serverAndClient;

import dat.util.Colors;

public record Message( String message, String sender, String receivers )
{
    
    private static final String endOfSender = Colors.RESET_ANSI + "    ";
    
    @Override
    public synchronized String toString()
    {
        return Colors.BLUE_ANSI + this.sender + ":" + Colors.RESET_ANSI + "    " + this.message;
    }
    
    public static synchronized Message createMessage( String rawMessage )
    {
        int endOfSenderInfo = rawMessage.indexOf( endOfSender );
        
        String sender = rawMessage.substring( 0, endOfSenderInfo-1 );
        String message = rawMessage.substring( endOfSenderInfo+endOfSender.length() );
        String receivers = "all";
        
        return new Message( message, sender, receivers );
    }
    
}
