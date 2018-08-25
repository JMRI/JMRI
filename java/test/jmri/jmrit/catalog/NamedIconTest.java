package jmri.jmrit.catalog;

import java.awt.geom.AffineTransform;
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
        AffineTransform t = new AffineTransform();    //Identity transform
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        ni.transformImage(w,  h,  t, null);
        Assert.assertEquals(h, ni.getIconHeight());
        Assert.assertEquals(w,  ni.getIconWidth());
    }
    
    // Test that resize Affine Transform correctly affects size.
    @Test
    public void testTransformImageScale() {
        AffineTransform t = AffineTransform.getScaleInstance(0.5, 0.5);
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        ni.transformImage(w,  h,  t,  null);
        Assert.assertEquals(h, ni.getIconHeight());
        Assert.assertEquals(w, ni.getIconWidth());
        
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
