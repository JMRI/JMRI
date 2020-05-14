package jmri.jmrix.can.cbus.swing.modeswitcher;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Bundle class
 *
 * @author Andrew Crosland (C) 2020
 */
public class BundleTest {
    @Test public void testGoodKeyMessage() {
        Assert.assertEquals("Node Number", jmri.jmrix.can.cbus.swing.modeswitcher.Bundle.getMessage("BootNodeNumber"));
    }

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKeyMessage() {
            jmri.jmrix.can.cbus.swing.modeswitcher.Bundle.getMessage("FFFFFTTTTTTT");
    }

    @Test public void testGoodKeyMessageArg() {
        Assert.assertEquals("Failed to enter boot mode for node 1234", jmri.jmrix.can.cbus.swing.modeswitcher.Bundle.getMessage("BootEntryFailed", "1234"));
    }

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKeyMessageArg() {
            jmri.jmrix.can.cbus.swing.modeswitcher.Bundle.getMessage("FFFFFTTTTTTT", new Object[]{});
    }

    
}
