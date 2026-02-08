package apps.startup.configurexml;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 * @author Paul Bender Copyright (C) 2016
 */
public class BundleTest  {

    @Test
    public void testGoodKeysMessage() {
        assertEquals("File", Bundle.getMessage("MenuFile"));
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKeyMessage() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testGoodKeysMessageArg() {
        assertEquals("File", Bundle.getMessage("MenuFile", new Object[]{}));
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", new Object[]{}));
    }

    @Test
    public void testBadKeyMessageArg() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
    }

    @Test
    public void testLocaleMessage() {
        assertEquals("Chiudi", Bundle.getMessage(Locale.ITALY, "ButtonClose"));
    }

    @Test
    public void testLocaleMessageArg() {
        assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        assertEquals("PanelPro 1234, parte del progetto JMRI®", Bundle.getMessage(Locale.ITALY, "PanelProVersionCredit", "1234"));
    }

}
