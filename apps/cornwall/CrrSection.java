// CrrSection.java

package apps.cornwall;

import jmri.*;

/**
 * Automate sections 1A and 1B of the Cornwall RR.
 * <P>
 * Based on Crr0024.bas
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision: 1.1 $
 */
public class CrrSection extends jmri.jmrit.automat.AbstractAutomaton {

    /**
     * References the turnouts to be controlled.
     * <P>
     * These are actually signals
     */
    Turnout si1green1a;
    Turnout si2yellow1a;
    Turnout si3red1a;

    Turnout si4green1b;
    Turnout si5yellow1b;
    Turnout si6red1b;

    /**
     * References the sensors to be monitored
     */
    Sensor bo4;
    Sensor bo16;
    Sensor tu1;
    Sensor tu3;
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

        si1green1a  = tm.newTurnout("CT1","1a green");
        si2yellow1a = tm.newTurnout("CT2","1a yellow");
        si3red1a    = tm.newTurnout("CT3","1a red");

        si4green1b  = tm.newTurnout("CT4","1b green");
        si5yellow1b = tm.newTurnout("CT5","1b yellow");
        si6red1b    = tm.newTurnout("CT6","1b red");

        bo4  = sm.newSensor("CS7","RDG block 1");
        bo16 = sm.newSensor("CS27","PRR/Lebanon entrance (under bridge)");

        tu1  = sm.newSensor("CS1", "Cornwall Junction switch 1");
        tu3  = sm.newSensor("CS6", "Cornwall Junction switch 2");
        tu12 = sm.newSensor("CS29","Cornwall Junction switch 3");

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
        Sensor[] sensors = new Sensor[]{ bo4, bo16, tu1, tu3, tu12};
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
        boolean tu3now  = ( tu3.getKnownState() == Sensor.ACTIVE);
        boolean tu12now = (tu12.getKnownState() == Sensor.ACTIVE);

        // section 1a
        if (
                ( bo4now && tu1now && tu3now && tu12now)
             || ( !tu3now && tu1now )
             || ( bo16now && !tu1now )
             || ( tu1now && tu3now && !tu12now )
            ) {
            // set red
            si1green1a.setCommandedState(Turnout.CLOSED);
            si2yellow1a.setCommandedState(Turnout.CLOSED);
            si3red1a.setCommandedState(Turnout.THROWN);
        } else {
            // set green
            si1green1a.setCommandedState(Turnout.THROWN);
            si2yellow1a.setCommandedState(Turnout.CLOSED);
            si3red1a.setCommandedState(Turnout.CLOSED);
        }

        // section 1B
        if (
                ( tu3now )
             || ( !tu3now && bo4now )
             || ( !tu3now && !tu12now )
            ) {
            // set red
            si4green1b.setCommandedState(Turnout.CLOSED);
            si5yellow1b.setCommandedState(Turnout.CLOSED);
            si6red1b.setCommandedState(Turnout.THROWN);
        } else {
            // set green
            si4green1b.setCommandedState(Turnout.THROWN);
            si5yellow1b.setCommandedState(Turnout.CLOSED);
            si6red1b.setCommandedState(Turnout.CLOSED);
        }
    }

    // initialize logging
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(CrrSection.class.getName());

}


/* @(#)CrrSection.java */
