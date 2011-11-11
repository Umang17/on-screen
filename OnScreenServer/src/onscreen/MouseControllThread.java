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

	private MouseControllThread() {
		try {
			r = new Robot();
			r.setAutoDelay(5);
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

	private synchronized int getX() {
		return MouseInfo.getPointerInfo().getLocation().x + xSpeed;
	}

	private synchronized int getY() {
		return MouseInfo.getPointerInfo().getLocation().y + ySpeed;
	}

	@Override
	public void run() {
		while (true) {
			int x, y;
			synchronized (instance) {
				x = getX();
				y = getY();
				r.mouseMove(x, y);
			}
		}
	}
}
