package jmri.jmrit.conditional;

import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Bundle class
 *
 * @author Dave Sand Copyright (C) 2017
 */
public class BundleTest  {

    @Test public void testGoodKeyMessage() {
        Assert.assertEquals("Row", Bundle.getMessage("ColumnLabelRow"));  // NOI18N
    }

    @Test public void testBadKeyMessage() {
        try {
            Bundle.getMessage("FFFFFTTTTTTT");  // NOI18N
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");
    }

    @Test public void testGoodKeyMessageArg() {
        Assert.assertEquals("Row", Bundle.getMessage("ColumnLabelRow", new Object[]{}));  // NOI18N
        Assert.assertEquals("Test \"test\" state is \"2\"", Bundle.getMessage("VarStateDescrpt", "Test", "test", "2"));  // NOI18N
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
        Assert.assertEquals("Zeile", Bundle.getMessage(Locale.GERMANY, "ColumnLabelRow"));  // NOI18N
    }

    @Test public void testLocaleMessageArg() {
        Assert.assertEquals("Zeile", Bundle.getMessage(Locale.GERMANY, "ColumnLabelRow", new Object[]{}));  // NOI18N
        Assert.assertEquals("Prüfung \"prüfung\" Zustand ist 2", Bundle.getMessage(Locale.GERMANY, "VarStateDescrpt", "Prüfung", "prüfung", "2"));  // NOI18N
    }

}
