package jmri.jmrix.lenz;

import java.util.EnumSet;
import java.util.HashMap;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * XNet implementation of a ThrottleManager based on the
 * AbstractThrottleManager.
 *
 * @author Paul Bender Copyright (C) 2002-2004
 * @navassoc 1 - * jmri.jmrix.lenz.XNetThrottle
 */
public class XNetThrottleManager extends AbstractThrottleManager implements XNetListener {

    protected HashMap<LocoAddress, XNetThrottle> throttles = new HashMap<LocoAddress, XNetThrottle>(5);

    protected XNetTrafficController tc = null;

    /**
     * Constructor.
     */
    public XNetThrottleManager(XNetSystemConnectionMemo memo) {
        super(memo);
        // connect to the TrafficManager
        tc = memo.getXNetTrafficController();

        // Register to listen for throttle messages
        tc.addXNetListener(XNetInterface.THROTTLE, this);
    }

    /**
     * Request a new throttle object be creaetd for the address, and let the
     * throttle listeners know about it.
     */
    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        XNetThrottle throttle;
        if (log.isDebugEnabled()) {
            log.debug("Requesting Throttle: " + address);
        }
        // range check for LH200 and Compact/Commander
        if (tc.getCommandStation().getCommandStationType() == 0x01 ||
            tc.getCommandStation().getCommandStationType() == 0x02 ) {
            if(address.getNumber()>=100) {
               String typeString = Bundle.getMessage(tc.getCommandStation().getCommandStationType() == 0x01?"CSTypeLH200":"CSTypeCompact");
               failedThrottleRequest(address,Bundle.getMessage("ThrottleErrorCSTwoDigit",typeString));
               return;
            }
        }
        if (throttles.containsKey(address)) {
            notifyThrottleKnown(throttles.get(address), address);
        } else {
            throttle = new XNetThrottle((XNetSystemConnectionMemo) adapterMemo, address, tc);
            throttles.put(address, throttle);
            notifyThrottleKnown(throttle, address);
        }
    }

    /**
     * XpressNet based systems DO NOT use the Dispatch Function.
     */
    @Override
    public boolean hasDispatchFunction() {
        return false;
    }

    /**
     * XpressNet based systems can have multiple throttles for the same
     * device.
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected boolean singleUse() {
        return false;
    }

    /**
     * Address 100 and above is a long address.
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }

    /**
     * Address 99 and below is a short address.
     */
    @Override
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    @Override
    public boolean addressTypeUnique() {
        return true;
    }

    /**
     * Local method for deciding short/long address
     */
    static protected boolean isLongAddress(int num) {
        return (num >= 100);
    }

    /**
     * What speed modes are supported by this system? value should be xor of
     * possible modes specifed by the DccThrottle interface XpressNet supports
     * 14,27,28 and 128 speed step modes
     */
    @Override
    public EnumSet<SpeedStepMode> supportedSpeedModes() {
        return EnumSet.of(SpeedStepMode.NMRA_DCC_128
                , SpeedStepMode.NMRA_DCC_28
                , SpeedStepMode.NMRA_DCC_27
                , SpeedStepMode.NMRA_DCC_14);
    }

    /**
     * Handle incoming messages for throttles.
     */
    @Override
    public void message(XNetReply r) {
        // We want to check to see if a throttle has taken over an address
        if (r.getElement(0) == XNetConstants.LOCO_INFO_RESPONSE) {
            if (r.getElement(1) == XNetConstants.LOCO_NOT_AVAILABLE) {
                // This is a take over message.  If we know about this throttle,
                // send the message on.
                LocoAddress address = new jmri.DccLocoAddress(r.getThrottleMsgAddr(),
                        isLongAddress(r.getThrottleMsgAddr()));
                if (throttles.containsKey(address)) {
                    throttles.get(address).message(r);
                }
            }
        }

    }

    /**
     * Listen for the messages to the LI100/LI101.
     */
    @Override
    public void message(XNetMessage l) {
    }

    /**
     * Handle a timeout notification.
     */
    @Override
    public void notifyTimeout(XNetMessage msg) {
    }

    @Override
    public void releaseThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
    }

    @Override
    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            if(!(t instanceof XNetThrottle)) {
               throw new IllegalArgumentException("Attempt to dispose non-XpressNet Throttle");
            }
            XNetThrottle lnt = (XNetThrottle) t;
            lnt.throttleDispose();
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(XNetThrottleManager.class);

}
