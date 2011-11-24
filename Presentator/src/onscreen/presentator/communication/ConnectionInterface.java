package onscreen.presentator.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface ConnectionInterface {

	public OutputStream getOutputStream() throws IOException;

	public InputStream getInputStream() throws IOException;

	public void connect() throws IOException;

	public void disconnect() throws IOException;
	
	public String getAddr();

}
