// CrrSection.java

package apps.cornwall;

import jmri.*;

/**
 * Automate sections 1A and 1B of the Cornwall RR.
 * <P>
 * Based on Crr0024.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.6 $
 */
public class CrrSection extends jmri.jmrit.automat.AbstractAutomaton {

    /**
     * References the signalheads to be controlled.
     */
    SignalHead si1a;
    SignalHead si1b;

    /**
     * References the sensors to be monitored
     */
    Sensor bo4;
    Sensor bo16;
    Sensor tu1;
    Sensor tu12;


    /**
     * Obtain the input and output objects
     * <P>
     * This also sets the outpts to an initial state
     * to make sure everything is consistent at the start.
     */
    protected void init() {
        TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        SensorManager  sm = InstanceManager.sensorManagerInstance();
        if ( tm==null || sm==null ) return;

        // get references to sample layout objects

        si1a  = new TripleTurnoutSignalHead("si1a", "Signal 1A",
                                            tm.newTurnout("CT1","1a green"),
                                            tm.newTurnout("CT2","1a yellow"),
                                            tm.newTurnout("CT3","1a red"));
        InstanceManager.signalHeadManagerInstance().register(si1a);
        si1a.setAppearance(SignalHead.RED);

        si1b  = new TripleTurnoutSignalHead("si1b", "Signal 1B",
                                            tm.newTurnout("CT4","1b green"),
                                            tm.newTurnout("CT5","1b yellow"),
                                            tm.newTurnout("CT6","1b red"));
        InstanceManager.signalHeadManagerInstance().register(si1b);
        si1b.setAppearance(SignalHead.RED);

        bo4  = sm.newSensor("CS7", "Reading Relay track 2 bo(04)");
        bo16 = sm.newSensor("CS27","PRR/Lebanon entrance bo(16)");

        tu1  = sm.newSensor("CS2", "Cornwall Jct 1 tu(01)");
        tu12 = sm.newSensor("CS29","East Cornwall Junction switch tu(12)");

        // set up the initial correlation
        setOutput();
    }

    /**
     * Watch sensors, and when it changes adjust outputs to match.
     * @return Always returns true to continue operation
     */
    protected boolean handle() {
        log.debug("Waiting for state change");

        // wait until a sensor changes state
        Sensor[] sensors = new Sensor[]{ bo4, bo16, tu1, tu12};
        waitSensorChange(sensors);

        // recalculate outputs
        setOutput();

        return true;   // never terminate permanently
    }

    /**
     * Set outputs to match the sensor state
     */
    void setOutput() {
        boolean bo4now  = ( bo4.getKnownState() == Sensor.ACTIVE);
        boolean bo16now = (bo16.getKnownState() == Sensor.ACTIVE);
        boolean tu1now  = ( tu1.getKnownState() == Sensor.ACTIVE);
        boolean tu12now = (tu12.getKnownState() == Sensor.ACTIVE);
        if (log.isDebugEnabled()) log.debug("Section 1 with bo4="+bo4now+" bo16="+bo16now+" tu1="+tu1now+" tu12="+tu12now);

        // section 1a
        if (
                ( bo4now && tu1now && tu12now)
             || ( bo16now && !tu1now )
             || ( tu1now && !tu12now )
            ) {
            si1a.setAppearance(SignalHead.RED);
            log.debug("1a set RED");
        } else {
            si1a.setAppearance(SignalHead.GREEN);
            log.debug("1a set GREEN");
        }

        // section 1B
        if (
                ( tu1now )
             || ( !tu1now && bo4now )
             || ( !tu1now && !tu12now )
            ) {
            si1b.setAppearance(SignalHead.RED);
            log.debug("1b set RED");
        } else {
            si1b.setAppearance(SignalHead.GREEN);
            log.debug("1b set GREEN");
        }
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CrrSection.class.getName());

}

/* @(#)CrrSection.java */
