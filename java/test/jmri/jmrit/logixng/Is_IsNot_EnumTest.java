package jmri.jmrit.logixng;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test SwingToolsTest
 * 
 * @author Daniel Bergqvist 2019
 */
public class Is_IsNot_EnumTest {

    @Test
    public void testEnum() {
        Assert.assertTrue("toString is correct",
                "is".equals(Is_IsNot_Enum.Is.toString()));
        Assert.assertTrue("toString is correct",
                "is not".equals(Is_IsNot_Enum.IsNot.toString()));
        Assert.assertTrue("Enum is correct",
                Is_IsNot_Enum.Is == Is_IsNot_Enum.valueOf("Is"));
        Assert.assertTrue("Enum is correct",
                Is_IsNot_Enum.IsNot == Is_IsNot_Enum.valueOf("IsNot"));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
