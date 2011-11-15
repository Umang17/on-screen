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
	private static final byte UP = 1, DOWN = 2, LEFT = 3, RIGHT = 4,
			CENTER = 5, LEFT_CLICK = 6, RIGHT_CLICK = 7;
	private static final byte MOUSE_UP = 1, MOUSE_DOWN = 2;
	private static final int MOUSECONTROLLER = 2, RELEASECONTROL = 4,
			EXIT_CMD = 10;
	private TextView tx, ty, tz;
	private float sensitivity = 0.9f;
	private float speed = 1.5f;
	private static final int MAX_SPEED = 100;

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
				stream.write(MOUSECONTROLLER);
				stream.write(dir);
				if (value != 0) {
					value = (int) (value * speed - speed);
				}
				if (value < speed * 10 * sensitivity) {
					stream.write(0);
				} else if (value >= MAX_SPEED) {
					stream.write(MAX_SPEED);
				} else {
					stream.write(value);
				}
				// Log.d(TAG, "Sent dir: " + dir + " " + value);
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
				sendDirection(LEFT, 0);

				sendDirection(UP, 0);

				stream.write(RELEASECONTROL);

				stream.write(EXIT_CMD);

				stream.flush();
				stream.close();
			} catch (IOException e) {
				Log.e(TAG,
						"Failed to write to stream: " + e.getLocalizedMessage());
			}
		}
	}

	public void onSensorChanged(SensorEvent event) {
		// Multiply by 10 to keep one decimal when transferring
		xAcc = Math.round(sensitivity * event.values[X] * 10);
		yAcc = Math.round(sensitivity * event.values[Y] * 10);

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
	}

	public void setSpeed(int progress) {
		if (progress == 0) {
			progress = 1;
		}
		speed = progress / 10f;
		Log.d(TAG, "Speed: " + speed);
	}

	public void setSensitivity(int progress) {
		if (progress == 0) {
			progress = 1;
		}
		sensitivity = progress / 100f;
		Log.d(TAG, "Sense: " + sensitivity);
	}

	private void sendClick(byte click, boolean up) {
		if (stream != null) {
			try {
				stream.write(MOUSECONTROLLER);
				stream.write(click);
				stream.write(up ? MOUSE_UP : MOUSE_DOWN); // Up or down
				stream.flush();
			} catch (IOException e) {
				Log.e(TAG,
						"Failed to write to stream: " + e.getLocalizedMessage());
			}
		}
	}

	public void sendLeftClick(boolean up) {
		sendClick(LEFT_CLICK, up);
		Log.d(TAG, "Left click: " + up);
	}

	public void sendRightClick(boolean up) {
		sendClick(RIGHT_CLICK, up);
		Log.d(TAG, "Right click: " + up);
	}
}
