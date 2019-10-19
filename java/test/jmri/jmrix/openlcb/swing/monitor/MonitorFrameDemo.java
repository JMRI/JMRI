package jmri.jmrix.openlcb.swing.monitor;

import java.awt.GraphicsEnvironment;
import javax.swing.JFrame;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.can.swing.monitor.MonitorFrame class
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class MonitorFrameDemo {

    private String testFormatted;
    private String testRaw;
    private CanSystemConnectionMemo memo = null;

    class OurScaffold extends TrafficControllerScaffold {

        /*
         * Forward CanMessage to object under test
         */
        public void testMessage(CanMessage f) {
            // FIXME: must clone, iterator is not threadsafe.
            for (jmri.jmrix.AbstractMRListener c : cmdListeners) {
                ((CanListener) c).message(f);
            }
        }

        public void testReply(CanReply f) {
            // FIXME: must clone, iterator is not threadsafe.
            for (jmri.jmrix.AbstractMRListener c : cmdListeners) {
                ((CanListener) c).reply(f);
            }
        }
    }

    private OurScaffold tcs = null;

    @Test
    public void testFireViaAction() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new MonitorAction().actionPerformed(null);

        jmri.jmrix.can.CanMessage msg
                = new jmri.jmrix.can.CanMessage(
                        new int[]{1, 2}, 0x12345678);
        msg.setExtended(true);

        tcs.testMessage(msg);

        jmri.jmrix.can.CanReply reply
                = new jmri.jmrix.can.CanReply(
                        new int[]{1, 2});
        reply.setExtended(true);
        reply.setHeader(0x12345678);

        tcs.testReply(reply);

    }

    @Test
    @Ignore("Duplicates Test in MonitorFrameTest")
    public void XtestFormatMsg() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        MonitorPane f = new MonitorPane() {
            @Override
            public void nextLine(String s1, String s2) {
                testFormatted = s1;
                testRaw = s2;
            }
        };
        memo.setTrafficController(tcs);
        f.initComponents(memo);

        jmri.jmrix.can.CanMessage msg
                = new jmri.jmrix.can.CanMessage(
                        new int[]{1, 2}, 0x12345678);
        msg.setExtended(true);

        f.message(msg);

        JFrame frame = new JFrame("Monitor OpenLCB message");
        frame.setLayout(new java.awt.FlowLayout());
        frame.add(f);
        frame.pack();
        frame.setVisible(true);

        Assert.assertEquals("formatted", "S: Alias 0x678 CID 2 frame\n", testFormatted);
        Assert.assertEquals("raw", "[12345678] 01 02                  ", testRaw);
        frame.dispose();
    }

    @Test
    @Ignore("Duplicates Test in MonitorFrameTest")
    public void XtestFormatReply() throws Exception {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        MonitorPane f = new MonitorPane() {
            @Override
            public void nextLine(String s1, String s2) {
                testFormatted = s1;
                testRaw = s2;
            }
        };
        memo.setTrafficController(tcs);
        f.initComponents(memo);

        jmri.jmrix.can.CanReply msg
                = new jmri.jmrix.can.CanReply(
                        new int[]{1, 2});
        msg.setExtended(true);
        msg.setHeader(0x12345678);

        f.setVisible(true);

        f.reply(msg);

        JFrame frame = new JFrame("Monitor OpenLCB message");
        frame.setLayout(new java.awt.FlowLayout());
        frame.add(f);
        frame.pack();
        frame.setVisible(true);

        Assert.assertEquals("formatted", "R: Alias 0x678 CID 2 frame\n", testFormatted);
        Assert.assertEquals("raw", "[12345678] 01 02                  ", testRaw);
        frame.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();

        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
        memo = new CanSystemConnectionMemo();
        tcs = new OurScaffold();

        memo.setTrafficController(tcs);
        jmri.InstanceManager.setDefault(CanSystemConnectionMemo.class, memo);
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetWindows(false, false);
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }
}
