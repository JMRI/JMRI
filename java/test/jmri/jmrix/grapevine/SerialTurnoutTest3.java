// SerialTurnoutTest3.java
package jmri.jmrix.grapevine;

import jmri.implementation.AbstractTurnoutTest;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.grapevine.SerialTurnout class, high card and high
 * port on card
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
public class SerialTurnoutTest3 extends AbstractTurnoutTest {

    private SerialTrafficControlScaffold tcis = null;

    public void setUp() {
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        tcis.registerNode(new SerialNode(1, SerialNode.NODE2002V6));

        t = new SerialTurnout("GT1416", "t4");
    }

    public int numListeners() {
        return tcis.numListeners();
    }

    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "81 7A 81 1B 81 18 81 39", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // CLOSED message
    }

    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "81 7A 81 1B 81 1E 81 3D", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // THROWN message
    }

    // from here down is testing infrastructure
    public SerialTurnoutTest3(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialTurnoutTest3.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialTurnoutTest3.class);
        return suite;
    }

}
