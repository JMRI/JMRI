package jmri.jmrit.dispatcher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.dispatcher package
 *
 * @author	Dave Duchamp
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
        TestSuite suite = new TestSuite("jmri.jmrit.dispatcher.PackageTest"); // no tests in class itself
        suite.addTest(jmri.jmrit.dispatcher.DispatcherTrainInfoTest.suite());
        suite.addTest(jmri.jmrit.dispatcher.DispatcherTrainInfoFileTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(jmri.jmrit.dispatcher.DispatcherFrameTest.suite());
        return suite;
    }

}
