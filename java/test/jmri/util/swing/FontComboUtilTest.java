package jmri.util.swing;

import java.util.List;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the jmri.util.swing.FontComboUtil class.
 *
 * @author Matthew Harris Copyright 2011
 */
public class FontComboUtilTest {

    // test constants
    @Test
    public void testFontComboUtilConstants() {
        assertTrue( (FontComboUtil.ALL != FontComboUtil.CHARACTER), "All and Character differ");
        assertTrue( (FontComboUtil.ALL != FontComboUtil.MONOSPACED), "All and Monospaced differ");
        assertTrue( (FontComboUtil.ALL != FontComboUtil.PROPORTIONAL), "All and Proportional differ");
        assertTrue( (FontComboUtil.ALL != FontComboUtil.SYMBOL), "All and Symbol differ");

        assertTrue( (FontComboUtil.CHARACTER != FontComboUtil.MONOSPACED), "Character and Monospaced differ");
        assertTrue( (FontComboUtil.CHARACTER != FontComboUtil.PROPORTIONAL), "Character and Proportional differ");
        assertTrue( (FontComboUtil.CHARACTER != FontComboUtil.SYMBOL), "Character and Symbol differ");

        assertTrue( (FontComboUtil.MONOSPACED != FontComboUtil.PROPORTIONAL), "Monospaced and Proportional differ");
        assertTrue( (FontComboUtil.MONOSPACED != FontComboUtil.SYMBOL), "Monospaced and Symbol differ");

        assertTrue( (FontComboUtil.PROPORTIONAL != FontComboUtil.SYMBOL), "Proportional and Symbol differ");
    }

    // test monospaced font list
    @Test
    @DisabledIfHeadless("On some completely headless Linuxes, this fails")
    public void testMonoSpacedFontList() {
        List<String> fonts = FontComboUtil.getFonts(FontComboUtil.MONOSPACED);
        assertFalse( fonts.isEmpty(), "Monospaced font list is not empty");
        // We can only guarantee the cross-platform fonts
        // so only these are referenced in the test.
        assertTrue( fonts.contains("Monospaced"), "List contains 'Monospaced'");
        assertFalse( fonts.contains("Serif"), "List does not contain 'Serif'");
        assertFalse( fonts.contains("SansSerif"), "List does not contain 'SansSerif'");
    }

    // test proportional font list
    @Test
    @DisabledIfHeadless("On some completely headless Linuxes, this fails")
    public void testProportionalFontList() {
        List<String> fonts = FontComboUtil.getFonts(FontComboUtil.PROPORTIONAL);
        assertFalse( fonts.isEmpty(), "Proportional font list is not empty");
        // We can only guarantee the cross-platform fonts
        // so only these are referenced in the test.
        assertFalse( fonts.contains("Monospaced"), "List does not contain 'Monospaced'");
        assertTrue( fonts.contains("Serif"), "List contains 'Serif'");
        assertTrue( fonts.contains("SansSerif"), "List contains 'SansSerif'");
    }

    // test character font list
    @Test
    public void testCharacterFontList() {
        List<String> fonts = FontComboUtil.getFonts(FontComboUtil.CHARACTER);
        assertFalse( fonts.isEmpty(), "Character font list is not empty");
        // We can only guarantee the cross-platform fonts
        // so only these are referenced in the test.
        assertTrue( fonts.contains("Monospaced"), "List contains 'Monospaced'");
        assertTrue( fonts.contains("Serif"), "List contains 'Serif'");
        assertTrue( fonts.contains("SansSerif"), "List contains 'SansSerif'");
    }

    // test all font list
    @Test
    public void testAllFontList() {
        List<String> fonts = FontComboUtil.getFonts(FontComboUtil.ALL);
        assertFalse( fonts.isEmpty(), "All font list is not empty");
        // We can only guarantee the cross-platform fonts
        // so only these are referenced in the test.
        assertTrue( fonts.contains("Monospaced"), "List contains 'Monospaced'");
        assertTrue( fonts.contains("Serif"), "List contains 'Serif'");
        assertTrue( fonts.contains("SansSerif"), "List contains 'SansSerif'");
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
