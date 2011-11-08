package com.and;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.util.Log;
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
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice device = mBluetoothAdapter.getRemoteDevice("00:1F:E1:EB:3B:DE");
        try {
            mmSocket = device.createInsecureRfcommSocketToServiceRecord(
                                MY_UUID_INSECURE);
            
            mmSocket.connect();
            Log.d("BLUETOOTH", "Connected!");
            OutputStream stream = mmSocket.getOutputStream();
            
            stream.write(1);
            stream.write(2);
            stream.flush();
            
        } catch (IOException ex) {
            Logger.getLogger(MainActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
