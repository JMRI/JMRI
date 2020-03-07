package jmri.jmrix.dcc4pc.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import jmri.jmrix.SystemConnectionMemo;
import jmri.jmrix.dcc4pc.Dcc4PcConnectionTypeList;
import jmri.jmrix.dcc4pc.Dcc4PcPortController;
import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.jmrix.dcc4pc.Dcc4PcTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for the Dcc4Pc system.
 * <p>
 * This connects an Dcc4Pc command station via a serial com port.
 *
 * @author Kevin Dickerson Copyright (C) 2012
 */
public class SerialDriverAdapter extends Dcc4PcPortController {

    public SerialDriverAdapter() {
        super(new Dcc4PcSystemConnectionMemo());
        option1Name = "Programmer"; // NOI18N
        options.put(option1Name, new Option("Programmer : ", validOption1()));
        setManufacturer(Dcc4PcConnectionTypeList.DCC4PC);
    }

    SerialPort activeSerialPort = null;

    @Override
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
                configureLeadsAndFlowControl(activeSerialPort, SerialPort.FLOWCONTROL_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
            }
            log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                    + " " + activeSerialPort.isReceiveTimeoutEnabled());

            serialStream = activeSerialPort.getInputStream();
            // purge contents, if any
            purgeStream(serialStream);

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

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException ex) {
            log.error("Unexpected exception while opening port {}", portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;
        }
        return null; // indicates OK return
    }

    public void setHandshake(int mode) {
        try {
            activeSerialPort.setFlowControlMode(mode);
        } catch (UnsupportedCommOperationException ex) {
            log.error("Unexpected exception while setting COM port handshake mode", ex);
        }
    }

    public SerialPort getSerialPort() {
        return activeSerialPort;
    }

    /**
     * Option 1 controls the connection used for programming
     */
    public String[] validOption1() {
        List<SystemConnectionMemo> connList = jmri.InstanceManager.getList(SystemConnectionMemo.class);
        if (!connList.isEmpty()) {
            ArrayList<String> progConn = new ArrayList<>();
            progConn.add("");
            String userName = "Dcc4Pc";
            if (this.getSystemConnectionMemo() != null) {
                userName = this.getSystemConnectionMemo().getUserName();
            }
            for (int i = 0; i < connList.size(); i++) {
                SystemConnectionMemo scm = connList.get(i);
                if ((scm.provides(jmri.AddressedProgrammerManager.class) || scm.provides(jmri.GlobalProgrammerManager.class))
                        && (!scm.getUserName().equals(userName))) {
                    progConn.add(scm.getUserName());
                }
            }
            return progConn.toArray(new String[progConn.size()]);
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
    @Override
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
    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");
            return null;
        }
        return new DataInputStream(serialStream);
    }

    @Override
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
     * {@inheritDoc}
     *
     * Currently only 115,200 bps
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"115,200 bps"};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{115200};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    InputStream serialStream = null;

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SerialDriverAdapter instance() {
        if (mInstance == null) {
            SerialDriverAdapter m = new SerialDriverAdapter();
            m.setManufacturer(Dcc4PcConnectionTypeList.DCC4PC);
            mInstance = m;
        }
        return mInstance;
    }

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static volatile SerialDriverAdapter mInstance = null;

    /**
     * set up all of the other objects to operate with an Dcc4Pc command station
     * connected to this port
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        Dcc4PcTrafficController control = new Dcc4PcTrafficController();
        this.getSystemConnectionMemo().setDcc4PcTrafficController(control);
        this.getSystemConnectionMemo().setDefaultProgrammer(getOptionState(option1Name));
        control.connectPort(this);
        this.getSystemConnectionMemo().configureManagers();

    }

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}
