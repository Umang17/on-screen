package onscreen;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JLabel;

/**
 *
 * @author Mattias
 */
public class ImageInterface extends JLabel {

    private BufferedImage image;
    private int screenX;
    private int screenY;
    
    public ImageInterface() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        screenX = (int) toolkit.getScreenSize().getWidth();
        screenY = (int) toolkit.getScreenSize().getHeight();
    }

    public synchronized void displayImage(File imageFile) throws IOException {
        BufferedImage imageIn = ImageIO.read(imageFile);
        
        // the created image to show
        BufferedImage scaledImage = new BufferedImage(
                screenX, screenY, BufferedImage.TYPE_INT_ARGB);

        // do the rendering of the new image
        Graphics2D graphics2D = scaledImage.createGraphics();
        graphics2D.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics2D.drawImage(imageIn, 0, 0, screenX, screenY, null);

        graphics2D.dispose();
        
        image = scaledImage;

        this.repaint();
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(image, 0, 0, null);
    }
}
