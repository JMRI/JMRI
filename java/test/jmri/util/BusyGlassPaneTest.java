package jmri.util;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class BusyGlassPaneTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JmriJFrame f = new JmriJFrame("BusyGlassPane ConstructorTest");
        java.awt.Component comp[] = f.getComponents();
        java.util.ArrayList<javax.swing.JComponent> cal = new java.util.ArrayList<javax.swing.JComponent>();
        java.util.ArrayList<java.awt.Rectangle> ral = new java.util.ArrayList<java.awt.Rectangle>();
        java.util.Arrays.stream(comp).forEach(i -> { 
               if(i instanceof javax.swing.JComponent ) {
                  ral.add(i.getBounds(null));
                  cal.add((javax.swing.JComponent)i);
               }
        });
        BusyGlassPane t = new BusyGlassPane(cal,ral,f.getContentPane(),f);
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(f);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BusyGlassPaneTest.class.getName());

}
