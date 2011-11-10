package onscreen;

import javax.swing.JFrame;

/**
 *
 * @author Mattias
 * 
 */
public class OnScreen {

    public static ImageController imageController;
    public static MouseController mouseController;
    public static JFrame frame;
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        frame = new JFrame("On Screen");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);

        frame.pack();
        
        mouseController = new MouseController();
        
        String homeFolder = System.getProperty("user.home");
        String fileLocation = homeFolder + "\\OnScreen\\";
        ImageInterface imageInterface = new ImageInterface();
        imageController = new ImageController(fileLocation, imageInterface); 
        frame.getContentPane().add(imageInterface);
        frame.setVisible(false);
        
        // Create the bluetooth listner
        Thread waitThread = new Thread(new WaitThread());
        waitThread.start();
    }
}