package jmri;

import java.util.Hashtable;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the jmri.SignalMastLogic class
 *
 * @author Egbert Broerse Copyright 2017
 */
public class SignalMastLogicTest {

    @Test
    public void testSetup() {
        NamedBeanHandleManager nbhm = InstanceManager.getDefault(NamedBeanHandleManager.class);
        // provide 2 turnouts:
        Turnout it1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        Turnout it2 = InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        // provide 2 sensors:
        Sensor is1 = InstanceManager.sensorManagerInstance().provideSensor("IS1");
        Sensor is2 = InstanceManager.sensorManagerInstance().provideSensor("IS2");
        // provide 3 virtual signal masts:
        SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0001)");
        assertNotNull( sm1, "SignalMast is null!");
        SignalMast sm2 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)");
        assertNotNull( sm2, "SignalMast is null!");
        SignalMast sm3 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0003)");
        assertNotNull( sm3, "SignalMast is null!");
        // provide a signal mast logic:
        SignalMastLogic sml = InstanceManager.getDefault(SignalMastLogicManager.class).newSignalMastLogic(sm1);
        sml.setDestinationMast(sm2);
        assertNotNull( sml, "SignalMastLogic is null!");
        sml.allowAutoMaticSignalMastGeneration(false, sm2);
        // add a control sensor
        sml.addSensor("IS1", 1, sm2); // Active
        // check config
        assertTrue( sml.isSensorIncluded(is1, sm2), "IS1 included");
        assertFalse( sml.isSensorIncluded(is2, sm2), "IS2 not included");
        assertEquals( 1, sml.getSensorState(is1, sm2), "IS1 state");
        // add 1 control turnout
        Hashtable<NamedBeanHandle<Turnout>, Integer> hashTurnouts = new Hashtable<>();
        NamedBeanHandle<Turnout> namedTurnout1 = nbhm.getNamedBeanHandle("IT1", it1);
        hashTurnouts.put(namedTurnout1, 1); // 1 = Closed
        sml.setTurnouts(hashTurnouts, sm2);
        // check config
        assertTrue( sml.isTurnoutIncluded(it1, sm2), "IT1 included");
        assertFalse( sml.isTurnoutIncluded(it2, sm2), "IT2 before");
        // add another control turnout
        NamedBeanHandle<Turnout> namedTurnout2 = nbhm.getNamedBeanHandle("IT2", it2);
        hashTurnouts.put(namedTurnout2, 2); // 2 = Thrown
        sml.setTurnouts(hashTurnouts, sm2);
        assertTrue( sml.isTurnoutIncluded(it2, sm2), "IT2 after");
        assertEquals( 1, sml.getTurnoutState(it1, sm2), "IT1 state");
        // add a control signal mast
        Hashtable<SignalMast, String> hashSignalMast = new Hashtable<>();
        hashSignalMast.put(sm3, "Stop");
        sml.setMasts(hashSignalMast, sm2);
        // check config
        assertTrue( sml.isSignalMastIncluded(sm3, sm2), "SM3 included");
        assertEquals( "Stop", sml.getSignalMastState(sm3, sm2), "SM3 aspect before");
        // set aspect to Proceed
        hashSignalMast.put(sm3, "Proceed");
        sml.setMasts(hashSignalMast, sm2);
        assertEquals( "Proceed", sml.getSignalMastState(sm3, sm2), "SM3 aspect after");
        // set comment
        sml.setComment("SMLTest", sm2);
        assertEquals( "SMLTest", sml.getComment(sm2), "comment");
        sml.initialise(sm2);
        // set SML disabled
        sml.setDisabled(sm2);
        assertFalse( sml.isEnabled(sm2), "disabled");
        // change source mast and check
        sml.replaceSourceMast(sm1, sm3);
        assertFalse( sml.getSourceMast() == sm1, "sourcemast");
        assertTrue( sml.getSourceMast() == sm3, "sourcemast");
        // clean up
        sml.dispose();
    }

    /**
     * Check that you can rename the SignalMast user names
     */
    @Test
    public void testRename() {
        Assumptions.assumeFalse( Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"), "Ignoring intermittent test");

        // provide 2 virtual signal masts:
        SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0001)");
        assertNotNull( sm1, "SignalMast sm1 is null!");
        SignalMast sm2 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)");
        assertNotNull( sm2, "SignalMast sm2 is null!");

        // Change logic delay from 500ms to 20ms to speed tests:
        InstanceManager.getDefault(SignalMastLogicManager.class).setSignalLogicDelay(20);

        // provide a signal mast logic:
        SignalMastLogic sml = InstanceManager.getDefault(SignalMastLogicManager.class).newSignalMastLogic(sm1);
        sml.setDestinationMast(sm2);
        assertNotNull( sml, "SignalMastLogic is null!");

        sml.initialise();
        JUnitUtil.waitFor( ()-> "Medium Approach".equals(sm1.getAspect()), "sm1 aspect (1)" );
        JUnitUtil.waitFor( ()-> "Stop".equals(sm2.getAspect()), "sm2 aspect (1)" );

        sm2.setAspect("Clear");
        JUnitUtil.waitFor( ()-> "Clear".equals(sm1.getAspect()), "sm1 aspect (2)" );
        JUnitUtil.waitFor( ()-> "Clear".equals(sm2.getAspect()), "sm2 aspect (2)" );

        // rename the masts
        sm1.setUserName("new name 1");
        sm2.setUserName("new name 2");

        sm2.setAspect("Stop");
        JUnitUtil.waitFor( ()-> "Medium Approach".equals(sm1.getAspect()), "sm1 aspect (3)" );
        JUnitUtil.waitFor( ()-> "Stop".equals(sm2.getAspect()), "sm2 aspect (3)" );

        // clean up
        sml.dispose();
        sm2.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initDefaultUserMessagePreferences();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
