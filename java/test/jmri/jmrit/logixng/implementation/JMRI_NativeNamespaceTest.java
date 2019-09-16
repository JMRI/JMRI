package jmri.jmrit.logixng.implementation;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DefaultLogixNG
 * 
 * @author Daniel Bergqvist 2018
 */
public class JMRI_NativeNamespaceTest {

    @Test
    public void testCtor() {
        Assert.assertNotNull("exists", new JMRI_NativeNamespace());
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNG();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
