package jmri.jmrix.tams;

import java.util.EnumSet;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TamsDCC implementation of a ThrottleManager.
 * <p>
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
        if (address instanceof DccLocoAddress ) {
            /*Here we do not set notifythrottle, we simply create a new Tams throttle.
            The Tams throttle in turn will notify the throttle manager of a successful or
            unsuccessful throttle connection. */
            log.info("new TamsThrottle for " + address);
            notifyThrottleKnown(new TamsThrottle((TamsSystemConnectionMemo) adapterMemo, (DccLocoAddress) address), address);
        }
        else {
            log.error("{} is not a DccLocoAddress",address);
            failedThrottleRequest(address, "LocoAddress " +address+ " is not a DccLocoAddress");
        }
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

    /**
     * Returns false
     * <p>
     * {@inheritDoc}
     */
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
    public EnumSet<SpeedStepMode> supportedSpeedModes() {
        return EnumSet.of(SpeedStepMode.NMRA_DCC_128, SpeedStepMode.NMRA_DCC_28);
    }

    @Override
    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            if (t instanceof TamsThrottle) {
                TamsThrottle lnt = (TamsThrottle) t;
                lnt.throttleDispose();
                return true;
            }
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(TamsThrottleManager.class);

}
