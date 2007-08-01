// RpsTrackingPanelTest.java

package jmri.jmrix.rps.trackingpanel;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import javax.swing.*;

/**
 * JUnit tests for the rps.RpsTrackingPanel class.
 * @author	Bob Jacobsen Copyright 2006
 * @version	$Revision: 1.1 $
 */
public class RpsTrackingPanelTest extends TestCase {

	public void testShow() {
        JFrame f = new JFrame("RPS Tracking");
        f.getContentPane().setLayout(new BoxLayout(f.getContentPane(), BoxLayout.Y_AXIS));
        RpsTrackingPanel p = new RpsTrackingPanel();
        p.setSize(400,400);
        p.setOrigin(0.,0.);
        p.setCoordMax(100.,100.);
        f.getContentPane().add(p);
        f.pack();
        f.setVisible(true);
  	}
        
	// from here down is testing infrastructure

	public RpsTrackingPanelTest(String s) {
            super(s);
	}

	// Main entry point
	static public void main(String[] args) {
            String[] testCaseName = {RpsTrackingPanelTest.class.getName()};
            junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
            TestSuite suite = new TestSuite(RpsTrackingPanelTest.class);
            return suite;
	}

}
