package onscreen.filehandeling;

import java.util.ArrayList;

/**
 * A write buffer to be used between threads.
 *
 * @author Mattias
 */
public class WriteBuffer {

    private static int MAXINQUEUE = 3;
    Queue q = new Queue();

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
        byte[] b = q.get();
        notifyAll();
        return b;
    }

    /**
     * Adds a new byte array to the buffer
     * 
     * @param b the byte array to add
     */
    public synchronized void put(byte[] b) {
        if (q.isFull()) {
            try {
                wait();
            } catch (InterruptedException ex) {
                return;
            }
        }
        q.put(b);
        notifyAll();
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
