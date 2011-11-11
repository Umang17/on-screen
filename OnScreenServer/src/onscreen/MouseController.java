package onscreen;

import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Robot;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Mattias
 */
public class MouseController {

	private Robot robot = null;
	private int screenX;
	private int screenY;
	private final static byte UP = 1;
	private final static byte DOWN = 2;
	private final static byte RIGHT = 3;
	private final static byte LEFT = 4;
	private final static byte CENTER = 5;
	private MouseThread mouseThread;

	public MouseController() {
		try {
			robot = new Robot();
		} catch (AWTException ex) {
			Logger.getLogger(MouseController.class.getName()).log(Level.SEVERE,
					null, ex);
		}
		robot.setAutoDelay(1);
		mouseThread = new MouseThread(robot);
		mouseThread.start();
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		screenX = (int) toolkit.getScreenSize().getWidth();
		screenY = (int) toolkit.getScreenSize().getHeight();
	}

	public void recive(InputStream stream) {
		int type = 0, value = 0;
		try {
			type = stream.read();
			value = stream.read();
		} catch (IOException ex) {
			Notification.notify("Some error while reciving.");
		}
		int x = MouseInfo.getPointerInfo().getLocation().x;
		int y = MouseInfo.getPointerInfo().getLocation().y;

		switch (type) {
		case UP:
			mouseThread.setY(-value);
			break;
		case DOWN:
			mouseThread.setY(value);
			break;
		case RIGHT:
			mouseThread.setX(value);
			break;
		case LEFT:
			mouseThread.setX(-value);
			break;
		case CENTER:
			x = screenX / 2;
			y = screenY / 2;
			robot.mouseMove(x, y);
			break;
		default:
			Notification.notify("unknown control byte");
			break;
		}
	}

	private class MouseThread extends Thread {
		private Robot r;
		private int xSpeed = 0, ySpeed = 0;

		public MouseThread(Robot r) {
			this.r = r;
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
				synchronized (MouseThread.this) {
					x = getX();
					y = getY();
					r.mouseMove(x, y);
				}
			}
		}
	}
}
