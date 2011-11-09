package com.and;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainActivity extends Activity
{
    private BluetoothAdapter mBluetoothAdapter;
    private static final UUID MY_UUID_INSECURE =
        UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
    private BluetoothSocket mmSocket;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        
    }

    private void doIt(BluetoothDevice device) throws FileNotFoundException, IOException {
        mmSocket = device.createInsecureRfcommSocketToServiceRecord(
                            MY_UUID_INSECURE);
        
        mmSocket.connect();
        Log.d("BLUETOOTH", "Connected!");
        OutputStream stream = mmSocket.getOutputStream();
        
        stream.write(1); // typ
        
        
        File dir = Environment.getExternalStorageDirectory();
        File yourFile = new File(dir, "DCIM/.thumbnails/1.jpg");
        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(yourFile));
        byte[] buffer = new byte[49];
        long length = yourFile.length();
        stream.write(longToBytes(length)); // storlek
        stream.write(new byte[12]);//{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 ,0});
        Log.d("Bluetooth", "length" + length);
        for (int i = 0; i < length; i+=50) {
            if (i > length) break;
            
            long upper = (i+49 > length) ? length - 1 : i+49;
            //Log.d("bluetooth", "i = " + i + " upper = " + upper + " length = " + length);
                buf.read(buffer, 0, (int)49);
                stream.write(buffer);
            stream.flush();
        }
        mmSocket.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("00:1F:E1:EB:3B:DE");
        try {
            doIt(device);
            
            
            
        } catch (IOException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
            
        }
    }
    
    
    
    public final byte[] longToBytes(long v) {
        byte[] writeBuffer = new byte[ 8 ];

        writeBuffer[0] = (byte)(v >>> 56);
        writeBuffer[1] = (byte)(v >>> 48);
        writeBuffer[2] = (byte)(v >>> 40);
        writeBuffer[3] = (byte)(v >>> 32);
        writeBuffer[4] = (byte)(v >>> 24);
        writeBuffer[5] = (byte)(v >>> 16);
        writeBuffer[6] = (byte)(v >>>  8);
        writeBuffer[7] = (byte)(v >>>  0);
        
        Log.d("bluetooth", "Buffer for size! " + writeBuffer.toString());

        return writeBuffer;
    }

}
