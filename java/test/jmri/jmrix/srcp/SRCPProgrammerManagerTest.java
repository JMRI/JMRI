package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * SRCPProgrammerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPProgrammerManager class
 *
 * @author	Bob Jacobsen
 */
public class SRCPProgrammerManagerTest extends TestCase {

    public void testCtor() {
        SRCPBusConnectionMemo sm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "A", 1);
        SRCPProgrammerManager s = new SRCPProgrammerManager(new SRCPProgrammer(sm), sm);
        Assert.assertNotNull(s);
    }

    // from here down is testing infrastructure
    public SRCPProgrammerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPProgrammerManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPProgrammerManagerTest.class);
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
