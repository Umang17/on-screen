package onscreen;

import java.io.File;

/**
 *
 * @author Mattias
 * 
 */
public class OnScreen {

    public static MouseController mouseController;
    public static String pdfReader;
    static KeyController keyController;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        mouseController = new MouseController();
        keyController = new KeyController();

        // Set up pdf reader
        if (args.length > 0) {
            pdfReader = args[0];
            if (!(new File(pdfReader.split(" ")[0])).exists()) {
                Notification.notify("Something wrong with custom pdf reader");
            }
        } else if ((new File(System.getenv("PROGRAMFILES") // 32bit windows
                + "\\SumatraPDF\\SumatraPDF.exe")).exists()) {
            pdfReader = System.getenv("PROGRAMFILES") 
                    + "\\SumatraPDF\\SumatraPDF.exe -esc-to-exit -presentation ";
            
        } else if ((new File(System.getenv("PROGRAMFILES(X86)") // 64bit windows
                + "\\SumatraPDF\\SumatraPDF.exe")).exists()) {
            pdfReader = System.getenv("PROGRAMFILES(X86)") 
                    + "\\SumatraPDF\\SumatraPDF.exe -esc-to-exit -presentation ";
        } else if ((new File(System.getenv("PROGRAMFILES") // 32bit windows
                + "\\OnScreen\\SumatraPDF.exe")).exists()) {
            pdfReader = System.getenv("PROGRAMFILES)") 
                    + "\\OnScreen\\SumatraPDF.exe -esc-to-exit -presentation ";
            
        }  else {
            Notification.notify("Could not find pdf reader!");
        }
        
        //Set up debug
        if (args.length > 1) {
            if (args[1].equals("true")) {
                Notification.setDebug(true);
            } else if (args[1].equals("false")) {
                Notification.setDebug(false);
            }
        }
        
        // Create the bluetooth listner
        Thread btWaitThread = new Thread(new BluetoothWaitThread());
        btWaitThread.start();
        
        Thread lanWaitThread = new Thread(new LanWaitThread());
        lanWaitThread.start();
    }
}