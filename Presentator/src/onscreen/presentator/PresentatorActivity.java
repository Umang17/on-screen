package onscreen.presentator;

import java.io.File;

import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.stickynotes.ReadNfcTag;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class PresentatorActivity extends Activity {

	private Bluetooth mBluetooth;
	private File mPresentationFile = null;

	public static final int MESSAGE_CONNECTED = 1;
	public static final int MESSAGE_READ = 2;
	public static final int MESSAGE_WRITE = 3;
	public static final int MESSAGE_TOAST = 5;

	public static final int STATE_TAKE_OVER = 1;
	public static final int STATE_LOAD = 2;

	public int state = 0;
	
	private ReadNfcTag readNfcTag;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MESSAGE_CONNECTED:
				if (state == STATE_LOAD) {
					// mBluetooth.sendPresentation(file);
				} else if (state == STATE_TAKE_OVER) {
					mBluetooth.requestControl();
				}
				break;

			case MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				// take care of the input from computer
				break;
			}

		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.start);
		
		readNfcTag=new ReadNfcTag();
		readNfcTag.onCreate(this, getClass());

		mBluetooth = new Bluetooth(mHandler);

		Button load = (Button) findViewById(R.id.load);
		load.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// some action

				state = STATE_LOAD;
			}
		});

		Button take_over = (Button) findViewById(R.id.take_over);
		take_over.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// some action

				state = STATE_TAKE_OVER;
			}
		});

		Button pref = (Button) findViewById(R.id.preferences);
		pref.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// some action
			}
		});

		Button exit = (Button) findViewById(R.id.exit);
		exit.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// closes the program
				finish();
			}
		});

	}
	
	@Override
    protected void onResume() {
        super.onResume();
        
        readNfcTag.onResume(getIntent());
	}
	
	@Override
    protected void onNewIntent(Intent intent) {
    	readNfcTag.onNewIntent(intent);
	}
}