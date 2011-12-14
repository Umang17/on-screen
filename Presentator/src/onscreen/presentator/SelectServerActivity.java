package onscreen.presentator;

import onscreen.presentator.nfc.ConcreteHandleTagDiscover;
import onscreen.presentator.nfc.HandleTagDiscoverWithBlock;
import onscreen.presentator.nfc.ReadNfcTag;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class SelectServerActivity extends Activity {
	public static final String SERVER_ADDRESS_INTENT = "ServerAddress";

	private static final String TAG = "SelectServer";
	private static final String PREFS_NAME = "ServerList";
	private static final String SERVER_COUNT = "serverCount";
	private static final String SERVER_ADDRESS = "serverAddress";
	private static final String SERVER_NAME = "serverName";

	private static final int ID_CONNECT = 0;
	private static final int ID_EDIT = 1;
	private static final int ID_DELETE = 2;

	private static final int EDIT_DIALOG = 0;

	private ServerInfoAdapter serverAdapter;
	private ServerInfo editingItem;
	
	private ReadNfcTag readNfcTag;

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				// Add the name and address to the adapter
				String address = device.getAddress();
				for (int i = 0; i < serverAdapter.getCount(); i++) {
					if (serverAdapter.getItem(i).address.compareTo(address) == 0) {
						// Only add it if it isn't there.
						return;
					}
				}
				serverAdapter.add(new ServerInfo(address, device.getName(),
						false));
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_list);

		serverAdapter = new ServerInfoAdapter(this, R.layout.server_list_item);

		// Read all old servers
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				MODE_PRIVATE);
		int serverCount = settings.getInt(SERVER_COUNT, 0);
		String address, name;
		for (int i = 0; i < serverCount; i++) {
			address = settings.getString(SERVER_ADDRESS + i, "");
			name = settings.getString(SERVER_NAME + i, "");
			if (address.length() > 0) { // Discard items with empty address
				serverAdapter.add(new ServerInfo(address, name, true));
			}
		}

		ListView lv = (ListView) findViewById(R.id.list);
		lv.setAdapter(serverAdapter);

		// Register listeners
		registerForContextMenu(lv);

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object item = parent.getItemAtPosition(position);
				Log.d(TAG, "Clicked: " + item.toString());
				if (item instanceof ServerInfo) {
					setResultAndFinish((ServerInfo) item);
				}
			}
		});

		// Using nfc but only for ignoring calls and get no popup about nfc
		// discovered.
		ConcreteHandleTagDiscover concreteHandler = new ConcreteHandleTagDiscover();
		HandleTagDiscoverWithBlock handleTagIDDiscoverWithBlock = new HandleTagDiscoverWithBlock(
				concreteHandler);

		readNfcTag = new ReadNfcTag(handleTagIDDiscoverWithBlock);
		readNfcTag.onCreate(this);
	}

	@Override
	protected void onPause() {
		readNfcTag.onPause();

		// Unregister the BroadcastReceiver
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (bluetoothAdapter != null) {
			bluetoothAdapter.cancelDiscovery();
		}
		unregisterReceiver(mReceiver);

		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		readNfcTag.onResume(getIntent());

		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter
				.getDefaultAdapter();
		if (bluetoothAdapter != null) {
			bluetoothAdapter.startDiscovery();
		}
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Store all old servers
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		int count = serverAdapter.getCount();
		int numberOfItems = 0;
		ServerInfo s;
		for (int i = 0; i < count; i++) {
			s = serverAdapter.getItem(i);
			if (s.doSave()) {
				editor.putString(SERVER_ADDRESS + numberOfItems, s.getAddress());
				editor.putString(SERVER_NAME + numberOfItems, s.getName());
				numberOfItems++;
			}
		}
		editor.putInt(SERVER_COUNT, numberOfItems);
		editor.commit();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.list) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			int pos = info.position;
			ServerInfo s = serverAdapter.getItem(pos);
			menu.setHeaderTitle(s.toString());
			menu.add(pos, ID_CONNECT, Menu.NONE, R.string.select_connect);
			menu.add(pos, ID_EDIT, Menu.NONE, R.string.select_edit);
			menu.add(pos, ID_DELETE, Menu.NONE, R.string.select_delete);
		} else {
			super.onCreateContextMenu(menu, v, menuInfo);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		Log.d(TAG, "Menu: " + item.getItemId() + " " + item.getGroupId());
		switch (item.getItemId()) {
		case ID_CONNECT:
			setResultAndFinish(serverAdapter.getItem(item.getGroupId()));
			return true;
		case ID_EDIT:
			editingItem = serverAdapter.getItem(item.getGroupId());
			showDialog(EDIT_DIALOG);
			return true;
		case ID_DELETE:
			serverAdapter.remove(serverAdapter.getItem(item.getGroupId()));
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case EDIT_DIALOG:
			EditText eName = (EditText) dialog
					.findViewById(R.id.editText_server_name);
			EditText eAddress = (EditText) dialog
					.findViewById(R.id.editText_server_address);

			eName.setText(editingItem.getName());
			eAddress.setText(editingItem.getAddress());
			return;
		}
		super.onPrepareDialog(id, dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case EDIT_DIALOG:
			View v = getLayoutInflater().inflate(R.layout.edit_server_dialog,
					null);
			final EditText eName = (EditText) v
					.findViewById(R.id.editText_server_name);
			final EditText eAddress = (EditText) v
					.findViewById(R.id.editText_server_address);

			eName.setText(editingItem.getName());
			eAddress.setText(editingItem.getAddress());

			return new AlertDialog.Builder(SelectServerActivity.this)
					.setView(v)
					.setTitle(R.string.edit_title)
					.setPositiveButton(R.string.edit_save,
							new OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									String newName = eName.getText().toString();
									String newAddress = eAddress.getText()
											.toString();
									if (newName.length() > 0) {
										editingItem.setName(newName);
									}
									if (newAddress.length() > 0) {
										editingItem.setAddress(newAddress);
									}
									editingItem.setSave(true); // Save it

									serverAdapter.notifyDataSetChanged();
								}
							}).setNegativeButton(R.string.edit_cancel, null)
					.create();
		default:
			return super.onCreateDialog(id);
		}
	}

	public void onServerConnectClick(View v) {
		// Just read the text and finish
		String text = ((EditText) findViewById(R.id.editText)).getText()
				.toString();
		Log.d(TAG, "Connect with: " + text);
		if (text.length() > 0) {
			ServerInfo serverInfo = new ServerInfo(text);
			serverAdapter.add(serverInfo);
			setResultAndFinish(serverInfo);
		}
	}

	/**
	 * Saves the server address in an intent, sets the results and finishes the
	 * activity.
	 * 
	 * @param server
	 */
	private void setResultAndFinish(ServerInfo server) {
		server.setSave(true); // Save it
		Intent data = new Intent();
		data.putExtra(SERVER_ADDRESS_INTENT, server.getAddress());
		setResult(RESULT_OK, data);
		finish();
	}

	private class ServerInfoAdapter extends ArrayAdapter<ServerInfo> {

		public ServerInfoAdapter(Context context, int textViewResourceId) {
			super(context, textViewResourceId);
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = getLayoutInflater().inflate(
						R.layout.server_list_item, null);
			}
			ServerInfo s = getItem(position);
			if (s != null) {
				TextView textViewName = (TextView) convertView
						.findViewById(R.id.textViewName);
				TextView textViewAddress = (TextView) convertView
						.findViewById(R.id.textViewAddress);
				if (textViewName != null) {
					textViewName.setText(s.getName());
				}
				if (textViewAddress != null) {
					textViewAddress.setText(s.getAddress());
				}
			}
			return convertView;
		}
	}

	/**
	 * A class to handle information about servers.
	 * 
	 */
	private class ServerInfo {
		private String address;
		private String name;
		private boolean save;

		public ServerInfo(String address) {
			this(address, address, true);
		}

		public ServerInfo(String address, String name, boolean save) {
			this.address = address;
			this.name = name;
			this.save = save;
		}

		public void setAddress(String address) {
			this.address = address;
		}

		public void setName(String name) {
			this.name = name;
		}

		public void setSave(boolean save) {
			this.save = save;
		}

		public String getAddress() {
			return address;
		}

		public String getName() {
			return name;
		}

		public boolean doSave() {
			return save;
		}

		@Override
		public String toString() {
			return name + " - " + address;
		}
	}
}
