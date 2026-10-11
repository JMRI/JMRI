package jmri.jmrix.can.cbus.swing.modules.sprogdcc;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of CbusNodeInfoPane
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class BundleTest {

    @Test public void testGoodKeys() {
        Assertions.assertEquals("(none)", Bundle.getMessage("none"));
        Assertions.assertEquals("No locomotive detected (301);", Bundle.getMessage("NoLocoDetected"));
        Assertions.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKey() {
        var ex = Assertions.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
        Assertions.assertNotNull(ex);
    }

    @Test public void testGoodKeyMessageArg() {
        Assertions.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", new Object[]{}));
        Assertions.assertEquals("About Test", Bundle.getMessage("TitleAbout", "Test"));
    }

    @Test
    public void testBadKeyMessageArg() {
        var ex = Assertions.assertThrows(java.util.MissingResourceException.class,
            () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
        Assertions.assertNotNull(ex);
    }

    @Test public void testLocaleMessage() {
        Assertions.assertEquals("Scambio", Bundle.getMessage(java.util.Locale.ITALY, "BeanNameTurnout"));
    }

    @Test public void testLocaleMessageArg() {
        Assertions.assertEquals("Scambio", Bundle.getMessage(java.util.Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        Assertions.assertEquals("Informazioni su Test", Bundle.getMessage(java.util.Locale.ITALY, "TitleAbout", "Test"));
    }

}
