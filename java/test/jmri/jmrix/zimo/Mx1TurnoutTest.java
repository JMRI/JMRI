package jmri.jmrix.zimo;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class Mx1TurnoutTest extends jmri.implementation.AbstractTurnoutTestBase {

    private Mx1TrafficController tc = null;

    @Test
    public void testCTor() {
        Assertions.assertNotNull( t, "exists");
    }

    @Override
    public int numListeners() {
        return tc.listeners.size();
    }

    @Override
    public void checkThrownMsgSent() {
        Assertions.assertNotNull(lastSent);

        // Note discrepancy in Mx1Turnout forwardCommandChangeToLayout CLOSED / THROWN
        // which has been present since initial class upload.

        Assertions.assertEquals(
            Mx1Message.getSwitchMsg(5, jmri.Turnout.CLOSED, true).toString(),
            lastSent.toString());
        Assertions.assertEquals("0 10 7 80 5 0 ", lastSent.toString());
    }

    @Override
    public void checkClosedMsgSent() {
        Assertions.assertNotNull(lastSent);

        // Note discrepancy in Mx1Turnout forwardCommandChangeToLayout CLOSED / THROWN
        // which has been present since initial class upload.

        Assertions.assertEquals(
            Mx1Message.getSwitchMsg(5, jmri.Turnout.THROWN, true).toString(),
            lastSent.toString());
        Assertions.assertEquals("0 10 7 80 5 4 ", lastSent.toString());
    }

    private Mx1Message lastSent = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        lastSent = null;
        tc = new Mx1TrafficController(){
           @Override
           public boolean status(){
              return true;
           }
           @Override
           public void sendMx1Message(Mx1Message m,Mx1Listener reply) {
               lastSent = m;
           }
        };
        t = new Mx1Turnout(5, tc, "Z");
    }

    @AfterEach
    @Override
    public void tearDown() {
        lastSent = null;
        tc = null;
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(Mx1TurnoutTest.class);

}
