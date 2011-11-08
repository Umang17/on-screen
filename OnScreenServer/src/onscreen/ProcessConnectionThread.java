package onscreen;

/**
 *
 * @author Mattias
 */
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.io.InputStream;

import javax.microedition.io.StreamConnection;
import javax.swing.JTextArea;

public class ProcessConnectionThread implements Runnable {

    private StreamConnection mConnection;
    private JTextArea textArea;
    // Constant that indicate command from devices
    private static final int EXIT_CMD = -1;
    private static final int KEY_RIGHT = 1;
    private static final int KEY_LEFT = 2;

    ProcessConnectionThread(StreamConnection connection, JTextArea textArea) {
        mConnection = connection;
        this.textArea = textArea;
    }

    @Override
    public void run() {
        try {
            // prepare to receive data
            InputStream inputStream = mConnection.openInputStream();

            textArea.append("waiting for input");

            while (true) {
                int command = inputStream.read();

                if (command == EXIT_CMD) {
                    textArea.append("finish process");
                    break;
                }
                processCommand(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Process the command from client
     * @param command the command code
     */
    private void processCommand(int command) {
        try {
            Robot robot = new Robot();
            switch (command) {
                case KEY_RIGHT:
                    //robot.keyPress(KeyEvent.VK_RIGHT);
                    textArea.append("Right");
                    break;
                case KEY_LEFT:
                    //robot.keyPress(KeyEvent.VK_LEFT);
                    textArea.append("Left");
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
