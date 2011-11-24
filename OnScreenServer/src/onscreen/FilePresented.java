package onscreen;

import java.io.File;

/**
 *
 * @author Mattias
 */
public class FilePresented {

    private String path;
    private String name;

    public FilePresented(String path, String name) {
        this.path = path;
        this.name = name; 
    }

    public synchronized File getFile() {
        return new File(path + name);
    }

    public synchronized String getName() {
        return name;
    }
    public synchronized String getFullName() {
        return path + name;   
    }

    public synchronized byte[] getLengthofName() {
        return intToByte(getNameAsByte().length);
    }

    public synchronized byte[] getNameAsByte() {
        return stringToBytes(getName());
    }

    private byte[] intToByte(int input) {
        byte[] writeBuffer = new byte[4];
        writeBuffer[0] = (byte) (input >> 24);
        writeBuffer[1] = (byte) (input >> 16);
        writeBuffer[2] = (byte) (input >> 8);
        writeBuffer[3] = (byte) (input);
        return writeBuffer;
    }
    
    private byte[] stringToBytes(String str) {
        return charArrayToBytes(str.toCharArray());
    }
    
    private byte[] charArrayToBytes(char[] array) {
        byte[] bytes = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            bytes[i] = (byte) array[i];
        }
        return bytes;
    }
}
