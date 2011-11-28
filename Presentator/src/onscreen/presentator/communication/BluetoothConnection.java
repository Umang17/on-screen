package onscreen.presentator.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

/**
 * A bluetooth connection. Gets the bluetooth address to connect to in the
 * constructor. For additional comments see ConnectionInterface.java
 * 
 * @author Elias Näslund and John Viklund
 * 
 */
public class BluetoothConnection implements ConnectionInterface {
	// MY_UUID is the app's UUID string, also used by the server code
	private static final UUID MY_UUID = UUID
			.fromString("04c6093b-0000-1000-8000-00805f9b34fb");

	private BluetoothSocket mSocket = null;
	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothDevice mBlueToothDevice;
	private String mAddr = null;
	private static final int NUMBER_OF_RETRIES = 10;

	public BluetoothConnection(String addr) {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		mBlueToothDevice = mBluetoothAdapter.getRemoteDevice(addr);
		mAddr = addr;
	}

	public OutputStream getOutputStream() throws IOException {
		if (mSocket != null) {
			return mSocket.getOutputStream();
		}
		return null;
	}

	public InputStream getInputStream() throws IOException {
		if (mSocket != null) {
			return mSocket.getInputStream();
		}
		return null;
	}

	public void connect() throws IOException {
		if (mSocket != null) {
			try {
				mSocket.close();
			} catch (IOException e) {
			}
		}
		// Disable discovery for connecting faster
		mBluetoothAdapter.cancelDiscovery();

		try {

			mSocket = mBlueToothDevice
					.createInsecureRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			// TODO bluetooth not available
		}
		mSocket.connect();
	}

	public void disconnect() throws IOException {
		if (mSocket != null) {
			mSocket.close();
			mSocket = null;
		}
	}

	public String getAddr() {
		return mAddr;
	}

	@Override
	public String toString() {
		return "BluetoothConnection: " + mBlueToothDevice.getAddress();
	}

	public int getRetries() {
		return NUMBER_OF_RETRIES;
	}
}
