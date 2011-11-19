package onscreen;

import java.util.ArrayList;

/**
 *
 * @author Mattias
 */
public class WriteBuffer {

    private static int MAXINQUE = 3;
    Que q = new Que();

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

    private class Que {

        ArrayList que = new ArrayList<byte[]>();

        private boolean isEmpty() {
            if (que.size() <= 0) {
                return true;
            }
            return false;
        }

        private boolean isFull() {
            if (que.size() >= MAXINQUE) {
                return true;
            }
            return false;
        }

        private byte[] get() {
            byte[] oldest = (byte[]) que.get(0);
            que.remove(0);
            return oldest;
        }

        private void put(byte[] b) {
            que.add(b);
        }
    }
}
