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
import onscreen.presentator.utility.FileProgress;
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
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Main class for this program. Sets everything up and handles the UI etc.
 * 
 * @author Elias Näslund, John Viklund and Viktor Lindgren
 *
 */
public class PresentatorActivity extends Activity implements Observer {

	private Connection mConnection;
	private File mPresentationFile = null;

	// MESSAGE_* is used for sending messages to the Handler
	public static final int MESSAGE_FILE_REC = 0;
	public static final int MESSAGE_TAKE_OVER = 1;
	public static final int MESSAGE_NO_PRES = 2;
	public static final int MESSAGE_PROGRESS_INC = 3;
	public static final int MESSAGE_PROGRESS_START = 4;
	public static final int MESSAGE_CLOCK = 5;
	public static final int MESSAGE_CONNECTED = 6;
	public static final int MESSAGE_DISCONNECTED = 7;
	public static final int MESSAGE_CONNECTION_LOST = 8;
	public static final int MESSAGE_CONNECTION_FAILED = 9;

	// Used for sending a bundle to the Handler
	public static final String BUNDLE_NAME = "Name";
	public static final String BUNDLE_TIME = "Time";
	public static final String BUNDLE_RUNNING = "Running";

	// Used in onActivityResult
	private static final int STATE_LOAD = 2;
	private static final int REQUEST_ENABLE_BT = 3;
	private static final int STATE_SELECT_SERVER = 4;

	// Used for dialogs
	private static final int DIALOG_FILE_PROGRESS = 0;
	private static final int DIALOG_TAKE_OVER = 1;

	// Used while taking over
	private boolean running;
	private int time;
	private String name;
	private String tag;

	// Used for debugging
	// private final String TAG = "PresentatorActivity";

	private ReadNfcTag readNfcTag;
	private StopWatch stopWatch;
	private FileProgress mFileProgress = null;
	private HandleTagDiscoverWithBlock handleTagIDDiscoverWithBlock;

	private ImageView imageStartStop;

	private final Handler mHandler = new PresentatorHandler();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.presentation);
		
		getWindow().addFlags(LayoutParams.FLAG_KEEP_SCREEN_ON);

		setControlButtonsVisible(false);

		// Setting up clock
		stopWatch = (StopWatch) findViewById(R.id.chrono);
		imageStartStop = (ImageView) findViewById(R.id.start_stop_image);

		// Setting up the object used for the connection to the server
		mConnection = new Connection(mHandler);

		// Used for reading RFID/NFC-tags
		ConcreteHandleTagDiscover concreteHandler = new ConcreteHandleTagDiscover();
		concreteHandler.addObserver(this);
		handleTagIDDiscoverWithBlock = new HandleTagDiscoverWithBlock(
				concreteHandler);

		readNfcTag = new ReadNfcTag(handleTagIDDiscoverWithBlock);
		readNfcTag.onCreate(this);

	}

	/**
	 * Handles dialogs
	 */
	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_FILE_PROGRESS:
			mFileProgress = new FileProgress(this);
			dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.file_progress_dialog)
					.setMessage(R.string.file_progress_message)
					.setView(mFileProgress.getView())
					.setCancelable(false)
					.setNegativeButton(R.string.cancel_transfer,
							new OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									onCancelTransferClick();
								}
							}).create();
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

	/**
	 * Override the functionality of the volume button. Can't change volume with
	 * them anymore.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_UP
				|| keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * Override the functionality of the volume button. Next/Prev when using
	 * volumebuttons.
	 */
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

	/**
	 * Our own menu.
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return true;
	}

	/**
	 * Set some menu buttons disabled.
	 */
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

	/**
	 * Decides what happens when selecting menu item.
	 */
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

	/**
	 * When reading a NFC-tag, open this program!
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		readNfcTag.onNewIntent(intent);
	}

	/**
	 * Handle the result of different intents.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		// Intent of choosing pdf file returns
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
		// Intent of starting bluetooth returns
		case REQUEST_ENABLE_BT:
			if (resultCode == RESULT_OK) {
				connect(tag);
			} else {
				// no bluetooth :(
			}
			break;
		// Intent of selecting server returns
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

	/**
	 * Called when a nfc-tag is read.
	 */
	public void update(Observable arg0, Object arg1) {
		if (arg0 instanceof ConcreteHandleTagDiscover) {
			String tagID = ((ConcreteHandleTagDiscover) arg0).getTagText();

			connect(tagID);
		}
	}

	private void onCancelTransferClick() {
		dismissDialog(DIALOG_FILE_PROGRESS);
		mConnection.stop();
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

	/**
	 * Pares the NFC-tag and then tries to connect to the addresses.
	 * 
	 * @param tag
	 */
	private void connect(String tag) {
		ArrayList<ConnectionInterface> connections = TagParser.parse(tag);
		if (connections == null) {
			return;
		}
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
					} else if (connection.getAddr().compareTo(
							mConnection.getAddr()) == 0) {
						mConnection.stop();
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
					} else if (connection.getAddr().compareTo(
							mConnection.getAddr()) == 0) {
						mConnection.stop();
					}
					return;
				}
			}

		}
		// tell user that either or neither bluetooth and wifi is on
		CharSequence text;
		int duration = Toast.LENGTH_SHORT;

		if (bluetooth && ip) {
			text = getString(R.string.neither_bluetooth_or_wifi);
		} else if (bluetooth) {
			BluetoothAdapter bluetoothAdapter = BluetoothAdapter
					.getDefaultAdapter();

			if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
				return;
			}
			text = getString(R.string.bluetooth_disabled);
		} else {
			text = getString(R.string.wifi_disabled);
		}

		Toast toast = Toast.makeText(this, text, duration);
		toast.show();
	}

	/**
	 * Makes screen buttons disabled and enabled.
	 * 
	 * @param visible
	 */
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
		// Log.d("Handler", "Before sending");
		mConnection.sendPresentation(mPresentationFile);
		// Log.d("Handler", "After sending");
	}

	/**
	 * Called when taking over an existing presentation
	 */
	private void takeOver() {
		((TextView) findViewById(R.id.presentationName)).setText(String.format(
				getResources().getString(R.string.presenting_file), name));
		mPresentationFile = null;
		stopWatch.setTime(time);
		// Log.d("TIME", "Time is: " + time);
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

	/**
	 * Handler for communication from the connection object.
	 * 
	 * @author Elias Näslund
	 * 
	 */
	private class PresentatorHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_CONNECTED:
				setControlButtonsVisible(true);
				break;
			case MESSAGE_CONNECTION_FAILED:
				Toast.makeText(PresentatorActivity.this,
						R.string.connection_failed, Toast.LENGTH_LONG).show();
				break;
			case MESSAGE_CONNECTION_LOST:
				Toast.makeText(PresentatorActivity.this,
						R.string.connection_lost, Toast.LENGTH_LONG).show();
				break;
			case MESSAGE_DISCONNECTED:
				// Log.d(TAG, "Disconnected from: " + mConnection.getAddr());
				if (mFileProgress != null) {
					dismissDialog(DIALOG_FILE_PROGRESS);
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
				mFileProgress.setFileSize((Long) msg.obj);
				break;
			case MESSAGE_PROGRESS_INC:
				mFileProgress.setProgress((Long) msg.obj);
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