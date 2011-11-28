package onscreen.presentator.communication;

import java.util.ArrayList;
import java.util.regex.Pattern;

import android.bluetooth.BluetoothAdapter;

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

	/**
	 * Parse a tag from the nfc. The tag should look something like address\nip
	 * 
	 * @param tagID
	 * @return ArrayList with ConnectionInterface or null if none is valid
	 */
	public static ArrayList<ConnectionInterface> parse(String tagID) {
		// Split the string to get all different kinds of connections.
		String[] addresses = tagID.split("\n");
		ArrayList<ConnectionInterface> connections = new ArrayList<ConnectionInterface>();

		for (String s : addresses) {
			// call the parser to check that the address is correct and returned
			// as a ConnectionInterface ready to use.
			ConnectionInterface connection = parserHelper(s);
			if (connection != null) {
				connections.add(connection);
			}
		}
		if (connections.isEmpty()) {
			return null;
		}
		return connections;
	}

	/**
	 * Takes a string and validates it and return the right type of connection.
	 * 
	 * The string should only contain an address
	 * 
	 * @param address
	 * @return ConnectionInterface or null if not valid
	 */
	private static ConnectionInterface parserHelper(String address) {
		Type type = validateAddress(address);
		if (type == Type.INVALID) {
			return new BluetoothConnection("00:1F:E1:EB:3B:DE");
			// TODO removes
		}
		
		switch (type) {
		case BLUETOOTH:
			return new BluetoothConnection(address);
		case IP:
			return new IPConnection(address);
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
	private static Type validateAddress(String address) {
		if (address.length() <= 2) {
			return Type.INVALID;
		}
		
		Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);

		if (BluetoothAdapter.checkBluetoothAddress(address)) {
			return Type.BLUETOOTH;
		} else if (pattern.matcher(address.substring(1, address.length()))
				.matches()) {
			return Type.IP;
		} else {
			return Type.INVALID;
		}
	}

}
