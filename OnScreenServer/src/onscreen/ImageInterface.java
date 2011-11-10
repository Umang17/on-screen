/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onscreen;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.JLabel;

/**
 *
 * @author Mattias
 */
class ImageInterface extends JLabel {
    private BufferedImage image;

    public ImageInterface() {
    }

    public void displayImage(File imageFile) throws IOException {
        image = ImageIO.read(imageFile);
        this.repaint();
        
    }
    
    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }
}
