package dat.serverAndClient;

import dat.util.Colors;

import java.util.Objects;

public record Message( String message, String sender, String receiver ) //TODO: unit tests and integration tests
{
    public static final String nameColor = Colors.BLUE_ANSI;
    public static final String toArrowColor = Colors.PURPLE_ANSI;
    
    public static final String ALL = "@AlL";
    
    private static final String END_OF_SENDER = toArrowColor + " -> " + Colors.RESET_ANSI;
    private static final String END_OF_RECEIVER = dyeName( ":" ) + "    ";
    
    @Override
    public String toString()
    {
        if ( Objects.equals( this.receiver, ALL ) ) {
            return dyeName( this.sender ) + END_OF_RECEIVER + this.message;
            
        } else {
            return dyeName( this.sender ) + END_OF_SENDER + dyeName( this.receiver ) + END_OF_RECEIVER + this.message;
        }
    }
    
    public static Message createMessage( String rawMessage )
    {
        if ( rawMessage == null ) {
            return null;
        }
        
        int endOfReceiverInfo = rawMessage.indexOf( END_OF_RECEIVER );
        
        String sender = rawMessage.substring( 0, endOfReceiverInfo );
        String message = rawMessage.substring( endOfReceiverInfo + END_OF_RECEIVER.length() );
        String receiver;
        
        if ( !sender.contains( END_OF_SENDER ) ) {
            sender = undyeName( sender );
            receiver = ALL;
            
        } else {
            int endOfSenderInfo = rawMessage.indexOf( END_OF_SENDER );
            
            sender = rawMessage.substring( 0, endOfSenderInfo );
            sender = undyeName( sender );
            
            receiver = rawMessage.substring( endOfSenderInfo + END_OF_SENDER.length(), endOfReceiverInfo );
            receiver = undyeName( receiver );
        }
        
        return new Message( message, sender, receiver );
    }
    
    public static String dyeName( String name )
    {
        return nameColor + name + Colors.RESET_ANSI;
    }
    
    public static String undyeName( String name )
    {
        
        if ( name.startsWith( nameColor ) ) {
            name = name.substring( nameColor.length() );
        }
        
        if ( name.endsWith( Colors.RESET_ANSI ) ) {
            name = name.substring( 0, name.length() - Colors.RESET_ANSI.length() );
        }
        
        return name;
    }
    
}
