package jmri.jmrix.oaktree;

import jmri.implementation.AbstractTurnoutTestBase;
import org.junit.After;
import org.junit.Before;

/**
 * Tests for the jmri.jmrix.oaktree.SerialTurnout class
 *
 * @author	Bob Jacobsen
  */
public class SerialTurnoutTest extends AbstractTurnoutTestBase {

    private SerialTrafficControlScaffold tcis = null;
    private OakTreeSystemConnectionMemo _memo = null;

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {
        //    Assert.assertTrue("message sent", tcis.outbound.size()>0);
        //    Assert.assertEquals("content", "41 54 08", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
        //    Assert.assertTrue("message sent", tcis.outbound.size()>0);
        //    Assert.assertEquals("content", "41 54 00", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
    }

    @Before
    @Override
    public void setUp() {
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        _memo = new OakTreeSystemConnectionMemo("O", "Oaktree");
        _memo.setTrafficController(tcis);
        SerialNode b = new SerialNode(1, SerialNode.IO48,_memo);
        t = new SerialTurnout("OT0104", "t4", _memo);
    }

    // OK to used this for class clean up?
    @After
    public void tearDown() {
        tcis = null;
        _memo.dispose();
        t = null;
        // JUnitUtil.tearDown() clean up is done through the AbstractTurnoutTestBase
    }

}
