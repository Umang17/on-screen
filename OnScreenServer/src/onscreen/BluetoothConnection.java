/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package onscreen;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

/**
 *
 * @author Mattias
 */
public class BluetoothConnection {

    private LocalDevice localDevice; // local Bluetooth Manager
    private DiscoveryAgent discoveryAgent; // discovery agent
    // Bluetooth Service name
    private static final String myServiceName = "MyBtService";
    // Bluetooth Service UUID of interest
    private static final String myServiceUUID = "2d26618601fb47c28d9f10b8ec891363";
    private UUID MYSERVICEUUID_UUID = new UUID(myServiceUUID, false);

    /**
     * Inits the system bluetooth
     * @throws BluetoothStateException If bluetooth does not exists
     */
    public void btInit() throws BluetoothStateException {
        localDevice = null;
        discoveryAgent = null;

        // Retrieve the local device to get to the Bluetooth Manager
        localDevice = LocalDevice.getLocalDevice();

        // Servers set the discoverable mode to GIAC
        localDevice.setDiscoverable(DiscoveryAgent.GIAC);

        // Clients retrieve the discovery agent
        discoveryAgent = localDevice.getDiscoveryAgent();
    }

    /**
     * Get the local bluetooth address
     * @return local address
     */
    public String getLocalAddress() {
        return localDevice.getBluetoothAddress();
    }
    
    public void destroy() throws Throwable {
        // Restore so device is no longer discoverable when we close
        // the application.
        localDevice.setDiscoverable(DiscoveryAgent.NOT_DISCOVERABLE);
    }

    StreamConnection listen() throws BluetoothStateException {
                // Define the server connection URL
        String connURL = 
                "btspp://localhost:" + 
                MYSERVICEUUID_UUID.toString() 
                + ";encrypt=false;authenticate=false"
                + ";name=" + myServiceName;
        
        try {
            StreamConnectionNotifier service =
                    (StreamConnectionNotifier) Connector.open(connURL);
            ServiceRecord sr = localDevice.getRecord(service);
            return (StreamConnection) service.acceptAndOpen();
            
        } catch (IOException ex) {
            throw new BluetoothStateException("Error " + ex.getLocalizedMessage() + ex.toString());
        }
    }
}
