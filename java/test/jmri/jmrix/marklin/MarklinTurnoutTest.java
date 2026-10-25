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
        // DCC address 1: wire = DCCACCSTART + 0 = 0x3800, bytes 7-8 = 38 00
        Assertions.assertEquals("00 16 47 11 06 00 00 38 00 00 01 00 00", m.toString());
    }

    @Override
    public void checkClosedMsgSent() throws InterruptedException {
        Assertions.assertNotNull(tc);
        MarklinMessage m = tc.getLastMessageSent();
        Assertions.assertNotNull(m);
        // DCC address 1: wire = DCCACCSTART + 0 = 0x3800, bytes 7-8 = 38 00
        Assertions.assertEquals("00 16 47 11 06 00 00 38 00 01 01 00 00", m.toString());
    }

    @Test
    public void testMm2Reply() {
        // Create an explicit MM turnout to test MM address matching
        MarklinTurnout mmTurnout = new MarklinTurnout("MCTMM1", "MC", tc);

        mmTurnout.message(MarklinMessage.setLocoSpeedSteps(1, 28)); // nothing happens

        mmTurnout.setCommandedState(Turnout.CLOSED);
        MarklinReply r = new MarklinReply();
        r.setCommand(MarklinConstants.ACCCOMMANDSTART);
        mmTurnout.reply(r); // nothing happens, wrong address

        r.setAddress(1 + MarklinConstants.MM1ACCSTART - 1); // MM wire address for address 1
        r.setElement(9, 0); // thrown
        mmTurnout.reply(r);
        Assertions.assertEquals(Turnout.THROWN, mmTurnout.getCommandedState());

        r.setElement(9, 0x01); // closed
        mmTurnout.reply(r);
        Assertions.assertEquals(Turnout.CLOSED, mmTurnout.getCommandedState());

        mmTurnout.dispose();
    }

    @Test
    public void testDccReply() {
        t.setCommandedState(Turnout.CLOSED);
        MarklinReply r = new MarklinReply();
        r.setCommand(MarklinConstants.ACCCOMMANDSTART);
        r.setAddress(999 + MarklinConstants.DCCACCSTART - 1); // wrong address
        ((MarklinTurnout)t).reply(r); // nothing happens

        r.setAddress(1 + MarklinConstants.DCCACCSTART - 1); // DCC wire address for address 1
        r.setElement(9, 0); // thrown
        ((MarklinTurnout)t).reply(r);
        Assertions.assertEquals(Turnout.THROWN, t.getCommandedState());

        r.setElement(9, 0x01); // closed
        ((MarklinTurnout)t).reply(r);
        Assertions.assertEquals(Turnout.CLOSED, t.getCommandedState());

        t.dispose();
    }

    @Test
    public void testCanReply() {
        MarklinTurnout canTurnout = new MarklinTurnout("MCTCAN200", "MC", tc);

        canTurnout.setCommandedState(Turnout.CLOSED);
        MarklinReply r = new MarklinReply();
        r.setCommand(MarklinConstants.ACCCOMMANDSTART);
        r.setAddress(201); // wrong address
        canTurnout.reply(r); // nothing happens

        r.setAddress(200); // CAN wire address is verbatim
        r.setElement(9, 0); // thrown
        canTurnout.reply(r);
        Assertions.assertEquals(Turnout.THROWN, canTurnout.getCommandedState());

        r.setElement(9, 0x01); // closed
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
