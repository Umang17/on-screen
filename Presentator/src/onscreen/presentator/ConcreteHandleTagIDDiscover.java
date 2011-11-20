package onscreen.presentator;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConcreteHandleTagIDDiscover implements HandleTagIDDiscover {
	
	private Bluetooth mBluetooth;

	public ConcreteHandleTagIDDiscover(Bluetooth mBluetooth){
		this.mBluetooth=mBluetooth;
	}

	public void handleTagIDDiscover(String bluetoothAdress) {
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
