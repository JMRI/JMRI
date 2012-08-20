package jmri.jmrix.ecos;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.DccLocoAddress;

import jmri.jmrix.AbstractThrottleManager;

/**
 * EcosDCC implementation of a ThrottleManager.
 * <P>
 * Based on early NCE code.
 *
 * @author	    Bob Jacobsen  Copyright (C) 2001, 2005
 * @author Modified by Kevin Dickerson
 * @version         $Revision$
 */
public class EcosDccThrottleManager extends AbstractThrottleManager implements EcosListener{

    /**
     * Constructor.
     */
    public EcosDccThrottleManager(EcosSystemConnectionMemo memo) {
        super(memo);
    }

    static private EcosDccThrottleManager mInstance = null;
    static public EcosDccThrottleManager instance() {
        return mInstance;
    }

    public void reply(EcosReply m) {
        //We are not sending commands from here yet!
   }

   public void message(EcosMessage m) {
        // messages are ignored
    }

    public void requestThrottleSetup(LocoAddress address, boolean control) {
        /*Here we do not set notifythrottle, we simply create a new ecos throttle.
        The ecos throttle in turn will notify the throttle manager of a successful or
        unsuccessful throttle connection. */
        log.debug("new EcosDccThrottle for "+address);
        new EcosDccThrottle((DccLocoAddress)address, (EcosSystemConnectionMemo)adapterMemo, control);
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
    
    public String[] getAddressTypes(){
        return new String[]{rb.getString("ComboItemDCC"),
                         rb.getString("ComboItemMotorola"),
                         rb.getString("ComboItemSelectrix"),
                         rb.getString("ComboItemMFX")};
    }
    
    public int[] getAddressIntTypes(){
        return new int[]{LocoAddress.DCC, LocoAddress.MOTOROLA, LocoAddress.SELECTRIX, LocoAddress.MFX};
    }
    
    public String getAddressTypeString(int prot){
        switch(prot){
            case LocoAddress.MOTOROLA: return rb.getString("ComboItemMotorola");
            case LocoAddress.SELECTRIX: return rb.getString("ComboItemSelectrix");
            case LocoAddress.MFX: return rb.getString("ComboItemMFX");
            default: return rb.getString("ComboItemDCC");
        }
    }
    
    public int getProtocolFromString(String selection){
        int val = LocoAddress.DCC;
        if (selection.equals(rb.getString("ComboItemDCC"))){
            val = LocoAddress.DCC;
        } else if (selection.equals(rb.getString("ComboItemMotorola"))){
            val = LocoAddress.MOTOROLA;
        } else if (selection.equals(rb.getString("ComboItemSelectrix"))){
            val = LocoAddress.SELECTRIX;
        } else if (selection.equals(rb.getString("ComboItemMFX"))){
            val = LocoAddress.MFX;
        } else {
            log.error("Protocol '" + selection + "' is unknown so will default to dcc");
        }
        return val;
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
        
    public void throttleSetup(EcosDccThrottle throttle, LocoAddress address, boolean result){
        /* this is called by the ecosdccthrottle, to inform the manager if it has successfully gained
        control of a loco, when setting up the throttle.*/
        if (result){
            log.debug("Ecos Throttle has control over loco "+address);
            notifyThrottleKnown(throttle, address);
        }
        else {
            log.debug("Ecos Throttle has NO control over loco "+address);
            failedThrottleRequest((DccLocoAddress) address, "Loco is alredy in use by anoher throttle " + address);
        }
    }

    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l){
        if (super.disposeThrottle(t, l)){
            EcosDccThrottle lnt = (EcosDccThrottle) t;
            lnt.throttleDispose();
            return true;
        }
        return false;
        //LocoNetSlot tSlot = lnt.getLocoNetSlot();
    }
    
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(EcosDccThrottleManager.class.getName());

}
