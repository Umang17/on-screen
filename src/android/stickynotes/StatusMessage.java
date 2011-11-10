package android.stickynotes;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;

public class StatusMessage extends Activity {

/**
 * Returns the Status of the Nfc Device with a String "enabled" or "disabled"
 * \return Status NfcDevice
 * @author Falkenstein
 * 
 */
	public static String getStatusNfcDevice () {
		
	NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter();
	if (nfcAdapter.isEnabled()) { 
	    String status = "enabled";
	
	return status;
	    }
	    else {
	        String status = "disabled";
	        return status;
	
	
	    }
	
	}
	/**
	 * Returns the TagId. Needs an Intent. So you have to get you intent from your "main" activity and give it to the method -> just add the following   *lines in your "main class"
	     *Intent intent =new Intent();
	    *System.out.println(com.example.StatusMessage.getNfcAdapterExtraID(intent));
	 *@author Falkenstein
	 */ 
	public static String getNfcAdapterExtraID (Intent intent) {
	    byte[] byte_id = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
	    return byte_id.toString();
	}
	
	public String byteToStr(byte[] input) {
	    StringBuffer buffer = new StringBuffer();
	    for (int i = 0; i < input.length; i++)
	        if (input[i] != 0) {
	            buffer.append( new Character((char)input[i]).toString());
	        }
	    return buffer.toString();
	}

}