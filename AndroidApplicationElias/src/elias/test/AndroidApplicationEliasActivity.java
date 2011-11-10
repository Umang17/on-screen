package elias.test;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

public class AndroidApplicationEliasActivity extends Activity {
    /** Called when the activity is first created. */
	
	private BluetoothAdapter mBluetoothAdapter;
    private static final UUID MY_UUID_INSECURE =
        UUID.fromString("04c6093b-0000-1000-8000-00805f9b34fb");
    private BluetoothSocket mmSocket;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("00:1F:E1:EB:3B:DE");
        try {
            mmSocket = device.createInsecureRfcommSocketToServiceRecord(
                                MY_UUID_INSECURE);
            
            mmSocket.connect();
            Log.d("BLUETOOTH", "Connected!");
            OutputStream stream = mmSocket.getOutputStream();
            
            stream.write(1); // typ
            
            
            File dir = Environment.getExternalStorageDirectory();
            File yourFile = new File(dir, "/DCIM/.thumbnails/1308403829869.jpg");
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(yourFile));
            byte[] buffer = new byte[50];
            long length = yourFile.length();
            stream.write(longToBytes(length)); // storlek
            stream.write(new byte[12]);
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream("/mnt/sdcard/download/test5.jpg"));
            long i=0;
            for(;i<=length; i+=50) {
            	buf.read(buffer);
            	stream.write(buffer);
            	stream.flush();
            }
            if(i>length) {
            	buf.read(buffer, 0, (int)(50+(length-i)));
            	stream.write(buffer, 0, (int)(50+(length-i)));
            }
            stream.flush();
            
            Log.d("BLUETOOTH", "i = " + i + " length = " + length);
                        
        } catch (IOException ex) {
            Logger.getLogger(AndroidApplicationEliasActivity.class.getName()).log(Level.SEVERE, null, ex);
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

        return writeBuffer;
    }
}