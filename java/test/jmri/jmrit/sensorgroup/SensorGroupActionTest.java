package jmri.jmrit.sensorgroup;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author	Bob Jacobsen Copyright 2003, 2007
 * @author Paul Bender Copyright (C) 2017	
 */
public class SensorGroupActionTest {

    @Test
    public void testCTor() {
        SensorGroupAction t = new SensorGroupAction();
        Assert.assertNotNull("exists",t);
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
        JUnitUtil.dispose(frame);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SensorGroupActionTest.class);

}
