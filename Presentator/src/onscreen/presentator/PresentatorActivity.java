package onscreen.presentator;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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
					try {
						mBluetooth.sendPresentation(mPresentationFile);
					} catch (IOException e) {
						e.printStackTrace();
					}
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
		setContentView(R.layout.presentation);

		readNfcTag = new ReadNfcTag();
		readNfcTag.onCreate(this, getClass());
		
		mBluetooth = new Bluetooth(mHandler);

		Button prev = (Button) findViewById(R.id.prev);
		prev.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// some action
				mBluetooth.sendPrev();
//				Intent loadIntent = new Intent(PresentatorActivity.this, SelectPDFActivity.class);
//				startActivityForResult(loadIntent, STATE_LOAD);
			}
		});

		Button next = (Button) findViewById(R.id.next);
		next.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// some action
				mBluetooth.sendNext();
//				state = STATE_TAKE_OVER;
			}
		});

		Button start = (Button) findViewById(R.id.start);
		start.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// some action
			}
		});

		Button pause = (Button) findViewById(R.id.pause);
		pause.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				// closes the program
				finish();
			}
		});
		
		Button reset = (Button) findViewById(R.id.reset);
		reset.setOnClickListener(new OnClickListener() {

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
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case STATE_LOAD:
			if (resultCode != RESULT_OK) {
				break;
			}
			String file = data.getStringExtra("File");
			Log.d("debug", file);
			File f = new File(file);
			mPresentationFile = f;
			state = STATE_LOAD;
			if (mBluetooth.isConnected()) {
				try {
					mBluetooth.sendPresentation(mPresentationFile);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
		}
	}
}