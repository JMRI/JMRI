package jmri.jmrix.marklin;

import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for MarklinTurnout.
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase  {

    private MarklinSystemConnectionMemo memo;
    private MarklinTrafficControlScaffold tc;

    @Override
    public int numListeners() {
        return 0; // no background Marklin listeners at present
    }

    @Override
    public void checkThrownMsgSent() {
        Assertions.assertNotNull(tc);
        MarklinMessage m = tc.getLastMessageSent();
        Assertions.assertNotNull(m);
        Assertions.assertEquals("00 16 03 00 06 00 00 38 00 00 01 00 00", m.toString());
    }

    @Override
    public void checkClosedMsgSent() throws InterruptedException {
        Assertions.assertNotNull(tc);
        MarklinMessage m = tc.getLastMessageSent();
        Assertions.assertNotNull(m);
        Assertions.assertEquals("00 16 03 00 06 00 00 38 00 01 01 00 00", m.toString());
    }

    @Test
    public void testMm2Reply() {
        // Create an explicit MM turnout to test MM address matching
        MarklinTurnout mmTurnout = new MarklinTurnout("MCTMM1", "MC", tc);

        mmTurnout.message(MarklinMessage.setLocoSpeedSteps(1, 28)); // nothing happens

        mmTurnout.setCommandedState(Turnout.CLOSED);
        MarklinReply r = new MarklinReply(
                jmri.jmrix.marklin.mcan.cs2.Cs2AccessoryCommands.buildAccessoryReply(
                        MarklinConstants.DCCACCSTART, 0, true));
        mmTurnout.reply(r); // nothing happens, wrong address
        Assertions.assertEquals(Turnout.CLOSED, mmTurnout.getCommandedState());

        r = new MarklinReply(
                jmri.jmrix.marklin.mcan.cs2.Cs2AccessoryCommands.buildAccessoryReply(
                        MarklinConstants.MM1ACCSTART, 0, true));
        mmTurnout.reply(r);
        Assertions.assertEquals(Turnout.THROWN, mmTurnout.getCommandedState());

        r = new MarklinReply(
                jmri.jmrix.marklin.mcan.cs2.Cs2AccessoryCommands.buildAccessoryReply(
                        MarklinConstants.MM1ACCSTART, 0x01, true));
        mmTurnout.reply(r);
        Assertions.assertEquals(Turnout.CLOSED, mmTurnout.getCommandedState());

        mmTurnout.dispose();
    }

    @Test
    public void testDccReply() {
        t.setCommandedState(Turnout.CLOSED);

        // Build a CS2-fingerprinted accessory reply frame for the DCC wire address.
        int dccAddr = 1 + MarklinConstants.DCCACCSTART - 1;
        int[] frame = new int[13];
        frame[0] = 0x00;
        frame[1] = 0x17; // command 0x0B, response=true
        frame[2] = 0x03; // hash high (CS2 fingerprint: (0x0300 >> 7) & 7 = 6)
        frame[3] = 0x00; // hash low
        frame[4] = 0x06; // DLC=6
        frame[5] = (dccAddr >> 24) & 0xFF;
        frame[6] = (dccAddr >> 16) & 0xFF;
        frame[7] = (dccAddr >>  8) & 0xFF;
        frame[8] =  dccAddr        & 0xFF;
        frame[9] = 0x00; // aspect=0 (thrown)
        frame[10] = 0x01; // power=1

        MarklinReply r = new MarklinReply(frame);
        ((MarklinTurnout)t).reply(r);
        Assertions.assertEquals(Turnout.THROWN, t.getCommandedState());

        // Second reply: aspect=1 (closed)
        frame[9] = 0x01;
        r = new MarklinReply(frame);
        ((MarklinTurnout)t).reply(r);
        Assertions.assertEquals(Turnout.CLOSED, t.getCommandedState());

        t.dispose();
    }

    @Test
    public void testCanReply() {
        MarklinTurnout canTurnout = new MarklinTurnout("MCTCAN200", "MC", tc);

        canTurnout.setCommandedState(Turnout.CLOSED);

        int canAddr = 200;
        int[] frame = new int[13];
        frame[0] = 0x00;
        frame[1] = 0x17; // command 0x0B, response=true
        frame[2] = 0x03; // hash high
        frame[3] = 0x00; // hash low
        frame[4] = 0x06; // DLC=6
        frame[5] = (canAddr >> 24) & 0xFF;
        frame[6] = (canAddr >> 16) & 0xFF;
        frame[7] = (canAddr >>  8) & 0xFF;
        frame[8] =  canAddr        & 0xFF;
        frame[9] = 0x00; // aspect=0 (thrown)
        frame[10] = 0x01; // power=1

        MarklinReply r = new MarklinReply(frame);
        canTurnout.reply(r);
        Assertions.assertEquals(Turnout.THROWN, canTurnout.getCommandedState());

        frame[9] = 0x01; // aspect=1 (closed)
        r = new MarklinReply(frame);
        canTurnout.reply(r);
        Assertions.assertEquals(Turnout.CLOSED, canTurnout.getCommandedState());

        canTurnout.dispose();
    }

    @Test
    public void testLegacyUnprefixedIsDcc() {
        // Unprefixed "1" must produce DCC wire address, same as "DCC1"
        MarklinTurnout legacy = new MarklinTurnout("MCT1", "MC", tc);
        MarklinTurnout explicit = new MarklinTurnout("MCTDCC1", "MC", tc);
        Assertions.assertEquals(explicit.getCANAddress(), legacy.getCANAddress());
        legacy.dispose();
        explicit.dispose();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        t = new MarklinTurnout("MCTDCC1", "MC", tc);
    }

    @AfterEach
    @Override
    public void tearDown() {
        Assertions.assertNotNull(memo);
        Assertions.assertNotNull(tc);
        memo.dispose();
        tc.dispose();
        tc = null;
        memo = null;
        t = null;
        JUnitUtil.tearDown();
    }

    // private static final Logger log = LoggerFactory.getLogger(MarklinTurnoutTest.class);

}
