package onscreen;

import java.io.File;

/**
 *
 * @author Mattias
 * 
 */
public class OnScreen {

    static final String nativeSumatra = "\\SumatraPDF\\SumatraPDF.exe";
    static final String onScreenSumatra = "\\OnScreen\\SumatraPDF.exe";
    static final String sumatraSettings = " -esc-to-exit -page 0 -presentation ";
    static final String env32 = System.getenv("PROGRAMFILES");
    static final String env64 = System.getenv("PROGRAMFILES(X86)");
                    static final String BTOFFSETTING = "nobt";
                static final String LANOFFSETTING = "nolan";
    static boolean BT = true;
    static boolean LAN = true;
    public static MouseController mouseController;
    public static String pdfReader;
    static KeyController keyController;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        mouseController = new MouseController();
        keyController = new KeyController();

        if (args.length > 0) {
            pdfReader = "";
            for (int i = 0; i < args.length; i++) {
                final String DEBUGSETTING = "debug";

                if (args[i].equals(DEBUGSETTING)) {
                    Notification.setDebug(true);
                } else if (args[i].equals(BTOFFSETTING)) {
                    BT = false;
                } else if (args[i].equals(LANOFFSETTING)) {
                    LAN = false;
                } else {
                    pdfReader += args[i];
                } 
            }
        } else if ((new File(env32 + nativeSumatra)).exists()) {
            pdfReader = env32 + nativeSumatra + sumatraSettings;
        } else if ((new File(env64 + nativeSumatra)).exists()) {
            pdfReader = env64 + nativeSumatra + sumatraSettings;
        } else if ((new File(env32 + onScreenSumatra)).exists()) {
            pdfReader = env32 + onScreenSumatra + sumatraSettings;
        } else {
            Notification.notify("Could not find pdf reader!");
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