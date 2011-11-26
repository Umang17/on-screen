package onscreen.presentator.communication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for validating addresses and return o ConnectionInferface ready to
 * use.
 * 
 * @author Elias Näslund and John Viklund
 * 
 */
public class TagParser {
	private enum Type {
		BLUETOOTH, IP, INVALID
	};

	// Advanced pattern to see that the ip address is correct. Checks number of
	// dots and that no part of the address is over 255.
	private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

	// Pattern to see if it is a valid bluetooth address.
	private static final String BLUETOOTH_PATTERN = "^\\p{XDigit}\\p{XDigit}(:\\p{XDigit}\\p{XDigit}){5}$";

	/**
	 * Parse a tag from the nfc. The tag should look something like b:addr\ni:ip
	 * 
	 * TODO return all valid connections
	 * 
	 * @param tagID
	 * @return ConnectionInterface or null if none is valid
	 */
	public static ConnectionInterface parse(String tagID) {
		// Split the string to get all different kinds of connections.
		String[] addresses = tagID.split("\n");
		for (String s : addresses) {
			// call the parser to check that the address is correct and returned
			// as a ConnectionInterface ready to use.
			ConnectionInterface connection = parserHelper(s);
			if (connection != null) {

				// BluetoothAdapter bluetoothAdapter =
				// BluetoothAdapter.getDefaultAdapter();
				// if (bluetoothAdapter == null) {
				// // bluetooth not existing on the device
				// continue;
				// }
				// if (bluetoothAdapter.isEnabled()) {
				// // check if bluetooth is enabled
				// }
				//
				// ConnectivityManager connManager = (ConnectivityManager)
				// getSystemService(PresentatorActivity.CONNECTIVITY_SERVICE);
				// NetworkInfo mWifi =
				// connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
				//
				// if (mWifi.isConnected()) {
				// // Do whatever
				// }

				return connection;
			}
		}
		return null;
	}

	/**
	 * Takes a string and validates it and return the right type of connection.
	 * 
	 * The string should look like b:address or i:address.
	 * 
	 * @param address
	 * @return ConnectionInterface or null if not valid
	 */
	private static ConnectionInterface parserHelper(String address) {
		if (!validateAddress(address)) {
			return new BluetoothConnection("00:1F:E1:EB:3B:DE");
			// TODO removes
		}
		String addr = address.substring(1, address.length());
		switch (getType(address)) {
		case BLUETOOTH:
			return new BluetoothConnection(addr);
		case IP:
			return new IPConnection(addr);
		default:
			return null;
		}
	}

	/**
	 * Validates the address.
	 * 
	 * @param tagID
	 * @return true if valid, false otherwise
	 */
	private static boolean validateAddress(String address) {
		Pattern pattern;
		Matcher match;
		if (address.length() <= 2) {
			return false;
		}
		switch (getType(address)) {
		case IP:
			pattern = Pattern.compile(IPADDRESS_PATTERN);
			match = pattern.matcher(address.substring(1, address.length()));
			return match.matches();
		case BLUETOOTH:
			pattern = Pattern.compile(BLUETOOTH_PATTERN);
			match = pattern.matcher(address.substring(1, address.length()));
			return match.matches();
		case INVALID:
			return false;
		}

		return false;
	}

	/**
	 * Turn i to Type.ID and b to Type.BLUETOOTH. if not b or i it returns
	 * Type.INVALID
	 * 
	 * @param type
	 * @return Type
	 */
	private static Type getType(String type) {
		switch (type.charAt(0)) {
		case 'b':
			return Type.BLUETOOTH;
		case 'i':
			return Type.IP;
		default:
			return Type.INVALID;

		}
	}
}
