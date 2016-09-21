package jmri.jmrix.maple;

import jmri.implementation.AbstractTurnoutTest;
import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.maple.SerialTurnout class
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
public class SerialTurnoutTest extends AbstractTurnoutTest {

    private SerialTrafficControlScaffold tcis = null;
    private SerialNode n = new SerialNode();

    public void setUp() {
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        n = new SerialNode(1, 0);
        t = new SerialTurnout("KT4", "t4");
        Assert.assertNotNull("exists", n);
        Assert.assertNotNull("turnout exists", t);
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
