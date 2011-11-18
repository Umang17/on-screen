package onscreen;

/**
 *
 * @author Mattias
 */
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import javax.microedition.io.StreamConnection;

public class ConnectedThread implements Runnable {

    private StreamConnection mConnection;
    private FileReciver fileReciver;
    private OutputStream outputStream;
    private BufferedInputStream bufferedInputStream;
    private static FilePresented filePresented = null;
    private static PresentationTimer presentationTimer;
    private static final int EXIT_CMD = -1;
    private static final int FILE = 1;
    private static final int MOUSECONTROLLER = 2;
    private static final int KEYCONTROLLER = 5;
    private static final int TIMECONTROLL = 7;
    private static final int STARTPRESENTATION = 4;

    ConnectedThread(StreamConnection connection) {
        mConnection = connection;
        fileReciver = new FileReciver();
    }

    @Override
    public void run() {
        try {
            Notification.notify("Have recived connection");
            // prepare to receive data
            outputStream = mConnection.openOutputStream();
            bufferedInputStream = 
                    new BufferedInputStream(mConnection.openInputStream());

            sendStartMessage();

            while (true) {
                int command = bufferedInputStream.read();

                switch (command) {
                    case EXIT_CMD:
                        Notification.notify("Killing connection.");
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
                        boolean exit = OnScreen.keyController.recive(read, filePresented);
                        if (exit) {
                            filePresented = null;
                        }
                        break;
                    case TIMECONTROLL:
                        presentationTimer.control(bufferedInputStream.read());
                        break;
                    default:
                        Notification.notify("Unknown control sequence " + command);
                        break;
                }

            }
        } catch (Exception e) {
            Notification.notify("Something went wrong ");
            e.printStackTrace();
        }
    }

    private void sendStartMessage() throws IOException {
        if (filePresented != null) {
            outputStream.write(1);
            outputStream.write(filePresented.getLengthofName());
            outputStream.write(filePresented.getNameAsByte());
            outputStream.write(filePresented.getCurrentSlide());
            outputStream.write(filePresented.getTotalSlides());
            outputStream.write(presentationTimer.getTime());
            Notification.notify("Sent presenting...");
        } else {
            outputStream.write(0);
            Notification.notify("Sent a 0...");
        }
        outputStream.flush();
    }

    private void startPresenting() throws IOException {
        filePresented = fileReciver.reciveFile(bufferedInputStream);
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec(OnScreen.pdfReader + "\"" + filePresented.getFullName() + "\"");
        outputStream.write(STARTPRESENTATION);
        presentationTimer = new PresentationTimer();
    }
}
