// SampleAutomaton.java

package jmri.jmrit.automat;

import jmri.*;

/**
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class SampleAutomaton extends AbstractAutomaton {

    Turnout turnout;
    Sensor sensor;

    public SampleAutomaton() {
        super();
        // get references to sample layout objects

        turnout = InstanceManager.turnoutManagerInstance().
                    newTurnout(null,"26");

        sensor = InstanceManager.sensorManagerInstance().
                    newSensor(null,"31");

        // set up the initial correlation
        now = sensor.getKnownState();
        setTurnout(now);
    }

    int now;

    /**
     *
     * @return false to terminate the automaton, for example due to an error.
     */
    public boolean handle() {
        log.debug("Starting; wait for state change");
        while (now == sensor.getKnownState()) {
            wait(2000);
        }
        // get new value
        now = sensor.getKnownState();
        log.debug("new state: "+now);

        // match the turnout to the conditions
        setTurnout(now);

        return true;   // never terminate voluntarily
    }

    void setTurnout(int now) {
        try {
            if (now == Sensor.ACTIVE)
                turnout.setCommandedState(Turnout.THROWN);
            else
                turnout.setCommandedState(Turnout.CLOSED);
        } catch (JmriException e) {
            log.error("exception during startup:"+e);
        }
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SampleAutomaton.class.getName());

}


/* @(#)SampleAutomaton.java */
