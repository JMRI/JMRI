package jmri.jmrix.dccpp;

import java.util.EnumSet;
import java.util.HashMap;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.ThrottleListener;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCC++ implementation of a ThrottleManager based on the
 * AbstractThrottleManager.
 *
 * @author Paul Bender Copyright (C) 2002-2004
 * @author Mark Underwood Copyright (C) 2015
 *
 * Based on XNetThrottleManager by Paul Bender
 */
public class DCCppThrottleManager extends AbstractThrottleManager implements DCCppListener {

    protected HashMap<LocoAddress, DCCppThrottle> throttles = new HashMap<LocoAddress, DCCppThrottle>(5);

    protected DCCppTrafficController tc;

    /**
     * Constructor.
     * @param memo the memo for the connection this tm will use
     */
    public DCCppThrottleManager(DCCppSystemConnectionMemo memo) {
        super(memo);
        DCCppMessage msg;
        // connect to the TrafficController manager
        tc = memo.getDCCppTrafficController();

        // Register to listen for throttle messages
        tc.addDCCppListener(DCCppInterface.THROTTLE, this);
        //Request number of available slots
        msg = DCCppMessage.makeCSMaxNumSlotsMsg();
        tc.sendDCCppMessage(msg, this);
    }

    /**
     * Request a new throttle object be created for the address, and let the
     * throttle listeners know about it.
     *
     */
    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        DCCppThrottle throttle;
        log.debug("Requesting Throttle: {}", address);
        if (throttles.containsKey(address)) {
            notifyThrottleKnown(throttles.get(address), address);
        } else {
            if (tc.getCommandStation().requestNewRegister(address.getNumber()) == DCCppConstants.NO_REGISTER_FREE) {
                failedThrottleRequest(address, "No Register available for Throttle. Address="+ address);
                log.error("No Register available for Throttle. Address = {}", address);
                return;
            }
            throttle = new DCCppThrottle((DCCppSystemConnectionMemo) adapterMemo, address, tc);
            throttles.put(address, throttle);
            notifyThrottleKnown(throttle, address);
        }
    }

    /**
     * DCC++ based systems DO NOT use the Dispatch Function
     * (do they?)
     */
    @Override
    public boolean hasDispatchFunction() {
        return false;
    }

    /**
     * DCC++ based systems can have multiple throttles for the same 
     * device
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected boolean singleUse() {
        return false;
    }

    /**
     * Address 128 and above is a long address
     *
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }

    /**
     * Address between 1 and 127 is a short address
     *
     */
    @Override
    public boolean canBeShortAddress(int address) {
        return (address >= 1 && !isLongAddress(address));
    }

    /**
     * There are no ambiguous addresses on this system.
     */
    @Override
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
     * possible modes specifed by the DccThrottle interface DCC++ supports
     * 14,27,28 and 128 speed step modes
     */
    @Override
    public EnumSet<SpeedStepMode> supportedSpeedModes() {
        return EnumSet.of(SpeedStepMode.NMRA_DCC_128); }

    // Handle incoming messages for throttles.
    @Override
    public void message(DCCppReply r) {
        // handle maxNumSlots and set value in commandstation
        if (r.getElement(0) == DCCppConstants.MAXNUMSLOTS_REPLY) {
            log.debug("MaxNumSlots reply received: {}", r);
            tc.getCommandStation().setCommandStationMaxNumSlots(r);
        }
    }

    // listen for the messages to the command station
    @Override
    public void message(DCCppMessage l) {
    }

    // Handle message timeout notification
    // If the message still has retries available, reduce retries and send it back to the traffic controller.
    @Override
    public void notifyTimeout(DCCppMessage msg) {
        log.debug("Notified of timeout on message '{}' , {} retries available.", msg, msg.getRetries());
        if (msg.getRetries() > 0) {
            msg.setRetries(msg.getRetries() - 1);
            tc.sendDCCppMessage(msg, this);
        }        
    }

    @Override
    public void releaseThrottle(DccThrottle t, ThrottleListener l) {
        super.releaseThrottle(t, l);
    }

    @Override
    public boolean disposeThrottle(DccThrottle t, ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            tc.getCommandStation().releaseRegister(t.getLocoAddress().getNumber());
            if (t instanceof DCCppThrottle) {
                DCCppThrottle lnt = (DCCppThrottle) t;
                throttles.remove(lnt.getLocoAddress()); // remove from throttles map.
                lnt.throttleDispose();
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispose() {
        tc.removeDCCppListener(DCCppInterface.THROTTLE, this);
        //stopThrottleRequestTimer(); no timer used in this tm
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppThrottleManager.class);

}
