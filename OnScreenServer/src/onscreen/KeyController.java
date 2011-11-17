/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onscreen;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

/**
 *
 * @author Mattias
 */
public class KeyController {
    
    private static final int EXIT = 0;   
    private static final int NEXT = 2;
    private static final int PREVIOUS = 1;
    private static final int BLANK = 3;
    private Robot rob;
    
    public KeyController () {
        try {
            rob = new Robot();
        } catch (AWTException ex) {}
    }

    boolean recive(int read, FilePresented file) {
        switch(read) {
            case EXIT:
                rob.keyPress(KeyEvent.VK_ESCAPE);
                rob.keyPress(KeyEvent.VK_ESCAPE);
                return true;
            case NEXT:
                rob.keyPress(KeyEvent.VK_LEFT);
                file.nextSlide();
                break;
            case PREVIOUS:
                rob.keyPress(KeyEvent.VK_RIGHT);
                file.previousSlide();
                break;
            case BLANK:
                rob.keyPress(KeyEvent.VK_PERIOD);
                file.toggleBlank();
                break;
        }
        return false;
    }
    
}
