package onscreen.presentator.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class BluetoothConnection implements ConnectionInterface {
	private static final UUID MY_UUID = UUID
			.fromString("04c6093b-0000-1000-8000-00805f9b34fb");

	private BluetoothSocket mSocket = null;
	private BluetoothAdapter bluetoothAdapter;

	public BluetoothConnection(String addr) {
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothDevice device = bluetoothAdapter.getRemoteDevice(addr);

		// Get a BluetoothSocket to connect with the given BluetoothDevice
		try {
			// MY_UUID is the app's UUID string, also used by the server
			// code
			mSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
		} catch (IOException e) {
			// TODO bluetooth not available
		}
	}

	public OutputStream getOutputStream() throws IOException {
		try {
			return mSocket.getOutputStream();
		} catch (NullPointerException e) {
			return null;
		}
	}

	public InputStream getInputStream() throws IOException {
		try {
			return mSocket.getInputStream();
		} catch (NullPointerException e) {
			return null;
		}
	}

	public void connect() throws IOException {
		bluetoothAdapter.cancelDiscovery();
		try {
			mSocket.connect();
		} catch (NullPointerException e) {
		}
	}

	public void disconnect() throws IOException {
		try {
			mSocket.close();
		} catch (NullPointerException e) {
		}
	}

}
