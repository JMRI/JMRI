package jmri;

import jmri.util.JUnitUtil;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the BeanSetting class
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class BeanSettingTest extends TestCase {

    public void testCreate() {
        new BeanSetting(null, 0);
    }

    public void testCheckSensor() throws JmriException {
        SensorManager sm = new jmri.managers.InternalSensorManager();
        Sensor s = sm.provideSensor("IS12");

        BeanSetting b = new BeanSetting(s, Sensor.ACTIVE);
        Assert.assertTrue("Initial check of sensor", !b.check());

        s.setState(Sensor.ACTIVE);
        Assert.assertTrue("check of ACTIVE sensor", b.check());
    }

    public void testCheckTurnout() throws JmriException {
        TurnoutManager sm = new jmri.managers.InternalTurnoutManager();
        Turnout s = sm.provideTurnout("IT12");

        BeanSetting b = new BeanSetting(s, Turnout.THROWN);
        Assert.assertTrue("Initial check of turnout", !b.check());

        s.setState(Turnout.THROWN);
        Assert.assertTrue("check of THROWN turnout", b.check());
    }

    public void testEquals() {
        TurnoutManager sm = new jmri.managers.InternalTurnoutManager();
        Turnout s1 = sm.provideTurnout("IT12");
        Turnout s2 = sm.provideTurnout("IT14");

        BeanSetting b1 = new BeanSetting(s1, Turnout.THROWN);
        BeanSetting b2 = new BeanSetting(s2, Turnout.THROWN);
        BeanSetting b3 = new BeanSetting(s1, Turnout.CLOSED);
        BeanSetting b4 = new BeanSetting(s2, Turnout.CLOSED);
        
        BeanSetting b5 = new BeanSetting(s1, Turnout.THROWN);
        BeanSetting b6 = new BeanSetting(s2, Turnout.THROWN);

        Assert.assertTrue (b1.equals(b1));
        Assert.assertFalse(b1.equals(b2));
        Assert.assertFalse(b1.equals(b3));
        Assert.assertFalse(b1.equals(b4));
        Assert.assertTrue (b1.equals(b5));
        Assert.assertFalse(b1.equals(b6));
        
        Assert.assertFalse(b2.equals(b1));
        Assert.assertTrue (b2.equals(b2));
        Assert.assertFalse(b2.equals(b3));
        Assert.assertFalse(b2.equals(b4));
        Assert.assertFalse(b2.equals(b5));
        Assert.assertTrue (b2.equals(b6));
        
        Assert.assertFalse(b3.equals(b1));
        Assert.assertFalse(b3.equals(b2));
        Assert.assertTrue (b3.equals(b3));
        Assert.assertFalse(b3.equals(b4));
        Assert.assertFalse(b3.equals(b5));
        Assert.assertFalse(b3.equals(b6));
        
        Assert.assertFalse(b4.equals(b1));
        Assert.assertFalse(b4.equals(b2));
        Assert.assertFalse(b4.equals(b3));
        Assert.assertTrue (b4.equals(b4));
        Assert.assertFalse(b4.equals(b5));
        Assert.assertFalse(b4.equals(b6));
        
        Assert.assertTrue (b5.equals(b1));
        Assert.assertFalse(b5.equals(b2));
        Assert.assertFalse(b5.equals(b3));
        Assert.assertFalse(b5.equals(b4));
        Assert.assertTrue (b5.equals(b5));
        Assert.assertFalse(b5.equals(b6));

        Assert.assertFalse(b6.equals(b1));
        Assert.assertTrue (b6.equals(b2));
        Assert.assertFalse(b6.equals(b3));
        Assert.assertFalse(b6.equals(b4));
        Assert.assertFalse(b6.equals(b5));
        Assert.assertTrue (b6.equals(b6));
    }
    
    // from here down is testing infrastructure
    public BeanSettingTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {BeanSettingTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(BeanSettingTest.class);
        return suite;
    }

    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}
