package jmri.jmrix.zimo.mx1;

import java.util.Arrays;
import jmri.jmrix.zimo.Mx1CommandStation;
import jmri.jmrix.zimo.Mx1Packetizer;
import jmri.jmrix.zimo.Mx1PortController;
import jmri.jmrix.zimo.Mx1SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide access to Zimo's MX-1 on an attached serial com port. Adapted for
 * use with Zimo MX-1 by Sip Bosch.
 *
 * @author Bob Jacobsen Copyright (C) 2002
 */
public class Mx1Adapter extends Mx1PortController {

    public Mx1Adapter() {
        super(new Mx1SystemConnectionMemo());
        option1Name = "FlowControl"; // NOI18N
        options.put(option1Name, new Option("MX-1 connection uses : ", validOption1));
        this.manufacturerName = jmri.jmrix.zimo.Mx1ConnectionTypeList.ZIMO;
    }

    @Override
    public String openPort(String portName, String appName) {
        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect ZIMO MX1 to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting ZIMO MX1 to {} {}", portName, currentSerialPort);
        
        // try to set it for communication via SerialDriver
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        setBaudRate(currentSerialPort, baud);
        configureLeads(currentSerialPort, true, true);
        // find and configure flow control
        FlowControl flow = FlowControl.RTSCTS; // default, but also defaults in selectedOption1
        if (getOptionState(option1Name).equals(validOption1[1])) {
            flow = FlowControl.NONE;
        }
        setFlowControl(currentSerialPort, flow);

        // report status
        reportPortStatus(log, portName);

        opened = true;

        return null; // indicates OK return
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
        return currentSerialPort.getCTS();
    }

    /**
     * set up all of the other objects to operate with a MX-1 connected to this
     * port
     */
    @Override
    public void configure() {
        Mx1CommandStation cs = new Mx1CommandStation(getSystemConnectionMemo().getSystemPrefix(), getSystemConnectionMemo().getUserName());
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
    public boolean status() {
        return opened;
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

    private final static Logger log = LoggerFactory.getLogger(Mx1Adapter.class);

}
