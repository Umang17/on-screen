package onscreen.presentator.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class IPConnection implements ConnectionInterface {
	private Socket mSocket = null;
	private String mAddr;
	private int mPort = 37;

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

}
