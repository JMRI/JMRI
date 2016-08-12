package jmri.jmrix.acela.serialdriver;

import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import jmri.jmrix.acela.AcelaPortController;
import jmri.jmrix.acela.AcelaSystemConnectionMemo;
import jmri.jmrix.acela.AcelaTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements SerialPortAdapter for the Acela system. This connects an Acela
 * interface to the CTI network via a serial com port. Normally controlled by
 * the SerialDriverFrame class.
 * <P>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2002
 *
 * @author	Bob Coleman, Copyright (C) 2007, 2008 Based on Mrc example, modified
 * to establish Acela support.
 */
public class SerialDriverAdapter extends AcelaPortController implements jmri.jmrix.SerialPortAdapter {

    public SerialDriverAdapter() {
        super(new AcelaSystemConnectionMemo());
        setManufacturer(jmri.jmrix.acela.AcelaConnectionTypeList.CTI);
    }

    SerialPort activeSerialPort = null;

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
                activeSerialPort.setSerialPortParams(currentBaudNumber(getCurrentBaudRate()), SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
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
        } catch (Exception ex) {
            log.error("Unexpected exception while opening port " + portName + " trace follows: " + ex);
            ex.printStackTrace();
            return "Unexpected error while opening port " + portName + ": " + ex;
        }

        return null; // indicates OK return

    }

    /**
     * set up all of the other objects to operate with an serial command station
     * connected to this port
     */
    public void configure() {
        // connect to the traffic controller
        AcelaTrafficController control = new AcelaTrafficController();
        control.connectPort(this);

        this.getSystemConnectionMemo().setAcelaTrafficController(control);
        this.getSystemConnectionMemo().configureManagers();

        // connect to a packetizing traffic controller
        // LnPacketizer packets = new LnPacketizer();
        // packets.connectPort(this);
        // do the common manager config
        // configureManagers();
        //now moved to the adapter memo
   	/*jmri.InstanceManager.setLightManager(new jmri.jmrix.acela.AcelaLightManager());

         AcelaSensorManager s;
         jmri.InstanceManager.setSensorManager(s = new jmri.jmrix.acela.AcelaSensorManager());
         this.getSystemConnectionMemo().getTrafficController().setSensorManager(s);	

         AcelaTurnoutManager t;
         jmri.InstanceManager.setTurnoutManager(t = new jmri.jmrix.acela.AcelaTurnoutManager());
         this.getSystemConnectionMemo().getTrafficController().setTurnoutManager(t);	*/
        // start operation
        // packets.startThreads();
    }

    // base class methods for the AcelaPortController interface
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

    public boolean status() {
        return opened;
    }

    /**
     * Get an array of valid baud rates.
     */
    public String[] validBaudRates() {
//	Really just want 9600 Baud for Acela
//      return new String[]{"9,600 bps", "19,200 bps", "38,400 bps", "57,600 bps"};
        return new String[]{"9,600 bps"};
    }

    /**
     * Return array of valid baud rates as integers.
     */
    public int[] validBaudNumber() {
//	Really just want 9600 Baud for Acela
//      return new int[]{9600, 19200, 38400, 57600};
        return new int[]{9600};
    }

    // private control members
    private boolean opened = false;
    InputStream serialStream = null;

    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static public SerialDriverAdapter instance() {
        if (mInstance == null) {
            mInstance = new SerialDriverAdapter();
        }
        return mInstance;
    }
    /**
     * @deprecated JMRI Since 4.4 instance() shouldn't be used, convert to JMRI multi-system support structure
     */
    @Deprecated
    static SerialDriverAdapter mInstance = null;

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class.getName());
}
