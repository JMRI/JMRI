package jmri.jmrix.loconet.usb_dcs210Plus;

import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.LnPacketizer;
import jmri.jmrix.loconet.LocoNetMessage;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Update the code in jmri.jmrix.loconet.locobuffer so that it refers to the
 * option settings for the Digitrax DCS210Plus's USB interface
 * <p>
 * Based on PR3Adapter.java
 *
 * @author Bob Jacobsen Copyright (C) 2004, 2005, 2006, 2008
 * @author B. Milhaupt Copyright (C) 2019
 */
public class UsbDcs210PlusAdapter extends LocoBufferAdapter {

    public UsbDcs210PlusAdapter() {
        super(new UsbDcs210PlusSystemConnectionMemo());

        options.remove(option2Name);
        options.put(option2Name, new Option(Bundle.getMessage("CommandStationTypeLabel"), commandStationOptions(), false));

    }

    @Override
    protected void reportOpen(String portName) {
        log.info("Connecting USB DCS210Plus via {} {}", portName, currentSerialPort);
    }

    /**
     * Set up all of the other objects to operate with a DCS210Plus USB interface connected to this
     * port. This overrides the version in loconet.locobuffer, but it has to
     * duplicate much of the functionality there, so the code is basically
     * copied.
     */
    @Override
    public void configure() {
        setCommandStationType(getOptionState(option2Name));
        setTurnoutHandling(getOptionState(option3Name));
        if (commandStationType == LnCommandStationType.COMMAND_STATION_USB_DCS210Plus_ALONE) {
            // DCS210Plus USB in standalone programmer case:
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
                    mTurnoutNoRetry, mTurnoutExtraSpace, mTranspondingAvailable, mInterrogateAtStart, mLoconetProtocolAutoDetect);  // never transponding!
            this.getSystemConnectionMemo().configureManagersPR2();
            this.getSystemConnectionMemo().getSlotManager().serviceModeReplyDelay = 500;

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
            setInterrogateOnStart(getOptionState("InterrogateOnStart"));
            setLoconetProtocolAutoDetect(getOptionState("LoconetProtocolAutoDetect"));
            // connect to a packetizing traffic controller
            LnPacketizer packets = getPacketizer(getOptionState(option4Name));
            packets.connectPort(this);

            // set traffic controller and configure command station and mangers
            this.getSystemConnectionMemo().setLnTrafficController(packets);
            // do the common manager config
            this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                    mTurnoutNoRetry, mTurnoutExtraSpace, mTranspondingAvailable, mInterrogateAtStart, mLoconetProtocolAutoDetect);

            this.getSystemConnectionMemo().configureManagersMS100();
            this.getSystemConnectionMemo().getSlotManager().serviceModeReplyDelay = 500;

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
     * The DCS210Plus USB interface can be used as a "Standalone Programmer", or with various LocoNet
     * command stations, or as an interface to a "Standalone LocoNet".  Provide those
     * options.
     *
     * @return an array of strings containing the various command station names and
     *      name(s) of modes without command stations
     */
    public String[] commandStationOptions() {
        String[] retval = new String[commandStationNames.length + 1];
        retval[0] = LnCommandStationType.COMMAND_STATION_DCS210PLUS.getName();
        retval[1] = LnCommandStationType.COMMAND_STATION_USB_DCS210Plus_ALONE.getName();
        int count = 2;
        for (String commandStationName : commandStationNames) {
            if (!commandStationName.equals(LnCommandStationType.COMMAND_STATION_DCS210PLUS.getName())) {
            // include all but COMMAND_STATION_DCS210Plus, which was forced  to
            // the front of the list (above)
                retval[count++] = commandStationName;
        }
    }
        // Note: Standalone loconet does not make sense for DCS210Plus USB interface.
        return retval;
    }

    @Override
    public UsbDcs210PlusSystemConnectionMemo getSystemConnectionMemo() {
        LocoNetSystemConnectionMemo m = super.getSystemConnectionMemo();
        if (m instanceof UsbDcs210PlusSystemConnectionMemo) {
            return (UsbDcs210PlusSystemConnectionMemo) m;
        }
        log.error("Cannot cast the system connection memo to a UsbDcs210PlusSystemConnection Memo.");
        return null;
    }

    private final static Logger log = LoggerFactory.getLogger(UsbDcs210PlusAdapter.class);
}
