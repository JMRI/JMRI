package jmri.jmrix.loconet.pr2;

import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it refers to the
 * switch settings on the new Digitrax PR2
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2005, 2006
 */
public class PR2Adapter extends LocoBufferAdapter {

    public PR2Adapter() {
        super(new PR2SystemConnectionMemo());

        options.remove(option2Name);
        options.put(option2Name, new Option(Bundle.getMessage("CommandStationTypeLabel"), commandStationOptions(), false));
    }

    /**
     * Always use flow control, not considered a user-settable option
     */
    @Override
    protected void setSerialPort(SerialPort activeSerialPort) throws UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // configure flow control from option
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        if (getOptionState(option1Name).equals(validOption1[1])) {
            flow = SerialPort.FLOWCONTROL_NONE;
        }
        configureLeadsAndFlowControl(activeSerialPort, flow);
        log.info("PR2 adapter"
                + (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? " set hardware flow control, mode=" : " set no flow control, mode=")
                + activeSerialPort.getFlowControlMode()
                + " RTSCTS_OUT=" + SerialPort.FLOWCONTROL_RTSCTS_OUT
                + " RTSCTS_IN=" + SerialPort.FLOWCONTROL_RTSCTS_IN);
    }

    /**
     * Set up all of the other objects to operate with a PR2 connected to this
     * port. This overrides the version in loconet.locobuffer, but it has to
     * duplicate much of the functionality there, so the code is basically
     * copied.
     */
    @Override
    public void configure() {

        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));

        // connect to a packetizing traffic controller
        // that does echoing
        jmri.jmrix.loconet.pr2.LnPr2Packetizer packets = new jmri.jmrix.loconet.pr2.LnPr2Packetizer();
        packets.connectPort(this);

        // create memo
        this.getSystemConnectionMemo().setLnTrafficController(packets);
        // do the common manager config
        this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                mTurnoutNoRetry, mTurnoutExtraSpace, mTranspondingAvailable);
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"57,600 baud"}; // NOI18N
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return new int[]{57600};
    }

    @Override
    public int defaultBaudIndex() {
        return 0;
    }

    // Option 1 does flow control, inherited from LocoBufferAdapter

    /**
     * The PR2 has one mode
     * @return a String[] containing appropriate options
     */
    public String[] commandStationOptions() {
        return new String[]{jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_PR2_ALONE.getName()};
    }

    private final static Logger log = LoggerFactory.getLogger(PR2Adapter.class);
}
