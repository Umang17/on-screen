package onscreen.communication;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;
import javax.microedition.io.StreamConnection;
import onscreen.filehandeling.FilePresented;
import onscreen.filehandeling.FileReciver;
import onscreen.Notification;
import onscreen.systemcontrol.NotifyThread;
import onscreen.OnScreen;
import onscreen.systemcontrol.PresentationTimer;

/**
 * Connected thread is lanunched when a connection is initialized. It does all 
 * handling of the connection and creates in/outstreams. 
 * 
 * @author Mattias
 */
public class ConnectedThread implements Runnable, Observer {

    private StreamConnection mConnection;
    private FileReciver fileReciver;
    private OutputStream outputStream;
    private BufferedInputStream bufferedInputStream;
    private static FilePresented filePresented = null;
    private static PresentationTimer presentationTimer = null;
    private static final int EXIT_CMD = -1;
    private static final int FILE = 1;
    private static final int KEYCONTROLLER = 5;
    private static final int TIMECONTROLL = 7;
    private static final int STARTPRESENTATION = 4;

    /**
     * 
     * @param connection the connection to handle, either bluetooth or lan.
     */
    ConnectedThread(StreamConnection connection) {
        if (presentationTimer == null) {
            presentationTimer = new PresentationTimer(this);
        }
        mConnection = connection;
        fileReciver = new FileReciver();
    }

    /**
     * Starts the handeling of the connection.
     */
    @Override
    public void run() {
        try {
            Notification.debugMessage("Have recived connection");
            // prepare to receive data
            outputStream = mConnection.openOutputStream();
            bufferedInputStream =
                    new BufferedInputStream(mConnection.openInputStream());

            sendStartMessage();

            while (true) {
                int command = bufferedInputStream.read();

                switch (command) {
                    case EXIT_CMD:
                        // Clean and terminate the connection.
                        Notification.debugMessage("Killing connection.");
                        presentationTimer.deleteObserver(this);
                        mConnection.close();
                        return;
                    case FILE:
                        startPresenting();
                        break;
                    case KEYCONTROLLER:
                        int read = bufferedInputStream.read();
                        boolean exit = OnScreen.keyController.recive(read);
                        if (exit) {
                            filePresented = null;
                        }
                        break;
                    case TIMECONTROLL:
                        Notification.debugMessage("Some timer event was recived!");
                        presentationTimer.control(bufferedInputStream.read(), this);
                        break;
                    default:
                        Notification.debugMessage("Unknown control sequence: " + command);
                        break;
                }
            }
        } catch (Exception e) {
            Notification.debugMessage("Something went wrong: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Sends a startmessage that either starts with a 0 if nothing is presenting
     * and in that case only contains a 0. If something is presenting the message
     * starts with a 1, followed by 4bytes length of name, then the name, 4 bytes 
     * time and last one byte if the time is running or not. Also sets up some 
     * timer things considering server if already presenting.
     * 
     * @throws IOException if there is a problem with the outputstream.
     */
    private void sendStartMessage() throws IOException {
        if (filePresented != null) {
            outputStream.write(1);
            outputStream.write(filePresented.getLengthofName());
            outputStream.write(filePresented.getNameAsByte());
            presentationTimer.pause(this);
            outputStream.write(presentationTimer.getTime());
            outputStream.write(presentationTimer.getRunning());
            presentationTimer.addObserver(this);
        } else {
            outputStream.write(0);
            Notification.debugMessage("Sent not presenting");
        }
        outputStream.flush();
    }

    /**
     * Runs when a file is recived. Takes care of killing the old presentation,
     * reciving the new file, and starting that presentation and timer. Also 
     * sends a start message to the phone to start presenting.
     * 
     * @throws IOException in case the out stream is not writable.
     */
    private void startPresenting() throws IOException {
        if (filePresented != null) {
            OnScreen.keyController.exit();
        }
        filePresented = fileReciver.reciveFile(bufferedInputStream);
        Runtime rt = Runtime.getRuntime();
        
        if (System.getProperty("os.name").startsWith("Windows")) {
            rt.exec(OnScreen.pdfReader + "\"" + filePresented.getFullName() + "\"");
        } else {
            String fileRun = filePresented.getFullName();
            fileRun.replace(" ", "\\ ");
            rt.exec(OnScreen.pdfReader + fileRun);
        }
               
        outputStream.write(STARTPRESENTATION);
        presentationTimer.reset(this);
        presentationTimer.start(this);
        presentationTimer.addObserver(this);
    }

    /**
     * Sends a update to threads about timer events. Will only notify phones that
     * not initiated the timer event.
     * 
     * @param o The presentation timer.
     * @param arg A notification object
     */
    @Override
    public void update(Observable o, Object arg) {
        Notification.debugMessage("Notifying about update!");
        try {
            NotifyThread nt = (NotifyThread) arg;
            if (nt.getCaller().equals(this)) {
                return;
            }
            outputStream.write(TIMECONTROLL);
            outputStream.write(nt.getRunning());
            outputStream.write(nt.getReset());

        } catch (IOException ex) {
            Notification.debugMessage("Someting went wrong "
                    + "when notifying about timer events "
                    + ex.getLocalizedMessage());
        }
    }
}
