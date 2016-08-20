package jmri.jmrit.withrottle;

import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.util.zeroconf.ZeroConfService;
import jmri.util.zeroconf.ZeroConfServiceEvent;
import jmri.util.zeroconf.ZeroConfServiceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Copied from UserInterface, but with the UI stuff removed.
 * Sets up to advertise service, and creates a thread for it to run in.
 *
 *	listen() has to run in a separate thread.
 *
 * @author Brett Hoffman Copyright (C) 2009, 2010
 */
public class FacelessServer implements DeviceListener, DeviceManager, ZeroConfServiceListener {

    private final static Logger log = LoggerFactory.getLogger(FacelessServer.class.getName());
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");

    UserPreferencesManager userPreferences = InstanceManager.getOptionalDefault(UserPreferencesManager.class);

//	Server iVars
    int port;
    ZeroConfService service;
    boolean isListen = true;
    ServerSocket socket = null;
    ArrayList<DeviceServer> deviceList;

    FacelessServer() {
        if (deviceList == null) {
            deviceList = new ArrayList<DeviceServer>(1);
        }
        createServerThread();
    }	//	End of constructor

    public void createServerThread() {
        FacelessThread s = new FacelessThread(this);
        s.setName("WiThrottleFacelessServer"); // NOI18N
        s.start();
    }

    public void listen() {
        int socketPort = WiThrottleManager.withrottlePreferencesInstance().getPort();

        try {	//Create socket on available port
            socket = new ServerSocket(socketPort);
        } catch (IOException e1) {
            log.error("New ServerSocket Failed during listen()");
            return;
        }

        port = socket.getLocalPort();
        if (log.isDebugEnabled()) {
            log.debug("WiThrottle listening on TCP port: " + port);
        }

        service = ZeroConfService.create("_withrottle._tcp.local.", port);
        service.addEventListener(this);
        service.publish();

        while (isListen) { //Create DeviceServer threads
            DeviceServer device;
            try {
                log.info("Creating new WiThrottle DeviceServer(socket) on port " + port + ", waiting for incoming connection...");
                device = new DeviceServer(socket.accept(), this);  //blocks here until a connection is made

                Thread t = new Thread(device);
                device.addDeviceListener(this);
                log.debug("Starting DeviceListener thread");
                t.start();
            } catch (IOException e3) {
                if (isListen) {
                    log.error("Listen Failed on port " + port);
                }
                return;
            }

        }

    }

    public void notifyDeviceConnected(DeviceServer device) {

        deviceList.add(device);
    }

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
     *
     */
    public void notifyDeviceInfoChanged(DeviceServer device) {

        //  Filter duplicate connections
        if ((device.getUDID() != null) && (deviceList.size() > 0)) {
            for (int i = 0; i < deviceList.size(); i++) {
                DeviceServer listDevice = deviceList.get(i);
                if ((device != listDevice) && (listDevice.getUDID() != null) && (listDevice.getUDID().equals(device.getUDID()))) {
                    //  If in here, array contains duplicate of a device
                    log.debug("Has duplicate of device, clearing old one.");
                    listDevice.closeThrottles();
                    break;
                }
            }
        }
    }

    public String getSelectedRosterGroup() {
//        return rosterGroupSelector.getSelectedRosterGroup();
        return null;
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addPropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removePropertyChangeListener(String propertyName,
            PropertyChangeListener listener) {
        // TODO Auto-generated method stub

    }

    @Override
    public void notifyDeviceAddressChanged(DeviceServer device) {
        // TODO Auto-generated method stub

    }

    @Override
    public void serviceQueued(ZeroConfServiceEvent se) {
    }

    @Override
    public void servicePublished(ZeroConfServiceEvent se) {
        try {
            InetAddress addr = se.getDNS().getInetAddress();
            // most addresses are Inet6Address objects, 
            if (addr instanceof Inet4Address && !addr.isLoopbackAddress()) {
                log.info("Published ZeroConf service for '{}' on {}:{}", se.getService().key(), addr.getHostAddress(), port); // NOI18N
            }
        } catch (NullPointerException ex) {
            log.error("NPE in FacelessServer.servicePublished(): {}", ex.getLocalizedMessage());
        } catch (IOException ex) {
            log.error("IOException in FacelessServer.servicePublished(): {}", ex.getLocalizedMessage());
        }
    }

    @Override
    public void serviceUnpublished(ZeroConfServiceEvent se) {
    }

    //  listen() has to run in a separate thread.
    static class FacelessThread extends Thread {

        FacelessServer fs;

        FacelessThread(FacelessServer _fs) {
            fs = _fs;
        }

        @Override
        public void run() {
            fs.listen();
            log.debug("Leaving ThreadNoUI.run()");
        }

    private final static Logger log = LoggerFactory.getLogger(FacelessThread.class.getName());
    }
}
