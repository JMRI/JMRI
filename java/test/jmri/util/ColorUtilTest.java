package jmri.util;

import java.awt.Color;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.ColorUtil class.
 *
 * @author	Paul Bender Copyright 2016
 */
public class ColorUtilTest extends TestCase {

    public void testBlackFromString() {
        Assert.assertEquals("Black from string", Color.black, ColorUtil.stringToColor("black"));
    }

    public void testStringFromBlack() {
        Assert.assertEquals("Black to string", "black", ColorUtil.colorToString(Color.black));
    }

    public void testDarkGrayFromString() {
        Assert.assertEquals("DarkGray from string", Color.darkGray, ColorUtil.stringToColor("darkGray"));
    }

    public void testGrayFromString() {
        Assert.assertEquals("Gray from string", Color.gray, ColorUtil.stringToColor("gray"));
    }

    public void testStringFromGray() {
        Assert.assertEquals("Gray to string", "gray", ColorUtil.colorToString(Color.gray));
    }

    public void testLightGrayFromString() {
        Assert.assertEquals("LightGray from string", Color.lightGray, ColorUtil.stringToColor("lightGray"));
    }

    public void testStringFromLightGray() {
        Assert.assertEquals("LightGray to string", "lightGray", ColorUtil.colorToString(Color.lightGray));
    }

    public void testStringFromDarkGray() {
        Assert.assertEquals("DarkGray to string", "darkGray", ColorUtil.colorToString(Color.darkGray));
    }

    public void testWhiteFromString() {
        Assert.assertEquals("White from string", Color.white, ColorUtil.stringToColor("white"));
    }

    public void testStringFromWhite() {
        Assert.assertEquals("White to string", "white", ColorUtil.colorToString(Color.white));
    }

    public void testStringFromRed() {
        Assert.assertEquals("Red to String", "red", ColorUtil.colorToString(Color.red));
    }

    public void testRedFromString() {
        Assert.assertEquals("Red from string", Color.red, ColorUtil.stringToColor("red"));
    }

    public void testStringFromPink() {
        Assert.assertEquals("Pink to string", "pink", ColorUtil.colorToString(Color.pink));
    }

    public void testPinkFromString() {
        Assert.assertEquals("Pink from string", Color.pink, ColorUtil.stringToColor("pink"));
    }

    public void testStringFromOrange() {
        Assert.assertEquals("Orange to string", "orange", ColorUtil.colorToString(Color.orange));
    }

    public void testOrangeFromString() {
        Assert.assertEquals("Orange from string", Color.orange, ColorUtil.stringToColor("orange"));
    }

    public void testYellowFromString() {
        Assert.assertEquals("Yellow from string", Color.yellow, ColorUtil.stringToColor("yellow"));
    }

    public void testStringFromYellow() {
        Assert.assertEquals("Yellow to string", "yellow", ColorUtil.colorToString(Color.yellow));
    }

    public void testStringFromGreen() {
        Assert.assertEquals("Green to String", "green", ColorUtil.colorToString(Color.green));
    }

    public void testGreenFromString() {
        Assert.assertEquals("Green from string", Color.green, ColorUtil.stringToColor("green"));
    }

    public void testStringFromBlue() {
        Assert.assertEquals("Blue to string", "blue", ColorUtil.colorToString(Color.blue));
    }

    public void testBlueFromString() {
        Assert.assertEquals("Blue from string", Color.blue, ColorUtil.stringToColor("blue"));
    }

    public void testStringFromMagenta() {
        Assert.assertEquals("Magenta to string", "magenta", ColorUtil.colorToString(Color.magenta));
    }

    public void testMagentaFromString() {
        Assert.assertEquals("Magenta to string", Color.magenta, ColorUtil.stringToColor("magenta"));
    }

    public void testStringFromCyan() {
        Assert.assertEquals("Cyan to string", "cyan", ColorUtil.colorToString(Color.cyan));
    }

    public void testCyanFromString() {
        Assert.assertEquals("Cyan to string", Color.cyan, ColorUtil.stringToColor("cyan"));
    }

    public void testDefaultStringFromColor() {
        Assert.assertEquals("other to string", "black", ColorUtil.colorToString(new Color(42,42,42)));
        jmri.util.JUnitAppender.assertErrorMessage("unknown color sent to colorToString");
    }

    public void testDefaultColorFromString() {
        Assert.assertEquals("other from color", Color.black, ColorUtil.stringToColor("other"));
        jmri.util.JUnitAppender.assertErrorMessage("unknown color text 'other' sent to stringToColor ");
    }

    public void testStringFromNull() {
        Assert.assertEquals("null to string", "track", ColorUtil.colorToString(null));
    }

    public void testColorFromTrack() {
        Assert.assertNull("track from null", ColorUtil.stringToColor("track"));
    }

    public void testHexStringFromMagenta() {
        Assert.assertEquals("hex string from magenta", ColorUtil.colorToHexString(Color.MAGENTA), "#FF00FF");
    }

    public void testHexStringFromNull() {
        Assert.assertNull("hex string from null", ColorUtil.colorToHexString(null));
    }

    public void testColorNameForMagenta() {
        Assert.assertEquals("color name for magenta", ColorUtil.colorToColorName(Color.MAGENTA), "magenta");
    }

    public void testColorNameForFFCCFF() {
        Assert.assertEquals("color name for #FFCCFF", ColorUtil.colorToColorName(new Color(255, 204, 255)), "#FFCCFF");
    }

    public void testColorNameForFF00FF() {
        Assert.assertEquals("color name for #FF00FF", ColorUtil.colorToColorName(new Color(255, 0, 255)), "magenta");
    }

    public void testColorNameFromNull() {
        Assert.assertNull("color name from null", ColorUtil.colorToColorName(null));
    }


    // from here down is infrastructure

    // The minimal setup for log4J
    protected void setUp() throws Exception {
       apps.tests.Log4JFixture.setUp();
       super.setUp();
    }

    protected void tearDown() throws Exception {
       super.tearDown();
       apps.tests.Log4JFixture.tearDown();
    }

    public ColorUtilTest(String s) {
       super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        apps.tests.Log4JFixture.initLogging();
        String[] testCaseName = {"-noloading", ColorUtilTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(ColorUtilTest.class);
        return suite;
    }

}
