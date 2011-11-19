package onscreen.presentator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class Bluetooth {

	private static final String TAG = "Bluetooth";
	private static final boolean D = true;

	private BluetoothAdapter mBluetoothAdapter;
	private static final UUID MY_UUID = UUID
			.fromString("04c6093b-0000-1000-8000-00805f9b34fb");

	// private final BluetoothAdapter mAdapter;
	private final Handler mHandler;
	private ConnectThread mConnectThread;
	private ConnectedThread mConnectedThread;
	
	private boolean mConnected = false;
	private StopWatch stopWatch;

	private static final int BYTE_SIZE = 1000;

	private static final byte TYPE_PRESENTATION = 1;
	private static final byte TYPE_COMMANDS = 5;

	private static final byte COMMAND_EXIT = 0;
	private static final byte COMMAND_NEXT = 1;
	private static final byte COMMAND_PREV = 2;
	private static final byte COMMAND_BLANK = 3;
	
	private static final byte TYPE_TIME = 7;
	
	private static final byte COMMAND_START = 7;
	private static final byte COMMAND_PAUSE = 8;
	private static final byte COMMAND_RESET = 9;

	public Bluetooth(Handler handler,StopWatch stopWatch) {
		this.stopWatch = stopWatch;
		mHandler = handler;
	}

	public void connect(String deviceAddr) throws IOException {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(deviceAddr);
		connect(device);
	}

	public boolean sendPresentation(File file) {
		if (!mConnected) return false;
		new SendFile().execute(file);
		return true;
	}

	public boolean sendExit() {
		if (!mConnected)
			return false;
		mConnectedThread.write(TYPE_COMMANDS);
		mConnectedThread.write(COMMAND_EXIT);
		return true;
	}

	public boolean sendNext() {
		if (!mConnected)
			return false;
		mConnectedThread.write(TYPE_COMMANDS);
		mConnectedThread.write(COMMAND_NEXT);
		return true;
	}

	public boolean sendPrev() {
		if (!mConnected)
			return false;
		mConnectedThread.write(TYPE_COMMANDS);
		mConnectedThread.write(COMMAND_PREV);
		return true;
	}

	public boolean sendBlank() {
		if (!mConnected)
			return false;
		mConnectedThread.write(TYPE_COMMANDS);
		mConnectedThread.write(COMMAND_BLANK);
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

	private final int bytesToInt(byte[] b) {
		int i = 0;

		i += unsignedByteToInt(b[0]) << 24;
		i += unsignedByteToInt(b[1]) << 16;
		i += unsignedByteToInt(b[2]) << 8;
		i += unsignedByteToInt(b[3]);
		return i;
	}

	private static int unsignedByteToInt(byte b) {
		return (int) b & 0xFF;
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
		if (D)
			Log.d(TAG, "connect to: " + device);

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectThread = new ConnectThread(device);
		mConnectThread.start();
	}

	public synchronized void connected(BluetoothSocket socket,
			BluetoothDevice device) {
		if (D)
			Log.d(TAG, "connected");

		// Cancel the thread that completed the connection
		// if (mConnectThread != null) {mConnectThread.cancel(); mConnectThread
		// = null;}

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (D)
			Log.d(TAG, "connected - after killing");
		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(socket);
		mConnectedThread.start();
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "stop");
		mConnected = false;

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
		if (D)
			Log.d(TAG, "in connectionFailed");
		mConnected = false;
		// Send a failure message back to the Activity
		// Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
		// Bundle bundle = new Bundle();
		// bundle.putString(BluetoothChat.TOAST, "Unable to connect device");
		// msg.setData(bundle);
		// mHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		// Bluetooth.this.start();
	}

	/**
	 * Indicate that the connection was lost and notify the UI Activity.
	 */
	private void connectionLost() {
		if (D)
			Log.d(TAG, "in connectionLost");
		mConnected = false;
		// Send a failure message back to the Activity
		// Message msg = mHandler.obtainMessage(BluetoothChat.MESSAGE_TOAST);
		// Bundle bundle = new Bundle();
		// bundle.putString(BluetoothChat.TOAST, "Device connection was lost");
		// msg.setData(bundle);
		// mHandler.sendMessage(msg);

		// Start the service over to restart listening mode
		// BluetoothChatService.this.start();
	}

	private class SendFile extends AsyncTask<File, Integer, Void> {

		@Override
		protected Void doInBackground(File... params) {
			if (D)
				Log.d(TAG, "sendFile");
			File file = params[0];
			
			BufferedInputStream buf;
			try {
				buf = new BufferedInputStream(new FileInputStream(file));
			} catch (FileNotFoundException e) {
				return null;
			}
			long length = file.length();
			Message m = new Message();
			mHandler.obtainMessage(PresentatorActivity.MESSAGE_PROGRESS_START, length).sendToTarget();
			mConnectedThread.write(TYPE_PRESENTATION);
			if (D)
				Log.d(TAG, "sent type");

			mConnectedThread.write(longToBytes(length)); // send the size of the
															// byte stream.
			if (D)
				Log.d(TAG, "sent length"+length);

			String name = file.getName();
			char[] nameChar = name.toCharArray();
			int nameSize = nameChar.length;
			mConnectedThread.write(intToBytes(nameSize)); // send the size of
															// the
															// name
			if (D)
				Log.d(TAG, "sent name size");

			mConnectedThread.write(charArrayToBytes(nameChar)); // send the name
																// of
																// the file
			if (D)
				Log.d(TAG, "sent all but file...");

			byte[] buffer = new byte[BYTE_SIZE];
			long num_of_int = length / BYTE_SIZE; // somehow just send incr
													// message
													// at the right moments
			for (long i = 0; i <= length; i += BYTE_SIZE) {
				try {
					buf.read(buffer);
				} catch (IOException e) {
					return null;
				}
				mConnectedThread.write(buffer);
					mHandler.obtainMessage(PresentatorActivity.MESSAGE_PROGRESS_INC, i).sendToTarget();
			}

			mConnectedThread.write(intToBytes(10));
			mHandler.obtainMessage(PresentatorActivity.MESSAGE_PROGRESS_INC, length).sendToTarget();
			
			return null;
		}

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
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
			}
			mmSocket = tmp;
			if (D)
				Log.d(TAG, "in ConnectThread");
		}

		public void run() {
			setName("ConnectThread");
			if (D)
				Log.d(TAG, "in ConnectThread-run");
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
				} catch (IOException closeException) {
				}
				connectionFailed();
				return;
			}
			if (D)
				Log.d(TAG, "in ConnectedThread - at end of run");
			// Do work to manage the connection (in a separate thread)
			connected(mmSocket, mmDevice);
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		private static final int BYTE_SIZE = 1024;

		public ConnectedThread(BluetoothSocket socket) {
			if (D)
				Log.d(TAG, "ConnectedThread");
			mmSocket = socket;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();
				tmpOut = socket.getOutputStream();
			} catch (IOException e) {
			}

			mmInStream = tmpIn;
			mmOutStream = tmpOut;
			mConnected = true;
			if (D)
				Log.d(TAG, "ConnectedThread - end");

		}

		public void run() {
			if (D)
				Log.d(TAG, "ConnectedThread - run");
			byte[] buffer = new byte[1024]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					if (D)
						Log.d(TAG, "before read");
					int controlInt = mmInStream.read();

					switch (controlInt) {
					case 0: // no presentation...
						mHandler.sendEmptyMessage(PresentatorActivity.MESSAGE_NO_PRES);
						break;
					case 1: // presentation available...
						// TODO: read all bytes!
						// FIXME: ELIAS!!!
						bytes = mmInStream.read(buffer, 0, 4); // read length of
							Log.d(TAG, "read " + bytes);									// name
						int size = bytesToInt(buffer);
						if (D)
							Log.d(TAG, "length = " + size);
						bytes = mmInStream.read(buffer, 0, size); // read name
						char[] chars = new char[size];
						for (int i = 0; i < size; i++) {
							chars[i] = (char) buffer[i];
						}
						String fileName = String.copyValueOf(chars);
						if (D)
							Log.d(TAG, "name = " + fileName);

						bytes = mmInStream.read(buffer, 0, 4); // read current
																// slide
						int currentSlide = bytesToInt(buffer);
						if (D)
							Log.d(TAG, "current slide = " + currentSlide);

						bytes = mmInStream.read(buffer, 0, 4); // read total
																// number of
																// slides
						int totalNr = bytesToInt(buffer);

						if (D)
							Log.d(TAG, "total slides = " + totalNr);

						bytes = mmInStream.read(buffer, 0, 4); // read time
						int time = bytesToInt(buffer);
						//TODO ELIAS:FIX REFERENCE FOR stopWatch
						stopWatch.setBaseTime(time);
						Log.d(TAG, "time = " + time);
						bytes = mmInStream.read(buffer, 0, 1); // read running
						int running = bytesToInt(buffer);
						
						handleIfClockRunning(running,true);
						
						Bundle bundle = new Bundle();
						bundle.putString(PresentatorActivity.BUNDLE_NAME,
								fileName);
						bundle.putInt(PresentatorActivity.BUNDLE_TIME, time);
						bundle.putInt(PresentatorActivity.BUNDLE_TOTAL_SLIDE,
								totalNr);
						bundle.putInt(PresentatorActivity.BUNDLE_CURRENT_SLIDE,
								currentSlide);
						mHandler.obtainMessage(
								PresentatorActivity.MESSAGE_TAKE_OVER, 4, -1,
								bundle).sendToTarget(); // 4 is just a
														// number....
						break;
					case 4: // file rec...
						mHandler.sendEmptyMessage(PresentatorActivity.MESSAGE_FILE_REC);
						break;
					case 7:
						//Coded as:
						//1 if running else 0
						//1 if reset else 0
						int runningClock=mmInStream.read();
						int reset=mmInStream.read();
						handleIfClockRunning(runningClock,false);
						if(reset==1){
							stopWatch.resetClock();
						} 
						
					}
					if (D)
						Log.d(TAG, "after read");
					// Send the obtained bytes to the UI Activity

				} catch (IOException e) {
					connectionLost();
					break;
				}
			}
		}

		private void handleIfClockRunning(int running,boolean forced) {
			if(running==1 && (!stopWatch.isRunningNow() || forced)){
				stopWatch.startClock();
			}else if (running==0 && (stopWatch.isRunningNow() || forced)) {
				stopWatch.pauseClock();
			}
			
		}

		/* Call this from the main Activity to send data to the remote device */
		public void write(byte[] bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {
				if (D)
					Log.d(TAG, "writing failed");
			}
		}

		/* Call this from the main Activity to send data to the remote device */
		public void write(byte bytes) {
			try {
				mmOutStream.write(bytes);
			} catch (IOException e) {
				if (D)
					Log.d(TAG, "write failed");

			}
		}

		/* Call this from the main Activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}

	}
	
	private void sendClockSetting(byte command) {
		if (isConnected()){
			mConnectedThread.write(TYPE_TIME); //Time
			mConnectedThread.write(command); //Type: Start,Pause,Reset
		}		
	}

	public void sendStartClock() {
		sendClockSetting(COMMAND_START); //Start	
	}

	public void sendPauseClock() {
		sendClockSetting(COMMAND_PAUSE); //Pause		
	}

	public void sendResetClock() {
		sendClockSetting(COMMAND_RESET); //Reset
	}

}