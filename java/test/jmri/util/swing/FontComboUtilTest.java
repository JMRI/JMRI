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
public class FontComboUtilTest {

    // test constants
    @Test
    public void testFontComboUtilConstants() {
        Assert.assertTrue("All and Character differ", (FontComboUtil.ALL != FontComboUtil.CHARACTER));
        Assert.assertTrue("All and Monospaced differ", (FontComboUtil.ALL != FontComboUtil.MONOSPACED));
        Assert.assertTrue("All and Proportional differ", (FontComboUtil.ALL != FontComboUtil.PROPORTIONAL));
        Assert.assertTrue("All and Symbol differ", (FontComboUtil.ALL != FontComboUtil.SYMBOL));

        Assert.assertTrue("Character and Monospaced differ", (FontComboUtil.CHARACTER != FontComboUtil.MONOSPACED));
        Assert.assertTrue("Character and Proportional differ", (FontComboUtil.CHARACTER != FontComboUtil.PROPORTIONAL));
        Assert.assertTrue("Character and Symbol differ", (FontComboUtil.CHARACTER != FontComboUtil.SYMBOL));

        Assert.assertTrue("Monospaced and Proportional differ", (FontComboUtil.MONOSPACED != FontComboUtil.PROPORTIONAL));
        Assert.assertTrue("Monospaced and Symbol differ", (FontComboUtil.MONOSPACED != FontComboUtil.SYMBOL));

        Assert.assertTrue("Proportional and Symbol differ", (FontComboUtil.PROPORTIONAL != FontComboUtil.SYMBOL));
    }

    // test monospaced font list
    @Test
    public void testMonoSpacedFontList() {
        Assume.assumeFalse("On some completely headless Linuxes, this fails", GraphicsEnvironment.isHeadless());
        List<String> fonts = FontComboUtil.getFonts(FontComboUtil.MONOSPACED);
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
        List<String> fonts = FontComboUtil.getFonts(FontComboUtil.PROPORTIONAL);
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
        List<String> fonts = FontComboUtil.getFonts(FontComboUtil.CHARACTER);
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
        List<String> fonts = FontComboUtil.getFonts(FontComboUtil.ALL);
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
