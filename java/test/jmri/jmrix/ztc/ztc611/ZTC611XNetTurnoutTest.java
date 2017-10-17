package jmri.jmrix.ztc.ztc611;

import jmri.jmrix.lenz.LenzCommandStation;
import jmri.jmrix.lenz.XNetInterfaceScaffold;
import jmri.jmrix.lenz.XNetReply;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the {@link jmri.jmrix.ztc.ztc611.ZTC611XNetTurnout} class.
 *
 * @author	Bob Jacobsen
 */
public class ZTC611XNetTurnoutTest extends jmri.jmrix.lenz.XNetTurnoutTest{

    // Test the XNetTurnout message sequence.
    @Test
    @Override
    @Ignore("causes a hang, probably incorrect sequence in test")
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

        int n = lnis.outbound.size();

        ((jmri.jmrix.lenz.XNetTurnout) t).message(m);

        while (n == lnis.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.
        Assert.assertEquals("off message sent", "52 05 80 D7",
                lnis.outbound.elementAt(n).toString());

        // the turnout will not set its state until it sees an OK message.
        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        n = lnis.outbound.size();

        ((jmri.jmrix.lenz.XNetTurnout) t).message(m);

        while (n == lnis.outbound.size()) {
        } // busy loop.  Wait for
        // outbound size to change.

        Assert.assertEquals("off message sent", "52 05 80 D7",
                lnis.outbound.elementAt(n).toString());

        m = new XNetReply();
        m.setElement(0, 0x01);
        m.setElement(1, 0x04);
        m.setElement(2, 0x05);

        ((jmri.jmrix.lenz.XNetTurnout) t).message(m);

        // no wait here.  The last reply should cause the turnout to
        // set it's state, but it will not cause another reply.
        Assert.assertTrue(t.getKnownState() == jmri.Turnout.CLOSED);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        lnis = new XNetInterfaceScaffold(new LenzCommandStation());

        t = new ZTC611XNetTurnout("XT", 21, lnis);
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(ZTC611XNetTurnoutTest.class);

}
