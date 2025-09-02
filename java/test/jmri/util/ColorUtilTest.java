package jmri.util;

import java.awt.Color;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the jmri.util.ColorUtil class.
 *
 * @author Paul Bender Copyright 2016
 */
public class ColorUtilTest {

    @Test
    public void testBlackFromString() {
        assertEquals( Color.black, ColorUtil.stringToColor(ColorUtil.ColorBlack), "Black from string");
    }

    @Test
    public void testStringFromBlack() {
        assertEquals( ColorUtil.ColorBlack, ColorUtil.colorToString(Color.black), "Black to string");
    }

    @Test
    public void testDarkGrayFromString() {
        assertEquals( Color.darkGray, ColorUtil.stringToColor(ColorUtil.ColorDarkGray), "DarkGray from string");
    }

    @Test
    public void testGrayFromString() {
        assertEquals( Color.gray, ColorUtil.stringToColor(ColorUtil.ColorGray), "Gray from string");
    }

    @Test
    public void testStringFromGray() {
        assertEquals( ColorUtil.ColorGray, ColorUtil.colorToString(Color.gray), "Gray to string");
    }

    @Test
    public void testLightGrayFromString() {
        assertEquals( Color.lightGray, ColorUtil.stringToColor(ColorUtil.ColorLightGray), "LightGray from string");
    }

    @Test
    public void testStringFromLightGray() {
        assertEquals( ColorUtil.ColorLightGray, ColorUtil.colorToString(Color.lightGray), "LightGray to string");
    }

    @Test
    public void testStringFromDarkGray() {
        assertEquals( ColorUtil.ColorDarkGray, ColorUtil.colorToString(Color.darkGray), "DarkGray to string");
    }

    @Test
    public void testWhiteFromString() {
        assertEquals( Color.white, ColorUtil.stringToColor(ColorUtil.ColorWhite), "White from string");
    }

    @Test
    public void testStringFromWhite() {
        assertEquals( ColorUtil.ColorWhite, ColorUtil.colorToString(Color.white), "White to string");
    }

    @Test
    public void testStringFromRed() {
        assertEquals( ColorUtil.ColorRed, ColorUtil.colorToString(Color.red), "Red to String");
    }

    @Test
    public void testRedFromString() {
        assertEquals( Color.red, ColorUtil.stringToColor(ColorUtil.ColorRed), "Red from string");
    }

    @Test
    public void testStringFromPink() {
        assertEquals( ColorUtil.ColorPink, ColorUtil.colorToString(Color.pink), "Pink to string");
    }

    @Test
    public void testPinkFromString() {
        assertEquals( Color.pink, ColorUtil.stringToColor(ColorUtil.ColorPink), "Pink from string");
    }

    @Test
    public void testStringFromOrange() {
        assertEquals( ColorUtil.ColorOrange, ColorUtil.colorToString(Color.orange), "Orange to string");
    }

    @Test
    public void testOrangeFromString() {
        assertEquals( Color.orange, ColorUtil.stringToColor(ColorUtil.ColorOrange), "Orange from string");
    }

    @Test
    public void testYellowFromString() {
        assertEquals( Color.yellow, ColorUtil.stringToColor(ColorUtil.ColorYellow), "Yellow from string");
    }

    @Test
    public void testStringFromYellow() {
        assertEquals( ColorUtil.ColorYellow, ColorUtil.colorToString(Color.yellow), "Yellow to string");
    }

    @Test
    public void testStringFromGreen() {
        assertEquals( ColorUtil.ColorGreen, ColorUtil.colorToString(Color.green), "Green to String");
    }

    @Test
    public void testGreenFromString() {
        assertEquals( Color.green, ColorUtil.stringToColor(ColorUtil.ColorGreen), "Green from string");
    }

    @Test
    public void testStringFromBlue() {
        assertEquals( ColorUtil.ColorBlue,ColorUtil.colorToString(Color.blue), "Blue to string");
    }

    @Test
    public void testBlueFromString() {
        assertEquals( Color.blue, ColorUtil.stringToColor(ColorUtil.ColorBlue), "Blue from string");
    }

    @Test
    public void testStringFromMagenta() {
        assertEquals( ColorUtil.ColorMagenta, ColorUtil.colorToString(Color.magenta), "Magenta to string");
    }

    @Test
    public void testMagentaFromString() {
        assertEquals( Color.magenta, ColorUtil.stringToColor(ColorUtil.ColorMagenta), "Magenta to string");
    }

    @Test
    public void testStringFromCyan() {
        assertEquals( ColorUtil.ColorCyan, ColorUtil.colorToString(Color.cyan), "Cyan to string");
    }

    @Test
    public void testCyanFromString() {
        assertEquals( Color.cyan, ColorUtil.stringToColor(ColorUtil.ColorCyan), "Cyan to string");
    }

    @Test
    public void testDefaultStringFromColor() {
        assertEquals( ColorUtil.ColorBlack, ColorUtil.colorToString(new Color(42,42,42)), "other to string");
        JUnitAppender.assertErrorMessage("unknown color sent to colorToString");
    }

    @Test
    public void testDefaultColorFromString() {
        Exception ex = assertThrows( IllegalArgumentException.class, () ->
            ColorUtil.stringToColor("other"));
        assertEquals("unknown color text 'other'", ex.getMessage());
        JUnitAppender.assertErrorMessage("unknown color text 'other' sent to stringToColor");
    }

    @Test
    public void testStringFromNull() {
        assertEquals( ColorUtil.ColorTrack, ColorUtil.colorToString(null), "null to string");
    }

    @Test
    public void testColorFromTrack() {
        assertNull( ColorUtil.stringToColor(ColorUtil.ColorTrack), "track from null");
    }

    @Test
    public void testHexStringFromMagenta() {
        assertEquals( ColorUtil.colorToHexString(Color.MAGENTA), "#FF00FF", "hex string from magenta");
    }

    @Test
    public void testHexStringFromNull() {
        assertNull( ColorUtil.colorToHexString(null), "hex string from null");
    }

    @Test
    public void testColorNameForMagenta() {
        assertEquals( ColorUtil.colorToColorName(Color.MAGENTA),ColorUtil.ColorMagenta, "color name for magenta");
    }

    @Test
    public void testColorNameForFFCCFF() {
        assertEquals( "#FFCCFF", ColorUtil.colorToColorName(new Color(255, 204, 255)), "color name for #FFCCFF");
    }

    @Test
    public void testColorNameForFF00FF() {
        assertEquals( ColorUtil.colorToColorName(new Color(255, 0, 255)), ColorUtil.ColorMagenta, "color name for #FF00FF");
    }

    @Test
    public void testColorNameFromNull() {
        assertNull( ColorUtil.colorToColorName(null), "color name from null");
    }

    @BeforeEach
    public void setUp() {
       JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
       JUnitUtil.tearDown();
    }

}
