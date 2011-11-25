package onscreen.presentator.communication;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import onscreen.presentator.PresentatorActivity;
import onscreen.presentator.utility.ByteOperation;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

public class Connection {

	private static final String TAG = "Connection";
	private static final boolean D = true;

	private final Handler mHandler;
	private ConnectedThread mConnectedThread = null;
	private ConnectingThread mConnectingThread = null;
	private ConnectionInterface mConnection = null;

	private boolean mConnected = false;

	private static final int BYTE_SIZE = 1000;

	private static final byte TYPE_PRESENTATION = 1;
	private static final byte TYPE_COMMANDS = 5;
	private static final byte TYPE_TIME = 7;

	private static final byte COMMAND_EXIT = 0;
	private static final byte COMMAND_NEXT = 1;
	private static final byte COMMAND_PREV = 2;
	private static final byte COMMAND_BLANK = 3;

	private static final byte COMMAND_START = 7;
	private static final byte COMMAND_PAUSE = 8;
	private static final byte COMMAND_RESET = 9;

	public Connection(Handler handler) {
		mHandler = handler;
	}

	public boolean sendPresentation(File file) {
		if (!mConnected)
			return false;
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

	private void sendClockSetting(byte command) {
		if (isConnected()) {
			mConnectedThread.write(TYPE_TIME); // Time
			mConnectedThread.write(command); // Type: Start,Pause,Reset
		}
	}

	public void sendStartClock() {
		sendClockSetting(COMMAND_START); // Start
	}

	public void sendPauseClock() {
		sendClockSetting(COMMAND_PAUSE); // Pause
	}

	public void sendResetClock() {
		sendClockSetting(COMMAND_RESET); // Reset
	}

	public boolean isConnected() {
		return mConnected;
	}

	public String getAddr() {
		if (mConnection == null || !mConnected) {
			return null;
		}
		return mConnection.getAddr();
	}

	public synchronized void connect(ConnectionInterface connection) {
		if (D)
			Log.d(TAG, "connect to: " + connection);

		mConnection = connection;
		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		// Start the thread to connect with the given device
		mConnectingThread = new ConnectingThread(mConnection);
		mConnectingThread.start();
	}

	public synchronized void connected(ConnectionInterface connection) {
		if (D)
			Log.d(TAG, "connected");
		mHandler.sendEmptyMessage(PresentatorActivity.MESSAGE_CONNECTED);

		// Cancel any thread currently running a connection
		if (mConnectedThread != null) {
			mConnectedThread.cancel();
			mConnectedThread = null;
		}

		if (D)
			Log.d(TAG, "connected - after killing");
		// Start the thread to manage the connection and perform transmissions
		mConnectedThread = new ConnectedThread(connection);
		mConnectedThread.start();
	}

	/**
	 * Stop all threads
	 */
	public synchronized void stop() {
		if (D)
			Log.d(TAG, "stop");
		mConnected = false;
		mHandler.sendEmptyMessage(PresentatorActivity.MESSAGE_DISCONNECTED);

		if (mConnectingThread != null) {
			mConnectingThread.cancel();
			mConnectingThread = null;
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
		mHandler.sendEmptyMessage(PresentatorActivity.MESSAGE_DISCONNECTED);
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
		mHandler.sendEmptyMessage(PresentatorActivity.MESSAGE_DISCONNECTED);
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
			mHandler.obtainMessage(PresentatorActivity.MESSAGE_PROGRESS_START,
					length).sendToTarget();
			mConnectedThread.write(TYPE_PRESENTATION);
			if (D)
				Log.d(TAG, "sent type");

			// send the size of the byte stream
			mConnectedThread.write(ByteOperation.longToBytes(length));
			if (D)
				Log.d(TAG, "sent length" + length);

			String name = file.getName();
			char[] nameChar = name.toCharArray();
			int nameSize = nameChar.length;
			// send the size of the name
			mConnectedThread.write(ByteOperation.intToBytes(nameSize));
			if (D)
				Log.d(TAG, "sent name size");

			// send the name of the file
			mConnectedThread.write(ByteOperation.charArrayToBytes(nameChar));

			if (D)
				Log.d(TAG, "sent all but file...");

			byte[] buffer = new byte[BYTE_SIZE];

			for (long i = 0; i <= length; i += BYTE_SIZE) {
				try {
					buf.read(buffer);
				} catch (IOException e) {
					return null;
				}
				mConnectedThread.write(buffer);
				mHandler.obtainMessage(
						PresentatorActivity.MESSAGE_PROGRESS_INC, i)
						.sendToTarget();
			}

			mConnectedThread.write(ByteOperation.intToBytes(10));
			mHandler.obtainMessage(PresentatorActivity.MESSAGE_PROGRESS_INC,
					length).sendToTarget();

			return null;
		}
	}

	private class ConnectingThread extends Thread {
		private final ConnectionInterface mmConnection;

		public ConnectingThread(ConnectionInterface connection) {
			mmConnection = connection;
		}

		@Override
		public void run() {
			for (int i = 0; i < 10; i++) {
				try {
					mmConnection.connect();
					connected(mmConnection);
					return;
				} catch (IOException e) {
					Log.d(TAG, "Failed to connect: " + e.getLocalizedMessage());
				}
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
				}
			}
			connectionFailed();
		}

		/** Will cancel an in-progress connection, and close the socket */
		public void cancel() {
			try {
				mConnection.disconnect();
			} catch (IOException e) {
			}
		}
	}

	private class ConnectedThread extends Thread {
		private final ConnectionInterface mmConnection;
		private final InputStream mmInStream;
		private final OutputStream mmOutStream;

		public ConnectedThread(ConnectionInterface connection) {
			if (D)
				Log.d(TAG, "ConnectedThread");
			mmConnection = connection;
			InputStream tmpIn = null;
			OutputStream tmpOut = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = connection.getInputStream();
				tmpOut = connection.getOutputStream();
			} catch (IOException e) {
				// kill connection
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
			int bytes = 0; // bytes returned from read()

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
						int read = 0;
						int offset = 0,
						length = 4;

						// read length of name, 4 bytes
						while (read != 4) {
							bytes = mmInStream.read(buffer, offset, length);
							read = read + bytes;
							offset = offset + bytes - 1;
							length = length - bytes;
						}
						if (D)
							Log.d(TAG, "read (4) " + read);
						int size = ByteOperation.bytesToInt(buffer);
						if (D)
							Log.d(TAG, "length = " + size);

						read = 0;
						offset = 0;
						length = size;
						// read name
						while (read != size) {
							bytes = mmInStream.read(buffer, 0, length);
							read = read + bytes;
							offset = offset + bytes - 1;
							length = length - bytes;
						}

						// convert name bytes to a string!
						char[] chars = new char[size];
						for (int i = 0; i < size; i++) {
							chars[i] = (char) buffer[i];
						}
						String fileName = String.copyValueOf(chars);
						if (D)
							Log.d(TAG, "name = " + fileName);

						read = 0;
						offset = 0;
						length = 4;
						// read time, 4 bytes
						while (read != 4) {
							bytes = mmInStream.read(buffer, offset, length);
							read = read + bytes;
							offset = offset + bytes - 1;
							length = length - bytes;
						}
						int time = ByteOperation.bytesToInt(buffer);
						if (D)
							Log.d(TAG, "time = " + time);

						bytes = mmInStream.read(buffer, 0, 1); // read running
						boolean running = buffer[0] == 1 ? true : false;

						Bundle bundle = new Bundle();
						bundle.putString(PresentatorActivity.BUNDLE_NAME,
								fileName);
						bundle.putInt(PresentatorActivity.BUNDLE_TIME, time);
						bundle.putBoolean(PresentatorActivity.BUNDLE_RUNNING,
								running);
						mHandler.obtainMessage(
								PresentatorActivity.MESSAGE_TAKE_OVER, 4, -1,
								bundle).sendToTarget(); // 4 is just a
														// number....
						break;
					case 4: // file rec...
						mHandler.sendEmptyMessage(PresentatorActivity.MESSAGE_FILE_REC);
						break;
					case 7: // timer reset
						// Coded as:
						// 1 if running else 0
						// 1 if reset else 0
						int runningClock = mmInStream.read(); // running 1 ==
																// starta 0 ==
																// stop
						int reset = mmInStream.read(); // reset 1 == nolla

						mHandler.obtainMessage(
								PresentatorActivity.MESSAGE_CLOCK,
								runningClock, reset).sendToTarget();
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
				mmConnection.disconnect();
			} catch (IOException e) {
			}
		}
	}
}