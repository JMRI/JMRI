package jmri.jmrix.mrc.serialdriver;

import jmri.jmrix.SerialPort;
import jmri.jmrix.mrc.MrcPacketizer;
import jmri.jmrix.mrc.MrcPortController;
import jmri.jmrix.mrc.MrcSystemConnectionMemo;

/**
 * Implements SerialPortAdapter for the MRC system. This connects an MRC command
 * station via a serial com port. Normally controlled by the SerialDriverFrame
 * class.
 * <p>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002, 2023
 */
public class SerialDriverAdapter extends MrcPortController {

    public SerialDriverAdapter() {
        super(new MrcSystemConnectionMemo());
        setManufacturer(jmri.jmrix.mrc.MrcConnectionTypeList.MRC);
        options.put("CabAddress", new Option("Cab Address:", validOption1, false)); // NOI18N
    }

    @Override
    public String openPort(String portName, String appName) {
        // get and open the primary port
        currentSerialPort = activatePort(this.getSystemPrefix(), portName, log, 1, SerialPort.Parity.ODD);
        if (currentSerialPort == null) {
            log.error("failed to connect MRC to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting MRC to {} {}", portName, currentSerialPort);

        // try to set it for communication via SerialDriver
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(getCurrentBaudRate());
        setBaudRate(currentSerialPort, baud);
        configureLeads(currentSerialPort, true, true);
        setFlowControl(currentSerialPort, FlowControl.NONE);

        // report status
        reportPortStatus(log, portName);

        opened = true;

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
        packets.setCabNumber(Integer.parseInt(getOptionState("CabAddress")));// NOI18N

        this.getSystemConnectionMemo().configureManagers();

        packets.startThreads();
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
        return new String[]{"38,400 bps"}; // NOI18N
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

    protected String[] validOption1 = new String[]{"01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"};// NOI18N

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
