package onscreen.presentator;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import onscreen.presentator.communication.BluetoothConnection;
import onscreen.presentator.communication.Connection;

public class ConcreteHandleTagIDDiscover implements HandleTagIDDiscover {
	
	private Connection mConnection;
	final String bluetoothAdress = "00:1F:E1:EB:3B:DE";

	public ConcreteHandleTagIDDiscover(Connection connection){
		this.mConnection = connection;
	}

	public void handleTagIDDiscover(String tagID) {
		if (!mConnection.isConnected()){
			try {
				BluetoothConnection bluetooth = new BluetoothConnection("00:1F:E1:EB:3B:DE");
				mConnection.connect(bluetooth);
			} catch (IOException ex) {
				Logger.getLogger(PresentatorActivity.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}		
	}

}
