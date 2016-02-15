package jmri.jmrix.lenz.li100;

import jmri.JmriException;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.managers.DefaultProgrammerManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * LI100XNetProgrammerTest.java
 *
 * Description:	tests for the jmri.jmrix.lenz.li100.LI100XNetProgrammer class
 *
 * @author	Paul Bender
 * @version $Revision$
 */
public class LI100XNetProgrammerTest extends TestCase {

    static final int RESTART_TIME = 20;

    public void testCtor() {
// infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());

        LI100XNetProgrammer p = new LI100XNetProgrammer(t);
        Assert.assertNotNull(p);
    }

    public void testWriteCvSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        LI100XNetProgrammer p = new LI100XNetProgrammer(t) {
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // and do the write
        p.writeCV(29, 34, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "23 16 1D 22 0A", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);
        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x14);
        mr2.setElement(2, 0x1D);
        mr2.setElement(3, 0x22);
        mr2.setElement(4, 0x48);
        t.sendTestMessage(mr2);

        // At this point, the LI100 XPressNet programmer
        // deviates from the standard XPressNet programmer.  
        // the LI100 version requests an exit from service 
        // before sending a result to the registered listeners.
        Assert.assertEquals("mode message sent", 3, t.outbound.size());
        Assert.assertEquals("exit service mode message contents", "21 81 A0", t.outbound.elementAt(2).toString());

        // send reply stating service mode has exited       
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x61);
        mr3.setElement(1, 0x01);
        mr3.setElement(2, 0x60);
        t.sendTestMessage(mr3);

        jmri.util.JUnitUtil.releaseThread(this);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    public void testWriteRegisterSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        LI100XNetProgrammer p = new LI100XNetProgrammer(t) {
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // set register mode
        p.setMode(DefaultProgrammerManager.REGISTERMODE);

        // and do the write
        p.writeCV(29, 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "23 12 05 0C 38", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x10);
        mr2.setElement(2, 0x05);
        mr2.setElement(3, 0x0C);
        mr2.setElement(4, 0x7A);
        t.sendTestMessage(mr2);

        // At this point, the LI100 XPressNet programmer
        // deviates from the standard XPressNet programmer.  
        // the LI100 version requests an exit from service 
        // before sending a result to the registered listeners.
        Assert.assertEquals("mode message sent", 3, t.outbound.size());
        Assert.assertEquals("exit service mode message contents", "21 81 A0", t.outbound.elementAt(2).toString());

        // send reply stating service mode has exited       
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x61);
        mr3.setElement(1, 0x01);
        mr3.setElement(2, 0x60);
        t.sendTestMessage(mr3);

        jmri.util.JUnitUtil.releaseThread(this);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Register mode received value", 12, l.getRcvdValue());

    }

    public void testReadCvSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        LI100XNetProgrammer p = new LI100XNetProgrammer(t) {
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // and do the read
        p.readCV(29, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "22 15 1D 2A", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x14);
        mr2.setElement(2, 0x1D);
        mr2.setElement(3, 0x22);
        mr2.setElement(4, 0x48);
        t.sendTestMessage(mr2);

        // At this point, the LI100 XPressNet programmer
        // deviates from the standard XPressNet programmer.  
        // the LI100 version requests an exit from service 
        // before sending a result to the registered listeners.
        Assert.assertEquals("mode message sent", 3, t.outbound.size());
        Assert.assertEquals("exit service mode message contents", "21 81 A0", t.outbound.elementAt(2).toString());

        // send reply stating service mode has exited       
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x61);
        mr3.setElement(1, 0x01);
        mr3.setElement(2, 0x60);
        t.sendTestMessage(mr3);

        jmri.util.JUnitUtil.releaseThread(this);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());

    }

    public void testReadRegisterSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        LI100XNetProgrammer p = new LI100XNetProgrammer(t) {
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // set register mode
        p.setMode(DefaultProgrammerManager.REGISTERMODE);

        // and do the read
        p.readCV(29, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "22 11 05 36", t.outbound.elementAt(0).toString());
        // send reply (enter service mode )
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x10);
        mr2.setElement(2, 0x05);
        mr2.setElement(3, 0x22);
        mr2.setElement(4, 0x54);
        t.sendTestMessage(mr2);

        // At this point, the LI100 XPressNet programmer
        // deviates from the standard XPressNet programmer.  
        // the LI100 version requests an exit from service 
        // before sending a result to the registered listeners.
        //Assert.assertEquals("mode message sent", 3, t.outbound.size());
        Assert.assertEquals("exit service mode message contents", "21 81 A0", t.outbound.elementAt(2).toString());

        // send reply stating service mode has exited       
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x61);
        mr3.setElement(1, 0x01);
        mr3.setElement(2, 0x60);
        t.sendTestMessage(mr3);

        jmri.util.JUnitUtil.releaseThread(this);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Register mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testWriteCvSequence test, but
    // it checks the sequence for CVs greater than 255, which use
    // different XPressNet commands.
    public void testWriteHighCvSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        LI100XNetProgrammer p = new LI100XNetProgrammer(t) {
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // and do the write
        p.writeCV(300, 34, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "23 1D 2C 22 30", t.outbound.elementAt(0).toString());
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);
        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x15);
        mr2.setElement(2, 0x2C);
        mr2.setElement(3, 0x22);
        mr2.setElement(4, 0x78);
        t.sendTestMessage(mr2);

        // At this point, the LI100 XPressNet programmer
        // deviates from the standard XPressNet programmer.  
        // the LI100 version requests an exit from service 
        // before sending a result to the registered listeners.
        Assert.assertEquals("mode message sent", 3, t.outbound.size());
        Assert.assertEquals("exit service mode message contents", "21 81 A0", t.outbound.elementAt(2).toString());

        // send reply stating service mode has exited       
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x61);
        mr3.setElement(1, 0x01);
        mr3.setElement(2, 0x60);
        t.sendTestMessage(mr3);

        jmri.util.JUnitUtil.releaseThread(this);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testReadCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use
    // different XPressNet commands.
    public void testReadHighCvSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        LI100XNetProgrammer p = new LI100XNetProgrammer(t) {
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // and do the read
        p.readCV(300, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "22 19 2C 17", t.outbound.elementAt(0).toString());

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        XNetReply mr2 = new XNetReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x15);
        mr2.setElement(2, 0x2C);
        mr2.setElement(3, 0x22);
        mr2.setElement(4, 0x78);
        t.sendTestMessage(mr2);

        // At this point, the LI100 XPressNet programmer
        // deviates from the standard XPressNet programmer.  
        // the LI100 version requests an exit from service 
        // before sending a result to the registered listeners.
        Assert.assertEquals("mode message sent", 3, t.outbound.size());
        Assert.assertEquals("exit service mode message contents", "21 81 A0", t.outbound.elementAt(2).toString());

        // send reply stating service mode has exited       
        XNetReply mr3 = new XNetReply();
        mr3.setElement(0, 0x61);
        mr3.setElement(1, 0x01);
        mr3.setElement(2, 0x60);
        t.sendTestMessage(mr3);

        jmri.util.JUnitUtil.releaseThread(this);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());

    }

    // from here down is testing infrastructure
    public LI100XNetProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", LI100XNetProgrammerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(LI100XNetProgrammerTest.class);
        return suite;
    }

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
