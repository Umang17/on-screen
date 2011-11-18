package onscreen.presentator;

import android.os.SystemClock;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Chronometer.OnChronometerTickListener;

class StopWatch{
	private Chronometer chrono;
	private Button btnStart;
	private Button btnPause;
	private boolean resume=false;
	private String currentTime="";	
	private Long currentTimeLastStop;
	private boolean clockSetByComputer=false;
	private boolean isRunning=false;
	
	public StopWatch(final Chronometer chrono, Button btnStart, Button btnPause, Button btnReset){
		this.chrono=chrono;
		this.btnStart=btnStart;
		this.btnPause=btnPause;
		
		btnPause.setEnabled(false);			
		
		chrono.setOnChronometerTickListener(new OnChronometerTickListener() {

			public void onChronometerTick(Chronometer arg0) {
				
					long seconds = (SystemClock.elapsedRealtime() - chrono.getBase()) / 1000;
					
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
		if (resume) {
			return (chrono.getBase() + SystemClock.elapsedRealtime() - currentTimeLastStop) / 1000;
		} else {
			return (SystemClock.elapsedRealtime() - chrono.getBase()) / 1000;
		}
	}
	
	/**
	 * 
	 * @param time the stopwatch is displayed
	 */
	public void setBaseTime(int time){
		chrono.setBase(SystemClock.elapsedRealtime()-time*1000);
		clockSetByComputer=true;
	}

	/**
	 * Start the clock to tick
	 */
	public void startClock(){
		btnPause.setEnabled(true);
		btnStart.setEnabled(false);
		if (!resume) {
			chrono.setBase(SystemClock.elapsedRealtime());
			chrono.start();
		} else {
			if (clockSetByComputer){
				clockSetByComputer=false;
			} else {
				long time=chrono.getBase()+SystemClock.elapsedRealtime()-currentTimeLastStop;
				chrono.setBase(time);
			}
			chrono.start();
		}
		isRunning=true;
	}
	
	public void pauseClock(){
		btnStart.setEnabled(true);
		btnPause.setEnabled(false);
		chrono.stop();
		resume = true;
		btnStart.setText("Resume");
		currentTimeLastStop=SystemClock.elapsedRealtime();
		isRunning=false;
	}
	
	public void resetClock(){
		chrono.stop();
		chrono.setText("0:00:00");
		btnStart.setText("Start");
		resume = false;
		currentTimeLastStop=SystemClock.elapsedRealtime();
		btnStart.setEnabled(true);				
		btnPause.setEnabled(false);
		isRunning=false;
	}
	
	public boolean isRunningNow(){
		return isRunning;
	}
}
