// SerialTurnoutTest.java
package jmri.jmrix.grapevine;

import jmri.implementation.AbstractTurnoutTest;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.grapevine.SerialTurnout class, low address.
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
public class SerialTurnoutTest extends AbstractTurnoutTest {

    private SerialTrafficControlScaffold tcis = null;

    public void setUp() {
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        tcis.registerNode(new SerialNode(1, SerialNode.NODE2002V6));

        t = new SerialTurnout("GT1104", "t4");
    }

    public int numListeners() {
        return tcis.numListeners();
    }

    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "81 18 81 0C", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // CLOSED message
    }

    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "81 1E 81 00", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // THROWN message
    }

    // from here down is testing infrastructure
    public SerialTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialTurnoutTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialTurnoutTest.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutTest.class.getName());

}
