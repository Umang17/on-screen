package onscreen;

/**
 *
 * @author Mattias
 */
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.StreamConnection;

public class ProcessConnectionThread implements Runnable {

    private StreamConnection mConnection;
    private FileReciver fileReciver;
    private static final int EXIT_CMD = 10;
    private static final int FILE = 1;
    private static final int MOUSECONTROLLER = 2;
    private static final int REQUESTCONTROL = 3;
    private static final int RELEASECONTROL = 4;
    private static final int KEYCONTROLLER = 5;
    private static final int RELEASE = 6;
    private static final int TIMECONTROLL = 7;
    private static final int RESET = 8;
    private static final int PAUSE = 9;
    private static final int MESSAGE_PRESENTING = 3;
    private static final int STARTPRESENTATION = 4;
    private static ProcessConnectionThread lockOwner = null;
    private InputStream inputStream;
    private OutputStream outputStream;
    private FilePresented filePresented;
    private boolean PRESENTING = false;
    private PresentationTimer presentationTimer;

    ProcessConnectionThread(StreamConnection connection) {
        mConnection = connection;
        fileReciver = new FileReciver();
    }

    private synchronized boolean lockControl() {
        lockOwner = this;
        return true;
    }

    private synchronized boolean canControl() {
        if (lockOwner == this) {
            return true;
        }
        return false;
    }

    private synchronized void releaseControl() {
        if (canControl()) {
            lockOwner = null;
        }
    }

    public void sendReleaseRequest() {
        try {
            outputStream.write(RELEASE);
            outputStream.flush();
        } catch (IOException ex) {
        }
    }

    @Override
    public void run() {
        try {
            Notification.notify("Have recived connection");
            // prepare to receive data
            inputStream = mConnection.openInputStream();
            outputStream = mConnection.openOutputStream();
            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            //outputStream.write(MESSAGE_PRESENTING);
            if (PRESENTING) {
                outputStream.write(1);
                outputStream.write(filePresented.getLengthofName());
                outputStream.write(filePresented.getNameAsByte());
                outputStream.write(filePresented.getCurrentSlide());
                outputStream.write(filePresented.getTotalSlides());
                outputStream.write(presentationTimer.getTime());
            } else {
                outputStream.write(0);
            }
            outputStream.flush();

            while (true) {
                int command = bufferedInputStream.read();

                switch (command) {
                    case EXIT_CMD:
                        mConnection.close();
                        return;
                    case FILE:
                        startPresenting(bufferedInputStream, outputStream);

                        break;
                    case MOUSECONTROLLER:
                        if (canControl()) {
                            OnScreen.mouseController.recive(bufferedInputStream);
                        } else {
                            bufferedInputStream.read();
                            bufferedInputStream.read();
                        }
                        break;
                    case KEYCONTROLLER:
                        if (canControl()) {
                            PRESENTING = OnScreen.keyController.recive(bufferedInputStream.read(), filePresented);
                        } else {
                            bufferedInputStream.read();
                        }
                        break;
                    case TIMECONTROLL:
                        int read = bufferedInputStream.read();
                        if (read == RESET) {
                            presentationTimer.reset();
                        } else if (read == PAUSE ) {
                            presentationTimer.pause();
                        } else { //START
                            presentationTimer.start();
                        }
                        break;
                    case REQUESTCONTROL:
                        if (lockControl()) {
                            outputStream.write(1);
                        } else {
                            outputStream.write(0);
                        }
                        break;
                    case RELEASECONTROL:
                        releaseControl();
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

    private void startPresenting(BufferedInputStream bufferedInputStream, OutputStream outputStream) throws IOException {
        filePresented = fileReciver.reciveFile(bufferedInputStream);
        Runtime rt = Runtime.getRuntime();
        Process pr = rt.exec(OnScreen.pdfReader + filePresented);
        outputStream.write(STARTPRESENTATION);
        PRESENTING = true;
        presentationTimer = new PresentationTimer();
    }
}
