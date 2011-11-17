package onscreen.presentator;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PresentatorActivity extends Activity {

	private Bluetooth mBluetooth;
	private File mPresentationFile = null;

	public static final int MESSAGE_FILE_REC = 0;
	public static final int MESSAGE_TAKE_OVER = 1;
	public static final int MESSAGE_NO_PRES = 2;

	public static final int STATE_TAKE_OVER = 1;
	public static final int STATE_LOAD = 2;

	public int state = 0;

	private ReadNfcTag readNfcTag;

	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MESSAGE_NO_PRES:
				Log.d("Handler", "no pres!");
				// try {
				// mBluetooth.sendPresentation(mPresentationFile);
				// } catch (IOException e) {
				// e.printStackTrace();
				// }
				break;

			case MESSAGE_TAKE_OVER:
				Log.d("Handler", "taking over");
				byte[] readBuf = (byte[]) msg.obj;
				// take care of the input from computer
				break;

			case MESSAGE_FILE_REC:
				Log.d("Handler", "file rec...");
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
				mBluetooth.sendPrev();
			}
		});

		Button next = (Button) findViewById(R.id.next);
		next.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mBluetooth.sendNext();
			}
		});

		Button blank = (Button) findViewById(R.id.blankscreen);
		blank.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				mBluetooth.sendBlank();
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
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
				|| keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			mBluetooth.sendNext();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			mBluetooth.sendPrev();
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.open_presentation:
			// Start the PDF selector
			Intent loadIntent = new Intent(PresentatorActivity.this,
					SelectPDFActivity.class);
			startActivityForResult(loadIntent, STATE_LOAD);
			return true;
		case R.id.open_settings:
			// TODO
			return true;
		case R.id.connect:
			try {
				mBluetooth.connect("00:1F:E1:EB:3B:DE");
			} catch (IOException ex) {
				Logger.getLogger(PresentatorActivity.class.getName()).log(
						Level.SEVERE, null, ex);
			}
			return true;
		case R.id.disconnect:
			mBluetooth.stop();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
				Log.d("SelectPDFReturn", "Not ok");
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