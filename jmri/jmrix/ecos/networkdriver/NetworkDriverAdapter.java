// NetworkDriverAdapter.java

package jmri.jmrix.ecos.networkdriver;

import jmri.jmrix.ConnectionStatus;
import jmri.jmrix.ecos.*;

import java.io.*;
import java.net.*;
import java.util.Vector;

/**
 * Implements SerialPortAdapter for the ECOS system network connection.
 * <P>This connects
 * an ECOS command station via a telnet connection.
 * Normally controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2003, 2008
 * @version	$Revision: 1.6 $
 */
public class NetworkDriverAdapter extends EcosPortController {

    /**
     * set up all of the other objects to operate with an ECOS command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        EcosTrafficController.instance().connectPort(this);

/*         jmri.InstanceManager.setProgrammerManager( */
/*                 new NceProgrammerManager( */
/*                     new NceProgrammer())); */

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.ecos.EcosPowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.ecos.EcosTurnoutManager());

        jmri.InstanceManager.store(
                new jmri.jmrix.ecos.EcosLocoAddressManager(),
                jmri.jmrix.ecos.EcosLocoAddressManager.class);

        /*jmri.InstanceManager.store(
                new jmri.jmrix.ecos.EcosPreferences(),
                jmri.jmrix.ecos.EcosPreferences.class);*/


        jmri.InstanceManager.setThrottleManager(new jmri.jmrix.ecos.EcosDccThrottleManager());

        jmri.InstanceManager.setSensorManager(new jmri.managers.InternalSensorManager());

        jmri.InstanceManager.setSensorManager(new jmri.jmrix.ecos.EcosSensorManager());

        jmri.jmrix.ecos.ActiveFlag.setActive();

    }

    // base class methods for the EcosPortController interface
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            ConnectionStatus.instance().setConnectionState(
            		hostName, ConnectionStatus.CONNECTION_DOWN);
        }
        try {
            return new DataInputStream(socket.getInputStream());
        } catch (java.io.IOException ex1) {
            log.error("Exception getting input stream: "+ex1);
            return null;
        }
    }

    public void connect(String host, int port) {
        try {
            socket = new Socket(host, port);
            opened = true;
        } catch (Exception e) {
            log.error("error opening ECOS network connection: "+e);
            ConnectionStatus.instance().setConnectionState(
            		hostName, ConnectionStatus.CONNECTION_DOWN);
        }
    }

    public DataOutputStream getOutputStream() {
        if (!opened) log.error("getOutputStream called before load(), stream not available");
        try {
            return new DataOutputStream(socket.getOutputStream());
        }
     	catch (java.io.IOException e) {
            log.error("getOutputStream exception: "+e);
            ConnectionStatus.instance().setConnectionState(
            		hostName, ConnectionStatus.CONNECTION_DOWN);
     	}
     	return null;
    }

    public boolean status() {return opened;}

    // private control members
    private boolean opened = false;

    static public NetworkDriverAdapter instance() {
        if (mInstance == null) mInstance = new NetworkDriverAdapter();
        return mInstance;
    }
    static NetworkDriverAdapter mInstance = null;

    Socket socket;
    
    String hostName = null;
    public void setHostName (String hostName){
    	this.hostName = hostName;
        if (this.hostName.equals("")) this.hostName = "(none)";
     }
    
    Vector<String> portNameVector = null;
    public Vector<String> getPortNames() {
    	portNameVector = new Vector<String>();
    	portNameVector.addElement(hostName);
        return portNameVector;
    }
    public String openPort(String portName, String appName)  {
        log.error("Unexpected call to openPort");
        return null;
    }
    public String[] validBaudRates() {
        log.error("Unexpected call to validBaudRates");
        return null;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(NetworkDriverAdapter.class.getName());

}
