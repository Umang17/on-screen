package onscreen;

import java.util.ArrayList;

/**
 *
 * @author Mattias
 */
public class WriteBuffer {

    private static int MAXINQUEUE = 3;
    Queue q = new Queue();

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

    private class Queue {

        ArrayList queue = new ArrayList<byte[]>();

        private boolean isEmpty() {
            if (queue.size() <= 0) {
                return true;
            }
            return false;
        }

        private boolean isFull() {
            if (queue.size() >= MAXINQUEUE) {
                return true;
            }
            return false;
        }

        private byte[] get() {
            byte[] oldest = (byte[]) queue.get(0);
            queue.remove(0);
            return oldest;
        }

        private void put(byte[] b) {
            queue.add(b);
        }
    }
}
