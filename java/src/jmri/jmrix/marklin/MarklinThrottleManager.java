package jmri.jmrix.marklin;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

/**
 * MarklinDCC implementation of a ThrottleManager.
 * <P>
 * Based on early NCE code.
 
 *
 * Based on work by Bob Jacobsen
 * @author	Kevin Dickerson  Copyright (C) 2012
 * @version         $Revision: 19121 $
 */
public class MarklinThrottleManager extends AbstractThrottleManager implements MarklinListener{

    /**
     * Constructor.
     */
    public MarklinThrottleManager(MarklinSystemConnectionMemo memo) {
        super(memo);
    }

    static private MarklinThrottleManager mInstance = null;
    static public MarklinThrottleManager instance() {
        return mInstance;
    }

    public void reply(MarklinReply m) {
        //We are not sending commands from here yet!
   }

   public void message(MarklinMessage m) {
        // messages are ignored
    }

    public void requestThrottleSetup(LocoAddress address, boolean control) {
        /*Here we do not set notifythrottle, we simply create a new Marklin throttle.
        The Marklin throttle in turn will notify the throttle manager of a successful or
        unsuccessful throttle connection. */
        log.debug("new MarklinThrottle for "+address);
        notifyThrottleKnown(new MarklinThrottle((MarklinSystemConnectionMemo)adapterMemo, (DccLocoAddress)address), address);
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
    public boolean addressTypeUnique() { return false; }
    
    @Override
    protected boolean singleUse() { return false; }
    
    public String[] getAddressTypes(){
        return new String[]{
                        LocoAddress.Protocol.DCC.getPeopleName(),
                        LocoAddress.Protocol.MFX.getPeopleName(),
                        LocoAddress.Protocol.MOTOROLA.getPeopleName()};
    }

    public LocoAddress.Protocol[] getAddressProtocolTypes(){
        return new LocoAddress.Protocol[]{
                    LocoAddress.Protocol.DCC, 
                    LocoAddress.Protocol.MFX, 
                    LocoAddress.Protocol.MOTOROLA};
    }

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
            MarklinThrottle lnt = (MarklinThrottle) t;
            lnt.throttleDispose();
            return true;
        }
        return false;
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MarklinThrottleManager.class.getName());

}