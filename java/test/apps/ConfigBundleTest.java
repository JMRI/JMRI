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

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKeyMessage() {
            ConfigBundle.getMessage("FFFFFTTTTTTT");
    }

    @Test
    public void testGoodKeysMessageArg() {
        Assert.assertEquals("File", ConfigBundle.getMessage("MenuFile", new Object[]{}));
        Assert.assertEquals("Turnout", ConfigBundle.getMessage("BeanNameTurnout", new Object[]{}));
    }

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKeyMessageArg() {
            ConfigBundle.getMessage("FFFFFTTTTTTT", new Object[]{});
    }
}
