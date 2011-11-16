package onscreen;

public class Notification {   
    private static boolean debug = false;
    
    public static void notify(String notification) {
        if (debug) {
            System.out.println(notification);
        }
    }
    
    public static void setDebug(boolean debug) {
        Notification.debug = debug;
    }
 }
