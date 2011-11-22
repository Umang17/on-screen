package onscreen;

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
	private final static byte LEFT_CLICK = 6;
	private final static byte RIGHT_CLICK = 7;
	private MouseControllThread mouseThread;

	public MouseController() {
		mouseThread = MouseControllThread.getInstance();
	}

	public void recive(int type, float value) {
		value = value / 10;

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
		case LEFT_CLICK:
			mouseThread.leftClick((int) (value * 10));
			break;
		case RIGHT_CLICK:
			mouseThread.rightClick((int) (value * 10));
			break;
		default:
			Notification.debugMessage("unknown control byte");
			break;
		}
	}
}
