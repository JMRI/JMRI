package jmri.jmrix.loconet.logixng.swing;

import java.util.Locale;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Bundle
 * 
 * @author Daniel Bergqvist 2018
 */
public class BundleTest {

    @Test
    public void testBundle() {
        Assert.assertTrue("bundle is correct", "About {0}".equals(Bundle.getMessage("TitleAbout")));
        Assert.assertTrue("bundle is correct", "About LocoNet".equals(Bundle.getMessage("TitleAbout", "LocoNet")));
        Assert.assertTrue("bundle is correct", "About {0}".equals(Bundle.getMessage(Locale.US, "TitleAbout")));
        Assert.assertTrue("bundle is correct", "About LocoNet".equals(Bundle.getMessage(Locale.US, "TitleAbout", "LocoNet")));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
