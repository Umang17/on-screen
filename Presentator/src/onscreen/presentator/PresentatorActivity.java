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
				mBluetooth.sendPresentation(mPresentationFile);
				break;

			case MESSAGE_TAKE_OVER:
				Log.d("Handler", "taking over");
				byte[] readBuf = (byte[]) msg.obj; // shuld maybe not be
													// bytes...
				// take care of the input from computer
				// should output a dialog asking if user want to take over or
				// send a new presentation. But only if a presentation already
				// is loaded.
				if (mPresentationFile != null) {
					// give user options...
				} else {
					mBluetooth.requestControl();
				}
				break;

			case MESSAGE_FILE_REC:
				Log.d("Handler", "file rec...");
				mProgressDialog.cancel();
				break;

			case MESSAGE_PROGRESS_START:
				mProgressDialog.setProgress(0);
				// maybe use setMax...
				mProgressDialog.show();

			case MESSAGE_PROGRESS_INC:
				// maybe incr with the size of the BYTE_SIZE
				mProgressDialog.incrementProgressBy(1);
			}
		}
	};

	private Chronometer chrono;
	private Button btnStart;
	private Button btnPause;
	private boolean resume=false;
	private String currentTime="";	
	private Long currentTimeLastStop;
	private long elapsedTime=0;

	private void setClock() {
		chrono = (Chronometer) findViewById(R.id.chrono);
		btnStart = (Button) findViewById(R.id.start);
		btnPause = (Button) findViewById(R.id.pause);
		btnPause.setEnabled(false);
		
		chrono.setOnChronometerTickListener(new OnChronometerTickListener() {

			public void onChronometerTick(Chronometer arg0) {
				
				//if(!resume){	
						long seconds = (SystemClock.elapsedRealtime() - chrono.getBase())/1000;
						
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
						Log.d(TAG, currentTime);
						arg0.setText(currentTime);
				/*		elapsedTime=SystemClock.elapsedRealtime();
				} else {
					long seconds = (elapsedTime - chrono.getBase())/1000;
					
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
					Log.d(TAG, currentTime);
					arg0.setText(currentTime);
					
					elapsedTime=elapsedTime+1000;
				}*/
			}
		});
		chrono.setText("0:00:00");
	}

	private void handleButtonClick(View v) {
		switch (v.getId()) {
			case R.id.start:
				
				btnPause.setEnabled(true);
				btnStart.setEnabled(false);
				if (!resume) {
					Log.d(TAG, "Starting");
					chrono.setBase(SystemClock.elapsedRealtime());
					chrono.start();
				} else {
					Log.d(TAG, "Resuming");
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

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.presentation);

		readNfcTag = new ReadNfcTag();
		readNfcTag.onCreate(this, getClass());

		setClock();

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
				handleButtonClick(v);
			}
		});

		Button pause = (Button) findViewById(R.id.pause);
		pause.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				handleButtonClick(v);
			}
		});

		Button reset = (Button) findViewById(R.id.reset);
		reset.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				handleButtonClick(v);
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
}