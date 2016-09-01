package jmri.jmrix.jmriclient.json;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.jmriclient package
 *
 * @author	Bob Jacobsen
 * @version	$Revision: 18472 $
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
        TestSuite suite = new TestSuite("jmri.jmrix.jmriclient.json.PackageTest");  // no tests in this class itself
        suite.addTest(BundleTest.suite());
        suite.addTest(jmri.jmrix.jmriclient.json.swing.PackageTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrix.jmriclient.json.configurexml.PackageTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(JsonNetworkConnectionConfigTest.class));

        // if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
        // there are currently no swing tests.
        // }
        return suite;
    }

}
