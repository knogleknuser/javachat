package dat.serverAndClient;

import dat.util.Colors;

public class ConsoleCommands
{
    
    public static final String COMMAND_START = "/";
    public static final String COMMAND_EXIT = COMMAND_START + "exit";
    public static final String COMMAND_HELP = COMMAND_START + "help";
    public static final String COMMAND_COMPUTER_START = Colors.RESET_ANSI;
    public static final String COMMAND_COMPUTER_MYNAME = COMMAND_COMPUTER_START + "myname";
    
    
    
    //Command Checker-------------------------------------------------------------------------------------------------------
    public static boolean isCommand( String inputLine )
    {
        if ( inputLine.startsWith( COMMAND_START ) ) {
            return true;
        }
        return false;
    }
    
    
    
    
    //Command Selector--------------------------------------------------------------------------------------------------------
    public static Message runCommand( String inputLine, ChatIF chatIF )
    {
        if ( !isCommand( inputLine ) ) {
            System.err.println( "ERROR: "+chatIF.getName()+" THOUGHT NON-COMMAND WAS A COMMAND?" );
        }
        
        switch ( inputLine ) {
            
            case COMMAND_HELP:
                printCommandHelp();
                return null;
            
            case COMMAND_EXIT:
                chatIF.close();
                return null;
            
            default:
                System.out.println( "\"" + inputLine + "\" is not a recognized command" );
                return null;
        }
    }

    
    
    //Commands--------------------------------------------------------------------------------------------------------
    private static void printCommandHelp() //<---------------------------------------------------------------------------UPDATE THIS WHEN YOU ADD NEW COMMANDS YOU NERDS
    {
        System.out.println( COMMAND_HELP );
        System.out.println( COMMAND_EXIT );
    }

    
}
