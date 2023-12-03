package jmri.jmrix.cmri.serial.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.util.Arrays;

import jmri.jmrix.cmri.CMRISystemConnectionMemo;
import jmri.jmrix.cmri.serial.SerialPortAdapter;
import jmri.jmrix.cmri.serial.SerialTrafficController;

import com.fazecast.jSerialComm.*;

/**
 * Provide access to C/MRI via a serial com port. Normally controlled by the
 * cmri.serial.serialdriver.SerialDriverFrame class.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 */
public class SerialDriverAdapter extends SerialPortAdapter {

    public SerialDriverAdapter() {
        super(new CMRISystemConnectionMemo());
        this.manufacturerName = jmri.jmrix.cmri.CMRIConnectionTypeList.CMRI;
    }

    SerialPort activeSerialPort = null;

    @Override
    public String openPort(String portName, String appName) {
        // open the port, check ability to set moderators
 
        // get and open the primary port
        activeSerialPort = SerialPort.getCommPort(portName);  // name of program, msec to wait
        activeSerialPort.openPort();
        
        // try to set it for communication via SerialDriver
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        activeSerialPort.setBaudRate(baud);
        activeSerialPort.setDTR();
        activeSerialPort.setRTS();
        activeSerialPort.setFlowControl(
                SerialPort.FLOW_CONTROL_DTR_ENABLED |
                SerialPort.FLOW_CONTROL_CTS_ENABLED);
        activeSerialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);
            
        // get and save stream
        serialStream = activeSerialPort.getInputStream();

        // purge contents, if any
        //purgeStream(serialStream);

        // report status?
        if (log.isInfoEnabled()) {
            log.info("{} port opened at {} baud, sees  DTR: {} RTS: {} DSR: {} CTS: {}  name: {}", portName, activeSerialPort.getBaudRate(), activeSerialPort.getDTR(), activeSerialPort.getRTS(), activeSerialPort.getDSR(), activeSerialPort.getCTS(), activeSerialPort);
        }

        opened = true;

        return null; // indicates OK return
    }

    /**
     * Can the port accept additional characters? Yes, always
     * @return always true.
     */
    public boolean okToSend() {
        return true;
    }

    /**
     * set up all of the other objects to operate connected to this port
     */
    @Override
    public void configure() {
        // connect to the traffic controller
        SerialTrafficController tc = new SerialTrafficController();
        tc.connectPort(this);
        ((CMRISystemConnectionMemo)getSystemConnectionMemo()).setTrafficController(tc);
        ((CMRISystemConnectionMemo)getSystemConnectionMemo()).configureManagers();
    }

    // base class methods for the SerialPortAdapter interface
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

        return new DataOutputStream(activeSerialPort.getOutputStream());
    }

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Local method to do specific port configuration.
     *
     */
    protected void setSerialPort() {
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        activeSerialPort.setComPortParameters(baud, 8,
                SerialPort.TWO_STOP_BITS, SerialPort.NO_PARITY);

        // find and configure flow control
        int flow = SerialPort.FLOW_CONTROL_DISABLED; // default
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

    protected String[] validSpeeds = new String[]{Bundle.getMessage("Baud9600"),
            Bundle.getMessage("Baud19200"), Bundle.getMessage("Baud28800"),
            Bundle.getMessage("Baud57600"), Bundle.getMessage("Baud115200")};
    protected int[] validSpeedValues = new int[]{9600, 19200, 28800, 57600, 115200};

    @Override
    public int defaultBaudIndex() {
        return 1;
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
