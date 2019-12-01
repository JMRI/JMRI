package jmri.jmrix.lenz.hornbyelite;

import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.Turnout;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the {@link jmri.jmrix.lenz.hornbyelite.EliteXNetTurnout} class.
 *
 * @author	Bob Jacobsen
 */
public class EliteXNetTurnoutTest extends jmri.jmrix.lenz.XNetTurnoutTest{

    @Override
    public void checkClosedMsgSent() {
        Assert.assertEquals("closed message", "52 05 8A DD",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertEquals("thrown message", "52 05 8B DC",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());
    }

    // Test the XNetTurnout message sequence.
    @Test
    @Override
    public void testXNetTurnoutMsgSequence() {
        t.setFeedbackMode(jmri.Turnout.DIRECT);
        // set closed
        try {
            t.setCommandedState(jmri.Turnout.CLOSED);
        } catch (Exception e) {
            log.error("TO exception: " + e);
        }

        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

        Assert.assertEquals("on message sent", "52 05 8A DD",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

        // notify that the command station received the reply
        XNetReply m = new XNetReply();
        m.setElement(0, 0x42);
        m.setElement(1, 0x05);
        m.setElement(2, 0x04);     // set CLOSED
        m.setElement(3, 0x43);

        ((EliteXNetTurnout) t).message(m);

        // no wait here.  The last reply should cause the turnout to
        // set it's state, but it will not cause another reply.
        Assert.assertTrue(t.getKnownState() == jmri.Turnout.CLOSED);
    }

    @Test
    @Override
    public void checkIncoming() {
        t.setFeedbackMode(Turnout.MONITORING);
        jmri.util.JUnitUtil.waitFor(() -> {
            return t.getFeedbackMode() == Turnout.MONITORING;
        }, "Feedback mode set");

	    listenStatus = Turnout.UNKNOWN;
	    t.addPropertyChangeListener(new Listen());

        // notify the object that somebody else changed it...
        XNetReply m = new XNetReply("42 05 04 43"); // set CLOSED
        ((EliteXNetTurnout) t).message(m);
        jmri.util.JUnitUtil.waitFor(() -> {
            return listenStatus != Turnout.UNKNOWN;
        }, "Turnout state changed");
        Assert.assertEquals("state after CLOSED message",Turnout.CLOSED,t.getKnownState());

	    listenStatus = Turnout.UNKNOWN;

        m = new XNetReply("42 05 08 4F"); // set THROWN
        ((EliteXNetTurnout) t).message(m);
        jmri.util.JUnitUtil.waitFor(() -> {
            return listenStatus != Turnout.UNKNOWN;
        }, "Turnout state changed");
        Assert.assertEquals("state after THROWN message",Turnout.THROWN,t.getKnownState());
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        lnis = new XNetInterfaceScaffold(new HornbyEliteCommandStation());

        t = new EliteXNetTurnout("X", 21, lnis);
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @Override
    @After
    public void tearDown() {
	    JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(EliteXNetTurnoutTest.class);

}
