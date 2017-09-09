package jmri.jmris.json;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmris.json package
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
        TestSuite suite = new TestSuite(PackageTest.class.getPackage().getName());
        suite.addTest(new JUnit4TestAdapter(JsonServerTest.class));
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonServerActionTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonServerPreferencesPanelTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonServerPreferencesTest.class));
        suite.addTest(new JUnit4TestAdapter(JsonProgrammerServerTest.class));
        return suite;
    }

}
