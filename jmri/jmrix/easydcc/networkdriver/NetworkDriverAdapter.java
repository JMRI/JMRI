// NetworkDriverAdapter.java

package jmri.jmrix.easydcc.networkdriver;

import jmri.jmrix.easydcc.EasyDccPortController;
import jmri.jmrix.easydcc.EasyDccProgrammer;
import jmri.jmrix.easydcc.EasyDccProgrammerManager;
import jmri.jmrix.easydcc.EasyDccTrafficController;

import java.io.*;
import java.net.*;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;

/**
 * Implements SerialPortAdapter for the EasyDcc system network connection.
 * <P>This connects
 * an EasyDcc command station via a telnet connection.
 * Normally controlled by the NetworkDriverFrame class.
 *
 * @author	Bob Jacobsen   Copyright (C) 2001, 2002, 2003
 * @version	$Revision: 1.1 $
 */
public class NetworkDriverAdapter extends EasyDccPortController {

    /**
     * set up all of the other objects to operate with an EasyDcc command
     * station connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        EasyDccTrafficController.instance().connectPort(this);

        jmri.InstanceManager.setProgrammerManager(
                new EasyDccProgrammerManager(
                    new EasyDccProgrammer()));

        jmri.InstanceManager.setPowerManager(new jmri.jmrix.easydcc.EasyDccPowerManager());

        jmri.InstanceManager.setTurnoutManager(new jmri.jmrix.easydcc.EasyDccTurnoutManager());

        // start operation
        // sourceThread = new Thread(p);
        // sourceThread.start();
        sinkThread = new Thread(EasyDccTrafficController.instance());
        sinkThread.start();

        jmri.jmrix.easydcc.ActiveFlag.setActive();
    }

    private Thread sinkThread;

    // base class methods for the EasyDccPortController interface
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
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
            log.error("error opening EasyDcc network connection: "+e);
        }
    }

    public DataOutputStream getOutputStream() {
        if (!opened) log.error("getOutputStream called before load(), stream not available");
        try {
            return new DataOutputStream(socket.getOutputStream());
        }
     	catch (java.io.IOException e) {
            log.error("getOutputStream exception: "+e);
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

    public Vector getPortNames() {
        log.error("Unexpected call to getPortNames");
        return null;
    }
    public String openPort(String portName, String appName)  {
        log.error("Unexpected call to openPort");
        return null;
    }
    public String[] validBaudRates() {
        log.error("Unexpected call to validBaudRates");
        return null;
    }

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(NetworkDriverAdapter.class.getName());

}
