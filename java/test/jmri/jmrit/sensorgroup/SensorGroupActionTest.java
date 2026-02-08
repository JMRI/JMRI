package jmri.jmrit.sensorgroup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.swing.JFrame;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import org.netbeans.jemmy.operators.JFrameOperator;

/**
 *
 * @author Bob Jacobsen Copyright 2003, 2007
 * @author Paul Bender Copyright (C) 2017
 */
public class SensorGroupActionTest {

    @Test
    public void testCTor() {
        SensorGroupAction t = new SensorGroupAction();
        assertNotNull(t, "exists");
    }

    @Test
    @DisabledIfHeadless
    public void testActionCreateAndFire() {
        SensorGroupAction a = new SensorGroupAction("Sensor Group");
        a.actionPerformed(null);
        // wait for frame with "Sensor Group" in title, case insensitive
        // first boolean is false for exact to allow substring to match
        // second boolean is false to all case insensitive match
        JFrame frame = JFrameOperator.waitJFrame("Sensor Group", false, false);
        assertNotNull(frame);
        // verify the action provided the expected frame class
        assertEquals(SensorGroupFrame.class.getName(), frame.getClass().getName());
        JUnitUtil.dispose(frame);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(SensorGroupActionTest.class);

}
