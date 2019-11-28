package jmri;

import java.util.Hashtable;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.SignalMastLogic class
 *
 * @author	Egbert Broerse Copyright 2017
 */
public class SignalMastLogicTest {

    @Test
    public void testSetup() {
        jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);
        // provide 2 turnouts:
        Turnout it1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        Turnout it2 = InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        // provide 2 sensors:
        Sensor is1 = InstanceManager.sensorManagerInstance().provideSensor("IS1");
        Sensor is2 = InstanceManager.sensorManagerInstance().provideSensor("IS2");
        // provide 3 virtual signal masts:
        SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0001)");
        Assert.assertNotNull("SignalMast is null!", sm1);
        SignalMast sm2 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)");
        Assert.assertNotNull("SignalMast is null!", sm2);
        SignalMast sm3 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0003)");
        Assert.assertNotNull("SignalMast is null!", sm3);
        // provide a signal mast logic:
        SignalMastLogic sml = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).newSignalMastLogic(sm1);
        sml.setDestinationMast(sm2);
        Assert.assertNotNull("SignalMastLogic is null!", sml);
        sml.allowAutoMaticSignalMastGeneration(false, sm2);
        // add a control sensor
        sml.addSensor("IS1", 1, sm2); // Active
        // check config
        Assert.assertEquals("IS1 included", true, sml.isSensorIncluded(is1, sm2));
        Assert.assertEquals("IS2 not included", false, sml.isSensorIncluded(is2, sm2));
        Assert.assertEquals("IS1 state", 1, sml.getSensorState(is1, sm2));
        // add 1 control turnout
        Hashtable<NamedBeanHandle<Turnout>, Integer> hashTurnouts = new Hashtable<>();
        NamedBeanHandle<Turnout> namedTurnout1 = nbhm.getNamedBeanHandle("IT1", it1);
        hashTurnouts.put(namedTurnout1, 1); // 1 = Closed
        sml.setTurnouts(hashTurnouts, sm2);
        // check config
        Assert.assertTrue("IT1 included", sml.isTurnoutIncluded(it1, sm2));
        Assert.assertFalse("IT2 before", sml.isTurnoutIncluded(it2, sm2));
        // add another control turnout
        NamedBeanHandle<Turnout> namedTurnout2 = nbhm.getNamedBeanHandle("IT2", it2);
        hashTurnouts.put(namedTurnout2, 2); // 2 = Thrown
        sml.setTurnouts(hashTurnouts, sm2);
        Assert.assertEquals("IT2 after", true, sml.isTurnoutIncluded(it2, sm2));
        Assert.assertEquals("IT1 state", 1, sml.getTurnoutState(it1, sm2));
        // add a control signal mast
        Hashtable<SignalMast, String> hashSignalMast = new Hashtable<>();
        hashSignalMast.put(sm3, "Stop");
        sml.setMasts(hashSignalMast, sm2);
        // check config
        Assert.assertEquals("SM3 included", true, sml.isSignalMastIncluded(sm3, sm2));
        Assert.assertEquals("SM3 aspect before", "Stop", sml.getSignalMastState(sm3, sm2));
        // set aspect to Proceed
        hashSignalMast.put(sm3, "Proceed");
        sml.setMasts(hashSignalMast, sm2);
        Assert.assertEquals("SM3 aspect after", "Proceed", sml.getSignalMastState(sm3, sm2));
        // set comment
        sml.setComment("SMLTest", sm2);
        Assert.assertEquals("comment", "SMLTest", sml.getComment(sm2));
        sml.initialise(sm2);
        // set SML disabled
        sml.setDisabled(sm2);
        Assert.assertEquals("disabled", false, sml.isEnabled(sm2));
        // change source mast and check
        sml.replaceSourceMast(sm1, sm3);
        Assert.assertFalse("sourcemast", sml.getSourceMast() == sm1);
        Assert.assertTrue("sourcemast", sml.getSourceMast() == sm3);
        // clean up
        sml.dispose();
    }

    /**
     * Check that you can rename the SignalMast user names
     */
    @Test
    public void testRename() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        // provide 2 virtual signal masts:
        SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0001)");
        Assert.assertNotNull("SignalMast sm1 is null!", sm1);
        SignalMast sm2 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)");
        Assert.assertNotNull("SignalMast sm2 is null!", sm2);

        // Change logic delay from 500ms to 20ms to speed tests:
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).setSignalLogicDelay(20);

        // provide a signal mast logic:
        SignalMastLogic sml = InstanceManager.getDefault(jmri.SignalMastLogicManager.class).newSignalMastLogic(sm1);
        sml.setDestinationMast(sm2);
        Assert.assertNotNull("SignalMastLogic is null!", sml);

        sml.initialise();
        JUnitUtil.waitFor( ()->{ return sm1.getAspect().equals("Medium Approach"); }, "sm1 aspect (1)" );
        JUnitUtil.waitFor( ()->{ return sm2.getAspect().equals("Stop"); }, "sm2 aspect (1)" );
        
        sm2.setAspect("Clear");
        JUnitUtil.waitFor( ()->{ return sm1.getAspect().equals("Clear"); }, "sm1 aspect (2)" );
        JUnitUtil.waitFor( ()->{ return sm2.getAspect().equals("Clear"); }, "sm2 aspect (2)" );

        // rename the masts
        sm1.setUserName("new name 1");
        sm2.setUserName("new name 2");

        sm2.setAspect("Stop");
        JUnitUtil.waitFor( ()->{ return sm1.getAspect().equals("Medium Approach"); }, "sm1 aspect (3)" );
        JUnitUtil.waitFor( ()->{ return sm2.getAspect().equals("Stop"); }, "sm2 aspect (3)" );

        // clean up
        sml.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
