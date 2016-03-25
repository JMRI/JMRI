package jmri.jmrix.rps.trackingpanel;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.rps package.
 *
 * @author Bob Jacobsen Copyright 2006
 */
public class TrackingPanelTest extends TestCase {

    // from here down is testing infrastructure
    public TrackingPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {TrackingPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite("jmri.jmrix.rps.trackingpanel.TrackingPanelTest");
        suite.addTest(RpsTrackingFrameTest.suite());
        suite.addTest(RpsTrackingPanelTest.suite());
        return suite;
    }

}
