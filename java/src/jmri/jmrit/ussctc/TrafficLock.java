package jmri.jmrit.ussctc;

import jmri.*;


/**
 * Implements a traffic lock.
 * <p>
 * A signal can't be set if a route (set of turnout settings) is present
 * and the far-end signal is set against. Each lock object handles one route.
 *
 * @author Bob Jacobsen Copyright (C) 2007, 2017
 */
public class TrafficLock implements Lock {

    /**
     * @param signal SignalHeadSection at far end of this route
     * @param direction Setting that, if present in the far SignalHeadSection, means to lock
     */
    public TrafficLock(SignalHeadSection signal, CodeGroupThreeBits direction) {
        this.farSignal = signal;
        this.direction = direction;
        beans = null;
    }

    /**
     * @param signal SignalHeadSection at far end of this route
     * @param direction Setting that, if present in the far SignalHeadSection, means to lock
     */
    public TrafficLock(SignalHeadSection signal, CodeGroupThreeBits direction, BeanSetting[] beans) {
        this.farSignal = signal;
        this.direction = direction;
        this.beans = beans;
    }

    SignalHeadSection farSignal;
    CodeGroupThreeBits direction;
    BeanSetting[] beans;
    
    /**
     * Test the lock conditions
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
