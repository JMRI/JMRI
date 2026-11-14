package jmri.jmrix.dcc4pc.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import jmri.SystemConnectionMemo;
import jmri.jmrix.dcc4pc.Dcc4PcConnectionTypeList;
import jmri.jmrix.dcc4pc.Dcc4PcPortController;
import jmri.jmrix.dcc4pc.Dcc4PcSystemConnectionMemo;
import jmri.jmrix.dcc4pc.Dcc4PcTrafficController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.purejavacomm.CommPortIdentifier;
import jmri.jmrix.purejavacomm.NoSuchPortException;
import jmri.jmrix.purejavacomm.PortInUseException;
import jmri.jmrix.purejavacomm.UnsupportedCommOperationException;

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

    jmri.jmrix.purejavacomm.SerialPort activeSerialPort = null;

    @Override
    public String openPort(String portName, String appName) {

        try {
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = portID.open(getSystemConnectionMemo(), 2000);
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }
            try {
                activeSerialPort.setSerialPortParams(115200,
                        jmri.jmrix.purejavacomm.SerialPort.DATABITS_8,
                        jmri.jmrix.purejavacomm.SerialPort.STOPBITS_1,
                        jmri.jmrix.purejavacomm.SerialPort.PARITY_NONE);
                configureLeadsAndFlowControl(activeSerialPort,
                        jmri.jmrix.purejavacomm.SerialPort.FLOWCONTROL_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
            }
            log.debug("Serial timeout was observed as: {} {}", activeSerialPort.getReceiveTimeout(), activeSerialPort.isReceiveTimeoutEnabled());

            serialStream = activeSerialPort.getInputStream();
            // purge contents, if any
            purgeStream(serialStream);

            // report status?
            if (log.isInfoEnabled()) {
                log.info("{} port opened at {} baud, sees  DTR: {} RTS: {} DSR: {} CTS: {}  CD: {}", portName, activeSerialPort.getBaudRate(), activeSerialPort.isDTR(), activeSerialPort.isRTS(), activeSerialPort.isDSR(), activeSerialPort.isCTS(), activeSerialPort.isCD());
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

    public jmri.jmrix.purejavacomm.SerialPort getSerialPort() {
        return activeSerialPort;
    }

    /**
     * Close the port after it has failed, so it can be opened again with
     * {@link #openPort}.
     */
    public void closeSerialPort() {
        opened = false;
        jmri.jmrix.purejavacomm.SerialPort p = activeSerialPort;
        if (p != null) {
            try {
                p.close();
            } catch (RuntimeException e) {
                log.debug("Closing the failed port", e);
            }
        }
    }

    /**
     * @return true if the operating system lists this connection's port. A
     *         USB interface's port disappears while it is unplugged.
     */
    public boolean isPortPresent() {
        return getActualPortNames().contains(getCurrentPortName());
    }

    /**
     * Option 1 controls the connection used for programming.
     * @return array with options for programming.
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
     * {@inheritDoc}
     * <p>
     * The Programmer choices are worked out each time: the list made when
     * this connection was created can't include connections that start after
     * it, and LocoNet often does. The saved choice stays in the list even if
     * that connection isn't running, so opening Preferences doesn't clear it.
     */
    @Override
    public String[] getOptionChoices(String option) {
        if (!option1Name.equals(option)) {
            return super.getOptionChoices(option);
        }
        List<String> choices = new ArrayList<>(java.util.Arrays.asList(validOption1()));
        String current = getOptionState(option1Name);
        if (current != null && !current.isEmpty() && !choices.contains(current)) {
            choices.add(current);
        }
        return choices.toArray(new String[choices.size()]);
    }

    /**
     * Get a String that says what Option 2 represents May be an empty string,
     * but will not be null
     * @return option to match detected locos to roster.
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
        log.debug("configureOption2: {}", value);
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
            log.error("getOutputStream exception", e);
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
     * set up all of the other objects to operate with an Dcc4Pc command station
     * connected to this port
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        Dcc4PcTrafficController control = new Dcc4PcTrafficController();
        this.getSystemConnectionMemo().setDcc4PcTrafficController(control);
        control.connectPort(this);
        this.getSystemConnectionMemo().configureManagers();
        // Programming on Main goes out through another connection (by default, the default
        // command station's); RailCom brings the reply back
        this.getSystemConnectionMemo().useProgrammerFrom(getOptionState(option1Name));

    }

    private static final Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}
