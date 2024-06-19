package jmri.jmrix.xpa.serialdriver;

import java.io.IOException;

import jmri.InstanceManager;
import jmri.jmrix.xpa.XpaPortController;
import jmri.jmrix.xpa.XpaSystemConnectionMemo;
import jmri.jmrix.xpa.XpaTrafficController;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.jmrix.purejavacomm.CommPortIdentifier;
import jmri.jmrix.purejavacomm.NoSuchPortException;
import jmri.jmrix.purejavacomm.PortInUseException;
import jmri.jmrix.purejavacomm.UnsupportedCommOperationException;

/**
 * Implements SerialPortAdapter for a modem connected to an XPA.
 * <p>
 * This connects an XPA+Modem connected to an XpressNet based command station
 * via a serial com port. Normally controlled by the SerialDriverFrame class.
 * <p>
 * The current implementation only handles the 9,600 baud rate. It uses the
 * first configuraiont variable for the modem initialization string.
 *
 * @author Paul Bender Copyright (C) 2004
 */
public class SerialDriverAdapter extends XpaPortController {

    public SerialDriverAdapter() {

        super(new XpaSystemConnectionMemo());
        ((XpaSystemConnectionMemo)getSystemConnectionMemo()).setXpaTrafficController(new XpaTrafficController());


        option1Name = "ModemInitString"; // NOI18N
        options.put(option1Name, new Option(Bundle.getMessage("ModemInitStringLabel"), new String[]{"ATX0E0"}));
        this.manufacturerName = jmri.jmrix.lenz.LenzConnectionTypeList.LENZ;
    }

    jmri.jmrix.purejavacomm.SerialPort activeSerialPort = null;

    @Override
    public String openPort(String portName, String appName) {
        // open the port, check ability to set moderators
        try {
            // get and open the primary port
            CommPortIdentifier portID = CommPortIdentifier.getPortIdentifier(portName);
            try {
                activeSerialPort = portID.open(appName, 2000);  // name of program, msec to wait
            } catch (PortInUseException p) {
                return handlePortBusy(p, portName, log);
            }

            // try to set it for communication via SerialDriver
            try {
                activeSerialPort.setSerialPortParams(9600,
                        jmri.jmrix.purejavacomm.SerialPort.DATABITS_8,
                        jmri.jmrix.purejavacomm.SerialPort.STOPBITS_1,
                        jmri.jmrix.purejavacomm.SerialPort.PARITY_NONE);
            } catch (UnsupportedCommOperationException e) {
                log.error("Cannot set serial parameters on port {}: {}", portName, e.getMessage());
                return "Cannot set serial parameters on port " + portName + ": " + e.getMessage();
            }

            // disable flow control; hardware lines used for signaling, XON/XOFF might appear in data
            configureLeadsAndFlowControl(activeSerialPort, 0);

            // set timeout
            // activeSerialPort.enableReceiveTimeout(1000);
            log.debug("Serial timeout was observed as: {} {}", activeSerialPort.getReceiveTimeout(), activeSerialPort.isReceiveTimeoutEnabled());

            // purge contents, if any
            purgeStream(activeSerialPort.getInputStream());

            // report status?
            if (log.isInfoEnabled()) {
                log.info("{} port opened at {} baud, sees  DTR: {} RTS: {} DSR: {} CTS: {}  CD: {}", portName, activeSerialPort.getBaudRate(), activeSerialPort.isDTR(), activeSerialPort.isRTS(), activeSerialPort.isDSR(), activeSerialPort.isCTS(), activeSerialPort.isCD());
            }

            opened = true;

        } catch (NoSuchPortException p) {
            return handlePortNotFound(p, portName, log);
        } catch (IOException ex) {
            log.error("Unexpected exception while opening port {}", portName, ex);
            return "IO Exception while opening port " + portName + ": " + ex;
        }

        return null; // indicates OK return

    }

    /**
     * set up all of the other objects to operate with an XPA+Modem Connected to
     * an XpressNet based command station connected to this port
     */
    @Override
    public void configure() {

        // connect to the traffic controller
        XpaSystemConnectionMemo memo = ((XpaSystemConnectionMemo)getSystemConnectionMemo());
        XpaTrafficController tc = memo.getXpaTrafficController();
        tc.connectPort(this);

        InstanceManager.store(memo.getPowerManager(), jmri.PowerManager.class);
        InstanceManager.store(memo.getTurnoutManager(),jmri.TurnoutManager.class);
        InstanceManager.store(memo.getThrottleManager(),jmri.ThrottleManager.class);
        memo.register();

        // start operation
        tc.startTransmitThread();
        sinkThread = new Thread(tc);
        sinkThread.start();
    }

    private Thread sinkThread;

    @Override
    public boolean status() {
        return opened;
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

    private final static Logger log = LoggerFactory.getLogger(SerialDriverAdapter.class);

}
