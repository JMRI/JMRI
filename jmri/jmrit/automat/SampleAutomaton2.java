// SampleAutomaton2.java

package jmri.jmrit.automat;

import jmri.*;

/**
 * This sample Automaton watches a Sensor, and adjusts
 * the momentum of a locomotive using ops-mode programming
 * when the sensor state changes.
 * <P>
 * The sensor and decoder are hardcoded, as this is
 * an example of just the Automaton function.  Adding a GUI
 * to configure these would be straight-forward. The values
 * could be passed via the constructor, or the constructor
 * (which can run in any required thread) could invoke
 * a dialog.
 * <P>
 * For test purposes, one of these objects can be
 * created and invoked by a SampleAutomaton2Action.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.4 $
 * @see         jmri.jmrit.automat.SampleAutomaton2Action
 */
public class SampleAutomaton2 extends AbstractAutomaton {

    /**
     * References the locomotive decoder to be controlled
     */
    Programmer programmer;
    /**
     * References the sensor to be monitored
     */
    Sensor sensor;

    /**
     * By default, monitors sensor "32" and controls locomotive 1234(long).
     *
     */
    protected void init() {
        // get references to sample layout objects

        sensor = InstanceManager.sensorManagerInstance().
                    provideSensor("32");

        programmer = InstanceManager.programmerManagerInstance()
                        .getOpsModeProgrammer(true, 4321);

        // set up the initial correlation
        now = sensor.getKnownState();
        setMomentum(now);
    }

    int now;

    /**
     * Watch "sensor", and when it changes the momentum CV to match.
     * @return Always returns true to continue operation
     */
    protected boolean handle() {
        log.debug("Waiting for state change");

        // wait until the sensor changes state
        waitSensorChange(now, sensor);

        // get new value
        now = sensor.getKnownState();
        log.debug("Found new state: "+now);

        // match the decoder's momentum
        setMomentum(now);

        return true;   // never terminate voluntarily
    }

    /**
     * Set CV3, acceleration momentum, to match the sensor state.
     * When the sensor is active, set the momentum to 30;
     * when inactive, set the momentum to 0.
     * @param now The current value of the sensor state.
     */
    void setMomentum(int now) {
        try {
            if (now == Sensor.ACTIVE)
                programmer.writeCV(3, 30, null);
            else
                programmer.writeCV(3, 0, null);
        } catch (JmriException e) {
            log.error("exception setting turnout:"+e);
        }
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SampleAutomaton2.class.getName());
}

/* @(#)SampleAutomaton2.java */
