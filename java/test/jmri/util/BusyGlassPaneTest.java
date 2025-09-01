package jmri.util;

import java.util.ArrayList;

import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class BusyGlassPaneTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {
        JmriJFrame f = new JmriJFrame("BusyGlassPane ConstructorTest");
        java.awt.Component comp[] = f.getComponents();
        ArrayList<javax.swing.JComponent> cal = new ArrayList<>();
        ArrayList<java.awt.Rectangle> ral = new ArrayList<>();
        java.util.Arrays.stream(comp).forEach(i -> { 
               if(i instanceof javax.swing.JComponent ) {
                  ral.add(i.getBounds(null));
                  cal.add((javax.swing.JComponent)i);
               }
        });
        BusyGlassPane t = new BusyGlassPane(cal,ral,f.getContentPane(),f);
        Assertions.assertNotNull( t, "exists");
        JUnitUtil.dispose(f);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(BusyGlassPaneTest.class.getName());

}
