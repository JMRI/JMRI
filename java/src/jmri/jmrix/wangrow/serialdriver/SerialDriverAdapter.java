package jmri.jmrix.wangrow.serialdriver;

import jmri.jmrix.nce.NcePortController;
import jmri.jmrix.nce.NceSystemConnectionMemo;
import jmri.jmrix.nce.NceTrafficController;

/**
 * Implements SerialPortAdapter for the Wangrow system.
 * <p>
 * Note that this still uses a significant number of classes from the
 * {@link jmri.jmrix.nce} package.
 * <p>
 * This connects an Wangrow command station via a serial com port. Normally
 * controlled by the SerialDriverFrame class.
 * <p>
 * The current implementation only handles the 9,600 baud rate, and does not use
 * any other options at configuration time.
 *
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public class SerialDriverAdapter extends NcePortController {

    public SerialDriverAdapter() {
        super(new NceSystemConnectionMemo());
        setManufacturer(jmri.jmrix.wangrow.WangrowConnectionTypeList.WANGROW);
    }

    @Override
    public String openPort(String portName, String appName) {

        // get and open the primary port
        currentSerialPort = activatePort(portName, log);
        if (currentSerialPort == null) {
            log.error("failed to connect Wangrow to {}", portName);
            return Bundle.getMessage("SerialPortNotFound", portName);
        }
        log.info("Connecting Wangrow to {} {}", portName, currentSerialPort);
        
        // try to set it for communication via SerialDriver, configure comm options
        setBaudRate(currentSerialPort, 9600);
        configureLeads(currentSerialPort, true, true);
        setFlowControl(currentSerialPort, FlowControl.NONE);

        // report status
        reportPortStatus(log, portName);

        opened = true;

        return null; // indicates OK return
    }

    /**
     * set up all of the other objects to operate with an NCE command station
     * connected to this port
     */
    @Override
    public void configure() {
        NceTrafficController tc = new NceTrafficController();
        this.getSystemConnectionMemo().setNceTrafficController(tc);
        tc.setAdapterMemo(this.getSystemConnectionMemo());

        // set the command option
        tc.setCommandOptions(NceTrafficController.OPTION_1999);

        tc.connectPort(this);

        this.getSystemConnectionMemo().configureManagers();
    }

    // base class methods for the NcePortController interface

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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SerialDriverAdapter.class);

}
