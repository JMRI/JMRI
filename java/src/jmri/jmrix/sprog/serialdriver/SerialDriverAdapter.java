package jmri.jmrix.sprog.serialdriver;

import java.io.DataInputStream;
import java.io.DataOutputStream;

import jmri.jmrix.sprog.SprogConstants.SprogMode;
import jmri.jmrix.sprog.SprogPortController;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogTrafficController;
import jmri.jmrix.sprog.update.SprogType;

import com.fazecast.jSerialComm.*;

/**
 * Implements SerialPortAdapter for the Sprog system.
 * <p>
 * This connects an Sprog command station via a serial com port. Also used for
 * the USB SPROG, which appears to the computer as a serial port.
 * <p>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 * Updated January 2010 for gnu io (RXTX) - Andrew Berridge.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public class SerialDriverAdapter extends SprogPortController {

    public SerialDriverAdapter() {
        super(new SprogSystemConnectionMemo(SprogMode.SERVICE));
        // Set the username to match name, once refactored to handle multiple connections or user setable names/prefixes then this can be removed
        this.baudRate = 9600;
        this.getSystemConnectionMemo().setUserName(Bundle.getMessage("SprogProgrammerTitle"));
        // create the traffic controller
        this.getSystemConnectionMemo().setSprogTrafficController(new SprogTrafficController(this.getSystemConnectionMemo()));
    }

    public SerialDriverAdapter(SprogMode sm) {
        super(new SprogSystemConnectionMemo(sm));
        this.baudRate = 9600;
        this.getSystemConnectionMemo().setUserName("SPROG");
        // create the traffic controller
        this.getSystemConnectionMemo().setSprogTrafficController(new SprogTrafficController(this.getSystemConnectionMemo()));
    }

    public SerialDriverAdapter(SprogMode sm, int baud, SprogType type) {
        super(new SprogSystemConnectionMemo(sm, type));
        this.baudRate = baud;
        this.getSystemConnectionMemo().setUserName("SPROG");
        // create the traffic controller
        this.getSystemConnectionMemo().setSprogTrafficController(new SprogTrafficController(this.getSystemConnectionMemo()));
    }

    public SerialDriverAdapter(SprogMode sm, int baud) {
        super(new SprogSystemConnectionMemo(sm));
        this.baudRate = baud;
        this.getSystemConnectionMemo().setUserName("SPROG");
        // create the traffic controller
        this.getSystemConnectionMemo().setSprogTrafficController(new SprogTrafficController(this.getSystemConnectionMemo()));
    }

    SerialPort activeSerialPort = null;

    private int baudRate = -1;

    @Override
    public String openPort(String portName, String appName) {
        // get and open the primary port
        activeSerialPort = SerialPort.getCommPort(portName);
        //activeSerialPort = SerialPort.getCommPorts()[3];
        activeSerialPort.openPort();
        log.info("Connecting SPROG to {}", activeSerialPort);

        // try to set it for communication via SerialDriver
        activeSerialPort.setBaudRate(baudRate);
        activeSerialPort.setDTR();
        activeSerialPort.setRTS();
        activeSerialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        activeSerialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_BLOCKING, 0, 0);

        // get and save stream
        serialStream = new DataInputStream(activeSerialPort.getInputStream());
        log.trace("SerialDriverAdapter serialStream: {}", serialStream);

        // add Sprog Traffic Controller as event listener
        activeSerialPort.addDataListener( new SerialPortDataListener() {
            @Override 
            public int getListeningEvents() {
                log.trace("getListeningEvents");
                return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
            }
            @Override
            public void serialEvent(SerialPortEvent event) {
                log.trace("serial event start");
                // invoke
                getSystemConnectionMemo().getSprogTrafficController().handleOneIncomingReply();
                log.trace("serial event end");
            }
        }
        );

        opened = true;
        return null; // indicates OK return

    }

    public void setHandshake(boolean on) {
        log.warn("setHandshake");
        if (on) {
            activeSerialPort.setFlowControl(
                SerialPort.FLOW_CONTROL_RTS_ENABLED |
                SerialPort.FLOW_CONTROL_CTS_ENABLED
            );
        } else {
            activeSerialPort.setFlowControl(SerialPort.FLOW_CONTROL_DISABLED);
        }
//         try {
//             activeSerialPort.setFlowControlMode(mode);
//         } catch (UnsupportedCommOperationException ex) {
//             log.error("Unexpected exception while setting COM port handshake mode,", ex);
//         }
    }

    // base class methods for the SprogPortController interface
    @Override
    public DataInputStream getInputStream() {
        
        if (!opened) {
            log.error("getInputStream called before load(), stream not available", new Exception("traceback"));
            return null;
        }
        return serialStream;
    }

    @Override
    public DataOutputStream getOutputStream() {
        if (!opened) {
            log.error("getOutputStream called before load(), stream not available", new Exception("traceback"));
        }
        return new DataOutputStream(activeSerialPort.getOutputStream());
    }

    /**
     * {@inheritDoc}
     * Currently only 9,600 bps
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"9,600 bps"};
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{9600};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    DataInputStream serialStream = null;

    protected int numSlots = 1;

    /**
     * Set up all of the other objects to operate with an Sprog command station
     * connected to this port.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings( value = "SLF4J_FORMAT_SHOULD_BE_CONST",
        justification = "passing exception text")
    @Override
    public void configure() {
        // connect to the traffic controller
        this.getSystemConnectionMemo().getSprogTrafficController().connectPort(this);

        log.debug("Configure command station");
        this.getSystemConnectionMemo().configureCommandStation(numSlots, getOptionState("TrackPowerState"));
        this.getSystemConnectionMemo().configureManagers();

        if (getOptionState("TrackPowerState") != null && getOptionState("TrackPowerState").equals(Bundle.getMessage("PowerStateOn"))) {
            try {
                this.getSystemConnectionMemo().getPowerManager().setPower(jmri.PowerManager.ON);
            } catch (jmri.JmriException e) {
                log.error(e.toString());
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
