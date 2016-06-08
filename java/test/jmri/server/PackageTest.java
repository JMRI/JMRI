/**
 * PackageTest.java
 *
 * Description:	tests for the jmri.server package
 *
 * @author	Bob Jacobsen 2009
 */
package jmri.server;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
        TestSuite suite = new TestSuite("jmri.server");   // no tests in this class itself

        suite.addTest(BundleTest.suite());
        suite.addTest(jmri.server.json.PackageTest.suite());

        return suite;
    }

}
