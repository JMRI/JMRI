package jmri.jmrix.loconet;

import javax.annotation.Nonnull;

/**
 * Manage the LocoNet-specific Sensor implementation.
 * System names are "LSnnn", where L is the user configurable system prefix,
 * nnn is the sensor number without padding.  Valid sensor numbers are in the
 * range 1 to 2048, inclusive.
 *
 * Provides a mechanism to perform the LocoNet "Interrogate" process in order
 * to get initial values from those LocoNet devices which support the process
 * and provide LocoNet Sensor (and/or LocoNet Turnout) functionality.
 *
 * @author Bob Jacobsen      Copyright (C) 2001
 * @author B. Milhaupt       Copyright (C) 2020
 * @author Daniel Bergqvist  Copyright (C) 2021
 */
public class LnStringIOManager extends jmri.managers.AbstractStringIOManager implements LocoNetListener {

    protected final LnTrafficController tc;

    public LnStringIOManager(LocoNetSystemConnectionMemo memo) {
        super(memo);
        tc = memo.getLnTrafficController();
        if (tc == null) {
            log.error("LnStringIOManager Created, yet there is no Traffic Controller");
//            return;   // The return statement is not needed now since we don't register a listener.
        }

        // Don't register a listener now. But keep this line in case it's
        // neede later.
        // ctor has to register for LocoNet events
//        tc.addLocoNetListener(~0, this);
    }

    /** {@inheritDoc} */
    @Override
    @Nonnull
    public LocoNetSystemConnectionMemo getMemo() {
        return (LocoNetSystemConnectionMemo) memo;
    }

    // to free resources when no longer used
    /** {@inheritDoc} */
    @Override
    public void dispose() {
        tc.removeLocoNetListener(~0, this);
        super.dispose();
    }

    // LocoNet-specific methods

    /**
     * Listen for sensor messages, creating them as needed.
     * @param l LocoNet message to be examined
     */
    @Override
    public void message(LocoNetMessage l) {
//        switch (l.getOpCode()) {
//            case LnConstants.OPC_INPUT_REP:                /* page 9 of LocoNet PE */
//                break;
//            //$FALL-THROUGH$
//            default:  // here we didn't find an interesting command
//                return;
//        }
        // reach here for LocoNet sensor input command; make sure we know about this one
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnStringIOManager.class);

}
