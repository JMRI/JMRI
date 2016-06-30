package jmri.jmrix.ieee802154.serialdriver;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.ieee802154.serialdriver package
 *
 * @author	Paul Bender
 * @version	$Revision$
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
        TestSuite suite = new TestSuite("jmri.jmrix.ieee802154.serialdriver.SerialTest");  // no tests in this class itself
        suite.addTest(new TestSuite(SerialSystemConnectionMemoTest.class));
        suite.addTest(new TestSuite(SerialTrafficControllerTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialNodeTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SerialDriverAdapterTest.class));
        return suite;
    }

}
