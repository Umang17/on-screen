package test.test;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TestActivity extends Activity {
	private SensorManager mSensorManager;
	private OrientationSensorListener oListner;

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