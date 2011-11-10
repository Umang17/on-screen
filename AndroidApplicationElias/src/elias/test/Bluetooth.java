package elias.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

public class Bluetooth {
	private static final Bluetooth instance = new Bluetooth();

	private BluetoothAdapter mBluetoothAdapter;
	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
	private BluetoothSocket mmSocket;

	private static final int BYTE_SIZE = 1000;

	private static final byte TYPE_PICTURE = 1;
	private static final byte TYPE_MOUSE = 2;
	private static final byte TYPE_REQ_CONTROL = 3;
	private static final byte TYPE_REL_CONTROL = 4;
	
	private Bluetooth() {}
	
	public static Bluetooth getInstance() {
		return instance;
	}
		
	public void init(String deviceAddr) throws IOException {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddr);
		mmSocket = device
				.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
		connect();
	}

	public void connect() throws IOException {
		mmSocket.connect();
	}
	
	public boolean sendPicture(File file) throws IOException {
		return sendFile(file, TYPE_PICTURE);
	}
	
	public boolean getControl() {
		
		return false;
	}
	
	public boolean releaseControl() {
		
		return false;
	}

	private boolean sendFile(File file, byte type) throws IOException {
		OutputStream stream = mmSocket.getOutputStream();

		stream.write(type); // send the type of the data

		BufferedInputStream buf = new BufferedInputStream(new FileInputStream(
				file));
		
		long length = file.length();
		stream.write(longToBytes(length)); // send the size of the byte stream.

		String name = file.getName();
		char[] nameChar = name.toCharArray();
		int nameSize = nameChar.length;
		stream.write(intToBytes(nameSize)); // send the size of the name
		stream.flush();
		
		stream.write(charArrayToBytes(nameChar)); // send the name of the file
		stream.flush();

		byte[] buffer = new byte[BYTE_SIZE];
		for (long i = 0; i <= length; i += BYTE_SIZE) {
			buf.read(buffer);
			stream.write(buffer);
			stream.flush();
		}

		return true;
	}

	private final byte[] longToBytes(long v) {
		byte[] writeBuffer = new byte[8];

		writeBuffer[0] = (byte) (v >>> 56);
		writeBuffer[1] = (byte) (v >>> 48);
		writeBuffer[2] = (byte) (v >>> 40);
		writeBuffer[3] = (byte) (v >>> 32);
		writeBuffer[4] = (byte) (v >>> 24);
		writeBuffer[5] = (byte) (v >>> 16);
		writeBuffer[6] = (byte) (v >>> 8);
		writeBuffer[7] = (byte) (v >>> 0);

		return writeBuffer;
	}
	
	private final byte[] intToBytes(int v) {
		byte[] writeBuffer = new byte[4];
		

		writeBuffer[0] = (byte) (v >>> 24);
		writeBuffer[1] = (byte) (v >>> 16);
		writeBuffer[2] = (byte) (v >>> 8);
		writeBuffer[3] = (byte) (v >>> 0);
		
		return writeBuffer;
	}
	
	private final byte[] charArrayToBytes(char[] array) {
		byte[] bytes = new byte[array.length];
		for (int i = 0; i < array.length; i++) {
			bytes[i] = (byte) array[i];
		}
		return bytes;
	}

}
