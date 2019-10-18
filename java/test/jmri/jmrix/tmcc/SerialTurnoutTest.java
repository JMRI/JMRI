package jmri.jmrix.tmcc;

import jmri.implementation.AbstractTurnoutTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * Tests for the SerialTurnout class
 *
 * @author Bob Jacobsen
 */
public class SerialTurnoutTest extends AbstractTurnoutTestBase {

    private SerialTrafficControlScaffold tcis = null;
    private TmccSystemConnectionMemo memo = null;

    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface
        memo = new TmccSystemConnectionMemo("T", "TMCC Test");
        tcis = new SerialTrafficControlScaffold(memo);
        memo.setTrafficController(tcis); // important for successful getTrafficController()

        t = new SerialTurnout("T", 4, memo);
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {
//       tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//       Assert.assertTrue("message sent", tcis.outbound.size() > 0);
//       Assert.assertEquals("content", "41 54 08", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // OUT (THROWN) message
    }

    @Override
    public void checkClosedMsgSent() {
//       tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//       Assert.assertTrue("message sent", tcis.outbound.size() > 0);
//       Assert.assertEquals("content", "41 54 00", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // THROUGH (CLOSED) message
    }

    // The minimal setup for log4J
    @After
    public void tearDown() {
        t.dispose();
        t = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();

    }

}
