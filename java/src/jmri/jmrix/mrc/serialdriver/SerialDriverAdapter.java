package jmri.jmrix.mrc.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import jmri.jmrix.mrc.MrcPacketizer;
import jmri.jmrix.mrc.MrcPortController;
import jmri.jmrix.mrc.MrcSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for the MRC system. This connects an MRC command
 * station via a serial com port. Normally controlled by the SerialDriverFrame
 * class.
 * <p>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public class SerialDriverAdapter extends MrcPortController {

    SerialPort activeSerialPort = null;

    public SerialDriverAdapter() {
        super(new MrcSystemConnectionMemo());
        setManufacturer(jmri.jmrix.mrc.MrcConnectionTypeList.MRC);
        options.put("CabAddress", new Option("Cab Address:", validOption1, false)); //IN18N
    }

    @Override
    public String openPort(String portName, String appName) {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = (SerialPort) portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for communication via SerialDriver
            try {
                activeSerialPort.setSerialPortParams(currentBaudNumber(getCurrentBaudRate()), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_ODD);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());//IN18N
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();//IN18N
            }

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            configureLeadsAndFlowControl(activeSerialPort, 0);

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.info("Serial timeout was observed as: " + activeSerialPort.getReceiveTimeout()
                    + " " + activeSerialPort.isReceiveTimeoutEnabled());//IN18N
            log.info("input buffer " + activeSerialPort.getInputBufferSize());//IN18N
            // get and save stream
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
                );//IN18N
            }

            opened = true;

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException ex) {
            log.error("Unexpected exception while opening port {}", portName, ex);
            return "Unexpected error while opening port " + portName + ": " + ex;//IN18N
        }

        return null; // indicates OK return

    }

    /**
     * set up all of the other objects to operate with an serial command station
     * connected to this port
     */
    @Override
    public void configure() {
        MrcPacketizer packets = new MrcPacketizer();
        packets.connectPort(this);
        this.getSystemConnectionMemo().setMrcTrafficController(packets);

        packets.setAdapterMemo(this.getSystemConnectionMemo());
        packets.setCabNumber(Integer.parseInt(getOptionState("CabAddress")));//IN18N

        this.getSystemConnectionMemo().configureManagers();

        packets.startThreads();
    }

    // base class methods for the MrcPortController interface
    @Override
    public DataInputStream getInputStream() {
        if (!opened) {
            log.error("getInputStream called before load(), stream not available");//IN18N
            return null;
        }

        return new DataInputStream(serialStream);
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available");//IN18N
        }
        try {
            return new DataOutputStream(activeSerialPort.getOutputStream());
        } catch (java.io.IOException e) {
            log.error("getOutputStream exception: " + e);//IN18N
        }
        return null;
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"38,400 bps"}; //IN18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{38400};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    protected String[] validOption1 = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};//IN18N

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}
