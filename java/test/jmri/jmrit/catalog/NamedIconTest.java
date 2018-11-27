package jmri.jmrit.catalog;

import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.PixelGrabber;
import javax.swing.JLabel;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Joe Comuzzi Copyright (C) 2018	
 */
public class NamedIconTest {

    /**
     * Test constructor for NamedIcon
     */
    @Test
    public void testCTor() {
        NamedIcon t = new NamedIcon("program:resources/logo.gif","logo");
        Assert.assertNotNull("exists",t);
    }

    /**
     *  Test transformImage. Confirm that resize Affine Transform doesn't affects size.
     *  The size comes from the width and height parameters
     */
    @Test
    public void testTransformImage() {
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        AffineTransform t = AffineTransform.getScaleInstance(0.5, 0.5);
        
        ni.transformImage(w/2,  h,  t,  null);
        Assert.assertEquals(w/2, ni.getIconWidth());
        Assert.assertEquals(h, ni.getIconHeight());
    }
    
    /**
     *  Test rotate method with no scaling, 45 deg rotation. The size
     *  should change.
     */
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
    
    /**
     *  Test scaling, no rotation
     */
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
    
    /**
     *  Test rotate method with scaling, 270 degrees
     *  should just swap height and width and we'll scale by 2.0
     */
    @Test
    public void testRotate270() {
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        JLabel comp = new JLabel();
        double scale = 2.0;
        
        ni.scale(scale, comp);
        ni.rotate(270, comp);
        // The +1 in the below is a bit of a crock, but it's because the argument of
        // Math.ceil(Math.abs(w*Math.cos(rad))) is slightly more than zero, so it
        // rounds up!
        Assert.assertEquals((int) Math.ceil(w * scale) + 1, ni.getIconHeight());
        Assert.assertEquals((int) Math.ceil(h * scale) + 1, ni.getIconWidth());
    } 
    
    /**
     *  Test setLoad and scale. 30 degrees is also simple geometry.
     *  Also test getDegrees and getScale while we're here
     */
    @Test
    public void testSetLoad()
    {
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        JLabel comp = new JLabel();
        double scale = 0.333;
        int degrees = 30;
        
        ni.setLoad(degrees, scale, comp);  
        Assert.assertEquals(ni.getDegrees(), degrees);
        Assert.assertEquals(ni.getScale(), scale, .001);
        
        // Be wary of numerical instability in these tests. e.g. because of rounding in NamedIcon, sometimes
        // cos(30) is not exactly 0.5 and the ceil operation gives different answers!
        double sqrt3 = Math.sqrt(3);
        int expectedHeight = (int) (Math.ceil(h * sqrt3 * scale / 2.0 + w * scale / 2.0));
        Assert.assertEquals(expectedHeight, ni.getIconHeight());
        int expectedWidth = (int) (Math.ceil(h * scale / 2.0 + w * sqrt3 * scale / 2.0));
        Assert.assertEquals(expectedWidth, ni.getIconWidth());
    }
    
    /**
     *  Test reduceTo method.
     *  
     */
    @Test
    public void testReduceTo() {
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        
        // Test that limit of one won't let you reduce the size.
        ni.reduceTo(10, 10, 1.0);
        Assert.assertEquals(h, ni.getIconHeight());
        Assert.assertEquals(w, ni.getIconWidth());
       
        // Test that we can reduce the size
        ni.reduceTo(w / 3, h / 2, 0.1);
        Assert.assertEquals(w / 3, ni.getIconWidth());
        Assert.assertEquals(h / 3, ni.getIconHeight());
    }
    
    /**
     *  Test flip method
     */
    @Test
    public void testFlip() {
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        JLabel comp = new JLabel();
        
        ni.flip(NamedIcon.NOFLIP, comp);       
        Assert.assertEquals(w, ni.getIconWidth());
        Assert.assertEquals(h, ni.getIconHeight());
        int [] noflipPixels = getPixels(ni.getImage());
        
        ni.flip(NamedIcon.VERTICALFLIP, comp);
        Assert.assertEquals(w, ni.getIconWidth());
        Assert.assertEquals(h, ni.getIconHeight());
        
        int [] flipPixels = getPixels(ni.getImage()); 
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Assert.assertEquals(noflipPixels[j * w + i], flipPixels[(h - 1 - j) * w + i]);
            }
        }
    }
    
    /**
     *  Test createRotatedImage. N.B, createRotatedImage forces the color
     *  model, so we can't easily compare to the original image. Instead,
     *  we compare the image rotated by 90 deg to the image rotated by 180 deg,
     */
    @Test
    public void testCreateRotatedImage() {
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        JLabel comp = new JLabel();
        Image img = ni.getImage();
        
        Image rot1Image = ni.createRotatedImage(img, comp, 1);
        Assert.assertEquals(w, rot1Image.getHeight(null));
        Assert.assertEquals(h, rot1Image.getWidth(null));
        int [] rot1Pixels = getPixels(rot1Image);
        
        Image rot2Image = ni.createRotatedImage(img, comp, 2);
        Assert.assertEquals(h, rot2Image.getHeight(null));
        Assert.assertEquals(w, rot2Image.getWidth(null));
        int [] rot2Pixels = getPixels(rot2Image);

        for (int i = 0; i < h; i++) {
            for (int j = 0; j < w; j++) {
                Assert.assertEquals(rot1Pixels[j * h + i], rot2Pixels[(h - 1 - i) * w + j]); 
            }
        }
    }
    
    /**
     *  Test setRotation and getRotation. Since this calls createRotatedImage, we compare
     *  the image rotated by 90 deg to the image rotated by 270 deg.
     */
    @Test
    public void testSetRotation() {
        NamedIcon ni = new NamedIcon("program:resources/logo.gif","logo");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        JLabel comp = new JLabel();
        
        Assert.assertEquals(0, ni.getRotation());
            
        ni.setRotation(1, comp);
        Assert.assertEquals(h, ni.getIconWidth());
        Assert.assertEquals(w, ni.getIconHeight());
        Assert.assertEquals(1, ni.getRotation());
        
        int [] rot1Pixels = getPixels(ni.getImage());
            
        ni.setRotation(3, comp);
        Assert.assertEquals(h, ni.getIconWidth());
        Assert.assertEquals(w, ni.getIconHeight());
        Assert.assertEquals(3, ni.getRotation());
        
        int [] rot3Pixels = getPixels(ni.getImage());
    
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                Assert.assertEquals(rot1Pixels[j * w + i], rot3Pixels[(h - j) * w - 1 - i]);
            }
        }
    }
    
    /**
     * Test rotate and scale with blinking GIF. This will use the animated GIF codepath.
     */
    @Test
    public void testAnimatedGif() {
        NamedIcon ni = new NamedIcon("program:resources/icons/largeschematics/aspects/CSD-1962/003_o40_p.gif", "blink");
        int h = ni.getIconHeight();
        int w = ni.getIconWidth();
        JLabel comp = new JLabel();
        
        double scale = 2.0;
        
        ni.scale(scale, comp);
        ni.rotate(270, comp);
        // The +1 in the below is a bit of a crock, but it's because the argument of
        // Math.ceil(Math.abs(w*Math.cos(rad))) is slightly more than zero, so it
        // rounds up!
        Assert.assertEquals((int) Math.ceil(w * scale) + 1, ni.getIconHeight());
        Assert.assertEquals((int) Math.ceil(h * scale), ni.getIconWidth());       
    }

    /**
     * Helper routine to grab the pixels from an Image
     * @param img  Image to get pixels from
     * @return array of ints, one for each pixel
     */
    private int [] getPixels(Image img) {
        int w = img.getWidth(null);
        int h = img.getHeight(null);
        int[] pixels = new int[w * h];
        PixelGrabber pg = new PixelGrabber(img, 0, 0, w, h, pixels, 0, w);
        try {
            pg.grabPixels();
        } catch (InterruptedException ie) {
        }
        return pixels;
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
