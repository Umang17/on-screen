package onscreen.presentator.nfc;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;

/**
 * Handles everything that has with nfc.
 * It will call handleTagDiscover(text) when a tag is discovered.
 * 
 * Use onCreate, onPause, onNewIntent and onResume functions in your Activity.
 * @author viktor
 *
 */

public class ReadNfcTag {

	private PendingIntent mNfcPendingIntent;
	private IntentFilter[] mWriteTagFilters;

	private Activity activity;
	private NfcAdapter mNfcAdapter;

	private HandleTagDiscover hTIDD;

	/**
	 * Calls handleTagIDDiscover.handleTagIDDiscover(message) when a new tag is
	 * discovered. If no message found the tag ID is returned and if that is not found, else ignores.
	 * 
	 * Don't forget to use onPause, onResume, onNewIntent and onCreate in your Activity.
	 * 
	 * @param handleTagIDDiscover
	 */
	public ReadNfcTag(HandleTagDiscover handleTagIDDiscover) {
		hTIDD = handleTagIDDiscover;
	}

	/**
	 * The class that you want to handle the intents in.
	 * Use this in your onCreate-method.
	 * @param mainClass
	 */
	public void onCreate(Activity mainClass) {

		this.activity = mainClass;

		mNfcAdapter = NfcAdapter.getDefaultAdapter(mainClass);
		if (mNfcAdapter == null) {
			return;
		}

		// Handle all of our received NFC intents in this activity.
		mNfcPendingIntent = PendingIntent.getActivity(mainClass, 0, new Intent(
				mainClass, mainClass.getClass())
				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	}

	/**
	 * Enables nfc and see if a tag was discovered.
	 * @param intent
	 */
	public void onResume(Intent intent) {
		if (mNfcAdapter == null) {
			return;
		}
		enableNFC();

		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
			handleNewTagIDIntent(intent);
		}
	}

	/**
	 * Setting filters
	 */
	private void enableNFC() {

		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		mWriteTagFilters = new IntentFilter[] { tagDetected };
		mNfcAdapter.enableForegroundDispatch(activity, mNfcPendingIntent,
				mWriteTagFilters, null);
	}

	/**
	 * Disables nfc forground.
	 */
	public void onPause() {
		if (mNfcAdapter == null) {
			return;
		}
		try {
			mNfcAdapter.disableForegroundDispatch(activity);
		} catch (Exception e) {
			try {
				if(!mNfcAdapter.isEnabled()){
					mNfcAdapter = NfcAdapter.getDefaultAdapter(activity);
					mNfcAdapter.disableForegroundDispatch(activity);
				}
			} catch (Exception e2) {}			
		}		
	}

	/**
	 * 
	 * @return the NFC tag id, if no id discovered return empty string.
	 */
	public String getNFCTagID(Intent intent) {
		Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
		if (tagFromIntent == null) {
			return "";
		}
		byte[] tagID = tagFromIntent.getId();
		return (tagID.length == 0) ? "" : ByteArrayToHexString(tagID);
	}

	/**
	 * Checks if it is a ACTION_TAG_DISCOVERED intent and handles it. 
	 * @param intent to handle.
	 */
	public void onNewIntent(Intent intent) {
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			handleNewTagIDIntent(intent);
		}
	}
	
	/**
	 * Try to read message, else try to read tagID, else do nothing and not call handleTagDiscover.
	 * @param intent to read message from.
	 */
	
	private void handleNewTagIDIntent(Intent intent) {
		String text = null;
		
		//Read content from tag
		try {
			NdefMessage[] messages = getNdefMessages(intent);
	        byte[] payload = messages[0].getRecords()[0].getPayload();
	        
	        //Removing first byte, the uri.
	        if (payload.length-1<=1) return;
	        
	        byte[] newPayload=new byte[payload.length-1];
	        for (int i = 0; i < payload.length; i++) {
	        	if (i+1==payload.length){
	        		break;
	        	}
	        	newPayload[i]=payload[i+1];
			}
	        text=new String(newPayload);
		} catch (Exception e) {}
		
        //If no message return tagID instead
        if (text==null || text==""){
        	String tagID = getNFCTagID(intent);
    		if (tagID == "") {
    			return;
    		}
        	text=tagID; 
        }

		hTIDD.handleTagDiscover(text);
	}
	
	/**
	 * Handles NDEF messages
	 * @param intent to get messages from
	 * @return Can be null
	 */
	private NdefMessage[] getNdefMessages(Intent intent) {
        // Parse the intent
        NdefMessage[] msgs = null;
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            } else {
                // Unknown tag type
                byte[] empty = new byte[] {};
                NdefRecord record = new NdefRecord(NdefRecord.TNF_UNKNOWN, empty, empty, empty);
                NdefMessage msg = new NdefMessage(new NdefRecord[] {
                    record
                });
                msgs = new NdefMessage[] {
                    msg
                };
            }
        } else {
            //Log.d(TAG, "Unknown intent.");
            //finish();
        }
        return msgs;
    }
	
	/**
	 * Converts from a bytearray to a hextring
	 * @param inarray
	 * @return
	 */
	private String ByteArrayToHexString(byte[] inarray) {
		int i, j, in;
		String[] hex = { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A",
				"B", "C", "D", "E", "F" };
		String out = "";

		for (j = 0; j < inarray.length; ++j) {
			in = (int) inarray[j] & 0xff;
			i = (in >> 4) & 0x0f;
			out += hex[i];
			i = in & 0x0f;
			out += hex[i];
		}
		return out;
	}

}
