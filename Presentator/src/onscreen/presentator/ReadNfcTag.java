package onscreen.presentator;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.util.Log;

public class ReadNfcTag {
	
	private static final String TAG = "ReadNfcTag";
	
	private PendingIntent mNfcPendingIntent;
	private IntentFilter[] mWriteTagFilters;
    
    private PresentatorActivity mainClass;
    private NfcAdapter mNfcAdapter;

	public void onCreate(PresentatorActivity mainClass, Class<? extends PresentatorActivity> class1) {
        
		this.mainClass = mainClass;
		
		mNfcAdapter = NfcAdapter.getDefaultAdapter(mainClass);
		if(mNfcAdapter == null) {return;}
		
		// Handle all of our received NFC intents in this activity.
        mNfcPendingIntent = PendingIntent.getActivity(mainClass, 0,
                new Intent(mainClass, class1).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);    		
	}
	
	public void onResume(Intent intent){
		if(mNfcAdapter == null) {return;}
		enableNFC();  
                
        if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(intent.getAction())){
        	showID(intent);
        } 
	}
	
    private void enableNFC(){
    	
    	IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mWriteTagFilters = new IntentFilter[] {
            tagDetected
        };
        mNfcAdapter.enableForegroundDispatch(mainClass, mNfcPendingIntent, mWriteTagFilters, null);
    }
    
    public void onPause(){
    	if(mNfcAdapter == null) {return;}
    	mNfcAdapter.disableForegroundDispatch(mainClass);
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
	private void showID(Intent intent){
    	String text="tag ID: " + getNFCTagID(intent) +"\n";    	    	    	
    	Log.d(TAG,text);
	}

	public void onNewIntent(Intent intent) {
		Log.d(TAG,"onNewIntent "+intent.getAction());
    	if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())) {
    		showID(intent);
        }		
	}

}
