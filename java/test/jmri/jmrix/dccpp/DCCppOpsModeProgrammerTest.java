package jmri.jmrix.dccpp;

import org.junit.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * DCCppOpsModeProgrammerTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppOpsModeProgrammer class
 *
 * @author	Paul Bender
 * @author	Mark Underwood (C) 2015
 */
public class DCCppOpsModeProgrammerTest extends TestCase {

    public void testCtor() {
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppOpsModeProgrammer t = new DCCppOpsModeProgrammer(5, tc);
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public DCCppOpsModeProgrammerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppOpsModeProgrammerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppOpsModeProgrammerTest.class);
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
