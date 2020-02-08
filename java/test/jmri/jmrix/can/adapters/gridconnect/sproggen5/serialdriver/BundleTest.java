package jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver;

import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Bundle class
 *
 * @author Bob Andrew Crosland (C) 2020
 */
public class BundleTest {

    @Test public void testGoodKeys() {
        Assert.assertEquals("(none)", jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.Bundle.getMessage("none"));
        Assert.assertEquals("No locomotive detected (301);", jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.Bundle.getMessage("NoLocoDetected"));
        Assert.assertEquals("Turnout", jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.Bundle.getMessage("BeanNameTurnout"));
    }

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKey() {
            jmri.jmrix.can.adapters.gridconnect.sproggen5.serialdriver.Bundle.getMessage("FFFFFTTTTTTT");
    }

}
