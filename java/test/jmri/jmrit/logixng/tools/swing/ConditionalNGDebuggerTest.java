package jmri.jmrit.logixng.tools.swing;

import jmri.InstanceManager;
import jmri.jmrit.logixng.ConditionalNG;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ConditionalNGDebugger
 * 
 * @author Daniel Bergqvist 2021
 */
public class ConditionalNGDebuggerTest {

    @Test
    public void testCtor() {
        ConditionalNG conditionalNG = InstanceManager.getDefault(ConditionalNG_Manager.class).createConditionalNG(null);
        ConditionalNGDebugger t = new ConditionalNGDebugger(conditionalNG);
        Assert.assertNotNull("not null", t);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
}
