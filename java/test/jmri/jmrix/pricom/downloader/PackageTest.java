package jmri.jmrix.pricom.downloader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.pricom.downloader package.
 *
 * @author Bob Jacobsen Copyright 2005
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
        TestSuite suite = new TestSuite("jmri.jmrix.pricom.pockettester");
        suite.addTest(jmri.jmrix.pricom.downloader.PdiFileTest.suite());
        suite.addTest(jmri.jmrix.pricom.downloader.LoaderPaneTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(LoaderFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(LoaderPanelActionTest.class));
        return suite;
    }

}
