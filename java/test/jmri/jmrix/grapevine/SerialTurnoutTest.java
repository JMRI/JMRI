package jmri.jmrix.grapevine;

import jmri.implementation.AbstractTurnoutTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.grapevine.SerialTurnout class, low address.
 *
 * @author	Bob Jacobsen
 */
public class SerialTurnoutTest extends AbstractTurnoutTestBase {

    private GrapevineSystemConnectionMemo memo = null; 
    private SerialTrafficControlScaffold tcis = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();

        // prepare an interface
        memo = new GrapevineSystemConnectionMemo();
        tcis = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tcis); // important for successful getTrafficController()
        tcis.registerNode(new SerialNode(1, SerialNode.NODE2002V6, tcis));

        t = new SerialTurnout("GT1104", "t4", memo);
    }

    @Override
    public int numListeners() {
        //log.debug("Grapevine tcis numlisteners = {}", tcis.numListeners());
        return tcis.numListeners();
    }

    @Override
    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "81 18 81 0C", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // CLOSED message
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "81 1E 81 00", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // THROWN message
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

    //private final static Logger log = LoggerFactory.getLogger(SerialTurnoutTest.class);

}
