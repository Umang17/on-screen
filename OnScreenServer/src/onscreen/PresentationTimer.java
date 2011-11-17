package onscreen;

/**
 *
 * @author Mattias
 */
public class PresentationTimer {

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

    public synchronized int getTime() {
        return getCurrentTime() - startTime + collectedTime;
    }

    private int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000);
    }
}
