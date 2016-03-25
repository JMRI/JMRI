// MonitorFrameTest.java
package jmri.jmrix.openlcb.swing.monitor;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.can.swing.monitor.MonitorFrame class
 *
 * @author Bob Jacobsen Copyright 2010
 */
public class MonitorFrameTest extends TestCase {

    String testFormatted;
    String testRaw;

    public void testFormatMsg() throws Exception {
        // skip if headless, as requires display to show
        if (System.getProperty("jmri.headlesstest", "false").equals("true")) {
            return;
        }

        TrafficControllerScaffold tcs = new TrafficControllerScaffold();

        MonitorPane f = new MonitorPane() {

            public void nextLine(String s1, String s2) {
                testFormatted = s1;
                testRaw = s2;
            }
        };
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
        f.initComponents(memo);

        jmri.jmrix.can.CanMessage msg
                = new jmri.jmrix.can.CanMessage(
                        new int[]{1, 2}, 0x12345678);
        msg.setExtended(true);

        f.message(msg);

        Assert.assertEquals("formatted", "S: Alias 0x678 CID 2 frame\n", testFormatted);
        Assert.assertEquals("raw", "[12345678] 01 02                  ", testRaw);
        memo.dispose();
    }

    public void testFormatReply() throws Exception {
        // skip if headless, as requires display to show
        if (System.getProperty("jmri.headlesstest", "false").equals("true")) {
            return;
        }

        TrafficControllerScaffold tcs = new TrafficControllerScaffold();

        MonitorPane f = new MonitorPane() {
            public void nextLine(String s1, String s2) {
                testFormatted = s1;
                testRaw = s2;
            }
        };
        CanSystemConnectionMemo memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
        f.initComponents(memo);

        jmri.jmrix.can.CanReply msg
                = new jmri.jmrix.can.CanReply(
                        new int[]{1, 2});
        msg.setExtended(true);
        msg.setHeader(0x12345678);

        f.reply(msg);

        Assert.assertEquals("formatted", "R: Alias 0x678 CID 2 frame\n", testFormatted);
        Assert.assertEquals("raw", "[12345678] 01 02                  ", testRaw);
        memo.dispose();
    }

    // from here down is testing infrastructure
    public MonitorFrameTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.AllTest.initLogging();
        String[] testCaseName = {"-noloading", MonitorFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(MonitorFrameTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
