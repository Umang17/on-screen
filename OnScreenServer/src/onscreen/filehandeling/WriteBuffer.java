package onscreen.filehandeling;

import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import onscreen.Notification;

/**
 * A write buffer to be used between threads.
 *
 * @author Mattias
 */
public class WriteBuffer {

    private static int MAXINQUEUE = 15;
    //Queue q = new Queue();
    ArrayBlockingQueue<byte[]> q = new ArrayBlockingQueue<byte[]>(MAXINQUEUE);

    /**
     * Get the next byte array to use
     * 
     * @return next byte array 
     */
    public synchronized byte[] get() {
        if (q.isEmpty()) {
            try {
                wait();
            } catch (InterruptedException ex) {
                return null;
            }
        }
        byte[] b = q.poll();
        notifyAll();
        return b;
    }

    /**
     * Adds a new byte array to the buffer
     * 
     * @param b the byte array to add
     */
    public synchronized void put(byte[] b) {
        if (q.size() >= MAXINQUEUE) {
            try {
                wait();
            } catch (InterruptedException ex) {
                ex.printStackTrace();
                return;
            }
        }
        q.add(b);
        notifyAll();
    }

    public void waitForAllRead() {
        while (!q.isEmpty()) {
        }
    }

    /**
     * A simple FIFO queue
     */
    private class Queue {

        ArrayList queue = new ArrayList<byte[]>();

        /**
         * Is the queue empty?
         * 
         * @return true if empty false otherwise 
         */
        private boolean isEmpty() {
            if (queue.size() <= 0) {
                return true;
            }
            return false;
        }

        /**
         * Is the queue full?
         * 
         * @return true if full false otherwise
         */
        private boolean isFull() {
            if (queue.size() >= MAXINQUEUE) {
                return true;
            }
            return false;
        }

        /**
         * Get and delete the first element in the queue
         * 
         * @return the first element in the queue
         */
        private byte[] get() {
            byte[] oldest = (byte[]) queue.get(0);
            queue.remove(0);
            return oldest;
        }

        /**
         * Adds a new element last in the queue.
         * 
         * @param b the element to add
         */
        private void put(byte[] b) {
            queue.add(b);
        }
    }
}
