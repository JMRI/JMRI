package jmri.jmrix.acela;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the {@link jmri.jmrix.acela.AcelaTurnout} class.
 *
 * @author	Bob Coleman
 */
public class AcelaTurnoutTest extends jmri.implementation.AbstractTurnoutTest {

    private AcelaTrafficControlScaffold tcis = null;
    private AcelaSystemConnectionMemo memo = null;

    public int numListeners() {
        return tcis.numListeners();
    }

    public void checkClosedMsgSent() {

//        Assert.assertEquals("closed message","52 05 88 00",
//                tcis.outbound.elementAt(tcis.outbound.size()-1).toString());
//	Assert.assertTrue("closed message sent", tcis.outbound.size()>0);
        Assert.assertEquals("CLOSED state", jmri.Turnout.CLOSED, t.getCommandedState());

    }

    public void checkThrownMsgSent() {

//        Assert.assertEquals("thrown message","52 05 89 00",
//                tcis.outbound.elementAt(tcis.outbound.size()-1).toString());
//	Assert.assertTrue("thrown message sent", tcis.outbound.size()>0);
        Assert.assertEquals("THROWN state", jmri.Turnout.THROWN, t.getCommandedState());
    }

    public void checkIncoming() {
        // notify the object that somebody else changed it...
        AcelaReply m = new AcelaReply();
        m.setElement(0, 0x42);
        m.setElement(1, 0x05);
        m.setElement(2, 0x04);     // set CLOSED
        m.setElement(3, 0x43);
        tcis.sendTestMessage(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

        m = new AcelaReply();
        m.setElement(0, 0x42);
        m.setElement(1, 0x05);
        m.setElement(2, 0x08);     // set THROWN
        m.setElement(3, 0x4F);
        tcis.sendTestMessage(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.THROWN);
    }

    // AcelaTurnout test for incoming status message
    public void testAcelaTurnoutStatusMsg() {
        // prepare an interface
        // set closed
        try {
            t.setCommandedState(jmri.Turnout.CLOSED);
        } catch (Exception e) {
            log.error("TO exception: " + e);
        }
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

        // notify that somebody else changed it...
        AcelaReply m = new AcelaReply();
        m.setElement(0, 0x42);
        m.setElement(1, 0x05);
        m.setElement(2, 0x04);     // set CLOSED
        m.setElement(3, 0x43);
        tcis.sendTestMessage(m);
        Assert.assertTrue(t.getCommandedState() == jmri.Turnout.CLOSED);

    }

    // from here down is testing infrastructure
    public AcelaTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", AcelaTurnoutTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(AcelaTurnoutTest.class);
        return suite;
    }

    AcelaNode a0, a1, a2, a3;

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

        tcis = new AcelaTrafficControlScaffold();
        memo = new AcelaSystemConnectionMemo(tcis);

        // We need to delete the nodes so we can re-allocate them
        // otherwise we get another set of nodes for each test case
        // which really messes up the addresses.
        // We also seem to need to explicitly init each node.
        if (tcis.getNumNodes() > 0) {
            //    tcis.deleteNode(3);
            //    tcis.deleteNode(2);
            //    tcis.deleteNode(1);
            //    tcis.deleteNode(0);
            tcis.resetStartingAddresses();
        }
        if (tcis.getNumNodes() <= 0) {
            a0 = new AcelaNode(0, AcelaNode.AC,tcis);
            a0.initNode();
            a1 = new AcelaNode(1, AcelaNode.TB,tcis);
            a1.initNode();
            a2 = new AcelaNode(2, AcelaNode.D8,tcis);
            a2.initNode();
            a3 = new AcelaNode(3, AcelaNode.SY,tcis);
            a3.initNode();
        } else {
            a0 = (AcelaNode) (tcis.getNode(0));
            tcis.initializeAcelaNode(a0);
            a1 = (AcelaNode) (tcis.getNode(1));
            tcis.initializeAcelaNode(a1);
            a2 = (AcelaNode) (tcis.getNode(2));
            tcis.initializeAcelaNode(a2);
            a3 = (AcelaNode) (tcis.getNode(3));
            tcis.initializeAcelaNode(a3);
        }

        // Must allocate a valid turnout t for abstract tests
        t = new AcelaTurnout("AT11",memo);
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(AcelaTurnoutTest.class.getName());

}
