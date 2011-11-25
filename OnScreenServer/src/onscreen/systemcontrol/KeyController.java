/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onscreen.systemcontrol;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

/**
 * Controls the system by sending key events to the system, used to control a 
 * presentation.
 * 
 * @author Mattias
 */
public class KeyController {

    private static final int EXIT = 0;
    private static final int NEXT = 2;
    private static final int PREVIOUS = 1;
    private static final int BLANK = 3;
    private Robot rob;

    /**
     * Attempts to create a new controller robot.
     */
    public KeyController() {
        try {
            rob = new Robot();
        } catch (AWTException ex) {
        }
    }

    /**
     * Recives a int with a control code and performs the corresponding action on 
     * the system.
     * 
     * @param read the control int
     * @return true if the system should exit presentation mode, false otherwise.
     */
    public boolean recive(int read) {
        switch (read) {
            case EXIT:
                exit();
                return true;
            case NEXT:
                rob.keyPress(KeyEvent.VK_LEFT);
                break;
            case PREVIOUS:
                rob.keyPress(KeyEvent.VK_RIGHT);
                break;
            case BLANK:
                rob.keyPress(KeyEvent.VK_PERIOD);
                break;
        }
        return false;
    }

    /**
     * Sends a exist signal to the system.
     */
    public void exit() {
        rob.keyPress(KeyEvent.VK_ESCAPE);
        try {
            Thread.sleep(50);
        } catch (InterruptedException ex) {
        }
        rob.keyPress(KeyEvent.VK_ESCAPE);
    }
}
