package jmri.jmrit.consisttool;

import java.util.Locale;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class BundleTest  {

    @Test public void testGoodKeysMessage() {
        Assert.assertEquals("Tools", Bundle.getMessage("MenuTools"));
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test public void testBadKeyMessage() {
        try {
            Bundle.getMessage("FFFFFTTTTTTT");
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");
    }

    @Test public void testGoodKeysMessageArg() {
        Assert.assertEquals("Tools", Bundle.getMessage("MenuTools", "foo"));
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", "foo"));
        Assert.assertEquals("About Test", Bundle.getMessage("TitleAbout", "Test"));
    }

    @Test public void testBadKeyMessageArg() {
        try {
            Bundle.getMessage("FFFFFTTTTTTT", "foo");
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");
    }


}
