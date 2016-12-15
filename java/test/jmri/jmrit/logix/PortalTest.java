package jmri.jmrit.logix;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 *
 * @author  Bob Jacobsen Copyright 2010, 2014
 */
public class PortalTest extends TestCase {
    
    public void testAddPortal() {
    }
    
    // from here down is testing infrastructure
    public PortalTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", OBlockTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        return new TestSuite(OBlockTest.class);
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
//        blkMgr = new OBlockManager();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
