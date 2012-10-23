// PackageTest.java

package jmri.configurexml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the jmri.configxml package.
 * @author	Bob Jacobsen
 * @version         $Revision$
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure

    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests, including others in the package
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.config.ConfigXmlTest");  // no tests in this class itself
        suite.addTest(LoadFileTest.suite());
        suite.addTest(ConfigXmlManagerTest.suite());
        suite.addTest(BlockManagerXmlTest.suite());
        suite.addTest(SectionManagerXmlTest.suite());
        suite.addTest(SchemaTest.suite());
        suite.addTest(DefaultJavaBeanConfigXMLTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() { apps.tests.Log4JFixture.setUp(); }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }

}
