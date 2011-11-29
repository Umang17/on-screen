package onscreen.filehandeling;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import onscreen.Notification;

/**
 * The file writer thread are responisble to write a file to disk from a queue. 
 *
 * @author Mattias
 */
public class FileWriterThread extends Thread {

    private File fileName;
    private BufferedOutputStream out = null;
    private WriteBuffer wb;

    /**
     * Creates a new file writer thread to write a file to disk.
     * 
     * @param wb the write buffer to read from
     * @param file the file to write to 
     */
    public FileWriterThread(WriteBuffer wb, File file) {
        this.wb = wb;
        fileName = file;

        try {
            out = new BufferedOutputStream(new FileOutputStream(fileName));
        } catch (FileNotFoundException ex) {
            Notification.debugMessage("Failed in open file");
        }
    }

    /**
     * Starts the thread and returns when interrupted.
     */
    @Override
    public void run() {
        while (!isInterrupted()) {
            write(wb.get());
        }
        Notification.debugMessage("Exitiing writing");
    }

    /**
     * Writes a byte array to the file. Closes the file on a nullpointer exception.
     * 
     * @param bytes the bytes to write
     */
    public synchronized void write(byte[] bytes) {
        try {
            out.write(bytes);
        } catch (IOException ex) {
        } catch (NullPointerException ex) {
            Notification.debugMessage("Exiting due to null pointer exception");
            close();
        }
    }

    /**
     * Finishes the filewriter by closing the stream and interrupting forcing 
     * run to terminate and thread to die.
     */
    public synchronized void close() {
        try {
            out.flush();
            out.close();
            interrupt();
        } catch (IOException ex) {
        }
    }

    public void abort() {
        try {
            out.close();
        } catch (IOException ex) {
        }
        fileName.delete();
        interrupt();
    }
}
