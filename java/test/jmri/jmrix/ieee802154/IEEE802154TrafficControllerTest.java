package jmri.jmrix.ieee802154;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * IEEE802154TrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.ieee802154.IEEE802154TrafficController
 * class
 *
 * @author	Paul Bender
 */
public class IEEE802154TrafficControllerTest{

    IEEE802154TrafficController m;

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", m);
    }

    @Test
    public void testGetIEEE802154Messge() {
        Assert.assertNull("IEEE802154Message", m.getIEEE802154Message(5));
    }

    @Test
    public void testGetPollReplyHandler() {
        Assert.assertNull("pollReplyHandler", m.pollReplyHandler());
    }

    @Test
    public void checkPollMessageNoNodes() {
        // no nodes, should return null.
        Assert.assertNull("pollMessage", m.pollMessage());
    }

    @Test
    public void checkPollReplyHandler() {
        // always returns null.
        Assert.assertNull("pollReplyHandler", m.pollReplyHandler());
    }

    @Test
    public void checkEnterProgMode() {
        // No Programming Mode, returns null.
        Assert.assertNull("enterProgMode", m.enterProgMode());
    }

    @Test
    public void checkExitProgMode() {
        // No Programming Mode, returns null.
        Assert.assertNull("enterNormalMode", m.enterNormalMode());
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        m = new IEEE802154TrafficController() {
            public void setInstance() {
            }
            protected jmri.jmrix.AbstractMRReply newReply() {
                return null;
            }
            public IEEE802154Node newNode() {
                return null;
            }
        };
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
