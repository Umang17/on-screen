package onscreen.presentator;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

public class ReadNfcTag {
	
	//TODO CHANGE TAG!
	private static final String TAG = "stickynotes";
	
	PendingIntent mNfcPendingIntent;
    IntentFilter[] mWriteTagFilters;
    IntentFilter[] mNdefExchangeFilters;
    
    private PresentatorActivity mainClass;
    NfcAdapter mNfcAdapter;

	public void onCreate(PresentatorActivity mainClass, Class<? extends PresentatorActivity> class1) {
        
		this.mainClass = mainClass;
		
		mNfcAdapter = NfcAdapter.getDefaultAdapter(mainClass);
		// Handle all of our received NFC intents in this activity.
        mNfcPendingIntent = PendingIntent.getActivity(mainClass, 0,
                new Intent(mainClass, class1).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter ndefDetected = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        mNdefExchangeFilters = new IntentFilter[] { ndefDetected };        		
	}
	
	public void onResume(Intent intent){
		enableNFC();  
        
        Log.d(TAG,"onResume: "+intent.getAction());         
        
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())){
        	mainClass.onNewIntent(intent);
        } 
	}
	
    private NdefMessage getNoteAsNdef() {
        byte[] textBytes = "text".getBytes();
        NdefRecord textRecord = new NdefRecord(NdefRecord.TNF_MIME_MEDIA, "text/plain".getBytes(),
                new byte[] {}, textBytes);
        return new NdefMessage(new NdefRecord[] {
            textRecord
        });
    }
	
    private void enableNFC(){
    	//enableNdefExchangeMode
    	mNfcAdapter.enableForegroundNdefPush(mainClass, getNoteAsNdef());
        //mNfcAdapter.enableForegroundDispatch(this, mNfcPendingIntent, mNdefExchangeFilters, null);
    	
        //enableTagWriteMode
    	IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[] {
            tagDetected
        };
        mNfcAdapter.enableForegroundDispatch(mainClass, mNfcPendingIntent, mWriteTagFilters, null);
    }
	
	/**
     * 
     * @return the NFC tag id, if no id discovered return empty string.
     */
    public String getNFCTagID(Intent intent){
    	Tag tagFromIntent = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
    	if(tagFromIntent == null){
    		return "";
    	} 
    	byte[] tagID = tagFromIntent.getId();
    	return (tagID.length==0)? "" : ByteArrayToHexString(tagID);
    }

    private void showID(Intent intent) {		
    	String text="tag ID: " + getNFCTagID(intent) +"\n";    	    	    	
    	Log.d(TAG,text);
    }
    	
	private String ByteArrayToHexString(byte [] inarray){
	    int i, j, in;
	    String [] hex = {"0","1","2","3","4","5","6","7","8","9","A","B","C","D","E","F"};
	    String out= "";
	
	    for(j = 0 ; j < inarray.length ; ++j) 
	        {
	        in = (int) inarray[j] & 0xff;
	        i = (in >> 4) & 0x0f;
	        out += hex[i];
	        i = in & 0x0f;
	        out += hex[i];
	        }
	    return out;
	}

	public void onNewIntent(Intent intent) {
    	if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction()) ||
    			NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
    		showID(intent);
        }		
	}

}
