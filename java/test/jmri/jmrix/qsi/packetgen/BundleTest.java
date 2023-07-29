package jmri.jmrix.qsi.packetgen;

import java.util.Locale;

import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class BundleTest  {

    @Test public void testGoodKeyMessage() {
        Assertions.assertEquals("(none)", Bundle.getMessage("none"));
        Assertions.assertEquals("No locomotive detected (301);", Bundle.getMessage("NoLocoDetected"));
        Assertions.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKeyMessage() {
        Exception ex = Assertions.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
        Assertions.assertNotNull(ex);
    }

    @Test public void testGoodKeyMessageArg() {
        Assertions.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", new Object[]{}));
        Assertions.assertEquals("About Test", Bundle.getMessage("TitleAbout", "Test"));
    }

    @Test
    public void testBadKeyMessageArg() {
        Exception ex = Assertions.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
        Assertions.assertNotNull(ex);
    }

    @Test public void testLocaleMessage() {
        Assertions.assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
    }

    @Test public void testLocaleMessageArg() {
        Assertions.assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        Assertions.assertEquals("Informazioni su Test", Bundle.getMessage(Locale.ITALY, "TitleAbout", "Test"));
    }

}
