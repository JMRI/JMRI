package apps;

import java.util.Locale;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Tests for the ConfigBundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class ConfigBundleTest {

    // This differs to BundleTest in that we are testing ConfigBundle.getMessage,
    // instead of the normal Bundle.getMessage

    @Test
    public void testGoodKeyMessage() {
        assertEquals("File", ConfigBundle.getMessage("MenuFile"));
        assertEquals("Turnout", ConfigBundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKeyMessage() {
        assertThrows(java.util.MissingResourceException.class, () -> ConfigBundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testGoodKeysMessageArg() {
        assertEquals("File", ConfigBundle.getMessage("MenuFile", new Object[]{}));
        assertEquals("Turnout", ConfigBundle.getMessage("BeanNameTurnout", new Object[]{}));
    }

    @Test
    public void testBadKeyMessageArg() {
        assertThrows(java.util.MissingResourceException.class, () -> ConfigBundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
    }

    @Test
    public void testLocaleMessage() {
        assertEquals("Scambio", ConfigBundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
    }

    @Test
    public void testLocaleMessageArg() {
        assertEquals("Scambio", ConfigBundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        assertEquals("Informazioni su Test", ConfigBundle.getMessage(Locale.ITALY, "TitleAbout", "Test"));
    }

}
