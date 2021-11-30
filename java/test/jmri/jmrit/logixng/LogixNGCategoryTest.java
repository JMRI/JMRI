package jmri.jmrit.logixng;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Category
 * 
 * @author Daniel Bergqvist 2018
 */
public class LogixNGCategoryTest {

    @Test
    public void testEnum() {
        Assert.assertTrue("ITEM".equals(Category.ITEM.name()));
        Assert.assertTrue("COMMON".equals(Category.COMMON.name()));
        Assert.assertTrue("OTHER".equals(Category.OTHER.name()));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
