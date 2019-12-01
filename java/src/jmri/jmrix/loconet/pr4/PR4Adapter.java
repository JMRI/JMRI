package jmri.jmrix.loconet.pr4;

import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Override {@link jmri.jmrix.loconet.locobuffer.LocoBufferAdapter} so that it refers to the
 * (switch) settings on the Digitrax PR4.
 * <p>
 * Based on PR3Adapter.java
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2005, 2006, 2008
 * @author B. Milhaupt Copyright (C) 2019
 */
public class PR4Adapter extends LocoBufferAdapter {

    public PR4Adapter() {
        super(new PR4SystemConnectionMemo());

        options.remove(option2Name);
        options.put(option2Name, new Option(Bundle.getMessage("CommandStationTypeLabel"), commandStationOptions(), false));

    }

    /**
     * Sets up the serial port characteristics.  Always uses flow control, which is
     * not considered a user-settable option.  Sets the PR4 for the appropriate
     * operating mode, based on the selected "command station type".
     *
     * @param activeSerialPort  the port to be configured
     */
    @Override
    protected void setSerialPort(SerialPort activeSerialPort) throws UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = currentBaudNumber(mBaudRate);
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // configure flow control to always on
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        if (getOptionState(option1Name).equals(validOption1[1])) {
            flow = SerialPort.FLOWCONTROL_NONE;
        }
        configureLeadsAndFlowControl(activeSerialPort, flow);

        log.info("PR4 adapter"
                + (activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? " set hardware flow control, mode=" : " set no flow control, mode=")
                + activeSerialPort.getFlowControlMode()
                + " RTSCTS_OUT=" + SerialPort.FLOWCONTROL_RTSCTS_OUT
                + " RTSCTS_IN=" + SerialPort.FLOWCONTROL_RTSCTS_IN);
    }

    /**
     * Set up all of the other objects to operate with a PR4 connected to this
     * port. This overrides the version in loconet.locobuffer, but it has to
     * duplicate much of the functionality there, so the code is basically
     * copied.
     * 
     * Note that the PR4 does not support "LocoNet Data Signal termination" when
     * in LocoNet interface mode (i.e. MS100 mode).
     */
    @Override
    public void configure() {
        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        if (commandStationType == LnCommandStationType.COMMAND_STATION_PR4_ALONE) {
            // PR4 standalone case
            // connect to a packetizing traffic controller
            // that does echoing
            //
            // Note - already created a LocoNetSystemConnectionMemo, so re-use
            // it when creating a PR2 Packetizer.  (If create a new one, will
            // end up with two "LocoNet" menus...)
            jmri.jmrix.loconet.pr2.LnPr2Packetizer packets =
                    new jmri.jmrix.loconet.pr2.LnPr2Packetizer(this.getSystemConnectionMemo());
            packets.connectPort(this);

            // set traffic controller and configure command station and mangers
            this.getSystemConnectionMemo().setLnTrafficController(packets);
            // do the common manager config
            this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                    mTurnoutNoRetry, mTurnoutExtraSpace, mTranspondingAvailable);  // never transponding!
            this.getSystemConnectionMemo().configureManagersPR2();

            // start operation
            packets.startThreads();

            // set mode
            LocoNetMessage msg = new LocoNetMessage(6);
            msg.setOpCode(0xD3);
            msg.setElement(1, 0x10);
            msg.setElement(2, 1);  // set PR2
            msg.setElement(3, 0);
            msg.setElement(4, 0);
            packets.sendLocoNetMessage(msg);

        } else {
            // MS100 modes - connecting to a separate command station
            // get transponding option
            setTranspondingAvailable(getOptionState("TranspondingPresent"));
            // connect to a packetizing traffic controller
            LnPacketizer packets = getPacketizer(getOptionState(option4Name));
            packets.connectPort(this);

            // set traffic controller and configure command station and mangers
            this.getSystemConnectionMemo().setLnTrafficController(packets);
            // do the common manager config
            this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                    mTurnoutNoRetry, mTurnoutExtraSpace, mTranspondingAvailable);

            this.getSystemConnectionMemo().configureManagersMS100();

            // start operation
            packets.startThreads();

            // set mode
            LocoNetMessage msg = new LocoNetMessage(6);
            msg.setOpCode(0xD3);
            msg.setElement(1, 0x10);
            msg.setElement(2, 0);  // set MS100, no power
            msg.setElement(3, 0);
            msg.setElement(4, 0);
            packets.sendLocoNetMessage(msg);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return String[] containing the single valid baud rate, "57,600".
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"57,600 baud"}; // NOI18N
    }

    /**
     * {@inheritDoc}
     *
     * @return int[] containing the single valid baud rate, 57600.
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
     * The PR4 can be used as a "Standalone Programmer", or with various LocoNet
     * command stations.  The PR4 does not support "LocoNet Data signal termination",
     * so that is not added as a valid option (as it would be for the PR3).
     *
     * @return an array of strings containing the various command station names and
     *      name(s) of modes without command stations
     */
    public String[] commandStationOptions() {
        String[] retval = new String[commandStationNames.length + 1];
        retval[0] = LnCommandStationType.COMMAND_STATION_PR4_ALONE.getName();
        for (int i = 0; i < commandStationNames.length; i++) {
            retval[i + 1] = commandStationNames[i];
        }
        return retval;
    }

    @Override
    public PR4SystemConnectionMemo getSystemConnectionMemo() {
        LocoNetSystemConnectionMemo m = super.getSystemConnectionMemo();
        if (m instanceof PR4SystemConnectionMemo) {
            return (PR4SystemConnectionMemo) m;
        }
        log.error("Cannot cast the system connection memo to a PR4SystemConnection Memo.");
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(PR4Adapter.class);
}
