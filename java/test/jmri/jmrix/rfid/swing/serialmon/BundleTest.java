package jmri.jmrix.rfid.swing.serialmon;

import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class BundleTest  {

    @Test public void testGoodKeys() {
        Assertions.assertEquals("(none)", Bundle.getMessage("none"));
        Assertions.assertEquals("No locomotive detected (301);", Bundle.getMessage("NoLocoDetected"));
        Assertions.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKey() {
        Assertions.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test public void testGoodKeyMessageArg() {
        Assertions.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", new Object[]{}));
        Assertions.assertEquals("About Test", Bundle.getMessage("TitleAbout", "Test"));
    }

    @Test
    public void testBadKeyMessageArg() {
        Assertions.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
    }

    @Test public void testLocaleMessage() {
        Assertions.assertEquals("Scambio", Bundle.getMessage(java.util.Locale.ITALY, "BeanNameTurnout"));
    }

    @Test public void testLocaleMessageArg() {
        Assertions.assertEquals("Scambio", Bundle.getMessage(java.util.Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        Assertions.assertEquals("Informazioni su Test", Bundle.getMessage(java.util.Locale.ITALY, "TitleAbout", "Test"));
    }

}
