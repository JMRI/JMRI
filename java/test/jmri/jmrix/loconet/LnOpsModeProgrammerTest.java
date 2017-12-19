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

     public void testCommandStationRead1() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 4, true);

        // set command station opsw mode
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETCSOPSWMODE);

        // attempt a command station opsw access
        lnopsmodeprogrammer.readCV("1.01", pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // should have written and not returned

        Assert.assertEquals("sent byte 0", 0xBB, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x7f, lnis.outbound.get(0).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x00, lnis.outbound.get(0).getElement(2) & 0xFF);

        int testVal = 0;

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // Known-good message in reply
        // command station reply
        m = new LocoNetMessage(new int[] {LnConstants.OPC_SL_RD_DATA,
            0x0E, 0x7F, 0x10, 0x00, 0x30, 0x02,
            0x07, 0x01, 0x08, 0x00, 0x1B, 0x69, 0x77});
        lnopsmodeprogrammer.message(m);

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.02", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.03", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 3, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.04", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 4, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", testVal, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.05", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 5, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another (out-of-order) command station opsw access
        lnopsmodeprogrammer.readCV("1.07", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 6, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another (out-of-order) command station opsw access
        lnopsmodeprogrammer.readCV("1.06", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 7, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.08", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming error reply ", 8, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());


        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.17", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 9, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.18", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 10, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.19", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 11, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.20", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 12, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.21", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 13, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.22", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 14, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.23", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 15, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.24", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 16, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.25", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 17, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.26", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 18, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.27", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 19, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.28", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 20, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.29", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 21, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.30", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 22, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.31", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 23, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.32", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 24, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());



     }


     public void testCommandStationReadTimeout() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETCSOPSWMODE);
        lnopsmodeprogrammer.readCV("1.14", pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // No reply message, wait for timeout
        jmri.util.JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"programming reply not received");
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Reply status Not OK", jmri.ProgListener.FailedTimeout, pl.getRcvdStatus());
        Assert.assertTrue("Correct thread", pl.wasRightThread());
     }


     public void testCommandStationRead2() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 4, true);

        // set command station opsw mode
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETCSOPSWMODE);

        // attempt a command station opsw access
        lnopsmodeprogrammer.readCV("1.01", pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // should have written and not returned

        Assert.assertEquals("sent byte 0", 0xBB, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x7f, lnis.outbound.get(0).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x00, lnis.outbound.get(0).getElement(2) & 0xFF);

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // Known-good message in reply
        // command station reply
        m = new LocoNetMessage(new int[] {LnConstants.OPC_SL_RD_DATA,
            0x0E, 0x7F, 0x7d, 0x6e, 0x5a, 0x4d,
            0x07, 0x3b, 0x2e, 0x1e, 0x0f, 0x69, 0x77});
        lnopsmodeprogrammer.message(m);

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.02", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.03", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 3, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.04", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 4, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.05", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 5, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.06", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 6, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.07", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 7, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());


        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.08", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 8, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.09", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 9, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.10", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 10, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.11", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 11, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.12", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 12, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.13", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 13, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.14", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 14, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.15", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 15, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.16", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 16, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.17", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 17, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.18", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 18, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.19", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 19, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.20", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 20, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.21", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 21, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.22", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 22, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.23", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 23, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.24", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 24, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.25", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 25, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.26", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 26, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.27", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 27, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.28", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 28, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.29", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 29, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.30", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 30, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.31", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 31, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.32", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 32, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.33", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 33, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.34", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 34, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.35", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 35, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.36", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 36, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.37", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 37, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.38", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 38, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.39", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 39, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.40", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 40, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.41", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 41, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.42", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 42, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.43", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 43, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.44", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 44, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.45", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 45, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.46", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 46, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.47", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 47, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.48", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 48, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.49", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 49, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.50", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 50, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.51", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 51, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.52", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 52, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.53", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 53, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.54", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 54, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.55", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 55, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.56", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 56, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.57", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 57, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.58", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 58, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.59", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 59, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.60", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 60, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.61", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 61, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.62", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 62, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.63", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 63, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt another command station opsw access
        lnopsmodeprogrammer.readCV("1.64", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 64, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 0, pl.getRcvdValue());

        // attempt an out-of-range command station opsw access
        lnopsmodeprogrammer.readCV("1.65", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 65, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 1, pl.getRcvdStatus());

     }

     public void testCommandStationReadOutOfBounds1() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 4, true);

        // set command station opsw mode
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETCSOPSWMODE);

        // attempt a command station opsw access
        lnopsmodeprogrammer.readCV("1.01", pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // should have written and not returned

        Assert.assertEquals("sent byte 0", 0xBB, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x7f, lnis.outbound.get(0).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x00, lnis.outbound.get(0).getElement(2) & 0xFF);

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // Known-good message in reply
        // command station reply
        m = new LocoNetMessage(new int[] {LnConstants.OPC_SL_RD_DATA,
            0x0E, 0x7F, 0x7d, 0x6e, 0x5a, 0x4d,
            0x07, 0x3b, 0x2e, 0x1e, 0x0f, 0x69, 0x77});
        lnopsmodeprogrammer.message(m);

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Got programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Reply value matches", 1, pl.getRcvdValue());


        // attempt an out-of-range command station opsw access
        lnopsmodeprogrammer.readCV("1.0", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());

        // attempt an out-of-range command station opsw access
        lnopsmodeprogrammer.readCV("1.-1", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 3, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());

        // attempt an out-of-range command station opsw access
        lnopsmodeprogrammer.readCV("1", pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 1, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 4, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());

     }

     public void testCommandStationWriteOutOfBounds1() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 4, true);

        // set command station opsw mode
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETCSOPSWMODE);

        // attempt an out-of-range command station opsw access
        lnopsmodeprogrammer.writeCV("1.0", 1, pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 0, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());

        // attempt an out-of-range command station opsw access
        lnopsmodeprogrammer.writeCV("1.-1", 0, pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 0, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());

        // attempt an out-of-range command station opsw access
        lnopsmodeprogrammer.writeCV("1", 1, pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 0, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 3, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());

        // attempt an out-of-range command station opsw access
        lnopsmodeprogrammer.writeCV("1.1", -1, pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 0, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 4, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());

        // attempt an out-of-range command station opsw access
        lnopsmodeprogrammer.writeCV("1.1", 2, pl);

        // should not have sent another message
        Assert.assertEquals("no new message sent", 0, lnis.outbound.size());
        Assert.assertEquals("should get a programming reply", 5, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status Not Implemented", 8, pl.getRcvdStatus());

     }

     public void testCmdStnOpSwWrite() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(sm, memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETCSOPSWMODE);
        lnopsmodeprogrammer.writeCV("1.5", 1, pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0", 0xbb, lnis.outbound.get(0).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x7f, lnis.outbound.get(0).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x00, lnis.outbound.get(0).getElement(2) & 0xFF);

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m); // propagate the message from

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());
        Assert.assertEquals("received no messages", 0, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xE7, 0x0e, 0x7f, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
            0x00, 0x69});

        lnis.sendTestMessage(m);
        Assert.assertEquals("sent byte 0", 0xeF, lnis.outbound.get(1).getElement(0) & 0xFF);
        Assert.assertEquals("sent byte 1", 0x0e, lnis.outbound.get(1).getElement(1) & 0xFF);
        Assert.assertEquals("sent byte 2", 0x7f, lnis.outbound.get(1).getElement(2) & 0xFF);
        Assert.assertEquals("sent byte 3", 0x10, lnis.outbound.get(1).getElement(3) & 0xFF);
        Assert.assertEquals("sent byte 4", 0x00, lnis.outbound.get(1).getElement(4) & 0xFF);
        Assert.assertEquals("sent byte 5", 0x00, lnis.outbound.get(1).getElement(5) & 0xFF);
        Assert.assertEquals("sent byte 6", 0x00, lnis.outbound.get(1).getElement(6) & 0xFF);
        Assert.assertEquals("sent byte 7", 0x00, lnis.outbound.get(1).getElement(7) & 0xFF);
        Assert.assertEquals("sent byte 8", 0x00, lnis.outbound.get(1).getElement(8) & 0xFF);
        Assert.assertEquals("sent byte 9", 0x00, lnis.outbound.get(1).getElement(9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(1).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x00, lnis.outbound.get(1).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(1).getElement(12) & 0xFF);

        // No reply message, wait for timeout
        jmri.util.JUnitUtil.waitFor(()->{return lnis.outbound.size() == 2;},"programming reply not received");
        lnopsmodeprogrammer.message(m); // propagate reply to to ops mode programmer

        Assert.assertEquals("two message sent", 2, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());
        Assert.assertEquals("received one messages", 1, lnis.getReceivedMsgCount());

        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("two messages sent", 2, lnis.outbound.size());
        Assert.assertEquals("one programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write
        lnopsmodeprogrammer.writeCV("1.5", 0, pl);

        // should have written
        Assert.assertEquals("three messages sent", 3, lnis.outbound.size());
        Assert.assertEquals("one previous programming reply", 1, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0",  0xeF, lnis.outbound.get(1).getElement( 0) & 0xFF);
        Assert.assertEquals("sent byte 1",  0x0e, lnis.outbound.get(1).getElement( 1) & 0xFF);
        Assert.assertEquals("sent byte 2",  0x7f, lnis.outbound.get(1).getElement( 2) & 0xFF);
        Assert.assertEquals("sent byte 3",  0x00, lnis.outbound.get(1).getElement( 3) & 0xFF);
        Assert.assertEquals("sent byte 4",  0x00, lnis.outbound.get(1).getElement( 4) & 0xFF);
        Assert.assertEquals("sent byte 5",  0x00, lnis.outbound.get(1).getElement( 5) & 0xFF);
        Assert.assertEquals("sent byte 6",  0x00, lnis.outbound.get(1).getElement( 6) & 0xFF);
        Assert.assertEquals("sent byte 7",  0x00, lnis.outbound.get(1).getElement( 7) & 0xFF);
        Assert.assertEquals("sent byte 8",  0x00, lnis.outbound.get(1).getElement( 8) & 0xFF);
        Assert.assertEquals("sent byte 9",  0x00, lnis.outbound.get(1).getElement( 9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(1).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x00, lnis.outbound.get(1).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(1).getElement(12) & 0xFF);

        // check echo of sent message has no effect
        m = lnis.outbound.get(2);
        lnopsmodeprogrammer.message(m); // propagate the message from

        Assert.assertEquals("three messages sent", 3, lnis.outbound.size());
        Assert.assertEquals("one previous programming reply", 1, pl.getRcvdInvoked());
        Assert.assertEquals("received a messages", 2, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("another message sent", 3, lnis.outbound.size());
        Assert.assertEquals("two programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write (same as last one)
        lnopsmodeprogrammer.writeCV("1.5", 0, pl);

        // should have written
        Assert.assertEquals("four messages sent", 4, lnis.outbound.size());
        Assert.assertEquals("two previous programming replies", 2, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0",  0xeF, lnis.outbound.get(3).getElement( 0) & 0xFF);
        Assert.assertEquals("sent byte 1",  0x0e, lnis.outbound.get(3).getElement( 1) & 0xFF);
        Assert.assertEquals("sent byte 2",  0x7f, lnis.outbound.get(3).getElement( 2) & 0xFF);
        Assert.assertEquals("sent byte 3",  0x00, lnis.outbound.get(3).getElement( 3) & 0xFF);
        Assert.assertEquals("sent byte 4",  0x00, lnis.outbound.get(3).getElement( 4) & 0xFF);
        Assert.assertEquals("sent byte 5",  0x00, lnis.outbound.get(3).getElement( 5) & 0xFF);
        Assert.assertEquals("sent byte 6",  0x00, lnis.outbound.get(3).getElement( 6) & 0xFF);
        Assert.assertEquals("sent byte 7",  0x00, lnis.outbound.get(3).getElement( 7) & 0xFF);
        Assert.assertEquals("sent byte 8",  0x00, lnis.outbound.get(3).getElement( 8) & 0xFF);
        Assert.assertEquals("sent byte 9",  0x00, lnis.outbound.get(3).getElement( 9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(3).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x00, lnis.outbound.get(3).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(3).getElement(12) & 0xFF);

        // check echo of sent message has no effect
        m = lnis.outbound.get(3);
        lnopsmodeprogrammer.message(m); // propagate the message from

        Assert.assertEquals("four messages sent", 4, lnis.outbound.size());
        Assert.assertEquals("one previous programming reply", 2, pl.getRcvdInvoked());
        Assert.assertEquals("received a messages", 3, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("another message sent", 4, lnis.outbound.size());
        Assert.assertEquals("three programming replies", 3, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());



        // try another write
        lnopsmodeprogrammer.writeCV("1.1", 1, pl);

        // should have written
        Assert.assertEquals("four messages sent", 5, lnis.outbound.size());
        Assert.assertEquals("three previous programming replies", 3, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0",  0xeF, lnis.outbound.get(4).getElement( 0) & 0xFF);
        Assert.assertEquals("sent byte 1",  0x0e, lnis.outbound.get(4).getElement( 1) & 0xFF);
        Assert.assertEquals("sent byte 2",  0x7f, lnis.outbound.get(4).getElement( 2) & 0xFF);
        Assert.assertEquals("sent byte 3",  0x01, lnis.outbound.get(4).getElement( 3) & 0xFF);
        Assert.assertEquals("sent byte 4",  0x00, lnis.outbound.get(4).getElement( 4) & 0xFF);
        Assert.assertEquals("sent byte 5",  0x00, lnis.outbound.get(4).getElement( 5) & 0xFF);
        Assert.assertEquals("sent byte 6",  0x00, lnis.outbound.get(4).getElement( 6) & 0xFF);
        Assert.assertEquals("sent byte 7",  0x00, lnis.outbound.get(4).getElement( 7) & 0xFF);
        Assert.assertEquals("sent byte 8",  0x00, lnis.outbound.get(4).getElement( 8) & 0xFF);
        Assert.assertEquals("sent byte 9",  0x00, lnis.outbound.get(4).getElement( 9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(4).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x00, lnis.outbound.get(4).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(4).getElement(12) & 0xFF);

        // check echo of sent message has no effect
        m = lnis.outbound.get(3);
        lnopsmodeprogrammer.message(m); // propagate the message from

        Assert.assertEquals("four messages sent", 5, lnis.outbound.size());
        Assert.assertEquals("three previous programming replies", 3, pl.getRcvdInvoked());
        Assert.assertEquals("received a messages", 4, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("another message sent", 5, lnis.outbound.size());
        Assert.assertEquals("four programming replies", 4, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write
        lnopsmodeprogrammer.writeCV("1.63", 1, pl);

        // should have written
        Assert.assertEquals("four messages sent", 6, lnis.outbound.size());
        Assert.assertEquals("four previous programming replies", 4, pl.getRcvdInvoked());

        Assert.assertEquals("sent byte 0",  0xeF, lnis.outbound.get(5).getElement( 0) & 0xFF);
        Assert.assertEquals("sent byte 1",  0x0e, lnis.outbound.get(5).getElement( 1) & 0xFF);
        Assert.assertEquals("sent byte 2",  0x7f, lnis.outbound.get(5).getElement( 2) & 0xFF);
        Assert.assertEquals("sent byte 3",  0x01, lnis.outbound.get(5).getElement( 3) & 0xFF);
        Assert.assertEquals("sent byte 4",  0x00, lnis.outbound.get(5).getElement( 4) & 0xFF);
        Assert.assertEquals("sent byte 5",  0x00, lnis.outbound.get(5).getElement( 5) & 0xFF);
        Assert.assertEquals("sent byte 6",  0x00, lnis.outbound.get(5).getElement( 6) & 0xFF);
        Assert.assertEquals("sent byte 7",  0x00, lnis.outbound.get(5).getElement( 7) & 0xFF);
        Assert.assertEquals("sent byte 8",  0x00, lnis.outbound.get(5).getElement( 8) & 0xFF);
        Assert.assertEquals("sent byte 9",  0x00, lnis.outbound.get(5).getElement( 9) & 0xFF);
        Assert.assertEquals("sent byte 10", 0x00, lnis.outbound.get(5).getElement(10) & 0xFF);
        Assert.assertEquals("sent byte 11", 0x40, lnis.outbound.get(5).getElement(11) & 0xFF);
        Assert.assertEquals("sent byte 12", 0x00, lnis.outbound.get(5).getElement(12) & 0xFF);

        // check echo of sent message has no effect
        m = lnis.outbound.get(5);
        lnopsmodeprogrammer.message(m); // propagate the message from

        Assert.assertEquals("six messages sent", 6, lnis.outbound.size());
        Assert.assertEquals("four previous programming replies", 4, pl.getRcvdInvoked());
        Assert.assertEquals("received a messages", 5, lnis.getReceivedMsgCount());

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xb4, 0x6f, 0x7f, 0x00});
        lnis.sendTestMessage(m);

        Assert.assertEquals("another message sent", 6, lnis.outbound.size());
        Assert.assertEquals("five programming replies", 5, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

        // try another write
        lnopsmodeprogrammer.writeCV("1.56", 1, pl);

        // should have written
        Assert.assertEquals("another message sent", 7, lnis.outbound.size());
        Assert.assertEquals("six programming replies", 6, pl.getRcvdInvoked());
        Assert.assertEquals("Reply status bad", 1, pl.getRcvdStatus());
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
