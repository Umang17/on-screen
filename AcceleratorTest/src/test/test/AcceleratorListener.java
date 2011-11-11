package test.test;

import java.io.IOException;
import java.io.OutputStream;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.TextView;

public class AcceleratorListener implements SensorEventListener {
	private int xAcc, yAcc, xPrev = Integer.MAX_VALUE,
			yPrev = Integer.MAX_VALUE;
	private long lastXUpdate = 0, lastYUpdate = 0;
	private static final int X = 0, Y = 1;
	private static final String TAG = "Accelerator";
	private static final long MIN_TIME_NANO = 1000000; // 1ms
	private OutputStream stream = null;
	public static final byte UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4;
	private TextView tx, ty, tz;

	public AcceleratorListener(TextView tx, TextView ty, TextView tz) {
		this.tx = tx;
		this.ty = ty;
		this.tz = tz;
	}

	public void setStream(OutputStream stream) {
		this.stream = stream;
	}

	public void reset() {
		lastXUpdate = 0;
		lastYUpdate = 0;
		xPrev = Integer.MAX_VALUE;
		yPrev = Integer.MAX_VALUE;
		Log.d(TAG, "reset");
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	private void sendDirection(byte dir, int value) {
		if (stream != null) {
			try {
				stream.write(2);
				stream.write(dir);
				if (value > Byte.MAX_VALUE) {
					stream.write(Byte.MAX_VALUE);
				} else {
					stream.write(value);
				}
				Log.d(TAG, "Sent dir: " + dir + " " + value);
				stream.flush();
			} catch (IOException e) {
				Log.e(TAG,
						"Failed to write to stream: " + e.getLocalizedMessage());
			}
		}
	}

	public void sendStop() {
		if (stream != null) {
			try {
				stream.write(2);
				stream.write(LEFT);
				stream.write(0);
				stream.write(2);
				stream.write(UP);
				stream.write(0);
				stream.write(4);
				stream.write(10);
				stream.flush();
				stream.close();
			} catch (IOException e) {
				Log.e(TAG,
						"Failed to write to stream: " + e.getLocalizedMessage());
			}
		}
	}

	public void onSensorChanged(SensorEvent event) {
		xAcc = Math.round(event.values[X]);
		yAcc = Math.round(event.values[Y]);

		if (event.timestamp >= lastXUpdate + MIN_TIME_NANO) {
			if (xAcc != xPrev) {
				if (xAcc < 0) {
					sendDirection(LEFT, -xAcc);
				} else {
					sendDirection(RIGHT, xAcc);
				}
				xPrev = xAcc;
				lastXUpdate = event.timestamp;
			}
		}

		if (event.timestamp >= lastYUpdate + MIN_TIME_NANO) {
			if (yAcc != yPrev) {
				if (yAcc < 0) {
					sendDirection(DOWN, -yAcc);
				} else {
					sendDirection(UP, yAcc);
				}
				yPrev = yAcc;
				lastYUpdate = event.timestamp;
			}
		}

		tx.setText(String.valueOf(xAcc));
		ty.setText(String.valueOf(yAcc));
		// tz.setText(String.valueOf(event.values[2]));
	}
}
