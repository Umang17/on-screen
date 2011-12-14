package onscreen.presentator.nfc;

import java.util.Timer;
import java.util.TimerTask;

/**
 * As a extension for when a tag is found it will ignore
 * calls within blockedTime. 
 * 
 * If not blocked continue with the other HandleTagDiscover.
 * 
 * @author Viktor Lindgren
 *
 */
public class HandleTagDiscoverWithBlock implements HandleTagDiscover {

	private Timer timer;
	private boolean blocked = false;
	private long blockedTime = 1000;
	private HandleTagDiscover cHTIDD;

	/**
	 * 
	 * @param cHTIDD what to continue handling with if not blocked.
	 */
	public HandleTagDiscoverWithBlock(HandleTagDiscover cHTIDD) {
		this.cHTIDD = cHTIDD;
		timer = new Timer();
	}
	
	/**
	 * 
	 * @param cHTIDD what to continue handling with if not blocked.
	 * @param blockedTime in milliseconds
	 */
	public HandleTagDiscoverWithBlock(HandleTagDiscover cHTIDD, Long blockedTime) {
		this(cHTIDD);
		this.blockedTime=blockedTime;				
	}

	/**
	 * Sets the it to nonblocking after the specified time.
	 * @author viktor
	 *
	 */
	private class resetBlockTimerTask extends TimerTask {
		@Override
		public void run() {
			blocked = false;
		}
	}

	/**
	 * @param text that was discovered that will be sent forward if not blocked.
	 */
	public void handleTagDiscover(String text) {
		if (blocked) { // Ignorde multiple tagsscan within blockedTime.
			return;
		}
		blocked = true;
		timer.schedule(new resetBlockTimerTask(), blockedTime);

		cHTIDD.handleTagDiscover(text);
	}

}
