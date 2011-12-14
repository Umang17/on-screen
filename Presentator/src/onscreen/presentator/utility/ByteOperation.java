package onscreen.presentator.utility;

/**
 * Class used for converting to and from bytes.
 * 
 * @author Elias Näslund and Mattias Lundberg
 * 
 */
public class ByteOperation {

	/**
	 * Takes a long and converts it to a 8 bytes array.
	 * 
	 * @param v
	 *            long to convert
	 * @return array with 8 bytes
	 */
	public static final byte[] longToBytes(long v) {
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

	/**
	 * Takes an int and converts it to a 4 bytes array.
	 * 
	 * @param v
	 *            int to convert
	 * @return array with 4 bytes
	 */
	public static final byte[] intToBytes(int v) {
		byte[] writeBuffer = new byte[4];

		writeBuffer[0] = (byte) (v >>> 24);
		writeBuffer[1] = (byte) (v >>> 16);
		writeBuffer[2] = (byte) (v >>> 8);
		writeBuffer[3] = (byte) (v >>> 0);

		return writeBuffer;
	}

	/**
	 * Convert a 4 bytes array to an int.
	 * 
	 * @param b
	 *            byte array to convert
	 * @return int
	 */
	public static final int bytesToInt(byte[] b) {
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

	/**
	 * Convert an array with chars to an equal size array of bytes.
	 * 
	 * @param array
	 *            with chars
	 * @return array with bytes
	 */
	public static final byte[] charArrayToBytes(char[] array) {
		byte[] bytes = new byte[array.length];
		for (int i = 0; i < array.length; i++) {
			bytes[i] = (byte) array[i];
		}
		return bytes;
	}
}
