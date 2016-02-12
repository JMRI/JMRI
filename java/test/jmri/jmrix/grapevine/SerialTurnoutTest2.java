// SerialTurnoutTest2.java
package jmri.jmrix.grapevine;

import jmri.implementation.AbstractTurnoutTest;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the jmri.jmrix.grapevine.SerialTurnout class, high part of 24 port
 * card.
 *
 * @author	Bob Jacobsen
 * @version	$Revision$
 */
public class SerialTurnoutTest2 extends AbstractTurnoutTest {

    private SerialTrafficControlScaffold tcis = null;

    public void setUp() {
        // prepare an interface
        tcis = new SerialTrafficControlScaffold();
        tcis.registerNode(new SerialNode(1, SerialNode.NODE2002V6));

        t = new SerialTurnout("GT1116", "t4");
    }

    public int numListeners() {
        return tcis.numListeners();
    }

    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "81 7A 81 1B 81 18 81 0C", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // CLOSED message
    }

    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "81 7A 81 1B 81 1E 81 00", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // THROWN message
    }

    // from here down is testing infrastructure
    public SerialTurnoutTest2(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SerialTurnoutTest2.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SerialTurnoutTest2.class);
        return suite;
    }

    private final static Logger log = LoggerFactory.getLogger(SerialTurnoutTest2.class.getName());

}
