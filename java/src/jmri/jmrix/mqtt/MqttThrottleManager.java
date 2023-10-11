package jmri.jmrix.mqtt;

import java.util.EnumSet;
import java.util.HashMap;

import jmri.DccThrottle;
import jmri.LocoAddress;
import jmri.SpeedStepMode;
import jmri.ThrottleListener;
import jmri.jmrix.AbstractThrottleManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.annotation.Nonnull;

/**
 * MQTT implementation of a ThrottleManager based on the
 * AbstractThrottleManager.
 *
 * @author Dean Cording Copyright (C) 2023
 */

public class MqttThrottleManager extends AbstractThrottleManager {

    protected HashMap<LocoAddress, MqttThrottle> throttles = new HashMap<LocoAddress, MqttThrottle>(5);

    /**
     * Constructor.
     * @param memo the memo for the connection this tm will use
     */
    public MqttThrottleManager(MqttSystemConnectionMemo memo) {
        super(memo);
    }

    public void setSendThrottleTopic(@Nonnull String sendThrottleTopic) {
        this.sendThrottleTopic = sendThrottleTopic;
    }
    public void setRcvThrottleTopic(@Nonnull String rcvThrottleTopic) {
        this.rcvThrottleTopic = rcvThrottleTopic;
    }
    public void setSendDirectionTopic(@Nonnull String sendDirectionTopic) {
        this.sendDirectionTopic = sendDirectionTopic;
    }
    public void setRcvDirectionTopic(@Nonnull String rcvDirectionTopic) {
        this.rcvDirectionTopic = rcvDirectionTopic;
    }
    public void setSendFunctionTopic(@Nonnull String sendFunctionTopic) {
        this.sendFunctionTopic = sendFunctionTopic;
    }
    public void setRcvFunctionTopic(@Nonnull String rcvFunctionTopic) {
        this.rcvFunctionTopic = rcvFunctionTopic;
    }

    @Nonnull
    public String sendThrottleTopic = "cab/{0}/throttle"; // for constructing topic; public for script access
    @Nonnull
    public String rcvThrottleTopic = "cab/{0}/throttle"; // for constructing topic; public for script access
    @Nonnull
    public String sendDirectionTopic = "cab/{0}/direction"; // for constructing topic; public for script access
    @Nonnull
    public String rcvDirectionTopic = "cab/{0}/direction"; // for constructing topic; public for script access
    @Nonnull
    public String sendFunctionTopic = "cab/{0}/function/{1}"; // for constructing topic; public for script access
    @Nonnull
    public String rcvFunctionTopic = "cab/{0}/function/{1}"; // for constructing topic; public for script access


    /**
     * Request a new throttle object be created for the address, and let the
     * throttle listeners know about it.
     *
     */
    @Override
    public void requestThrottleSetup(LocoAddress address, boolean control) {
        MqttThrottle throttle;
        log.debug("Requesting Throttle: {}", address);
        if (throttles.containsKey(address)) {
            notifyThrottleKnown(throttles.get(address), address);
        } else {
            throttle = new MqttThrottle((MqttSystemConnectionMemo) adapterMemo,
                sendThrottleTopic, rcvThrottleTopic,
                sendDirectionTopic, rcvDirectionTopic,
                sendFunctionTopic, rcvFunctionTopic, address);
            throttles.put(address, throttle);
            notifyThrottleKnown(throttle, address);
        }
    }

    /**
     * MQTT based systems DO NOT use the Dispatch Function
     */
    @Override
    public boolean hasDispatchFunction() {
        return true;
    }

    /**
     * MQTT based systems can have multiple throttles for the same
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

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean disposeThrottle(DccThrottle t, ThrottleListener l) {

        log.debug("disposeThrottle {}", t);

        if (super.disposeThrottle(t, l)) {
            if (t instanceof MqttThrottle) {
                MqttThrottle lnt = (MqttThrottle) t;
                throttles.remove(lnt.getLocoAddress()); // remove from throttles map.
                lnt.throttleDispose();
                return true;
            }
        }
        log.error("Dispose Throttle failed {}", t);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void dispatchThrottle(DccThrottle t, ThrottleListener l) {
        log.debug("dispatchThrottle {}", t);
        disposeThrottle(t,l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void releaseThrottle(DccThrottle t, ThrottleListener l) {
        log.debug("releaseThrottle {}", t);
        if (t instanceof MqttThrottle) { // should always be the case as it was made that way
            ((MqttThrottle)t).throttleRelease();
        }
        disposeThrottle(t, l);
    }


    private final static Logger log = LoggerFactory.getLogger(MqttThrottleManager.class);

}
