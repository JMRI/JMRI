package jmri.jmrix.tmcc;

import jmri.implementation.AbstractTurnoutTestBase;
import org.junit.Before;

/**
 * Tests for the SerialTurnout class
 *
 * @author	Bob Jacobsen
  */
public class SerialTurnoutTest extends AbstractTurnoutTestBase {

    private SerialTrafficControlScaffold tcis = null;

    @Before
    @Override
    public void setUp() {
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();

        t = new SerialTurnout(4);
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }

    @Override
    public void checkThrownMsgSent() {
//       tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//       Assert.assertTrue("message sent", tcis.outbound.size()>0);
//       Assert.assertEquals("content", "41 54 08", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
    }

    @Override
    public void checkClosedMsgSent() {
//       tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//       Assert.assertTrue("message sent", tcis.outbound.size()>0);
//       Assert.assertEquals("content", "41 54 00", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
    }

}
