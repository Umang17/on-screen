package onscreen;

/**
 *
 * @author Mattias
 */
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Observable;
import java.util.Observer;
import javax.microedition.io.StreamConnection;

public class ConnectedThread implements Runnable, Observer {

    private StreamConnection mConnection;
    private FileReciver fileReciver;
    private OutputStream outputStream;
    private BufferedInputStream bufferedInputStream;
    private static FilePresented filePresented = null;
    private static PresentationTimer presentationTimer = null;
    private static final int EXIT_CMD = -1;
    private static final int FILE = 1;
    private static final int MOUSECONTROLLER = 2;
    private static final int KEYCONTROLLER = 5;
    private static final int TIMECONTROLL = 7;
    private static final int STARTPRESENTATION = 4;

    ConnectedThread(StreamConnection connection) {
        if (presentationTimer == null) {
            presentationTimer = new PresentationTimer(this);
        }
        mConnection = connection;
        fileReciver = new FileReciver();
    }

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
                        Notification.debugMessage("Killing connection.");
                        mConnection.close();
                        return;
                    case FILE:
                        startPresenting();
                        break;
                    case MOUSECONTROLLER:
                        OnScreen.mouseController.recive(
                                bufferedInputStream.read(), bufferedInputStream.read());
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
                        Notification.debugMessage("Unknown control sequence " + command);
                        break;
                }
            }
        } catch (Exception e) {
            Notification.debugMessage("Something went wrong ");
        }
    }

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

    private void startPresenting() throws IOException {
        if (filePresented != null) {
            OnScreen.keyController.exit();
        }
        filePresented = fileReciver.reciveFile(bufferedInputStream);
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec(OnScreen.pdfReader + "\"" + filePresented.getFullName() + "\"");
        outputStream.write(STARTPRESENTATION);
        presentationTimer.reset(this);
        presentationTimer.start(this);
        presentationTimer.addObserver(this);
    }

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
