package elias.test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Bluetooth {
	
	private static final String TAG = "Bluetooth";
	private static final boolean D = true;
	
	private static final Bluetooth instance = new Bluetooth();

	private BluetoothAdapter mBluetoothAdapter;
	private static final UUID MY_UUID = UUID
			.fromString("04c6093b-0000-1000-8000-00805f9b34fb");

//	private final BluetoothAdapter mAdapter;
//	private final Handler mHandler;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	
	private boolean mConnected = false;
	
	private static final int BYTE_SIZE = 8192;

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
		connect(device);
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
		if (D) Log.d(TAG, "sendFile");
		mConnectedThread.write(type);
		if (D) Log.d(TAG, "sent type");
		BufferedInputStream buf = new BufferedInputStream(new FileInputStream(
				file));

		long length = file.length();
		mConnectedThread.write(longToBytes(length)); // send the size of the byte stream.
		if (D) Log.d(TAG, "sent length");

		String name = file.getName();
		char[] nameChar = name.toCharArray();
		int nameSize = nameChar.length;
		mConnectedThread.write(intToBytes(nameSize)); // send the size of the name
		if (D) Log.d(TAG, "sent name size");

		mConnectedThread.write(charArrayToBytes(nameChar)); // send the name of the file
    	if (D) Log.d(TAG, "sent all but file...");

		byte[] buffer = new byte[BYTE_SIZE];
		
		while (buf.read(buffer) != -1) {
			mConnectedThread.write(buffer);
		}
		
		mConnectedThread.write(intToBytes(10));

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
	
	public boolean isConnected() {
		return mConnected;
	}
	
    public synchronized void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device);


        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
    }
	
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device) {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
//        if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread = null;}

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {mConnectedThread.cancel(); mConnectedThread = null;}

        if (D) Log.d(TAG, "connected - after killing");
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();

        // Send the name of the connected device back to the UI Activity
//        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_DEVICE_NAME);
//        Bundle bundle = new Bundle();
//        bundle.putString(BluetoothChat.DEVICE_NAME, device.getName());
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);
    }
    
    /**
     * Stop all threads
     */
    public synchronized void stop() {
        if (D) Log.d(TAG, "stop");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed() {
        if (D) Log.d(TAG, "in connectionFailed");

        // Send a failure message back to the Activity
//        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        bundle.putString(BluetoothChat.TOAST, "Unable to connect device");
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
//        Bluetooth.this.start();
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost() {
    	if (D) Log.d(TAG, "in connectionLost");
        // Send a failure message back to the Activity
//        Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
//        Bundle bundle = new Bundle();
//        bundle.putString(BluetoothChat.TOAST, "Device connection was lost");
//        msg.setData(bundle);
//        mHandler.sendMessage(msg);

        // Start the service over to restart listening mode
//        BluetoothChatService.this.start();
    }
    
	private class ConnectThread extends Thread {
	    private final BluetoothSocket mmSocket;
	    private final BluetoothDevice mmDevice;
	 
	    public ConnectThread(BluetoothDevice device) {
	        // Use a temporary object that is later assigned to mmSocket,
	        // because mmSocket is final
	        BluetoothSocket tmp = null;
	        mmDevice = device;
	 
	        // Get a BluetoothSocket to connect with the given BluetoothDevice
	        try {
	            // MY_UUID is the app's UUID string, also used by the server code
	            tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
	        } catch (IOException e) { }
	        mmSocket = tmp;
	        if (D) Log.d(TAG, "in ConnectThread");
	    }
	 
	    public void run() {
	    	setName("ConnectThread");
	    	if (D) Log.d(TAG, "in ConnectThread-run");
	        // Cancel discovery because it will slow down the connection
	        mBluetoothAdapter.cancelDiscovery();
	        
	 
	        try {
	            // Connect the device through the socket. This will block
	            // until it succeeds or throws an exception
	            mmSocket.connect();
	        } catch (IOException connectException) {
	            // Unable to connect; close the socket and get out
	            try {
	                mmSocket.close();
	            } catch (IOException closeException) {}
	            connectionFailed();
	            return;
	        }
	        if (D) Log.d(TAG, "in ConnectedThread - at end of run");
	        // Do work to manage the connection (in a separate thread)
	        connected(mmSocket, mmDevice);
	    }
	 
	    /** Will cancel an in-progress connection, and close the socket */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }
	}
	
	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
	    private final InputStream mmInStream;
	    private final OutputStream mmOutStream;
	    
	    private static final int BYTE_SIZE = 1024;
	 
	    public ConnectedThread(BluetoothSocket socket) {
	    	if (D) Log.d(TAG, "ConnectedThread");
	        mmSocket = socket;
	        InputStream tmpIn = null;
	        OutputStream tmpOut = null;
	 
	        // Get the input and output streams, using temp objects because
	        // member streams are final
	        try {
	            tmpIn = socket.getInputStream();
	            tmpOut = socket.getOutputStream();
	        } catch (IOException e) { }
	 
	        mmInStream = tmpIn;
	        mmOutStream = tmpOut;
	        mConnected = true;
	    	if (D) Log.d(TAG, "ConnectedThread - end");

	    }
	 
	    public void run() {
	    	if (D) Log.d(TAG, "ConnectedThread - run");
	        byte[] buffer = new byte[1024];  // buffer store for the stream
	        int bytes; // bytes returned from read()
	 
	        // Keep listening to the InputStream until an exception occurs
	        while (true) {
//	            try {
//	                // Read from the InputStream
//	            	if (D) Log.d(TAG, "before read");
//	                bytes = mmInStream.read(buffer);
//	                if (D) Log.d(TAG, "after read");
//	                // Send the obtained bytes to the UI Activity
////	                mHandler.obtainMessage(MESSAGE_READ, bytes, -1, buffer)
////	                        .sendToTarget();
//	            } catch (IOException e) {
//	            	connectionLost();
//	                break;
//	            }
	        }
	    }
	 
	    /* Call this from the main Activity to send data to the remote device */
	    public void write(byte[] bytes) {
	        try {
	            mmOutStream.write(bytes);
//	            mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
//                .sendToTarget();
	        } catch (IOException e) {
	        	if (D) Log.d(TAG, "writing failed");
	        }
	    }
	 
	    /* Call this from the main Activity to send data to the remote device */
	    public void write(byte bytes) {
	        try {
	            mmOutStream.write(bytes);
//	            mHandler.obtainMessage(BluetoothChat.MESSAGE_WRITE, -1, -1, buffer)
//                .sendToTarget();
	        } catch (IOException e) { 
	        	if (D) Log.d(TAG, "write failed");

	        }
	    }
	    /* Call this from the main Activity to shutdown the connection */
	    public void cancel() {
	        try {
	            mmSocket.close();
	        } catch (IOException e) { }
	    }

		
		
	}


}
