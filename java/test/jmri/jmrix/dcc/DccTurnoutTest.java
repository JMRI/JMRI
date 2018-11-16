package jmri.jmrix.dcc;

import jmri.CommandStation;
import jmri.InstanceManager;
import jmri.implementation.MockCommandStation;
import jmri.implementation.AbstractTurnoutTestBase;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for the jmri.jmrix.dcc.DccTurnout class
 *
 * @author	Bob Jacobsen
 */
public class DccTurnoutTest extends AbstractTurnoutTestBase {

    MockCommandStation tcis;

    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tcis = new MockCommandStation();
        InstanceManager.setDefault(CommandStation.class, tcis);
        t = new DccTurnout(4);
    }

    @Override
    public int numListeners() {
        return 0;
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", tcis.lastPacket != null);
        Assert.assertEquals("content", "[-127, -2, 127]", java.util.Arrays.toString(tcis.lastPacket));  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", tcis.lastPacket != null);
        Assert.assertEquals("content", "[-127, -1, 126]", java.util.Arrays.toString(tcis.lastPacket));  // CLOSED message
    }

    // The minimal setup for log4J
    @After
    public void tearDown() {
        tcis = null;
        t = null;
        JUnitUtil.tearDown();
    }

}
