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
                "is".equals(Is_IsNot_Enum.IS.toString()));
        Assert.assertTrue("toString is correct",
                "is not".equals(Is_IsNot_Enum.IS_NOT.toString()));
        Assert.assertTrue("Enum is correct",
                Is_IsNot_Enum.IS == Is_IsNot_Enum.valueOf("IS"));
        Assert.assertTrue("Enum is correct",
                Is_IsNot_Enum.IS_NOT == Is_IsNot_Enum.valueOf("IS_NOT"));
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
        JUnitUtil.tearDown();
    }
    
}
