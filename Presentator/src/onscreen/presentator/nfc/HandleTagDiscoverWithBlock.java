package onscreen.presentator.nfc;

import java.util.Timer;
import java.util.TimerTask;

public class HandleTagDiscoverWithBlock implements HandleTagDiscover {

	private Timer timer;
	private boolean blocked = false;
	private final long blockedTime = 1000;
	private HandleTagDiscover cHTIDD;

	public HandleTagDiscoverWithBlock(HandleTagDiscover cHTIDD) {
		this.cHTIDD = cHTIDD;
		timer = new Timer();
	}

	private class resetBlockTimerTask extends TimerTask {
		@Override
		public void run() {
			blocked = false;
		}
	}

	public void handleTagDiscover(String tagID) {
		if (blocked) { // Ignorde multiple tagsscan within blockedTime.
			return;
		}
		blocked = true;
		timer.schedule(new resetBlockTimerTask(), blockedTime);

		cHTIDD.handleTagDiscover(tagID);
	}

}
