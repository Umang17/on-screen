package onscreen;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Mattias
 */
public class ImageReciver {

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

    public synchronized File reciveImage(InputStream stream, Notification noti) {

        // Get the size of the packet
        byte[] sizeBytes = new byte[8];
        int result = 0;
        try {
            result = stream.read(sizeBytes, 0, 8);
        } catch (IOException ex) {
            noti.notify("Failed in reciving name");
        }

        int size = byteArrayToInt(sizeBytes, 4);
        noti.notify("size is " + size);

        //TODO
        String si = "";
        for (Byte b : sizeBytes) {
            si += b;
        }
        noti.notify("Size array = " + si);
        
        //Get the length of the name
        byte[] imageNameLength = new byte[4];
        try {
            stream.read(imageNameLength, 0, 4);
        } catch (IOException ex) {
            noti.notify("Failed in reciving name length");
        }
        int nameSize = byteArrayToInt(imageNameLength, 0);
        noti.notify("size of name = " + nameSize);
        // Get the image name
        byte[] imageNameByte = new byte[nameSize];

        try {
            int read = stream.read(imageNameByte, 0, nameSize);
        } catch (IOException ex) {
            noti.notify("Failed in reciving name");
        }
        
        char[] chars = new char[imageNameByte.length];
        for (int i = 0; i < imageNameByte.length; i++) {
            chars[i] = (char)imageNameByte[i];
        }
         
        String imageName = String.copyValueOf(chars);
        noti.notify(imageName);
        String imageFormat = "jpeg";//imageName.split(".")[1];
        imageName = "fiel";//imageName.split(".")[0];
        
        String filePath = fileLocation + imageName + "." + imageFormat;
        File imageFile = new File(filePath);
        int i = 0;
        while (imageFile.exists()) {
            filePath = fileLocation + imageName + i++ + "." + imageFormat;
            imageFile = new File(filePath);
        }

        BufferedOutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(imageFile));
        } catch (FileNotFoundException ex) {
            noti.notify("Failed in writing/open file");
        }

        for (int a = 0; a<size;) {
            byte[] bytes = new byte[1000];
            try {
                int read = stream.read(bytes, 0, 1000);
                if (read == -1) {
                    out.close();
                    return imageFile;
                }
                a += read;
            } catch (IOException ex) {
                noti.notify("Failed in reciving data");
            }

            try {
                out.write(bytes);
                out.flush();
            } catch (Exception ex) {
                noti.notify("Failed in writing file");
            }
        }
        return imageFile;
    }
}
