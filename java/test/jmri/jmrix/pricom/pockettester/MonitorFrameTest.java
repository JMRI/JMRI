package jmri.jmrix.pricom.pockettester;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * JUnit tests for the MonitorFrame class
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class MonitorFrameTest {

    @Test
    @DisabledIfHeadless
    public void testCreate() {

        MonitorFrame monitorFrame = new MonitorFrame();
        Assertions.assertNotNull(monitorFrame);
    }

    // create and show, with some data present
    @Test
    @DisabledIfHeadless
    public void testShow() {

        MonitorFrame f = new MonitorFrame();
        f.initComponents();
        f.setVisible(true);
        f.asciiFormattedMessage(TestConstants.version);
        f.asciiFormattedMessage(TestConstants.speed0003A);
        f.asciiFormattedMessage(TestConstants.idlePacket);
        f.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();

        JUnitUtil.initDefaultUserMessagePreferences();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager();
        JUnitUtil.tearDown();
    }
}
