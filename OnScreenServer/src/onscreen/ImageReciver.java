package onscreen;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

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
        i += unsignedByteToInt(b[pos++]) << 0;
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
        noti.notify("Size array = " + si );

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
        noti.notify("name array = " + na );

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
        
        for(int a = 0; a < size;) {

            // recive the image
            byte[] bytes = new byte[49];
            try {
                int read = stream.read(bytes, 0, 49);
                if (read == -1) {
                    out.close();
                    break;
                }
                a+=read;
                //noti.notify("One recived!");
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
        //ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
//        Iterator<?> readers = ImageIO.getImageReadersByFormatName(imageFormat);
//
//        ImageReader reader = (ImageReader) readers.next();
//        Object source = bis;
//
//        ImageInputStream iis = null;
//        try {
//            iis = ImageIO.createImageInputStream(source);
//        } catch (IOException ex) {
//            Logger.getLogger(ImageReciver.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        reader.setInput(iis, true);
//        ImageReadParam param = reader.getDefaultReadParam();
//
//        Image image = null;
//        try {
//            image = reader.read(0, param);
//        } catch (IOException ex) {
//            Logger.getLogger(ImageReciver.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        BufferedImage bufferedImage =
//                new BufferedImage(image.getWidth(null),
//                image.getHeight(null), BufferedImage.TYPE_INT_RGB);
//        Graphics2D g2 = bufferedImage.createGraphics();
//        g2.drawImage(image, null, null);
//

        
//        try {
//            ImageIO.write(bufferedImage, imageFormat, imageFile);
//        } catch (IOException ex) {
//            Logger.getLogger(ImageReciver.class.getName()).log(Level.SEVERE, null, ex);
//        }
        return imageFile;
    }
}
