package jmri.jmrix.can.adapters.gridconnect.net;


import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class BundleTest  {

    @Test public void testGoodKeys() {
        Assert.assertEquals("(none)", Bundle.getMessage("none"));
        Assert.assertEquals("No locomotive detected (301);", Bundle.getMessage("NoLocoDetected"));
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKey() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test public void testGoodKeyMessageArg() {
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", new Object[]{}));
        Assert.assertEquals("About Test", Bundle.getMessage("TitleAbout", "Test"));
    }

    @Test
    public void testBadKeyMessageArg() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
    }

    @Test public void testLocaleMessage() {
        Assert.assertEquals("Scambio", Bundle.getMessage(java.util.Locale.ITALY, "BeanNameTurnout"));
    }

    @Test public void testLocaleMessageArg() {
        Assert.assertEquals("Scambio", Bundle.getMessage(java.util.Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        Assert.assertEquals("Informazioni su Test", Bundle.getMessage(java.util.Locale.ITALY, "TitleAbout", "Test"));
    }

}
