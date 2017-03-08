package jmri.jmrix.xpa.serialdriver;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import jmri.jmrix.xpa.XpaPortController;
import jmri.jmrix.xpa.XpaSystemConnectionMemo;
import jmri.jmrix.xpa.XpaTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for a modem connected to an XPA.
 * <P>
 * This connects an XPA+Modem connected to an XPressNet based command station
 * via a serial com port. Normally controlled by the SerialDriverFrame class.
 * <P>
 * The current implementation only handles the 9,600 baud rate. It uses the
 * first configuraiont variable for the modem initilization string.
 *
 * @author	Paul Bender Copyright (C) 2004
 */
public class SerialDriverAdapter extends XpaPortController implements jmri.jmrix.SerialPortAdapter {

    public SerialDriverAdapter() {

        super(new XpaSystemConnectionMemo());
        ((XpaSystemConnectionMemo)getSystemConnectionMemo()).setXpaTrafficController(new XpaTrafficController());


        option1Name = "ModemInitString";
        options.put(option1Name, new Option("Modem Initilization String : ", new String[]{"ATX0E0"}));
        this.manufacturerName = jmri.jmrix.lenz.LenzConnectionTypeList.LENZ;
    }

    SerialPort activeSerialPort = null;

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

            // try to set it for comunication via SerialDriver
            try {
                activeSerialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
            } catch (gnu.io.UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port " + portName + ": " + e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // set RTS high, DTR high
            activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
            activeSerialPort.setDTR(true);		// pin 1 in DIN8; on main connector, this is DTR

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            activeSerialPort.setFlowControlMode(0);

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
        } catch (gnu.io.UnsupportedCommOperationException ucoe) {
            log.error("Unsupported Communication Operation Exception while opening port " + portName + " trace follows: " + ucoe);
            return "Unsupported Communication Operation Exception while opening port " + portName + ": " + ucoe;
        } catch (java.io.IOException ex) {
            log.error("IO exception while opening port " + portName + " trace follows: " + ex);
            ex.printStackTrace();
            return "IO Exception while opening port " + portName + ": " + ex;
        }

        return null; // indicates OK return

    }

    /**
     * set up all of the other objects to operate with an XPA+Modem Connected to
     * an XPressNet based command station connected to this port
     */
    @Override
    public void configure() {

        // connect to the traffic controller
        XpaSystemConnectionMemo memo = ((XpaSystemConnectionMemo)getSystemConnectionMemo());
        XpaTrafficController tc = memo.getXpaTrafficController();
        tc.connectPort(this);
        
        memo.setPowerManager(new jmri.jmrix.xpa.XpaPowerManager(tc));
        jmri.InstanceManager.store(memo.getPowerManager(), jmri.PowerManager.class);

        memo.setTurnoutManager(new jmri.jmrix.xpa.XpaTurnoutManager(memo));
        jmri.InstanceManager.store(memo.getTurnoutManager(),jmri.TurnoutManager.class);
        memo.setThrottleManager(new jmri.jmrix.xpa.XpaThrottleManager(memo));
        jmri.InstanceManager.store(memo.getThrottleManager(),jmri.ThrottleManager.class);

        // start operation
        tc.startTransmitThread();
        sinkThread = new Thread(tc);
        sinkThread.start();
    }

    private Thread sinkThread;

    // base class methods for the XpaPortController interface
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

    @Override
    public boolean status() {
        return opened;
    }

    /**
     * Get an array of valid baud rates. This is currently only 19,200 bps
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"9,600 bps"};
    }

    private boolean opened = false;
    InputStream serialStream = null;

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class.getName());

}
