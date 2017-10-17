package jmri.jmrix.loconet;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base for classes representing a LocoNet communications port
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2002
 */
public abstract class LnPortController extends jmri.jmrix.AbstractSerialPortController {
    // base class. Implementations will provide InputStream and OutputStream
    // objects to LnTrafficController classes, who in turn will deal in messages.

    protected LnPortController(LocoNetSystemConnectionMemo connectionMemo) {
        super(connectionMemo);
        setManufacturer(LnConnectionTypeList.DIGITRAX);
    }

    // returns the InputStream from the port
    @Override
    public abstract DataInputStream getInputStream();

    // returns the outputStream to the port
    @Override
    public abstract DataOutputStream getOutputStream();

    /**
     * Check that this object is ready to operate. This is a question of
     * configuration, not transient hardware status.
     */
    @Override
    public abstract boolean status();

    /**
     * Can the port accept additional characters? This might go false for short
     * intervals, but it might also stick off if something goes wrong.
     * <P>
     * Provide a default implementation for the MS100, etc, in which this is
     * _always_ true, as we rely on the queueing in the port itself.
     */
    public boolean okToSend() {
        return true;
    }

    protected LnCommandStationType commandStationType = null;

    protected boolean mTurnoutNoRetry = false;
    protected boolean mTurnoutExtraSpace = false;

    protected LnCommandStationType[] commandStationTypes = {
        LnCommandStationType.COMMAND_STATION_DCS100,
        LnCommandStationType.COMMAND_STATION_DCS240,
        LnCommandStationType.COMMAND_STATION_DCS210,
        LnCommandStationType.COMMAND_STATION_DCS200,
        LnCommandStationType.COMMAND_STATION_DCS050,
        LnCommandStationType.COMMAND_STATION_DCS051,
        LnCommandStationType.COMMAND_STATION_DB150,
        LnCommandStationType.COMMAND_STATION_IBX_TYPE_1,
        LnCommandStationType.COMMAND_STATION_IBX_TYPE_2,
        LnCommandStationType.COMMAND_STATION_LBPS,
        LnCommandStationType.COMMAND_STATION_MM};

    protected String[] commandStationNames;

    {
        commandStationNames = new String[commandStationTypes.length];
        int i = 0;
        for (LnCommandStationType type : commandStationTypes) {
            commandStationNames[i++] = type.getName();
        }
    }

    // There are also "PR3 standalone programmer" and "Stand-alone LocoNet" in pr3/PR3Adapter
    //  and "PR2 standalone programmer" in pr2/Pr2Adaper
    /**
     * Set config info from a name, which needs to be one of the valid ones.
     */
    public void setCommandStationType(String name) {
        setCommandStationType(LnCommandStationType.getByName(name));
    }

    /**
     * Set config info from the command station type enum.
     */
    public void setCommandStationType(LnCommandStationType value) {
        if (value == null) {
            return;  // can happen while switching protocols
        }
        log.debug("setCommandStationType: " + value); // NOI18N
        commandStationType = value;
    }

    public void setTurnoutHandling(String value) {
        if (value.equals("One Only") || value.equals("Both")) { // NOI18N
            mTurnoutNoRetry = true;
        }
        if (value.equals("Spread") || value.equals("Both")) { // NOI18N
            mTurnoutExtraSpace = true;
        }
        log.debug("turnout no retry: " + mTurnoutNoRetry); // NOI18N
        log.debug("turnout extra space: " + mTurnoutExtraSpace); // NOI18N
    }

    @Override
    public LocoNetSystemConnectionMemo getSystemConnectionMemo() {
        return (LocoNetSystemConnectionMemo) super.getSystemConnectionMemo();
    }
    private final static Logger log = LoggerFactory.getLogger(LnPortController.class);
}
