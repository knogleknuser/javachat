package dat.serverAndClient;

import dat.util.Colors;

import java.util.Objects;

public record Message( String message, String sender, String receiver ) //TODO: unit tests and integration tests
{
    
    public static final String ALL = "@AlL";
    
    private static final String END_OF_SENDER = Colors.PURPLE_ANSI + " -> " + Colors.RESET_ANSI;
    private static final String END_OF_RECEIVER = formatName( ":" ) + "    ";
    
    @Override
    public String toString()
    {
        if ( Objects.equals( this.receiver, ALL ) ) {
            return formatName( this.sender ) + END_OF_RECEIVER + this.message;
            
        } else {
            return formatName( this.sender ) + END_OF_SENDER + formatName( this.receiver ) + END_OF_RECEIVER + this.message;
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
            sender = extractName( sender );
            receiver = ALL;
            
        } else {
            int endOfSenderInfo = rawMessage.indexOf( END_OF_SENDER );
            
            sender = rawMessage.substring( 0, endOfSenderInfo );
            sender = extractName( sender );
            
            receiver = rawMessage.substring( endOfSenderInfo + END_OF_SENDER.length(), endOfReceiverInfo );
            receiver = extractName( receiver );
        }
        
        return new Message( message, sender, receiver );
    }
    
    public static String formatName( String name )
    {
        return getNameColor() + name + Colors.RESET_ANSI;
    }
    
    public static String extractName( String name )
    {
        
        if ( name.startsWith( getNameColor() ) ) {
            name = name.substring( getNameColor().length() );
        }
        
        if ( name.endsWith( Colors.RESET_ANSI ) ) {
            name = name.substring( 0, name.length() - Colors.RESET_ANSI.length() );
        }
        
        return name;
    }
    
    public static String getNameColor()
    {
        return Colors.BLUE_ANSI;
    }
    
}
