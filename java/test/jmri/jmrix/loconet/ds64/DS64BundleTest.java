package jmri.jmrix.loconet.ds64;

import java.util.Locale;
import jmri.jmrix.loconet.ds64.DS64Bundle;
import jmri.jmrix.loconet.ds64.DS64Bundle;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the DS64Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class DS64BundleTest  {

    @Test public void testGoodKeyMessage() {
        Assert.assertEquals("Turnout", DS64Bundle.getMessage("BeanNameTurnout"));
    }

    @Test public void testBadKeyMessage() {
        try {
            DS64Bundle.getMessage("FFFFFTTTTTTT");
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");
    }

    @Test public void testGoodKeyMessageArg() {
        Assert.assertEquals("Turnout", DS64Bundle.getMessage("BeanNameTurnout", new Object[]{}));
        Assert.assertEquals("About Test", DS64Bundle.getMessage("TitleAbout", "Test"));
    }

    @Test public void testBadKeyMessageArg() {
        try {
            DS64Bundle.getMessage("FFFFFTTTTTTT", new Object[]{});
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");
    }

    @Test public void testLocaleMessage() {
        Assert.assertEquals("Scambio", DS64Bundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
    }

    @Test public void testLocaleMessageArg() {
        Assert.assertEquals("Scambio", DS64Bundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        Assert.assertEquals("Informazioni su Test", DS64Bundle.getMessage(Locale.ITALY, "TitleAbout", "Test"));
    }

}
