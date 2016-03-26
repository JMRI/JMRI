package jmri.jmrix.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SRCPTurnoutManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPTurnoutManager class
 *
 * @author	Bob Jacobsen
 */
public class SRCPTurnoutManagerTest extends TestCase {

    public void testCtor() {
        SRCPTurnoutManager m = new SRCPTurnoutManager();
        Assert.assertNotNull(m);
    }

    public void testBusCtor() {
        SRCPTrafficController et = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener l) {
                // we aren't actually sending anything to a layout.
            }
        };
        SRCPBusConnectionMemo memo = new SRCPBusConnectionMemo(et, "TEST", 1);
        SRCPTurnoutManager m = new SRCPTurnoutManager(memo, memo.getBus());
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public SRCPTurnoutManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPTurnoutManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPTurnoutManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
}
