package jmri.jmrix.can.cbus.swing.modeswitcher;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class
 *
 * @author Andrew Crosland (C) 2020
 */
public class BundleTest {
    @Test public void testGoodKeyMessage() {
        Assert.assertEquals("Node Number", Bundle.getMessage("BootNodeNumber"));
    }

    @Test
    public void testBadKeyMessage() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test public void testGoodKeyMessageArg() {
        Assert.assertEquals("Failed to enter boot mode for node 1234", Bundle.getMessage("BootEntryFailed", "1234"));
    }

    @Test
    public void testBadKeyMessageArg() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
    }

    @Test public void testLocaleMessage() {
        Assertions.assertEquals("Scambio", Bundle.getMessage(java.util.Locale.ITALY, "BeanNameTurnout"));
    }

    @Test public void testLocaleMessageArg() {
        Assertions.assertEquals("Scambio", Bundle.getMessage(java.util.Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        Assertions.assertEquals("Informazioni su Test", Bundle.getMessage(java.util.Locale.ITALY, "TitleAbout", "Test"));
    }

}
