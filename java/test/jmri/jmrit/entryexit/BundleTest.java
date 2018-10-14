package jmri.jmrit.entryexit;

import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class BundleTest  {

    @Test public void testGoodKeyMessage() {
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));  // NOI18N
    }

    @Test public void testBadKeyMessage() {
        try {
            Bundle.getMessage("FFFFFTTTTTTT");  // NOI18N
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");  // NOI18N
    }

    @Test public void testGoodKeyMessageArg() {
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", new Object[]{}));  // NOI18N
        Assert.assertEquals("About Test", Bundle.getMessage("TitleAbout", "Test"));  // NOI18N
    }

    @Test public void testBadKeyMessageArg() {
        try {
            Bundle.getMessage("FFFFFTTTTTTT", new Object[]{});  // NOI18N
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");  // NOI18N
    }

    @Test public void testLocaleMessage() {
        Assert.assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout"));  // NOI18N
    }

    @Test public void testLocaleMessageArg() {
        Assert.assertEquals("Scambio", Bundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));  // NOI18N
        Assert.assertEquals("Informazioni su Test", Bundle.getMessage(Locale.ITALY, "TitleAbout", "Test"));  // NOI18N
    }


}
