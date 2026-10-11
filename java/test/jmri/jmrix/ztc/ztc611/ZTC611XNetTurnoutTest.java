package jmri.jmrix.ztc.ztc611;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.Turnout;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the {@link jmri.jmrix.ztc.ztc611.ZTC611XNetTurnout} class.
 *
 * @author Bob Jacobsen
 */
public class ZTC611XNetTurnoutTest extends jmri.jmrix.lenz.XNetTurnoutTest{

    @Override
    protected void checkClosedOffSent() {
        // We do not send off messages to the ZTC 611
    }

    @Override
    protected void checkThrownOffSent() {
        // We do not send off messages to the ZTC 611
    }

    // Test the XNetTurnout message sequence.
    @Test
    @Override
    public void testXNetTurnoutMsgSequence() {
        t.setFeedbackMode( Turnout.DIRECT);
        // set closed
        assertDoesNotThrow( () -> t.setCommandedState( Turnout.CLOSED),
            "TO exception: ");

        assertEquals(Turnout.CLOSED, t.getCommandedState());

        assertEquals( "52 05 88 DF",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString(),
                "on message sent");

        ((ZTC611XNetTurnout)t).message(lnis.outbound.elementAt(lnis.outbound.size() - 1));

        // notify that the command station received the reply
        XNetReply m = new XNetReply();
        m.setElement(0, 0x42);
        m.setElement(1, 0x05);
        m.setElement(2, 0x04);     // set CLOSED
        m.setElement(3, 0x43);

        ((jmri.jmrix.lenz.XNetTurnout) t).message(m);

        // no wait here.  The last reply should cause the turnout to
        // set it's state, but it will not cause another reply.
        assertEquals(Turnout.CLOSED, t.getKnownState());
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        lnis = new XNetInterfaceScaffold(new LenzCommandStation());

        t = new ZTC611XNetTurnout("XT", 21, lnis);
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        t = null;
        lnis.terminateThreads();
        lnis = null;
        JUnitUtil.tearDown();

    }

    // private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ZTC611XNetTurnoutTest.class);

}
