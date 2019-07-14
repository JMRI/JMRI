package jmri.jmrit.withrottle;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.util.zeroconf.ZeroConfService;
import jmri.util.zeroconf.ZeroConfServiceEvent;
import jmri.util.zeroconf.ZeroConfServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied from UserInterface, but with the UI stuff removed. Sets up to
 * advertise service, and creates a thread for it to run in.
 * <p>
 * listen() has to run in a separate thread.
 *
 * @author Brett Hoffman Copyright (C) 2009, 2010
 * @author Paul Bender Copyright (C) 2018
 */
public class FacelessServer implements DeviceListener, DeviceManager, ZeroConfServiceListener {

    private final static Logger log = LoggerFactory.getLogger(FacelessServer.class);

    UserPreferencesManager userPreferences = InstanceManager.getNullableDefault(UserPreferencesManager.class);

// Server iVars
    int port;
    ZeroConfService service;
    boolean isListen = true;
    ServerSocket socket = null;
    final private ArrayList<DeviceServer> deviceList = new ArrayList<>();
    final private ArrayList<DeviceListener> deviceListenerList = new ArrayList<>();
    private int threadNumber = 1;

    FacelessServer() {
        createServerThread();
        setShutDownTask();
    } // End of constructor

    @Override
    public void listen() {
        int socketPort = InstanceManager.getDefault(WiThrottlePreferences.class).getPort();

        try { //Create socket on available port
            socket = new ServerSocket(socketPort);
        } catch (IOException e1) {
            log.error("New ServerSocket Failed during listen()");
            return;
        }

        port = socket.getLocalPort();
        log.debug("WiThrottle listening on TCP port: {}", port);

        service = ZeroConfService.create("_withrottle._tcp.local.", port);
        service.addEventListener(this);
        service.publish();

        addDeviceListener(this);

        while (isListen) { //Create DeviceServer threads
            DeviceServer device;
            try {
                log.info("Creating new WiThrottle DeviceServer(socket) on port {}, waiting for incoming connection...", port);
                device = new DeviceServer(socket.accept(), this);  //blocks here until a connection is made

                String threadName = "DeviceServer-" + threadNumber++;  // NOI18N
                Thread t = new Thread(device, threadName);
                for (DeviceListener dl : deviceListenerList) {
                    device.addDeviceListener(dl);
                }
                log.debug("Starting thread '{}'", threadName);  // NOI18N
                t.start();
            } catch (IOException e3) {
                if (isListen) {
                    log.error("Listen Failed on port {}", port);
                }
                return;
            }

        }

    }

    // package protected getters
    ZeroConfService getZeroConfService() {
        return service;
    }

    int getPort() {
        return port;
    }

    /**
     * Add a device listener that will be added for each new device connection
     *
     * @param dl the device listener to add
     */
    @Override
    public void addDeviceListener(DeviceListener dl) {
        if (!deviceListenerList.contains(dl)) {
            deviceListenerList.add(dl);
        }
    }

    /**
     * Remove a device listener from the list that will be added for each new
     * device connection
     *
     * @param dl the device listener to remove
     */
    @Override
    public void removeDeviceListener(DeviceListener dl) {
        if (deviceListenerList.contains(dl)) {
            deviceListenerList.remove(dl);
        }
    }

    @Override
    public void notifyDeviceConnected(DeviceServer device) {

        deviceList.add(device);
    }

    @Override
    public void notifyDeviceDisconnected(DeviceServer device) {
        if (deviceList.size() < 1) {
            return;
        }
        if (!deviceList.remove(device)) {
            return;
        }

        device.removeDeviceListener(this);
    }

//    public void notifyDeviceAddressChanged(DeviceServer device){
//    }
    /**
     * Received an UDID, filter out any duplicate.
     * <p>
     * @param device the device to filter for duplicates
     */
    @Override
    public void notifyDeviceInfoChanged(DeviceServer device) {

        //  Filter duplicate connections
        if ((device.getUDID() != null)) {
            for (DeviceServer listDevice : deviceList) {
                if (device != listDevice && device.getUDID().equals(listDevice.getUDID())) {
                    //  If in here, array contains duplicate of a device
                    log.debug("Has duplicate of device '{}', clearing old one.", listDevice.getUDID());
                    listDevice.closeThrottles();
                    break;
                }
            }
        }
    }

    public ArrayList<DeviceServer> getDeviceList() {
        return deviceList;
    }

    @Override
    public void notifyDeviceAddressChanged(DeviceServer device) {
        // TODO Auto-generated method stub

    }

    private String rosterGroup = null;

    @Override
    public void setSelectedRosterGroup(String group) {
        rosterGroup = group;
    }

    @Override
    public String getSelectedRosterGroup() {
        return rosterGroup;
    }

    @Override
    public void serviceQueued(ZeroConfServiceEvent se) {
    }

    @Override
    public void servicePublished(ZeroConfServiceEvent se) {
        try {
            log.info("Published ZeroConf service for '{}' on {}:{}", se.getService().getKey(), se.getAddress().getHostAddress(), port); // NOI18N
        } catch (NullPointerException ex) {
            log.error("NPE in FacelessServer.servicePublished(): {}", ex.getLocalizedMessage());
        }
    }

    // package protected method to disable the server.
    void disableServer() {
        isListen = false;
        stopDevices();
        try {
            socket.close();
            log.debug("closed socket in ServerThread");
            service.stop();
        } catch (NullPointerException ex) {
            log.debug("NPE while attempting to close socket, ignored");
        } catch (IOException ex) {
            log.error("socket in ServerThread won't close");
        }
    }

    // Clear out the deviceList array and close each device thread
    private void stopDevices() {
        DeviceServer device;
        int cnt = 0;
        if (deviceList.size() > 0) {
            do {
                device = deviceList.get(0);
                if (device != null) {
                    device.closeThrottles(); //Tell device to stop its throttles,
                    device.closeSocket();   //close its sockets
                    //close() will throw read error and it will be caught
                    //and drop the thread.
                    cnt++;
                    if (cnt > 200) {
                        break;
                    }
                }
            } while (!deviceList.isEmpty());
        }
        deviceList.clear();
    }

    private jmri.implementation.AbstractShutDownTask task = null;

    private void setShutDownTask() {
        task = new jmri.implementation.AbstractShutDownTask("WiThrottle Server ShutdownTask") {
            @Override
            public boolean execute() {
                disableServer();
                return true;
            }
        };
        jmri.InstanceManager.getDefault(jmri.ShutDownManager.class).register(task);
    }

    @Override
    public void serviceUnpublished(ZeroConfServiceEvent se) {
    }

}
