package jmri.jmrix.xpa;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Description:	tests for the jmri.jmrix.xpa.XpaTurnout class
 * <P>
 * @author	Paul Bender
 */
public class XpaTurnoutTest extends jmri.implementation.AbstractTurnoutTestBase  {

    private XpaTrafficControlScaffold xnis;
    private XpaSystemConnectionMemo memo = null;

    @Override
    public int numListeners() {
        return xnis.numListeners();
    }

    @Override
    public void checkClosedMsgSent() {
        Assert.assertEquals("closed message", "ATDT#3#3;",
                xnis.outbound.get(xnis.outbound.size() - 1).toString());
        Assert.assertEquals("CLOSED state", jmri.Turnout.CLOSED, t.getCommandedState());
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertEquals("thrown message", "ATDT#3#1;",
                xnis.outbound.get(xnis.outbound.size() - 1).toString());
        Assert.assertEquals("THROWN state", jmri.Turnout.THROWN, t.getCommandedState());
    }

    @Test
    public void testCtor() {
        Assert.assertNotNull(t);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        memo = new XpaSystemConnectionMemo();
        xnis = new XpaTrafficControlScaffold();
        memo.setXpaTrafficController(xnis);
        t = new XpaTurnout(3,memo);
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        memo = null;
        xnis = null;
    }

}
