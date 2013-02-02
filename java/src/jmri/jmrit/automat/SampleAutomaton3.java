// SampleAutomaton3.java

package jmri.jmrit.automat;

import org.apache.log4j.Logger;
import jmri.DccThrottle;
import jmri.InstanceManager;
import jmri.Sensor;

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
 * @version     $Revision$
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
     * By default, monitors sensor "182" for the forward end of the track
     */
    String fwdSensorName = "182";

    /**
     * By default, monitors sensor "178" for the reverse end of the track
     */
    String revSensorName = "178";

    /**
     * By default, controls locomotive 77(short).
     */
    int locoNumber = 77;
    boolean locoLong = false;

    protected void init() {
        // get references to sample layout objects

        fwdSensor = InstanceManager.sensorManagerInstance().
                    provideSensor(fwdSensorName);
		if (fwdSensor==null) {
			log.error("Failure to provide forward sensor "+fwdSensorName+" on initialization");
		}

        revSensor = InstanceManager.sensorManagerInstance().
                    provideSensor(revSensorName);
		if (revSensor==null) {
			log.error("Failure to provide reverse sensor "+revSensorName+" on initialization");
		}

        throttle = getThrottle(locoNumber, locoLong);
    }

    boolean moveFwd;
    int fwdState;
    int revState;

    /**
     * Watch the sensors, and change direction to match.
     * @return Always returns true to continue operation
     */
    protected boolean handle() {

        // we're supposed to be moving forward here
        // This initialization is only needed the first time through,
        // but it doesn't hurt to do it each time.
        moveFwd = true;
        throttle.setIsForward(moveFwd);
        throttle.setSpeedSetting(0.50f);

        // wait until the forward sensor is ACTIVE
        log.debug("Waiting for state change");
        while ((fwdState = fwdSensor.getKnownState()) != Sensor.ACTIVE) {
            waitSensorChange(fwdState, fwdSensor);
        }
        log.debug("Forward sensor active");

        moveFwd = false;
        throttle.setIsForward(moveFwd);
        throttle.setSpeedSetting(0.33f);

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
    static Logger log = Logger.getLogger(SampleAutomaton3.class.getName());

}


/* @(#)SampleAutomaton3.java */
