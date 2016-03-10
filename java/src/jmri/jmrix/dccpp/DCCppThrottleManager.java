package jmri.jmrix.dccpp;

import java.util.HashMap;
import jmri.LocoAddress;
import jmri.ThrottleManager;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCC++ implementation of a ThrottleManager based on the
 * AbstractThrottleManager.
 *
 * @author Paul Bender Copyright (C) 2002-2004
 * @author Mark Underwood Copyright (C) 2015
 * @version $Revision$
 *
 * Based on XNetThrottleManager by Paul Bender
 */
public class DCCppThrottleManager extends AbstractThrottleManager implements ThrottleManager, DCCppListener {

    protected HashMap<LocoAddress, DCCppThrottle> throttles = new HashMap<LocoAddress, DCCppThrottle>(5);

    protected DCCppTrafficController tc = null;

    /**
     * Constructor.
     */
    public DCCppThrottleManager(DCCppSystemConnectionMemo memo) {
        super(memo);
        // connect to the TrafficManager
        tc = memo.getDCCppTrafficController();

        // Register to listen for throttle messages
        tc.addDCCppListener(DCCppInterface.THROTTLE, this);
    }

    /**
     * Request a new throttle object be created for the address, and let the
     * throttle listeners know about it.
     *
     */
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        DCCppThrottle throttle;
        if (log.isDebugEnabled()) {
            log.debug("Requesting Throttle: " + address);
        }
        if (throttles.containsKey(address)) {
            notifyThrottleKnown(throttles.get(address), address);
        } else {
	    if (tc.getCommandStation().requestNewRegister(address.getNumber()) == DCCppConstants.NO_REGISTER_FREE) {
		// TODO: Eventually add something more robust here.
		log.error("No Register available for Throttle. Address = {}", address);
		return;
	    }
            throttle = new DCCppThrottle((DCCppSystemConnectionMemo) adapterMemo, address, tc);
            throttles.put(address, throttle);
            notifyThrottleKnown(throttle, address);
        }
    }

    /*
     * DCC++ based systems DO NOT use the Dispatch Function
     * (do they?)
     */
    public boolean hasDispatchFunction() {
        return false;
    }

    /*
     * DCC++ based systems can have multiple throttles for the same 
     * device
     */
    protected boolean singleUse() {
        return false;
    }

    /**
     * Address 128 and above is a long address
     *
     */
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }

    /**
     * Address 127 and below is a short address
     *
     */
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * There are no ambiguous addresses on this system.
     */
    public boolean addressTypeUnique() {
        return true;
    }

    /*
     * Local method for deciding short/long address
     * (is it?)
     */
    static protected boolean isLongAddress(int num) {
        return (num >= 128);
    }

    /**
     * What speed modes are supported by this system? value should be xor of
     * possible modes specifed by the DccThrottle interface XPressNet supports
     * 14,27,28 and 128 speed step modes
     */
    public int supportedSpeedModes() {
        return (jmri.DccThrottle.SpeedStepMode128); }

    // Handle incoming messages for throttles.
    public void message(DCCppReply r) {
	// Guts of how a throttle handles replies...
	//
	// What should this be??
	// For now, drop the message.
	/*
        // We want to check to see if a throttle has taken over an address
        if (r.getElement(0) == DCCppConstants.LOCO_INFO_RESPONSE) {
            if (r.getElement(1) == DCCppConstants.LOCO_NOT_AVAILABLE) {
                // This is a take over message.  If we know about this throttle,
                // send the message on.
                LocoAddress address = new jmri.DccLocoAddress(r.getThrottleMsgAddr(),
                        isLongAddress(r.getThrottleMsgAddr()));
                if (throttles.containsKey(address)) {
                    throttles.get(address).message(r);
                }
            }
        }
	*/

    }

    // listen for the messages to the LI100/LI101
    public void message(DCCppMessage l) {
    }

    // Handle a timeout notification
    public void notifyTimeout(DCCppMessage msg) {
    }

    public void releaseThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
    }

    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
	    tc.getCommandStation().releaseRegister(t.getLocoAddress().getNumber());
            DCCppThrottle lnt = (DCCppThrottle) t;
            lnt.throttleDispose();
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppThrottleManager.class.getName());

}
