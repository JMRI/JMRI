package jmri.util.exceptionhandler;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class BundleTest {

    @Test
    public void testGoodKeysMessage() {
        Assert.assertEquals("Help", Bundle.getMessage("ButtonHelp"));
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKeyMessage() {
            Bundle.getMessage("FFFFFTTTTTTT");
    }

    @Test
    public void testGoodKeysMessageArg() {
        Assert.assertEquals("Help", Bundle.getMessage("ButtonHelp", "foo"));
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout", "foo"));
        Assert.assertEquals("About Test", Bundle.getMessage("TitleAbout", "Test"));
    }

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKeyMessageArg() {
            Bundle.getMessage("FFFFFTTTTTTT", "foo");
    }

}
