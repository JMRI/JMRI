package jmri.util;

import java.awt.Dimension;
import java.awt.Point;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Provide Swing context for JUnit test classes.
 *
 * @author Bob Jacobsen - Copyright 2009
 * @since 2.5.3
 * @deprecated use {@link jmri.util.JUnitSwingUtil} instead
 */
@Deprecated // for removal after JMRI 4.18
public class SwingTestCase {

    public SwingTestCase(String s) {
        // nothing to do
    }

    /**
     * Get the displayed content of a JComponent.
     * <p>
     * static so that it can in invoked outside SwingTestCases subclasses
     * <p>
     * Note: this does no adjustment, e.g. pack, etc. That should have been
     * already been done as required.
     *
     * @param component Typically a JComponent, could be a JFrame, the item to
     *                      be returned
     * @param upLeft    the upper-left corner of the returned area in
     *                      component's coordinates
     * @param size      dimension of returned array
     * @return int[] array of ARGB values
     */
    public static int[] getDisplayedContent(java.awt.Container component, Dimension size, Point upLeft) {
        return JUnitSwingUtil.getDisplayedContent(component, size, upLeft);
    }

    /**
     * @deprecated use {@link jmri.util.JUnitSwingUtil.Pixel} instead
     */
    @Deprecated
    public static enum Pixel {

        TRANSPARENT(0x00000000),
        RED(0xFFFF0000),
        GREEN(0xFF00FF00),
        BLUE(0xFF0000FF),
        WHITE(0xFFFFFFFF),
        BLACK(0xFF000000),
        YELLOW(0xFFFFFF00);

        @Override
        public String toString() {
            return formatPixel(value);
        }

        public boolean equals(int v) {
            return value == v;
        }

        private final int value;

        private Pixel(int value) {
            this.value = value;
        }
    }

    /**
     * Parse ARCG pixel value.
     * 
     * @param pixel the pixel to parse
     * @return hexadecimal representation of the pixel
     * @deprecated use {@link jmri.util.JUnitSwingUtil#formatPixel(int)} instead
     */
    @Deprecated
    public static String formatPixel(int pixel) {
        return JUnitSwingUtil.formatPixel(pixel);
    }

    /**
     * Clean way to assert against a pixel value.
     *
     * @param name  Condition being asserted
     * @param value Correct ARGB value for test
     * @param pixel ARGB pixel value being tested
     * @deprecated use
     *             {@link jmri.util.JUnitSwingUtil#assertPixel(String, jmri.util.JUnitSwingUtil.Pixel, int)}
     *             instead
     */
    @Deprecated
    public static void assertPixel(String name, Pixel value, int pixel) {
        Assert.assertEquals(name, value.toString(), formatPixel(pixel));
    }

    /**
     * Check four corners and center of an image
     *
     * @param name        Condition being asserted
     * @param pixels      Array of ARCG pixels from image
     * @param size        the size of the image
     * @param upperLeft   expected value of pixel in top left corner of image
     * @param upperCenter expected value of pixel in top center of image
     * @param upperRight  expected value of pixel in top right corner of image
     * @param midLeft     expected value of pixel in center of left side of
     *                        image
     * @param center      expected value of pixel in center of image
     * @param midRight    expected value of pixel in center of right side of
     *                        image
     * @param lowerLeft   expected value of pixel in bottom left corner of image
     * @param lowerCenter expected value of pixel in bottom center of image
     * @param lowerRight  expected value of pixel in top right corner of image
     * @deprecated use {@link jmri.util.JUnitSwingUtil} instead
     */
    @Deprecated
    public static void assertImageNinePoints(String name, int[] pixels, Dimension size,
            Pixel upperLeft, Pixel upperCenter, Pixel upperRight,
            Pixel midLeft, Pixel center, Pixel midRight,
            Pixel lowerLeft, Pixel lowerCenter, Pixel lowerRight) {
        int rows = size.height;
        int cols = size.width;

        Assert.assertEquals("size consistency", pixels.length, rows * cols);

        assertPixel(name + " upper left", upperLeft, pixels[0]);
        assertPixel(name + " upper middle", upperCenter, pixels[0 + cols / 2]);
        assertPixel(name + " upper right", upperRight, pixels[0 + (cols - 1)]);

        assertPixel(name + " middle left", midLeft, pixels[(rows / 2) * cols]);
        assertPixel(name + " middle right", midRight, pixels[(rows / 2) * cols + (cols - 1)]);

        assertPixel(name + " lower left", lowerLeft, pixels[(rows * cols - 1) - (cols - 1)]);
        assertPixel(name + " lower middle", lowerCenter, pixels[(rows * cols - 1) - (cols - 1) + cols / 2]);
        assertPixel(name + " lower right", lowerRight, pixels[rows * cols - 1]);

        // we've checked the corners first on purpose, to see they're all right
        assertPixel(name + " center", center, pixels[(rows / 2) * cols + cols / 2]);
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

}
