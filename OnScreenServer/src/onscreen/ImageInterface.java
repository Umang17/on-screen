/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onscreen;

import java.io.File;
import javax.swing.JLabel;

/**
 *
 * @author Mattias
 */
class ImageInterface extends JLabel {

    public ImageInterface() {
    }

    public void displayImage(File image) {

        setIcon(new javax.swing.ImageIcon(image.getAbsolutePath()));
        
    }
}
