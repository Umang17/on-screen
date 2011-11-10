package onscreen;

/**
 *
 * @author Mattias
 */
import java.io.InputStream;

import java.io.OutputStream;
import javax.microedition.io.StreamConnection;

public class ProcessConnectionThread implements Runnable {

    private StreamConnection mConnection;
    private Notification noti;
    private static final int EXIT_CMD = -1;
    private static final int IMAGES = 1;
    private static final int CONTROLLER = 2;
    private ImageReciver imageReciver = null;

    ProcessConnectionThread(StreamConnection connection, Notification noti) {
        mConnection = connection;
        this.noti = noti;
    }

    @Override
    public void run() {
        try {
            OnScreen.frame.setVisible(true);
            // prepare to receive data
            InputStream inputStream = mConnection.openInputStream();
            OutputStream out = mConnection.openOutputStream();

            while (true) {
                int command = inputStream.read();
                
                /**
                 * Transmission protocol
                 * 1: Type
                 * 20: Length
                 * 1-*: Data!
                 * 
                 * EXIT -1
                 * 
                 * IMAGE 1
                 * 255: File name
                 * 1-* Data...
                 * 
                 * CONTROLLER 2
                 * 
                 * 
                 */

                switch (command) {
                    case EXIT_CMD:
                       break;
                    case IMAGES:
                        OnScreen.imageController.recive(inputStream, noti);
                        break;
                    case CONTROLLER:
                        OnScreen.mouseController.recive(inputStream, noti);
                       break;
                    default:
                        noti.notify("Unknown control sequence " + command);
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        OnScreen.frame.setVisible(false);
    }
    
    
}
