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
public class TrafficRelay {

    enum State {
        Left,
        Right,
        Neither }

    // logging to locks now, as related
    static String logMemoryName = "IMUSS CTC:LOCK:1:LOG";
        
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
        System.out.println("bean count "+beans.length);
    }

    SignalHeadSection farSignal;
    CodeGroupThreeBits direction;
    BeanSetting[] beans;
    
    /**
     * Test for new condition
     * @return True if lock is clear and operation permitted
     */
    public boolean isLockClear() {
        InstanceManager.getDefault(MemoryManager.class).provideMemory(logMemoryName).setValue("");
        if (beans != null) {
            // if route doesn't match, permitted
            for (BeanSetting bean : beans) {
               if ( ! bean.check()) return true;
            }
        }

        if (farSignal.getLastIndication() == direction || farSignal.isRunningTime() ) {
                InstanceManager.getDefault(MemoryManager.class).provideMemory(logMemoryName)
                    .setValue("Traffic locked to "+farSignal.getName());
                return false;
        }
        return true;
    }
    
}
