package onscreen;

/**
 *
 * @author Mattias
 */
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.swing.JTextArea;

public class WaitThread implements Runnable {

    Notification noti;

    /** Constructor */
    public WaitThread(Notification noti) {
        this.noti = noti;
    }

    @Override
    public void run() {
        waitForConnection();
    }

    /** Waiting for connection from devices */
    private void waitForConnection() {
        // retrieve the local Bluetooth device object
        LocalDevice local = null;

        StreamConnectionNotifier notifier;
        StreamConnection connection = null;

        // setup the server to listen for connection
        try {
            local = LocalDevice.getLocalDevice();
            local.setDiscoverable(DiscoveryAgent.GIAC);
            
            noti.notify(local.getBluetoothAddress() + "\n");

            UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"
            String url = "btspp://localhost:" + uuid.toString() + ";name=OnScreen";
            notifier = (StreamConnectionNotifier) Connector.open(url);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        // waiting for connection
        while (true) {
            try {
                noti.notify("waiting for connection...\n");
                connection = notifier.acceptAndOpen();
                noti.notify("got connection...\n");

                Thread processThread = new Thread(new ProcessConnectionThread(connection, noti));
                processThread.start();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }
}
