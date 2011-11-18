package onscreen;

public class Notification {   
    private static boolean debug = true;
    
    public static void notify(String notification) {
        if (debug) {
            System.out.println(notification);
        }
    }
    
    public static void setDebug(boolean debug) {
        Notification.debug = debug;
    }

    static void setDebug(String debug) {
        if (debug.equals("true")) {
            setDebug(true);
        } else {
            setDebug(false);
        }
    }
 }
