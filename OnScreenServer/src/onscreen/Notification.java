package onscreen;

/**
 * Creates a system for notifying users and developers using simple debug messages.
 */
public class Notification {

    private static boolean debug = true;

    /**
     * Prints the debugmessage if debug is enabled otherwise just returns.
     * 
     * @param notification The message to display to the user
     */
    public static void debugMessage(String notification) {
        if (debug) {
            System.out.println(notification);
        }
    }

    /**
     * Turn debug on or off.
     * 
     * @param debug true turns debugging on, false off 
     */
    public static void setDebug(boolean debug) {
        Notification.debug = debug;
    }

    /**
     * Turn debug on or off using a string 
     * 
     * @param debug turns on if string is "true" otherwise off.
     */
    static void setDebug(String debug) {
        if (debug.equals("true")) {
            setDebug(true);
        } else {
            setDebug(false);
        }
    }
}
