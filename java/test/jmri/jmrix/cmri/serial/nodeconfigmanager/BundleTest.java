package jmri.jmrix.cmri.serial.nodeconfigmanager;


import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Bundle class
 * Copied from nodeconfig
 *
 * @author Bob Jacobsen Copyright (C) 2012
 * @author Chuck Catania Copyright (C) 2017 
 */
public class BundleTest  {

    @Test public void testGoodKeys() {
        Assert.assertEquals("(none)", Bundle.getMessage("none"));
        Assert.assertEquals("No locomotive detected (301);", Bundle.getMessage("NoLocoDetected"));
        Assert.assertEquals("Turnout", Bundle.getMessage("BeanNameTurnout"));
    }

    @Test(expected = java.util.MissingResourceException.class)
    public void testBadKey() {
            Bundle.getMessage("FFFFFTTTTTTT");
    }

}
