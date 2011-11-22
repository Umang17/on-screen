package onscreen;

/**
 *
 * @author Mattias
 */
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

public class BluetoothWaitThread implements Runnable {

    @Override
    public void run() {
        waitForConnection();
    }

    private void waitForConnection() {
        // retrieve the local Bluetooth device object
        LocalDevice local = null;
        StreamConnectionNotifier notifier;
        StreamConnection connection = null;
        try {
            local = LocalDevice.getLocalDevice();
        } catch (BluetoothStateException ex) {
            Notification.debugMessage("Could not initiate bluetooth..");
            return;
        }

        // setup the server to listen for connection
        try {
            local.setDiscoverable(DiscoveryAgent.GIAC);

            Notification.debugMessage("Local bluetooth address: " + local.getBluetoothAddress() + "\n");

            UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"
            String url = "btspp://localhost:" + uuid.toString()
                    + ";name=OnScreen";
            notifier = (StreamConnectionNotifier) Connector.open(url);
        } catch (Exception e) {
            Notification.debugMessage("Problem starting bluetooth.");
            return;
        }

        // waiting for connection
        while (true) {
            try {
                connection = notifier.acceptAndOpen();
                Thread processThread = new Thread(
                        new ConnectedThread(connection));
                processThread.start();
            } catch (Exception e) {
                Notification.debugMessage("Problem while waiting for connection. Restarting!");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ex) {
                }
                Thread btWaitThread = new Thread(new BluetoothWaitThread());
                btWaitThread.start();
                return;
            }
        }

    }
}
