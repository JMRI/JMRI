package jmri.jmrix.easydcc;

import java.util.EnumSet;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * EasyDCC implementation of a ThrottleManager.
 * <p>
 * Based on early NCE code.
 *
 * @author Bob Jacobsen Copyright (C) 2001, 2005
 * @author Modified by Kelly Loyd
 */
public class EasyDccThrottleManager extends AbstractThrottleManager {

    private EasyDccSystemConnectionMemo _memo = null;

    /**
     * Constructor
     */
    public EasyDccThrottleManager(EasyDccSystemConnectionMemo memo) {
        super(memo);
        _memo = memo;
    }

    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        // Not sure if EasyDcc requires feedback. May need to extend this.
        /* It appears that the first command sent to the Queue in EasyDcc
         is 'lost' - so it may be beneficial to send a 'Send' command 
         just to wake up the command station.
         This was tested on v418 - also appears as an issue with the
         radio throttles. 
         */
        if (address instanceof DccLocoAddress ) {
            log.debug("new EasyDccThrottle for {}", address);
            notifyThrottleKnown(new EasyDccThrottle(_memo, (DccLocoAddress) address), address);
        }
        else {
            log.error("LocoAddress {} is not a DccLocoAddress",address);
            failedThrottleRequest(address, "LocoAddress " +address+ " is not a DccLocoAddress");
        }
    }

    // EasyDcc does not have a 'dispatch' function.
    @Override
    public boolean hasDispatchFunction() {
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

    /*
     * Local method for deciding short/long address.
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
            int value = 0;
            DccLocoAddress address = (DccLocoAddress) t.getLocoAddress();
            byte[] result = jmri.NmraPacket.speedStep128Packet(address.getNumber(),
                    address.isLongAddress(), value, t.getIsForward());
            // KSL 20040409 - this is messy, as I only wanted
            // the address to be sent.
            EasyDccMessage m = new EasyDccMessage(7);
            // for EasyDCC, release the loco.
            // D = Dequeue
            // Cx xx (address)
            int i = 0;  // message index counter
            m.setElement(i++, 'D');

            if (address.isLongAddress()) {
                m.setElement(i++, ' ');
                m.addIntAsTwoHex(result[0] & 0xFF, i);
                i = i + 2;
                m.setElement(i++, ' ');
                m.addIntAsTwoHex(result[1] & 0xFF, i);
            } else { // short address
                m.setElement(i++, ' ');
                m.addIntAsTwoHex(0, i);
                i = i + 2;
                m.setElement(i++, ' ');
                m.addIntAsTwoHex(result[0] & 0xFF, i);
            }

            _memo.getTrafficController().sendEasyDccMessage(m, null);
            if (t instanceof EasyDccThrottle) {
                EasyDccThrottle lnt = (EasyDccThrottle) t;
                lnt.throttleDispose();
                return true;
            }
            else {
                log.error("DccThrottle {} is not an EasyDccThrottle",t);
            }
        }
        return false;
    }

    private final static Logger log = LoggerFactory.getLogger(EasyDccThrottleManager.class);

}
