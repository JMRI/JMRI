package jmri.jmrix.cmri.serial;

import jmri.implementation.AbstractTurnoutTest;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.cmri.serial.SerialTurnout class
 *
 * @author	Bob Jacobsen
 */
public class SerialTurnoutTest extends AbstractTurnoutTest {

    private jmri.jmrix.cmri.CMRISystemConnectionMemo memo = null;
    private SerialTrafficControlScaffold tcis = null;
    private SerialNode n = null;

    public void setUp() {
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        memo = new jmri.jmrix.cmri.CMRISystemConnectionMemo();
        memo.setTrafficController(tcis);
        n = new SerialNode(0, SerialNode.SMINI,tcis);
        t = new SerialTurnout("CT4", "t4",memo);
        Assert.assertNotNull("exists", n);
    }

    public int numListeners() {
        return tcis.numListeners();
    }

    public void checkThrownMsgSent() {

//                tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//		Assert.assertTrue("message sent", tcis.outbound.size()>0);
//		Assert.assertEquals("content", "41 54 08", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // THROWN message
    }

    public void checkClosedMsgSent() {
//                tcis.sendSerialMessage(tcis.nextWrite(), null); // force outbound message; normally done by poll loop
//		Assert.assertTrue("message sent", tcis.outbound.size()>0);
//		Assert.assertEquals("content", "41 54 00", tcis.outbound.elementAt(tcis.outbound.size()-1).toString());  // CLOSED message
    }

    // from here down is testing infrastructure
    public SerialTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SerialTurnoutTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialTurnoutTest.class);
        return suite;
    }

}
