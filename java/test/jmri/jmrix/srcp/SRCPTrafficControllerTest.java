package jmri.jmrix.srcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * SRCPTrafficControllerTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPTrafficController class
 *
 * @author	Bob Jacobsen
 * @version $Revision$
 */
public class SRCPTrafficControllerTest extends TestCase {

    public void testCtor() {
        SRCPTrafficController m = new SRCPTrafficController();
        Assert.assertNotNull(m);
    }

    // from here down is testing infrastructure
    public SRCPTrafficControllerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", SRCPTrafficControllerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SRCPTrafficControllerTest.class);
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
    private final static Logger log = LoggerFactory.getLogger(SRCPTrafficControllerTest.class.getName());
}
