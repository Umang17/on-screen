package onscreen.presentator.communication;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * An interface for connections. Should be used for getting a common interface
 * for different kind of connection. Makes switching between different
 * connections easier.
 * 
 * The address to connect to could for instance be giving to the connection in
 * the constructor.
 * 
 * @author Elias Näslund and John Viklund
 * 
 */
public interface ConnectionInterface {

	/**
	 * Get the output stream associated with this socket. Returns the
	 * byte-oriented output stream.
	 * 
	 * Throws IOException if an error occurs while creating the output stream.
	 * 
	 * @return OutputStream or null if not connected
	 * @throws IOException
	 */
	public OutputStream getOutputStream() throws IOException;

	/**
	 * Get the input stream associated with this socket.
	 * 
	 * Throws IOException if an error occurs while creating the output stream.
	 * 
	 * @return inputStream or null if not connected
	 * @throws IOException
	 */
	public InputStream getInputStream() throws IOException;

	/**
	 * Attempt to connect to a remote device.
	 * 
	 * Throws IOException on error, for example connection failure.
	 * 
	 * @throws IOException
	 */
	public void connect() throws IOException;

	/**
	 * Disconncets the connection and release all associated resources.
	 * 
	 * Throws IOException
	 * 
	 * @throws IOException
	 */
	public void disconnect() throws IOException;

	/**
	 * Return the address of this connection.
	 * 
	 * @return
	 */
	public String getAddr();

	/**
	 * Returns the number if times to retry to connect.
	 * 
	 * @return
	 */
	public int getRetries();

}
