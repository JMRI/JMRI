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
        Assertions.assertEquals("00 16 47 11 06 00 00 30 00 00 01 00 00", m.toString());
    }

    @Override
    public void checkClosedMsgSent() throws InterruptedException {
        Assertions.assertNotNull(tc);
        MarklinMessage m = tc.getLastMessageSent();
        Assertions.assertNotNull(m);
        Assertions.assertEquals("00 16 47 11 06 00 00 30 00 01 01 00 00", m.toString());
    }

    @Test
    public void testMm2Reply() {

        ((MarklinTurnout)t).message(MarklinMessage.setLocoSpeedSteps(1, 28)); // nothing happens

        t.setCommandedState(Turnout.CLOSED);
        MarklinReply r = new MarklinReply();
        r.setCommand(MarklinConstants.ACCCOMMANDSTART);
        ((MarklinTurnout)t).reply(r); // nothing happens, unknown address

        r.setAddress( 1 + MarklinConstants.MM1ACCSTART - 1); // toNum + Constant -1
        r.setElement(9, 0); // thrown
        ((MarklinTurnout)t).reply(r);
        Assertions.assertEquals(Turnout.THROWN, t.getCommandedState());

        r.setElement(9, 0x01); // closed
        ((MarklinTurnout)t).reply(r);
        Assertions.assertEquals(Turnout.CLOSED, t.getCommandedState());

        t.dispose();
    }

    @Test
    public void testDccReply() {

        t.setCommandedState(Turnout.CLOSED);
        MarklinReply r = new MarklinReply();
        r.setCommand(MarklinConstants.ACCCOMMANDSTART);
        ((MarklinTurnout)t).reply(r); // nothing happens, unknown address

        r.setAddress( 1 + MarklinConstants.DCCACCSTART - 1); // toNum + Constant -1
        r.setElement(9, 0); // thrown
        ((MarklinTurnout)t).reply(r);
        Assertions.assertEquals(Turnout.THROWN, t.getCommandedState());

        r.setElement(9, 0x01); // closed
        ((MarklinTurnout)t).reply(r);
        Assertions.assertEquals(Turnout.CLOSED, t.getCommandedState());

        t.dispose();
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        t = new MarklinTurnout(1,"MC", tc);
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

    // private final static Logger log = LoggerFactory.getLogger(MarklinTurnoutTest.class);

}
