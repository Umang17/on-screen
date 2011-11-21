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
		
		chrono.setOnChronometerTickListener(new OnChronometerTickListener() {

			public void onChronometerTick(Chronometer arg0) {
				
					long seconds = (time() - chrono.getBase()) / 1000;
					
					int hour = (int) (seconds/3600);
					if(hour>=10) {
						chrono.setBase(chrono.getBase() - seconds*3600*1000);
						seconds -= hour*3600;
						hour = 0;
					}
					seconds -= hour*3600;
					int minutes = (int) (seconds/60);
					seconds -= minutes*60;
					
					currentTime = hour+":"
									+(minutes<10?"0"+minutes:minutes)+":"
									+(seconds<10?"0"+seconds:seconds);
					arg0.setText(currentTime);
			}
		});
		chrono.setText("0:00:00");			
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
