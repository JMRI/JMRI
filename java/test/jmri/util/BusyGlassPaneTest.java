package jmri.util;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

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

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BusyGlassPaneTest.class.getName());

}
