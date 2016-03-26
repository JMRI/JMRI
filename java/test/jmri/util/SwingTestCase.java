package jmri.util;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import junit.extensions.jfcunit.JFCTestCase;
import junit.extensions.jfcunit.JFCTestHelper;
import junit.extensions.jfcunit.TestHelper;

import junit.framework.Assert;

/**
 * Provide Swing context for JUnit test classes.
 * <p>
 * By default, JFCUnit closes all windows at the end of each test. JMRI tests
 * leave windows open, so that's been bypassed for now.
 *
 * @author	Bob Jacobsen - Copyright 2009
 * @version	$Revision$
 * @since 2.5.3
 */
public class SwingTestCase extends JFCTestCase {

    public SwingTestCase(String s) {
        super(s);
        setLockWait(10); // getLockWait() found default value 25 in JMRI 4.3.4
    }

    /**
     * Get the displayed content of a JComponent.
     * 
     * static so that it can in invoked outside SwingTestCases subclasses
     *
     * Note: this does no adjustment, e.g. pack, etc.  That should have been already been done as required.
     * 
     * @param component Typically a JComponent, could be a JFrame, the item to be returned
     * @param upLeft the upper-left corner of the returned area in component's coordinates
     * @param size dimension of returned array
     * @return int[] array of ARGB values
     */
    public static int[] getDisplayedContent(java.awt.Container component, Dimension size, Point upLeft) {
        // check pixel color (from http://stackoverflow.com/questions/13307962/how-to-get-the-color-of-a-point-in-a-jpanel )
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = image.createGraphics();
        component.paint(g2);
        
        int[] retval = image.getRGB(upLeft.x, upLeft.y, size.width, size.height, null, 0, size.width);

        g2.dispose();
        return retval;
    }

    protected enum Pixel { // protected to limit leakage outside Swing tests
    
        TRANSPARENT (0x00000000),
        RED         (0xFFFF0000),
        GREEN       (0xFF00FF00),
        BLUE        (0xFF0000FF),
        WHITE       (0xFFFFFFFF),
        BLACK       (0xFF000000),
        YELLOW      (0xFFFFFF00);
    
        public String toString() { return formatPixel(value); }
        public boolean equals(int v) { return value == v; }
        private final int value;
        private Pixel(int value) { this.value = value; }
    }
    
    /**
     * Standard parsing of ARCG pixel (int) value to String
     */
    public static String formatPixel(int pixel) {
        return String.format("0x%8s", Integer.toHexString(pixel)).replace(' ', '0');
    }
    
    /**
     * Clean way to assert against a pixel value.
     * @param name Condition being asserted
     * @param value Correct ARGB value for test
     * @param pixel ARGB piel value being tested
     */
    protected static void assertPixel(String name, Pixel value, int pixel) {
        Assert.assertEquals(name, value.toString(), formatPixel(pixel));
    }
    
    /**
     * Check four corners and center of an image
     * @param name Condition being asserted
     * @param pixels Image ARCG array
     */
    protected static void assertImageNinePoints(String name, int[] pixels, Dimension size, 
                        Pixel upperLeft, Pixel upperCenter, Pixel upperRight,
                        Pixel midLeft, Pixel center, Pixel midRight,
                        Pixel lowerLeft, Pixel lowerCenter, Pixel lowerRight
                        ) {
        int rows = size.height;
        int cols = size.width;
        
        Assert.assertEquals("size consistency", pixels.length, rows*cols);
        
        assertPixel(name+" upper left", upperLeft,     pixels[0]);
        assertPixel(name+" upper middle", upperCenter, pixels[0+cols/2]);
        assertPixel(name+" upper right", upperRight,   pixels[0+(cols-1)]);
        
        assertPixel(name+" middle left", midLeft,      pixels[(rows/2)*cols]);
        assertPixel(name+" middle right", midRight,    pixels[(rows/2)*cols+(cols-1)]);
        
        assertPixel(name+" lower left", lowerLeft,     pixels[(rows*cols-1)-(cols-1)]);
        assertPixel(name+" lower middle", lowerCenter, pixels[(rows*cols-1)-(cols-1)+cols/2]);
        assertPixel(name+" lower right", lowerRight,   pixels[rows*cols-1]);

        // we've checked the corners first on purpose, to see they're all right
        assertPixel(name+" center", center,            pixels[(rows/2)*cols+cols/2]);

    }
    
    /**
     * Provides a (slightly) better calibrated waiting interval
     * than a native awtSleep()
     */
    public void waitAtLeast(int delay) {
        long start = System.currentTimeMillis();
        while (System.currentTimeMillis() < start+delay) {
            awtSleep(20); // this is completely uncalibrated
        }
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        // Choose the test Helper
        setHelper(new JFCTestHelper()); // Uses the AWT Event Queue.
        // setHelper( new RobotTestHelper( ) ); // Uses the OS Event Queue.
    }

    protected void leaveAllWindowsOpen() {
        TestHelper.addSystemWindow(".");  // all windows left open
    }

    protected void tearDown() throws Exception {
        leaveAllWindowsOpen();
        TestHelper.cleanUp(this);
        super.tearDown();
    }

}
