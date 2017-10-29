package jmri.jmrit.speedometer;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;


/**
 * Test simple functioning of SpeedometerAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SpeedometerActionTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedometerAction action = new SpeedometerAction();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedometerAction action = new SpeedometerAction("Test SpeedometerAction");
        Assert.assertNotNull("exists", action);
    }

    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testMakePanel(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedometerAction action = new SpeedometerAction("Test SpeedometerAction");
        action.makePanel(); // this should throw an IllegalArgumentException.
    }

    @Test
    public void testActionCreateAndFire() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedometerAction action = new SpeedometerAction("Sensor Group");
        action.actionPerformed(null);
        // wait for frame with "Speedometer" in title, case insensitive
        // first boolean is false for exact to allow substring to match
        // second boolean is false to all case insensitive match
        JFrame frame = JFrameOperator.waitJFrame("Speedometer", false, false);
        Assert.assertNotNull(frame);
        // verify the action provided the expected frame class
        Assert.assertEquals(SpeedometerFrame.class.getName(), frame.getClass().getName());
        JUnitUtil.dispose(frame);
    }



    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {        JUnitUtil.tearDown();    }
}
