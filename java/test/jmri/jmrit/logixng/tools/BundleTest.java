package jmri.jmrit.logixng.tools;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Locale;

import org.junit.jupiter.api.Test;

/**
 * Test Bundle
 * 
 * @author Daniel Bergqvist 2020
 */
public class BundleTest {

    @Test
    public void testGoodKeysMessage() {

        assertEquals( "Generic", Bundle.getMessage("SocketTypeGeneric"));

        assertEquals("Tools", Bundle.getMessage("MenuTools"));
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKeyMessage() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testGoodKeysMessageArg() {

        assertEquals( "Test Bundle bb aa cc", Bundle.getMessage("TestBundle", "aa", "bb", "cc"));

        assertEquals("Tools", Bundle.getMessage("MenuTools", "foo"));
        assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", "foo"));
        assertEquals("About Test", Bundle.getMessage("TitleAbout", "Test"));
    }

    @Test
    public void testBadKeyMessageArg() {
        assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
    }

    @Test
    public void testLocaleMessage() {

        assertEquals( "Generic", Bundle.getMessage(Locale.US, "SocketTypeGeneric"));

        assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
    }

    @Test
    public void testLocaleMessageArg() {

        assertEquals( "Test Bundle bb aa cc", Bundle.getMessage(Locale.US, "TestBundle", "aa", "bb", "cc"));

        assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        assertEquals("Informazioni su Test", Bundle.getMessage(Locale.ITALY, "TitleAbout", "Test"));
    }

}
