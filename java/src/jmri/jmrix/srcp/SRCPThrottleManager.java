package jmri.jmrix.srcp;

import org.apache.log4j.Logger;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

/**
 * SRCP implementation of a ThrottleManager.
 * <P>
 * Based on early NCE code.
 *
 * @author	    Bob Jacobsen  Copyright (C) 2001, 2005, 2008
 * @author Modified by Kelly Loyd
 * @version         $Revision$
 */
public class SRCPThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     */
    public SRCPThrottleManager(SRCPSystemConnectionMemo memo) {
        super(memo);
    }

    public void requestThrottleSetup(LocoAddress address, boolean control) {
        log.debug("new SRCPThrottle for "+address);
        // Notify ready to go (without waiting for OK?)
        notifyThrottleKnown(new SRCPThrottle((SRCPSystemConnectionMemo)adapterMemo, (DccLocoAddress)address), address);
    }
    
    // KSL 20040409 - SRCP does not have a 'dispatch' function.
    public boolean hasDispatchFunction() { return false; }
    
    /**
     * Address 100 and above is a long address
     **/
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }
    
    /**
     * Address 99 and below is a short address
     **/
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    public boolean addressTypeUnique() { return true; }

    /*
     * Local method for deciding short/long address
     */
    static boolean isLongAddress(int num) {
        return (num>=100);
    }
    
    public int supportedSpeedModes() {
    	return(DccThrottle.SpeedStepMode128 | DccThrottle.SpeedStepMode28);
    }

    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l){
        if (super.disposeThrottle(t, l)){
        // Form a message to release the loco
            DccLocoAddress la = (DccLocoAddress) t.getLocoAddress();
            String msg = "TERM 1 GL "
                +(la.getNumber())
                +"\n";

        // and send it
            ((SRCPSystemConnectionMemo)adapterMemo).getTrafficController().sendSRCPMessage(new SRCPMessage(msg), null);
            return true;
        }
        return false;
        //LocoNetSlot tSlot = lnt.getLocoNetSlot();
    }

    static Logger log = Logger.getLogger(SRCPThrottleManager.class.getName());

}
