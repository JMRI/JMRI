package jmri.jmrix.rps.swing;

import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import javax.swing.JFrame;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import org.junit.*;

/**
 * Tests for the jmri.jmrix.rps.swing.AffineEntryPanel class
 *
 * @author Bob Jacobsen Copyright 2008
 */
public class AffineEntryPanelTest {

    @Test
    public void testCtor() {
        AffineEntryPanel p = new AffineEntryPanel();
        Assert.assertTrue(p.getTransform().equals(new AffineTransform()));
    }

    @Test
    public void testListener() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame f = new JmriJFrame();
        AffineEntryPanel p = new AffineEntryPanel();
        f.add(p);
        f.pack();
        f.setTitle("Test RPS Listener");
        f.setVisible(true);
        java.beans.PropertyChangeListener l = new java.beans.PropertyChangeListener() {
            @Override
            public void propertyChange(java.beans.PropertyChangeEvent e) {
                if (e.getPropertyName().equals("value")) {
                    System.out.println("See " + e.getPropertyName() + " as " + e.getNewValue());
                }
            }
        };
        p.addPropertyChangeListener(l);

        JFrame f2 = jmri.util.JmriJFrame.getFrame("Test RPS Listener");
        Assert.assertNotNull("found frame", f2);
        f2.dispose();
    }

    @Test
    public void testRoundTrip() {
        AffineEntryPanel p = new AffineEntryPanel();
        AffineTransform t = new AffineTransform(2., 3., 4., 5., 6., 7.);
        p.setTransform(t);
        Assert.assertTrue(p.getTransform().equals(t));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
