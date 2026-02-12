package apps.PanelPro;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
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

}
