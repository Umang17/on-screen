package onscreen;

/**
 *
 * @author Mattias
 */
import java.io.IOException;
import javax.microedition.io.SocketConnection;
import com.intel.bluetooth.gcf.socket.ServerSocketConnection;

public class LanWaitThread implements Runnable {

    @Override
    public void run() {
        waitForConnection();
    }

    private void waitForConnection() {

        // Use a normal server socket connection
        try {
            ServerSocketConnection scn = new ServerSocketConnection(8633);
            SocketConnection connection;
            while (true) {
                Notification.notify("waiting for connection...\n");
                connection = (SocketConnection) scn.acceptAndOpen();
                Notification.notify("got connection...\n");

                Thread processThread = new Thread(
                        new ConnectedThread(connection));
                processThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }
    }
}
