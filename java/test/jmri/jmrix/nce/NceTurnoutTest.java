package jmri.jmrix.nce;

import jmri.Turnout;
import jmri.implementation.AbstractTurnoutTestBase;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

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
        jmri.util.JUnitUtil.setUp();
        // prepare an interface
        tcis = new NceTrafficControlScaffold();

        t = new NceTurnout(tcis, "NT", 4);
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
    }

    @Test
    @SuppressWarnings("all") // suppressing "Comparing identical expressions" for this test only as we want to do runtime test
    public void testLockCoding() {
        Assert.assertTrue(Turnout.CABLOCKOUT != Turnout.PUSHBUTTONLOCKOUT);
        
        // test for proper bit coding, needed because CABLOCKOUT | PUSHBUTTONLOCKOUT is used for "both"
        Assert.assertTrue( (Turnout.CABLOCKOUT & Turnout.PUSHBUTTONLOCKOUT) == 0);
    }

    @Test
    public void testCanLockModes() {
        // prepare an interface
        tcis = new NceTrafficControlScaffold() {
            @Override
            public int getUsbSystem() { return NceTrafficController.USB_SYSTEM_NONE; }
        };

        Turnout t1 = new NceTurnout(tcis, "NT", 4);
        
        // by default, none
        Assert.assertTrue( ! t1.canLock(Turnout.PUSHBUTTONLOCKOUT));
        Assert.assertTrue( ! t1.canLock(Turnout.CABLOCKOUT));
        Assert.assertTrue( ! t1.canLock(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT));
        
        t1.setFeedbackMode(Turnout.MONITORING);

        // with MONITORING, just CABLOCKOUT
        Assert.assertTrue( ! t1.canLock(Turnout.PUSHBUTTONLOCKOUT));
        Assert.assertTrue( t1.canLock(Turnout.CABLOCKOUT));
        Assert.assertTrue( t1.canLock(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT));
        
        // add a decoder
        t1.setDecoderName(t1.getValidDecoderNames()[1]);  // [0] is the "unknown" NONE entry
        Assert.assertTrue( t1.canLock(Turnout.PUSHBUTTONLOCKOUT));
        Assert.assertTrue( t1.canLock(Turnout.CABLOCKOUT));
        Assert.assertTrue( t1.canLock(Turnout.CABLOCKOUT | Turnout.PUSHBUTTONLOCKOUT));
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
