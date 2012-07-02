package jmri.jmrix.tams;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

/**
 * TamsDCC implementation of a ThrottleManager.
 * <P>
 * Based on early NCE code.
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version         $Revision: 19121 $
 */
public class TamsThrottleManager extends AbstractThrottleManager implements TamsListener{

    /**
     * Constructor.
     */
    public TamsThrottleManager(TamsSystemConnectionMemo memo) {
        super(memo);
    }

    static private TamsThrottleManager mInstance = null;
    static public TamsThrottleManager instance() {
        return mInstance;
    }

    public void reply(TamsReply m) {
        //We are not sending commands from here yet!
   }

   public void message(TamsMessage m) {
        // messages are ignored
    }

    public void requestThrottleSetup(LocoAddress address, boolean control) {
        /*Here we do not set notifythrottle, we simply create a new Tams throttle.
        The Tams throttle in turn will notify the throttle manager of a successful or
        unsuccessful throttle connection. */
        log.debug("new TamsThrottle for "+address);
        notifyThrottleKnown(new TamsThrottle((TamsSystemConnectionMemo)adapterMemo, (DccLocoAddress)address), address);
    }

    @Override
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

    @Override
    protected boolean singleUse() { return false; }

    /*
     * Local method for deciding short/long address
     */
    static boolean isLongAddress(int num) {
        return (num>=100);
    }
    
    @Override
    public int supportedSpeedModes() {
    	return(DccThrottle.SpeedStepMode128 | DccThrottle.SpeedStepMode28);
    }
        
    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l){
        if (super.disposeThrottle(t, l)){
            TamsThrottle lnt = (TamsThrottle) t;
            lnt.throttleDispose();
            return true;
        }
        return false;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TamsThrottleManager.class.getName());

}