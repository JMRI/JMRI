package jmri.jmrit.symbolicprog;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for the SymbolicProgBundle class
 *
 * @author Bob Jacobsen Copyright (C) 2012
 */
public class SymbolicProgBundleTest {

    @Test
    public void testGoodKeys() {
        Assert.assertEquals("Read", Bundle.getMessage("ButtonRead"));
        Assert.assertEquals("Tools", Bundle.getMessage("MenuTools"));
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test
    public void testBadKey() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT"));
    }

    @Test
    public void testBadKeyMessageArg() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> Bundle.getMessage("FFFFFTTTTTTT", new Object[]{}));  // NOI18N
    }

}
