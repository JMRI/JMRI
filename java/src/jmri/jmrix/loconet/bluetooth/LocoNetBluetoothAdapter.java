package jmri.jmrix.loconet.bluetooth;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Vector;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;
import javax.bluetooth.UUID;
import javax.microedition.io.Connection;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LnPortController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to LocoNet via a LocoNet Bluetooth adapter.
 */
public class LocoNetBluetoothAdapter extends LnPortController {

    public LocoNetBluetoothAdapter() {
        this(new LocoNetSystemConnectionMemo());
    }

    public LocoNetBluetoothAdapter(LocoNetSystemConnectionMemo adapterMemo) {
        super(adapterMemo);
        option1Name = "CommandStation"; // NOI18N
        option2Name = "TurnoutHandle"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("CommandStationTypeLabel"), commandStationNames, false));
        options.put(option2Name, new Option(Bundle.getMessage("TurnoutHandling"),
                new String[]{Bundle.getMessage("HandleNormal"), Bundle.getMessage("HandleSpread"), Bundle.getMessage("HandleOneOnly"), Bundle.getMessage("HandleBoth")})); // I18N
    }

    Vector<String> portNameVector = null;

    @Override
    public Vector<String> getPortNames() {
        portNameVector = new Vector<>();
        try {
            RemoteDevice[] devices = LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
            if (devices != null) {
                for (RemoteDevice device : devices) {
                    portNameVector.add(device.getFriendlyName(false));
                }
            }
        } catch (IOException ex) {
            log.error("Unable to use bluetooth device", ex);
        }
        return portNameVector;
    }

    @Override
    public String openPort(String portName, String appName) {
        int[] responseCode = new int[]{-1};
        Exception[] exception = new Exception[]{null};
        try {
            // Find the RemoteDevice with this name.
            RemoteDevice[] devices = LocalDevice.getLocalDevice().getDiscoveryAgent().retrieveDevices(DiscoveryAgent.PREKNOWN);
            if (devices != null) {
                for (RemoteDevice device : devices) {
                    if (device.getFriendlyName(false).equals(portName)) {
                        Object[] waitObj = new Object[0];
                        // Start a search for a serialport service (UUID 0x1101)
                        LocalDevice.getLocalDevice().getDiscoveryAgent().searchServices(new int[]{0x0100}, new UUID[]{new UUID(0x1101)}, device, new DiscoveryListener() {
                            @Override
                            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
                                synchronized (waitObj) {
                                    for (ServiceRecord service : servRecord) {
                                        // Service found, get url for connection.
                                        String url = service.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
                                        if (url == null) {
                                            continue;
                                        }
                                        try {
                                            // Open connection.
                                            Connection conn = Connector.open(url, Connector.READ_WRITE);
                                            if (conn instanceof StreamConnection) { // The connection should be a StreamConnection, otherwise it's a one way communication.
                                                StreamConnection stream = (StreamConnection) conn;
                                                in = stream.openInputStream();
                                                out = stream.openOutputStream();
                                                opened = true;
                                                // Port is open, let openPort continue.
                                                //waitObj.notify();
                                            } else {
                                                throw new IOException("Could not establish a two-way communication");
                                            }
                                        } catch (IOException IOe) {
                                            exception[0] = IOe;
                                        }
                                    }
                                    if (!opened) {
                                        exception[0] = new IOException("No service found to connect to");
                                    }
                                }
                            }

                            @Override
                            public void serviceSearchCompleted(int transID, int respCode) {
                                synchronized (waitObj) {
                                    // Search for services complete, if the port was not opened, save the response code for error analysis.
                                    responseCode[0] = respCode;
                                    // Search completer, let openPort continue.
                                    waitObj.notify();
                                }
                            }

                            @Override
                            public void inquiryCompleted(int discType) {
                            }

                            @Override
                            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                            }
                        });
                        synchronized (waitObj) {
                            // Wait until either the port is open on the search has returned a response code.
                            while (!opened && responseCode[0] == -1) {
                                try {
                                    // Wait for search to complete.
                                    waitObj.wait();
                                } catch (InterruptedException ex) {
                                    log.error("Thread unexpectedly interrupted", ex);
                                }
                            }
                        }
                        break;
                    }
                }
            }
        } catch (BluetoothStateException BSe) {
            log.error("Exception when using bluetooth");
            return BSe.getLocalizedMessage();
        } catch (IOException IOe) {
            log.error("Unknown IOException when establishing connection to " + portName);
            return IOe.getLocalizedMessage();
        }

        if (!opened) {
            ConnectionStatus.instance().setConnectionState(null, portName, ConnectionStatus.CONNECTION_DOWN);
            if (exception[0] != null) {
                log.error("Exception when connecting to " + portName);
                return exception[0].getLocalizedMessage();
            }
            switch (responseCode[0]) {
                case DiscoveryListener.SERVICE_SEARCH_COMPLETED:
                    log.error("Bluetooth connection {} not opened, unknown error", portName);
                    return "Unknown error: failed to connect to " + portName;
                case DiscoveryListener.SERVICE_SEARCH_DEVICE_NOT_REACHABLE:
                    log.error("Bluetooth device {} could not be reached", portName);
                    return "Could not find " + portName;
                case DiscoveryListener.SERVICE_SEARCH_ERROR:
                    log.error("Error when searching for {}", portName);
                    return "Error when searching for " + portName;
                case DiscoveryListener.SERVICE_SEARCH_NO_RECORDS:
                    log.error("No serial service found on {}", portName);
                    return "Invalid bluetooth device: " + portName;
                case DiscoveryListener.SERVICE_SEARCH_TERMINATED:
                    log.error("Service search on {} ended prematurely", portName);
                    return "Search for " + portName + " ended unexpectedly";
                default:
                    log.warn("Unhandled response code: {}", responseCode[0]);
                    break;
            }
            log.error("Unknown error when connecting to {}", portName);
            return "Unknown error when connecting to " + portName;
        }

        return null; // normal operation
    }

    /**
     * Set up all of the other objects to operate.
     */
    @Override
    public void configure() {
        setCommandStationType(getOptionState(option1Name));
        setTurnoutHandling(getOptionState(option2Name));
        // connect to a packetizing traffic controller
        LnPacketizer packets = new LnPacketizer(this.getSystemConnectionMemo());
        packets.connectPort(this);

        // create memo
        this.getSystemConnectionMemo().setLnTrafficController(packets);
        // do the common manager config

        this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                mTurnoutNoRetry, mTurnoutExtraSpace, mTranspondingAvailable);
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
    }

    // base class methods for the LnPortController interface
    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(in);
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        return new DataOutputStream(out);
    }

    @Override
    public boolean status() {
        return opened;
    }

    // private control members
    private boolean opened = false;
    private InputStream in = null;
    private OutputStream out = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{};
    }

    private final static Logger log = LoggerFactory.getLogger(LocoNetBluetoothAdapter.class);

}
