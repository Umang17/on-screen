package onscreen.presentator;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;

public class PresentatorActivity extends Activity {

	private Bluetooth mBluetooth;
	private File mPresentationFile = null;

	public static final int MESSAGE_FILE_REC = 0;
	public static final int MESSAGE_TAKE_OVER = 1;
	public static final int MESSAGE_NO_PRES = 2;
	public static final int MESSAGE_PROGRESS_INC = 3;
	public static final int MESSAGE_PROGRESS_START = 4;
	public static final int MESSAGE_CLOCK = 5;
	
	public static final String BUNDLE_NAME = "Name";
	public static final String BUNDLE_TIME = "Time";
	public static final String BUNDLE_CURRENT_SLIDE = "CSlide";
	public static final String BUNDLE_TOTAL_SLIDE = "TSlide";
	public static final String BUNDLE_RUNNING = "Running";

	public static final int STATE_TAKE_OVER = 1;
	public static final int STATE_LOAD = 2;

	public int state = 0;

	//private final String TAG = "PresentatorActivity";

	private ReadNfcTag readNfcTag;
	private StopWatch stopWatch;	
	private FileProgressDialog mFileProgressDialog;
	private HandleTagIDDiscoverWithBlock handleTagIDDiscoverWithBlock;
	
	private Button btnStart;
	private Button btnPause;


	private final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MESSAGE_NO_PRES:
				Log.d("Handler", "no pres!");
				if (mPresentationFile == null) {
					break;
				}
				mBluetooth.sendPresentation(mPresentationFile);
				break;

			case MESSAGE_TAKE_OVER:
				Log.d("Handler", "taking over");
				Bundle bundle = (Bundle) msg.obj;
				String name = bundle.getString(BUNDLE_NAME);
				int time = bundle.getInt(BUNDLE_TIME);
				int currentSlide = bundle.getInt(BUNDLE_CURRENT_SLIDE);
				int totalNrOfSlides = bundle.getInt(BUNDLE_TOTAL_SLIDE);
				boolean running = bundle.getBoolean(BUNDLE_RUNNING);
				
				
				// should output a dialog asking if user want to take over or
				// send a new presentation. But only if a presentation already
				// is loaded.
				if (mPresentationFile != null) {
					// give user options...
					Log.d("Handler", "Before sending");
					mBluetooth.sendPresentation(mPresentationFile);
					Log.d("Handler", "After sending");
				} else {
					// set time
					TextView view = (TextView) findViewById(R.id.presentationName);
					view.setText(name);
					stopWatch.setBaseTime(time);
					if (running) {
						startClockAndSetButtons();
					} else {
						pauseClockAndSetButtons();
					}
				}
				break;

			case MESSAGE_FILE_REC:
				Log.d("Handler", "file rec...");
				mFileProgressDialog.cancel();
				TextView view = (TextView) findViewById(R.id.presentationName);
				view.setText(mPresentationFile.getName());
				stopWatch.resetClock();
				startClockAndSetButtons();
				break;

			case MESSAGE_PROGRESS_START:
				Log.d("Handler", "progress start...");
				mFileProgressDialog.setFileSize((Long)msg.obj);
				// maybe use setMax...
				mFileProgressDialog.show();
				break;

			case MESSAGE_PROGRESS_INC:
				// maybe incr with the size of the BYTE_SIZE
				mFileProgressDialog.setProgress((Long)msg.obj);
				break;
			
			case MESSAGE_CLOCK:
				boolean runningClock = msg.arg1 == 1 ? true : false;
				boolean reset = msg.arg2 == 1 ? true : false;
				
				if (reset) {
					resetClockAndSetButtons();
				}
				if (runningClock) {
					startClockAndSetButtons();
				} else {
					pauseClockAndSetButtons();
				}
				break;
			}
		}
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.presentation);		
		
		//Setting up clock
		Chronometer chrono = (Chronometer) findViewById(R.id.chrono);
		btnStart = (Button) findViewById(R.id.start);
		btnPause = (Button) findViewById(R.id.pause);
		
		stopWatch = new StopWatch(chrono);
		btnPause.setEnabled(false);	

		mBluetooth = new Bluetooth(mHandler);
		
		handleTagIDDiscoverWithBlock = new HandleTagIDDiscoverWithBlock(
				new ConcreteHandleTagIDDiscover(mBluetooth));

		readNfcTag = new ReadNfcTag(handleTagIDDiscoverWithBlock);
		readNfcTag.onCreate(this);

		mFileProgressDialog = new FileProgressDialog(this, 0);
		mFileProgressDialog.setCancelable(false); // can't cancel with back button

	}
	
	public void onPrevClick(View v) {
		mBluetooth.sendPrev();
	}
	
	public void onNextClick(View v) {
		mBluetooth.sendNext();
	}
	
	public void onBlankClick(View v) {
		mBluetooth.sendBlank();
	}
	
	public void onStartClick(View v) {
		startClockAndSetButtons();
		mBluetooth.sendStartClock();
		
	}
	
	public void onPauseClick(View v) {
		mBluetooth.sendPauseClock();
		pauseClockAndSetButtons();
		
	}
	
	public void onResetClick(View v) {
		mBluetooth.sendResetClock();
		resetClockAndSetButtons();
	}
	
	public void onPresentationClick(View v) {
		openSelectPresentation();
	}

	private void startClockAndSetButtons() {
		btnPause.setEnabled(true);
		btnStart.setEnabled(false);	
		stopWatch.startClock();
	}

	private void pauseClockAndSetButtons() {
		btnStart.setEnabled(true);
		btnPause.setEnabled(false);
		btnStart.setText(R.string.resume_button);
		stopWatch.pauseClock();
	}

	private void resetClockAndSetButtons() {
		btnStart.setEnabled(true);				
		btnPause.setEnabled(false);
		btnStart.setText(R.string.start_button);
		stopWatch.resetClock();
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
	
	private void openSelectPresentation() {
		// Start the PDF selector
		Intent loadIntent = new Intent(PresentatorActivity.this, SelectPDFActivity.class);
		startActivityForResult(loadIntent, STATE_LOAD);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.open_presentation:
			openSelectPresentation();
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
	protected void onPause() {
		mFileProgressDialog.cancel();
		readNfcTag.onPause();
		super.onPause();
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
			mBluetooth.sendPresentation(mPresentationFile);
			break;
		}
	}
}