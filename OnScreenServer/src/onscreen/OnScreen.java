package onscreen;

import java.io.File;

/**
 * The main class, starts all connections and handles the pdf reader and arguments 
 * to the program.
 *
 * @author Mattias
 */
public class OnScreen {

    static final String nativeSumatra = "\\SumatraPDF\\SumatraPDF.exe";
    static final String onScreenSumatra = "\\OnScreen\\SumatraPDF.exe";
    static final String sumatraSettings = " -esc-to-exit -page 1 -presentation ";
    static final String env32 = System.getenv("PROGRAMFILES");
    static final String env64 = System.getenv("PROGRAMFILES(X86)");
    static final String BTOFFSETTING = "--nobt";
    static final String LANOFFSETTING = "--nolan";
    static final String DEBUGSETTING = "--debug";
    static final String PDFSETTING = "--pdf=";
    static boolean BT = true;
    static boolean LAN = true;
    public static String pdfReader = "";
    static KeyController keyController;

    /**
     * Creates a new server system for the onscreen.
     * 
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        keyController = new KeyController();

        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                if (args[i].equals(DEBUGSETTING)) {
                    Notification.setDebug(true);
                } else if (args[i].equals(BTOFFSETTING)) {
                    BT = false;
                } else if (args[i].equals(LANOFFSETTING)) {
                    LAN = false;
                } else if (args[i].startsWith(PDFSETTING)) {
                    pdfReader = args[i].split("=")[1];
                } else {
                    Notification.debugMessage("Unknown setting " + args[i]);
                }
            }
        }

        // If no arguments for pdf reader sent using sumatra, either native or 
        // the one bundled with the onscreen installer
        if (pdfReader.isEmpty()) {
            if ((new File(env32 + nativeSumatra)).exists()) {
                pdfReader = env32 + nativeSumatra + sumatraSettings;
            } else if ((new File(env64 + nativeSumatra)).exists()) {
                pdfReader = env64 + nativeSumatra + sumatraSettings;
            } else if ((new File(env32 + onScreenSumatra)).exists()) {
                pdfReader = env32 + onScreenSumatra + sumatraSettings;
            }
        }

        if (pdfReader.isEmpty()) {
            Notification.debugMessage("Could not find pdf reader!");
        }

        if (BT) {
            Thread btWaitThread = new Thread(new BluetoothWaitThread());
            btWaitThread.start();
        }

        if (LAN) {
            Thread lanWaitThread = new Thread(new LanWaitThread());
            lanWaitThread.start();
        }
    }
}