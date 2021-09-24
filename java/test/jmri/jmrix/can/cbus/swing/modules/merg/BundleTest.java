package jmri.jmrix.can.cbus.swing.modules.merg;

import java.util.Locale;

import org.junit.*;

/**
 * Test simple functioning of CbusNodeInfoPane
 *
 * @author Andrew Crosland Copyright (C) 2021
 */
public class BundleTest {

    @org.junit.jupiter.api.Test public void testGoodKeys() {
        Assert.assertEquals("(none)", jmri.jmrix.can.cbus.swing.modules.merg.Bundle.getMessage("none"));
        Assert.assertEquals("No locomotive detected (301);", jmri.jmrix.can.cbus.swing.modules.merg.Bundle.getMessage("NoLocoDetected"));
        Assert.assertEquals("Turnout", jmri.jmrix.can.cbus.swing.modules.merg.Bundle.getMessage("BeanNameTurnout"));
    }

    @org.junit.jupiter.api.Test
    public void testBadKey() {
        Assert.assertThrows(java.util.MissingResourceException.class, () -> jmri.jmrix.can.cbus.swing.modules.merg.Bundle.getMessage("FFFFFTTTTTTT"));
    }
    
    @org.junit.jupiter.api.Test public void testLocaleMessage() {
        Assert.assertEquals("Scambio", jmri.jmrix.can.cbus.swing.modules.merg.Bundle.getMessage(Locale.ITALY, "BeanNameTurnout"));
    }

    @org.junit.jupiter.api.Test public void testLocaleMessageArg() {
        Assert.assertEquals("Scambio", jmri.jmrix.can.cbus.swing.modules.merg.Bundle.getMessage(Locale.ITALY, "BeanNameTurnout", new Object[]{}));
        Assert.assertEquals("Informazioni su Test", jmri.jmrix.can.cbus.swing.modules.merg.Bundle.getMessage(Locale.ITALY, "TitleAbout", "Test"));
    }
    
}
