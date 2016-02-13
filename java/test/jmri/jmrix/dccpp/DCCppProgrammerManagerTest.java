package jmri.jmrix.dccpp;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DCCppProgrammerManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.dccpp.DCCppProgrammerManager class
 *
 * @author	Paul Bender
 * @author	Mark Underwood (C) 2015
 * @version $Revision$
 */
public class DCCppProgrammerManagerTest extends TestCase {

    public void testCtor() {
        // infrastructure objects
        DCCppInterfaceScaffold tc = new DCCppInterfaceScaffold(new DCCppCommandStation());

        DCCppProgrammerManager t = new DCCppProgrammerManager(new DCCppProgrammer(tc), new DCCppSystemConnectionMemo(tc));
        Assert.assertNotNull(t);
    }

    // from here down is testing infrastructure
    public DCCppProgrammerManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", DCCppProgrammerManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DCCppProgrammerManagerTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

    private final static Logger log = LoggerFactory.getLogger(DCCppProgrammerManagerTest.class.getName());

}
