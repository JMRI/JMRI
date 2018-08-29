package jmri.jmrit.catalog;

import java.awt.geom.AffineTransform;
import javax.swing.JLabel;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class NamedIconTest {

    @Test
    public void testCTor() {
        NamedIcon t = new NamedIcon("program:resources/logo.gif","logo");
        Assert.assertNotNull("exists",t);
    }

    // Test that identity Affine Transform doesn't affect size.
    @Test
    public void testTransformImageIdentity() {
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        AffineTransform t = new AffineTransform();    //Identity transform
        ni.transformImage(w,  h,  t, null);
        Assert.assertEquals(w,  ni.getIconWidth());
        Assert.assertEquals(h, ni.getIconHeight());
    }
    
    // Test that resize Affine Transform doesn't affects size. The size comes from the width and
    // Height parameters
    @Test
    public void testTransformImageScale() {
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        AffineTransform t = AffineTransform.getScaleInstance(0.5, 0.5);
        ni.transformImage(w/2,  h,  t,  null);
        Assert.assertEquals(w/2, ni.getIconWidth());
        Assert.assertEquals(h, ni.getIconHeight());
    }
    
    // Test rotate method with no scaling, 45 deg.
    @Test
    public void testRotateNoScaling() {
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        ni.rotate(45, new JLabel());
        int rectSize = (int) Math.ceil((h + w)/Math.sqrt(2.));
        Assert.assertEquals(rectSize, ni.getIconHeight());
        Assert.assertEquals(rectSize, ni.getIconWidth());
    }
    
    // Test scaling, no rotation
    @Test
    public void testScaling() {
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        double scale = 1.2;
        ni.scale(scale, new JLabel());
        Assert.assertEquals((int) (h * scale), ni.getIconHeight());
        Assert.assertEquals((int) (w * scale), ni.getIconWidth());        
    }
    
    // Test rotate method with scaling, 90 or 270
    // should just swap height and width and we'll scale by 2.0
    @Test
    public void testRotate270() {
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        JLabel comp = new JLabel();
        double scale = 2.0;
        ni.scale(scale, comp);
        ni.rotate(270, comp);
        // The +1 in the below is a bit of crock, but it's because argument of
        // Math.ceil(Math.abs(w*Math.cos(rad))) is slightly more than zero, so it
        // rounds up!
        Assert.assertEquals((int) Math.ceil(w * scale) + 1, ni.getIconHeight());
        Assert.assertEquals((int) Math.ceil(h * scale) + 1, ni.getIconWidth());
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

    // private final static Logger log = LoggerFactory.getLogger(NamedIconTest.class);

}
