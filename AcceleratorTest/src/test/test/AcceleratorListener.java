package test.test;

import java.io.IOException;
import java.io.OutputStream;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;

public class AcceleratorListener implements SensorEventListener {
	private int xAcc, zAcc, xPrev = Integer.MAX_VALUE,
			zPrev = Integer.MAX_VALUE;
	private float xGrav, zGrav;
	private long lastX = 0, lastZ = 0;
	private static final int X = 0, Z = 2;
	private static final int SENSITIVITY = 5;
	private static final String TAG = "Accelerator";
	private static final long MIN_TIME_NANO =1 /*1000000*/; // 1ms
	private static final float alpha = 0.8f;
	private OutputStream stream = null;
	public static final byte UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4;

	public void setStream(OutputStream stream) {
		this.stream = stream;
	}

	public void reset() {
		lastX = 0;
		lastZ = 0;
		xPrev = Integer.MAX_VALUE;
		zPrev = Integer.MAX_VALUE;
		Log.d(TAG, "reset");
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	private void sendDirection(byte dir, int value) {
		if (stream != null) {
			try {
				value = Math.abs(value);
				stream.write(2);
				stream.write(dir);
				if (value > Byte.MAX_VALUE) {
					stream.write(Byte.MAX_VALUE);
				} else {
					stream.write(value);
				}
				stream.flush();
			} catch (IOException e) {
				Log.e(TAG,
						"Failed to write to stream: " + e.getLocalizedMessage());
			}
		}
	}

	public void onSensorChanged(SensorEvent event) {
		if (event.timestamp >= lastX + MIN_TIME_NANO) {
			xGrav = alpha * xGrav + (1 - alpha) * event.values[X];
			xAcc = Math.round(10 * (event.values[X] - xGrav));

			if (Math.abs(xAcc) < SENSITIVITY) {
				xAcc = 0;
			}
			if (xAcc != xPrev) {
				Log.d(TAG, "x axis changed: " + xAcc);
				if (xAcc >= 0) {
					sendDirection(LEFT, xAcc);
				} else {
					sendDirection(RIGHT, xAcc);
				}
				xPrev = xAcc;
				lastX = event.timestamp;
			}
		}

		if (event.timestamp >= lastZ + MIN_TIME_NANO) {
			zGrav = alpha * zGrav + (1 - alpha) * event.values[Z];
			zAcc = Math.round(10 * (event.values[Z] - zGrav));

			if (Math.abs(zAcc) < SENSITIVITY) {
				zAcc = 0;
			}
			if (zAcc != zPrev) {
				Log.d(TAG, "z axis changed: " + zAcc);
				if (zAcc >= 0) {
					sendDirection(UP, zAcc);
				} else {
					sendDirection(DOWN, zAcc);
				}
				zPrev = zAcc;
				lastZ = event.timestamp;
			}
		}
	}
}
