package test.test;

import java.io.IOException;
import java.io.OutputStream;
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

public class TestActivity extends Activity {
	private SensorManager mSensorManager;
	private OrientationSensorListener oListner;
	private BluetoothAdapter mBluetoothAdapter;
	private static final UUID MY_UUID_INSECURE = UUID
			.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
	private BluetoothSocket mmSocket;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		oListner = new OrientationSensorListener();
		oListner.load(savedInstanceState);

		final Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				oListner.reset();
			}
		});

	}

	@Override
	protected void onStart() {
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		BluetoothDevice device = mBluetoothAdapter
				.getRemoteDevice("00:1F:E1:EB:3B:DE");

		try {
			mmSocket = device
					.createInsecureRfcommSocketToServiceRecord(MY_UUID_INSECURE);
			mmSocket.connect();
			OutputStream stream = mmSocket.getOutputStream();
			oListner.setStream(stream);

		} catch (IOException e) {
			Log.e("NOOES", e.getLocalizedMessage());
		}
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(oListner,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
				SensorManager.SENSOR_DELAY_GAME);

		super.onStart();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		oListner.save(outState);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onStop() {
		mSensorManager.unregisterListener(oListner);
		super.onStop();
	}
}