package jmri.jmrix.loconet;

import jmri.ProgListenerScaffold;
import jmri.ProgrammerException;
import jmri.ProgrammingMode;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class LnOpsModeProgrammerTest extends jmri.AddressedProgrammerTestBase{

    private LocoNetInterfaceScaffold lnis;
    private SlotManager sm;
    private LocoNetSystemConnectionMemo memo;
    private ProgListenerScaffold pl;
    private LnOpsModeProgrammer lnopsmodeprogrammer;

    @Override
    @Test
    public void testGetCanWriteAddress() {
        assertFalse( programmer.getCanWrite("1234"), "can write address");
    }

    @Test
    public void testSetMode() {
        IllegalArgumentException ex = assertThrows( IllegalArgumentException.class, () ->
            lnopsmodeprogrammer.setMode(ProgrammingMode.PAGEMODE),
        "No IllegalArgumentException thrown");
        assertNotNull(ex);
    }

    @Test
    public void testGetMode() {
        ProgrammingMode intRet = lnopsmodeprogrammer.getMode();
        assertEquals( ProgrammingMode.OPSBYTEMODE, intRet, "OpsByteMode");
    }

    @Test
    public void testGetCanReadWithTransponding() {
        // allow transponding
        sm.setTranspondingAvailable(true);

        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 1, true);

        assertTrue( lnopsmodeprogrammer.getCanRead(), "ops mode can read with transponding");
    }

    @Test
    public void testSV2DataBytes() {
        LocoNetMessage m = new LocoNetMessage(15);

        // check data bytes
        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x12345678);
        assertEquals(0x10, m.getElement(10));
        assertEquals(0x78, m.getElement(11));
        assertEquals(0x56, m.getElement(12));
        assertEquals(0x34, m.getElement(13));
        assertEquals(0x12, m.getElement(14));
    }

    @Test
    public void testSV2highBits() {
        LocoNetMessage m = new LocoNetMessage(15);

        // check high bits
        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x01020384);
        assertEquals(0x11, m.getElement(10));
        assertEquals(0x04, m.getElement(11));
        assertEquals(0x03, m.getElement(12));
        assertEquals(0x02, m.getElement(13));
        assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x01028304);
        assertEquals(0x12, m.getElement(10));
        assertEquals(0x04, m.getElement(11));
        assertEquals(0x03, m.getElement(12));
        assertEquals(0x02, m.getElement(13));
        assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x01820304);
        assertEquals(0x14, m.getElement(10));
        assertEquals(0x04, m.getElement(11));
        assertEquals(0x03, m.getElement(12));
        assertEquals(0x02, m.getElement(13));
        assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x81020304);
        assertEquals(0x18, m.getElement(10));
        assertEquals(0x04, m.getElement(11));
        assertEquals(0x03, m.getElement(12));
        assertEquals(0x02, m.getElement(13));
        assertEquals(0x01, m.getElement(14));

        lnopsmodeprogrammer.loadSV2MessageFormat(m, 0, 0, 0x81828384);
        assertEquals(0x1F, m.getElement(10));
        assertEquals(0x04, m.getElement(11));
        assertEquals(0x03, m.getElement(12));
        assertEquals(0x02, m.getElement(13));
        assertEquals(0x01, m.getElement(14));
    }

    @Test
    public void testSOps16001Read() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 16001, true);

        lnopsmodeprogrammer.readCV("2",pl);

        // should have written and not returned
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        assertEquals( "[EF 0E 7C 2F 00 7D 01 00 00 01 00 7F 7F 00]", lnis.outbound.toString(), "message");

    }

    @Test
    public void testSv1Write() throws ProgrammerException {
        int testVal = 120;
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETSV1MODE);
        lnopsmodeprogrammer.writeCV("91",testVal,pl);

        // should have written and not returned
        assertEquals(1, lnis.outbound.size(), "one message sent");
        assertEquals(0, pl.getRcvdInvoked(), "No programming reply");

         // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        assertEquals(1, lnis.outbound.size(), "still one message sent");
        assertEquals(0, pl.getRcvdInvoked(), "No programming reply");

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xE5, 0x10, 0x53, 0x50, 0x01, 0x00, 0x01, 0x5B, 0x66, 0x7B, 0x00, 0x01, 0x00, 0x00, testVal, 0x36});
        lnopsmodeprogrammer.message(m);

        assertEquals(1, lnis.outbound.size(), "still one message sent");
        assertEquals(1, pl.getRcvdInvoked(), "Got programming reply");
        assertEquals(0, pl.getRcvdStatus(), "Reply status OK");
        assertEquals(testVal, pl.getRcvdValue(), "Reply value matches written");

    }

    @Test
    public void testBoardRead0() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBDOPSWMODE);
        lnopsmodeprogrammer.readCV("113.6",pl);

        // should have written and not returned
        assertEquals(1, lnis.outbound.size(), "one message sent");
        assertEquals(0, pl.getRcvdInvoked(), "No programming reply");

        assertEquals(0xD0, lnis.outbound.get(0).getElement(0) & 0xFF, "sent byte 0");
        assertEquals(0x62, lnis.outbound.get(0).getElement(1) & 0xFF, "sent byte 1");
        assertEquals(0x03, lnis.outbound.get(0).getElement(2) & 0xFF, "sent byte 2");
        assertEquals(113, lnis.outbound.get(0).getElement(3) & 0xFF, "sent byte 3");
        assertEquals(0x0A, lnis.outbound.get(0).getElement(4) & 0xFF, "sent byte 4");

        int testVal = 0;

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        assertEquals(1, lnis.outbound.size(), "still one message sent");
        assertEquals(0, pl.getRcvdInvoked(), "No programming reply");

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x50, 0x40, 0x00});
        lnopsmodeprogrammer.message(m);
        assertEquals(1, lnis.outbound.size(), "still one message sent");
        assertEquals(1, pl.getRcvdInvoked(), "Got programming reply");
        assertEquals(0, pl.getRcvdStatus(), "Reply status OK");
        assertEquals(testVal, pl.getRcvdValue(), "Reply value matches");

    }

    @Test
    public void testBoardRead1() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBDOPSWMODE);
        lnopsmodeprogrammer.readCV("113.6",pl);

        // should have written and not returned
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        assertEquals( 0xD0, lnis.outbound.get(0).getElement(0) & 0xFF, "sent byte 0");
        assertEquals( 0x62, lnis.outbound.get(0).getElement(1) & 0xFF, "sent byte 1");
        assertEquals( 0x03, lnis.outbound.get(0).getElement(2) & 0xFF, "sent byte 2");
        assertEquals( 113, lnis.outbound.get(0).getElement(3) & 0xFF, "sent byte 3");
        assertEquals( 0x0A, lnis.outbound.get(0).getElement(4) & 0xFF, "sent byte 4");

        int testVal = 1;

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x50, 0x60, 0x00});
        lnopsmodeprogrammer.message(m);
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 1, pl.getRcvdInvoked(), "Got programming reply");
        assertEquals( 0, pl.getRcvdStatus(), "Reply status OK");
        assertEquals( testVal, pl.getRcvdValue(), "Reply value matches");

    }

    @Test
    public void testBoardReadTimeout() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBDOPSWMODE);
        lnopsmodeprogrammer.readCV("113.6",pl);

        // should have written and not returned
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        // No reply message, wait for timeout
        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"programming reply not received");
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( jmri.ProgListener.FailedTimeout, pl.getRcvdStatus(), "Reply status Not OK");
        assertTrue( pl.wasRightThread(), "Correct thread");
    }

    @Test
    public void testBoardWrite() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        int testVal = 1;

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBDOPSWMODE);
        lnopsmodeprogrammer.writeCV("113.6", testVal, pl);

        // should have written
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        assertEquals( 0xD0, lnis.outbound.get(0).getElement(0) & 0xFF, "sent byte 0");
        assertEquals( 0x72, lnis.outbound.get(0).getElement(1) & 0xFF, "sent byte 1");
        assertEquals( 0x03, lnis.outbound.get(0).getElement(2) & 0xFF, "sent byte 2");
        assertEquals( 113, lnis.outbound.get(0).getElement(3) & 0xFF, "sent byte 3");
        assertEquals( 0x0B, lnis.outbound.get(0).getElement(4) & 0xFF, "sent byte 4");

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x50, 0x60, 0x00});
        lnopsmodeprogrammer.message(m);
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 1, pl.getRcvdInvoked(), "Got programming reply");
        assertEquals( 0, pl.getRcvdStatus(), "Reply status OK");
        assertEquals( testVal, pl.getRcvdValue(), "Reply value matches");

    }

    @Test
    public void testBoardWriteTimeout() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        int testVal = 1;

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBDOPSWMODE);
        lnopsmodeprogrammer.writeCV("113.6", testVal, pl);

        // should have written
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        // No reply message, wait for timeout
        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"programming reply not received");
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( jmri.ProgListener.FailedTimeout, pl.getRcvdStatus(), "Reply status Not OK");
        assertTrue( pl.wasRightThread(), "Correct thread");
     }

    @Test
    public void testSv1ARead() throws ProgrammerException {
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 1, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETSV1MODE);
        lnopsmodeprogrammer.readCV("83", pl);

        // should have written and not returned
        assertEquals(1, lnis.outbound.size(), "one message sent");
        assertEquals(0, pl.getRcvdInvoked(), "No programming reply");

        assertEquals(0xE5, lnis.outbound.get(0).getElement(0) & 0xFF, "sent byte 0");
        assertEquals(0x50, lnis.outbound.get(0).getElement(2) & 0xFF, "sent byte 2");

        int testVal = 132;

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        assertEquals(1, lnis.outbound.size(), "still one message sent");
        assertEquals(0, pl.getRcvdInvoked(), "No programming reply");

        // Known-good message in reply
        m = new LocoNetMessage(new int[]{0xE5, 0x10, 0x53, 0x50, 0x01, 0x00, 0x02, 0x5B, 0x66, 0x7B, 0x02, 0x01, 0x04, 0x00, 0x00, 0x48});
        lnopsmodeprogrammer.message(m);
        assertEquals(1, lnis.outbound.size(), "still one message sent");
        assertEquals(1, pl.getRcvdInvoked(), "Got programming reply");
        assertEquals(0, pl.getRcvdStatus(), "Reply status OK");
        assertEquals(testVal, pl.getRcvdValue(), "Reply value matches");
    }

    @Test
    public void testSv1BRead() throws ProgrammerException {
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETSV1MODE);
        lnopsmodeprogrammer.readCV("83",pl);

        // should have written and not returned
        assertEquals(1, lnis.outbound.size(), "one message sent");
        assertEquals(0, pl.getRcvdInvoked(), "No programming reply");

        assertEquals(0xE5, lnis.outbound.get(0).getElement(0) & 0xFF, "sent byte 0");
        assertEquals(0x50, lnis.outbound.get(0).getElement(2) & 0xFF, "sent byte 2");

        int testVal = 47; // 0x2F

        // Known-good message in reply
        LocoNetMessage m
            = new LocoNetMessage(new int[]{0xE5, 0x10, 0x53, 0x50, 0x01, 0x00, 0x02, 0x03, 0x66, 0x7B, 0x00, 0x01, 0x2F, 0x78, 0x10, 0x52});
        lnopsmodeprogrammer.message(m);

        assertEquals(1, lnis.outbound.size(), "still one message sent");
        assertEquals(1, pl.getRcvdInvoked(), "Got programming reply");
        assertEquals(0, pl.getRcvdStatus(), "Reply status OK");
        assertEquals(testVal, pl.getRcvdValue(), "Reply value matches");
    }

    @Test
    public void testSv2Write() throws ProgrammerException {
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETSV2MODE);
        lnopsmodeprogrammer.writeCV("22",33,pl);

        // should have written and not returned
        assertEquals(1, lnis.outbound.size(), "one message sent");
        assertEquals(0, pl.getRcvdInvoked(), "No programming reply");

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        assertEquals(1, lnis.outbound.size(), "still one message sent");
        assertEquals(0, pl.getRcvdInvoked(), "No programming reply");

        // turn the message around as a reply
        m.setElement(3, m.getElement(3) | 0x40);
        lnopsmodeprogrammer.message(m);

        assertEquals(1, lnis.outbound.size(), "still one message sent");
        assertEquals(1, pl.getRcvdInvoked(), "Got programming reply");
        assertEquals(0, pl.getRcvdStatus(), "Reply status OK");
    }

    @Test
    public void testSv2Read() throws ProgrammerException {
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETSV2MODE);
        lnopsmodeprogrammer.readCV("22",pl);

        // should have written and not returned
        assertEquals(1, lnis.outbound.size(), "one message sent");
        assertEquals(0, pl.getRcvdInvoked(), "No programming reply");

        int testVal = 130;

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        assertEquals(1, lnis.outbound.size(), "still one message sent");
        assertEquals(0, pl.getRcvdInvoked(), "No programming reply");

        // turn the message around as a reply
        m.setElement(3, m.getElement(3) | 0x40);
        m.setElement(10, m.getElement(10) & 0x7E | 1);
        m.setElement(11, testVal & 0x7F);
        lnopsmodeprogrammer.message(m);

        assertEquals(1, lnis.outbound.size(), "still one message sent");
        assertEquals(1, pl.getRcvdInvoked(), "Got programming reply");
        assertEquals(0, pl.getRcvdStatus(), "Reply status OK");
        assertEquals(130, pl.getRcvdValue(), "Reply value matches");
    }

    @Test
    public void testOpsReadDecoderTransponding() throws ProgrammerException {
        // allow transponding
        sm.setTranspondingAvailable(true);

        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        lnopsmodeprogrammer.setMode(ProgrammingMode.OPSBYTEMODE);
        lnopsmodeprogrammer.readCV("12", pl);

        // should have written
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        assertEquals( "EF 0E 7C 2F 00 00 04 00 00 0B 00 7F 7F 00", lnis.outbound.get(0).toString(), "sent");

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        // LACK followed by Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25});
        lnopsmodeprogrammer.message(m);
        sm.message(m);
        m = new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B});
        lnopsmodeprogrammer.message(m);
        sm.message(m);
        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set");
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 0, pl.getRcvdStatus(), "Reply status OK");
    }

    @Test
    public void testOpsReadLocoNetMode() throws ProgrammerException {
        // allow transponding
        sm.setTranspondingAvailable(false);

        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETOPSBOARD);
        lnopsmodeprogrammer.readCV("12", pl);

        // should have written
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        assertEquals( "EF 0E 7C 2F 00 00 04 00 00 0B 00 7F 7F 00", lnis.outbound.get(0).toString(), "sent");

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        // LACK followed by Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x6F, 0x01, 0x25});
        lnopsmodeprogrammer.message(m);
        sm.message(m);

        m = new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B});
        lnopsmodeprogrammer.message(m);
        sm.message(m);
        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set");
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 0, pl.getRcvdStatus(), "Reply status OK");

    }

    @Test
    public void testOpsReadLocoNetModeLACKRejected() throws ProgrammerException {
        // allow transponding
        sm.setTranspondingAvailable(false);

        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETOPSBOARD);
        lnopsmodeprogrammer.readCV("12", pl);

        // should have written
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        assertEquals( "EF 0E 7C 2F 00 00 04 00 00 0B 00 7F 7F 00", lnis.outbound.get(0).toString(), "sent");

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        // LACK "command rejected" followed by Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x6F, 0x00, 0x24});
        lnopsmodeprogrammer.message(m);
        sm.message(m);

        m = new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B});
        lnopsmodeprogrammer.message(m);
        sm.message(m);
        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set");
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 0, pl.getRcvdStatus(), "Reply status OK");

    }

    @Test
    public void testOpsReadLocoNetModeLACKAcceptedBlind() throws ProgrammerException {
        // allow transponding
        sm.setTranspondingAvailable(false);

        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETOPSBOARD);
        lnopsmodeprogrammer.readCV("12", pl);

        // should have written
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        assertEquals( "EF 0E 7C 2F 00 00 04 00 00 0B 00 7F 7F 00", lnis.outbound.get(0).toString(), "sent");

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);
        lnopsmodeprogrammer.message(m);
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        // LACK "accepted blind" followed by Known-good message in reply
        m = new LocoNetMessage(new int[]{0xB4, 0x6F, 0x40, 0x64});
        lnopsmodeprogrammer.message(m);
        sm.message(m);

        m = new LocoNetMessage(new int[]{0xE7, 0x0E, 0x7C, 0x2B, 0x00, 0x00, 0x02, 0x47, 0x00, 0x1C, 0x23, 0x7F, 0x7F, 0x3B});
        lnopsmodeprogrammer.message(m);
        sm.message(m);
        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set");
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 0, pl.getRcvdStatus(), "Reply status OK");

    }

    @Test
    public void testOneOps7genAccyCvReadAccess() throws ProgrammerException {
        // disallow transponding
        sm.setTranspondingAvailable(false);

        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, 4625, true);

        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBD7OPSWMODE);
        lnopsmodeprogrammer.readCV("12", pl);

        // should have written
        assertEquals( 1, lnis.outbound.size(), "one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        assertEquals( "ED 0B 7F 54 07 04 58 64 0B 00 00", lnis.outbound.get(0).toString(), "sent");

        // check echo of sent message has no effect
        LocoNetMessage m = lnis.outbound.get(0);

        lnis.sendTestMessage(m); // (Device sends the message on LocoNet)

        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 0, pl.getRcvdInvoked(), "No programming reply");

        JUnitUtil.waitFor(1);

        // receive a LACK "accepted" from "command station"
        m = new LocoNetMessage(new int[]{0xB4, 0x6d, 0x7f, 0x64});

        lnis.sendTestMessage(m); // (Device sends the message on LocoNet)

        // make sure the "accepted blind" was received.
        JUnitUtil.waitFor(10);

        // Now "receive" the reply from the 7th-gen Accy device
        m = new LocoNetMessage(new int[]{0xB4, 0x6E, 0x12, 0x00});

        // lnopsmodeprogrammer.message(m);
        lnis.sendTestMessage(m); // (Device sends the message on LocoNet)

        JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set");
        assertEquals( 1, lnis.outbound.size(), "still one message sent");
        assertEquals( 0, pl.getRcvdStatus(), "Reply status OK");
        assertEquals( 18, pl.getRcvdValue(), "Got read of 18");

        JUnitUtil.waitFor(10);
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

        log.debug("checkSome7thGenAccyReads start; address = {}", address);

        LocoNetMessage m;
        for (i = 0; i <8; ++ i) {
            log.debug("checkSome7thGenAccyReads iteration {}", i);

            lnis.clearReceivedMessages();
            assertEquals( 0, lnis.outbound.size(), "Zero messages sent");
            num = 1 << i;
            pl = new ProgListenerScaffold();
            assertEquals( 0, pl.getRcvdInvoked(), "no programmingListener reply (yet)");
            log.debug("At begin of loop {}, getRcvdInvolked = {}, Num {}",
                    i, pl.getRcvdInvoked(), num);

            log.debug("--> Sending readCV() from testcase loop {} with CV num {}", i, num);
            lnopsmodeprogrammer.readCV(Integer.toString(num), pl);
            log.debug("   testcase continuing...  After the CV Read accesss started, getRcvdInvolked = {}",
                    pl.getRcvdInvoked());
            // should have sent the CV access to Addr 1 CV num (i)

            assertEquals( 1, lnis.outbound.size(),
                "outbound size (i="+Integer.toString(i)+") ");
            log.debug("  testcase checks lnis.outbound.size() as 1 and was ok.");
            assertEquals( 0, pl.getRcvdInvoked(), "no programmingListener reply (yet)");

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
            assertEquals( "ED 0B 7F 54 07 " + sa2 + " " + sa1 +" 64 "+snum+" 00 00",
                lnis.outbound.get(0).toString(), "sent");
            // check echo of sent message has no effect
            log.debug("   testcase got access request message from JMRI's LocoNet transmit");
            m = lnis.outbound.get(0);
            log.debug("   testcase copies access request to IN as an echo");
            lnis.sendTestMessage(m);  // (LocoNet echo of transmitted message!)
            log.debug("   testcase has echoed CV access request");

            assertEquals( 1, lnis.outbound.size(), "Still 1 message sent");
            assertEquals( 0, pl.getRcvdInvoked(), "Still 0 programming replies");
            incoming++;

            // wait a little for command station response
            JUnitUtil.waitFor(1);

            assertEquals( 1, lnis.outbound.size(), "saw 1 message sent");
            // receive a LACK "accepted" from "command station"
            m = new LocoNetMessage(new int[]{0xB4, 0x6d, 0x7f, 0x64});

            log.debug("   testcase is sending c.s.'s 'long_ack' as {}", m.toString());
            lnis.sendTestMessage(m);  // (Command station default response)

            assertEquals( 1, lnis.outbound.size(), "saw 1 message sent");

            // make sure the "accepted blind" was received.
            assertNotNull(pl, "PL is not null");
            final int j= incoming;
            log.debug("   testcase getRcvdInvolked = {}; j = incoming = {}.",
                    pl.getRcvdInvoked(), Integer.toString(j));
            JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 0;},"getRcvdInvoked not set after CS long_ack");
            assertEquals( -1, pl.getRcvdStatus(), "Still Reply status not ready");

            // Now "receive" the reply from the 7th-gen Accy device

            log.debug("   testcase device opc_long_ack reply being sent: "+m.toString()+
                    "; j = "+Integer.toString(j)+" before sent.");

            // wait a while for the device to reply
            JUnitUtil.waitFor(1);

            m = getLnLongAckFromVal(num);
            lnis.sendTestMessage(m); // (Device sends the message on LocoNet)

            log.debug("   checkSome7thGenAccyReads: testcase device opc_long_ack reply and data was sent;"
                    + " after send, pl.getRcvdInvoked() is {}  for j = {}, num= {}.",
                    Integer.toString(pl.getRcvdInvoked()), j, num);
            JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set (#2)");
            assertEquals( 1, lnis.outbound.size(), "saw 1 message sent");
            assertEquals( 0, pl.getRcvdStatus(), "Still Reply status OK");
            assertEquals( num, pl.getRcvdValue(), "Expected read of "+num);
            log.debug("Got readCV result of {} at {}.",pl.getRcvdValue(), java.time.LocalTime.now());

            JUnitUtil.waitFor(1);

            log.debug("!! checkSome7thGenAccyReads: end of test !!");
        }
        log.debug("!!!!!! checkSome7thGenAccyReads: end of test loop !!!!!!");
    }

    private void checkSome7thGenAccyWrites(int address)  throws ProgrammerException {
        int incoming = 1;
        int num;
        int i;
        lnopsmodeprogrammer = new LnOpsModeProgrammer(memo, address, true);
        lnopsmodeprogrammer.setMode(LnProgrammerManager.LOCONETBD7OPSWMODE);
        log.debug("checkSome7thGenAccyWrites start, address = {}", address);

        LocoNetMessage m;
        for (i = 0; i <8; ++ i) {
            num = 1 << i;
            log.debug("checkSome7thGenAccyWrites iteration {}, num = {}", i, num);
            lnis.clearReceivedMessages();
            assertEquals( 0, lnis.outbound.size(), "Zero messages sent");
            pl = new ProgListenerScaffold();
            assertEquals( 0, pl.getRcvdInvoked(), "no programmingListener reply (yet)");
            log.debug("At begin of loop {}, getRcvdInvolked = {}, Num {}",
                    i, pl.getRcvdInvoked(), num);

            log.debug("--> Sending readCV() from testcase loop {} with CV num {}", i, num);
            lnopsmodeprogrammer.writeCV(Integer.toString(num), (~num) & 255, pl);
            log.debug("   testcase continuing...  After the CV Read accesss started, getRcvdInvolked = {}",
                    pl.getRcvdInvoked());
            // should have sent the CV access to Addr 1 CV num (i)
            assertEquals( 1, lnis.outbound.size(), "outbound size (i="+Integer.toString(i)+") ");
            log.debug("  testcase checks lnis.outbound.size() as 1 and was ok.");
            assertEquals( 0, pl.getRcvdInvoked(), "no programmingListener reply (yet)");

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

            assertEquals( "ED 0B 7F 54 "+evExtraBits+" " + sa2 + " " + sa1 +" 6C "+snum+" "+ expectVal+" 00",
                lnis.outbound.get(0).toString(), "sent");
            // check echo of sent message has no effect
            log.debug("   testcase got access request message from JMRI's LocoNet transmit");
            m = lnis.outbound.get(0);
            log.debug("   checkSome7thGenAccyWrites: testcase copies access request to IN as an echo");
            lnis.sendTestMessage(m);  // (LocoNet echo of transmitted message!)
            log.debug("   testcase has echoed CV access request");

            assertEquals( 1, lnis.outbound.size(), "Still 1 message sent");
            assertEquals( 0, pl.getRcvdInvoked(), "Still 0 programming replies");
            incoming++;

            // wait a little for command station response
            JUnitUtil.waitFor(1);

            assertEquals( 1, lnis.outbound.size(), "saw 1 message sent");
            // receive a LACK "accepted" from "command station"
            m = new LocoNetMessage(new int[]{0xB4, 0x6d, 0x7f, 0x64});

            log.debug("   checkSome7thGenAccyWrites: testcase is sending c.s.'s 'long_ack' as {}", m.toString());
            lnis.sendTestMessage(m);  // (Command station default response)

            assertEquals( 1, lnis.outbound.size(), "saw 1 message sent");

            // make sure the "accepted blind" was received.
            assertNotNull( pl, "PL is not null");
            final int j= incoming;
            log.debug("   testcase getRcvdInvolked = {}; j = incoming = {}.",
                    pl.getRcvdInvoked(), Integer.toString(j));
            JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 0;},"getRcvdInvoked not set after CS long_ack");
            assertEquals( -1, pl.getRcvdStatus(), "Still Reply status not ready");

            // Now "receive" the reply from the 7th-gen Accy device

            log.debug("   checkSome7thGenAccyWrites testcase device opc_long_ack reply being sent: "+m.toString()+
                    "; j = "+Integer.toString(j)+" before sent.");

            // wait a while for the device to reply
            JUnitUtil.waitFor(1);

            // send the reply from the device
            m = getLnLongAckFromVal(0x5A);
            lnis.sendTestMessage(m); // (Device sends the message on LocoNet)

            log.debug("   testcase device opc_long_ack reply was sent.");
            log.debug("   testcase: after send, pl.getRcvdInvoked() is {}  for j = {}, num= {}.",
                    Integer.toString(pl.getRcvdInvoked()), j, num);
            JUnitUtil.waitFor(()->{return pl.getRcvdInvoked() == 1;},"getRcvdInvoked not set (#2)");
            assertEquals( 1, lnis.outbound.size(), "saw 1 message sent");
            assertEquals( 0, pl.getRcvdStatus(), "Still Reply status OK");
            // figure result  expectationres
            int result = ((num & 1)==1)?0:1;
            log.debug("checkSome7thGenAccyWrites write value was {}, result expected is {}", num, result);

            assertEquals( result, pl.getRcvdValue(), "checkSome7thGenAccyWrites Expected result of "+Integer.toString(result));
            JUnitUtil.waitFor(1);

            log.debug("!! checkSome7thGenAccyWrites: end of test loop !!");
        }
        log.debug("end of checkSome7thGenAccyWrites");
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
        lnopsmodeprogrammer.dispose();
        memo.dispose();
        lnis = null;

        programmer = lnopsmodeprogrammer = null;
        JUnitUtil.tearDown();
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LnOpsModeProgrammerTest.class);

}
