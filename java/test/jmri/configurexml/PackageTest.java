package jmri.configurexml;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the jmri.configxml package.
 *
 * @author	Bob Jacobsen
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests, including others in the package
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.configurexml.PackageTest");  // no tests in this class itself

        suite.addTest(new JUnit4TestAdapter(SchemaTest.class));
        suite.addTest(new JUnit4TestAdapter(LoadAndCheckTest.class));
        suite.addTest(new JUnit4TestAdapter(LoadAndStoreTest.class));

        suite.addTest(ConfigXmlManagerTest.suite());

        suite.addTest(BlockManagerXmlTest.suite());
        //suite.addTest(OBlockManagerXmlTest.suite());
        suite.addTest(SectionManagerXmlTest.suite());
        suite.addTest(new JUnit4TestAdapter(TransitManagerXmlTest.class));

        suite.addTest(DefaultJavaBeanConfigXMLTest.suite());
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new JUnit4TestAdapter(DccLocoAddressXmlTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriConfigureXmlExceptionTest.class));

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
