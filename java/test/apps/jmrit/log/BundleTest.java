package apps.jmrit.log;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class BundleTest {

    @Test
    public void testGoodKeyMessage() {
        assertEquals("Message:", Bundle.getMessage("LogMessageLabel"));
        assertEquals("Add", Bundle.getMessage("ButtonAddText"));
        assertEquals("Add message to the log file", Bundle.getMessage("LogSendToolTip"));
        assertEquals("Add Log Entry", Bundle.getMessage("LogInputTitle"));
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKeyMessage() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testGoodKeyMessageArg() {
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", new Object[]{}));
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
