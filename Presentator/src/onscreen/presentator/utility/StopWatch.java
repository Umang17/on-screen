package onscreen.presentator.utility;

import android.content.Context;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.widget.Chronometer;

public class StopWatch extends Chronometer {

	private Long currentTimeLastStop;
	private boolean isRunning = false;
	private boolean clockReseted = true;

	public enum WatchState {
		RUNNING, PAUSED, STOPPED
	};

	private WatchState state = WatchState.STOPPED;

	public StopWatch(Context context) {
		super(context);
	}
	
	public StopWatch(Context context, AttributeSet attr) {
		super(context, attr);
	}

	/**
	 * @return the displayed time in seconds
	 */
	public Long getStopWatchTime() {
		if (!isRunning) {
			return getRestoreTime() / 1000;
		} else {
			return (time() - this.getBase()) / 1000;
		}
	}

	/**
	 * 
	 * @param time
	 *            the stopwatch is displayed
	 */
	public void setBaseTime(int time) {
		Long time2 = time();
		this.setBase(time2 - time * 1000);
		clockReseted = false;
		currentTimeLastStop = time2;
	}

	/**
	 * Start the clock to tick
	 */
	public void startClock() {
		if (isRunning)
			return;

		if (clockReseted) {
			this.setBase(time());
		} else {
			long time = getRestoreTime();
			this.setBase(time);
		}

		this.start();
		isRunning = true;
		clockReseted = false;
		state = WatchState.RUNNING;
	}

	private long getRestoreTime() {
		return this.getBase() + time() - currentTimeLastStop;
	}

	public void pauseClock() {
		if (!isRunning) {
			return;
		}
		this.stop();
		setClockIsNotRunning();
		state = WatchState.PAUSED;
	}

	public void resetClock() {
		this.setBase(time());
		this.refreshDrawableState();
		this.stop();
		setClockIsNotRunning();
		clockReseted = true;
		state = WatchState.STOPPED;
	}

	private void setClockIsNotRunning(Long time) {
		currentTimeLastStop = time;
		isRunning = false;
	}

	private void setClockIsNotRunning() {
		setClockIsNotRunning(time());
	}

	private Long time() {
		return SystemClock.elapsedRealtime();
	}

	public WatchState getState() {
		return state;
	}
}
