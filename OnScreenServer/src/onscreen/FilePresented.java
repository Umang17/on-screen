package onscreen;

import java.io.File;

/**
 * Represents the file currently being presented. Are able to keep track of the 
 * name and path.
 * 
 * @author Mattias
 */
public class FilePresented {

    private String path;
    private String name;

    /**
     * Creates a new file presented.
     * 
     * @param path the path to the file not including the file name
     * @param name the name of the file not including the path but including the 
     * file extention
     */
    public FilePresented(String path, String name) {
        this.path = path;
        this.name = name;
    }

    /**
     * Creates a file representation of the file presented.
     * 
     * @return a file representing the file presented
     */
    public synchronized File getFile() {
        return new File(path + name);
    }

    /**
     * Get the name of the file without the path.
     *  
     * @return the name of the file
     */
    public synchronized String getName() {
        return name;
    }

    /**
     * Get the full name and path of the file
     * 
     * @return path and name of the file
     */
    public synchronized String getFullName() {
        return path + name;
    }

    /**
     * Get the length of the file name as a byte array
     * 
     * @return length of file name 
     */
    public synchronized byte[] getLengthofName() {
        return intToByte(getNameAsByte().length);
    }

    /**
     * Get the name of the file as a byte array
     * 
     * @return the name of the file
     */
    public synchronized byte[] getNameAsByte() {
        return stringToBytes(getName());
    }

    /**
     * Creates a byte array from a int
     * 
     * @param input the int to create from
     * @return a byte array of length 4 containing the int
     */
    private byte[] intToByte(int input) {
        byte[] writeBuffer = new byte[4];
        writeBuffer[0] = (byte) (input >> 24);
        writeBuffer[1] = (byte) (input >> 16);
        writeBuffer[2] = (byte) (input >> 8);
        writeBuffer[3] = (byte) (input);
        return writeBuffer;
    }

    /**
     * Creates a byte array from a string
     * 
     * @param str the string to create from
     * @return a byte array containing the string
     */
    private byte[] stringToBytes(String str) {
        return charArrayToBytes(str.toCharArray());
    }

    /**
     * Creates a byte array from a char array
     * 
     * @param array the char array to create from
     * @return the byte array containing the char array
     */
    private byte[] charArrayToBytes(char[] array) {
        byte[] bytes = new byte[array.length];
        for (int i = 0; i < array.length; i++) {
            bytes[i] = (byte) array[i];
        }
        return bytes;
    }
}
