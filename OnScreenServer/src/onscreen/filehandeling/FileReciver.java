package onscreen.filehandeling;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import onscreen.Notification;

/**
 * Recives a file on a connection and take care of writing it to file.
 *
 * @author Mattias
 */
public class FileReciver {

    private final int NUM_BYTES = 1000;
    private String fileLocation;
    private String separator;

    /**
     * Creates a new file reciver and sets the folder properties.
     */
    public FileReciver() {
        String homeFolder = System.getProperty("user.home");
        separator = System.getProperty("file.separator");
        fileLocation = homeFolder + separator + "OnScreen" + separator;
        File dir = new File(fileLocation);
        if (!dir.exists()) {
            dir.mkdir();
        }
    }

    /**
     * Reciving the file from an connection and writes it to disk. also reads the 
     * file data suck as size and name.
     *  
     * @param stream the inputstream to recive from 
     * @return a file presented
     * @throws IOException if there is a problem reading or writing
     */
    public synchronized FilePresented reciveFile(BufferedInputStream stream) throws IOException {
        Notification.debugMessage("Starting to recive file");

        int size = read(stream, 8, 4);

        int nameSize = read(stream, 4);
        Notification.debugMessage("name size " + nameSize);

        String fileName = readString(stream, nameSize);

        return reciveFile(stream, size, fileName);
    }

    /**
     * Recives the actual file and writes it to disk.
     * 
     * @param stream the stream to recive from
     * @param size the size of the file, in bytes
     * @param fileName the name of the file
     * @return the file recived 
     * @throws IOException if problem writing or reading
     */
    private FilePresented reciveFile(BufferedInputStream stream, int size, String fileName) throws IOException {
        WriteBuffer wb = new WriteBuffer();

        // Creates the file and makes sure that it does not exist already.
        File file = new File(fileLocation + fileName);
        System.out.println(fileName);
        for (int i = 0; file.exists();) {
            String fName = fileName.split("\\.")[0];
            String type = fileName.split("\\.")[1];
            file = new File(fileLocation + fName + i++ + "." + type);
        }

        // Creates a new thread to write the file
        FileWriterThread fw = new FileWriterThread(wb, file);
        fw.start();
        try {       
            byte[] bytes = new byte[NUM_BYTES];
            
            for (int a = 0; a < size;) {
                if ((a + NUM_BYTES) > size) {
                    int use = NUM_BYTES;
                    use = size - a;
                    bytes = new byte[use];
                } 
                
                int read = stream.read(bytes);
                wb.put(subArray(bytes, 0, read));
                 
                a += read;
            }
        } catch (IOException ex) {
            Notification.debugMessage("Failed in reciving or writing data");
        } catch (NullPointerException ex) {
            Notification.debugMessage("Failed in reciving or writing data np excpe");
        }
        fw.close();

        FilePresented filePres = new FilePresented(fileLocation, file.getName());
        return filePres;
    }

    /**
     * Creates a string from the recived byte array
     * 
     * @param bytes the bytes to transform
     * @return the string represented by the bytes
     */
    private String byteToString(byte[] bytes) {
        char[] chars = new char[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            chars[i] = (char) bytes[i];
        }
        return String.copyValueOf(chars);
    }

    /**
     * Creates a int from a recived byte array.
     * 
     * @param b the byte array to transform
     * @param offset the position where to start in the byte array
     * @return the int representing the byte array
     */
    private static int byteArrayToInt(byte[] b, int offset) {
        int i = 0;
        int pos = offset;
        i += unsignedByteToInt(b[pos++]) << 24;
        i += unsignedByteToInt(b[pos++]) << 16;
        i += unsignedByteToInt(b[pos++]) << 8;
        i += unsignedByteToInt(b[pos++]);
        return i;
    }

    /**
     * Creates a int from a byte
     * 
     * @param b the byte
     * @return a int 
     */
    private static int unsignedByteToInt(byte b) {
        return (int) b & 0xFF;
    }

    /**
     * Reads a number of bytes from a stream offset defaults to 0.
     * 
     * @param stream the stream to read from
     * @param i the length to read
     * @return the read integer
     */
    private int read(BufferedInputStream stream, int i) {
        return read(stream, i, 0);
    }

    /**
     * Reads a int from the stream
     * 
     * @param stream the stream to read from 
     * @param i the length to read
     * @param offset the offset to read 
     * @return the read integer
     */
    private int read(BufferedInputStream stream, int i, int offset) {
        byte[] sizeBytes = new byte[i];
        try {
            stream.read(sizeBytes, 0, i);
        } catch (IOException ex) {
            Notification.debugMessage("Failed in reciving " + ex.getLocalizedMessage());
        }

        return byteArrayToInt(sizeBytes, offset);
    }

    /**
     * Reads a string from the stream
     * 
     * @param stream the stream to read from
     * @param nameSize the size of the stream to run
     * @return the string read from the stream
     */
    private String readString(BufferedInputStream stream, int nameSize) {
        byte[] imageNameByte = new byte[nameSize];

        try {
            stream.read(imageNameByte, 0, nameSize);
        } catch (IOException ex) {
            Notification.debugMessage("Failed in reciving " + ex.getLocalizedMessage());
        }
        return byteToString(imageNameByte);
    }

    /**
     * Creates a subarray between indexes for a byte array
     *  
     * @param bytes the original byte array
     * @param first the first byte
     * @param last the last byte
     * @return the byte array stripped down
     */
    private byte[] subArray(byte[] bytes, int first, int last) {
        if (first == 0 && last == bytes.length) {
            return bytes;
        }
        byte[] stripped = new byte[last - first];
        for (int i = first; i < last; i++) {
            stripped[i - first] = bytes[i];
        }
        return stripped;
    }
}
