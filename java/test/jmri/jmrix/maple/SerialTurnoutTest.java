package jmri.jmrix.maple;

import jmri.implementation.AbstractTurnoutTestBase;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the jmri.jmrix.maple.SerialTurnout class
 *
 * @author	Bob Jacobsen
  */
public class SerialTurnoutTest extends AbstractTurnoutTestBase {

    private SerialTrafficControlScaffold tcis = null;
    private MapleSystemConnectionMemo _memo = null;
    private SerialNode n = null; 

    @Test
    public void testNode() {
        Assert.assertNotNull("exists", n);
        Assert.assertNotNull("turnout exists", t);
    }

    @Test
    public void testCTor() {
        SerialTurnout t = new SerialTurnout("KT1", _memo);
        Assert.assertNotNull("exists", t);
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

    @Override
    @Before
    public void setUp() {
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        _memo = new MapleSystemConnectionMemo("K", "Maple");
        _memo.setTrafficController(tcis);
        n = new SerialNode(1, 0,tcis);
        t = new SerialTurnout("KT4", "t4", _memo);
    }

    // OK to used this for class clean up?
    @After
    public void tearDown() {
        tcis = null;
        _memo.dispose();
        n = null;
        t = null;
        // JUnitUtil.tearDown() clean up is done through the AbstractTurnoutTestBase
    }

}
