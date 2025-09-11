package jmri.util.swing;

import java.util.Locale;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class BundleTest  {

    @Test
    public void testGoodKeysMessage() {
        assertEquals("(symbol)", Bundle.getMessage("FontSymbol"));
        assertEquals("Help", Bundle.getMessage("ButtonHelp"));
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKeyMessage() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testGoodKeysMessageArg() {
        assertEquals("(symbol)", Bundle.getMessage("FontSymbol", "foo"));
        assertEquals("Help", Bundle.getMessage("ButtonHelp", "foo"));
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", "foo"));
        assertEquals("About Test", Bundle.getMessage("TitleAbout", "Test"));
    }

    @Test
    public void testBadKeyMessageArg() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
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
