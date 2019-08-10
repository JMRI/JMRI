package jmri.jmrix.loconet.uhlenbrock;

import java.util.Arrays;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it operates
 * correctly with the IC-COM and Intellibox II on-board USB port. Note that the
 * jmri.jmrix.loconet.intellibox package is for the first version of Uhlenbrock
 * Intellibox, whereas this package (jmri.jmrix.loconet.uhlenbrock) is for the
 * Intellibox II and the IB-COM.
 * <p>
 * Since this is by definition connected to an Intellibox II or IB-COM, the
 * command station prompt is suppressed.
 *
 * @author Alex Shepherd Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2005, 2010
 */
public class UhlenbrockAdapter extends LocoBufferAdapter {

    public UhlenbrockAdapter() {
        super(new UhlenbrockSystemConnectionMemo());

        // define command station options
        options.remove(option2Name);
        options.put(option2Name, new Option(Bundle.getMessage("CommandStationTypeLabel"), commandStationOptions(), false));

        validSpeeds = new String[]{Bundle.getMessage("Baud19200"), Bundle.getMessage("Baud38400"),
                Bundle.getMessage("Baud57600"), Bundle.getMessage("Baud115200")};
        validSpeedValues = new int[]{19200, 38400, 57600, 115200};
        configureBaudRate(validSpeeds[3]); // Set the default baud rate (localized)
    }

    /**
     * Set up all of the other objects to operate with an IB-II connected to
     * this port.
     */
    @Override
    public void configure() {

        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        // connect to a packetizing traffic controller
        UhlenbrockPacketizer packets = new UhlenbrockPacketizer();
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
        return Arrays.copyOf(validSpeeds, validSpeeds.length);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int[] validBaudNumbers() {
        return Arrays.copyOf(validSpeedValues, validSpeedValues.length);
    }

    @Override
    public int defaultBaudIndex() {
        return 3;
    }

    @Override
    public boolean okToSend() {
        return true;
    }

    /**
     * Local method to do specific configuration, overridden in class.
     */
    @Override
    protected void setSerialPort(SerialPort activeSerialPort) throws UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        configureLeadsAndFlowControl(activeSerialPort, SerialPort.FLOWCONTROL_NONE);

        log.info("Uhlenbrock adapter"
                + (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? " set hardware flow control, mode=" : " set no flow control, mode=")
                + activeSerialPort.getFlowControlMode()
                + " RTSCTS_OUT=" + SerialPort.FLOWCONTROL_RTSCTS_OUT
                + " RTSCTS_IN=" + SerialPort.FLOWCONTROL_RTSCTS_IN);
    }

    /**
     * Provide just one valid command station value.
     */
    public String[] commandStationOptions() {
        String[] retval = {
            LnCommandStationType.COMMAND_STATION_IBX_TYPE_2.getName()
        };
        return retval;
    }

    private final static Logger log = LoggerFactory.getLogger(UhlenbrockAdapter.class);

}
