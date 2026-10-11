package jmri;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the BeanSetting class
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class BeanSettingTest {

    @Test
    public void testCtorNullBean() {
        Exception ex = assertThrows(NullPointerException.class, () -> {
            assertNotNull(new BeanSetting(null, 0));
        });
        assertNotNull(ex, "Expected exception thrown");
    }

    @Test
    public void testCheckSensor() throws JmriException {
        SensorManager sm = InstanceManager.getDefault(SensorManager.class);
        Sensor s = sm.provideSensor("IS12");

        BeanSetting b = new BeanSetting(s, Sensor.ACTIVE);
        assertFalse( b.check(), "Initial check of sensor");

        s.setState(Sensor.ACTIVE);
        assertTrue( b.check(), "check of ACTIVE sensor");
    }

    @Test
    public void testCheckTurnout() throws JmriException {
        TurnoutManager sm = InstanceManager.getDefault(TurnoutManager.class);
        Turnout s = sm.provideTurnout("IT12");

        BeanSetting b = new BeanSetting(s, Turnout.THROWN);
        assertFalse( b.check(), "Initial check of turnout");

        s.setState(Turnout.THROWN);
        assertTrue( b.check(), "check of THROWN turnout");
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

        assertTrue(b1.equals(b1));
        assertFalse(b1.equals(b2));
        assertFalse(b1.equals(b3));
        assertFalse(b1.equals(b4));
        assertTrue(b1.equals(b5));
        assertFalse(b1.equals(b6));

        assertFalse(b2.equals(b1));
        assertTrue(b2.equals(b2));
        assertFalse(b2.equals(b3));
        assertFalse(b2.equals(b4));
        assertFalse(b2.equals(b5));
        assertTrue(b2.equals(b6));

        assertFalse(b3.equals(b1));
        assertFalse(b3.equals(b2));
        assertTrue(b3.equals(b3));
        assertFalse(b3.equals(b4));
        assertFalse(b3.equals(b5));
        assertFalse(b3.equals(b6));

        assertFalse(b4.equals(b1));
        assertFalse(b4.equals(b2));
        assertFalse(b4.equals(b3));
        assertTrue(b4.equals(b4));
        assertFalse(b4.equals(b5));
        assertFalse(b4.equals(b6));

        assertTrue(b5.equals(b1));
        assertFalse(b5.equals(b2));
        assertFalse(b5.equals(b3));
        assertFalse(b5.equals(b4));
        assertTrue(b5.equals(b5));
        assertFalse(b5.equals(b6));

        assertFalse(b6.equals(b1));
        assertTrue(b6.equals(b2));
        assertFalse(b6.equals(b3));
        assertFalse(b6.equals(b4));
        assertFalse(b6.equals(b5));
        assertTrue(b6.equals(b6));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
