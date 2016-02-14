// SerialDriverAdapter.java
package jmri.jmrix.dcc4pc.serialdriver;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.dcc4pc.Dcc4PcPortController;
import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.jmrix.dcc4pc.Dcc4PcTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the Dcc4Pc system.
 * <P>
 * This connects an Dcc4Pc command station via a serial com port.
 * <P>
 *
 * @author	Kevin Dickerson Copyright (C) 2012
 * @version	$Revision: 18133 $
 */
public class SerialDriverAdapter extends Dcc4PcPortController implements jmri.jmrix.SerialPortAdapter {

    public SerialDriverAdapter() {
        super(new Dcc4PcSystemConnectionMemo());
        option1Name = "Programmer";
        options.put(option1Name, new Option("Programmer : ", validOption1()));
        setManufacturer(jmri.jmrix.DCCManufacturerList.DCC4PC);
    }

    SerialPort activeSerialPort = null;

    public String openPort(String portName, String appName) {

        try {
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }
            try {
                activeSerialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
                activeSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
            } catch (gnu.io.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
            }
            log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                    + " " + activeSerialPort.isReceiveTimeoutEnabled());

            // purge contents, if any
            serialStream = activeSerialPort.getInputStream();
            int count = serialStream.available();
            log.debug("input stream shows " + count + " bytes available");
            while (count > 0) {
                serialStream.skip(count);
                count = serialStream.available();
            }

            // report status?
            if (log.isInfoEnabled()) {
                log.info(portName + " port opened at "
                        + activeSerialPort.getBaudRate() + " baud, sees "
                        + " DTR: " + activeSerialPort.isDTR()
                        + " RTS: " + activeSerialPort.isRTS()
                        + " DSR: " + activeSerialPort.isDSR()
                        + " CTS: " + activeSerialPort.isCTS()
                        + "  CD: " + activeSerialPort.isCD()
                );
            }

            opened = true;

        } catch (gnu.io.NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (Exception ex) {
            log.error("Unexpected exception while opening port " + portName + " trace follows: " + ex);
            ex.printStackTrace();
            return "Unexpected error while opening port " + portName + ": " + ex;
        }
        return null; // indicates OK return
    }

    public void setHandshake(int mode) {
        try {
            activeSerialPort.setFlowControlMode(mode);
        } catch (Exception ex) {
            log.error("Unexpected exception while setting COM port handshake mode trace follows: " + ex);
            ex.printStackTrace();
        }
    }

    public SerialPort getSerialPort() {
        return activeSerialPort;
    }

    /**
     * Option 1 controls the connection used for programming
     */
    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value = "EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] validOption1() {
        List<SystemConnectionMemo> connList = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        if (connList != null) {
            ArrayList<String> progConn = new ArrayList<String>();
            progConn.add("");
            String userName = "Dcc4Pc";
            if (this.getSystemConnectionMemo() != null) {
                userName = this.getSystemConnectionMemo().getUserName();
            }
            for (int i = 0; i < connList.size(); i++) {
                SystemConnectionMemo scm = connList.get(i);
                if (scm.provides(jmri.ProgrammerManager.class) && (!scm.getUserName().equals(userName))) {
                    progConn.add(scm.getUserName());
                }
            }
            String[] validOption1 = new String[progConn.size()];
            progConn.toArray(validOption1);
            return validOption1;
        } else {
            return new String[]{""};
        }
    }

    /**
     * Get a String that says what Option 2 represents May be an empty string,
     * but will not be null
     */
    public String option2Name() {
        return "Match Detected Locos to Roster: ";
    }

    /**
     * Set the second port option. Only to be used after construction, but
     * before the openPort call
     */
    public void configureOption2(String value) {
        super.configureOption2(value);
        log.debug("configureOption2: " + value);
        //Not yet implemented
        /*boolean enable = true;
         if(value.equals("No"))
         enable = false;
         adaptermemo.getRailCommManager().setRosterEntryDiscoveryAllowed(enable);*/
    }

    // base class methods for the Dcc4PcPortController interface
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialStream);
    }

    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");
        }
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        } catch (java.io.IOException e) {
            log.error("getOutputStream exception: " + e);
        }
        return null;
    }

    /**
     * Get an array of valid baud rates. This is currently only 19,200 bps
     */
    public String[] validBaudRates() {
        return new String[]{"115,200 bps"};
    }

    InputStream serialStream = null;

    static public SerialDriverAdapter instance() {
        if (mInstance == null) {
            SerialDriverAdapter m = new SerialDriverAdapter();
            m.setManufacturer(jmri.jmrix.DCCManufacturerList.DCC4PC);
            mInstance = m;
        }
        return mInstance;
    }

    static volatile SerialDriverAdapter mInstance = null;

    /**
     * set up all of the other objects to operate with an Dcc4Pc command station
     * connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        Dcc4PcTrafficController control = new Dcc4PcTrafficController();
        this.getSystemConnectionMemo().setDcc4PcTrafficController(control);
        this.getSystemConnectionMemo().setDefaultProgrammer(getOptionState(option1Name));
        control.connectPort(this);
        this.getSystemConnectionMemo().configureManagers();

        jmri.jmrix.dcc4pc.ActiveFlag.setActive();

    }

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class.getName());

}
