package jmri.jmrix.grapevine;

import jmri.implementation.AbstractTurnoutTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Tests for the jmri.jmrix.grapevine.SerialTurnout class, middle bank.
 *
 * @author	Bob Jacobsen
 */
public class SerialTurnoutTest1 extends AbstractTurnoutTestBase {

    private GrapevineSystemConnectionMemo memo = null; 
    private SerialTrafficControlScaffold tcis = null;

    @Before
    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();

        // prepare an interface
        memo = new GrapevineSystemConnectionMemo();
        //runtime check
        Assert.assertEquals("G", memo.getSystemPrefix());
        tcis = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tcis);
        tcis.registerNode(new SerialNode(1, SerialNode.NODE2002V6, tcis));

        t = new SerialTurnout("GT1304", "t4", memo);
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }

    @Override
    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "81 18 81 2A", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // CLOSED message
    }
    
    @Override
    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "81 1E 81 2E", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // THROWN message
    }

    // reset objects
    @After
    public void tearDown() {
        tcis.terminateThreads();
        tcis = null;
        memo = null;
        t.dispose();
        JUnitUtil.tearDown();
    }

}
