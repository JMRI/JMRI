package jmri.jmrix.loconet;

import jmri.ProgListenerScaffold;
import jmri.ProgrammerException;
import jmri.util.JUnitUtil;
import jmri.ProgrammingMode;

import org.junit.Assert;
import org.junit.jupiter.api.*;

public class LnOpsModeProgrammerTest extends jmri.AddressedProgrammerTestBase{

    LocoNetInterfaceScaffold lnis;
    SlotManager sm;
    LocoNetSystemConnectionMemo memo;
    ProgListenerScaffold pl;
    LnOpsModeProgrammer lnopsmodeprogrammer;

    @Override
    @Test
    public void testGetCanWriteAddress() {
        Assert.assertFalse("can write address", programmer.getCanWrite("1234"));
    }

    @Test
    public void testSetMode() {
        try {
            lnopsmodeprogrammer.setMode(ProgrammingMode.PAGEMODE);
        } catch (IllegalArgumentException e) {
            return;
        }
        Assert.fail("No IllegalArgumentException thrown");
    }

    @Test
    public void testGetMode() {
        ProgrammingMode intRet = lnopsmodeprogrammer.getMode();
        Assert.assertEquals("OpsByteMode", ProgrammingMode.OPSBYTEMODE, intRet);
    }

    @Test
    public void testGetCanReadWithTransponding() {
        // allow transponding
        sm.setTranspondingAvailable(true);

        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 1, true);

        Assert.assertEquals("ops mode can read with transponding", true,
                lnopsmodeprogrammer.getCanRead());
    }

    @Test
    public void testSV2DataBytes() {
        LocoNetMessage m = new LocoNetMessage(15);

        // check data bytes
        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x12345678);
        Assert.assertEquals(0x10, m.getElement(10));
        Assert.assertEquals(0x78, m.getElement(11));
        Assert.assertEquals(0x56, m.getElement(12));
        Assert.assertEquals(0x34, m.getElement(13));
        Assert.assertEquals(0x12, m.getElement(14));
    }

    @Test
    public void testSV2highBits() {
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

    @Test
     public void testSOps16001Read() throws ProgrammerException {
        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 16001, true);

        lnopsmodeprogrammer.readCV("2",pl);

        // should have written and not returned
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("message", "[EF 0E 7C 2F 00 7D 01 00 00 01 00 7F 7F 00]", lnis.outbound.toString());

     }

     @Test
     public void testSv1Write() throws ProgrammerException {
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

     @Test
     public void testBoardRead0() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

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

     @Test
     public void testBoardRead1() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

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

     @Test
     public void testBoardReadTimeout() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

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

     @Test
     public void testBoardWrite() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

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

     @Test
     public void testBoardWriteTimeout() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

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

     @Test
     public void testSv1ARead() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 1, true);

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

     @Test
     public void testSv1BRead() throws ProgrammerException {
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

     @Test
     public void testSv2Write() throws ProgrammerException {
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

     @Test
     public void testSv2Read() throws ProgrammerException {
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

     @Test
     public void testOpsReadDecoderTransponding() throws ProgrammerException {
        // allow transponding
        sm.setTranspondingAvailable(true);

        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        lnopsmodeprogrammer.setMode(ProgrammingMode.OPSBYTEMODE);
        lnopsmodeprogrammer.readCV("12", pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("sent", "EF 0E 7C 2F 00 00 04 00 00 0B 00 7F 7F 00", lnis.outbound.get(0).toString());

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // LACK followed by Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25});
        lnopsmodeprogrammer.message(m);
        sm.message(m);
        m = new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B});
        lnopsmodeprogrammer.message(m);
        sm.message(m);
        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set");
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
     }

     @Test
     public void testOpsReadLocoNetMode() throws ProgrammerException {
        // allow transponding
        sm.setTranspondingAvailable(false);

        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETOPSBOARD);
        lnopsmodeprogrammer.readCV("12", pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("sent", "EF 0E 7C 2F 00 00 04 00 00 0B 00 7F 7F 00", lnis.outbound.get(0).toString());

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // LACK followed by Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25});
        lnopsmodeprogrammer.message(m);
        sm.message(m);

        m = new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B});
        lnopsmodeprogrammer.message(m);
        sm.message(m);
        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set");
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

     }

     @Test
     public void testOpsReadLocoNetModeLACKRejected() throws ProgrammerException {
        // allow transponding
        sm.setTranspondingAvailable(false);

        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETOPSBOARD);
        lnopsmodeprogrammer.readCV("12", pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("sent", "EF 0E 7C 2F 00 00 04 00 00 0B 00 7F 7F 00", lnis.outbound.get(0).toString());

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // LACK "command rejected" followed by Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x6F, 0x00, 0x24});
        lnopsmodeprogrammer.message(m);
        sm.message(m);

        m = new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B});
        lnopsmodeprogrammer.message(m);
        sm.message(m);
        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set");
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

     }

     @Test
     public void testOpsReadLocoNetModeLACKAcceptedBlind() throws ProgrammerException {
        // allow transponding
        sm.setTranspondingAvailable(false);

        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETOPSBOARD);
        lnopsmodeprogrammer.readCV("12", pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("sent", "EF 0E 7C 2F 00 00 04 00 00 0B 00 7F 7F 00", lnis.outbound.get(0).toString());

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        // LACK "accepted blind" followed by Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x6F, 0x40, 0x64});
        lnopsmodeprogrammer.message(m);
        sm.message(m);

        m = new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B});
        lnopsmodeprogrammer.message(m);
        sm.message(m);
        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set");
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());

     }

    @Test
     public void testOneOps7genAccyCvReadAccess() throws ProgrammerException {
        // disallow transponding
        sm.setTranspondingAvailable(false);

        LnOpsModeProgrammer lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4625, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBD7OPSWMODE);
        lnopsmodeprogrammer.readCV("12", pl);

        // should have written
        Assert.assertEquals("one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        Assert.assertEquals("sent", "ED 0B 7F 54 07 04 58 64 0B 00 00", lnis.outbound.get(0).toString());

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);

        lnis.sendTestMessage(m); // (Device sends the message on LocoNet)

        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("No programming reply", 0, pl.getRcvdInvoked());

        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {

        }

        // receive a LACK "accepted" from "command station"
        m = new LocoNetMessage(new int[]{0xB4, 0x6d, 0x7f, 0x64});

        lnis.sendTestMessage(m); // (Device sends the message on LocoNet)

        // make sure the "accepted blind" was received.
        try {
            Thread.sleep(10);

        } catch (InterruptedException e) {

        }

        // Now "receive" the reply from the 7th-gen Accy device
        m = new LocoNetMessage(new int[]{0xB4, 0x6E, 0x12, 0x00});

        // lnopsmodeprogrammer.message(m);
        lnis.sendTestMessage(m); // (Device sends the message on LocoNet)

        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set");
        Assert.assertEquals("still one message sent", 1, lnis.outbound.size());
        Assert.assertEquals("Reply status OK", 0, pl.getRcvdStatus());
        Assert.assertEquals("Got read of 18", 18, pl.getRcvdValue());

        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {

        }
        // end of first read!

        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set");
     }

     @Test
     public void testOps7genAccyAccesses() throws ProgrammerException {
        // disallow transponding
        sm.setTranspondingAvailable(false);

        checkSome7thGenAccyReads(1);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(2);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(3);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(4);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(5);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(6);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(7);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(8);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(9);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(16);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(17);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(32);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(33);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(64);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(65);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(128);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(129);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(256);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(257);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(512);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(513);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(1024);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(1025);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(2030);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(2031);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyReads(2040);
        lnopsmodeprogrammer.dispose();

    }

    /*
    This is the targeted test.  The one that fails...
    */
    @Test
    public void testOps7genAccyWritesAccesses() throws ProgrammerException {
        // disallow transponding
        sm.setTranspondingAvailable(false);
        checkSome7thGenAccyWrites(1);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyWrites(2);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyWrites(3);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyWrites(4);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyWrites(256);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyWrites(275);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyWrites(519);
        lnopsmodeprogrammer.dispose();

        checkSome7thGenAccyWrites(2039);
        lnopsmodeprogrammer.dispose();

    }

     private void checkSome7thGenAccyReads(int address)  throws ProgrammerException {
        int incoming = 1;
        int num;
        int i;
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, address, true);
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBD7OPSWMODE);

        LocoNetMessage m;
        for (i = 0; i <8; ++ i) {

            lnis.clearReceivedMessages();
            Assert.assertEquals("Zero messages sent", 0, lnis.outbound.size());
            num = 1 << i;
            pl = new ProgListenerScaffold();
            Assert.assertEquals("no programmingListener reply (yet)",
                    0, pl.getRcvdInvoked());
            log.debug("At begin of loop {}, getRcvdInvolked = {}, Num {}",
                    i, pl.getRcvdInvoked(), num);

            log.debug("--> Sending readCV() from testcase loop {} with CV num {}", i, num);
            lnopsmodeprogrammer.readCV(Integer.toString(num), pl);
            log.debug("   testcase continuing...  After the CV Read accesss started, getRcvdInvolked = {}",
                    pl.getRcvdInvoked());
            // should have sent the CV access to Addr 1 CV num (i)
            Assert.assertEquals("outbound size (i="+Integer.toString(i)+") ",
                    1, lnis.outbound.size());
            log.debug("  testcase checks lnis.outbound.size() as 1 and was ok.");
            Assert.assertEquals("no programmingListener reply (yet)",
                    0, pl.getRcvdInvoked());

            String snum = "0"+Integer.toHexString(num-1);
            if (snum.length() >= 3) {
                snum = snum.substring(snum.length() - 2);
            }
            snum = snum.toUpperCase();

            // Map the NMRA address into the Bytes of the LocoNet message
            int a = getNmraBasicAccyAddr(address);
            int a1 = a & 0x7f;
            int a2 = (a >> 8) & 0x7f;
            String sa1 = "0"+(Integer.toHexString(a1)).toUpperCase();
            if (sa1.length() != 2) {
                sa1 = sa1.substring(sa1.length()-2);
            }
            String sa2 = "0"+Integer.toHexString(a2).toUpperCase();
            if (sa2.length() != 2) {
                sa2 = sa2.substring(sa2.length()-2);
            }
            Assert.assertEquals("sent", "ED 0B 7F 54 07 " + sa2 + " " + sa1 +" 64 "+snum+" 00 00",
                    lnis.outbound.get(0).toString());
            // check echo of sent message has no effect
            log.debug("   testcase got access request message from JMRI's LocoNet transmit");
            m = lnis.outbound.get(0);
            log.debug("   testcase copies access request to IN as an echo");
            lnis.sendTestMessage(m);  // (LocoNet echo of transmitted message!)
            log.debug("   testcase has echoed CV access request");

            Assert.assertEquals("Still 1 message sent", 1, lnis.outbound.size());
            Assert.assertEquals("Still 0 programming replies",
                    0, pl.getRcvdInvoked());
            incoming++;

            // wait a little for command station response
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }

            Assert.assertEquals("saw 1 message sent", 1, lnis.outbound.size());
            // receive a LACK "accepted" from "command station"
            m = new LocoNetMessage(new int[]{0xB4, 0x6d, 0x7f, 0x64});

            log.debug("   testcase is sending c.s.'s 'long_ack' as {}", m.toString());
            lnis.sendTestMessage(m);  // (Command station default response)

            Assert.assertEquals("saw 1 message sent", 1, lnis.outbound.size());

            // make sure the "accepted blind" was received.
            Assert.assertNotNull("PL is not null",pl);
            final int j= incoming;
            log.debug("   testcase getRcvdInvolked = {}; j = incoming = {}.",
                    pl.getRcvdInvoked(), Integer.toString(j));
            JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 0;},"getRcvdInvoked not set after CS long_ack");
            Assert.assertEquals("Still Reply status not ready", -1, pl.getRcvdStatus());

            // Now "receive" the reply from the 7th-gen Accy device

            log.debug("   testcase device opc_long_ack reply being sent: "+m.toString()+
                    "; j = "+Integer.toString(j)+" before sent.");

            // wait a while for the device to reply
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }

            m = getLnLongAckFromVal(num);
            lnis.sendTestMessage(m); // (Device sends the message on LocoNet)

            log.debug("   testcase device opc_long_ack reply and data was sent.");

            log.debug("   testcase: after send, pl.getRcvdInvoked() is {}  for j = {}, num= {}.",
                    Integer.toString(pl.getRcvdInvoked()), j, num);
            JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set (#2)");
            Assert.assertEquals("saw 1 message sent", 1, lnis.outbound.size());
            Assert.assertEquals("Still Reply status OK", 0, pl.getRcvdStatus());
            Assert.assertEquals("Expected read of "+num, num, pl.getRcvdValue());
            log.debug("Got readCV result of {} at {}.",pl.getRcvdValue(), java.time.LocalTime.now());

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }

            log.debug("!!!!!! end of test loop !!!!!!");
        }
     }

    private void checkSome7thGenAccyWrites(int address)  throws ProgrammerException {
        int incoming = 1;
        int num;
        int i;
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, address, true);
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBD7OPSWMODE);

        LocoNetMessage m;
        for (i = 0; i <8; ++ i) {

            lnis.clearReceivedMessages();
            Assert.assertEquals("Zero messages sent", 0, lnis.outbound.size());
            num = 1 << i;
            pl = new ProgListenerScaffold();
            Assert.assertEquals("no programmingListener reply (yet)",
                    0, pl.getRcvdInvoked());
            log.debug("At begin of loop {}, getRcvdInvolked = {}, Num {}",
                    i, pl.getRcvdInvoked(), num);

            log.debug("--> Sending readCV() from testcase loop {} with CV num {}", i, num);
            lnopsmodeprogrammer.writeCV(Integer.toString(num), (~num) & 255, pl);
            log.debug("   testcase continuing...  After the CV Read accesss started, getRcvdInvolked = {}",
                    pl.getRcvdInvoked());
            // should have sent the CV access to Addr 1 CV num (i)
            Assert.assertEquals("outbound size (i="+Integer.toString(i)+") ",
                    1, lnis.outbound.size());
            log.debug("  testcase checks lnis.outbound.size() as 1 and was ok.");
            Assert.assertEquals("no programmingListener reply (yet)",
                    0, pl.getRcvdInvoked());

            String snum = "0"+Integer.toHexString(num-1);
            if (snum.length() >= 3) {
                snum = snum.substring(snum.length() - 2);
            }
            snum = snum.toUpperCase();

            // Map the NMRA address into the Bytes of the LocoNet message
            int a = getNmraBasicAccyAddr(address);
            int a1 = a & 0x7f;
            int a2 = (a >> 8) & 0x7f;

            String sa1 = "0"+(Integer.toHexString(a1)).toUpperCase();
            if (sa1.length() != 2) {
                sa1 = sa1.substring(sa1.length()-2);
            }
            String sa2 = "0"+Integer.toHexString(a2).toUpperCase();
            if (sa2.length() != 2) {
                sa2 = sa2.substring(sa2.length()-2);
            }
            String expectVal = "0"+Integer.toHexString(((~num)&127) ).toUpperCase();
            if (expectVal.length() > 2) {
                expectVal = expectVal.substring(expectVal.length()-2);
            }

            String evExtraBits = "0"+Integer.toHexString(7 + ((((~num)&128)==128)?0x10:0));
            if (evExtraBits.length() > 2) {
                evExtraBits = evExtraBits.substring(evExtraBits.length()-2);
            }

            Assert.assertEquals("sent", "ED 0B 7F 54 "+evExtraBits+" " + sa2 + " " + sa1 +" 6C "+snum+" "+ expectVal+" 00",
                    lnis.outbound.get(0).toString());
            // check echo of sent message has no effect
            log.debug("   testcase got access request message from JMRI's LocoNet transmit");
            m = lnis.outbound.get(0);
            log.debug("   testcase copies access request to IN as an echo");
            lnis.sendTestMessage(m);  // (LocoNet echo of transmitted message!)
            log.debug("   testcase has echoed CV access request");

            Assert.assertEquals("Still 1 message sent", 1, lnis.outbound.size());
            Assert.assertEquals("Still 0 programming replies",
                    0, pl.getRcvdInvoked());
            incoming++;

            // wait a little for command station response
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }

            Assert.assertEquals("saw 1 message sent", 1, lnis.outbound.size());
            // receive a LACK "accepted" from "command station"
            m = new LocoNetMessage(new int[]{0xB4, 0x6d, 0x7f, 0x64});

            log.debug("   testcase is sending c.s.'s 'long_ack' as {}", m.toString());
            lnis.sendTestMessage(m);  // (Command station default response)

            Assert.assertEquals("saw 1 message sent", 1, lnis.outbound.size());

            // make sure the "accepted blind" was received.
            Assert.assertNotNull("PL is not null",pl);
            final int j= incoming;
            log.debug("   testcase getRcvdInvolked = {}; j = incoming = {}.",
                    pl.getRcvdInvoked(), Integer.toString(j));
            JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 0;},"getRcvdInvoked not set after CS long_ack");
            Assert.assertEquals("Still Reply status not ready", -1, pl.getRcvdStatus());

            // Now "receive" the reply from the 7th-gen Accy device

            log.debug("   testcase device opc_long_ack reply being sent: "+m.toString()+
                    "; j = "+Integer.toString(j)+" before sent.");

            // wait a while for the device to reply
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }

            m = getLnLongAckFromVal(0x5A);
            lnis.sendTestMessage(m); // (Device sends the message on LocoNet)

            log.debug("   testcase device opc_long_ack reply and data was sent.");

            log.debug("   testcase: after send, pl.getRcvdInvoked() is {}  for j = {}, num= {}.",
                    Integer.toString(pl.getRcvdInvoked()), j, num);
            JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set (#2)");
            Assert.assertEquals("saw 1 message sent", 1, lnis.outbound.size());
            Assert.assertEquals("Still Reply status OK", 0, pl.getRcvdStatus());
            Assert.assertEquals("Expected result of 0x5A", 0x5A, pl.getRcvdValue());
            log.debug("Got readCV result of {} at {}.",pl.getRcvdValue(), java.time.LocalTime.now());

            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }

            log.debug("!!!!!! end of test loop !!!!!!");
        }
     }

    private LocoNetMessage getLnLongAckFromVal(int val) {
        int opc = LnConstants.OPC_LONG_ACK;
        int prevOpc = val>127 ? 0x6D : 0x6E;
        int val7 = val & 0x7f;
        int chk = 255 ^ opc ^ prevOpc ^ val7;
        return new LocoNetMessage(new int[] {opc, prevOpc, val7, chk});
    }

    /*
    "Basic-like" address
    10AAAAAA 1AAA1AA0
      000000  100 00
      765432  098 10
    */
    private int getNmraBasicAccyAddr(int addr) {
        int a = addr-1;
        int a2 = a & 0x3;
        int a3 = (a & 0xFC) >> 2;
        int a4 = (~((a & 0x700) >> 8)) & 0x7;
        return (a2 << 1) + (a4 << 4) + (a3 << 8) + (0x8000 + 0x80 + 0x8);
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();

        jmri.InstanceManager.setDefault(jmri.jmrix.ConnectionConfigManager.class,
                new jmri.jmrix.ConnectionConfigManager());

        lnis = new LocoNetInterfaceScaffold();
        sm = new SlotManager(lnis);
        sm.pmManagerGotReply = true; // tells slotman probing done
        memo = new LocoNetSystemConnectionMemo(lnis, sm);
        pl = new ProgListenerScaffold();
        programmer = lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 1, true);
    }

    @AfterEach
    @Override
    public void tearDown() {
        memo.dispose();
        lnis = null;

        programmer = lnopsmodeprogrammer = null;
        JUnitUtil.tearDown();
    }
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnOpsModeProgrammerTest.class);
}
