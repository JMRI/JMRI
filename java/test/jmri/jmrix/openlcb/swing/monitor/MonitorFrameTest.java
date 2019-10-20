package jmri.jmrix.openlcb.swing.monitor;

import java.awt.GraphicsEnvironment;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.jmrix.can.swing.monitor.MonitorFrame class
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class MonitorFrameTest {

    private String testFormatted;
    private String testRaw;

    private TrafficControllerScaffold tcs = null;
    private CanSystemConnectionMemo memo = null;

    @Test
    public void testFormatMsg() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        MonitorPane f = new MonitorPane() {

            @Override
            public void nextLine(String s1, String s2) {
                testFormatted = s1;
                testRaw = s2;
            }
        };
        f.initComponents(memo);

        jmri.jmrix.can.CanMessage msg
                = new jmri.jmrix.can.CanMessage(
                        new int[]{1, 2}, 0x12345678);
        msg.setExtended(true);

        f.message(msg);

        Assert.assertEquals("formatted", "S: Alias 0x678 CID 2 frame\n", testFormatted);
        Assert.assertEquals("raw", "[12345678] 01 02                  ", testRaw);
        f.dispose();
    }

    @Test
    public void testFormatReply() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        MonitorPane f = new MonitorPane() {
            @Override
            public void nextLine(String s1, String s2) {
                testFormatted = s1;
                testRaw = s2;
            }
        };
        f.initComponents(memo);

        jmri.jmrix.can.CanReply msg
                = new jmri.jmrix.can.CanReply(
                        new int[]{1, 2});
        msg.setExtended(true);
        msg.setHeader(0x12345678);

        f.reply(msg);

        Assert.assertEquals("formatted", "R: Alias 0x678 CID 2 frame\n", testFormatted);
        Assert.assertEquals("raw", "[12345678] 01 02                  ", testRaw);
        f.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        jmri.util.JUnitUtil.initConfigureManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        tcs = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
        jmri.InstanceManager.setDefault(CanSystemConnectionMemo.class, memo);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetWindows(false, false);
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
