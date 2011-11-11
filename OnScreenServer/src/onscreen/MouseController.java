package onscreen;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author Mattias
 */
public class MouseController {

	private final static byte UP = 1;
	private final static byte DOWN = 2;
	private final static byte RIGHT = 3;
	private final static byte LEFT = 4;
	private final static byte CENTER = 5;
	private MouseControllThread mouseThread;

	public MouseController() {
		mouseThread = MouseControllThread.getInstance();
		mouseThread.start();
	}

	public void recive(InputStream stream) {
		int type = 0, value = 0;
		try {
			type = stream.read();
			value = stream.read();
		} catch (IOException ex) {
			Notification.notify("Some error while reciving.");
		}

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
			mouseThread.center();
			break;
		default:
			Notification.notify("unknown control byte");
			break;
		}
	}
}
