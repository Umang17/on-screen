package onscreen.presentator;

import java.util.Timer;
import java.util.TimerTask;

public class HandleTagIDDiscoverWithBlock implements HandleTagIDDiscover {
	
	private Timer timer;
	private boolean blocked=false;
	private final long blockedTime=1000;
	private HandleTagIDDiscover cHTIDD;

	public HandleTagIDDiscoverWithBlock(HandleTagIDDiscover cHTIDD){
		this.cHTIDD=cHTIDD;
		timer=new Timer();
	}
	
	private class resetBlockTimerTask extends TimerTask{
		@Override
		public void run() {
			blocked=false;
		}
	}

	public void handleTagIDDiscover(String bluetoothAdress) {		
		if(blocked){ //Ignorde multiple tagsscan within blockedTime.
			return;
		}
		blocked=true;		
		timer.schedule(new resetBlockTimerTask(), blockedTime);
		
		cHTIDD.handleTagIDDiscover(bluetoothAdress);
	}

}
