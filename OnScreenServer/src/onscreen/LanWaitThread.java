package onscreen;

/**
 *
 * @author Mattias
 */
import java.io.IOException;
import javax.microedition.io.SocketConnection;
import com.intel.bluetooth.gcf.socket.ServerSocketConnection;

public class LanWaitThread implements Runnable {

    public static int PORT = 8633;

    @Override
    public void run() {
        waitForConnection();
    }

    private void waitForConnection() {

        // Use a normal server socket connection
        try {
            ServerSocketConnection scn = new ServerSocketConnection(PORT);
            SocketConnection connection;
            while (true) {
                Notification.debugMessage("Local lan address: "
                        + scn.getLocalAddress() + ":" + PORT + "\n");
                connection = (SocketConnection) scn.acceptAndOpen();

                Thread processThread = new Thread(
                        new ConnectedThread(connection));
                processThread.start();
            }
        } catch (IOException e) {
            Notification.debugMessage("Failed on LAN. Restarting!\n");
            PORT++;
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
            }
            Thread lanWaitThread = new Thread(new LanWaitThread());
            lanWaitThread.start();
            return;
        }
    }
}
