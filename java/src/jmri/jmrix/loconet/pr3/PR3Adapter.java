package jmri.jmrix.loconet.pr3;

import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import purejavacomm.SerialPort;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it refers to the
 * switch settings on the new Digitrax PR3
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2005, 2006, 2008
  */
public class PR3Adapter extends LocoBufferAdapter {

    public PR3Adapter() {
        super(new PR3SystemConnectionMemo());

        options.remove(option2Name);
        options.put(option2Name, new Option(Bundle.getMessage("CommandStationTypeLabel"), commandStationOptions(), false));
    }

    /**
     * Sets up the serial port characteristics.  Always uses flow control, which is
     * not considered a user-settable option.  Sets the PR3 for the appropriate
     * operating mode, based on the selected "command station type".
     *
     * @param activeSerialPort - the port to be configured
     */
    @Override
    protected void setSerialPort(SerialPort activeSerialPort) throws UnsupportedCommOperationException {
        // find the baud rate value, configure comm options
        int baud = 57600;  // default, but also defaulted in the initial value of selectedSpeed
        for (int i = 0; i < validBaudNumber().length; i++) {
            if (validBaudRates()[i].equals(mBaudRate)) {
                baud = validBaudNumber()[i];
            }
        }
        activeSerialPort.setSerialPortParams(baud, SerialPort.DATABITS_8,
                SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

        // configure flow control to always on
        int flow = SerialPort.FLOWCONTROL_RTSCTS_OUT;
        if (getOptionState(option1Name).equals(validOption1[1])) {
            flow = SerialPort.FLOWCONTROL_NONE;
        }
        configureLeadsAndFlowControl(activeSerialPort, flow);

        log.info("PR3 adapter"
                +(activeSerialPort.getFlowControlMode() == SerialPort.FLOWCONTROL_RTSCTS_OUT ? " set hardware flow control, mode=" : " set no flow control, mode=")
                +activeSerialPort.getFlowControlMode()
                + " RTSCTS_OUT=" + SerialPort.FLOWCONTROL_RTSCTS_OUT
                + " RTSCTS_IN=" + SerialPort.FLOWCONTROL_RTSCTS_IN);
    }

    /**
     * Set up all of the other objects to operate with a PR3 connected to this
     * port. This overrides the version in loconet.locobuffer, but it has to
     * duplicate much of the functionality there, so the code is basically
     * copied.
     */
    @Override
    public void configure() {
        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        if (commandStationType == LnCommandStationType.COMMAND_STATION_PR3_ALONE) {
            // PR3 standalone case
            // connect to a packetizing traffic controller
            // that does echoing
            jmri.jmrix.loconet.pr2.LnPr2Packetizer packets = new jmri.jmrix.loconet.pr2.LnPr2Packetizer();
            packets.connectPort(this);

            // create memo
            /*PR3SystemConnectionMemo memo
             = new PR3SystemConnectionMemo(packets, new SlotManager(packets));*/
            this.getSystemConnectionMemo().setLnTrafficController(packets);
            // do the common manager config
            this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                    mTurnoutNoRetry, mTurnoutExtraSpace);
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
            // connect to a packetizing traffic controller
            LnPacketizer packets = new LnPacketizer();
            packets.connectPort(this);

            // create memo
            /*PR3SystemConnectionMemo memo
             = new PR3SystemConnectionMemo(packets, new SlotManager(packets));*/
            this.getSystemConnectionMemo().setLnTrafficController(packets);
            // do the common manager config
            this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                    mTurnoutNoRetry, mTurnoutExtraSpace);

            this.getSystemConnectionMemo().configureManagersMS100();

            // start operation
            packets.startThreads();

            // set mode
            LocoNetMessage msg = new LocoNetMessage(6);
            msg.setOpCode(0xD3);
            msg.setElement(1, 0x10);
            msg.setElement(2, 0);  // set MS100, no power
            if (commandStationType == LnCommandStationType.COMMAND_STATION_STANDALONE) {
                msg.setElement(2, 3);  // set MS100, with power
            }
            msg.setElement(3, 0);
            msg.setElement(4, 0);
            packets.sendLocoNetMessage(msg);
        }
    }

    /**
     * Get an array of valid baud rates.
     *
     * @return String[] containing the single valid baud rate, "57,600".
     */
    @Override
    public String[] validBaudRates() {
        return new String[]{"57,600 baud"}; // NOI18N
    }

    /**
     * Get an array of valid baud rates as integers. This allows subclasses to
     * change the arrays of speeds.
     * @return int[] containing the single valud baud rate, 57600.
     */
    @Override
    public int[] validBaudNumber() {
        return new int[]{57600};
    }

    // Option 1 does flow control, inherited from LocoBufferAdapter

    /**
     * The PR3 can be used as a "Standalone Programmer", or with various LocoNet
     * command stations, or as an interface to a "Standalone LocoNet".  Provide those
     * options.
     *
     * @return an array of strings containing the various command station names and
     *      name(s) of modes without command stations
     */
    public String[] commandStationOptions() {
        String[] retval = new String[commandStationNames.length + 2];
        retval[0] = LnCommandStationType.COMMAND_STATION_PR3_ALONE.getName();
        for (int i = 0; i < commandStationNames.length; i++) {
            retval[i + 1] = commandStationNames[i];
        }
        retval[retval.length - 1] = LnCommandStationType.COMMAND_STATION_STANDALONE.getName();
        return retval;
    }

    @Override
    public PR3SystemConnectionMemo getSystemConnectionMemo() {
        if (super.getSystemConnectionMemo() instanceof PR3SystemConnectionMemo) {
            return (PR3SystemConnectionMemo) super.getSystemConnectionMemo();
        } else {
            log.error("Cannot cast the system connection memo to a PR3SystemConnection Memo.");
            return null;
        }
    }

    private final static Logger log = LoggerFactory.getLogger(PR3Adapter.class);
}
