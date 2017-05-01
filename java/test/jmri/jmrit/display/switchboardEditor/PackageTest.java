package jmri.jmrit.display.switchboardEditor;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.display.switchboardEditor package
 *
 * @author	Egbert Broerse Copyright 2017
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();

        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class.getName());

        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.jmrit.display.switchboardEditor.configurexml.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(SwitchboardEditorTest.class));

        return suite;
    }
}
