package jmri.util.swing;

import java.util.List;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.util.swing.FontComboUtil class.
 *
 * @author	Matthew Harris Copyright 2011
 */
public class FontComboUtilTest extends TestCase {

    // test constants
    public void testFontUtilConstants() {
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
    public void testMonoSpacedFontList() {
        List<String> fonts = FontComboUtil.getFonts(FontComboUtil.MONOSPACED);
        Assert.assertFalse("Monospaced font list is not empty", fonts.isEmpty());
        // We can only guarantee the cross-platform fonts
        // so only these are referenced in the test.
        Assert.assertTrue("List contains 'Monospaced'", fonts.contains("Monospaced"));
        Assert.assertFalse("List does not contain 'Serif'", fonts.contains("Serif"));
        Assert.assertFalse("List does not contain 'SansSerif'", fonts.contains("SansSerif"));
    }

    // test proportional font list
    public void testProportionalFontList() {
        List<String> fonts = FontComboUtil.getFonts(FontComboUtil.PROPORTIONAL);
        Assert.assertFalse("Proportional font list is not empty", fonts.isEmpty());
        // We can only guarantee the cross-platform fonts
        // so only these are referenced in the test.
        Assert.assertFalse("List does not contain 'Monospaced'", fonts.contains("Monospaced"));
        Assert.assertTrue("List contains 'Serif'", fonts.contains("Serif"));
        Assert.assertTrue("List contains 'SansSerif'", fonts.contains("SansSerif"));
    }

    // test character font list
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
    public void testAllFontList() {
        List<String> fonts = FontComboUtil.getFonts(FontComboUtil.ALL);
        Assert.assertFalse("All font list is not empty", fonts.isEmpty());
        // We can only guarantee the cross-platform fonts
        // so only these are referenced in the test.
        Assert.assertTrue("List contains 'Monospaced'", fonts.contains("Monospaced"));
        Assert.assertTrue("List contains 'Serif'", fonts.contains("Serif"));
        Assert.assertTrue("List contains 'SansSerif'", fonts.contains("SansSerif"));
    }

    // from here down is testing infrastructure
    public FontComboUtilTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {FontComboUtilTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(FontComboUtilTest.class);
        return suite;
    }

    //private static final Logger log = LoggerFactory.getLogger(FontComboUtilTest.class.getName());
}
