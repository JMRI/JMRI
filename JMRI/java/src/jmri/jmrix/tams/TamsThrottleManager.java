package jmri.jmrix.tams;

import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TamsDCC implementation of a ThrottleManager.
 * <P>
 * Based on early NCE code.
 * 
 * Based on work by Bob Jacobsen 
 *
 * @author	Kevin Dickerson
 * 
 */
public class TamsThrottleManager extends AbstractThrottleManager implements TamsListener {

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

    @Override
    public void reply(TamsReply m) {
        //We are not sending commands from here yet!
    }

    @Override
    public void message(TamsMessage m) {
        // messages are ignored
    }

    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        /*Here we do not set notifythrottle, we simply create a new Tams throttle.
         The Tams throttle in turn will notify the throttle manager of a successful or
         unsuccessful throttle connection. */
        log.info("new TamsThrottle for " + address);
        notifyThrottleKnown(new TamsThrottle((TamsSystemConnectionMemo) adapterMemo, (DccLocoAddress) address), address);
    }

    @Override
    public boolean hasDispatchFunction() {
        return false;
    }

    /**
     * Address 100 and above is a long address
     *
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }

    /**
     * Address 99 and below is a short address
     *
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

    @Override
    protected boolean singleUse() {
        return false;
    }

    /*
     * Local method for deciding short/long address
     */
    static boolean isLongAddress(int num) {
        return (num >= 100);
    }

    @Override
    public int supportedSpeedModes() {
        return (DccThrottle.SpeedStepMode128 | DccThrottle.SpeedStepMode28);
    }

    @Override
    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            TamsThrottle lnt = (TamsThrottle) t;
            lnt.throttleDispose();
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(TamsThrottleManager.class);

}
