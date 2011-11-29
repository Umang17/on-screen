package onscreen.presentator;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Observable;
import java.util.Observer;

import onscreen.presentator.communication.BluetoothConnection;
import onscreen.presentator.communication.Connection;
import onscreen.presentator.communication.ConnectionInterface;
import onscreen.presentator.communication.IPConnection;
import onscreen.presentator.communication.TagParser;
import onscreen.presentator.nfc.ConcreteHandleTagDiscover;
import onscreen.presentator.nfc.HandleTagDiscoverWithBlock;
import onscreen.presentator.nfc.ReadNfcTag;
import onscreen.presentator.utility.FileProgressDialog;
import onscreen.presentator.utility.StopWatch;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PresentatorActivity extends Activity implements Observer {

	private Connection mConnection;
	private File mPresentationFile = null;

	public static final int MESSAGE_FILE_REC = 0;
	public static final int MESSAGE_TAKE_OVER = 1;
	public static final int MESSAGE_NO_PRES = 2;
	public static final int MESSAGE_PROGRESS_INC = 3;
	public static final int MESSAGE_PROGRESS_START = 4;
	public static final int MESSAGE_CLOCK = 5;
	public static final int MESSAGE_CONNECTED = 6;
	public static final int MESSAGE_DISCONNECTED = 7;

	public static final String BUNDLE_NAME = "Name";
	public static final String BUNDLE_TIME = "Time";
	public static final String BUNDLE_RUNNING = "Running";

	private static final int STATE_LOAD = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	private static final int STATE_SELECT_SERVER = 4;

	private static final int DIALOG_FILE_PROGRESS = 0;
	private static final int DIALOG_TAKE_OVER = 1;

	// used while taking over
	private boolean running;
	private int time;
	private String name;
	private String tag;

	private final String TAG = "PresentatorActivity";

	private ReadNfcTag readNfcTag;
	private StopWatch stopWatch;
	private FileProgressDialog mFileProgressDialog = null;
	private HandleTagDiscoverWithBlock handleTagIDDiscoverWithBlock;

	private ImageView imageStartStop;

	private final Handler mHandler = new PresentatorHandler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.presentation);

		setControlButtonsVisible(false);

		// Setting up clock
		stopWatch = (StopWatch) findViewById(R.id.chrono);
		imageStartStop = (ImageView) findViewById(R.id.start_stop_image);

		mConnection = new Connection(mHandler);

		ConcreteHandleTagDiscover concreteHandler = new ConcreteHandleTagDiscover();
		concreteHandler.addObserver(this);
		handleTagIDDiscoverWithBlock = new HandleTagDiscoverWithBlock(
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
			dialog = mFileProgressDialog;
			break;
		case DIALOG_TAKE_OVER:
			dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.dialog_take_title)
					.setMessage(R.string.dialog_take_message)
					.setCancelable(false)
					.setPositiveButton(R.string.dialog_continue,
							new OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									onTakeOverClick();
								}
							})
					.setNegativeButton(R.string.dialog_upload,
							new OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									onUploadClick();
								}
							}).create();
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
			mConnection.sendCommand(Connection.COMMAND_NEXT);
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			mConnection.sendCommand(Connection.COMMAND_PREV);
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
	public boolean onPrepareOptionsMenu(Menu menu) {
		boolean connected = mConnection.isConnected();
		MenuItem disconnectItem = menu.findItem(R.id.disconnect);
		if (disconnectItem != null) {
			disconnectItem.setEnabled(connected);
		}
		MenuItem resetTimerItem = menu.findItem(R.id.menu_reset_watch);
		if (resetTimerItem != null) {
			resetTimerItem.setEnabled(connected);
		}
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
			openSelectServer();
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
			String file = data.getStringExtra("File");
			Log.d("debug", file);
			File f = new File(file);
			mPresentationFile = f;
			name = mPresentationFile.getName();
			((TextView) findViewById(R.id.presentationName)).setText(String
					.format(getResources().getString(R.string.selected_file),
							name));
			upload();
			break;
		case REQUEST_ENABLE_BT:
			if (resultCode == RESULT_OK) {
				connect(tag);
			} else {
				// no bluetooth :(
			}
			break;
		case STATE_SELECT_SERVER:
			if (resultCode != RESULT_OK) {
				Log.d("SelectServerReturn", "Not ok");
				break;
			}
			String address = data
					.getStringExtra(SelectServerActivity.SERVER_ADDRESS_INTENT);
			connect(address);
			break;
		}
	}

	public void update(Observable arg0, Object arg1) {
		if (arg0 instanceof ConcreteHandleTagDiscover) {
			String tagID = ((ConcreteHandleTagDiscover) arg0).getTagText();

			connect(tagID);
		}
	}

	private void onUploadClick() {
		dismissDialog(DIALOG_TAKE_OVER);
		upload();
	}

	private void onTakeOverClick() {
		dismissDialog(DIALOG_TAKE_OVER);
		takeOver();
	}

	public void onPrevClick(View v) {
		mConnection.sendCommand(Connection.COMMAND_PREV);
	}

	public void onNextClick(View v) {
		mConnection.sendCommand(Connection.COMMAND_NEXT);
	}

	public void onBlankClick(View v) {
		mConnection.sendCommand(Connection.COMMAND_BLANK);
	}

	public void onStartStopClick(View v) {
		if (stopWatch.running()) {
			mConnection.sendCommand(Connection.COMMAND_PAUSE);
			pauseClockAndSetButtons();
		} else {
			startClockAndSetButtons();
			mConnection.sendCommand(Connection.COMMAND_START);
		}
	}

	private void connect(String tag) {
		ArrayList<ConnectionInterface> connections = TagParser.parse(tag);
		this.tag = tag;

		boolean bluetooth = false;
		boolean ip = false;
		Iterator<ConnectionInterface> iter = connections.iterator();

		while (iter.hasNext()) {
			ConnectionInterface connection = iter.next();

			if (connection instanceof BluetoothConnection) {
				bluetooth = true;
				BluetoothAdapter bluetoothAdapter = BluetoothAdapter
						.getDefaultAdapter();
				if (bluetoothAdapter == null) {
					continue;
				}
				if (bluetoothAdapter.isEnabled()) {
					if (mConnection.getAddr() == null
							|| connection.getAddr().compareTo(
									(mConnection.getAddr())) != 0) {
						mConnection.stop();
						mConnection.connect(connection);
					}
					return;
				}
			} else if (connection instanceof IPConnection) {
				ip = true;
				ConnectivityManager connManager = (ConnectivityManager) getSystemService(PresentatorActivity.CONNECTIVITY_SERVICE);
				NetworkInfo mWifi = connManager
						.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

				if (mWifi.isConnected()) {
					if (mConnection.getAddr() == null
							|| connection.getAddr().compareTo(
									(mConnection.getAddr())) != 0) {
						mConnection.stop();
						mConnection.connect(connection);
					}
					return;
				}
			}

		}
		// tell user that either or neither bluetooth and wifi is on
		CharSequence text;
		int duration = Toast.LENGTH_SHORT;

		if (bluetooth && ip) {
			text = "Please enable bluetooth or connect to wifi and try again!";
		} else if (bluetooth) {
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();

			if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				return;
			}
			text = "Bluetooth unavailable, you can't connect!";
		} else {
			text = "Please connect to wifi and try again!";
		}
		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
	}

	private void setControlButtonsVisible(boolean visible) {
		if (visible) {
			((TextView) findViewById(R.id.textViewInfo)).setText("");
		} else {
			((TextView) findViewById(R.id.textViewInfo))
					.setText(R.string.disconnected_info);
		}
		((Button) findViewById(R.id.blankscreen))
				.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		((LinearLayout) findViewById(R.id.linearLayoutNextPrev))
				.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
		((RelativeLayout) findViewById(R.id.RelativeLayoutClock))
				.setVisibility(visible ? View.VISIBLE : View.INVISIBLE);
	}

	private void upload() {
		Log.d("Handler", "Before sending");
		mConnection.sendPresentation(mPresentationFile);
		Log.d("Handler", "After sending");
	}

	private void takeOver() {
		((TextView) findViewById(R.id.presentationName)).setText(String.format(
				getResources().getString(R.string.presenting_file), name));
		mPresentationFile = null;
		stopWatch.setTime(time);
		Log.d("TIME", "Time is: " + time);
		if (running) {
			mConnection.sendCommand(Connection.COMMAND_START);
			startClockAndSetButtons();
		} else {
			pauseClockAndSetButtons();
		}
	}

	private void resetWatch() {
		mConnection.sendCommand(Connection.COMMAND_RESET);
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

	private void openSelectServer() {
		Intent loadIntent = new Intent(PresentatorActivity.this,
				SelectServerActivity.class);
		startActivityForResult(loadIntent, STATE_SELECT_SERVER);
	}

	private class PresentatorHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_CONNECTED:
				setControlButtonsVisible(true);
				break;
			case MESSAGE_DISCONNECTED:
				// Toast.makeText(PresentatorActivity.this,
				// R.string.disconnected_message, Toast.LENGTH_LONG)
				// .show();
				Log.d(TAG, "Disconnected from: " + mConnection.getAddr());
				if (mFileProgressDialog != null) {
					if (mFileProgressDialog.isShowing()) {
						dismissDialog(DIALOG_FILE_PROGRESS);
					}
				}
				if (mPresentationFile == null) {
					((TextView) findViewById(R.id.presentationName))
							.setText(R.string.no_selected_presentation);
				} else {
					((TextView) findViewById(R.id.presentationName))
							.setText(String.format(
									getResources().getString(
											R.string.selected_file),
									mPresentationFile.getName()));
				}
				setControlButtonsVisible(false);
				break;
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
				((TextView) findViewById(R.id.presentationName)).setText(String
						.format(getResources().getString(
								R.string.presenting_file), name));
				dismissDialog(DIALOG_FILE_PROGRESS);
				stopWatch.resetClock();
				startClockAndSetButtons();
				break;
			case MESSAGE_PROGRESS_START:
				Log.d("Handler", "progress start...");
				((TextView) findViewById(R.id.presentationName)).setText(String
						.format(getResources().getString(
								R.string.transferring_file), name));
				showDialog(DIALOG_FILE_PROGRESS);
				mFileProgressDialog.setFileSize((Long) msg.obj);
				break;
			case MESSAGE_PROGRESS_INC:
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