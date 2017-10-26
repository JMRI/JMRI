package jmri.jmrix.anyma.udmx;

import java.util.ArrayList;
import java.util.List;
import jmri.jmrix.USBPortAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to XpressNet via a ZTC611 connected via an FTDI virtual comm
 * port.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 * @author George Warner, Copyright (C) 2017
 */
public class udmxAdapter extends USBPortAdapter {

    public udmxAdapter() {
        super();
        log.info("*udmxAdapter()");
//        option1Name = "FlowControl"; // NOI18N
//        options.put(option1Name, new Option(Bundle.getMessage("XconnectionUsesLabel", Bundle.getMessage("CSTypeZtc640")), validOption1));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPortNames() {
        log.info("*getPortNames()");
        return new ArrayList<>();
    }

    private String portName = null;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPort(String s) {
        log.info("*setPort('{}')", s);
        portName = s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getCurrentPortName() {
        return portName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureOption1(String value) {
        log.info("*configureOption1('{}')", value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureOption2(String value) {
        log.info("*configureOption2('{}')", value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureOption3(String value) {
        log.info("*configureOption3('{}')", value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configureOption4(String value) {
        log.info("*configureOption4('{}')", value);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getManufacturer() {
        log.info("*getManufacturer()");
        return "anyma";
    }

    @Override
    public String openPort(String portName, String appName) {
        log.info("*setPort('{}', '{}')", portName, appName);
        try {
            //    // get and open the primary port
            //    CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            //    try {
            //        activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            //    } catch (PortInUseException p) {
            //        return handlePortBusy(p, portName, log);
            //    }
            //
            //    // set timeout
            //    activeSerialPort.enableReceiveTimeout(10);
            //    log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
            //            + " " + activeSerialPort.isReceiveTimeoutEnabled());
            //
            //    // get and save stream
            //    serialStream = activeSerialPort.getInputStream();
            //
            //    // purge contents, if any
            //    purgeStream(serialStream);
            //
            //    // report status?
            //    if (log.isInfoEnabled()) {
            //        // report now
            //        log.info(portName + " port opened at "
            //                + activeSerialPort.getBaudRate() + " baud with"
            //                + " DTR: " + activeSerialPort.isDTR()
            //                + " RTS: " + activeSerialPort.isRTS()
            //                + " DSR: " + activeSerialPort.isDSR()
            //                + " CTS: " + activeSerialPort.isCTS()
            //                + "  CD: " + activeSerialPort.isCD()
            //        );
            //    }
            //    if (log.isDebugEnabled()) {
            //        // report additional status
            //        log.debug(" port flow control shows "
            //                + (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? "hardware flow control" : "no flow control"));
            //    }
            //    // arrange to notify later
            //    activeSerialPort.addEventListener(new SerialPortEventListener() {
            //        @Override
            //        public void serialEvent(SerialPortEvent e) {
            //            int type = e.getEventType();
            //            switch (type) {
            //                case SerialPortEvent.DATA_AVAILABLE:
            //                    log.debug("SerialEvent: DATA_AVAILABLE is {}", e.getNewValue());
            //                    return;
            //                case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
            //                    log.debug("SerialEvent: OUTPUT_BUFFER_EMPTY is {}", e.getNewValue());
            //                    setOutputBufferEmpty(true);
            //                    return;
            //                case SerialPortEvent.CTS:
            //                    log.debug("SerialEvent: CTS is {}", e.getNewValue());
            //                    return;
            //                case SerialPortEvent.DSR:
            //                    log.debug("SerialEvent: DSR is {}", e.getNewValue());
            //                    return;
            //                case SerialPortEvent.RI:
            //                    log.debug("SerialEvent: RI is {}", e.getNewValue());
            //                    return;
            //                case SerialPortEvent.CD:
            //                    log.debug("SerialEvent: CD is {}", e.getNewValue());
            //                    return;
            //                case SerialPortEvent.OE:
            //                    log.debug("SerialEvent: OE (overrun error) is {}", e.getNewValue());
            //                    return;
            //                case SerialPortEvent.PE:
            //                    log.debug("SerialEvent: PE (parity error) is {}", e.getNewValue());
            //                    return;
            //                case SerialPortEvent.FE:
            //                    log.debug("SerialEvent: FE (framing error) is {}", e.getNewValue());
            //                    return;
            //                case SerialPortEvent.BI:
            //                    log.debug("SerialEvent: BI (break interrupt) is {}", e.getNewValue());
            //                    return;
            //                default:
            //                    log.debug("SerialEvent of unknown type: {} value: {}", type, e.getNewValue());
            //                    return;
            //            }
            //        }
            //    }
            //    );
            //    try {
            //        activeSerialPort.notifyOnFramingError(true);
            //    } catch (Exception e) {
            //        if (log.isDebugEnabled()) {
            //            log.debug("Could not notifyOnFramingError: " + e);
            //        }
            //    }
            //
            //    try {
            //        activeSerialPort.notifyOnBreakInterrupt(true);
            //    } catch (Exception e) {
            //        if (log.isDebugEnabled()) {
            //            log.debug("Could not notifyOnBreakInterrupt: " + e);
            //        }
            //    }
            //
            //    try {
            //        activeSerialPort.notifyOnParityError(true);
            //    } catch (Exception e) {
            //        if (log.isDebugEnabled()) {
            //            log.debug("Could not notifyOnParityError: " + e);
            //        }
            //    }
            //
            //    try {
            //        activeSerialPort.notifyOnOutputEmpty(true);
            //    } catch (Exception e) {
            //        if (log.isDebugEnabled()) {
            //            log.debug("Could not notifyOnOutputEmpty: " + e);
            //        }
            //    }
            //
            //    try {
            //        activeSerialPort.notifyOnOverrunError(true);
            //    } catch (Exception e) {
            //        if (log.isDebugEnabled()) {
            //            log.debug("Could not notifyOnOverrunError: " + e);
            //        }
            //    }
            //
            //    opened = true;
            //
            //} catch (NoSuchPortException p) {
            //    return handlePortNotFound(p, portName, log);
            //} catch (IOException ex) {
            //    log.error("IO exception while opening port " + portName + " trace follows: " + ex);
            //    ex.printStackTrace();
            //    return "IO Exception while opening port " + portName + ": " + ex;
            //} catch (java.util.TooManyListenersException tmlex) {
            //    log.error("Too Many Listeners exception while opening port " + portName + " trace follows: " + tmlex);
            //    tmlex.printStackTrace();
            //    return "Too Many Listeners Exception while opening port " + portName + ": " + tmlex;
            //} catch (UnsupportedCommOperationException ucex) {
            //    log.error("unsupported Comm Operation exception while opening port " + portName + " trace follows: " + ucex);
            //    ucex.printStackTrace();
            //    return "Unsupported Comm Exception while opening port " + portName + ": " + ucex;
        } catch (Exception e) {
            log.error("Exception: " + e);
        }

        return null; // normal operation
    }

    /**
     * set up all of the other objects to operate with a ZTC611 connected to
     * this port
     */
    @Override
    public void configure() {
        log.info("*configure()");
//        // connect to a packetizing traffic controller
//        XNetTrafficController packets = new ZTC611XNetPacketizer(new LenzCommandStation());
//        packets.connectPort(this);
//
//        // start operation
//        // packets.startThreads();
//        this.getSystemConnectionMemo().setXNetTrafficController(packets);
//        new ZTC611XNetInitializationManager(this.getSystemConnectionMemo());
    }

    private boolean opened = false;

    @Override
    public boolean status() {
        log.info("*status()");
        return opened;
    }

    //meanings are assigned to these above, so make sure the order is consistent
    //protected String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionNoRecomm"), Bundle.getMessage("FlowOptionHw")};

    private final static Logger log = LoggerFactory.getLogger(udmxAdapter.class);
}
