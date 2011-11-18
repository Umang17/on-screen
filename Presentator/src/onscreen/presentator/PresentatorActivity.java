package onscreen.presentator;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Chronometer.OnChronometerTickListener;

import java.util.logging.Level;
import java.util.logging.Logger;

public class PresentatorActivity extends Activity {

	private Bluetooth mBluetooth;
	private File mPresentationFile = null;

	public static final int MESSAGE_FILE_REC = 0;
	public static final int MESSAGE_TAKE_OVER = 1;
	public static final int MESSAGE_NO_PRES = 2;
	public static final int MESSAGE_PROGRESS_INC = 3;
	public static final int MESSAGE_PROGRESS_START = 4;
	
	public static final String BUNDLE_NAME = "Name";
	public static final String BUNDLE_TIME = "Time";
	public static final String BUNDLE_CURRENT_SLIDE = "CSlide";
	public static final String BUNDLE_TOTAL_SLIDE = "TSlide";

	public static final int STATE_TAKE_OVER = 1;
	public static final int STATE_LOAD = 2;

	public int state = 0;

	private final String TAG = "PresentatorActivity";

	private ReadNfcTag readNfcTag;
	private ProgressDialog mProgressDialog;

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
				}
				break;

			case MESSAGE_FILE_REC:
				Log.d("Handler", "file rec...");
				mProgressDialog.cancel();
				TextView view = (TextView) findViewById(R.id.presentationName);
				view.setText(mPresentationFile.getName());
				// start clock
				break;

			case MESSAGE_PROGRESS_START:
				Log.d("Handler", "progress start...");
				mProgressDialog.setProgress(0);
				// maybe use setMax...
				mProgressDialog.show();

			case MESSAGE_PROGRESS_INC:
				Log.d("Handler", "progress inc...");
				// maybe incr with the size of the BYTE_SIZE
				mProgressDialog.setProgress(msg.arg1);
			}
		}
	};

	private stopWatch stopWatch;

	private class stopWatch{
		private Chronometer chrono;
		private Button btnStart;
		private Button btnPause;
		private boolean resume=false;
		private String currentTime="";	
		private Long currentTimeLastStop;
		
		public stopWatch(final Chronometer chrono, Button btnStart, Button btnPause){
			this.chrono=chrono;
			this.btnStart=btnStart;
			this.btnPause=btnPause;
			
			btnPause.setEnabled(false);
			
			chrono.setOnChronometerTickListener(new OnChronometerTickListener() {

				public void onChronometerTick(Chronometer arg0) {
					
						long seconds = (SystemClock.elapsedRealtime() - chrono.getBase()) / 1000;
						
						long hour = seconds/3600;
						if(hour>=10) {
							chrono.setBase(chrono.getBase() - seconds*3600*1000);
							seconds -= hour*3600;
							hour = 0;
						}
						seconds -= hour*3600;
						long minutes = seconds/60;
						seconds -= minutes*60;
						
						currentTime = hour+":"
										+(minutes<10?"0"+minutes:minutes)+":"
										+(seconds<10?"0"+seconds:seconds);
						arg0.setText(currentTime);
				}
			});
			chrono.setText("0:00:00");			
		}
		
		public void handleButtonClick(View v) {
			switch (v.getId()) {
				case R.id.start:
					btnPause.setEnabled(true);
					btnStart.setEnabled(false);
					if (!resume) {
						chrono.setBase(SystemClock.elapsedRealtime());
						chrono.start();
					} else {
						long time=chrono.getBase()+SystemClock.elapsedRealtime()-currentTimeLastStop;
						chrono.setBase(time);
						chrono.start();
					}				
					break;
					
				case R.id.pause:				
					btnStart.setEnabled(true);
					btnPause.setEnabled(false);
					chrono.stop();
					resume = true;
					btnStart.setText("Resume");
					currentTimeLastStop=SystemClock.elapsedRealtime();
					break;
					
				case R.id.reset:
					chrono.stop();
					chrono.setText("0:00:00");
					btnStart.setText("Start");
					resume = false;
					currentTimeLastStop=SystemClock.elapsedRealtime();
					btnStart.setEnabled(true);				
					btnPause.setEnabled(false);
					break;
			}
		
		
		}
		/**
		 * 
		 * @return the displayed time in seconds
		 */
		public Long getStopWatchTime() {
			if (resume) {
				return (chrono.getBase() + SystemClock.elapsedRealtime() - currentTimeLastStop) / 1000;
			} else {
				return (SystemClock.elapsedRealtime() - chrono.getBase()) / 1000;
			}
		}
	}

	

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.presentation);

		readNfcTag = new ReadNfcTag();
		readNfcTag.onCreate(this, getClass());

		//Setting up clock
		Chronometer chrono = (Chronometer) findViewById(R.id.chrono);
		Button btnStart = (Button) findViewById(R.id.start);
		Button btnPause = (Button) findViewById(R.id.pause);
		stopWatch = new stopWatch(chrono,btnStart,btnPause);

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
				stopWatch.handleButtonClick(v);
			}
		});

		Button pause = (Button) findViewById(R.id.pause);
		pause.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				stopWatch.handleButtonClick(v);
			}
		});

		Button reset = (Button) findViewById(R.id.reset);
		reset.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				stopWatch.handleButtonClick(v);
			}
		});

		mProgressDialog = new ProgressDialog(this);
		mProgressDialog.setTitle("File transfering....");
		mProgressDialog.setCancelable(false); // can't cancel with back button
		mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

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
			mBluetooth.sendPresentation(mPresentationFile);
			break;
		}
	}
	
	public void handleTagIDDiscover(String bluetoothAdress){
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