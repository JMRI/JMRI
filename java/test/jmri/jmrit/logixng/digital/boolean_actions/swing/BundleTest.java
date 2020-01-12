package jmri.jmrit.logixng.digital.boolean_actions.swing;

import java.util.Locale;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ExpressionTimer
 * 
 * @author Daniel Bergqvist 2018
 */
public class BundleTest {

    @Test
    public void testBundle() {
        Assert.assertTrue("bundle is correct", "Generic".equals(Bundle.getMessage("SocketTypeGeneric")));
        Assert.assertTrue("bundle is correct", "Test Bundle bb aa cc".equals(Bundle.getMessage("TestBundle", "aa", "bb", "cc")));
        Assert.assertTrue("bundle is correct", "Generic".equals(Bundle.getMessage(Locale.US, "SocketTypeGeneric")));
        Assert.assertTrue("bundle is correct", "Test Bundle bb aa cc".equals(Bundle.getMessage(Locale.US, "TestBundle", "aa", "bb", "cc")));
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
