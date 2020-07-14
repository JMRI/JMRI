package jmri.jmrit.roster.swing;


import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the Bundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class BundleTest  {

    @Test public void testGoodKeys() {
        Assert.assertEquals("Roster", Bundle.getMessage("MenuItemRoster"));
        Assert.assertEquals("Tools", Bundle.getMessage("MenuTools"));
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKey() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }


}
