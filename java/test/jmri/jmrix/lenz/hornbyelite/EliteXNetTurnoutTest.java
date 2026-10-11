package jmri.jmrix.lenz.hornbyelite;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.Turnout;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the {@link jmri.jmrix.lenz.hornbyelite.EliteXNetTurnout} class.
 *
 * @author Bob Jacobsen
 */
public class EliteXNetTurnoutTest extends jmri.jmrix.lenz.XNetTurnoutTest {

    @Override
    public void checkClosedMsgSent() {
        assertEquals( "52 05 8A DD",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "closed message");
    }

    @Override
    public void checkThrownMsgSent() {
        assertEquals( "52 05 8B DC",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "thrown message");
    }

    @Override
    protected void checkClosedOffSent() {
        // We do not send off messages to the Elite
    }

    @Override
    protected void checkThrownOffSent() {
        // We do not send off messages to the Elite
    }

    // Test the XNetTurnout message sequence.
    @Test
    @Override
    public void testXNetTurnoutMsgSequence() {
        t.setFeedbackMode(jmri.Turnout.DIRECT);
        // set closed
        assertDoesNotThrow( () -> t.setCommandedState( Turnout.CLOSED ),
            "TO exception: {}");

        assertEquals(Turnout.CLOSED, t.getCommandedState());

        assertEquals( "52 05 8A DD",
            lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
            "on message sent");

        // notify that the command station received the reply
        XNetReply m = new XNetReply();
        m.setElement(0, 0x42);
        m.setElement(1, 0x05);
        m.setElement(2, 0x04);     // set CLOSED
        m.setElement(3, 0x43);

        ((EliteXNetTurnout) t).message(m);

        // no wait here.  The last reply should cause the turnout to
        // set it's state, but it will not cause another reply.
        assertEquals(Turnout.CLOSED, t.getKnownState());
    }

    @Test
    @Override
    public void checkIncoming() {
        t.setFeedbackMode(Turnout.MONITORING);
        JUnitUtil.waitFor(() -> t.getFeedbackMode() == Turnout.MONITORING, "Feedback mode set");

        listenStatus = Turnout.UNKNOWN;
        t.addPropertyChangeListener(new Listen());

        // notify the object that somebody else changed it...
        XNetReply m = new XNetReply("42 05 04 43"); // set CLOSED
        ((EliteXNetTurnout) t).message(m);
        JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        assertEquals( Turnout.CLOSED, t.getKnownState(), "state after CLOSED message");

        listenStatus = Turnout.UNKNOWN;

        m = new XNetReply("42 05 08 4F"); // set THROWN
        ((EliteXNetTurnout) t).message(m);
        JUnitUtil.waitFor(() -> listenStatus != Turnout.UNKNOWN, "Turnout state changed");
        assertEquals( Turnout.THROWN, t.getKnownState(), "state after THROWN message");
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        lnis = new XNetInterfaceScaffold(new HornbyEliteCommandStation());

        t = new EliteXNetTurnout("X", 21, lnis);
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @Override
    @AfterEach
    public void tearDown() {
        lnis.terminateThreads();
        JUnitUtil.tearDown();
    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EliteXNetTurnoutTest.class);

}
