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
        String deviceAddr = "00:1F:E1:EB:3B:DE";
        Bluetooth bluetooth = Bluetooth.getInstance();
        try {
			bluetooth.init(deviceAddr);
		} catch (IOException e) {
			e.printStackTrace();
		}
        
        try {
            Log.d("BLUETOOTH", "Connected!");

            
            File dir = Environment.getExternalStorageDirectory();
//            File yourFile = new File(dir, "/DCIM/.thumbnails/1308403829869.jpg");
            File yourFile = new File(dir, "/download/IMG_4750.jpeg");
            bluetooth.sendPicture(yourFile);   
            
                        
        } catch (IOException ex) {
            Logger.getLogger(AndroidApplicationEliasActivity.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    

}