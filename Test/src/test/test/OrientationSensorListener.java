package test.test;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.util.Log;

public class OrientationSensorListener implements SensorEventListener {
	private float startValues[] = null, prevValues[] = null;
	private float rotation, pitch;
	private static final int ROTATION = 0, PITCH = 1;
	private static final int SENSITIVITY = 5;
	private static final String TAG = "Orientation",
			START_VALUES = "StartValues", PREV_VALUES = "PrevValues";

	public OrientationSensorListener() {
	}

	public void save(Bundle b) {
		b.putFloatArray(START_VALUES, startValues);
		b.putFloatArray(PREV_VALUES, prevValues);
	}

	public void load(Bundle b) {
		if (b != null) {
			startValues = b.getFloatArray(START_VALUES);
			prevValues = b.getFloatArray(PREV_VALUES);
		}
	}

	public void reset() {
		startValues = null;
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	public void onSensorChanged(SensorEvent event) {
		if (startValues == null) {
			startValues = event.values.clone();
			prevValues = null;
		}

		// Make the rotation angle go from -180 to +180 with 0 at the starting
		// direction.
		rotation = startValues[ROTATION] - event.values[ROTATION];
		if (rotation > 180) {
			rotation = 360 - rotation;
		} else if (rotation < -180) {
			rotation = 360 + rotation;
		}
		pitch = event.values[PITCH];

		if (prevValues == null) {
			// First time should be centered so no need to calculate any
			// changes.
			// TODO Send center
			Log.d(TAG, "Centering.");
			prevValues = new float[2];
		} else {
			float rotationDiff = prevValues[ROTATION] - rotation;
			float pitchDiff = prevValues[PITCH] - pitch;

			if (Math.abs(rotationDiff) > SENSITIVITY) {
				prevValues[ROTATION] = rotation;
				Log.d(TAG, "Rotation changed.");
				// TODO Send stuff
			}
			if (Math.abs(pitchDiff) > SENSITIVITY) {
				prevValues[PITCH] = pitch;
				Log.d(TAG, "Pitch changed.");
				// TODO Send stuff
			}
		}
	}

}
