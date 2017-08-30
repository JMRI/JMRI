package jmri.jmrix.easydcc;

import jmri.implementation.AbstractTurnoutTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for the jmri.jmrix.nce.EasyDccTurnout class
 *
 * @author	Bob Jacobsen
 */
public class EasyDccTurnoutTest extends AbstractTurnoutTestBase {

    private EasyDccTrafficControlScaffold tcis = null;

    @Before
    @Override
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface
        tcis = new EasyDccTrafficControlScaffold();

        t = new EasyDccTurnout(4);
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "S 02 81 FE 7F", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "S 02 81 FF 7E", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // CLOSED message
    }

    // The minimal setup for log4J
    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
