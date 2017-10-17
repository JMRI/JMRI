/**
 * DCCppProgrammerTest.java
 *
 * Description:	JUnit tests for the DCCppProgrammer class
 *
 * @author	Bob Jacobsen
 * @author	Mark Underwood (C) 2015
 */
package jmri.jmrix.dccpp;

import jmri.JmriException;
import jmri.ProgrammingMode;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

public class DCCppProgrammerTest extends TestCase {

    static final int RESTART_TIME = 20;

    public void testWriteCvSequence() throws JmriException {
        // infrastructure objects
        DCCppInterfaceScaffold t = new DCCppInterfaceScaffold(new DCCppCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        DCCppProgrammer p = new DCCppProgrammer(t) {
            @Override
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // and do the write
        p.writeCV(29, 34, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "W 29 34 0 87", t.outbound.elementAt(0).toString());
        // send reply
        DCCppReply mr1 = DCCppReply.parseDCCppReply("r 0|87|29 34");
        t.sendTestMessage(mr1);

        // At this point, the standard DCC++ programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        jmri.util.JUnitUtil.releaseThread(this);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    public void testWriteRegisterSequence() throws JmriException {
        /*
        // infrastructure objects
        DCCppInterfaceScaffold t = new DCCppInterfaceScaffold(new DCCppCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        DCCppProgrammer p = new DCCppProgrammer(t) {
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV(29, 12, l);
        // check "prog mode" message sent
        Assert.assertEquals("read message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "23 12 05 0C 38", t.outbound.elementAt(0).toString());

        // send reply
        DCCppReply mr1 = new DCCppReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        DCCppReply mr2 = new DCCppReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x10);
        mr2.setElement(2, 0x05);
        mr2.setElement(3, 0x0C);
        mr2.setElement(4, 0x7A);
        t.sendTestMessage(mr2);

        // At this point, the standard XpressNet programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        jmri.util.JUnitUtil.releaseThread(this);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);

        Assert.assertEquals("Register mode received value", 12, l.getRcvdValue());
         */
    }

    public void testReadCvSequence() throws JmriException {
        // infrastructure objects
        DCCppInterfaceScaffold t = new DCCppInterfaceScaffold(new DCCppCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        DCCppProgrammer p = new DCCppProgrammer(t) {
            @Override
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // and do the read
        p.readCV(29, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "R 29 0 82", t.outbound.elementAt(0).toString());

        // send reply
        DCCppReply mr1 = DCCppReply.parseDCCppReply("r 0|82|29 12");
        t.sendTestMessage(mr1);

        // At this point, the standard DCC++ programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        jmri.util.JUnitUtil.releaseThread(this);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);

        Assert.assertEquals("Register mode received value", 12, l.getRcvdValue());
    }

    public void testReadRegisterSequence() throws JmriException {
        /*
        // infrastructure objects
        DCCppInterfaceScaffold t = new DCCppInterfaceScaffold(new DCCppCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        DCCppProgrammer p = new DCCppProgrammer(t) {
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the read
        p.readCV(29, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "22 11 05 36", t.outbound.elementAt(0).toString());
        // send reply (enter service mode )
        DCCppReply mr1 = new DCCppReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        Assert.assertEquals("inquire message sent", 2, t.outbound.size());
        Assert.assertEquals("inquire message contents", "21 10 31", t.outbound.elementAt(1).toString());

        // send a result string
        DCCppReply mr2 = new DCCppReply();
        mr2.setElement(0, 0x63);
        mr2.setElement(1, 0x10);
        mr2.setElement(2, 0x05);
        mr2.setElement(3, 0x22);
        mr2.setElement(4, 0x54);
        t.sendTestMessage(mr2);

        // At this point, the standard XpressNet programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        jmri.util.JUnitUtil.releaseThread(this);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Register mode received value", 34, l.getRcvdValue());
         */
    }

    // this test is the same as the testWriteCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use
    // different XpressNet commands.
    public void testWriteHighCvSequence() throws JmriException {
        // infrastructure objects
        DCCppInterfaceScaffold t = new DCCppInterfaceScaffold(new DCCppCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        DCCppProgrammer p = new DCCppProgrammer(t) {
            @Override
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // and do the write
        p.writeCV(300, 34, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("write message contents", "W 300 34 0 87", t.outbound.elementAt(0).toString());
        // send reply
        DCCppReply mr1 = DCCppReply.parseDCCppReply("r 0|87|300 34");
        t.sendTestMessage(mr1);

        // At this point, the standard DCC++ programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        jmri.util.JUnitUtil.releaseThread(this);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testReadCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use
    // different XpressNet commands.
    public void testReadCvHighSequence() throws JmriException {
        // infrastructure objects
        DCCppInterfaceScaffold t = new DCCppInterfaceScaffold(new DCCppCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        DCCppProgrammer p = new DCCppProgrammer(t) {
            @Override
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };

        // and do the read
        p.readCV(300, l);
        // check "prog mode" message sent
        Assert.assertEquals("mode message sent", 1, t.outbound.size());
        Assert.assertEquals("read message contents", "R 300 0 82", t.outbound.elementAt(0).toString());

        // send reply
        DCCppReply mr1 = DCCppReply.parseDCCppReply("r 0|82|300 34");
        t.sendTestMessage(mr1);

        // At this point, the standard DCC++ programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.
        jmri.util.JUnitUtil.releaseThread(this);

        //failure in this test occurs with the next line.
        Assert.assertFalse("Receive Called by Programmer", l.getRcvdInvoked() == 0);

        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // Test to make sure the getCanWrite(int,string) function works correctly
    // TODO: Fix test to verify exception thrown for Register and paged modes.
    public void testGetCanWrite() {
        // infrastructure objects
        DCCppCommandStation cs = new DCCppCommandStation();
        DCCppInterfaceScaffold t = new DCCppInterfaceScaffold(cs);

        //cs.setCommandStationType(DCCppConstants.CS_TYPE_LZ100);
        //cs.setCommandStationSoftwareVersion(3.5f);
        DCCppProgrammer p = new DCCppProgrammer(t);

        //p.setMode(ProgrammingMode.REGISTERMODE);
        //Assert.assertTrue("DCC++ Base Station can write CV3 in Register Mode", p.getCanWrite("3"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 0 CS Version: 3.5");
        //p.setMode(ProgrammingMode.PAGEMODE);
        //Assert.assertTrue("DCC++ Base Station Can Write CV3 in paged mode", p.getCanWrite("3"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 0 CS Version: 3.5");
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("DCC++ Base Station Can Write CV3 in direct byte mode", p.getCanWrite("3"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 0 CS Version: 3.5");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("DCC++ Base Station Can Write CV3 in direct bit mode", p.getCanWrite("3"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 0 CS Version: 3.5");

        //p.setMode(ProgrammingMode.REGISTERMODE);
        //Assert.assertFalse("DCC++ Base Station Can not Write CV300 in register mode", p.getCanWrite("300"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 0 CS Version: 3.5");
        //p.setMode(ProgrammingMode.PAGEMODE);
        //Assert.assertFalse("DCC++ Base Station Can not Write CV300 in paged mode", p.getCanWrite("300"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 0 CS Version: 3.5");
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("DCC++ Base Station Can Write CV300 in direct byte mode", p.getCanWrite("300"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 0 CS Version: 3.5");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("DCC++ Base Station Can Write CV300 in direct bit mode", p.getCanWrite("300"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 0 CS Version: 3.5");

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertFalse("DCC++ Base Station Can Not Write CV3000 in direct byte mode", p.getCanWrite("3000"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 0 CS Version: 3.5");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertFalse("DCC++ Base Station Can Not  Write CV3000 in direct bit mode", p.getCanWrite("3000"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 0 CS Version: 3.5");

    }

    // Test to make sure the getCanRead(int,string) function works correctly
    // TODO: Fix test to verify exception thrown for Register and paged modes.
    public void testGetCanRead() {
        // infrastructure objects
        DCCppCommandStation cs = new DCCppCommandStation();
        DCCppInterfaceScaffold t = new DCCppInterfaceScaffold(cs);

        //cs.setCommandStationType(DCCppConstants.CS_TYPE_LZ100);
        //cs.setCommandStationSoftwareVersion(3.5f);
        DCCppProgrammer p = new DCCppProgrammer(t);

        //p.setMode(ProgrammingMode.REGISTERMODE);
        //Assert.assertTrue("DCC++ Base Station Can Read CV3 in register mode", p.getCanRead("3"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 1 CS Version: 4.0");
        //p.setMode(ProgrammingMode.PAGEMODE);
        //Assert.assertTrue("DCC++ Base Station Can Read CV3 in paged mode", p.getCanRead("3"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 1 CS Version: 4.0");
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("DCC++ Base Station Can Read CV3 in direct byte mode", p.getCanRead("3"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 1 CS Version: 4.0");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("DCC++ Base Station Can Read CV3 in direct bit mode", p.getCanRead("3"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 1 CS Version: 4.0");

        //p.setMode(ProgrammingMode.REGISTERMODE);
        //Assert.assertFalse("DCC++ Base Station Can not Read CV300 in register mode", p.getCanRead("300"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 1 CS Version: 4.0");
        //p.setMode(ProgrammingMode.PAGEMODE);
        //Assert.assertFalse("DCC++ Base Station Can not Read CV300 in paged mode", p.getCanRead("300"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 1 CS Version: 4.0");
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("DCC++ Base Station Can Read CV300 in direct byte mode", p.getCanRead("300"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 1 CS Version: 4.0");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("DCC++ Base Station Can Read CV300 in direct bit mode", p.getCanRead("300"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 1 CS Version: 4.0");

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertFalse("DCC++ Base Station Can not Read CV3000 in direct byte mode", p.getCanRead("3000"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 1 CS Version: 4.0");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertFalse("DCC++ Base Station Can not Read CV3000 in direct bit mode", p.getCanRead("3000"));
        //jmri.util.JUnitAppender.assertErrorMessage("cs Type: 1 CS Version: 4.0");

    }

    // from here down is testing infrastructure
    public DCCppProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppProgrammerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppProgrammerTest.class);
        return suite;
    }

    // The minimal setup is for log4J
    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

}
