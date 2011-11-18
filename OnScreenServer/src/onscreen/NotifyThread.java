/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onscreen;

/**
 *
 * @author Mattias
 */
public class NotifyThread {
    
    private int reset;
    private int running;
    private ConnectedThread conThread;
    
    public NotifyThread (int reset, int running, ConnectedThread conThread) {
        this.reset = reset;
        this.running = running;
        this.conThread = conThread;
    } 
    
    public int getReset() {
        return reset;
    }
    
    public int getRunning() {
        return running;
    }
    
    public ConnectedThread getCaller() {
        return conThread;
    }
    
}
