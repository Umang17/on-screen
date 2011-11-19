package onscreen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author Mattias
 */
public class FileWriterThread extends Thread {

    private File fileName;
    private BufferedOutputStream out = null;
    private WriteBuffer wb;
    private boolean running = true;

    public FileWriterThread(WriteBuffer wb, File file) {
        this.wb = wb;
        fileName = file;

        try {
            out = new BufferedOutputStream(new FileOutputStream(fileName));
        } catch (FileNotFoundException ex) {
            Notification.notify("Failed in open file");
        }
    }

    @Override
    public void run() {
        while (running) {
            write(wb.get());
        }
    }

    public synchronized void write(byte[] bytes) {
        try {
            out.write(bytes);
        } catch (IOException ex) {
        }
    }

    public synchronized void close() {
        try {
            out.flush();
            running = false;
            interrupt();
            out.close();
        } catch (IOException ex) {
        }
    }
}
