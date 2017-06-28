package jmri.jmrix.maple;

import jmri.implementation.AbstractTurnoutTestBase;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for the jmri.jmrix.maple.SerialTurnout class
 *
 * @author	Bob Jacobsen
  */
public class SerialTurnoutTest extends AbstractTurnoutTestBase {

    private SerialTrafficControlScaffold tcis = null;
    private SerialNode n = new SerialNode();

    @Override
    @Before
    public void setUp() {
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        n = new SerialNode(1, 0);
        t = new SerialTurnout("KT4", "t4");
        Assert.assertNotNull("exists", n);
        Assert.assertNotNull("turnout exists", t);
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {

//                tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//		Assert.assertTrue("message sent", tcis.outbound.size()>0);
//		Assert.assertEquals("content", "41 54 08", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
//                tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//		Assert.assertTrue("message sent", tcis.outbound.size()>0);
//		Assert.assertEquals("content", "41 54 00", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
    }
}
