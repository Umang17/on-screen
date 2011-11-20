package onscreen.presentator;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConcreteHandleTagIDDiscover implements HandleTagIDDiscover {
	
	private Bluetooth mBluetooth;
	final String bluetoothAdress = "00:1F:E1:EB:3B:DE";

	public ConcreteHandleTagIDDiscover(Bluetooth mBluetooth){
		this.mBluetooth=mBluetooth;
	}

	public void handleTagIDDiscover(String tagID) {
		if (!mBluetooth.isConnected()){
			try {
				mBluetooth.connect(bluetoothAdress);
			} catch (IOException ex) {
				Logger.getLogger(PresentatorActivity.class.getName()).log(
						Level.SEVERE, null, ex);
			}
		}		
	}

}
