package jmri.jmrit.ussctc;

import jmri.*;

/**
 * Models a traffic relay.
 * <p>
 * A traffic relay has three states, representing a section of track is 
 * allocated to traffic in one direction, the other, or neither.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class TrafficRelay implements Lock {

    enum State {
        Left,
        Right,
        Neither }

    /**
     * @param signal SignalHeadSection at far end of this route
     * @param direction Setting that, if present in the far SignalHeadSection, means to lock
     */
    public TrafficRelay(SignalHeadSection signal, CodeGroupThreeBits direction) {
        this.farSignal = signal;
        this.direction = direction;
        beans = null;
    }

    /**
     * @param signal SignalHeadSection at far end of this route
     * @param direction Setting that, if present in the far SignalHeadSection, means to lock
     */
    public TrafficRelay(SignalHeadSection signal, CodeGroupThreeBits direction, BeanSetting[] beans) {
        this.farSignal = signal;
        this.direction = direction;
        this.beans = beans;
    }

    SignalHeadSection farSignal;
    CodeGroupThreeBits direction;
    BeanSetting[] beans;
    
    /**
     * Test for new condition
     * @return True if lock is clear and operation permitted
     */
    @Override
    public boolean isLockClear() {
        if (beans != null) {
            // if route doesn't match, permitted
            for (BeanSetting bean : beans) {
                if ( ! bean.check()) {
                    lockLogger.setStatus(this, "");
                    return true;
                }
            }
        }

        if (farSignal.getLastIndication() == direction || farSignal.isRunningTime() ) {
                lockLogger.setStatus(this, "Traffic locked to "+farSignal.getName());
                return false;
        }
        lockLogger.setStatus(this, "");
        return true;
    }
    
}
