// CrrSection.java

package apps.cornwall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;

/**
 * Abstract base class for Cornwall RR automation.
 * <P>
 * Defines useful constants, and handles basic structure of hte
 * update process.
 *
 * @author	Bob Jacobsen    Copyright (C) 2003
 * @version     $Revision$
 */
public abstract class CrrSection extends jmri.jmrit.automat.AbstractAutomaton {
    static final int RED    = SignalHead.RED;
    static final int YELLOW = SignalHead.YELLOW;
    static final int GREEN  = SignalHead.GREEN;
    static final int DARK   = SignalHead.DARK;
    static final int FLASHYELLOW = SignalHead.FLASHYELLOW;

    static final int ACTIVE   = Sensor.ACTIVE;
    static final int INACTIVE = Sensor.INACTIVE;

    static final int CLOSED   = Turnout.CLOSED;
    static final int THROWN   = Turnout.THROWN;

    /**
     * Calculate the signal settings
     */
    abstract void setOutput();

    /**
     * Locate and define the output Signal object, stored in sig
     */
    abstract void defineIO();

    /**
     * References the signalhead to be controlled.
     */
    SignalHead sig;

    /**
     * Array of sensors and/or turnouts needed as inputs; changes in these will
     * kick off processing
     */
    NamedBean[] inputs;


    /**
     * Obtain the output object, sets the output to an initial state
     * to make sure everything is consistent at the start.
     */
    protected void init() {
        TurnoutManager tm = InstanceManager.turnoutManagerInstance();
        SensorManager  sm = InstanceManager.sensorManagerInstance();
        SignalHeadManager hm = InstanceManager.signalHeadManagerInstance();
        if ( tm==null || sm==null || hm ==null) {
            log.error("Can't function with missing managers");
            return;
        }

        // get references to needed layout objects
        defineIO();

        // set up the initial correlation
        sig.setAppearance(SignalHead.RED);
        setOutput();
    }

    /**
     * Watch sensors, and when it changes adjust outputs to match.
     * @return Always returns true to continue operation
     */
    protected boolean handle() {
        log.debug("Waiting for state change");

        // wait until a sensor changes state
        waitChange(inputs);

        // recalculate outputs
        int oldAppearance = sig.getAppearance();
        setOutput();

        // report if needed
        int newAppearance = sig.getAppearance();

        if ( (oldAppearance != newAppearance) && log.isDebugEnabled()) {
            switch (newAppearance) {
            case DARK:   log.debug(sig.getUserName()+"set DARK"); break;
            case RED:    log.debug(sig.getUserName()+"set RED");  break;
            case YELLOW: log.debug(sig.getUserName()+"set YELLOW"); break;
            case GREEN:  log.debug(sig.getUserName()+"set GREEN"); break;
            default:     log.warn(sig.getUserName()+"set to unknown new appearance: "+newAppearance); break;
            }
        }

        return true;   // never terminate permanently
    }

    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD", "LI_LAZY_INIT_UPDATE_STATIC"})
    // this code uses statics poorly, but it's not threaded, and too old to fix, so mark as OK
    CrrSection() {
        SensorManager tm = InstanceManager.sensorManagerInstance();
        // initialize the static turnout list
        if (tu==null) {
            tu = new Sensor[]{
                null,
                tm.getByUserName("Cornwall Jct 1 tu(01)"),
                tm.getByUserName("Ridge tu(02)"),
                tm.getByUserName("Cornwall Jct 3 tu(03)"),
                tm.getByUserName("Reading Relay Track West Switch tu(04)"),
                tm.getByUserName("Lickdale East tu(05)"),
                tm.getByUserName("Colebrook West tu(06)"),
                tm.getByUserName("Colebrook East tu(07)"),
                tm.getByUserName("Colebrook Branch tu(08)"),
                tm.getByUserName("Conewago West tu(09)"),
                tm.getByUserName("Blackies tu(10)"),
                tm.getByUserName("Lickdale Xover tu(11)"),
                tm.getByUserName("East Cornwall Junction Switch tu(12)"),
                tm.getByUserName("Cornwall Mine Lead tu(13)"),
                tm.getByUserName("Lebanon Jct East tu(14)"),
                tm.getByUserName("Lebanon Jct West tu(15)"),
                tm.getByUserName("Lebanon Low Line West tu(16)"),
                tm.getByUserName("Conewago Xover West tu(17)"),
                tm.getByUserName("Conewago Xover East tu(18)"),
                tm.getByUserName("Conewago Reading West tu(19)"),
                tm.getByUserName("Conewago Reading East tu(20)"),
                tm.getByUserName("Conewago Departure East tu(21)"),
                tm.getByUserName("Conewago Reading Interchange East tu(22)"),
                tm.getByUserName("Lickdale Interchange NorthWest Switch tu(23)"),
                tm.getByUserName("Lickdale Interchange NorthEast Switch tu(24)"),
                tm.getByUserName("Lickdale Interchange SouthWest Switch tu(25)"),
                tm.getByUserName("Lickdale Interchange SouthEast Switch tu(26)")
            };
            // check for error!
            if (tu.length != 26+1) log.error("Unexpected tu[] length: "+tu.length);
            for (int i = 1; i<tu.length; i++)
                if (tu[i]==null) log.error("tu["+i+"] unexpectedly null");
        }

        // initialize the static occupancy detector list
        if (bo==null) {
            bo = new Sensor[]{
                null,
                tm.getByUserName("Q Curve bo(01)"),
                tm.getByUserName("PRR Highline 2 bo(02)"),
                tm.getByUserName("PRR Highline 1 bo(03)"),
                tm.getByUserName("Reading Relay Track 2 bo(04)"),
                tm.getByUserName("Ridge bo(05)"),
                tm.getByUserName("Lickdale Approach bo(06)"),
                tm.getByUserName("Lickdale Passenger Siding bo(07)"),
                tm.getByUserName("Lickdale Mainline bo(08)"),
                tm.getByUserName("Gate North bo(09)"),
                tm.getByUserName("Gate South bo(10)"),
                tm.getByUserName("Colebrook Main bo(11)"),
                tm.getByUserName("Colebrook Passing Siding bo(12)"),
                tm.getByUserName("Edisonville North bo(13)"),
                tm.getByUserName("Edisonville South bo(14)"),
                tm.getByUserName("Conewago Approach bo(15)"),
                tm.getByUserName("PRR/Lebanon Entrance bo(16)"),
                tm.getByUserName("Cornwall Relay 1 bo(17)"),
                tm.getByUserName("Reading Low Line Track 2 bo(18)"),
                tm.getByUserName("Reading Low Line Track 1 bo(19)"),
                tm.getByUserName("Lebanon Main Lead bo(20)"),
                tm.getByUserName("Lebanon Yard Lead bo(21)"),
                tm.getByUserName("Conewago Main West bo(22)"),
                tm.getByUserName("Conewago Yard West bo(23)"),
                tm.getByUserName("Conewago Main East bo(24)"),
                tm.getByUserName("Conewago Departure bo(25)"),
                tm.getByUserName("Conewago Reading Interchange bo(26)"),
                tm.getByUserName("Relay Track 2 bo(27)"),
                tm.getByUserName("Lickdale Interchange Track 4 bo(28)"),
                tm.getByUserName("Lickdale Interchange Track 3 bo(29)"),
                tm.getByUserName("Relay Track 1 bo(30)"),
                tm.getByUserName("Lickdale Interchange Track 2 bo(31)"),
                tm.getByUserName("Lickdale Interchange Track 1 bo(32)"),
                tm.getByUserName("Conewago Interlocking bo(33)"),
                tm.getByUserName("Conewago Pocket bo(34)")
            };
            // check for error!
            if (bo.length != 34+1) log.error("Unexpected bo[] length: "+bo.length);
            for (int i = 1; i<bo.length; i++)
                if (bo[i]==null) log.error("bo["+i+"] unexpectedly null");

            // Also initialize 'gate'
            gate = tm.getByUserName("Gate Power Interlock Gate");
            if (gate == null) log.error("Failed to initialize gate");
        }

        if (si == null) {
            // unlike the sections above, this is done programmatically
            si = new Turnout[133];  // allow room for si[0] = null through si[132]
            si[0] = null;

            int n = 1;
            TurnoutManager man = InstanceManager.turnoutManagerInstance();
            final String[] letters = new String[]{"a", "b", "c"};
            final String[] lights = new String[]{"green", "yellow", "red"};

            for (int sig = 1; sig<=29; sig++) {  // sig is signal number
                for (int letter = 0; letter<3; letter++) { // loop over signal a, b, c
                    for (int light = 0; light<3; light++) { // loop over green, yellow, red
                        String name = ""+sig
                                +letters[letter]+" "
                                +lights[light];
                        // see if that one exists, and store it
                        Turnout t = man.getByUserName(name);
                        if (t!=null) {
                            // yes, save
                            si[n] = t;
                            log.debug("Found si["+n+"] = "+name);
                            n++;
                        }
                    }
                }
            }
            if (n!=132+1) log.error("unexpected number of found si[] entries: "+n);
            if (si[0]!= null) log.error("unexpected non-null si[0]");
            if (si[132]==null) log.error("unexpected null in si[132]");
        }
    }

    /**
     * Java sensor object representing the "gate" input
     */
    static Sensor gate;

    /**
     * Java array of Sensor objects corresponding to TU() turnout sensors
     * in the C/MRI code.  Note that TU[1] is like TU(1) in BASIC, so there's
     * an extra null entry at the beginning (as TU[0]).
     * <P>
     * Initialization of the contents happens when the first CrrSection object
     * constructor is run.
     */
    static Sensor[] tu = null;

    /**
     * Java array of Sensor objects corresponding to BO() occupancy sensors
     * in the C/MRI code.  Note that BO[1] is like BO(1) in BASIC, so there's
     * an extra null entry at the beginning (as BO[0]).
     * <P>
     * Initialization of the contents happens when the first CrrSection object
     * constructor is run.
     */
    static Sensor[] bo = null;

    /**
     * Java array of Turnout objects corresponding to SI() signal outputs
     * in the C/MRI code.  Note that SI[1] is like SI(1) in BASIC, so there's
     * an extra null entry at the beginning (as SI[0]).
     * <P>
     * Initialization of the contents happens when the first CrrSection object
     * constructor is run.
     */
    static Turnout[] si = null;

    // initialize logging
    static Logger log = LoggerFactory.getLogger(CrrSection.class.getName());

}

/* @(#)CrrSection.java */
