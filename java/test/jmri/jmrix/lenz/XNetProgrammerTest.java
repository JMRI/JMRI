package jmri.jmrix.lenz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import jmri.JmriException;
import jmri.util.JUnitUtil;
import jmri.ProgrammingMode;

import org.junit.jupiter.api.*;

/**
 * XNetProgrammerTest.java
 *
 * JUnit tests for the XNetProgrammer class
 *
 * @author Bob Jacobsen
 */
public class XNetProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    static final int RESTART_TIME = 20;
        
    // infrastructure objects
    protected XNetInterfaceScaffold t = null; 
    protected jmri.ProgListenerScaffold l = null;
    protected XNetProgrammer p = null;

    @Test
    @Override
    public void testDefault() {
        assertEquals( ProgrammingMode.DIRECTBYTEMODE, programmer.getMode(),
                "Check Default");
    }

    @Override
    @Test
    public void testDefaultViaBestMode() {
        assertEquals( ProgrammingMode.DIRECTBYTEMODE, ((XNetProgrammer)programmer).getBestMode(),
                "Check Default");
    }

    @Override
    @Test
    public void testGetCanWriteAddress() {
        assertFalse( programmer.getCanWrite("1234"), "can write address");
    }

    @Test
    public void testWriteCvSequence() throws JmriException {
        // and do the write
        p.writeCV("29", 34, l);
        // check "prog mode" message sent
        assertEquals( 1, t.outbound.size(), "mode message sent");
        assertEquals( "23 16 1D 22 0A", t.outbound.elementAt(0).toString(), "write message contents");
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        assertEquals( 2, t.outbound.size(), "inquire message sent");
        assertEquals( "21 10 31", t.outbound.elementAt(1).toString(), "inquire message contents");

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
        JUnitUtil.waitFor(()-> l.getRcvdInvoked() != 0, "Receive Called by Programmer");
        assertEquals( 34, l.getRcvdValue(), "Direct mode received value");
    }

    @Test
    public void testWriteRegisterSequence() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the write
        p.writeCV("29", 12, l);
        // check "prog mode" message sent
        assertEquals( 1, t.outbound.size(), "read message sent");
        assertEquals( "23 12 05 0C 38", t.outbound.elementAt(0).toString(), "write message contents");

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        assertEquals( 2, t.outbound.size(), "inquire message sent");
        assertEquals( "21 10 31", t.outbound.elementAt(1).toString(), "inquire message contents");

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
        JUnitUtil.waitFor(()-> l.getRcvdInvoked() != 0, "Receive Called by Programmer");
        assertEquals( 12, l.getRcvdValue(), "Register mode received value");
    }

    @Test
    public void testReadCvSequence() throws JmriException {
        // and do the read
        p.readCV("29", l);
        // check "prog mode" message sent
        assertEquals( 1, t.outbound.size(), "mode message sent");
        assertEquals( "22 15 1D 2A", t.outbound.elementAt(0).toString(), "read message contents");

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        assertEquals( 2, t.outbound.size(), "inquire message sent");
        assertEquals( "21 10 31", t.outbound.elementAt(1).toString(), "inquire message contents");

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
        JUnitUtil.waitFor(()-> l.getRcvdInvoked() != 0, "Receive Called by Programmer");
        assertEquals( 34, l.getRcvdValue(), "Register mode received value");
    }

    @Test
    public void testReadRegisterSequence() throws JmriException {
        // set register mode
        p.setMode(ProgrammingMode.REGISTERMODE);

        // and do the read
        p.readCV("29", l);
        // check "prog mode" message sent
        assertEquals( 1, t.outbound.size(), "mode message sent");
        assertEquals( "22 11 05 36", t.outbound.elementAt(0).toString(), "read message contents");
        // send reply (enter service mode )
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        JUnitUtil.waitFor(()-> t.outbound.size() > 1, "2 messages outbound");
        assertEquals( 2, t.outbound.size(), "inquire message sent");
        assertEquals( "21 10 31", t.outbound.elementAt(1).toString(), "inquire message contents");

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
        JUnitUtil.waitFor(()-> l.getRcvdInvoked() != 0, "Receive Called by Programmer");
        assertEquals( 34, l.getRcvdValue(), "Register mode received value");
    }

    // this test is the same as the testWriteCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use
    // different XpressNet commands.
    @Test
    public void testWriteHighCvSequence() throws JmriException {
        // and do the write
        p.writeCV("300", 34, l);
        // check "prog mode" message sent
        assertEquals( 1, t.outbound.size(), "mode message sent");
        assertEquals( "23 1D 2C 22 30", t.outbound.elementAt(0).toString(), "write message contents");
        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        assertEquals( 2, t.outbound.size(), "inquire message sent");
        assertEquals( "21 10 31", t.outbound.elementAt(1).toString(), "inquire message contents");

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
        JUnitUtil.waitFor(()-> l.getRcvdInvoked() != 0, "Receive Called by Programmer");
        assertEquals( 34, l.getRcvdValue(), "Direct mode received value");
    }

    // this test is the same as the testReadCvSequence test, but
    // it checks the sequence for CVs greater than 256, which use
    // different XpressNet commands.
    @Test
    public void testReadHighCvSequence() throws JmriException {
        // and do the read
        p.readCV("300", l);
        // check "prog mode" message sent
        assertEquals( 1, t.outbound.size(), "mode message sent");
        assertEquals( "22 19 2C 17", t.outbound.elementAt(0).toString(), "read message contents");

        // send reply
        XNetReply mr1 = new XNetReply();
        mr1.setElement(0, 0x61);
        mr1.setElement(1, 0x02);
        mr1.setElement(2, 0x63);
        t.sendTestMessage(mr1);

        assertEquals( 2, t.outbound.size(), "inquire message sent");
        assertEquals( "21 10 31", t.outbound.elementAt(1).toString(), "inquire message contents");

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
        JUnitUtil.waitFor(()-> l.getRcvdInvoked() != 0, "Receive Called by Programmer");
        assertEquals( 34, l.getRcvdValue(), "Direct mode received value");
    }

    // Test to make sure the getCanWrite(int,string) function works correctly
    @Test
    public void testGetCanWriteV35LZ100() {
        // clean up from setup before overwriting
        t.terminateThreads();
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        cs.setCommandStationSoftwareVersion(3.5f);
        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertTrue( p.getCanWrite("3"), "Version 3.5 LZ100 Can Write CV3 in register mode");

        p.setMode(ProgrammingMode.PAGEMODE);
        assertTrue( p.getCanWrite("3"), "Version 3.5 LZ100 Can Write CV3 in paged mode");

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertTrue( p.getCanWrite("3"), "Version 3.5 LZ100 Can Write CV3 in direct byte mode");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertTrue( p.getCanWrite("3"), "Version 3.5 LZ100 Can Write CV3 in direct bit mode");

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertFalse( p.getCanWrite("300"), "Version 3.5 LZ100 Can not Write CV300 in register mode");

        p.setMode(ProgrammingMode.PAGEMODE);
        assertFalse( p.getCanWrite("300"), "Version 3.5 LZ100 Can not Write CV300 in paged mode");

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertFalse( p.getCanWrite("300"), "Version 3.5 LZ100 Can not Write CV300 in direct byte mode");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertFalse( p.getCanWrite("300"), "Version 3.5 LZ100 Can not Write CV300 in direct bit mode");

    }

    // Test to make sure the getCanWrite(int,string) function works correctly
    @Test
    public void testGetCanWriteV36LZ100() {
        // clean up from setup before overwriting
        t.terminateThreads();
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        cs.setCommandStationSoftwareVersion(3.6f);
        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertTrue( p.getCanWrite("3"), "Version 3.6 LZ100 Can Write CV3 in register mode");

        p.setMode(ProgrammingMode.PAGEMODE);
        assertTrue( p.getCanWrite("3"), "Version 3.6 LZ100 Can Write CV3 in paged mode");

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertTrue( p.getCanWrite("3"), "Version 3.6 LZ100 Can Write CV3 in direct byte mode");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertTrue( p.getCanWrite("3"), "Version 3.6 LZ100 Can Write CV3 in direct bit mode");

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertFalse( p.getCanWrite("300"), "Version 3.6 LZ100 Can not Write CV300 in register mode");

        p.setMode(ProgrammingMode.PAGEMODE);
        assertFalse( p.getCanWrite("300"), "Version 3.6 LZ100 Can not Write CV300 in paged mode");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertTrue( p.getCanWrite("300"), "Version 3.6 LZ100 Can Write CV300 in direct bit mode");

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertTrue( p.getCanWrite("300"), "Version 3.6 LZ100 Can Write CV300 in direct byte mode");

    }

    // Test to make sure the getCanWrite(int,string) function works correctly
    @Test
    public void testGetCanWriteV30LH200() {
        // clean up from setup before overwriting
        t.terminateThreads();
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LH200);
        cs.setCommandStationSoftwareVersion(3.0f);
        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertTrue( p.getCanWrite("3"), "Version 3.0 LH200 Can Write CV3 in register mode");

        p.setMode(ProgrammingMode.PAGEMODE);
        assertTrue( p.getCanWrite("3"), "Version 3.0 LH200 Can Write CV3 in paged mode");

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertTrue( p.getCanWrite("3"), "Version 3.0 LH200 Can Write CV3 in direct byte mode");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertTrue( p.getCanWrite("3"), "Version 3.0 LH200 Can Write CV3 in direct bit mode");

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertFalse( p.getCanWrite("300"), "Version 3.0 LH200 Can not Write CV300 in register mode");

        p.setMode(ProgrammingMode.PAGEMODE);
        assertFalse( p.getCanWrite("300"), "Version 3.0 LH200 Can not Write CV300 in paged mode");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertFalse( p.getCanWrite("300"), "Version 3.0 LH200 Can not Write CV300 in direct bit mode");

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertFalse( p.getCanWrite("300"), "Version 3.0 LH200 Can not Write CV300 in direct byte mode");

    }

    // Test to make sure the getCanWrite(int,string) function works correctly
    @Test
    public void testGetCanWriteV40MultiMaus() {
        // clean up from setup before overwriting
        t.terminateThreads();
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LH200);
        cs.setCommandStationSoftwareVersion(4.0f);
        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertTrue( p.getCanWrite("3"), "Version 4.0 MultiMaus Can Write CV3 in register mode" );

        p.setMode(ProgrammingMode.PAGEMODE);
        assertTrue( p.getCanWrite("3"), "Version 4.0 MultiMaus Can Write CV3 in paged mode");

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertTrue( p.getCanWrite("3"), "Version 4.0 MultiMaus Can Write CV3 in direct byte mode");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertTrue( p.getCanWrite("3"), "Version 4.0 MultiMaus Can Write CV3 in direct bit mode");

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertFalse( p.getCanWrite("300"), "Version 4.0 MultiMaus Can not Write CV300 in register mode" );

        p.setMode(ProgrammingMode.PAGEMODE);
        assertFalse( p.getCanWrite("300"), "Version 4.0 MultiMaus Can not Write CV300 in paged mode");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertFalse( p.getCanWrite("300"), "Version 4.0 MultiMaus Can not Write CV300 in direct bit mode");

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertFalse( p.getCanWrite("300"), "Version 4.0 MultiMaus Can not Write CV300 in direct byte mode");

    }

    // Test to make sure the getCanRead(int,string) function works correctly
    @Test
    public void testGetCanReadV35LZ100() {
        // clean up from setup before overwriting
        t.terminateThreads();
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        cs.setCommandStationSoftwareVersion(3.5f);

        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertTrue( p.getCanRead("3"), "Version 3.5 LZ100 Can Read CV3 in register mode");

        p.setMode(ProgrammingMode.PAGEMODE);
        assertTrue( p.getCanRead("3"), "Version 3.5 LZ100 Can Read CV3 in paged mode");

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertTrue( p.getCanRead("3"), "Version 3.5 LZ100 Can Read CV3 in direct byte mode");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertTrue( p.getCanRead("3"), "Version 3.5 LZ100 Can Read CV3 in direct bit mode");

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertFalse( p.getCanRead("300"), "Version 3.5 LZ100 Can not Read CV300 in register mode");

        p.setMode(ProgrammingMode.PAGEMODE);
        assertFalse( p.getCanRead("300"), "Version 3.5 LZ100 Can not Read CV300 in paged mode");

        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertFalse( p.getCanRead("300"), "Version 3.5 LZ100 Can not Read CV300 in direct byte mode");

        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertFalse( p.getCanRead("300"), "Version 3.5 LZ100 Can not Read CV300 in direct bit mode");

    }

    // Test to make sure the getCanRead(int,string) function works correctly
    @Test
    public void testGetCanReadV36LZ100() {
        // clean up from setup before overwriting
        t.terminateThreads();
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LZ100);
        cs.setCommandStationSoftwareVersion(3.6f);

        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertTrue( p.getCanRead("3"), "Version 3.6 LZ100 Can Read CV3 in register mode");
        p.setMode(ProgrammingMode.PAGEMODE);
        assertTrue( p.getCanRead("3"), "Version 3.6 LZ100 Can Read CV3 in paged mode");
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertTrue( p.getCanRead("3"), "Version 3.6 LZ100 Can Read CV3 in direct byte mode");
        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertTrue( p.getCanRead("3"), "Version 3.6 LZ100 Can Read CV3 in direct bit mode");

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertFalse( p.getCanRead("300"), "Version 3.6 LZ100 Can not Read CV300 in register mode");
        p.setMode(ProgrammingMode.PAGEMODE);
        assertFalse( p.getCanRead("300"), "Version 3.6 LZ100 Can not Read CV300 in paged mode");
        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertTrue( p.getCanRead("300"), "Version 3.6 LZ100 Can Read CV300 in direct bit mode");
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertTrue( p.getCanRead("300"), "Version 3.6 LZ100 Can Read CV300 in direct byte mode");
    }

    // Test to make sure the getCanRead(int,string) function works correctly
    @Test
    public void testGetCanReadV30LH200() {
        // clean up from setup before overwriting
        t.terminateThreads();
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_LH200);
        cs.setCommandStationSoftwareVersion(3.0f);

        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertTrue( p.getCanRead("3"), "Version 3.0 LH200 Can Read CV3 in register mode");
        p.setMode(ProgrammingMode.PAGEMODE);
        assertTrue( p.getCanRead("3"), "Version 3.0 LH200 Can Read CV3 in paged mode");
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertTrue( p.getCanRead("3"), "Version 3.0 LH200 Can Read CV3 in direct byte mode");
        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertTrue( p.getCanRead("3"), "Version 3.0 LH200 Can Read CV3 in direct bit mode");

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertFalse( p.getCanRead("300"), "Version 3.0 LH200 Can not Read CV300 in register mode");
        p.setMode(ProgrammingMode.PAGEMODE);
        assertFalse( p.getCanRead("300"), "Version 3.0 LH200 Can not Read CV300 in paged mode");
        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertFalse( p.getCanRead("300"), "Version 3.0 LH200 Can not Read CV300 in direct bit mode");
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertFalse( p.getCanRead("300"), "Version 3.0 LH200 Can not Read CV300 in direct byte mode");
    }

    // Test to make sure the getCanRead(int,string) function works correctly
    @Test
    public void testGetCanReadV40MultiMaus() {
        // clean up from setup before overwriting
        t.terminateThreads();
        // infrastructure objects
        LenzCommandStation cs = new LenzCommandStation();
        t = new XNetInterfaceScaffold(cs);

        cs.setCommandStationType(XNetConstants.CS_TYPE_MULTIMAUS);
        cs.setCommandStationSoftwareVersion(4.0f);

        p = new XNetProgrammer(t);

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertFalse( p.getCanRead("3"), "Version 4.0 MultiMaus Can Read CV3 in register mode");
        p.setMode(ProgrammingMode.PAGEMODE);
        assertFalse( p.getCanRead("3"), "Version 4.0 MultiMaus Can Read CV3 in paged mode");
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertFalse( p.getCanRead("3"), "Version 4.0 MultiMaus Can Read CV3 in direct byte mode");
        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertFalse( p.getCanRead("3"), "Version 4.0 MultiMaus Can Read CV3 in direct bit mode");

        p.setMode(ProgrammingMode.REGISTERMODE);
        assertFalse( p.getCanRead("300"), "Version 4.0 MultiMaus Can not Read CV300 in register mode");
        p.setMode(ProgrammingMode.PAGEMODE);
        assertFalse( p.getCanRead("300"), "Version 4.0 MultiMaus Can not Read CV300 in paged mode");
        p.setMode(ProgrammingMode.DIRECTBITMODE);
        assertFalse( p.getCanRead("300"), "Version 4.0 MultiMaus Can not Read CV300 in direct bit mode");
        p.setMode(ProgrammingMode.DIRECTBYTEMODE);
        assertFalse( p.getCanRead("300"), "Version 4.0 MultiMaus Can not Read CV300 in direct byte mode");

    }

    @Override
    @BeforeEach
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
        programmer = p;
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.terminateThreads();
        t = null;
        l = null;
        programmer = null;
        p = null;
        JUnitUtil.tearDown();
    }

}
