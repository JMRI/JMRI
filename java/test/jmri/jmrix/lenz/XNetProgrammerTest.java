/**
 * XNetProgrammerTest.java
 *
 * Description:	JUnit tests for the XNetProgrammer class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.lenz;

import jmri.JmriException;
import jmri.managers.DefaultProgrammerManager;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class XNetProgrammerTest extends TestCase {

    static final int RESTART_TIME = 20;

    public void testWriteCvSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        XNetProgrammer p = new XNetProgrammer(t) {
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

        // At this point, the standard XPressNet programmer 
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        // failure in this test occurs with the next line.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    public void testWriteRegisterSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        XNetProgrammer p = new XNetProgrammer(t) {
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

        // At this point, the standard XPressNet programmer 
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        // failure in this test occurs with the next line.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Register mode received value", 12, l.getRcvdValue());
    }

    public void testReadCvSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        XNetProgrammer p = new XNetProgrammer(t) {
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

        // At this point, the standard XPressNet programmer 
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        // failure in this test occurs with the next line.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Register mode received value", 34, l.getRcvdValue());
    }

    public void testReadRegisterSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        XNetProgrammer p = new XNetProgrammer(t) {
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

        // At this point, the standard XPressNet programmer 
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        // failure in this test occurs with the next line.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Register mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testWriteCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use 
    // different XPressNet commands.
    public void testWriteHighCvSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        XNetProgrammer p = new XNetProgrammer(t) {
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

        // At this point, the standard XPressNet programmer 
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        //failure in this test occurs with the next line.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testReadCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use 
    // different XPressNet commands.
    public void testReadCvHighSequence() throws JmriException {
        // infrastructure objects
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(new LenzCommandStation());
        jmri.ProgListenerScaffold l = new jmri.ProgListenerScaffold();

        XNetProgrammer p = new XNetProgrammer(t) {
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

        // At this point, the standard XPressNet programmer 
        // should send a result to the programmer listeners, and 
        // wait for either the next read/write request or for the 
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        //failure in this test occurs with the next line.
        jmri.util.JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // Test to make sure the getCanWrite(int,string) function works correctly 
    public void testGetCanWriteV35LZ100() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        cs.setCommandStationSoftwareVersion(3.5f);
        XNetProgrammer p = new XNetProgrammer(t);

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Write CV3 in register mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Write CV3 in paged mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Write CV3 in direct byte mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Write CV3 in direct bit mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Write CV300 in register mode", p.getCanWrite("300"));

        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Write CV300 in paged mode", p.getCanWrite("300"));

        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Write CV300 in direct byte mode", p.getCanWrite("300"));

        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Write CV300 in direct bit mode", p.getCanWrite("300"));

    }

    // Test to make sure the getCanWrite(int,string) function works correctly 
    public void testGetCanWriteV36LZ100() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        cs.setCommandStationSoftwareVersion(3.6f);
        XNetProgrammer p = new XNetProgrammer(t);

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Write CV3 in register mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Write CV3 in paged mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Write CV3 in direct byte mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Write CV3 in direct bit mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertFalse("Version 3.6 LZ100 Can not Write CV300 in register mode", p.getCanWrite("300"));

        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertFalse("Version 3.6 LZ100 Can not Write CV300 in paged mode", p.getCanWrite("300"));

        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Write CV300 in direct bit mode", p.getCanWrite("300"));

        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Write CV300 in direct byte mode", p.getCanWrite("300"));

    }

    // Test to make sure the getCanWrite(int,string) function works correctly 
    public void testGetCanWriteV30LH200() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LH200);
        cs.setCommandStationSoftwareVersion(3.0f);
        XNetProgrammer p = new XNetProgrammer(t);

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Write CV3 in register mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Write CV3 in paged mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Write CV3 in direct byte mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Write CV3 in direct bit mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Write CV300 in register mode", p.getCanWrite("300"));

        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Write CV300 in paged mode", p.getCanWrite("300"));

        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Write CV300 in direct bit mode", p.getCanWrite("300"));

        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Write CV300 in direct byte mode", p.getCanWrite("300"));

    }

    // Test to make sure the getCanWrite(int,string) function works correctly 
    public void testGetCanWriteV40MultiMaus() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LH200);
        cs.setCommandStationSoftwareVersion(4.0f);
        XNetProgrammer p = new XNetProgrammer(t);

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertTrue("Version 4.0 MultiMaus Can Write CV3 in register mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertTrue("Version 4.0 MultiMaus Can Write CV3 in paged mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertTrue("Version 4.0 MultiMaus Can Write CV3 in direct byte mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertTrue("Version 4.0 MultiMaus Can Write CV3 in direct bit mode", p.getCanWrite("3"));

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Write CV300 in register mode", p.getCanWrite("300"));

        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Write CV300 in paged mode", p.getCanWrite("300"));

        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Write CV300 in direct bit mode", p.getCanWrite("300"));

        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Write CV300 in direct byte mode", p.getCanWrite("300"));

    }

    // Test to make sure the getCanRead(int,string) function works correctly 
    public void testGetCanReadV35LZ100() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        cs.setCommandStationSoftwareVersion(3.5f);

        XNetProgrammer p = new XNetProgrammer(t);

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Read CV3 in register mode", p.getCanRead("3"));

        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Read CV3 in paged mode", p.getCanRead("3"));

        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Read CV3 in direct byte mode", p.getCanRead("3"));

        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Read CV3 in direct bit mode", p.getCanRead("3"));

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Read CV300 in register mode", p.getCanRead("300"));

        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Read CV300 in paged mode", p.getCanRead("300"));

        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Read CV300 in direct byte mode", p.getCanRead("300"));

        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Read CV300 in direct bit mode", p.getCanRead("300"));

    }

    // Test to make sure the getCanRead(int,string) function works correctly 
    public void testGetCanReadV36LZ100() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        cs.setCommandStationSoftwareVersion(3.6f);

        XNetProgrammer p = new XNetProgrammer(t);

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Read CV3 in register mode", p.getCanRead("3"));
        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Read CV3 in paged mode", p.getCanRead("3"));
        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Read CV3 in direct byte mode", p.getCanRead("3"));
        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Read CV3 in direct bit mode", p.getCanRead("3"));

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertFalse("Version 3.6 LZ100 Can not Read CV300 in register mode", p.getCanRead("300"));
        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertFalse("Version 3.6 LZ100 Can not Read CV300 in paged mode", p.getCanRead("300"));
        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Read CV300 in direct bit mode", p.getCanRead("300"));
        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Read CV300 in direct byte mode", p.getCanRead("300"));
    }

    // Test to make sure the getCanRead(int,string) function works correctly 
    public void testGetCanReadV30LH200() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LH200);
        cs.setCommandStationSoftwareVersion(3.0f);

        XNetProgrammer p = new XNetProgrammer(t);

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Read CV3 in register mode", p.getCanRead("3"));
        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Read CV3 in paged mode", p.getCanRead("3"));
        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Read CV3 in direct byte mode", p.getCanRead("3"));
        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Read CV3 in direct bit mode", p.getCanRead("3"));

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Read CV300 in register mode", p.getCanRead("300"));
        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Read CV300 in paged mode", p.getCanRead("300"));
        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Read CV300 in direct bit mode", p.getCanRead("300"));
        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Read CV300 in direct byte mode", p.getCanRead("300"));
    }

    // Test to make sure the getCanRead(int,string) function works correctly 
    public void testGetCanReadV40MultiMaus() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        XNetInterfaceScaffold t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_MULTIMAUS);
        cs.setCommandStationSoftwareVersion(4.0f);

        XNetProgrammer p = new XNetProgrammer(t);

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can Read CV3 in register mode", p.getCanRead("3"));
        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can Read CV3 in paged mode", p.getCanRead("3"));
        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can Read CV3 in direct byte mode", p.getCanRead("3"));
        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can Read CV3 in direct bit mode", p.getCanRead("3"));

        p.setMode(DefaultProgrammerManager.REGISTERMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Read CV300 in register mode", p.getCanRead("300"));
        p.setMode(DefaultProgrammerManager.PAGEMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Read CV300 in paged mode", p.getCanRead("300"));
        p.setMode(DefaultProgrammerManager.DIRECTBITMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Read CV300 in direct bit mode", p.getCanRead("300"));
        p.setMode(DefaultProgrammerManager.DIRECTBYTEMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Read CV300 in direct byte mode", p.getCanRead("300"));

    }

    // from here down is testing infrastructure
    public XNetProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XNetProgrammerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XNetProgrammerTest.class);
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
