package jmri.web.servlet.json;

import java.util.Locale;
import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the JsonBundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class JsonBundleTest {

    @Test
    public void testGoodKeyMessage() {
        Assert.assertEquals("Turnout", JsonBundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKeyMessage() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testGoodKeyMessageArg() {
        Assert.assertEquals("Turnout", JsonBundle.getMessage("BeanNameTurnout", new Object[]{}));
        Assert.assertEquals("About Test", JsonBundle.getMessage("TitleAbout", "Test"));
    }

    @Test
    public void testBadKeyMessageArg() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
    }

    @Test
    public void testLocaleMessage() {
        Assert.assertEquals("Scambio", JsonBundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
    }

    @Test
    public void testLocaleMessageArg() {
        Assert.assertEquals("Scambio", JsonBundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        Assert.assertEquals("Informazioni su Test", JsonBundle.getMessage(Locale.ITALY, "TitleAbout", "Test"));
    }

}
