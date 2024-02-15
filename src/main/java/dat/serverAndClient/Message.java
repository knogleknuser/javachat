package dat.serverAndClient;

import dat.util.Colors;

import java.util.Objects;

public record Message( String message, String sender, String receivers ) //TODO: unit tests and integration tests
{
    
    public static final String ALL = "@AlL";
    
    private static final String END_OF_SENDER = Colors.PURPLE_ANSI + " -> " + Colors.BLUE_ANSI;
    private static final String END_OF_RECEIVER = ":" + Colors.RESET_ANSI + "    ";
    
    @Override
    public String toString()
    {
        if ( Objects.equals( this.receivers, ALL ) ) {
            return Colors.BLUE_ANSI + this.sender + END_OF_RECEIVER + this.message;
            
        } else {
            return Colors.BLUE_ANSI + this.sender + END_OF_SENDER + this.receivers + END_OF_RECEIVER + this.message;
        }
    }
    
    public static Message createMessage( String rawMessage )
    {
        int endOfReceiverInfo = rawMessage.indexOf( END_OF_RECEIVER );
        
        String sender = rawMessage.substring( 0, endOfReceiverInfo );
        String message = rawMessage.substring( endOfReceiverInfo + END_OF_RECEIVER.length() );
        String receivers;
        
        if ( !sender.contains( END_OF_SENDER ) ) {
            receivers = ALL;
            
        } else {
            int endOfSenderInfo = rawMessage.indexOf( END_OF_SENDER );
            
            sender = rawMessage.substring( 0, endOfSenderInfo );
            receivers = rawMessage.substring( endOfSenderInfo + END_OF_SENDER.length(), endOfReceiverInfo );
        }
        
        return new Message( message, sender, receivers );
    }
    
}
