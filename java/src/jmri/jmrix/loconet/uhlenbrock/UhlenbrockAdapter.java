// LocoBufferAdapter.java
package jmri.jmrix.loconet.uhlenbrock;

import gnu.io.SerialPort;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it operates
 * correctly with the IC-COM and Intellibox II on-board USB port. Note that the
 * jmri.jmrix.loconet.intellibox package is for the first version of Uhlenbrock
 * Intellibox, whereas this package (jmri.jmrix.loconet.uhlenbrock is for the
 * Intellibox II and the IB-COM.
 * <P>
 * Since this is by definition connected to an Intellibox II or IB-COM, the
 * command station prompt is suppressed.
 *
 * @author	Alex Shepherd Copyright (C) 2004
 * @author Bob Jacobsen Copyright (C) 2005, 2010
 * @version	$Revision: 17977 $
 */
public class UhlenbrockAdapter extends LocoBufferAdapter {

    public UhlenbrockAdapter() {
        super(new UhlenbrockSystemConnectionMemo());

        // define command station options
        options.remove(option2Name);
        options.put(option2Name, new Option("Command station type:", commandStationOptions(), false));

        validSpeeds = new String[]{"19200", "38400", "57600", "115200"};
        validSpeedValues = new int[]{19200, 38400, 57600, 115200};
        configureBaudRate("115200"); //Set the default baud rate
    }

    /**
     * Set up all of the other objects to operate with a LocoBuffer connected to
     * this port.
     */
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
                mTurnoutNoRetry, mTurnoutExtraSpace);
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();
        jmri.jmrix.loconet.ActiveFlag.setActive();
    }

    /**
     * Get an array of valid baud rates.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public String[] validBaudRates() {
        return validSpeeds;
    }

    /**
     * Get an array of valid baud rates as integers.
     */
    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings(value = "EI_EXPOSE_REP") // OK to expose array instead of copy until Java 1.6
    public int[] validBaudNumber() {
        return validSpeedValues;
    }

    public boolean okToSend() {
        return true;
    }

    /**
     * Local method to do specific configuration, overridden in class
     */
    protected void setSerialPort(SerialPort activeSerialPort) throws gnu.io.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        activeSerialPort.setFlowControlMode(SerialPort.FLOWCONTROL_NONE);
        log.info("Found flow control " + activeSerialPort.getFlowControlMode()
                + " RTSCTS_OUT=" + SerialPort.FLOWCONTROL_RTSCTS_OUT
                + " RTSCTS_IN= " + SerialPort.FLOWCONTROL_RTSCTS_IN);
    }

    /**
     * Provide just one valid command station value
     */
    public String[] commandStationOptions() {
        String[] retval = {
            LnCommandStationType.COMMAND_STATION_IBX_TYPE_2.getName()
        };
        return retval;
    }

    private final static Logger log = LoggerFactory.getLogger(UhlenbrockAdapter.class.getName());

}
