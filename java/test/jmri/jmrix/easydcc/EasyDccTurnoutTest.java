package jmri.jmrix.easydcc;

import jmri.implementation.AbstractTurnoutTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the jmri.jmrix.easydcc.EasyDccTurnout class
 *
 * @author Bob Jacobsen
 */
public class EasyDccTurnoutTest extends AbstractTurnoutTestBase {

    private EasyDccTrafficControlScaffold tcis = null;
    private EasyDccSystemConnectionMemo memo = null;

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        memo = new EasyDccSystemConnectionMemo("E", "EasyDCC Test");
        tcis = new EasyDccTrafficControlScaffold(memo);
        memo.setEasyDccTrafficController(tcis); // important for successful getTrafficController()

        t = new EasyDccTurnout("E", 4, memo);
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content",
                "S 02 81 FE 7F", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content",
                "S 02 81 FF 7E", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // CLOSED message
    }

    // reset objects
    @Override
    @AfterEach
    public void tearDown() {
        tcis.terminateThreads();
        t.dispose();
        JUnitUtil.tearDown();
    }

}
