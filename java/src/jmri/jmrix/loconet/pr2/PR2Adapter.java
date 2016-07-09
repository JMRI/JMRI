// PR2Adapter.java
package jmri.jmrix.loconet.pr2;

import gnu.io.SerialPort;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it refers to the
 * switch settings on the new Digitrax PR2
 *
 * @author	Bob Jacobsen Copyright (C) 2004, 2005, 2006
 * @version	$Revision$
 */
public class PR2Adapter extends LocoBufferAdapter {

    public PR2Adapter() {
        super(new PR2SystemConnectionMemo());

        options.remove(option2Name);
        options.put(option2Name, new Option("Command station type:", commandStationOptions(), false));
    }

    /**
     * Always use flow control, not considered a user-setable option
     */
    protected void setSerialPort(SerialPort activeSerialPort) throws gnu.io.UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = 57600;  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i < validBaudNumber().length; i++) {
            if (validBaudRates()[i].equals(mBaudRate)) {
                baud = validBaudNumber()[i];
            }
        }
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // set RTS high, DTR high - done early, so flow control can be configured after
        activeSerialPort.setRTS(true);		// not connected in some serial ports and adapters
        activeSerialPort.setDTR(true);		// pin 1 in Mac DIN8; on main connector, this is DTR

        // configure flow control from option
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        if (getOptionState(option1Name).equals(validOption1[1])) {
            flow = SerialPort.FLOWCONTROL_NONE;
        }
        activeSerialPort.setFlowControlMode(flow);
        log.debug("Found flow control " + activeSerialPort.getFlowControlMode()
                + " RTSCTS_OUT=" + SerialPort.FLOWCONTROL_RTSCTS_OUT
                + " RTSCTS_IN= " + SerialPort.FLOWCONTROL_RTSCTS_IN);
    }

    /**
     * Set up all of the other objects to operate with a PR2 connected to this
     * port. This overrides the version in loconet.locobuffer, but it has to
     * duplicate much of the functionality there, so the code is basically
     * copied.
     */
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
                mTurnoutNoRetry, mTurnoutExtraSpace);
        this.getSystemConnectionMemo().configureManagers();

        // start operation
        packets.startThreads();

    }

    /**
     * Get an array of valid baud rates.
     */
    public String[] validBaudRates() {
        return new String[]{"57,600 baud"};
    }

    /**
     * Get an array of valid baud rates as integers. This allows subclasses to
     * change the arrays of speeds.
     */
    public int[] validBaudNumber() {
        return new int[]{57600};
    }

    // Option 1 does flow control, inherited from LocoBufferAdapter

    /**
     * The PR2 has one mode
     */
    public String[] commandStationOptions() {
        return new String[]{jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_PR2_ALONE.getName()};
    }

    private final static Logger log = LoggerFactory.getLogger(PR2Adapter.class.getName());
}
