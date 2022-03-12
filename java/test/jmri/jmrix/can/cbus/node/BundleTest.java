package jmri.jmrix.can.cbus.node;

import java.util.Locale;
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
        Assert.assertEquals("Look for ON or OFF events", Bundle.getMessage("AllEventsTooltip"));
        Assert.assertEquals("Invalid Dynamic Priority Value", Bundle.getMessage("DynPriErrorDialog"));
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
        Assert.assertEquals("Tabella Eventi", Bundle.getMessage(Locale.ITALY, "MenuItemEventTable"));
    }

    @Test public void testLocaleMessageArg() {
        Assert.assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        Assert.assertEquals("Informazioni su Test", Bundle.getMessage(Locale.ITALY, "TitleAbout", "Test"));
    }
    
}
