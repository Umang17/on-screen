package onscreen;

import java.awt.AWTException;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.Toolkit;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Creates a system for notifying users and developers using simple debug messages.
 */
public class Notification {

    private static boolean debug = true;
    private static boolean init = false;
    private static TrayIcon trayIcon = new TrayIcon(Toolkit.getDefaultToolkit().getImage("src/tray.gif"));

    private static void init() {
        SystemTray tray = SystemTray.getSystemTray();
        try {
            tray.add(trayIcon);
        } catch (AWTException ex) {
            return;
        }
        ActionListener exitListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                System.out.println("Exiting...");
                System.exit(0);
            }
        };
        PopupMenu popup = new PopupMenu();
        MenuItem defaultItem = new MenuItem("Exit");
        defaultItem.addActionListener(exitListener);
        popup.add(defaultItem);

        ActionListener actionListener = new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                trayIcon.displayMessage("Action Event",
                        "An Action Event Has Been Performed!",
                        TrayIcon.MessageType.INFO);
            }
        };
        trayIcon.setImageAutoSize(true);
    trayIcon.addActionListener(actionListener);
        init = true;
    }

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
     * Displays a notification in the system tray
     * @param notification 
     */
    public static void message(String notification) {
        if (!init) {
            init();
        }
        trayIcon.displayMessage("OnScreen", notification,
                TrayIcon.MessageType.INFO);
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
