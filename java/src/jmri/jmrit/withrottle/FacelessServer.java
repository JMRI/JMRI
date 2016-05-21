
package jmri.jmrit.withrottle;


/**
 *	FacelessServer.java
 *	Copied from UserInterface, but with the UI stuff removed.  Sets up to advertise service, and creates a thread for it to run in.
 *
 *	@author Brett Hoffman   Copyright (C) 2009, 2010
 *	@version $Revision: 20499 $
 */


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.util.ResourceBundle;
import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.UserPreferencesManager;
import jmri.util.zeroconf.ZeroConfService;


//	listen() has to run in a separate thread.
public class FacelessServer implements DeviceListener, DeviceManager {

    static Logger log = LoggerFactory.getLogger(FacelessServer.class.getName());
    static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.withrottle.WiThrottleBundle");

    UserPreferencesManager userPreferences = InstanceManager.getDefault(UserPreferencesManager.class);

//	Server iVars
    int port;
    ZeroConfService service;
    boolean isListen = true;
    ServerSocket socket = null;
    ArrayList<DeviceServer> deviceList;


    FacelessServer(){
        if (deviceList == null) deviceList = new ArrayList<DeviceServer>(1);
        createServerThread();
    }	//	End of constructor


    public void createServerThread(){
        FacelessThread s = new FacelessThread(this);
        s.start();
    }


    public void listen(){
        int socketPort = 0;
        if (WiThrottleManager.withrottlePreferencesInstance().isUseFixedPort()){
            socketPort = Integer.parseInt(WiThrottleManager.withrottlePreferencesInstance().getPort());
        }

        try{	//Create socket on available port
            socket = new ServerSocket(socketPort);
        } catch(IOException e1){
            log.error("New ServerSocket Failed during listen()");
            return;
        }

        port = socket.getLocalPort();
        if(log.isDebugEnabled()) log.debug("WiThrottle listening on TCP port: " + port);

        service = ZeroConfService.create("_withrottle._tcp.local.", port);
        service.publish();

        if (service.isPublished()) {
        	String addressText = "";
            //show the ip addresses found on this system in the log
            try {
            	for (Inet4Address addr : service.serviceInfo().getInet4Addresses()) {
            		if (addr != null && !addr.isLoopbackAddress()) {
            			addressText += addr.getHostAddress() + " ";
            		}
            	}
            	log.info("WiThrottle found these local addresses: " + addressText);
            } catch (Exception except) {
            	log.error("Failed to determine this system's IP address: " + except.toString());
            }
        } else {
            log.error("WiThrottle did not start ZeroConf (JmDNS)");
        }
            
        while (isListen){ //Create DeviceServer threads
            DeviceServer device;
            try{
                log.info("Creating new WiThrottle DeviceServer(socket) on port " + port + ", waiting for incoming connection...");
                device = new DeviceServer(socket.accept(), this);  //blocks here until a connection is made

                Thread t = new Thread(device);
                device.addDeviceListener(this);
                log.debug("Starting DeviceListener thread");
                t.start();
            } catch (IOException e3){
                if (isListen)log.error("Listen Failed on port " + port);
                return;
            }

        }


    }


    public void notifyDeviceConnected(DeviceServer device){

        deviceList.add(device);
    }

    public void notifyDeviceDisconnected(DeviceServer device){
        if (deviceList.size()<1) return;
        if (!deviceList.remove(device)) return;

        device.removeDeviceListener(this);
    }

//    public void notifyDeviceAddressChanged(DeviceServer device){
//    }
/**
 * Received an UDID, filter out any duplicate.
 * @param device
 */
    public void notifyDeviceInfoChanged(DeviceServer device){

        //  Filter duplicate connections
        if ((device.getUDID() != null) && (deviceList.size() > 0)){
            for (int i = 0;i < deviceList.size();i++){
                DeviceServer listDevice = deviceList.get(i);
                if ((device != listDevice) && (listDevice.getUDID() != null) && (listDevice.getUDID().equals(device.getUDID()))){
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
    
}

//  listen() has to run in a separate thread.
class FacelessThread extends Thread {
    FacelessServer fs;

    FacelessThread(FacelessServer _fs){
        fs = _fs;
    }

    @Override
    public void run() {
        fs.listen();
        log.debug("Leaving ThreadNoUI.run()");
    }

    static Logger log = LoggerFactory.getLogger(FacelessThread.class.getName());
}

 	  	 

 	  	 
