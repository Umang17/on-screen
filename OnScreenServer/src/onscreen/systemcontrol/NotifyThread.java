package onscreen.systemcontrol;

import onscreen.communication.ConnectedThread;

/**
 * A simple class to send data between the timer and connections to handle timer 
 * events in a nice way.
 * 
 * @author Mattias
 */
public class NotifyThread {

    private int reset;
    private int running;
    private ConnectedThread conThread;

    /**
     * Creates a new notifiy.
     * For the integers (reset and running) 1 = true and 0 = false.
     * 
     * @param reset whether the timer is reseted or not
     * @param running whether the timer is running or not 
     * @param conThread the calling thread so it does not get notified
     */
    public NotifyThread(int reset, int running, ConnectedThread conThread) {
        this.reset = reset;
        this.running = running;
        this.conThread = conThread;
    }

    /**
     * Get whether the thread is reseted or not. 
     *
     * @return if the notify is reset or not 1 = true and 0 = false
     */
    public int getReset() {
        return reset;
    }

    /**
     * Get whether the timer is running or not.
     * 
     * @return if the timer is running or not 1 = true and 0 = false 
     */
    public int getRunning() {
        return running;
    }

    /**
     * Gets the calling thread, not to notify.
     * 
     * @return the caller thread
     */
    public ConnectedThread getCaller() {
        return conThread;
    }
}
