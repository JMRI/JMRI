package jmri.jmrix.lenz.hornbyelite;

import jmri.LocoAddress;
import jmri.jmrix.lenz.XNetReply;
import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetTrafficController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle with code specific to an XpressNet
 * connection on the Hornby Elite.
 *
 * @author Paul Bender (C) 2008-2009
 */
public class EliteXNetThrottle extends jmri.jmrix.lenz.XNetThrottle {

    /**
     * Interval to check the status of the throttle
     */
    protected static final int statTimeoutValue = 5000;

    /**
     * Constructor
     */
    public EliteXNetThrottle(XNetSystemConnectionMemo memo, XNetTrafficController tc) {
        super(memo, tc);
        log.debug("Elite XNetThrottle constructor");
    }

    /**
     * Constructor by address
     */
    public EliteXNetThrottle(XNetSystemConnectionMemo memo, LocoAddress address, XNetTrafficController tc) {
        super(memo, address, tc);
        log.debug("Elite XNetThrottle constructor called for address " + address);
    }

    /**
     * Send the XpressNet message to set the Momentary state of locomotive
     * functions F0, F1, F2, F3, F4.
     */
    @Override
    protected void sendMomentaryFunctionGroup1() {
        log.debug("Momentary function request not supported by Elite.");
        return;
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F5,
     * F6, F7, F8.
     */
    @Override
    protected void sendMomentaryFunctionGroup2() {
        log.debug("Momentary function request not supported by Elite.");
        return;
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F9,
     * F10, F11, F12.
     */
    @Override
    protected void sendMomentaryFunctionGroup3() {
        log.debug("Momentary function request not supported by Elite.");
        return;
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F13,
     * F14, F15, F16, F17, F18, F19, F20.
     */
    @Override
    protected void sendMomentaryFunctionGroup4() {
        log.debug("Momentary function request not supported by Elite.");
        return;
    }

    /**
     * Send the XpressNet message to set the momentary state of functions F21,
     * F22, F23, F24, F25, F26, F27, F28.
     */
    @Override
    protected void sendMomentaryFunctionGroup5() {
        log.debug("Momentary function request not supported by Elite.");
        return;
    }

    /**
     * Send a request to get the status of functions from the command station.
     */
    @Override
    synchronized protected void sendFunctionStatusInformationRequest() {
        log.debug("Momentary function request not supported by Elite.");
        return;
    }

    /**
     * Handle incoming messages for this throttle.
     */
    @Override
    public void message(XNetReply l) {
        // First, we want to see if this throttle is waiting for a message
        //or not.
        log.debug("Throttle - received message ");
        if (requestState == THROTTLEIDLE) {
            log.debug("Current throttle status is THROTTLEIDLE");
            // We haven't sent anything, but we might be told someone else
            // has taken over this address
            if (l.getElement(0) == 0xE5) {
                log.debug("Throttle - message is LOCO_INFO_RESPONSE ");
                if (l.getElement(1) == 0xF8) {
                    /* This is a Hornby Elite specific response
                     * which occurs when the Elite throttle changes 
                     * speed.  If this is for this throttle,
                     * we need to handle it.
                     * The address is in bytes 3 and 4*/
                    if (getDccAddressHigh() == l.getElement(2) && getDccAddressLow() == l.getElement(3)) {
                        //Set the Is available flag to "False"                    
                        log.info("Loco {} in use by another device", getDccAddress());
                        setIsAvailable(false);
                        // Set the speed step mode and availabliity
                        // from byte 5
                        parseSpeedAndAvailability(l.getElement(4));
                        // Parse the speed step and direction from
                        // byte 6.
                        parseSpeedAndDirection(l.getElement(5));
                    }
                } else if (l.getElement(1) == 0xF9) {
                    /* This is a Hornby Elite specific response
                     * which occurs when the Elite throttle changes 
                     * functions.  If this is for this throttle,
                     * we need to handle it.
                     * The address is in bytes 3 and 4*/
                    if (getDccAddressHigh() == l.getElement(2) && getDccAddressLow() == l.getElement(3)) {
                        // Set the Is available flag to "False"
                        log.info("Loco {} in use by another device", getDccAddress());
                        setIsAvailable(false);
                        // Parse the function status from bytes 5 and 6.
                        parseFunctionInformation(l.getElement(4),
                                l.getElement(5));
                    }
                }
            }
        }
        // We didn't find any Elite specific messages, so send the
        // message on to the standard XpressNet throttle message handler
        super.message(l);
    }

    /*
     * Since the Elite sends status messages when the throttle changes,
     * override the startStatusTimer/stopStatustimer method to do nothing. 
     */
    @Override
    protected void startStatusTimer() {
    }

    @Override
    protected void stopStatusTimer() {
    }

    // register for notification
    private final static Logger log = LoggerFactory.getLogger(EliteXNetThrottle.class);

}
