package jmri.jmrit.speedometer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Test simple functioning of SpeedometerAction
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class SpeedometerActionTest {

    @Test
    public void testCtor() {
        SpeedometerAction action = new SpeedometerAction();
        assertNotNull(action, "exists");
    }

    @Test
    public void testStringCtor() {
        SpeedometerAction action = new SpeedometerAction("Test SpeedometerAction");
        assertNotNull(action, "exists");
    }

    @Test
    public void testMakePanel(){
        SpeedometerAction action = new SpeedometerAction("Test SpeedometerAction");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> action.makePanel());
        assertNotNull(ex);
    }

    @Test
    @DisabledIfHeadless
    public void testActionCreateAndFire() {
        SpeedometerAction action = new SpeedometerAction("Sensor Group");
        action.actionPerformed(null);
        // wait for frame with "Speedometer" in title, case insensitive
        // first boolean is false for exact to allow substring to match
        // second boolean is false to all case insensitive match
        JFrame frame = JFrameOperator.waitJFrame("Speedometer", false, false);
        assertNotNull(frame);
        // verify the action provided the expected frame class
        assertEquals(SpeedometerFrame.class.getName(), frame.getClass().getName());
        JUnitUtil.dispose(frame);
    }



    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
