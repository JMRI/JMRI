package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.lenz.XNetTrafficController;

/**
 * Extend jmri.jmrix.XNetTurnout to handle turnouts on Hornby Elite
 * connections.
 * @see jmri.jmrix.lenz.XNetTurnout for further documentation.
 *
 * @author Paul Bender Copyright (C) 2008
 */
public class EliteXNetTurnout extends jmri.jmrix.lenz.XNetTurnout {

    public EliteXNetTurnout(String prefix, int pNumber, XNetTrafficController tc) {  // a human-readable turnout number must be specified!
        super(prefix, pNumber, tc);
        mNumber = pNumber + 1;  // The Elite has an off by 1 error.  What the 
        // protocol says should be address 2 is address 
        // 1 on the Elite.
    }

    /**
     * Send an "Off" message to the decoder for this output.
     */
    @Override
    protected synchronized void sendOffMessage() {
        // The Elite appears to react to the on and off messages
        // in the same manner, and does not handle feedback properly
        // Set the known state to the command state and the internalState
        // to idlestate.
        newKnownState(getCommandedState());
        internalState = jmri.jmrix.lenz.XNetTurnout.IDLE;
    }

}
