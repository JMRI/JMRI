// RpsTrackingFrameTest.java

package jmri.jmrix.rps.trackingpanel;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;

/**
 * JUnit tests for the rps.RpsTrackingFrame class.
 * @author	Bob Jacobsen Copyright 2008
 * @version	$Revision: 1.1 $
 */
public class RpsTrackingFrameTest extends TestCase {

    public void testShow() {
        RpsTrackingFrame f = new RpsTrackingFrame("Test RPS Tracking");
        f.initComponents();
        f.setVisible(true);
    }
    
    // from here down is testing infrastructure
    
    public RpsTrackingFrameTest(String s) {
        super(s);
    }
    
    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {RpsTrackingFrameTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }
    
    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RpsTrackingFrameTest.class);
        return suite;
    }

}
