package onscreen.presentator.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

/**
 * A ip connection. Gets the ip to connect to in the constructor. For additional
 * comments see ConnectionInterface.java
 * 
 * @author Elias Näslund and John Viklund
 * 
 */
public class IPConnection implements ConnectionInterface {
	private Socket mSocket = null;
	private String mAddr;
	private static final int mPort = 8633;
	private static final int NUMBER_OF_RETRIES = 2;

	public IPConnection(String addr) {
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
		mSocket = new Socket();
		mSocket.connect(new InetSocketAddress(mAddr, mPort));
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
		return "IPConnection: " + mAddr;
	}

	public int getRetries() {
		return NUMBER_OF_RETRIES;
	}
}
