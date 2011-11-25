package onscreen.presentator.nfc;

import onscreen.presentator.PresentatorActivity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Parcelable;

public class ReadNfcTag {

	//private static final String TAG = "ReadNfcTag";

	private PendingIntent mNfcPendingIntent;
	private IntentFilter[] mWriteTagFilters;

	private PresentatorActivity mainClass;
	private NfcAdapter mNfcAdapter;

	private HandleTagDiscover hTIDD;

	/**
	 * Calls handleTagIDDiscover.handleTagIDDiscover(message) when a new tag is
	 * discovered. If no message found the tagID is returned if it is found, else ignores.
	 * 
	 * Don't forget to use onPause, onResume and onCreate
	 * 
	 * @param handleTagIDDiscover
	 */
	public ReadNfcTag(HandleTagDiscover handleTagIDDiscover) {
		hTIDD = handleTagIDDiscover;
	}

	public void onCreate(PresentatorActivity mainClass) {

		this.mainClass = mainClass;

		mNfcAdapter = NfcAdapter.getDefaultAdapter(mainClass);
		if (mNfcAdapter == null) {
			return;
		}

		// Handle all of our received NFC intents in this activity.
		mNfcPendingIntent = PendingIntent.getActivity(mainClass, 0, new Intent(
				mainClass, mainClass.getClass())
				.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
	}

	public void onResume(Intent intent) {
		if (mNfcAdapter == null) {
			return;
		}
		enableNFC();

		if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())) {
			handleNewTagIDIntent(intent);
		}
	}

	private void enableNFC() {

		IntentFilter tagDetected = new IntentFilter(
				NfcAdapter.ACTION_TAG_DISCOVERED);
		mWriteTagFilters = new IntentFilter[] { tagDetected };
		mNfcAdapter.enableForegroundDispatch(mainClass, mNfcPendingIntent,
				mWriteTagFilters, null);
	}

	public void onPause() {
		if (mNfcAdapter == null) {
			return;
		}
		mNfcAdapter.disableForegroundDispatch(mainClass);
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

	public void onNewIntent(Intent intent) {
		//Log.d(TAG, "onNewIntent " + intent.getAction());
		if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
			handleNewTagIDIntent(intent);
		}
	}
	
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
	 * Converts to a hexstring
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
