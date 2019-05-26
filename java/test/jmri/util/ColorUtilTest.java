package jmri.util;

import java.awt.Color;

import org.junit.*;

/**
 * Tests for the jmri.util.ColorUtil class.
 *
 * @author	Paul Bender Copyright 2016
 */
public class ColorUtilTest {

    @Test
    public void testBlackFromString() {
        Assert.assertEquals("Black from string", Color.black, ColorUtil.stringToColor(ColorUtil.ColorBlack));
    }

    @Test
    public void testStringFromBlack() {
        Assert.assertEquals("Black to string", ColorUtil.ColorBlack, ColorUtil.colorToString(Color.black));
    }

    @Test
    public void testDarkGrayFromString() {
        Assert.assertEquals("DarkGray from string", Color.darkGray, ColorUtil.stringToColor(ColorUtil.ColorDarkGray));
    }

    @Test
    public void testGrayFromString() {
        Assert.assertEquals("Gray from string", Color.gray, ColorUtil.stringToColor(ColorUtil.ColorGray));
    }

    @Test
    public void testStringFromGray() {
        Assert.assertEquals("Gray to string",ColorUtil.ColorGray, ColorUtil.colorToString(Color.gray));
    }

    @Test
    public void testLightGrayFromString() {
        Assert.assertEquals("LightGray from string", Color.lightGray, ColorUtil.stringToColor(ColorUtil.ColorLightGray));
    }

    @Test
    public void testStringFromLightGray() {
        Assert.assertEquals("LightGray to string",ColorUtil.ColorLightGray, ColorUtil.colorToString(Color.lightGray));
    }

    @Test
    public void testStringFromDarkGray() {
        Assert.assertEquals("DarkGray to string",ColorUtil.ColorDarkGray, ColorUtil.colorToString(Color.darkGray));
    }

    @Test
    public void testWhiteFromString() {
        Assert.assertEquals("White from string", Color.white, ColorUtil.stringToColor(ColorUtil.ColorWhite));
    }

    @Test
    public void testStringFromWhite() {
        Assert.assertEquals("White to string",ColorUtil.ColorWhite, ColorUtil.colorToString(Color.white));
    }

    @Test
    public void testStringFromRed() {
        Assert.assertEquals("Red to String",ColorUtil.ColorRed, ColorUtil.colorToString(Color.red));
    }

    @Test
    public void testRedFromString() {
        Assert.assertEquals("Red from string",Color.red, ColorUtil.stringToColor(ColorUtil.ColorRed));
    }

    @Test
    public void testStringFromPink() {
        Assert.assertEquals("Pink to string",ColorUtil.ColorPink, ColorUtil.colorToString(Color.pink));
    }

    @Test
    public void testPinkFromString() {
        Assert.assertEquals("Pink from string", Color.pink, ColorUtil.stringToColor(ColorUtil.ColorPink));
    }

    @Test
    public void testStringFromOrange() {
        Assert.assertEquals("Orange to string",ColorUtil.ColorOrange, ColorUtil.colorToString(Color.orange));
    }

    @Test
    public void testOrangeFromString() {
        Assert.assertEquals("Orange from string", Color.orange, ColorUtil.stringToColor(ColorUtil.ColorOrange));
    }

    @Test
    public void testYellowFromString() {
        Assert.assertEquals("Yellow from string", Color.yellow, ColorUtil.stringToColor(ColorUtil.ColorYellow));
    }

    @Test
    public void testStringFromYellow() {
        Assert.assertEquals("Yellow to string",ColorUtil.ColorYellow, ColorUtil.colorToString(Color.yellow));
    }

    @Test
    public void testStringFromGreen() {
        Assert.assertEquals("Green to String",ColorUtil.ColorGreen, ColorUtil.colorToString(Color.green));
    }

    @Test
    public void testGreenFromString() {
        Assert.assertEquals("Green from string", Color.green, ColorUtil.stringToColor(ColorUtil.ColorGreen));
    }

    @Test
    public void testStringFromBlue() {
        Assert.assertEquals("Blue to string",ColorUtil.ColorBlue,ColorUtil.colorToString(Color.blue));
    }

    @Test
    public void testBlueFromString() {
        Assert.assertEquals("Blue from string", Color.blue, ColorUtil.stringToColor(ColorUtil.ColorBlue));
    }

    @Test
    public void testStringFromMagenta() {
        Assert.assertEquals("Magenta to string",ColorUtil.ColorMagenta, ColorUtil.colorToString(Color.magenta));
    }

    @Test
    public void testMagentaFromString() {
        Assert.assertEquals("Magenta to string", Color.magenta, ColorUtil.stringToColor(ColorUtil.ColorMagenta));
    }

    @Test
    public void testStringFromCyan() {
        Assert.assertEquals("Cyan to string",ColorUtil.ColorCyan, ColorUtil.colorToString(Color.cyan));
    }

    @Test
    public void testCyanFromString() {
        Assert.assertEquals("Cyan to string", Color.cyan, ColorUtil.stringToColor(ColorUtil.ColorCyan));
    }

    @Test
    public void testDefaultStringFromColor() {
        Assert.assertEquals("other to string",ColorUtil.ColorBlack, ColorUtil.colorToString(new Color(42,42,42)));
        jmri.util.JUnitAppender.assertErrorMessage("unknown color sent to colorToString");
    }

    @Test
    public void testDefaultColorFromString() {
        try {
            ColorUtil.stringToColor("other"); //NOI18N
            Assert.fail("Expected exception not thrown");
        } catch (IllegalArgumentException ex) {
            Assert.assertEquals("unknown color text 'other'", ex.getMessage());
        }
        jmri.util.JUnitAppender.assertErrorMessage("unknown color text 'other' sent to stringToColor");
    }

    @Test
    public void testStringFromNull() {
        Assert.assertEquals("null to string",ColorUtil.ColorTrack, ColorUtil.colorToString(null));
    }

    @Test
    public void testColorFromTrack() {
        Assert.assertNull("track from null", ColorUtil.stringToColor(ColorUtil.ColorTrack));
    }

    @Test
    public void testHexStringFromMagenta() {
        Assert.assertEquals("hex string from magenta", ColorUtil.colorToHexString(Color.MAGENTA), "#FF00FF");
    }

    @Test
    public void testHexStringFromNull() {
        Assert.assertNull("hex string from null", ColorUtil.colorToHexString(null));
    }

    @Test
    public void testColorNameForMagenta() {
        Assert.assertEquals("color name for magenta", ColorUtil.colorToColorName(Color.MAGENTA),ColorUtil.ColorMagenta);
    }

    @Test
    public void testColorNameForFFCCFF() {
        Assert.assertEquals("color name for #FFCCFF", ColorUtil.colorToColorName(new Color(255, 204, 255)), "#FFCCFF");
    }

    @Test
    public void testColorNameForFF00FF() {
        Assert.assertEquals("color name for #FF00FF", ColorUtil.colorToColorName(new Color(255, 0, 255)), ColorUtil.ColorMagenta);
    }

    @Test
    public void testColorNameFromNull() {
        Assert.assertNull("color name from null", ColorUtil.colorToColorName(null));
    }

    @Before
    public void setUp() throws Exception {
       jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
       jmri.util.JUnitUtil.tearDown();
    }

}
