package onscreen.systemcontrol;

import onscreen.communication.ConnectedThread;
import java.util.Observable;
import onscreen.Notification;

/**
 * Remembers and controls the timer of the presentation.
 *
 * @author Mattias
 */
public class PresentationTimer extends Observable {

    private static final int RESET = 9;
    private static final int PAUSE = 8;
    private static final int START = 7;
    private static final int SEND_PAUSE = 0;
    private static final int SEND_RUNNING = 1;
    private int startTime;
    private int collectedTime;
    private boolean pause = false;

    /**
     * Creates a new presentation timer 
     * 
     * @param caller the caller is not to be notified of this event again!
     */
    public PresentationTimer(ConnectedThread caller) {
        start(caller);
    }

    /**
     * Appends the previous ran time to the current collected time.
     * 
     * @param ranTime the time to add to the timer 
     */
    public synchronized void setRanTime(int ranTime) {
        collectedTime += ranTime;
    }

    /**
     * Start the timer, is safe to call multiple times.
     * 
     * @param caller the caller is not to be notified of this event again!
     */
    public synchronized void start(ConnectedThread caller) {
        if (pause) {
            pause = false;
            startTime = getCurrentTime();
            setChanged();
            notifyObservers(new NotifyThread(0, getRunning(), caller));
        }
    }

    /**
     * Pauses the timer, is safe to call multiple times.
     * 
     * @param caller the caller is not to be notified of this event again!
     */
    public synchronized void pause(ConnectedThread caller) {
        if (!pause) {
            collectedTime += getCurrentTime() - startTime;
            pause = true;
            setChanged();
            notifyObservers(new NotifyThread(0, getRunning(), caller));
        }
    }

    /**
     * Resets the timer, is NOT safe to call multiple times.
     * 
     * @param caller the caller is not to be notified of this event again!
     */
    public synchronized void reset(ConnectedThread caller) {
        collectedTime = 0;
        pause = true;
        setChanged();
        notifyObservers(new NotifyThread(1, getRunning(), caller));

    }

    /**
     * Get the current time ran for the timer.
     *  
     * @return the current time as byte array
     */
    public synchronized byte[] getTime() {
        if (pause) {
            Notification.debugMessage("Sending time(pause): " + (collectedTime));
            return intToByte(collectedTime);
        } else {
            Notification.debugMessage("Sending time: " + (getCurrentTime() - startTime + collectedTime));
            return intToByte(getCurrentTime() - startTime + collectedTime);
        }
    }

    /**
     * Get the current time for the system.
     * 
     * @return the current time for the system in seconds
     */
    private int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    /**
     * Handels incoming control messages.
     *  
     * @param read the message to handle
     * @param caller the caller is not to be notified of this event again! 
     */
    public void control(int read, ConnectedThread caller) {
        if (read == RESET) {
            reset(caller);
        } else if (read == PAUSE) {
            pause(caller);
        } else { //START
            start(caller);
        }
    }

    /**
     * Get whether the timer is running or not.
     * 
     * @return 1 if running 0 if not
     */
    public byte getRunning() {
        if (pause) {
            return SEND_PAUSE;
        } else {
            return SEND_RUNNING;
        }
    }

    /**
     * Transforms a integer to a byte array of length 4.
     * 
     * @param input the integer
     * @return the byte array representing the integer
     */
    private byte[] intToByte(int input) {
        byte[] writeBuffer = new byte[4];
        writeBuffer[0] = (byte) (input >> 24);
        writeBuffer[1] = (byte) (input >> 16);
        writeBuffer[2] = (byte) (input >> 8);
        writeBuffer[3] = (byte) (input);

        return writeBuffer;
    }
}
