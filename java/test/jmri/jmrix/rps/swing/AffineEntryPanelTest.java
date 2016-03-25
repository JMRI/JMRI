package jmri.jmrix.rps.swing;

import java.awt.geom.AffineTransform;
import javax.swing.JFrame;
import jmri.util.JmriJFrame;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.rps.swing.AffineEntryPanel class
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class AffineEntryPanelTest extends TestCase {

    public void testCtor() {
        AffineEntryPanel p = new AffineEntryPanel();
        Assert.assertTrue(p.getTransform().equals(new AffineTransform()));
    }

    public void testListener() {
        JmriJFrame f = new JmriJFrame();
        AffineEntryPanel p = new AffineEntryPanel();
        f.add(p);
        f.pack();
        f.setTitle("Test RPS Listener");
        f.setVisible(true);
        java.beans.PropertyChangeListener l = new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("value")) {
                    System.out.println("See " + e.getPropertyName() + " as " + e.getNewValue());
                }
            }
        };
        p.addPropertyChangeListener(l);
        
        JFrame f2 = jmri.util.JmriJFrame.getFrame("Test RPS Listener");
        Assert.assertTrue("found frame", f2 != null);
        f2.dispose();
    }

    public void testRoundTrip() {
        AffineEntryPanel p = new AffineEntryPanel();
        AffineTransform t = new AffineTransform(2., 3., 4., 5., 6., 7.);
        p.setTransform(t);
        Assert.assertTrue(p.getTransform().equals(t));
    }

//    test order isn't guaranteed!
//    public void testFrameCreation() {
//        JFrame f = jmri.util.JmriJFrame.getFrame("Test RPS Listener");
//        Assert.assertTrue("found frame", f != null);
//        f.dispose();
//    }

    // from here down is testing infrastructure
    public AffineEntryPanelTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {AffineEntryPanelTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(AffineEntryPanelTest.class);
        return suite;
    }

}
