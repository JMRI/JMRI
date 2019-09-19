package jmri.jmrit.logixng;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test LogixNG
 * 
 * @author Daniel Bergqvist 2018
 */
public class LogixNG_InstanceManagerTest {

    @Test
    public void testLogixNG_InstanceManager() {
        
        Assert.assertTrue("name is correct", "STANDALONE".equals(LogixNG_InstanceManager.TemplateType.STANDALONE.name()));
        Assert.assertTrue("name is correct", "TEMPLATE".equals(LogixNG_InstanceManager.TemplateType.TEMPLATE.name()));
        Assert.assertTrue("name is correct", "BASED_ON_TEMPLATE".equals(LogixNG_InstanceManager.TemplateType.BASED_ON_TEMPLATE.name()));
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
