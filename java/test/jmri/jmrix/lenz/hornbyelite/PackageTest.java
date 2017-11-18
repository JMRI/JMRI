package jmri.jmrix.lenz.hornbyelite;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.lenz.hornbyelite package
 *
 * @author Paul Bender
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrix.lenz.hornbyelite");  // no tests in this class itself
        suite.addTest(new TestSuite(HornbyEliteCommandStationTest.class));
        suite.addTest(new TestSuite(EliteAdapterTest.class));
        suite.addTest(new TestSuite(EliteConnectionTypeListTest.class));
        suite.addTest(new TestSuite(EliteXNetInitializationManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EliteXNetThrottleManagerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EliteXNetThrottleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EliteXNetTurnoutTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EliteXNetTurnoutManagerTest.class));
        suite.addTest(new TestSuite(EliteXNetProgrammerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ConnectionConfigTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.lenz.hornbyelite.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(EliteXNetSystemConnectionMemoTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        return suite;
    }

}
