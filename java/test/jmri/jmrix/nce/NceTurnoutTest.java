/**
 * NceTurnoutTest.java
 *
 * Description:	tests for the jmri.jmrix.nce.NceTurnout class
 *
 * @author	Bob Jacobsen
 */
package jmri.jmrix.nce;

import apps.tests.Log4JFixture;
import jmri.implementation.AbstractTurnoutTest;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

public class NceTurnoutTest extends AbstractTurnoutTest {

    private NceTrafficControlScaffold tcis = null;

    @Override
    public void setUp() {
        Log4JFixture.setUp();
        // prepare an interface
        tcis = new NceTrafficControlScaffold();

        t = new NceTurnout(tcis, "NT", 4);
    }

    @Override
    public void tearDown() {
        Log4JFixture.tearDown();
    }

    @Override
    public int numListeners() {
        return tcis.numListeners();
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

    // from here down is testing infrastructure
    public NceTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NceTurnoutTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NceTurnoutTest.class);
        return suite;
    }

}
