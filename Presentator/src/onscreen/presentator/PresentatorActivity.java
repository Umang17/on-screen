package onscreen.presentator;

import java.io.File;
import java.util.Observable;
import java.util.Observer;

import onscreen.presentator.communication.Connection;
import onscreen.presentator.communication.ConnectionInterface;
import onscreen.presentator.communication.TagParser;
import onscreen.presentator.nfc.ConcreteHandleTagIDDiscover;
import onscreen.presentator.nfc.HandleTagIDDiscoverWithBlock;
import onscreen.presentator.nfc.ReadNfcTag;
import onscreen.presentator.utility.FileProgressDialog;
import onscreen.presentator.utility.StopWatch;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PresentatorActivity extends Activity implements Observer {

	private Connection mConnection;
	private File mPresentationFile = null;

	public static final int MESSAGE_FILE_REC = 0;
	public static final int MESSAGE_TAKE_OVER = 1;
	public static final int MESSAGE_NO_PRES = 2;
	public static final int MESSAGE_PROGRESS_INC = 3;
	public static final int MESSAGE_PROGRESS_START = 4;
	public static final int MESSAGE_CLOCK = 5;

	public static final String BUNDLE_NAME = "Name";
	public static final String BUNDLE_TIME = "Time";
	public static final String BUNDLE_RUNNING = "Running";

	public static final int STATE_TAKE_OVER = 1;
	public static final int STATE_LOAD = 2;

	private static final int DIALOG_FILE_PROGRESS = 0;
	private static final int DIALOG_TAKE_OVER = 1;

	public int state = 0;

	// used while taking over
	private boolean running;
	private int time;
	private String name;

	private final String TAG = "PresentatorActivity";

	private ReadNfcTag readNfcTag;
	private StopWatch stopWatch;
	private FileProgressDialog mFileProgressDialog = null;
	private HandleTagIDDiscoverWithBlock handleTagIDDiscoverWithBlock;

	private ImageView imageStartStop;

	private final Handler mHandler = new PresentatorHandler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final boolean customTitleSupported = requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);

		setContentView(R.layout.presentation);

		if (customTitleSupported) {
			getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
					R.layout.titlebar);
		}

		setControlButtons(false);

		// Setting up clock
		stopWatch = (StopWatch) findViewById(R.id.chrono);
		imageStartStop = (ImageView) findViewById(R.id.start_stop_image);

		mConnection = new Connection(mHandler);

		ConcreteHandleTagIDDiscover concreteHandler = new ConcreteHandleTagIDDiscover();
		concreteHandler.addObserver(this);
		handleTagIDDiscoverWithBlock = new HandleTagIDDiscoverWithBlock(
				concreteHandler);

		readNfcTag = new ReadNfcTag(handleTagIDDiscoverWithBlock);
		readNfcTag.onCreate(this);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_FILE_PROGRESS:
			mFileProgressDialog = new FileProgressDialog(this, 0);
			mFileProgressDialog.setCancelable(false);
			dialog = mFileProgressDialog;
			break;
		case DIALOG_TAKE_OVER:
			dialog = new Dialog(this);
			LinearLayout ll = (LinearLayout) LayoutInflater.from(this).inflate(
					R.layout.take_over_dialog, null);
			dialog.setContentView(ll);
			break;
		default:
			dialog = null;
			break;
		}
		return dialog;
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
			mConnection.sendNext();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			mConnection.sendPrev();
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
			openSelectPresentation();
			return true;
		case R.id.menu_reset_watch:
			resetWatch();
			return true;
		case R.id.connect:
			ConnectionInterface connection = TagParser.parse("bla");
			if (connection.getAddr() != mConnection.getAddr()) {
				mConnection.stop();
				mConnection.connect(connection);
			}
			// TODO remove
			Log.d(TAG, "After connect");
			return true;
		case R.id.disconnect:
			mConnection.stop();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onPause() {
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
			// TODO Fix the enabling / disabling of buttons
			setControlButtons(true);
			String file = data.getStringExtra("File");
			Log.d("debug", file);
			File f = new File(file);
			mPresentationFile = f;
			TextView view = (TextView) findViewById(R.id.presentationName);
			view.setText(mPresentationFile.getName());
			upload();
			break;
		}
	}

	public void update(Observable arg0, Object arg1) {
		if (arg0 instanceof ConcreteHandleTagIDDiscover) {
			String tagID = ((ConcreteHandleTagIDDiscover) arg0).getTag();

			ConnectionInterface connection = TagParser.parse(tagID);

			if (connection.getAddr() != mConnection.getAddr()) {
				mConnection.stop();
				mConnection.connect(connection);
			}
		}
	}

	public void onUploadClick(View v) {
		dismissDialog(DIALOG_TAKE_OVER);
		upload();
	}

	public void onTakeOverClick(View v) {
		dismissDialog(DIALOG_TAKE_OVER);
		takeOver();
	}

	public void onPrevClick(View v) {
		mConnection.sendPrev();
	}

	public void onNextClick(View v) {
		mConnection.sendNext();
	}

	public void onBlankClick(View v) {
		mConnection.sendBlank();
	}

	public void onStartStopClick(View v) {
		switch (stopWatch.getState()) {
		case RUNNING:
			mConnection.sendPauseClock();
			pauseClockAndSetButtons();
			break;
		case PAUSED:
		case STOPPED:
			startClockAndSetButtons();
			mConnection.sendStartClock();
			break;
		}
	}

	public void onPresentationClick(View v) {
		openSelectPresentation();
	}

	private void setControlButtons(boolean enable) {
		((Button) findViewById(R.id.blankscreen)).setEnabled(enable);
		((ImageButton) findViewById(R.id.next)).setEnabled(enable);
		((ImageButton) findViewById(R.id.prev)).setEnabled(enable);
	}

	private void upload() {
		Log.d("Handler", "Before sending");
		mConnection.sendPresentation(mPresentationFile);
		Log.d("Handler", "After sending");
	}

	private void takeOver() {
		TextView view = (TextView) findViewById(R.id.presentationName);
		view.setText(name);
		stopWatch.setBaseTime(time);
		Log.d("TIME", "Time is: " + time);
		if (running) {
			mConnection.sendStartClock();
			startClockAndSetButtons();
		} else {
			pauseClockAndSetButtons();
		}
	}

	private void resetWatch() {
		mConnection.sendResetClock();
		resetClockAndSetButtons();
	}

	private void startClockAndSetButtons() {
		imageStartStop.setImageResource(R.drawable.ic_media_pause);
		stopWatch.startClock();
	}

	private void pauseClockAndSetButtons() {
		imageStartStop.setImageResource(R.drawable.ic_media_play);
		stopWatch.pauseClock();
	}

	private void resetClockAndSetButtons() {
		imageStartStop.setImageResource(R.drawable.ic_media_play);
		stopWatch.resetClock();
	}

	private void openSelectPresentation() {
		// Start the PDF selector
		Intent loadIntent = new Intent(PresentatorActivity.this,
				SelectPDFActivity.class);
		startActivityForResult(loadIntent, STATE_LOAD);
	}

	private class PresentatorHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case MESSAGE_NO_PRES:
				Log.d("Handler", "no pres!");
				if (mPresentationFile == null) {
					break;
				}
				upload();
				break;

			case MESSAGE_TAKE_OVER:
				Log.d("Handler", "taking over");
				Bundle bundle = (Bundle) msg.obj;
				name = bundle.getString(BUNDLE_NAME);
				time = bundle.getInt(BUNDLE_TIME);
				running = bundle.getBoolean(BUNDLE_RUNNING);

				if (mPresentationFile != null) {
					showDialog(DIALOG_TAKE_OVER);
				} else {
					takeOver();
				}
				break;

			case MESSAGE_FILE_REC:
				Log.d("Handler", "file rec...");
				dismissDialog(DIALOG_FILE_PROGRESS);
				stopWatch.resetClock();
				startClockAndSetButtons();
				break;

			case MESSAGE_PROGRESS_START:
				Log.d("Handler", "progress start...");
				// maybe use setMax...
				showDialog(DIALOG_FILE_PROGRESS);
				mFileProgressDialog.setFileSize((Long) msg.obj);
				break;

			case MESSAGE_PROGRESS_INC:
				// maybe incr with the size of the BYTE_SIZE
				mFileProgressDialog.setProgress((Long) msg.obj);
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
	}
}