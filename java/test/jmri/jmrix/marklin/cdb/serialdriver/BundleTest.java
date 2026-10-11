package jmri.jmrix.marklin.cdb.serialdriver;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class BundleTest  {

    @Test
    public void testGoodKeys() {
        assertEquals("(none)", Bundle.getMessage("none"));
        assertEquals("No locomotive detected (301);", Bundle.getMessage("NoLocoDetected"));
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKey() {
        var ex = assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
        assertNotNull(ex);
    }

    @Test
    public void testGoodKeyMessageArg() {
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", new Object[]{}));
        assertEquals("About Test", Bundle.getMessage("TitleAbout", "Test"));
    }

    @Test
    public void testBadKeyMessageArg() {
        var ex = assertThrows(java.util.MissingResourceException.class,
            () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
        assertNotNull(ex);
    }

    @Test
    public void testLocaleMessage() {
        assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
    }

    @Test
    public void testLocaleMessageArg() {
        assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        assertEquals("Informazioni su Test", Bundle.getMessage(Locale.ITALY, "TitleAbout", "Test"));
    }

}
