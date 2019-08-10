package jmri.jmrix.zimo.mx1;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.TooManyListenersException;
import jmri.jmrix.zimo.Mx1CommandStation;
import jmri.jmrix.zimo.Mx1Packetizer;
import jmri.jmrix.zimo.Mx1PortController;
import jmri.jmrix.zimo.Mx1SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.SerialPortEvent;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Provide access to Zimo's MX-1 on an attached serial com port. Adapted for
 * use with Zimo MX-1 by Sip Bosch.
 *
 * @author	Bob Jacobsen Copyright (C) 2002
 */
public class Mx1Adapter extends Mx1PortController {

    public Mx1Adapter() {
        super(new Mx1SystemConnectionMemo());
        option1Name = "FlowControl"; // NOI18N
        options.put(option1Name, new Option("MX-1 connection uses : ", validOption1));
        this.manufacturerName = jmri.jmrix.zimo.Mx1ConnectionTypeList.ZIMO;
    }

    SerialPort activeSerialPort = null;

    @Override
    public String openPort(String portName, String appName) {
        // open the port in MX-1 mode, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }
            // try to set it for Can Net
            try {
                setSerialPort();
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                    + " " + activeSerialPort.isReceiveTimeoutEnabled());

            // get and save stream
            serialStream = activeSerialPort.getInputStream();

            // purge contents, if any
            purgeStream(serialStream);

            // report status?
            if (log.isInfoEnabled()) {
                // report now
                log.info(portName + " port opened at "
                        + activeSerialPort.getBaudRate() + " baud with"
                        + " DTR: " + activeSerialPort.isDTR()
                        + " RTS: " + activeSerialPort.isRTS()
                        + " DSR: " + activeSerialPort.isDSR()
                        + " CTS: " + activeSerialPort.isCTS()
                        + "  CD: " + activeSerialPort.isCD()
                );
            }
            if (log.isDebugEnabled()) {
                // report additional status
                log.debug(" port flow control shows " // NOI18N
                        + (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? "hardware flow control" : "no flow control")); // NOI18N

                // log events
                setPortEventLogging(activeSerialPort);
            }

            opened = true;

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException ex) {
            log.error("Unexpected exception while opening port {} trace follows: ", portName, ex);
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
     * @return true if more data can be sent; false otherwise
     */
    @Override
    public boolean okToSend() {
        return activeSerialPort.isCTS();
    }

    /**
     * set up all of the other objects to operate with a MX-1 connected to this
     * port
     */
    @Override
    public void configure() {
        Mx1CommandStation cs = new Mx1CommandStation(getSystemConnectionMemo().getSystemPrefix(), getSystemConnectionMemo().getUserName());
        getSystemConnectionMemo().setCommandStation(cs);
        // connect to a packetizing traffic controller
        Mx1Packetizer packets = new Mx1Packetizer(cs, Mx1Packetizer.ASCII);
        packets.connectPort(this);

        getSystemConnectionMemo().setMx1TrafficController(packets);
        getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
    }

    // base class methods for the ZimoPortController interface
    
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
            log.error("getOutputStream exception: " + e.getMessage());
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Local method to do specific configuration.
     *
     * @throws purejavacomm.UnsupportedCommOperationException if unable to
     *                                                        configure the
     *                                                        serial port
     */
    protected void setSerialPort() throws UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // find and configure flow control
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT; // default, but also defaults in selectedOption1
        if (getOptionState(option1Name).equals(validOption1[1])) {
            flow = 0;
        }
        configureLeadsAndFlowControl(activeSerialPort, flow);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return Arrays.copyOf(validSpeedValues, validSpeedValues.length);
    }

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud1200"),
            Bundle.getMessage("Baud2400"), Bundle.getMessage("Baud4800"),
            Bundle.getMessage("Baud9600"), Bundle.getMessage("Baud19200"),
            Bundle.getMessage("Baud38400")};
    protected int[] validSpeedValues = new int[]{1200, 2400, 4800, 9600, 19200, 38400};

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // meanings are assigned to these above, so make sure the order is consistent
    protected String[] validOption1 = new String[]{Bundle.getMessage("FlowOptionHwRecomm"), Bundle.getMessage("FlowOptionNo")};
    protected String[] validOption2 = new String[]{"3", "5"};
    //protected String selectedOption1=validOption1[0];

    InputStream serialStream = null;

    /**
     * @return the default adapter
     * @deprecated since 4.4 instance() shouldn't be used, convert to JMRI
     * multi-system support structure
     */
    @Deprecated
    static public Mx1Adapter instance() {
        if (mInstance == null) {
            mInstance = new Mx1Adapter();
        }
        return mInstance;
    }
    static Mx1Adapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(Mx1Adapter.class);

}
