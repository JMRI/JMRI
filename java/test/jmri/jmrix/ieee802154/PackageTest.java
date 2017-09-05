package jmri.jmrix.ieee802154;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.ieee802154 package
 *
 * @author	Paul Bender
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
        TestSuite suite = new TestSuite("jmri.jmrix.ieee802154.IEEE802154Test");  // no tests in this class itself
        suite.addTest(new TestSuite(IEEE802154MessageTest.class));
        suite.addTest(new TestSuite(IEEE802154ReplyTest.class));
        suite.addTest(new TestSuite(IEEE802154SystemConnectionMemoTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(IEEE802154TrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.ieee802154.xbee.PackageTest.class));
        suite.addTest(jmri.jmrix.ieee802154.serialdriver.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(IEEE802154NodeTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.ieee802154.swing.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(IEEE802154PortControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialConnectionTypeListTest.class));
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
