package onscreen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Mattias
 */
public class FileReciver {

    private final int NUM_BYTES = 1000;
    private String fileLocation;

    public FileReciver() {
        String homeFolder = System.getProperty("user.home");
        String separator = System.getProperty("file.separator");
        fileLocation = homeFolder + separator + "OnScreen" + separator;
    }

    public static int byteArrayToInt(byte[] b, int offset) {
        int i = 0;
        int pos = offset;
        i += unsignedByteToInt(b[pos++]) << 24;
        i += unsignedByteToInt(b[pos++]) << 16;
        i += unsignedByteToInt(b[pos++]) << 8;
        i += unsignedByteToInt(b[pos++]);
        return i;
    }

    private static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    public synchronized String reciveFile(InputStream stream) {
        Notification.notify("Starting to recive file");
        // Get the size of the packet
        byte[] sizeBytes = new byte[8];
        try {
            stream.read(sizeBytes, 0, 8);
        } catch (IOException ex) {
            Notification.notify("Failed in reciving name");
        }

        int size = byteArrayToInt(sizeBytes, 4);

        //Get the length of the name
        byte[] imageNameLength = new byte[4];
        try {
            stream.read(imageNameLength, 0, 4);
        } catch (IOException ex) {
            Notification.notify("Failed in reciving name length");
        }
        int nameSize = byteArrayToInt(imageNameLength, 0);

        // Get the image name
        byte[] imageNameByte = new byte[nameSize];

        try {
            stream.read(imageNameByte, 0, nameSize);
        } catch (IOException ex) {
            Notification.notify("Failed in reciving name");
        }

        char[] chars = new char[imageNameByte.length];
        for (int i = 0; i < imageNameByte.length; i++) {
            chars[i] = (char) imageNameByte[i];
        }
        String imageName = String.copyValueOf(chars);
        
        FileWriterThread fw = new FileWriterThread(fileLocation, imageName);
        fw.start();

        long timeS = System.currentTimeMillis();
        
        try {
            for (int a = 0; a < size + NUM_BYTES;) {
                byte[] bytes = new byte[NUM_BYTES];
                int read = stream.read(bytes);
                if (read < 0) break;
                if (read == NUM_BYTES) fw.write(bytes);
                if (read < NUM_BYTES) {
                    fw.write(bytes, read);
                    break;
                }
                a += read;
            }
            
        } catch (IOException ex) {
            Notification.notify("Failed in reciving or writing data");
        }
        File fileName = fw.getFile();
        fw.close();
        double t = (System.currentTimeMillis() - timeS) / 1000;
        Notification.notify("It took: " + t + "seconds");
        return fileName.toString();
    }
}
