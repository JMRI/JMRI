// LnTcpDriverAdapter.java

package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.loconet.*;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;
import java.util.Vector;

/**
 * Implements SerialPortAdapter for the LocoNetOverTcp system network connection.
 * <P>This connects
 * a Loconet via a telnet connection.
 * Normally controlled by the LnTcpDriverFrame class.
 *
 * @author      Bob Jacobsen   Copyright (C) 2001, 2002, 2003
 * @author      Alex Shepherd Copyright (C) 2003, 2006
 * @version     $Revision: 1.20 $
 */

public class LnTcpDriverAdapter extends LnPortController {

    public LnTcpDriverAdapter() {
        adaptermemo = new LocoNetSystemConnectionMemo();
    }
    /**
     * set up all of the other objects to operate with a LocoNet
     * connected via this class.
     */
    public void configure() {
        // connect to a packetizing traffic controller
        LnOverTcpPacketizer packets = new LnOverTcpPacketizer();
        packets.connectPort(this);

        // create memo
        adaptermemo.setSlotManager(new SlotManager(packets));
        adaptermemo.setLnTrafficController(packets);        
        // do the common manager config
        adaptermemo.configureCommandStation(mCanRead, mProgPowersOff, commandStationName);
        adaptermemo.configureManagers();

        // start operation
        packets.startThreads();
        jmri.jmrix.loconet.ActiveFlag.setActive();

    }

    // base class methods for the LnPortController interface
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
    private Socket socket = null;

    public void connect(String host, int port) throws Exception{
        try {
            socket = new Socket(host, port);
            opened = true;
        } catch (Exception e) {
            log.error("error opening LocoNetOverTcp network connection: "+e);
            throw e;
        }
    }

    static private LnTcpDriverAdapter mInstance = null;
    static public synchronized LnTcpDriverAdapter instance() {
        if (mInstance == null){
            mInstance = new LnTcpDriverAdapter();
        }
        return mInstance;
    }

    public Vector<String> getPortNames() {
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

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] getCommandStationNames() { return commandStationNames; }
    public String   getCurrentCommandStation() { return commandStationName; }
    
    public SystemConnectionMemo getSystemConnectionMemo() { return adaptermemo; }
    
    public void dispose(){
        if (adaptermemo!=null)
            adaptermemo.dispose();
        adaptermemo = null;
    }

    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(LnTcpDriverAdapter.class.getName());

}
