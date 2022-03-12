package jmri.jmrix.ztc.ztc611;

import jmri.Turnout;
import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        t.setFeedbackMode(jmri.Turnout.DIRECT);
        // set closed
        try {
            t.setCommandedState(jmri.Turnout.CLOSED);
        } catch (Exception e) {
            log.error("TO exception: {}", e);
        }

        Assert.assertEquals(Turnout.CLOSED, t.getCommandedState());

        Assert.assertEquals("on message sent", "52 05 88 DF",
                lnis.outbound.elementAt(lnis.outbound.size() - 1).toString());

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
        Assert.assertEquals(Turnout.CLOSED, t.getKnownState());
    }

    @Override
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
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

    private final static Logger log = LoggerFactory.getLogger(ZTC611XNetTurnoutTest.class);

}
