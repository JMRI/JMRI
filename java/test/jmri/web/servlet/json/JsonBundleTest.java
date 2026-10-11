package jmri.web.servlet.json;

import java.util.Locale;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the JsonBundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class JsonBundleTest {

    @Test
    public void testGoodKeyMessage() {
        assertEquals("Turnout", JsonBundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKeyMessage() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testGoodKeyMessageArg() {
        assertEquals("Turnout", JsonBundle.getMessage("BeanNameTurnout", new Object[]{}));
        assertEquals("About Test", JsonBundle.getMessage("TitleAbout", "Test"));
    }

    @Test
    public void testBadKeyMessageArg() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
    }

    @Test
    public void testLocaleMessage() {
        assertEquals("Scambio", JsonBundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
    }

    @Test
    public void testLocaleMessageArg() {
        assertEquals("Scambio", JsonBundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        assertEquals("Informazioni su Test", JsonBundle.getMessage(Locale.ITALY, "TitleAbout", "Test"));
    }

}
