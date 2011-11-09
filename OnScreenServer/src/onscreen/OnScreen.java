/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onscreen;

import java.io.File;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Mattias
 * 
 * just a test
 */
public class OnScreen {

    public static ImageController imageController;
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("On Screen");
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setSize(800, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Notification not = new Notification();
        not.notify("Hello!");

        frame.pack();
        frame.setVisible(true);
        
        
        String homeFolder = System.getProperty("user.home");
        String fileLocation = homeFolder + "\\OnScreen\\";
        ImageInterface imageInterface = new ImageInterface();
        imageController = new ImageController(fileLocation, imageInterface); 
        imageController.display(new File("C:\\test.jpg"));
        frame.getContentPane().add(imageInterface);

        Thread waitThread = new Thread(new WaitThread(not));
        waitThread.start();
    }
}