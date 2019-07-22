package jmri.jmrix.srcp;

import java.util.EnumSet;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRCP implementation of a ThrottleManager.
 * <p>
 * Based on early NCE code.
 *
 * @author	Bob Jacobsen Copyright (C) 2001, 2005, 2008
 * @author Modified by Kelly Loyd
 */
public class SRCPThrottleManager extends AbstractThrottleManager {

    private int bus;

    /**
     * Constructor.
     */
    public SRCPThrottleManager(SRCPBusConnectionMemo memo) {
        super(memo);
        bus = memo.getBus();
    }

    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        log.debug("new SRCPThrottle for " + address);
        // Notify ready to go (without waiting for OK?)
        if(address instanceof DccLocoAddress) {
           notifyThrottleKnown(new SRCPThrottle((SRCPBusConnectionMemo) adapterMemo, (DccLocoAddress) address), address);
        } else { 
          // we need to notify that the request failed, because the
          // address is not a DccLocoAddress, but that notification also
          // requires a DccLocoAddress.
          throw new java.lang.IllegalArgumentException("Request for throttle for unsupported non-DCC address.");          
        }
    }

    // KSL 20040409 - SRCP does not have a 'dispatch' function.
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

    /*
     * Local method for deciding short/long address
     */
    static boolean isLongAddress(int num) {
        return (num >= 100);
    }

    @Override
    public EnumSet<SpeedStepMode> supportedSpeedModes() {
        return EnumSet.of(SpeedStepMode.NMRA_DCC_128, SpeedStepMode.NMRA_DCC_28,
            SpeedStepMode.NMRA_DCC_27, SpeedStepMode.NMRA_DCC_14);
    }

    @Override
    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            // Form a message to release the loco
            DccLocoAddress la = (DccLocoAddress) t.getLocoAddress();
            String msg = "TERM " + bus + " GL "
                    + (la.getNumber())
                    + "\n";

            // and send it
            ((SRCPBusConnectionMemo) adapterMemo).getTrafficController().sendSRCPMessage(new SRCPMessage(msg), null);
            return true;
        }
        return false;
        //LocoNetSlot tSlot = lnt.getLocoNetSlot();
    }

    private final static Logger log = LoggerFactory.getLogger(SRCPThrottleManager.class);

}
