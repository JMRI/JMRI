// SampleAutomaton3.java

package jmri.jmrit.automat;

import jmri.*;

/**
 * This sample Automaton runs a locomotive back and forth
 * on a piece of track by watching two sensors.
 * <P>
 * The sensors and locomotive are hardcoded, as this is
 * an example of just the Automaton function.  Adding a GUI
 * to configure these would be straight-forward. The values
 * could be passed via the constructor, or the constructor
 * (which can run in any required thread) could invoke
 * a dialog.
 * <P>
 * For test purposes, one of these objects can be
 * created and invoked by a SampleAutomaton3Action.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 * @see         jmri.jmrit.automat.SampleAutomaton3Action
 */
public class SampleAutomaton3 extends AbstractAutomaton {

    /**
     * References the locomotive decoder to be controlled
     */
    DccThrottle throttle = null;

    /**
     * References the sensor the locomotive will enter when
     * it moves forward to the limit.
     */
    Sensor fwdSensor;

    /**
     * References the sensor the locomotive will enter when
     * it moves backward to the limit.
     */
    Sensor revSensor;

    /**
     * By default, monitors sensor "182" forward, "178" backward
     * and controls locomotive 77(short).
     *
     */
    public void init() {
        // get references to sample layout objects

        fwdSensor = InstanceManager.sensorManagerInstance().
                    newSensor(null,"182");

        revSensor = InstanceManager.sensorManagerInstance().
                    newSensor(null,"178");

        throttle = getThrottle(77, false);
        //InstanceManager.throttleManagerInstance()
        //        .requestThrottle(77,new ThrottleListener() {
        //            public void notifyThrottleFound(DccThrottle t) {
        //                throttle = t;
        //            }
        //        });
    }

    boolean moveFwd;
    int fwdState;
    int revState;

    /**
     * Watch the sensors, and change direction to match.
     * @return Always returns true to continue operation
     */
    public boolean handle() {
        // make sure the throttle has been initialized
        // while (throttle == null) {
        //     log.debug("waiting for throttle");
        //     wait(10000);
        //    if (throttle == null) log.warn("Still waiting for throttle!");
        // }

        // we're supposed to be moving forward here
        // This initialization is only needed the first time through,
        // but it doesn't hurt to do it each time.
        moveFwd = true;
        throttle.setIsForward(moveFwd);

        log.debug("Waiting for state change");

        // wait until the forward sensor is ACTIVE
        while ((fwdState = fwdSensor.getKnownState()) != Sensor.ACTIVE) {
            waitSensorChange(fwdState, fwdSensor);
        }
        log.debug("Forward sensor active");

        moveFwd = false;
        throttle.setIsForward(moveFwd);

        // wait until the reverse sensor is ACTIVE
        while ((revState = revSensor.getKnownState()) != Sensor.ACTIVE) {
            waitSensorChange(revState, revSensor);
        }
        log.debug("Backward sensor active");

        moveFwd = true;
        throttle.setIsForward(moveFwd);

        return true;   // never terminate voluntarily
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SampleAutomaton3.class.getName());

}


/* @(#)SampleAutomaton3.java */
