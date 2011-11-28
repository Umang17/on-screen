package onscreen.presentator;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class SelectServerActivity extends Activity {
	private static final String TAG = "SelectServer";
	public static final String SERVER_ADDRESS_INTENT = "ServerAddress";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.server_list);

		ListView lv = (ListView) findViewById(R.id.list);

		ArrayList<ServerInfo> values = new ArrayList<ServerInfo>();
		values.add(new ServerInfo("192.168.0.1"));

		lv.setAdapter(new ArrayAdapter<ServerInfo>(this,
				R.layout.server_list_item, values));

		lv.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Object item = parent.getItemAtPosition(position);
				Log.d(TAG, item.toString());
				if (item instanceof ServerInfo) {
					Intent data = new Intent();
					data.putExtra(SERVER_ADDRESS_INTENT,
							((ServerInfo) item).getAddress());
					setResult(RESULT_OK, data);
					finish();
				}
			}
		});
	}

	private class ServerInfo {
		private String address;

		public ServerInfo(String address) {
			this.address = address;
		}

		public String getAddress() {
			return address;
		}

		@Override
		public String toString() {
			return address;
		}
	}
}
