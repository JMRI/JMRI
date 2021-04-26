package apps;

import org.junit.Assert;
import org.junit.jupiter.api.*;

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
        Assert.assertThrows(java.util.MissingResourceException.class, () -> ConfigBundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testGoodKeysMessageArg() {
        Assert.assertEquals("File", ConfigBundle.getMessage("MenuFile", new Object[]{}));
        Assert.assertEquals("Turnout", ConfigBundle.getMessage("BeanNameTurnout", new Object[]{}));
    }

    @Test
    public void testBadKeyMessageArg() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> ConfigBundle.getMessage("FFFFFTTTTTTT", new Object[]{}));
    }
}
