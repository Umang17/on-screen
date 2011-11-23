package onscreen.presentator.communication;

public class TagParser {
	private enum Type {
		BLUETOOTH, IP
	};

	public static ConnectionInterface parse(String tagID) {
		switch (getType(tagID)) {
		case BLUETOOTH:
			BluetoothConnection connection = new BluetoothConnection(
					getBluetoothAdress(tagID));
			return connection;
		default:
			return null;
		}
	}

	private static String getBluetoothAdress(String tagID) {
		// TODO :)
		return "00:1F:E1:EB:3B:DE";
	}

	private static Type getType(String tagID) {
		// TODO Support IP
		return Type.BLUETOOTH;
	}
}
