package onscreen.presentator;

import android.os.SystemClock;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;

class StopWatch{
	private Chronometer chrono;
	private String currentTime="";	
	private Long currentTimeLastStop;
	private boolean isRunning=false;
	private boolean clockReseted=true;
	
	public StopWatch(final Chronometer chrono){
		this.chrono=chrono;		
		chrono.setFormat("H:MM:SS");
		chrono.setOnChronometerTickListener(new OnChronometerTickListener() {

			public void onChronometerTick(Chronometer arg0) {
				chrono.refreshDrawableState();
			}
		});		
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
		if(!isRunning){
			setClockIsNotRunning(time2);
		}
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
	}
	
	public void resetClock(){
		chrono.stop();
		chrono.setText("0:00:00");
		setClockIsNotRunning();
		clockReseted=true;
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

	public boolean isRunningNow(){
		return isRunning;
	}
}
