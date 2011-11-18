package onscreen;

import java.io.BufferedInputStream;
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

    public synchronized FilePresented reciveFile(BufferedInputStream stream) {
        Notification.notify("Starting to recive file");

        int size = read(stream, 8, 4);

        int nameSize = read(stream, 4);
        Notification.notify("name size " + nameSize);

        String fileName = readString(stream, nameSize);

        return reciveFile(stream, size, fileName);
    }

    private FilePresented reciveFile(InputStream stream, int size, String fileName) {
        FileWriterThread fw = new FileWriterThread(fileLocation, fileName);
        fw.start();
        try {
            for (int a = 0; a < size + NUM_BYTES;) {
                byte[] bytes = new byte[NUM_BYTES];
                int read = stream.read(bytes);
                if (read < 0) {
                    break;
                }
                if (read == NUM_BYTES) {
                    fw.write(bytes);
                }
                if (read < NUM_BYTES) {
                    fw.write(bytes, read);
                    break;
                }
                a += read;
            }
        } catch (IOException ex) {
            Notification.notify("Failed in reciving or writing data");
        }
        FilePresented file = new FilePresented(fileLocation, fw.getFile().getName());
        fw.close();
        return file;
    }

    private String byteToString(byte[] imageNameByte) {
        char[] chars = new char[imageNameByte.length];
        for (int i = 0; i < imageNameByte.length; i++) {
            chars[i] = (char) imageNameByte[i];
        }
        return String.copyValueOf(chars);
    }    
    
    private static int byteArrayToInt(byte[] b, int offset) {
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

    private int read(BufferedInputStream stream, int i) {
        return read(stream, i, 0);
    }

    private int read(BufferedInputStream stream, int i, int offset) {
        byte[] sizeBytes = new byte[i];
        try {
            stream.read(sizeBytes, 0, i);
        } catch (IOException ex) {
            Notification.notify("Failed in reciving " + ex.getLocalizedMessage());
        }

        return byteArrayToInt(sizeBytes, offset);
    }

    private String readString(BufferedInputStream stream, int nameSize) {
        byte[] imageNameByte = new byte[nameSize];

        try {
            stream.read(imageNameByte, 0, nameSize);
        } catch (IOException ex) {
            Notification.notify("Failed in reciving " + ex.getLocalizedMessage());
        }
        return byteToString(imageNameByte);
    }
}
