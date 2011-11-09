/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onscreen;

import java.io.File;
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
        imageInterface.displayImage(displayImage);
        return false;
    }
    
    public synchronized int recive(InputStream stream, Notification noti) {
        if (imageReciver == null) {
            imageReciver = new ImageReciver(folder);
        }
        File added = imageReciver.reciveImage(stream, noti);
        images.add(added);
        noti.notify(added.getName());
        display(added);
        return images.indexOf(added);
    }
}
