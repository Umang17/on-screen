package onscreen.presentator.utility;

import android.os.SystemClock;
import android.widget.Chronometer;

public class StopWatch{
	private Chronometer chrono;
	private Long currentTimeLastStop;
	private boolean isRunning=false;
	private boolean clockReseted=true;
	public enum WatchState {RUNNING, PAUSED, STOPPED};
	private WatchState state = WatchState.STOPPED;
	
	public StopWatch(final Chronometer chrono){
		this.chrono=chrono;
		resetClock();
	}
	
	/**
	 * @return the displayed time in seconds
	 */
	public Long getStopWatchTime() {
		if (!isRunning) {
			return getRestoreTime() / 1000;
		} else {
			return (time() - chrono.getBase()) / 1000;
		}
	}
	
	/**
	 * 
	 * @param time the stopwatch is displayed
	 */
	public void setBaseTime(int time){
		Long time2=time();
		chrono.setBase(time2-time*1000);
		clockReseted=false;
		currentTimeLastStop=time2;
	}

	/**
	 * Start the clock to tick
	 */
	public void startClock(){
		if (isRunning)
			return;
		
		if(clockReseted){
			chrono.setBase(time());
		} else {
			long time=getRestoreTime();
			chrono.setBase(time);
		}
				
		chrono.start();
		isRunning=true;
		clockReseted=false;
		state = WatchState.RUNNING;
	}
	
	private long getRestoreTime() {
		return chrono.getBase()+time()-currentTimeLastStop;
	}

	public void pauseClock(){
		if (!isRunning){
			return;
		}
		chrono.stop();
		setClockIsNotRunning();
		state = WatchState.PAUSED;
	}
	
	public void resetClock(){
		chrono.setBase(time());
		chrono.refreshDrawableState();
		chrono.stop();
		setClockIsNotRunning();
		clockReseted=true;
		state = WatchState.STOPPED;
	}
	
	private void setClockIsNotRunning(Long time) {
		currentTimeLastStop=time;
		isRunning=false;
	}
	
	private void setClockIsNotRunning(){
		setClockIsNotRunning(time());
	}

	private Long time() {
		return SystemClock.elapsedRealtime();
	}

	public WatchState getState() {
		return state;
	}
}
