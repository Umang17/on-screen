/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onscreen;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author Mattias
 */
public class OnScreen {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        JFrame frame = new JFrame("On Screen");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea textArea = new JTextArea(50, 80);
        JScrollPane scrollPane = new JScrollPane(textArea);
        textArea.setEditable(false);
        frame.setContentPane(scrollPane);
        textArea.append("Started application\n");

        frame.pack();
        frame.setVisible(true);

        Thread waitThread = new Thread(new WaitThread(textArea));
        waitThread.start();
    }
}
