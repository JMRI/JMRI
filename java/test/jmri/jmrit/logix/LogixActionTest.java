package jmri.jmrit.logix;

import jmri.InstanceManager;
import jmri.Memory;
import jmri.Sensor;
import jmri.SignalHead;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the OPath class
 *
 * @author	Pete Cressman Copyright 2014
 */
public class LogixActionTest extends TestCase {

    public void testLogixAction() throws Exception {
        jmri.configurexml.ConfigXmlManager cm = new jmri.configurexml.ConfigXmlManager() {
        };

        /* headless OK
        if (System.getProperty("jmri.headlesstest", "false").equals("true")) {
            return;
        }*/ 

        // load and display sample file. Panel file does not display screen
        java.io.File f = new java.io.File("java/test/jmri/jmrit/logix/valid/LogixActionTest.xml");
        cm.load(f);
        InstanceManager.getDefault(jmri.LogixManager.class).activateAllLogixs();

        Memory im6 = InstanceManager.memoryManagerInstance().getMemory("IM6");
        Assert.assertNotNull("Memory IM6", im6);
        Assert.assertEquals("Contents IM6", "EastToWestOnSiding", im6.getValue());

        /* Find Enable Logix button  <<< I'd like to use jfcunit but can't figure out the usage
         AbstractButtonFinder finder = new AbstractButtonFinder("Enable/Disable Tests" );
         JButton button = ( JButton ) finder.find();
         Assert.assertNotNull(button);   // Fails here after long wait. stack overflow?
         // Click button
         getHelper().enterClickAndLeave( new MouseEventData( this, button ) );
         */
        // OK, do it this way
        Sensor sensor = InstanceManager.sensorManagerInstance().getSensor("enableButton");
        Assert.assertNotNull("Sensor IS5", sensor);
        sensor.setState(Sensor.ACTIVE);
        sensor.setState(Sensor.INACTIVE);
        sensor.setState(Sensor.ACTIVE);
        SignalHead sh1 = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH1");
        Assert.assertEquals("SignalHead IH1", SignalHead.RED, sh1.getAppearance());

        // do some buttons -Sensors
        sensor = InstanceManager.sensorManagerInstance().getSensor("ISLITERAL");
        sensor.setState(Sensor.ACTIVE);		// activate direct logix action
        Sensor is1 = InstanceManager.sensorManagerInstance().getSensor("sensor1");
        Assert.assertEquals("direct set Sensor IS1 active", Sensor.ACTIVE, is1.getState());		// action
        sensor = InstanceManager.sensorManagerInstance().getSensor("ISINDIRECT");
        sensor.setState(Sensor.ACTIVE);		// activate Indirect logix action
        Assert.assertEquals("Indirect set Sensor IS1 inactive", Sensor.INACTIVE, is1.getState());		// action

        // SignalHead buttons
        Sensor is4 = InstanceManager.sensorManagerInstance().getSensor("IS4");
        is4.setState(Sensor.ACTIVE);		// activate direct logix action
        Assert.assertEquals("direct set SignalHead IH1 to Green", SignalHead.GREEN, sh1.getAppearance());
        is4.setState(Sensor.INACTIVE);		// activate direct logix action
        Assert.assertEquals("direct set SignalHead IH1 to Red", SignalHead.RED, sh1.getAppearance());

        Memory im3 = InstanceManager.memoryManagerInstance().getMemory("IM3");
        Assert.assertNotNull("Memory IM3", im3);
        Assert.assertEquals("Contents IM3", "IH1", im3.getValue());
        Sensor is3 = InstanceManager.sensorManagerInstance().getSensor("IS3");
        is3.setState(Sensor.ACTIVE);		// activate indirect logix action
        Assert.assertEquals("Indirect set SignalHead IH1 to Green", SignalHead.GREEN, sh1.getAppearance());
        is3.setState(Sensor.INACTIVE);		// activate indirect logix action
        Assert.assertEquals("Indirect set SignalHead IH1 to Red", SignalHead.RED, sh1.getAppearance());
        // change memory value
        im3.setValue("IH2");
        is3.setState(Sensor.ACTIVE);		// activate logix action
        Assert.assertEquals("Contents IM3", "IH2", im3.getValue());
        SignalHead sh2 = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead("IH2");
        Assert.assertEquals("Indirect SignalHead IH2", SignalHead.GREEN, sh2.getAppearance());

        // Turnout Buttons
        Sensor is6 = InstanceManager.sensorManagerInstance().getSensor("IS6");
        is6.setState(Sensor.ACTIVE);		// activate direct logix action
        Turnout it2 = InstanceManager.turnoutManagerInstance().getTurnout("IT2");
        Assert.assertEquals("direct set Turnout IT2 to Closed", Turnout.CLOSED, it2.getState());
        Memory im4 = InstanceManager.memoryManagerInstance().getMemory("IM4");
        Assert.assertEquals("Contents IM4", "IT3", im4.getValue());
        Sensor is7 = InstanceManager.sensorManagerInstance().getSensor("IS7");
        is7.setState(Sensor.INACTIVE);		// activate indirect logix action
        Turnout it3 = InstanceManager.turnoutManagerInstance().getTurnout("IT3");
        Assert.assertEquals("Indirect set Turnout IT2 to Thrown", Turnout.THROWN, it3.getState());
        is7.setState(Sensor.ACTIVE);		// activate indirect logix action
        Assert.assertEquals("Indirect set Turnout IT2 to Closed", Turnout.CLOSED, it3.getState());
        // change memory value
        im4.setValue("IT2");
        Assert.assertEquals("Contents IM4", "IT2", im4.getValue());
        is7.setState(Sensor.INACTIVE);		// activate indirect logix action
        Assert.assertEquals("Indirect set Turnout IT2 to Thrown", Turnout.THROWN, it2.getState());
        is7.setState(Sensor.ACTIVE);		// activate indirect logix action
        Assert.assertEquals("Indirect set Turnout IT2 to Closed", Turnout.CLOSED, it2.getState());

        // OBlock Buttons
        OBlock ob1 = InstanceManager.getDefault(OBlockManager.class).getOBlock("Left");
        Assert.assertEquals("OBlock OB1", (OBlock.OUT_OF_SERVICE | Sensor.INACTIVE), ob1.getState());
        OBlock ob2 = InstanceManager.getDefault(OBlockManager.class).getOBlock("Right");
        Assert.assertEquals("OBlock OB2", (OBlock.TRACK_ERROR | Sensor.INACTIVE), ob2.getState());
        Sensor is8 = InstanceManager.sensorManagerInstance().getSensor("IS8");
        is8.setState(Sensor.ACTIVE);			// direct action
        Assert.assertEquals("Direct set OBlock OB1 to normal", Sensor.INACTIVE, ob1.getState());
        is8.setState(Sensor.INACTIVE);			// direct action
        Assert.assertEquals("Direct set OBlock OB1 to OOS", (OBlock.OUT_OF_SERVICE | Sensor.INACTIVE), ob1.getState());
        Sensor is9 = InstanceManager.sensorManagerInstance().getSensor("IS9");
        is9.setState(Sensor.ACTIVE);			// indirect action
        Assert.assertEquals("Indirect set OBlock OB2 to normal", Sensor.INACTIVE, ob2.getState());
        // change memory value
        Memory im5 = InstanceManager.memoryManagerInstance().getMemory("IM5");
        im5.setValue("OB1");
        is9.setState(Sensor.INACTIVE);			// indirect action
        Assert.assertEquals("Indirect set OBlock OB1 to normal",
                (OBlock.TRACK_ERROR | OBlock.OUT_OF_SERVICE | Sensor.INACTIVE), ob1.getState());
        is9.setState(Sensor.ACTIVE);			// indirect action
        is8.setState(Sensor.ACTIVE);			// indirect action
        Assert.assertEquals("Direct set OBlock OB1 to normal", Sensor.INACTIVE, ob1.getState());

        // Warrant buttons
        Sensor is14 = InstanceManager.sensorManagerInstance().getSensor("IS14");
        is14.setState(Sensor.ACTIVE);			// indirect action
        Warrant w = InstanceManager.getDefault(WarrantManager.class).getWarrant("EastToWestOnSiding");
        Assert.assertTrue("warrant EastToWestOnSiding allocated", w.isAllocated());
        Sensor is15 = InstanceManager.sensorManagerInstance().getSensor("IS15");
        is15.setState(Sensor.ACTIVE);			// indirect action
        Assert.assertFalse("warrant EastToWestOnSiding deallocated", w.isAllocated());
        // change memory value
        im6.setValue("WestToEastOnMain");
        is14.setState(Sensor.INACTIVE);			// toggle
        is14.setState(Sensor.ACTIVE);			// indirect action
        Warrant w2 = InstanceManager.getDefault(WarrantManager.class).getWarrant("WestToEastOnMain");
        Assert.assertTrue("warrant WestToEastOnMain allocated", w2.isAllocated());
        im6.setValue("LeftToRightOnPath");
        is14.setState(Sensor.INACTIVE);			// toggle
        is14.setState(Sensor.ACTIVE);			// indirect action
        w = InstanceManager.getDefault(WarrantManager.class).getWarrant("LeftToRightOnPath");
        Assert.assertTrue("warrant LeftToRightOnPath allocated", w.isAllocated());
    }

    // from here down is testing infrastructure
    public LogixActionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LogixActionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(LogixActionTest.class);
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initMemoryManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initLogixManager();
        JUnitUtil.initConditionalManager();
    }

    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
