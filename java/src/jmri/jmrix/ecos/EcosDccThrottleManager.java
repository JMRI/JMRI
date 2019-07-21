package jmri.jmrix.ecos;

import java.util.EnumSet;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EcosDCC implementation of a ThrottleManager.
 * <p>
 * Based on early NCE code.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2005
 * @author Modified by Kevin Dickerson
 */
public class EcosDccThrottleManager extends AbstractThrottleManager implements EcosListener {

    /**
     * Constructor.
     */
    public EcosDccThrottleManager(EcosSystemConnectionMemo memo) {
        super(memo);
    }

    @Override
    public void reply(EcosReply m) {
        //We are not sending commands from here yet!
    }

    @Override
    public void message(EcosMessage m) {
        // messages are ignored
    }

    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        /*Here we do not set notifythrottle, we simply create a new ecos throttle.
         The ecos throttle in turn will notify the throttle manager of a successful or
         unsuccessful throttle connection. */
        if ( address instanceof DccLocoAddress ) {
            log.debug("new EcosDccThrottle for " + address);
            new EcosDccThrottle((DccLocoAddress) address, (EcosSystemConnectionMemo) adapterMemo, control);
        }
        else {
            log.error("{} is not an DccLocoAddress",address);
            failedThrottleRequest(address, "LocoAddress " +address+ " is not a DccLocoAddress");
        }
    }

    @Override
    public boolean hasDispatchFunction() {
        return false;
    }

    /**
     * Address 100 and above is a long address
     */
    @Override
    public boolean canBeLongAddress(int address) {
        return isLongAddress(address);
    }

    /**
     * Address 99 and below is a short address
     */
    @Override
    public boolean canBeShortAddress(int address) {
        return !isLongAddress(address);
    }

    /**
     * Are there any ambiguous addresses (short vs. long) on this system?
     */
    @Override
    public boolean addressTypeUnique() {
        return false;
    }

    @Override
    public String[] getAddressTypes() {
        return new String[]{
            LocoAddress.Protocol.DCC.getPeopleName(),
            LocoAddress.Protocol.MFX.getPeopleName(),
            LocoAddress.Protocol.MOTOROLA.getPeopleName(),
            LocoAddress.Protocol.SELECTRIX.getPeopleName(),
            LocoAddress.Protocol.LGB.getPeopleName()};
    }

    @Override
    public LocoAddress.Protocol[] getAddressProtocolTypes() {
        return new LocoAddress.Protocol[]{
            LocoAddress.Protocol.DCC,
            LocoAddress.Protocol.MFX,
            LocoAddress.Protocol.MOTOROLA,
            LocoAddress.Protocol.SELECTRIX,
            LocoAddress.Protocol.LGB};
    }


    /*
     * Decide whether given a long address or not.
     */
    static boolean isLongAddress(int num) {
        return (num >= 127);
    }

    @Override
    public EnumSet<SpeedStepMode> supportedSpeedModes() {
        return EnumSet.of(SpeedStepMode.NMRA_DCC_128, SpeedStepMode.NMRA_DCC_28, SpeedStepMode.NMRA_DCC_14);
    }

    public void throttleSetup(EcosDccThrottle throttle, LocoAddress address, boolean result) {
        /* this is called by the ecosdccthrottle, to inform the manager if it has successfully gained
         control of a loco, when setting up the throttle.*/
        if (result) {
            log.debug("Ecos Throttle has control over loco " + address);
            notifyThrottleKnown(throttle, address);
        } else {
            log.debug("Ecos Throttle has NO control over loco " + address);
            failedThrottleRequest(address, "Loco is alredy in use by anoher throttle " + address);
        }
    }

    @Override
    public boolean disposeThrottle(jmri.DccThrottle t, jmri.ThrottleListener l) {
        if (super.disposeThrottle(t, l)) {
            if ( t instanceof EcosDccThrottle ) {
                EcosDccThrottle lnt = (EcosDccThrottle) t;
                lnt.throttleDispose();
                return true;
            }
            else {
                log.error("{} is not an EcosDccThrottle",t);
            }
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(EcosDccThrottleManager.class);

}
