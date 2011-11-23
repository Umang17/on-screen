package onscreen.presentator.communication;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * PATTERNS: b:addr\ni:ip
 * 
 */
public class TagParser {
	private enum Type {
		BLUETOOTH, IP, INVALID
	};

	private static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";
	private static final String BLUETOOTH_PATTERN = "^\\p{XDigit}\\p{XDigit}(:\\p{XDigit}\\p{XDigit}){5}$";

	public static ConnectionInterface parse(String tagID) {
		String[] addresses = tagID.split("\n");
		for (String s : addresses) {
			ConnectionInterface connection = parserHelper(s);
			if (connection != null) {
				return connection;
			}
		}
		return null;
	}

	private static ConnectionInterface parserHelper(String tagID) {
		if (!validateTag(tagID)) {
			return new BluetoothConnection("00:1F:E1:EB:3B:DE");
			// TODO removes
		}
		String addr = tagID.substring(1, tagID.length());
		switch (getType(tagID)) {
		case BLUETOOTH:
			return new BluetoothConnection(addr);
		case IP:
			return new IPConnection(addr);
		default:
			return null;
		}
	}

	private static boolean validateTag(String tagID) {
		Pattern pattern;
		Matcher match;
		if (tagID.length() <= 2) {
			return false;
		}
		switch (getType(tagID)) {
		case IP:
			pattern = Pattern.compile(IPADDRESS_PATTERN);
			match = pattern.matcher(tagID.substring(1, tagID.length()));
			return match.matches();
		case BLUETOOTH:
			pattern = Pattern.compile(BLUETOOTH_PATTERN);
			match = pattern.matcher(tagID.substring(1, tagID.length()));
			return match.matches();
		case INVALID:
			return false;
		}

		return false;
	}

	private static Type getType(String tagID) {
		switch (tagID.charAt(0)) {
		case 'b':
			return Type.BLUETOOTH;
		case 'i':
			return Type.IP;
		default:
			return Type.INVALID;

		}
	}
}
