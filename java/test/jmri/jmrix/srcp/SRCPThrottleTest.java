package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * SRCPThrottleTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPThrottle class
 *
 * @author	Bob Jacobsen
 */
public class SRCPThrottleTest extends TestCase {

    public void testCtor() {
        SRCPBusConnectionMemo sm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "TEST", 1);
        SRCPThrottle s = new SRCPThrottle(sm, new jmri.DccLocoAddress(1, true));
        Assert.assertNotNull(s);
    }

    // from here down is testing infrastructure
    public SRCPThrottleTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPThrottleTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPThrottleTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }
}
