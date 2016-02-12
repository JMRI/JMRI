package jmri.jmrix.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SRCPTurnoutTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPTurnout class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class SRCPTurnoutTest extends TestCase {

    public void testCtor() {
        SRCPTrafficController et = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener l) {
                // we aren't actually sending anything to a layout.
            }
        };
        SRCPBusConnectionMemo memo = new SRCPBusConnectionMemo(et, "TEST", 1);
        memo.setTurnoutManager(new SRCPTurnoutManager(memo, memo.getBus()));
        SRCPTurnout m = new SRCPTurnout(1, memo);
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public SRCPTurnoutTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPTurnoutTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPTurnoutTest.class);
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
    private final static Logger log = LoggerFactory.getLogger(SRCPTurnoutTest.class.getName());
}
