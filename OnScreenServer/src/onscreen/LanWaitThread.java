package onscreen;

import java.io.IOException;
import javax.microedition.io.SocketConnection;
import com.intel.bluetooth.gcf.socket.ServerSocketConnection;

/**
 * Sets up a lan connection and wait for a connecting phone.
 * 
 * @author Mattias
 */
public class LanWaitThread implements Runnable {

    public static int PORT = 8633;

    /**
     * Run the thread to start and wait.
     */
    @Override
    public void run() {
        waitForConnection();
    }

    /**
     * Starts the connection and waits for a connection.
     */
    private void waitForConnection() {
        try {
            ServerSocketConnection scn = new ServerSocketConnection(PORT);
            SocketConnection connection;
            Notification.debugMessage("Local lan address: "
                    + scn.getLocalAddress() + ":" + PORT + "\n");
            while (true) {
                connection = (SocketConnection) scn.acceptAndOpen();

                Thread processThread = new Thread(
                        new ConnectedThread(connection));
                processThread.start();
            }
        } catch (IOException e) {
            // Attempts to restart and finding a free port in case of problem.
            Notification.message("Failed on LAN. Restarting!\n");
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
