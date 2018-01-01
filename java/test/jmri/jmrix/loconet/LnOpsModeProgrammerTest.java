package jmri.jmrix.loconet;

import jmri.ProgListenerScaffold;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import jmri.managers.DefaultProgrammerManager;
import jmri.util.JUnitUtil;
import junit.framework.TestCase;
import org.junit.Assert;

public class LnOpsModeProgrammerTest extends TestCase {

    LocoNetInterfaceScaffold lnis;
    SlotManager sm;
    LocoNetSystemConnectionMemo memo;
    ProgListenerScaffold pl;

    public void testSetMode() {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        try {
            lnopsmodeprogrammer.setMode(ProgrammingMode.PAGEMODE);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("No IllegalArgumentException thrown");

    }

    public void testGetMode() {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        ProgrammingMode intRet = lnopsmodeprogrammer.getMode();
        Assert.assertEquals("OpsByteMode", ProgrammingMode.OPSBYTEMODE, intRet);
    }

    public void testGetCanRead() {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        Assert.assertEquals("ops mode always can read", true,
                lnopsmodeprogrammer.getCanRead());
    }

    public void testSV2DataBytes() {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        LocoNetMessage m = new LocoNetMessage(15);

        // check data bytes
        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x12345678);
        Assert.assertEquals(0x10, m.getElement(10));
        Assert.assertEquals(0x78, m.getElement(11));
        Assert.assertEquals(0x56, m.getElement(12));
        Assert.assertEquals(0x34, m.getElement(13));
        Assert.assertEquals(0x12, m.getElement(14));
    }

    public void testSV2highBits() {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        LocoNetMessage m = new LocoNetMessage(15);

        // check high bits
        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x01020384);
        Assert.assertEquals(0x11, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x01028304);
        Assert.assertEquals(0x12, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x01820304);
        Assert.assertEquals(0x14, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x81020304);
        Assert.assertEquals(0x18, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x81828384);
        Assert.assertEquals(0x1F, m.getElement(10));
        Assert.assertEquals(0x04, m.getElement(11));
        Assert.assertEquals(0x03, m.getElement(12));
        Assert.assertEquals(0x02, m.getElement(13));
        Assert.assertEquals(0x01, m.getElement(14));
    }

     public void testSOps16001Read() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 16001, true);

        lnopsmodeprogrammer.readCV("2",pl);

        // should have written and not returned
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("message", "[EF 0E 7C 2F 00 7D 01 00 00 01 00 7F 7F 00]", lnis.outbound.toString());

     }

      public void testSv1Write() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        int testVal = 120;
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETSV1MODE);
        lnopsmodeprogrammer.writeCV("91",testVal,pl);

        // should have written and not returned
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

         // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xE5, 0x10, 0x53, 0x50, 0x01, 0x00, 0x01, 0x5B, 0x66, 0x7B, 0x00, 0x01, 0x00, 0x00, testVal, 0x36});
        lnopsmodeprogrammer.message(m);

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches written", testVal, pl.getRcvdValue());

     }

     public void testBoardRead0() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBDOPSWMODE);
        lnopsmodeprogrammer.readCV("113.6",pl);

        // should have written and not returned
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0", 0xD0, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x62, lnis.outbound.get(0).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x03, lnis.outbound.get(0).getElement(2) & 0xFF);
        Assert.assertEquals("sent byte 3", 113, lnis.outbound.get(0).getElement(3) & 0xFF);
        Assert.assertEquals("sent byte 4", 0x0A, lnis.outbound.get(0).getElement(4) & 0xFF);

        int testVal = 0;

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x50, 0x40, 0x00});
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

     }

     public void testBoardRead1() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBDOPSWMODE);
        lnopsmodeprogrammer.readCV("113.6",pl);

        // should have written and not returned
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0", 0xD0, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x62, lnis.outbound.get(0).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x03, lnis.outbound.get(0).getElement(2) & 0xFF);
        Assert.assertEquals("sent byte 3", 113, lnis.outbound.get(0).getElement(3) & 0xFF);
        Assert.assertEquals("sent byte 4", 0x0A, lnis.outbound.get(0).getElement(4) & 0xFF);

        int testVal = 1;

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x50, 0x60, 0x00});
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

     }

     public void testBoardReadTimeout() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBDOPSWMODE);
        lnopsmodeprogrammer.readCV("113.6",pl);

        // should have written and not returned
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // No reply message, wait for timeout
        jmri.util.JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"programming reply not received");
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Reply status Not OK", jmri.ProgListener.FailedTimeout, pl.getRcvdStatus());
        Assert.assertTrue("Correct thread", pl.wasRightThread());
     }


     public void testBoardWrite() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 4, true);

        int testVal = 1;

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBDOPSWMODE);
        lnopsmodeprogrammer.writeCV("113.6", testVal, pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0", 0xD0, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x72, lnis.outbound.get(0).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x03, lnis.outbound.get(0).getElement(2) & 0xFF);
        Assert.assertEquals("sent byte 3", 113, lnis.outbound.get(0).getElement(3) & 0xFF);
        Assert.assertEquals("sent byte 4", 0x0B, lnis.outbound.get(0).getElement(4) & 0xFF);

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x50, 0x60, 0x00});
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

     }

     public void testBoardWriteTimeout() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 4, true);

        int testVal = 1;

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBDOPSWMODE);
        lnopsmodeprogrammer.writeCV("113.6", testVal, pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // No reply message, wait for timeout
        jmri.util.JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"programming reply not received");
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Reply status Not OK", jmri.ProgListener.FailedTimeout, pl.getRcvdStatus());
        Assert.assertTrue("Correct thread", pl.wasRightThread());
     }

     public void testSv1ARead() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETSV1MODE);
        lnopsmodeprogrammer.readCV("83",pl);

        // should have written and not returned
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0", 0xE5, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x50, lnis.outbound.get(0).getElement(2) & 0xFF);

        int testVal = 132;

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xE5, 0x10, 0x53, 0x50, 0x01, 0x00, 0x02, 0x5B, 0x66, 0x7B, 0x02, 0x01, 0x04, 0x00, 0x00, 0x48});
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

     }

     public void testSv1BRead() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETSV1MODE);
        lnopsmodeprogrammer.readCV("83",pl);

        // should have written and not returned
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0", 0xE5, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x50, lnis.outbound.get(0).getElement(2) & 0xFF);

        int testVal = 47; // 0x2F

        // Known-good message in reply
        LocoNetMessage m
            = new LocoNetMessage(new int[]{0xE5, 0x10, 0x53, 0x50, 0x01, 0x00, 0x02, 0x03, 0x66, 0x7B, 0x00, 0x01, 0x2F, 0x78, 0x10, 0x52});
        lnopsmodeprogrammer.message(m);

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

     }

     public void testSv2Write() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETSV2MODE);
        lnopsmodeprogrammer.writeCV("22",33,pl);

        // should have written and not returned
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // turn the message around as a reply
        m.setElement(3, m.getElement(3) | 0x40);
        lnopsmodeprogrammer.message(m);

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

     }

     public void testSv2Read() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 1, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETSV2MODE);
        lnopsmodeprogrammer.readCV("22",pl);

        // should have written and not returned
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        int testVal = 130;

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // turn the message around as a reply
        m.setElement(3, m.getElement(3) | 0x40);
        m.setElement(10, (m.getElement(10)&0x7E) | ((testVal & 0x80) != 0 ? 1 : 0));
        m.setElement(11, testVal & 0x7F);
        lnopsmodeprogrammer.message(m);

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 130, pl.getRcvdValue());

     }


    // from here down is testing infrastructure
    public LnOpsModeProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {LnOpsModeProgrammerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

        lnis = new LocoNetInterfaceScaffold();
        sm = new SlotManager(lnis);
        memo = new LocoNetSystemConnectionMemo(lnis, sm);
        pl = new ProgListenerScaffold();

    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }
}
