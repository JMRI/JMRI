package jmri.jmrix.can.cbus;

import jmri.ProgListener;
import jmri.ProgListenerScaffold;
import jmri.Programmer;
import jmri.ProgrammingMode;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Andrew Crosland Copyright (C) 2021
 */
public class CbusDccProgrammerTest extends jmri.jmrix.AbstractProgrammerTest {

    @Test
    @Override
    public void testDefault() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBITMODE,
                programmer.getMode());        
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBITMODE,
                programmer2.getMode());        
    }
    
    @Override
    @Test
    public void testDefaultViaBestMode() {
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBITMODE,
                ((CbusDccProgrammer)programmer).getBestMode());        
        Assert.assertEquals("Check Default", ProgrammingMode.DIRECTBITMODE,
                ((CbusDccProgrammer)programmer2).getBestMode());        
    }

    @Test
    public void testDispose() {

        Assertions.assertEquals(2, tcis.numListeners(),"2 tcis listeners");

        programmer.dispose();
        programmer2.dispose();

        Assertions.assertEquals(0, tcis.numListeners(),"0 tcis listeners");
    }

    private static CanReply sstatReply(int status) {
        return new CanReply( new int[]{CbusConstants.CBUS_SSTAT, CbusConstants.SERVICE_HANDLE, status }, 0x12 );
    }

    // SSTAT BUSY must end the write promptly with a ProgrammerBusy status
    @Test
    public void testSstatBusyEndsWrite() throws jmri.ProgrammerException {
        CbusDccProgrammer p = (CbusDccProgrammer) programmer;
        ProgListenerScaffold l = new ProgListenerScaffold();

        p.writeCV("4", 5, l);
        Assertions.assertEquals(1, tcis.outbound.size(), "write command sent");
        Assertions.assertEquals(CbusConstants.CBUS_WCVS, tcis.outbound.lastElement().getElement(0), "WCVS sent");
        Assertions.assertEquals(0, l.getRcvdInvoked(), "listener not yet notified");
        Assertions.assertEquals(CbusDccProgrammer.COMMANDSENT, p.progState, "awaiting reply");

        p.reply(sstatReply(CbusConstants.SSTAT_BUSY));
        JUnitAppender.assertWarnMessageStartingWith("Programmer busy");

        Assertions.assertEquals(1, l.getRcvdInvoked(), "listener notified once");
        Assertions.assertEquals(ProgListener.ProgrammerBusy, l.getRcvdStatus(), "busy status reported");
        Assertions.assertEquals(-1, l.getRcvdValue(), "no value");
        Assertions.assertEquals(CbusDccProgrammer.NOTPROGRAMMING, p.progState, "programming operation ended");

        // late replies for the ended operation are ignored
        p.reply(sstatReply(CbusConstants.SSTAT_WR_ACK));
        Assertions.assertEquals(1, l.getRcvdInvoked(), "no further notification");

        // and the programmer is free for the next operation, by a different listener
        ProgListenerScaffold l2 = new ProgListenerScaffold();
        p.writeCV("4", 6, l2);
        Assertions.assertEquals(2, tcis.outbound.size(), "2nd write command sent");
        p.reply(sstatReply(CbusConstants.SSTAT_WR_ACK));
        Assertions.assertEquals(1, l2.getRcvdInvoked(), "2nd listener notified");
        Assertions.assertEquals(ProgListener.OK, l2.getRcvdStatus(), "2nd write OK");
        Assertions.assertEquals(6, l2.getRcvdValue(), "2nd write value");
        Assertions.assertEquals(1, l.getRcvdInvoked(), "1st listener not notified again");
        Assertions.assertEquals(CbusDccProgrammer.NOTPROGRAMMING, p.progState, "programming operation ended");
    }

    // SSTAT CV_ERROR must end the read promptly with an error status
    @Test
    public void testSstatCvErrorEndsRead() throws jmri.ProgrammerException {
        Assertions.assertNotNull(memo);
        CbusDccProgrammer p = new CbusDccProgrammer(memo); // memo required for readCV
        ProgListenerScaffold l = new ProgListenerScaffold();

        p.readCV("29", l);
        Assertions.assertEquals(1, tcis.outbound.size(), "read command sent");
        Assertions.assertEquals(CbusConstants.CBUS_QCVS, tcis.outbound.lastElement().getElement(0), "QCVS sent");
        Assertions.assertEquals(0, l.getRcvdInvoked(), "listener not yet notified");

        p.reply(sstatReply(CbusConstants.SSTAT_CV_ERROR));
        JUnitAppender.assertWarnMessageStartingWith("CV error reported by command station");

        Assertions.assertEquals(1, l.getRcvdInvoked(), "listener notified once");
        Assertions.assertEquals(ProgListener.UnknownError, l.getRcvdStatus(), "error status reported");
        Assertions.assertEquals(CbusDccProgrammer.NOTPROGRAMMING, p.progState, "programming operation ended");

        // a late CV report for the ended operation is ignored
        p.reply(new CanReply( new int[]{CbusConstants.CBUS_PCVS, CbusConstants.SERVICE_HANDLE, 0, 29, 0x12 }, 0x12 ));
        Assertions.assertEquals(1, l.getRcvdInvoked(), "no further notification");

        // and a subsequent read works
        ProgListenerScaffold l2 = new ProgListenerScaffold();
        p.readCV("29", l2);
        Assertions.assertEquals(2, tcis.outbound.size(), "2nd read command sent");
        p.reply(new CanReply( new int[]{CbusConstants.CBUS_PCVS, CbusConstants.SERVICE_HANDLE, 0, 29, 0x12 }, 0x12 ));
        Assertions.assertEquals(1, l2.getRcvdInvoked(), "2nd listener notified");
        Assertions.assertEquals(ProgListener.OK, l2.getRcvdStatus(), "2nd read OK");
        Assertions.assertEquals(0x12, l2.getRcvdValue(), "2nd read value");

        p.dispose();
    }

    // existing NO_ACK and OVLD error replies must also leave the programming state
    @Test
    public void testSstatNoAckAndOverloadEndOperation() throws jmri.ProgrammerException {
        CbusDccProgrammer p = (CbusDccProgrammer) programmer;
        ProgListenerScaffold l = new ProgListenerScaffold();

        p.writeCV("4", 5, l);
        p.reply(sstatReply(CbusConstants.SSTAT_NO_ACK));
        JUnitAppender.assertWarnMessageStartingWith("handle error reply");
        Assertions.assertEquals(1, l.getRcvdInvoked(), "listener notified once");
        Assertions.assertEquals(ProgListener.NoLocoDetected, l.getRcvdStatus(), "no loco status reported");
        Assertions.assertEquals(CbusDccProgrammer.NOTPROGRAMMING, p.progState, "programming operation ended");

        l = new ProgListenerScaffold();
        p.writeCV("4", 5, l);
        p.reply(sstatReply(CbusConstants.SSTAT_OVLD));
        JUnitAppender.assertWarnMessageStartingWith("Programming overload");
        Assertions.assertEquals(1, l.getRcvdInvoked(), "listener notified once");
        Assertions.assertEquals(ProgListener.ProgrammingShort, l.getRcvdStatus(), "short status reported");
        Assertions.assertEquals(CbusDccProgrammer.NOTPROGRAMMING, p.progState, "programming operation ended");
    }
    
/*
    @Test
    public void testSetGetMode() {
        programmer.setMode(ProgrammingMode.REGISTERMODE);
        Assert.assertEquals("Check mode matches set", ProgrammingMode.REGISTERMODE,
                programmer.getMode());        
    }

    @Test
    public void testWriteSequence() throws jmri.ProgrammerException {
        p.writeCV("4", 5, testListener);

        Assert.assertEquals("listeners", 0, tc.numListeners());
        Assert.assertEquals("sent count", 1, tc.outbound.size());
        Assert.assertEquals("content 1", "[78] 96 00 03 04 05",
                tc.outbound.get(0).toString());

        // no reply from CAN and listener replies immediately,
        // contrast read test below
        Assert.assertTrue("listener invoked", testListener.getRcvdInvoked()>0);
        Assert.assertEquals("status", 0, testListener.getRcvdStatus());
    }

    @Test
    public void testReadSequence() throws jmri.ProgrammerException {
        p.readCV("4", testListener);

        Assert.assertEquals("listeners", 0, tc.numListeners());
        Assert.assertEquals("sent count", 1, tc.outbound.size());
        Assert.assertEquals("content 1", "[78] 71 00 03 04",
                tc.outbound.get(0).toString());
        Assert.assertTrue("listener not invoked", testListener.getRcvdInvoked()==0);

        // pretend reply from CAN
        int[] frame = new int[]{0x97, 0, 3, 5};
        CanReply f = new CanReply(frame);
        p.reply(f);

        Assert.assertTrue("listener invoked", testListener.getRcvdInvoked()>0);
        Assert.assertEquals("status", 0, testListener.getRcvdStatus());
        Assert.assertEquals("value", 5, testListener.getRcvdValue());
    }

*/
    
    protected Programmer programmer2;
    private TrafficControllerScaffold tcis = null;
    private CanSystemConnectionMemo memo = null;

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        tcis = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcis);
        programmer = new CbusDccProgrammer(tcis);
        programmer2 = new CbusDccProgrammer(tcis);
    }

    @Override
    @AfterEach
    public void tearDown() {
        programmer.dispose();
        programmer2.dispose();
        programmer2 = null;
        programmer = null;
        Assertions.assertNotNull(tcis);
        tcis.terminateThreads();
        tcis = null;
        Assertions.assertNotNull(memo);
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();

    }

    // private static final Logger log = LoggerFactory.getLogger(CbusDccProgrammerTest.class);

}
