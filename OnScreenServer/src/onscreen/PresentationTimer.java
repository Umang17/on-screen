package onscreen;

import java.util.Observable;

/**
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

    public PresentationTimer(ConnectedThread caller) {
        start(caller);
    }

    public synchronized void setRanTime(int ranTime) {
        collectedTime += ranTime;
    }

    public synchronized void start(ConnectedThread caller) {
        pause = false;
        startTime = getCurrentTime();
        setChanged();
        notifyObservers(new NotifyThread(0, getRunning(), caller));
    }

    public synchronized void pause(ConnectedThread caller) {
        pause = true;
        collectedTime = getCurrentTime() - startTime;
        setChanged();
        notifyObservers(new NotifyThread(0, getRunning(), caller));
    }

    public synchronized void reset(ConnectedThread caller) {
        collectedTime = 0;
        pause = true;
        collectedTime = getCurrentTime() - startTime;
        setChanged();
        notifyObservers(new NotifyThread(1, getRunning(), caller));
    }

    public synchronized byte[] getTime() {
        if (pause) {
            return intToByte(collectedTime);
        } else {
            return intToByte(getCurrentTime() - startTime + collectedTime);
        }
    }

    private int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    public void control(int read, ConnectedThread caller) {
        if (read == RESET) {
            reset(caller);
        } else if (read == PAUSE) {
            pause(caller);
        } else { //START
            start(caller);
        }
    }

    public byte getRunning() {
        if (pause) {
            return SEND_PAUSE;
        } else {
            return SEND_RUNNING;
        }
    }

    private byte[] intToByte(int input) {
        byte[] writeBuffer = new byte[4];
        writeBuffer[0] = (byte) (input >> 24);
        writeBuffer[1] = (byte) (input >> 16);
        writeBuffer[2] = (byte) (input >> 8);
        writeBuffer[3] = (byte) (input);

        return writeBuffer;
    }
}
