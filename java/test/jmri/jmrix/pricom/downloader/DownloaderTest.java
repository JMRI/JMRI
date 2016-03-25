// DownloaderTest.java
package jmri.jmrix.pricom.downloader;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.pricom.downloader package.
 *
 * @author Bob Jacobsen Copyright 2005
 */
public class DownloaderTest extends TestCase {

    // from here down is testing infrastructure
    public DownloaderTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DownloaderTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.pricom.pockettester.DownloaderTest");
        suite.addTest(jmri.jmrix.pricom.downloader.PdiFileTest.suite());
        suite.addTest(jmri.jmrix.pricom.downloader.LoaderPaneTest.suite());
        return suite;
    }

}
