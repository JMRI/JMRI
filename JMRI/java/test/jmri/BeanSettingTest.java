package jmri;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the BeanSetting class
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class BeanSettingTest {

    @Test
    public void testCtorNullBean() {
        // JUnit 5 assertThrows would be good here
        boolean thrown = false;
        try {
            new BeanSetting(null, 0);
        } catch (NullPointerException ex) {
            thrown = true;
        }
        Assert.assertTrue("Expected exception thrown", thrown);
    }

    @Test
    public void testCheckSensor() throws JmriException {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Sensor s = sm.provideSensor("IS12");

        BeanSetting b = new BeanSetting(s, Sensor.ACTIVE);
        Assert.assertTrue("Initial check of sensor", !b.check());

        s.setState(Sensor.ACTIVE);
        Assert.assertTrue("check of ACTIVE sensor", b.check());
    }

    @Test
    public void testCheckTurnout() throws JmriException {
        TurnoutManager sm = InstanceManager.getDefault(TurnoutManager.class);
        Turnout s = sm.provideTurnout("IT12");

        BeanSetting b = new BeanSetting(s, Turnout.THROWN);
        Assert.assertTrue("Initial check of turnout", !b.check());

        s.setState(Turnout.THROWN);
        Assert.assertTrue("check of THROWN turnout", b.check());
    }

    @Test
    public void testEquals() {
        TurnoutManager sm = InstanceManager.getDefault(TurnoutManager.class);
        Turnout s1 = sm.provideTurnout("IT12");
        Turnout s2 = sm.provideTurnout("IT14");

        BeanSetting b1 = new BeanSetting(s1, Turnout.THROWN);
        BeanSetting b2 = new BeanSetting(s2, Turnout.THROWN);
        BeanSetting b3 = new BeanSetting(s1, Turnout.CLOSED);
        BeanSetting b4 = new BeanSetting(s2, Turnout.CLOSED);

        BeanSetting b5 = new BeanSetting(s1, Turnout.THROWN);
        BeanSetting b6 = new BeanSetting(s2, Turnout.THROWN);

        Assert.assertTrue(b1.equals(b1));
        Assert.assertFalse(b1.equals(b2));
        Assert.assertFalse(b1.equals(b3));
        Assert.assertFalse(b1.equals(b4));
        Assert.assertTrue(b1.equals(b5));
        Assert.assertFalse(b1.equals(b6));

        Assert.assertFalse(b2.equals(b1));
        Assert.assertTrue(b2.equals(b2));
        Assert.assertFalse(b2.equals(b3));
        Assert.assertFalse(b2.equals(b4));
        Assert.assertFalse(b2.equals(b5));
        Assert.assertTrue(b2.equals(b6));

        Assert.assertFalse(b3.equals(b1));
        Assert.assertFalse(b3.equals(b2));
        Assert.assertTrue(b3.equals(b3));
        Assert.assertFalse(b3.equals(b4));
        Assert.assertFalse(b3.equals(b5));
        Assert.assertFalse(b3.equals(b6));

        Assert.assertFalse(b4.equals(b1));
        Assert.assertFalse(b4.equals(b2));
        Assert.assertFalse(b4.equals(b3));
        Assert.assertTrue(b4.equals(b4));
        Assert.assertFalse(b4.equals(b5));
        Assert.assertFalse(b4.equals(b6));

        Assert.assertTrue(b5.equals(b1));
        Assert.assertFalse(b5.equals(b2));
        Assert.assertFalse(b5.equals(b3));
        Assert.assertFalse(b5.equals(b4));
        Assert.assertTrue(b5.equals(b5));
        Assert.assertFalse(b5.equals(b6));

        Assert.assertFalse(b6.equals(b1));
        Assert.assertTrue(b6.equals(b2));
        Assert.assertFalse(b6.equals(b3));
        Assert.assertFalse(b6.equals(b4));
        Assert.assertFalse(b6.equals(b5));
        Assert.assertTrue(b6.equals(b6));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
