package jmri.jmrix.srcp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SRCPPowerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPPowerManager class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class SRCPPowerManagerTest extends TestCase {

    public void testCtor() {
        SRCPPowerManager m = new SRCPPowerManager();
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public SRCPPowerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPPowerManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPPowerManagerTest.class);
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
    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SRCPPowerManagerTest.class.getName());
}
