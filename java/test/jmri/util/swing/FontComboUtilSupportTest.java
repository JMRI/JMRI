package jmri.util.swing;

import java.awt.GraphicsEnvironment;
import java.util.List;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the jmri.util.swing.FontComboUtil class.
 *
 * @author	Matthew Harris Copyright 2011
 */
public class FontComboUtilSupportTest {

    // test monospaced font list
    @Test
    public void testMonoSpacedFontList() {
        Assume.assumeFalse("On some completely headless Linuxes, this fails", GraphicsEnvironment.isHeadless());
        List<String> fonts = (new FontComboUtilSupport()).getFonts(FontComboUtil.MONOSPACED);
        Assert.assertFalse("Monospaced font list is not empty", fonts.isEmpty());
        // We can only guarantee the cross-platform fonts
        // so only these are referenced in the test.
        Assert.assertTrue("List contains 'Monospaced'", fonts.contains("Monospaced"));
        Assert.assertFalse("List does not contain 'Serif'", fonts.contains("Serif"));
        Assert.assertFalse("List does not contain 'SansSerif'", fonts.contains("SansSerif"));
    }

    // test proportional font list
    @Test
    public void testProportionalFontList() {
        Assume.assumeFalse("On some completely headless Linuxes, this fails", GraphicsEnvironment.isHeadless());
        List<String> fonts = (new FontComboUtilSupport()).getFonts(FontComboUtil.PROPORTIONAL);
        Assert.assertFalse("Proportional font list is not empty", fonts.isEmpty());
        // We can only guarantee the cross-platform fonts
        // so only these are referenced in the test.
        Assert.assertFalse("List does not contain 'Monospaced'", fonts.contains("Monospaced"));
        Assert.assertTrue("List contains 'Serif'", fonts.contains("Serif"));
        Assert.assertTrue("List contains 'SansSerif'", fonts.contains("SansSerif"));
    }

    // test character font list
    @Test
    public void testCharacterFontList() {
        List<String> fonts = (new FontComboUtilSupport()).getFonts(FontComboUtil.CHARACTER);
        Assert.assertFalse("Character font list is not empty", fonts.isEmpty());
        // We can only guarantee the cross-platform fonts
        // so only these are referenced in the test.
        Assert.assertTrue("List contains 'Monospaced'", fonts.contains("Monospaced"));
        Assert.assertTrue("List contains 'Serif'", fonts.contains("Serif"));
        Assert.assertTrue("List contains 'SansSerif'", fonts.contains("SansSerif"));
    }

    // test all font list
    @Test
    public void testAllFontList() {
        List<String> fonts = (new FontComboUtilSupport()).getFonts(FontComboUtil.ALL);
        Assert.assertFalse("All font list is not empty", fonts.isEmpty());
        // We can only guarantee the cross-platform fonts
        // so only these are referenced in the test.
        Assert.assertTrue("List contains 'Monospaced'", fonts.contains("Monospaced"));
        Assert.assertTrue("List contains 'Serif'", fonts.contains("Serif"));
        Assert.assertTrue("List contains 'SansSerif'", fonts.contains("SansSerif"));
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
}
