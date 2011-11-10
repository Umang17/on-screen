package test.test;

import java.io.IOException;
import java.net.Socket;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class TestActivity extends Activity {
	private SensorManager mSensorManager;
	private AcceleratorListener aList;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.main);

		aList = new AcceleratorListener();

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mSensorManager.registerListener(aList,
				mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
				SensorManager.SENSOR_DELAY_GAME);

		final Button button = (Button) findViewById(R.id.button1);
		button.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				aList.reset();
				try {
					Socket s = new Socket("192.168.1.1", 8633);
					aList.setStream(s.getOutputStream());
					Log.w("Test", "test");
				} catch (IOException e) {
					Log.e("Test", "Socket failed :(");
					e.printStackTrace();
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