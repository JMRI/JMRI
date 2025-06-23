package jmri.jmrix.bidib;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.util.EnumSet;
import java.util.HashMap;
import jmri.DccLocoAddress;
import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.ThrottleListener;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * BiDiB implementation of a ThrottleManager.
 *
 * @author Bob Jacobsen Copyright (C) 2001
 * @author Eckart Meyer Copyright (C) 2019
 */
public class BiDiBThrottleManager extends AbstractThrottleManager {

    /**
     * Constructor.
     * @param memo system connection memo
     */
    public BiDiBThrottleManager(BiDiBSystemConnectionMemo memo) {
        super(memo);
        this.tc = memo.getBiDiBTrafficController();
        this.prefix = memo.getSystemPrefix();
    }

    //protected HashMap<LocoAddress, BiDiBThrottle> throttles = new HashMap<LocoAddress, BiDiBThrottle>();
    protected HashMap<LocoAddress, BiDiBThrottle> throttles = new HashMap<>();
    BiDiBTrafficController tc = null;
    String prefix = "";

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",justification = "Cast safe by design")
    @Override
    public void requestThrottleSetup(LocoAddress a, boolean control) {
        DccLocoAddress address = (DccLocoAddress) a;
        BiDiBThrottle throttle;
        log.debug("request BiDiBThrottle for {}", address);
        // the BiDiB protocol doesn't require an interaction with the command
        // station for this, so immediately trigger the callback.
        
        // We have our own throttle list here to save the loco data when no throttle
        // is active - JMRI completely forgets loco data after removing the last throttle
        // for that loco (see AbstactThottleManager, HasMap addressThrottles)
        // Perhaps we could fiddle with the dipose methods, but there is no example from
        // other throttle manager. Instead, we use the variant from DCC++ here.
        
        if (throttles.containsKey(address)) {
            log.debug("BiDiB throttle found.");
            throttle = throttles.get(address);
            notifyThrottleKnown(throttle, address);
            throttle.requestState(); //try to get loco state from command station
        } else {
            log.debug("create new BiDiB throttle.");
            throttle = new BiDiBThrottle((BiDiBSystemConnectionMemo) adapterMemo, address);
            throttles.put(address, throttle);
            notifyThrottleKnown(throttle, address);
        }
    }

    /*
     * BiDiB based systems can have multiple throttles for the same 
     * device
     */
    @Override
    protected boolean singleUse() {
        return false;
    }

    /**
     * Addresses 0-10239 can be long
     *
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return ((address >= 0) && (address <= 10239));
    }

    /**
     * The short addresses 1-127 are available
     *
     */
    @Override
    public boolean canBeShortAddress(int address) {
        return ((address >= 1) && (address <= 127));
    }

    /**
     * Are there any ambiguous addresses (short vs long) on this system?
     */
    @Override
    public boolean addressTypeUnique() {
        return true;
    }

//    @Override
//    public int supportedSpeedModes() {
//        return (DccThrottle.SpeedStepMode128 | DccThrottle.SpeedStepMode28 | DccThrottle.SpeedStepMode14);
//        //return (DccThrottle.SpeedStepMode14 );
//    }
    @Override
    public EnumSet<SpeedStepMode> supportedSpeedModes() {
        return EnumSet.of(SpeedStepMode.NMRA_DCC_128
                , SpeedStepMode.NMRA_DCC_28
                , SpeedStepMode.NMRA_DCC_14);
    }

/// just to see what happens...
    @Override
    public void dispatchThrottle(DccThrottle t, ThrottleListener l) {
        log.debug("BiDiBThrottleManager.dispatchThrottle: {}, {}", t, l);
        super.dispatchThrottle(t, l);
    }

    // called when "Release" button is pressed an also when Throttle window is close.
    @Override
    public void releaseThrottle(DccThrottle t, ThrottleListener l) {
        log.debug("BiDiBThrottleManager.releaseThrottle: {}, {}", t, l);
        super.releaseThrottle(t, l);
    }
///
    
    /**
     * {@inheritDoc}
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",justification = "Cast safe by design")
    @Override
    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            BiDiBThrottle bt = (BiDiBThrottle) t;
            bt.throttleDispose();
            //throttles.remove(bt.locoAddress);//TEST - so a new instance will be created next time
            return true;
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(BiDiBThrottleManager.class);

}
