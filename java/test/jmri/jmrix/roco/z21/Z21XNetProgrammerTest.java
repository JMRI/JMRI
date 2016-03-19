package jmri.jmrix.roco.z21;

import jmri.JmriException;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the z21XNetProgrammer class
 *
 * @author	Bob Jacobsen
 */
public class Z21XNetProgrammerTest extends jmri.jmrix.lenz.XNetProgrammerTest {

    static final int RESTART_TIME = 20;

    @Override
    public void testWriteCvSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        Z21XNetProgrammer p = new Z21XNetProgrammer(t) {
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // and do the write
        p.writeCV(29, 34, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "24 12 00 1C 22 08", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x64);
        mr1.setElement(1, 0x14);
        mr1.setElement(2, 0x00);
        mr1.setElement(3, 0x1C);
        mr1.setElement(4, 0x22);
        mr1.setElement(5, 0x4E);
        t.sendTestMessage(mr1);

        // At this point, the z21XPressNetProgrammer 
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");

        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    @Override
    public void testReadCvSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        Z21XNetProgrammer p = new Z21XNetProgrammer(t) {
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // and do the read
        p.readCV(29, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "23 11 00 1C 2E", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x64);
        mr1.setElement(1, 0x14);
        mr1.setElement(2, 0x00);
        mr1.setElement(3, 0x1C);
        mr1.setElement(4, 0x22);
        mr1.setElement(5, 0x4E);
        t.sendTestMessage(mr1);

        // At this point, the z21XPressNetProgrammer 
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");

        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testWriteCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use 
    // different XPressNet commands.
    @Override
    public void testWriteHighCvSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        Z21XNetProgrammer p = new Z21XNetProgrammer(t) {
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // and do the write
        p.writeCV(300, 34, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "24 12 01 2B 22 3E", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x64);
        mr1.setElement(1, 0x14);
        mr1.setElement(2, 0x01);
        mr1.setElement(3, 0x2B);
        mr1.setElement(4, 0x22);
        mr1.setElement(5, 0x78);
        t.sendTestMessage(mr1);

        // At this point, the z21XPressNetProgrammer 
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");

        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testReadCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use 
    // different XPressNet commands.
    @Override
    public void testReadCvHighSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        Z21XNetProgrammer p = new Z21XNetProgrammer(t) {
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // and do the read
        p.readCV(300, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "23 11 01 2B 18", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x64);
        mr1.setElement(1, 0x14);
        mr1.setElement(2, 0x01);
        mr1.setElement(3, 0x2B);
        mr1.setElement(4, 0x22);
        mr1.setElement(5, 0x78);
        t.sendTestMessage(mr1);

        // At this point, the z21XPressNetProgrammer 
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // from here down is testing infrastructure
    public Z21XNetProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();
        String[] testCaseName = {"-noloading", Z21XNetProgrammerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(Z21XNetProgrammerTest.class);
        return suite;
    }

    // The minimal setup is for log4J
    // The minimal setup for log4J
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
