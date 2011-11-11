package onscreen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mattias
 */
public class FileWriterThread extends Thread {
    
    private File imageFile;
    private BufferedOutputStream out = null;
    private boolean closed = false;
    
    public FileWriterThread(String path, String file) {
        setFile(path, file);
    }
    
    public synchronized void setFile(String path, String file) {
        imageFile = new File(path + file);

        for (int i = 0; imageFile.exists();) {
            imageFile = new File(path + i++ + file);
        }

        try {
            out = new BufferedOutputStream(new FileOutputStream(imageFile));
        } catch (FileNotFoundException ex) {
            Notification.notify("Failed in open file");
        }
        
    }

    @Override
    public void run() {
        while(!closed) {
            
        }
    }
    
    public synchronized void write(byte[] bytes) {
        try {
            out.write(bytes);
            out.flush();
        } catch (IOException ex) {}
    }
    
    public synchronized void close() {
         try {
            out.flush();
            out.close();
        } catch (IOException ex) {}
        closed = true;
    }
    
    public synchronized File getFile() {
        return imageFile;
    }

    void write(byte[] bytes, int read) {
        for (int i = 0; i < read; i++) {
            byte[] toW = new byte[1];
            toW[0] = bytes[i];
            try {
                out.write(toW);
                out.flush();
            } catch (IOException ex) {}
        }
    }
}
