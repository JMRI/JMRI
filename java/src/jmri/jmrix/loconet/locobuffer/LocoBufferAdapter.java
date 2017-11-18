package jmri.jmrix.loconet.locobuffer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.Vector;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LnPortController;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.SerialPortEventListener;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Provide access to LocoNet via a LocoBuffer attached to a serial comm port.
 * <P>
 * Normally controlled by the LocoBufferFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2008, 2010
 */
public class LocoBufferAdapter extends LnPortController implements jmri.jmrix.SerialPortAdapter {

    public LocoBufferAdapter() {
        this(new LocoNetSystemConnectionMemo());
    }

    public LocoBufferAdapter(LocoNetSystemConnectionMemo adapterMemo) {
        super(adapterMemo);
        option1Name = "FlowControl"; // NOI18N
        option2Name = "CommandStation"; // NOI18N
        option3Name = "TurnoutHandle"; // NOI18N
        options.put(option1Name, new Option("Connection uses:", validOption1));
        options.put(option2Name, new Option("Command station type:", getCommandStationListWithStandaloneLN(), false));
        options.put(option3Name, new Option("Turnout command handling:", new String[]{"Normal", "Spread", "One Only", "Both"}));
    }
    
    /**
     * Create a list of possible command stations and append "Standalone LocoNet"
     * 
     * Note: This is not suitable for use by any class which extends this class if
     * the hardware interface is part of a command station.
     * 
     * @return String[] containing the array of command stations, plus "Standalone 
     *          LocoNet"
     */
    public String[] getCommandStationListWithStandaloneLN() {
        String[] result = new String[commandStationNames.length + 1];
        for (int i = 0 ; i < result.length-1; ++i) {
            result[i] = commandStationNames[i];
        }
        result[commandStationNames.length] = LnCommandStationType.COMMAND_STATION_STANDALONE.getName();
        return result;
    }
    
    Vector<String> portNameVector = null;
    SerialPort activeSerialPort = null;

    @SuppressWarnings("unchecked")
    @Override
    public Vector<String> getPortNames() {
        // first, check that the comm package can be opened and ports seen
        portNameVector = new Vector<String>();
        Enumeration<CommPortIdentifier> portIDs = CommPortIdentifier.getPortIdentifiers();
        // find the names of suitable ports
        while (portIDs.hasMoreElements()) {
            CommPortIdentifier id = portIDs.nextElement();
            // filter out line printers 
            if (id.getPortType() != CommPortIdentifier.PORT_PARALLEL) // accumulate the names in a vector
            {
                portNameVector.addElement(id.getName());
            }
        }
        return portNameVector;
    }

    @Override
    public String openPort(String portName, String appName) {
        // open the primary and secondary ports in LocoNet mode, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }
            // try to set it for LocoNet via LocoBuffer
            try {
                setSerialPort(activeSerialPort);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage(); // NOI18N
            }

            // set timeout
            try {
                activeSerialPort.enableReceiveTimeout(10);
                log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout() // NOI18N
                        + " " + activeSerialPort.isReceiveTimeoutEnabled());
            } catch (Exception et) {
                log.info("failed to set serial timeout: " + et); // NOI18N
            }

            // get and save stream
            serialStream = activeSerialPort.getInputStream();

            // purge contents, if any
            purgeStream(serialStream);

            // report status?
            if (log.isInfoEnabled()) {
                // report now
                log.info(portName + " port opened at " // NOI18N
                        + activeSerialPort.getBaudRate() + " baud with" // NOI18N
                        + " DTR: " + activeSerialPort.isDTR() // NOI18N
                        + " RTS: " + activeSerialPort.isRTS() // NOI18N
                        + " DSR: " + activeSerialPort.isDSR() // NOI18N
                        + " CTS: " + activeSerialPort.isCTS() // NOI18N
                        + "  CD: " + activeSerialPort.isCD() // NOI18N
                );
            }
            if (log.isDebugEnabled()) {
                // report additional status
                log.debug(" port flow control shows " // NOI18N
                        + (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? "hardware flow control" : "no flow control")); // NOI18N
            }
            if (log.isDebugEnabled()) {
                // arrange to notify later
                activeSerialPort.addEventListener(new SerialPortEventListener() {
                    @Override
                    public void serialEvent(SerialPortEvent e) {
                        int type = e.getEventType();
                        switch (type) {
                            case SerialPortEvent.DATA_AVAILABLE:
                                log.info("SerialEvent: DATA_AVAILABLE is " + e.getNewValue()); // NOI18N
                                return;
                            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
                                log.info("SerialEvent: OUTPUT_BUFFER_EMPTY is " + e.getNewValue()); // NOI18N
                                return;
                            case SerialPortEvent.CTS:
                                log.info("SerialEvent: CTS is " + e.getNewValue()); // NOI18N
                                return;
                            case SerialPortEvent.DSR:
                                log.info("SerialEvent: DSR is " + e.getNewValue()); // NOI18N
                                return;
                            case SerialPortEvent.RI:
                                log.info("SerialEvent: RI is " + e.getNewValue()); // NOI18N
                                return;
                            case SerialPortEvent.CD:
                                log.info("SerialEvent: CD is " + e.getNewValue()); // NOI18N
                                return;
                            case SerialPortEvent.OE:
                                log.info("SerialEvent: OE (overrun error) is " + e.getNewValue()); // NOI18N
                                return;
                            case SerialPortEvent.PE:
                                log.info("SerialEvent: PE (parity error) is " + e.getNewValue()); // NOI18N
                                return;
                            case SerialPortEvent.FE:
                                log.info("SerialEvent: FE (framing error) is " + e.getNewValue()); // NOI18N
                                return;
                            case SerialPortEvent.BI:
                                log.info("SerialEvent: BI (break interrupt) is " + e.getNewValue()); // NOI18N
                                return;
                            default:
                                log.info("SerialEvent of unknown type: " + type + " value: " + e.getNewValue()); // NOI18N
                                return;
                        }
                    }
                }
                );
                try {
                    activeSerialPort.notifyOnFramingError(true);
                } catch (Exception e) {
                    log.debug("Could not notifyOnFramingError: " + e); // NOI18N
                }

                try {
                    activeSerialPort.notifyOnBreakInterrupt(true);
                } catch (Exception e) {
                    log.debug("Could not notifyOnBreakInterrupt: " + e); // NOI18N
                }

                try {
                    activeSerialPort.notifyOnParityError(true);
                } catch (Exception e) {
                    log.debug("Could not notifyOnParityError: " + e); // NOI18N
                }

                try {
                    activeSerialPort.notifyOnOverrunError(true);
                } catch (Exception e) {
                    log.debug("Could not notifyOnOverrunError: " + e); // NOI18N
                }

            }

            opened = true;

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException | TooManyListenersException ex) {
            log.error("Unexpected exception while opening port {} trace follows:", portName, ex); // NOI18N
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // normal operation
    }

    /**
     * Can the port accept additional characters? The state of CTS determines
     * this, as there seems to be no way to check the number of queued bytes and
     * buffer length. This might go false for short intervals, but it might also
     * stick off if something goes wrong.
     * 
     * @return an indication of whether the interface is accepting transmit messages.
     */
    @Override
    public boolean okToSend() {
        return activeSerialPort.isCTS();
    }

    /**
     * Set up all of the other objects to operate with a LocoBuffer connected to
     * this port.
     */
    @Override
    public void configure() {

        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        // connect to a packetizing traffic controller
        LnPacketizer packets = new LnPacketizer();
        packets.connectPort(this);

        // create memo
        this.getSystemConnectionMemo().setLnTrafficController(packets);
        // do the common manager config

        this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                mTurnoutNoRetry, mTurnoutExtraSpace);
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
    }

    // base class methods for the LnPortController interface
    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available"); // NOI18N
            return null;
        }
        return new DataInputStream(serialStream);
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available"); // NOI18N
        }
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        } catch (java.io.IOException e) {
            log.error("getOutputStream exception: " +  // NOI18N
                    e.getMessage());
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Local method to do specific configuration, overridden in class
     * @param activeSerialPort is the serial port to be configured
     * @throws UnsupportedCommOperationException Usually if the hardware isn't present or capable
     */
    protected void setSerialPort(SerialPort activeSerialPort) throws UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT; // default, but also defaults in selectedOption1
        if (getOptionState(option1Name).equals(validOption1[1])) {
            flow = SerialPort.FLOWCONTROL_NONE;
        }
        configureLeadsAndFlowControl(activeSerialPort, flow);
        
        log.debug("Found flow control " + activeSerialPort.getFlowControlMode() // NOI18N
                + " RTSCTS_OUT=" + SerialPort.FLOWCONTROL_RTSCTS_OUT // NOI18N
                + " RTSCTS_IN= " + SerialPort.FLOWCONTROL_RTSCTS_IN); // NOI18N
    }

    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    @Override
    public int[] validBaudNumber() {
        return Arrays.copyOf(validSpeedValues, validSpeedValues.length);
    }

    protected String[] validSpeeds = new String[]{"19,200 baud (J1 on 1&2)", "57,600 baud (J1 on 2&3)"};
    protected int[] validSpeedValues = new int[]{19200, 57600};

    // meanings are assigned to these above, so make sure the order is consistent
    protected String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionHwRecomm"), Bundle.getMessage("FlowOptionNo")};

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    private final static Logger log = LoggerFactory.getLogger(LocoBufferAdapter.class);

}
