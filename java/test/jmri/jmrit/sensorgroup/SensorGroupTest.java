package jmri.jmrit.sensorgroup;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 * Tests for classes in the jmri.jmrit.sensorgroup package
 *
 * @author	Bob Jacobsen Copyright 2003, 2007
 */
public class SensorGroupTest {

    @Test
    public void testFrameCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorGroupFrame frame = new SensorGroupFrame();
        Assert.assertNotNull(frame);
    }

    @Test
    public void testActionCreateAndFire() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SensorGroupAction a = new SensorGroupAction("Sensor Group");
        a.actionPerformed(null);
        // wait for frame with "Sensor Group" in title, case insensitive
        // first boolean is false for exact to allow substring to match
        // second boolean is false to all case insensitive match
        JFrame frame = JFrameOperator.waitJFrame("Sensor Group", false, false);
        Assert.assertNotNull(frame);
        // verify the action provided the expected frame class
        Assert.assertEquals(SensorGroupFrame.class.getName(), frame.getClass().getName());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
