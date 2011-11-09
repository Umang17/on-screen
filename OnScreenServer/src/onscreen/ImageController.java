/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onscreen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

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
