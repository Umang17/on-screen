package onscreen;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MouseControllThread extends Thread {
	private Robot r;
	private int xSpeed = 0, ySpeed = 0;
	private static final MouseControllThread instance = new MouseControllThread();
	private long lastX = Long.MIN_VALUE, lastY = Long.MIN_VALUE;

	private MouseControllThread() {
		try {
			r = new Robot();
			// r.setAutoDelay(5);
			start();
		} catch (AWTException ex) {
			Logger.getLogger(MouseController.class.getName()).log(Level.SEVERE,
					null, ex);
		}
	}

	public static MouseControllThread getInstance() {
		return instance;
	}

	public synchronized void setX(int x) {
		xSpeed = x;
	}

	public synchronized void setY(int y) {
		ySpeed = y;
	}

	public synchronized void stopMouse() {
		xSpeed = 0;
		ySpeed = 0;
	}

	public synchronized void center() {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		int screenX = (int) toolkit.getScreenSize().getWidth();
		int screenY = (int) toolkit.getScreenSize().getHeight();
		int x = screenX / 2;
		int y = screenY / 2;
		r.mouseMove(x, y);
	}

	private synchronized int getXSpeed() {
		return xSpeed;
	}

	private synchronized int getYSpeed() {
		return ySpeed;
	}

	private synchronized int getXPos() {
		return MouseInfo.getPointerInfo().getLocation().x;
	}

	private synchronized int getYPos() {
		return MouseInfo.getPointerInfo().getLocation().y;
	}

	// One ns is 1000000 ms
	private static final int MS_TO_NS = 1000000;
	private static final int INVERT = MS_TO_NS * (10);

	@Override
	public void run() {
		int x, y, xSpeed, ySpeed;
		int xChange, yChange;
		long currentTime;
		while (true) {
			xSpeed = getXSpeed();
			ySpeed = getYSpeed();

			currentTime = System.nanoTime();

			xChange = 0;
			if (xSpeed != 0) {
				if (currentTime >= lastX + INVERT - MS_TO_NS * Math.abs(xSpeed)) {
					xChange = (int) Math.signum(xSpeed);
					lastX = currentTime;
				}
			}

			yChange = 0;
			if (ySpeed != 0) {
				if (currentTime >= lastY + INVERT - MS_TO_NS * Math.abs(ySpeed)) {
					yChange = (int) Math.signum(ySpeed);
					lastY = currentTime;
				}
			}

			if (xChange != 0 || yChange != 0) {
				x = getXPos() + xChange;
				y = getYPos() + yChange;
				r.mouseMove(x, y);
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
