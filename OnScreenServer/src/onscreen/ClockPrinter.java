package onscreen;

import java.util.Timer;
import java.util.TimerTask;

public class ClockPrinter {
	
	private PresentationTimer pr;
	private Timer mytimer;
	private int delay=1000;
	
	public class DisplayTime extends TimerTask{

		@Override
		public void run() {
			System.out.println("PresentationTimer say that time is "+pr.getTime());
			schedule();
		}
		
	}
	
	public ClockPrinter (PresentationTimer pr){
		this.pr=pr;
		mytimer=new Timer();
		schedule();
	}

	private void schedule() {
		mytimer.schedule(new DisplayTime(), delay);		
	}

}
