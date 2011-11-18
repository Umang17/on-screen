package onscreen;

/**
 *
 * @author Mattias
 */
public class PresentationTimer {

    private static final int RESET = 8;
    private static final int PAUSE = 9;
    private int startTime;
    private int collectedTime;

    public PresentationTimer() {
        start();
    }

    public synchronized void start() {
        startTime = getCurrentTime();
    }

    public synchronized void pause() {
        collectedTime = getCurrentTime() - startTime;
    }

    public synchronized void reset() {
        collectedTime = 0;
    }

    public synchronized byte[] getTime() {
        return intToByte(getCurrentTime() - startTime + collectedTime);
    }

    private int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }

    void control(int read) {
        if (read == RESET) {
            reset();
        } else if (read == PAUSE) {
            pause();
        } else { //START
            start();
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
