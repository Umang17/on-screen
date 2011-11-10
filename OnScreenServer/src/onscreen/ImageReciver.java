package onscreen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Mattias
 */
public class ImageReciver {

    private final int NUM_BYTES = 1000;
    private String fileLocation;

    public ImageReciver(String fileLocation) {
        this.fileLocation = fileLocation;
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

    public synchronized File reciveImage(InputStream stream) {

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

        String filePath = fileLocation + imageName;
        File imageFile = new File(filePath);

        for (int i = 0; imageFile.exists();) {
            imageFile = new File(fileLocation + i++ + imageName);
        }

        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(imageFile));
        } catch (FileNotFoundException ex) {
            Notification.notify("Failed in open file");
        }

        long timeS = System.currentTimeMillis();
        for (int a = 0; a < size + NUM_BYTES;) {
            byte[] bytes = new byte[NUM_BYTES];
            try {
                a += stream.read(bytes, 0, NUM_BYTES);
                out.write(bytes);
                out.flush();
            } catch (IOException ex) {
                Notification.notify("Failed in reciving or writing data");
            }
        }
        double t = (System.currentTimeMillis() - timeS) / 1000;
        Notification.notify("It took: " + t + "seconds");
        return imageFile;
    }
}
