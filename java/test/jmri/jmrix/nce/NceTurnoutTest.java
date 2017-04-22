package jmri.jmrix.nce;

import jmri.implementation.AbstractTurnoutTestBase;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for the jmri.jmrix.nce.NceTurnout class
 *
 * @author	Bob Jacobsen
  */
public class NceTurnoutTest extends AbstractTurnoutTestBase {

    private NceTrafficControlScaffold tcis = null;

    @Before
    @Override
    public void setUp() {
        // prepare an interface
        tcis = new NceTrafficControlScaffold();

        t = new NceTurnout(tcis, "NT", 4);
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        // 2004 eprom output:
        // Assert.assertEquals("content", "93 02 81 FE 7F", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
        Assert.assertEquals("content", "AD 00 04 04 00", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        // 2004 eprom output:
        //Assert.assertEquals("content", "93 02 81 FF 7E", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
        Assert.assertEquals("content", "AD 00 04 03 00", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // CLOSED message
    }

}
