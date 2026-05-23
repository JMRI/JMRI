package jmri.jmrix.dccpp.swing.exrail;

import java.util.Locale;
import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class.
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class BundleTest {

    @Test
    public void testGoodKeys() {
        Assertions.assertEquals("DCC-EX EXRAIL Automations", Bundle.getMessage("ExrailFrameTitle"));
        Assertions.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout")); // from parent bundle
    }

    @Test
    public void testBadKey() {
        var ex = Assertions.assertThrows(java.util.MissingResourceException.class,
                () -> Bundle.getMessage("FFFFFTTTTTTT"));
        Assertions.assertNotNull(ex);
    }

    @Test
    public void testGoodKeyMessageArg() {
        Assertions.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", new Object[]{}));
    }

    @Test
    public void testLocaleMessage() {
        Assertions.assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
    }
}
