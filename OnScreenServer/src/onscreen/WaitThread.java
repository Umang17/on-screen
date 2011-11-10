package onscreen;

/**
 *
 * @author Mattias
 */
import java.io.IOException;

import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import com.intel.bluetooth.gcf.socket.ServerSocketConnection;

public class WaitThread implements Runnable {
	private static final boolean USE_BLUETOOTH = false;

	@Override
	public void run() {
		waitForConnection();
	}

	private void waitForConnection() {
		if (USE_BLUETOOTH) {
			// retrieve the local Bluetooth device object
			LocalDevice local = null;
			StreamConnectionNotifier notifier;
			StreamConnection connection = null;

			// setup the server to listen for connection
			try {
				local = LocalDevice.getLocalDevice();
				local.setDiscoverable(DiscoveryAgent.GIAC);

				Notification.notify(local.getBluetoothAddress() + "\n");

				UUID uuid = new UUID(80087355); // "04c6093b-0000-1000-8000-00805f9b34fb"
				String url = "btspp://localhost:" + uuid.toString()
						+ ";name=OnScreen";
				notifier = (StreamConnectionNotifier) Connector.open(url);
			} catch (Exception e) {
				e.printStackTrace();
				return;
			}

			// waiting for connection
			while (true) {
				try {
					connection = notifier.acceptAndOpen();
					Thread processThread = new Thread(
							new ProcessConnectionThread(connection));
					processThread.start();
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
			}
		} else {
			// Use a normal server socket connection
			try {
				ServerSocketConnection scn = new ServerSocketConnection(8633);
				SocketConnection connection;
				while (true) {
					Notification.notify("waiting for connection...\n");
					connection = (SocketConnection) scn.acceptAndOpen();
					Notification.notify("got connection...\n");

					Thread processThread = new Thread(
							new ProcessConnectionThread(connection));
					processThread.start();
				}
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
		}
	}
}
