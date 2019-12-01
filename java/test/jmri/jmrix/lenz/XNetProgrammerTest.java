/**
 * XNetProgrammerTest.java
 *
 * Description:	JUnit tests for the XNetProgrammer class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.lenz;

import jmri.JmriException;
import jmri.util.JUnitUtil;
import jmri.ProgrammingMode;
import org.junit.*;

public class XNetProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    static final int RESTART_TIME = 20;
        
    // infrastructure objects
    protected XNetInterfaceScaffold t = null; 
    protected jmri.ProgListenerScaffold l = null;
    protected XNetProgrammer p = null;

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBYTEMODE,
                programmer.getMode());        
    }

    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBYTEMODE,
                ((XNetProgrammer)programmer).getBestMode());        
    }

    @Override
    @Test
    public void testGetCanWriteAddress() {
        Assert.assertFalse("can write address", programmer.getCanWrite("1234"));
    }    

    @Test
    public void testWriteCvSequence() throws JmriException {
        // and do the write
        p.writeCV("29", 34, l);
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

        // At this point, the standard XpressNet programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        // failure in this test occurs with the next line.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    @Test
    public void testWriteRegisterSequence() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV("29", 12, l);
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

        // At this point, the standard XpressNet programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        // failure in this test occurs with the next line.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Register mode received value", 12, l.getRcvdValue());
    }

    @Test
    public void testReadCvSequence() throws JmriException {
        // and do the read
        p.readCV("29", l);
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

        // At this point, the standard XpressNet programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        // failure in this test occurs with the next line.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Register mode received value", 34, l.getRcvdValue());
    }

    @Test
    public void testReadRegisterSequence() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the read
        p.readCV("29", l);
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

        // At this point, the standard XpressNet programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        // failure in this test occurs with the next line.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Register mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testWriteCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use
    // different XpressNet commands.
    @Test
    public void testWriteHighCvSequence() throws JmriException {
        // and do the write
        p.writeCV("300", 34, l);
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

        // At this point, the standard XpressNet programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        //failure in this test occurs with the next line.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // this test is the same as the testReadCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use
    // different XpressNet commands.
    @Test
    public void testReadHighCvSequence() throws JmriException {
        // and do the read
        p.readCV("300", l);
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

        // At this point, the standard XpressNet programmer
        // should send a result to the programmer listeners, and
        // wait for either the next read/write request or for the
        // traffic controller to exit from service mode.  We just
        // need to wait a few seconds and see that the listener we
        // registered earlier received the values we expected.

        //failure in this test occurs with the next line.
        JUnitUtil.waitFor(()->{return l.getRcvdInvoked() != 0;}, "Receive Called by Programmer");
        Assert.assertEquals("Direct mode received value", 34, l.getRcvdValue());
    }

    // Test to make sure the getCanWrite(int,string) function works correctly
    @Test
    public void testGetCanWriteV35LZ100() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        cs.setCommandStationSoftwareVersion(3.5f);
        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Write CV3 in register mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Write CV3 in paged mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Write CV3 in direct byte mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Write CV3 in direct bit mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Write CV300 in register mode", p.getCanWrite("300"));

        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Write CV300 in paged mode", p.getCanWrite("300"));

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Write CV300 in direct byte mode", p.getCanWrite("300"));

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Write CV300 in direct bit mode", p.getCanWrite("300"));

    }

    // Test to make sure the getCanWrite(int,string) function works correctly
    @Test
    public void testGetCanWriteV36LZ100() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        cs.setCommandStationSoftwareVersion(3.6f);
        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Write CV3 in register mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Write CV3 in paged mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Write CV3 in direct byte mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Write CV3 in direct bit mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertFalse("Version 3.6 LZ100 Can not Write CV300 in register mode", p.getCanWrite("300"));

        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertFalse("Version 3.6 LZ100 Can not Write CV300 in paged mode", p.getCanWrite("300"));

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Write CV300 in direct bit mode", p.getCanWrite("300"));

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Write CV300 in direct byte mode", p.getCanWrite("300"));

    }

    // Test to make sure the getCanWrite(int,string) function works correctly
    @Test
    public void testGetCanWriteV30LH200() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LH200);
        cs.setCommandStationSoftwareVersion(3.0f);
        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Write CV3 in register mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Write CV3 in paged mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Write CV3 in direct byte mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Write CV3 in direct bit mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Write CV300 in register mode", p.getCanWrite("300"));

        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Write CV300 in paged mode", p.getCanWrite("300"));

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Write CV300 in direct bit mode", p.getCanWrite("300"));

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Write CV300 in direct byte mode", p.getCanWrite("300"));

    }

    // Test to make sure the getCanWrite(int,string) function works correctly
    @Test
    public void testGetCanWriteV40MultiMaus() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LH200);
        cs.setCommandStationSoftwareVersion(4.0f);
        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertTrue("Version 4.0 MultiMaus Can Write CV3 in register mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertTrue("Version 4.0 MultiMaus Can Write CV3 in paged mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("Version 4.0 MultiMaus Can Write CV3 in direct byte mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("Version 4.0 MultiMaus Can Write CV3 in direct bit mode", p.getCanWrite("3"));

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Write CV300 in register mode", p.getCanWrite("300"));

        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Write CV300 in paged mode", p.getCanWrite("300"));

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Write CV300 in direct bit mode", p.getCanWrite("300"));

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Write CV300 in direct byte mode", p.getCanWrite("300"));

    }

    // Test to make sure the getCanRead(int,string) function works correctly
    @Test
    public void testGetCanReadV35LZ100() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        cs.setCommandStationSoftwareVersion(3.5f);

        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Read CV3 in register mode", p.getCanRead("3"));

        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Read CV3 in paged mode", p.getCanRead("3"));

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Read CV3 in direct byte mode", p.getCanRead("3"));

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("Version 3.5 LZ100 Can Read CV3 in direct bit mode", p.getCanRead("3"));

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Read CV300 in register mode", p.getCanRead("300"));

        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Read CV300 in paged mode", p.getCanRead("300"));

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Read CV300 in direct byte mode", p.getCanRead("300"));

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertFalse("Version 3.5 LZ100 Can not Read CV300 in direct bit mode", p.getCanRead("300"));

    }

    // Test to make sure the getCanRead(int,string) function works correctly
    @Test
    public void testGetCanReadV36LZ100() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        cs.setCommandStationSoftwareVersion(3.6f);

        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Read CV3 in register mode", p.getCanRead("3"));
        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Read CV3 in paged mode", p.getCanRead("3"));
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Read CV3 in direct byte mode", p.getCanRead("3"));
        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Read CV3 in direct bit mode", p.getCanRead("3"));

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertFalse("Version 3.6 LZ100 Can not Read CV300 in register mode", p.getCanRead("300"));
        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertFalse("Version 3.6 LZ100 Can not Read CV300 in paged mode", p.getCanRead("300"));
        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Read CV300 in direct bit mode", p.getCanRead("300"));
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.6 LZ100 Can Read CV300 in direct byte mode", p.getCanRead("300"));
    }

    // Test to make sure the getCanRead(int,string) function works correctly
    @Test
    public void testGetCanReadV30LH200() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LH200);
        cs.setCommandStationSoftwareVersion(3.0f);

        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Read CV3 in register mode", p.getCanRead("3"));
        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Read CV3 in paged mode", p.getCanRead("3"));
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Read CV3 in direct byte mode", p.getCanRead("3"));
        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertTrue("Version 3.0 LH200 Can Read CV3 in direct bit mode", p.getCanRead("3"));

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Read CV300 in register mode", p.getCanRead("300"));
        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Read CV300 in paged mode", p.getCanRead("300"));
        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Read CV300 in direct bit mode", p.getCanRead("300"));
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertFalse("Version 3.0 LH200 Can not Read CV300 in direct byte mode", p.getCanRead("300"));
    }

    // Test to make sure the getCanRead(int,string) function works correctly
    @Test
    public void testGetCanReadV40MultiMaus() {
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_MULTIMAUS);
        cs.setCommandStationSoftwareVersion(4.0f);

        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can Read CV3 in register mode", p.getCanRead("3"));
        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can Read CV3 in paged mode", p.getCanRead("3"));
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can Read CV3 in direct byte mode", p.getCanRead("3"));
        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can Read CV3 in direct bit mode", p.getCanRead("3"));

        p.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Read CV300 in register mode", p.getCanRead("300"));
        p.setMode(ProgrammingMode.PAGEMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Read CV300 in paged mode", p.getCanRead("300"));
        p.setMode(ProgrammingMode.DIRECTBITMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Read CV300 in direct bit mode", p.getCanRead("300"));
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        Assert.assertFalse("Version 4.0 MultiMaus Can not Read CV300 in direct byte mode", p.getCanRead("300"));

    }

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // infrastructure objects
        t = new XNetInterfaceScaffold(new LenzCommandStation());
        l = new jmri.ProgListenerScaffold();
        p = new XNetProgrammer(t) {
            @Override
            protected synchronized void restartTimer(int delay) {
                super.restartTimer(RESTART_TIME);
            }
        };
	programmer=p;
    }

    @Override
    @After
    public void tearDown() {
        t = null;
        l = null;
        programmer=p=null;
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
