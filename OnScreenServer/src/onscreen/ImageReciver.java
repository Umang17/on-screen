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

    private String fileLocation;

    public ImageReciver(String fileLocation) {
        this.fileLocation = fileLocation;
    }

    public static int byteArrayToInt(byte[] b, int offset) {
        int i = 0;
        int pos = 4;
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

        int size = byteArrayToInt(sizeBytes, 0);
        noti.notify("size is " + size);

        //TODO
        String si = "";
        for (Byte b : sizeBytes) {
            si += b;
        }
        noti.notify("Size array = " + si);

        // Get the image name
        byte[] imageNameByte = new byte[12];

        try {
            int read = stream.read(imageNameByte, 0, 12);
        } catch (IOException ex) {
            noti.notify("Failed in reciving bytes");
        }
        //FIXME
        String imageName = imageNameByte.toString();
        String imageFormat = "jpg";//imageName.split(".")[1];

        String na = "";
        for (Byte b : imageNameByte) {
            na += b;
        }
        noti.notify("name array = " + na);

        //imageName = imageName.split(".")[0];
        //FIXME 
        imageName = "myimage";

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

        for (int a = 0; a < size;) {
            byte[] bytes = new byte[49];
            try {
                int read = stream.read(bytes, 0, 49);
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
