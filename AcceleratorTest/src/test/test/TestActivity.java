package test.test;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.UUID;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class TestActivity extends Activity {
	private SensorManager mSensorManager;
	private AcceleratorListener aList;
	private BluetoothAdapter mBluetoothAdapter;
	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
	private BluetoothSocket mmSocket = null;
	private Socket s = null;
	private static final boolean USE_BLUETOOTH = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		TextView tx = (TextView) findViewById(R.id.x);
		TextView ty = (TextView) findViewById(R.id.y);
		TextView tz = (TextView) findViewById(R.id.z);

		aList = new AcceleratorListener(tx, ty, tz);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(aList,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);

		final Button button1 = (Button) findViewById(R.id.button1);
		button1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				aList.reset();
				aList.setStream(null);

				if (USE_BLUETOOTH) {
					mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
					BluetoothDevice device = mBluetoothAdapter
							.getRemoteDevice("00:1F:E1:EB:3B:DE");

					try {
						mmSocket = device
								.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
						mmSocket.connect();
						OutputStream stream = mmSocket.getOutputStream();
						stream.write(3);
						aList.setStream(stream);

					} catch (IOException e) {
						Log.e("NOOES", e.getLocalizedMessage());
					}
				} else {
					try {
						s = new Socket("130.240.5.55", 8633);
						aList.setStream(s.getOutputStream());
						Log.w("Test", "test");
					} catch (IOException e) {
						Log.e("Test", "Socket failed :(");
						e.printStackTrace();
					}
				}
			}
		});

		final Button button2 = (Button) findViewById(R.id.button2);
		button2.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				aList.reset();
				aList.sendStop();
				aList.setStream(null);

				if (USE_BLUETOOTH) {
					try {
						if (mmSocket != null) {
							mmSocket.close();
						}
					} catch (IOException e) {
						Log.e("NOOES", e.getLocalizedMessage());
					}
				} else {
					try {
						if (s != null) {
							s.close();
						}
					} catch (IOException e) {
						Log.e("NOOES", e.getLocalizedMessage());
					}
				}
			}
		});

	}

	@Override
	protected void onStop() {
		mSensorManager.unregisterListener(aList);
		super.onStop();
	}
}