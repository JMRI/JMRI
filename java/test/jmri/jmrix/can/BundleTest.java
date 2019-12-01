package jmri.jmrix.can;

import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

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

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKey() {
            Bundle.getMessage("FFFFFTTTTTTT");
    }
    @Test public void testGoodKeyMessageArg() {
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", new Object[]{}));
        Assert.assertEquals("About Test", Bundle.getMessage("TitleAbout", "Test"));
    }    
    
    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKeyMessageArg() {
            Bundle.getMessage("FFFFFTTTTTTT", new Object[]{});
    }
    
    @Test public void testLocaleMessage() {
        Assert.assertEquals("CAN Konsole", Bundle.getMessage(Locale.GERMANY, "MenuItemConsole"));
    }

    @Test public void testLocaleMessageArg() {
        Assert.assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        Assert.assertEquals("Informazioni su Test", Bundle.getMessage(Locale.ITALY, "TitleAbout", "Test"));
    }

}
