// RpsTrackingFrameTest.java

package jmri.jmrix.rps.trackingpanel;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.jmrix.rps.*;

import javax.swing.JFrame;
import javax.vecmath.Point3d;

/**
 * JUnit tests for the rps.RpsTrackingFrame class.
 * @author	Bob Jacobsen Copyright 2008
 * @version	$Revision$
 */
public class RpsTrackingFrameTest extends TestCase {

    public void testShow() {
        new Engine(){ void reset() { _instance = null; }}.reset();
        Engine.instance().setMaxReceiverNumber(2);
        Engine.instance().setReceiver(1, new Receiver(new Point3d(12.,12.,0.)));
        Engine.instance().setReceiver(2, new Receiver(new Point3d(12.,12.,0.)));

        RpsTrackingFrame f = new RpsTrackingFrame("Test RPS Tracking");
        f.initComponents();
        f.setVisible(true);

        RpsTrackingPanel p = f.panel; // use local access
        
        Reading loco = new Reading("21", null);
        Measurement m = new Measurement(loco, 0.0, 0.0, 0.0, 0.133, 5, "source");
        p.notify(m);
        
        loco = new Reading("21", null);
        m = new Measurement(loco, 5., 5., 0.0, 0.133, 5, "source");
        p.notify(m);
        
        loco = new Reading("21", null);
        m = new Measurement(loco, 0., 5., 0.0, 0.133, 5, "source");
        p.notify(m);

        loco = new Reading("21", null);
        m = new Measurement(loco, 5., 0., 0.0, 0.133, 5, "source");
        p.notify(m);

    }
    
    public void testXFrameCreation() {
    	JFrame f = jmri.util.JmriJFrame.getFrame("Test RPS Tracking");
    	Assert.assertTrue("found frame", f !=null );
    	if (f != null)
    		f.dispose();
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
