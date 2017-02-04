package jmri;

import jmri.InstanceManager;
import jmri.SignalMastLogic;
import jmri.SignalMast;
import jmri.Sensor;
import jmri.Turnout;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.SignalMastLogic class
 *
 * @author	Egbert Broerse Copyright 2017
 */
public class SignalMastLogicTest {

    @Test
    public void testSetup() {
        // provide 2 turnouts:
        Turnout it1 = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        Turnout it2 = InstanceManager.turnoutManagerInstance().provideTurnout("IT2");
        // provide 2 sensors:
        Sensor is1 = InstanceManager.sensorManagerInstance().provideSensor("IS1");
        Sensor is2 = InstanceManager.sensorManagerInstance().provideSensor("IS2");
        // provide 3 virtual signal masts:
        SignalMast sm1 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)");
        Assert.assertNotNull("SignalMast is null!", sm1);
        SignalMast sm2 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)");
        Assert.assertNotNull("SignalMast is null!", sm2);
        SignalMast sm3 = new jmri.implementation.VirtualSignalMast("IF$vsm:AAR-1946:CPL($0002)");
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
        // add a control turnout
        //sml.addTurnout("IT1", "Closed", sm2); // wrong method name
        // check config
        //Assert.assertEquals("IT1 included", true, sml.isTurnoutIncluded(it1, sm2));
        //Assert.assertEquals("IT2 not included", false, sml.isTurnoutIncluded(it2, sm2));
        //Assert.assertEquals("IS1 state", jmri.InstanceManager.turnoutManagerInstance().getClosedText(), sml.getTurnoutState(it1, sm2));
        // add a control signal mast
        //sml.addSignalMast("SM3", "Stop", sm2); // wrong method name
        // check config
        //Assert.assertEquals("SM3 included", true, sml.isSignalMastIncluded(sm3, sm2));
        //Assert.assertEquals("SM3 aspect", "Stop", sml.getSignalMastState(sm3, sm2));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
