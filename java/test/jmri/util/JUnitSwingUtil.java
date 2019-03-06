package jmri.util;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;

import javax.swing.AbstractButton;

import org.junit.Assert;
import org.netbeans.jemmy.operators.AbstractButtonOperator;
import org.netbeans.jemmy.operators.JButtonOperator;

/**
 * Utilities for GUI unit testing.
 * 
 * @author Randall Wood, Copyright 2019
 *
 */
public final class JUnitSwingUtil {

    /**
     * Get the displayed content of a JComponent.
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
        // check pixel color (from http://stackoverflow.com/questions/13307962/how-to-get-the-color-of-a-point-in-a-jpanel )
        BufferedImage image = new BufferedImage(size.width, size.height, BufferedImage.TYPE_4BYTE_ABGR);
        Graphics2D g2 = image.createGraphics();
        component.paint(g2);

        int[] retval = image.getRGB(upLeft.x, upLeft.y, size.width, size.height, null, 0, size.width);

        g2.dispose();
        return retval;
    }

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
     */
    public static String formatPixel(int pixel) {
        return String.format("0x%8s", Integer.toHexString(pixel)).replace(' ', '0');
    }

    /**
     * Clean way to assert against a pixel value.
     *
     * @param name  Condition being asserted
     * @param value Correct ARGB value for test
     * @param pixel ARGB piel value being tested
     */
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
     */
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

    /**
     * Press a button after finding it in a container by title.
     * 
     * @param frame container containing button to press
     * @param text button title
     * @return the pressed button
     */
    public static AbstractButton pressButton(Container frame, String text) {
        AbstractButton button = JButtonOperator.findAbstractButton(frame, text, true, true);
        Assert.assertNotNull(text + " Button not found", button);
        AbstractButtonOperator abo = new AbstractButtonOperator(button);
        abo.doClick();
        return button;
    }

}
