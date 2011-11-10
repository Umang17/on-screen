/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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

    public MouseController() {
        try {
            robot = new Robot();
        } catch (AWTException ex) {
            Logger.getLogger(MouseController.class.getName()).log(Level.SEVERE, null, ex);
        }
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        screenX = (int) toolkit.getScreenSize().getWidth();
        screenY = (int) toolkit.getScreenSize().getHeight();
    }

    public void recive(InputStream stream, Notification noti) {
        byte[] recivedBytes = new byte[2];
        try {
            int result = stream.read(recivedBytes, 0, 2);
        } catch (IOException ex) {
            noti.notify("Some error while reciving.");
        }
        int x = MouseInfo.getPointerInfo().getLocation().x;
        int y = MouseInfo.getPointerInfo().getLocation().y;

        byte type = recivedBytes[0];
        int value = (int) recivedBytes[1];
        if (value == 0) {
            try {
                value = stream.read();
            } catch (IOException ex) {
                Logger.getLogger(MouseController.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        switch (type) {
            case UP:
                y += (int) value * screenY / 40;
                break;
            case DOWN:
                y -= (int) value * screenY / 40;
                break;
            case RIGHT:
                x += (int) value * screenX / 40;
                break;
            case LEFT:
                x -= (int) value * screenX / 40;
                break;
            case CENTER:
                x = screenX / 2;
                y = screenY / 2;
                break;
            default:
                noti.notify("unknown control byte");
                break;
        }
        robot.mouseMove(x, y);
    }
}
