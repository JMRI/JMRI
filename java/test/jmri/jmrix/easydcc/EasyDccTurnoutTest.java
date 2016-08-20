/**
 * EasyDccTurnoutTest.java
 *
 * Description:	tests for the jmri.jmrix.nce.EasyDccTurnout class
 *
 * @author	Bob Jacobsen
 * @version
 */
package jmri.jmrix.easydcc;

import jmri.implementation.AbstractTurnoutTest;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

public class EasyDccTurnoutTest extends AbstractTurnoutTest {

    private EasyDccTrafficControlScaffold tcis = null;

    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        // prepare an interface
        tcis = new EasyDccTrafficControlScaffold();

        t = new EasyDccTurnout(4);
    }

    public int numListeners() {
        return tcis.numListeners();
    }

    public void checkThrownMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "S 02 81 FE 7F", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // THROWN message
    }

    public void checkClosedMsgSent() {
        Assert.assertTrue("message sent", tcis.outbound.size() > 0);
        Assert.assertEquals("content", "S 02 81 FF 7E", tcis.outbound.elementAt(tcis.outbound.size() - 1).toString());  // CLOSED message
    }

    // from here down is testing infrastructure
    public EasyDccTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", EasyDccTurnoutTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(EasyDccTurnoutTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
