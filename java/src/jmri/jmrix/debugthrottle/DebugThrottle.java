package jmri.jmrix.debugthrottle;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jmri.DccLocoAddress;
import jmri.LocoAddress;
import jmri.jmrix.AbstractThrottle;
import jmri.jmrix.SystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of DccThrottle for debugging use.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 */
public class DebugThrottle extends AbstractThrottle {

    /**
     * Constructor
     */
    public DebugThrottle(DccLocoAddress address, SystemConnectionMemo memo) {
        super(memo);

        log.debug("DebugThrottle constructor called for address {}", address);

        // cache settings. It would be better to read the
        // actual state, but I don't know how to do this
        this.speedSetting = 0;
        this.f0 = false;
        this.f1 = false;
        this.f2 = false;
        this.f3 = false;
        this.f4 = false;
        this.f5 = false;
        this.f6 = false;
        this.f7 = false;
        this.f8 = false;
        this.f9 = false;
        this.f10 = false;
        this.f11 = false;
        this.f12 = false;
        this.isForward = true;

        this.address = address;
        setSpeedStepMode(jmri.SpeedStepMode.NMRA_DCC_128);
    }

    DccLocoAddress address;

    @Override
    public LocoAddress getLocoAddress() {
        return address;
    }

    @Override
    public String toString() {
        return getLocoAddress().toString();
    }

    /**
     * Send the message to set the state of functions F0, F1, F2, F3, F4
     */
    @Override
    protected void sendFunctionGroup1() {
        log.debug("sendFunctionGroup1 called for address {}, dir={},F0={},F1={},F2={},F3={},F4={}",
                this.address,
                (this.isForward ? "FWD":"REV"),
                (this.f0 ? "On":"Off"),
                (this.f1 ? "On":"Off"),
                (this.f2 ? "On":"Off"),
                (this.f3 ? "On":"Off"),
                (this.f4 ? "On":"Off"));
    }

    /**
     * Send the message to set the state of functions F5, F6, F7, F8
     */
    @Override
    protected void sendFunctionGroup2() {
        log.debug("sendFunctionGroup2() called");
    }

    /**
     * Send the message to set the state of functions F9, F10, F11, F12
     */
    @Override
    protected void sendFunctionGroup3() {
        log.debug("sendFunctionGroup3() called");
    }

    /**
     * Set the speed {@literal &} direction
     * <p>
     * This intentionally skips the emergency stop value of 1.
     *
     * @param speed Number from 0 to 1; less than zero is emergency stop
     */
    @SuppressFBWarnings(value = "FE_FLOATING_POINT_EQUALITY") // OK to compare floating point, notify on any change
    @Override
    public void setSpeedSetting(float speed) {
        log.debug("setSpeedSetting: float speed: {} for address {}", speed, this.address);
        float oldSpeed = this.speedSetting;
        if (speed > 1.0) {
            log.warn("Speed was set too high: " + speed);
        }
        this.speedSetting = speed;
        if (oldSpeed != this.speedSetting) {
            notifyPropertyChangeListener(SPEEDSETTING, oldSpeed, this.speedSetting);
        }
        record(speed);
    }

    @Override
    public void setIsForward(boolean forward) {
        log.debug("setIsForward({}) called for address {}, was {}", forward, this.address, this.isForward);
        boolean old = this.isForward;
        this.isForward = forward;
        sendFunctionGroup1();  // send the command
        if (old != this.isForward) {
            notifyPropertyChangeListener(ISFORWARD, old, this.isForward);
        }
    }

    @Override
    protected void throttleDispose() {
        log.debug("throttleDispose() called for address {}", this.address);
        finishRecord();
    }

    // initialize logging
    private final static Logger log = LoggerFactory.getLogger(DebugThrottle.class);

}
