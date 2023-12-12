package jmri.jmrix.loconet.uhlenbrock;

import java.util.Arrays;
import jmri.jmrix.loconet.LnCommandStationType;
import jmri.jmrix.loconet.locobuffer.LocoBufferAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        options.put("InterrogateOnStart", new Option(Bundle.getMessage("InterrogateOnStart"),
                new String[]{Bundle.getMessage("ButtonYes"), Bundle.getMessage("ButtonNo")} )); // NOI18N

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
        setInterrogateOnStart(getOptionState("InterrogateOnStart"));
        // connect to a packetizing traffic controller
        UhlenbrockPacketizer packets = new UhlenbrockPacketizer(this.getSystemConnectionMemo());
        packets.connectPort(this);

        // create memo
        this.getSystemConnectionMemo().setLnTrafficController(packets);
        // do the common manager config
        this.getSystemConnectionMemo().configureCommandStation(commandStationType,
                mTurnoutNoRetry, mTurnoutExtraSpace, mTranspondingAvailable, mInterrogateAtStart, false); //never Xp slots
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

    @Override
    protected void reportOpen(String portName) {
        log.info("Connecting Uhlenbrock via {} {}", portName, currentSerialPort);
    }

    /**
     * Always off flow control
     */
    @Override
    protected void setLocalFlowControl() {
        FlowControl flow = FlowControl.NONE;
        setFlowControl(currentSerialPort, flow);
    }

    /**
     * Provide just one valid command station value.
     * @return array with single entry, name of COMMAND_STATION_IBX_TYPE_2 .
     */
    public String[] commandStationOptions() {
        String[] retval = {
            LnCommandStationType.COMMAND_STATION_IBX_TYPE_2.getName()
        };
        return retval;
    }

    private final static Logger log = LoggerFactory.getLogger(UhlenbrockAdapter.class);

}
