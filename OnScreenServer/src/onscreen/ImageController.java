package onscreen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 *
 * @author Mattias
 */
public class ImageController {
    
    private ImageReciver imageReciver = null;
    private Vector<File> images;
    private String folder;
    private final ImageInterface imageInterface;
    
    public ImageController(String serviceFolder, ImageInterface imageInterface) {
        images = new Vector<File>(10);
        folder = serviceFolder + "images\\";
        this.imageInterface = imageInterface;
    }
    
    public synchronized int append(File newImage) {
        images.add(newImage);
        return images.indexOf(newImage);
    }
    
    public synchronized boolean display(int index) {
        
        return true;
    }
    
    public synchronized boolean display(File displayImage) {
        try {
            imageInterface.displayImage(displayImage);
        } catch (IOException ex) {
            return false;
        }
        return true;
    }
    
    public synchronized void recive(InputStream stream) {
        if (imageReciver == null) {
            imageReciver = new ImageReciver(folder);
        }
        File added = imageReciver.reciveImage(stream);
        images.add(added);
        Notification.notify(added.getName());
        display(added);
    }
}
