package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * SRCPSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPSystemConnectionMemo class
 *
 * @author	Bob Jacobsen
 */
public class SRCPSystemConnectionMemoTest extends TestCase {

    public void testCtor() {
        SRCPSystemConnectionMemo m = new SRCPSystemConnectionMemo();
        Assert.assertNotNull(m);
    }

    public void testTCCtor() {
        SRCPTrafficController et = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener l) {
                // we aren't actually sending anything to a layout.
            }
        };
        SRCPSystemConnectionMemo m = new SRCPSystemConnectionMemo(et);
        Assert.assertNotNull(m);
    }

    // Full Constructor specifies prefix,name, and traffic controller.
    public void testFullCtor() {
        SRCPTrafficController et = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener l) {
                // we aren't actually sending anything to a layout.
            }
        };
        SRCPSystemConnectionMemo m = new SRCPSystemConnectionMemo("D", "SRCP", et);
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public SRCPSystemConnectionMemoTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPSystemConnectionMemoTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPSystemConnectionMemoTest.class);
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
