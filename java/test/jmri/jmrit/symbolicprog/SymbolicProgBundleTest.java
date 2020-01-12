package jmri.jmrit.symbolicprog;

import org.junit.Assert;
import org.junit.Test;

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

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKey() {
            Bundle.getMessage("FFFFFTTTTTTT");
    }

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKeyMessageArg() {
            Bundle.getMessage("FFFFFTTTTTTT", new Object[]{});  // NOI18N
    }

}
