package jmri.jmrix.pricom.pockettester;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the MonitorFrame class
 *
 * @author	Bob Jacobsen Copyright 2005
 */
public class MonitorFrameTest {

    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MonitorFrame monitorFrame = new MonitorFrame();
        Assert.assertNotNull(monitorFrame);
    }

    // create and show, with some data present
    @Test
    public void testShow() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        MonitorFrame f = new MonitorFrame();
        f.initComponents();
        f.setVisible(true);
        f.asciiFormattedMessage(PackageTest.version);
        f.asciiFormattedMessage(PackageTest.speed0003A);
        f.asciiFormattedMessage(PackageTest.idlePacket);
        f.dispose();
    }

    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
