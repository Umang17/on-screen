package onscreen.presentator;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

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

	private ArrayList<ServerInfo> servers = new ArrayList<ServerInfo>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_list);

		// Read all old servers
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				MODE_PRIVATE);
		int serverCount = settings.getInt(SERVER_COUNT, 0);
		String address, name;
		for (int i = 0; i < serverCount; i++) {
			address = settings.getString(SERVER_ADDRESS + i, "");
			name = settings.getString(SERVER_NAME + i, "");
			if (address.length() > 0) {
				servers.add(new ServerInfo(address, name));
			}
		}

		// TODO Remove debug code
		if (servers.size() == 0) {
			servers.add(new ServerInfo("130.240.93.115"));
			servers.add(new ServerInfo("00:1F:E1:EB:3B:DE"));
		}

		ListView lv = (ListView) findViewById(R.id.list);
		lv.setAdapter(new ArrayAdapter<ServerInfo>(this,
				R.layout.server_list_item, servers));

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
	}

	@Override
	protected void onStop() {
		super.onStop();

		// Store all old servers
		SharedPreferences settings = getSharedPreferences(PREFS_NAME,
				MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		int size = servers.size();
		editor.putInt(SERVER_COUNT, size);
		ServerInfo s;
		for (int i = 0; i < size; i++) {
			s = servers.get(i);
			editor.putString(SERVER_ADDRESS + i, s.getAddress());
			editor.putString(SERVER_NAME + i, s.getName());
		}
		editor.commit();
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		if (v.getId() == R.id.list) {
			AdapterContextMenuInfo info = (AdapterContextMenuInfo) menuInfo;
			int pos = info.position;
			ServerInfo s = servers.get(pos);
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
			// TODO
			return true;
		case ID_EDIT:
			// TODO
			return true;
		case ID_DELETE:
			// TODO
			return true;
		default:
			return super.onContextItemSelected(item);
		}
	}

	public void onServerConnectClick(View v) {
		// Just read the text and finish
		String text = ((EditText) findViewById(R.id.editText)).getText()
				.toString();
		Log.d(TAG, "Connect with: " + text);
		if (text.length() > 0) {
			ServerInfo serverInfo = new ServerInfo(text);
			servers.add(serverInfo);
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
		Intent data = new Intent();
		data.putExtra(SERVER_ADDRESS_INTENT, server.getAddress());
		setResult(RESULT_OK, data);
		finish();
	}

	/**
	 * A small class to handle information about servers.
	 * 
	 */
	private class ServerInfo {
		private String address;
		private String name;

		public ServerInfo(String address) {
			this(address, address);
		}

		public ServerInfo(String address, String name) {
			this.address = address;
			this.name = name;
		}

		public String getAddress() {
			return address;
		}

		public String getName() {
			return name;
		}

		@Override
		public String toString() {
			return name;
		}
	}
}
