package apps;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the ConfigBundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class ConfigBundleTest {

    @Test
    public void testGoodKeysMessage() {
        Assert.assertEquals("File", ConfigBundle.getMessage("MenuFile"));
        Assert.assertEquals("Turnout", ConfigBundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKeyMessage() {
        try {
            ConfigBundle.getMessage("FFFFFTTTTTTT");
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");
    }

    @Test
    public void testGoodKeysMessageArg() {
        Assert.assertEquals("File", ConfigBundle.getMessage("MenuFile", new Object[]{}));
        Assert.assertEquals("Turnout", ConfigBundle.getMessage("BeanNameTurnout", new Object[]{}));
    }

    @Test
    public void testBadKeyMessageArg() {
        try {
            ConfigBundle.getMessage("FFFFFTTTTTTT", new Object[]{});
        } catch (java.util.MissingResourceException e) {
            return;
        } // OK
        Assert.fail("No exception thrown");
    }
}
