package jmri.jmrit.logixng.util.parser;

import java.util.Locale;
import java.util.Map;

import jmri.InstanceManager;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Bundle
 * 
 * @author Daniel Bergqvist 2019
 */
public class FunctionManagerTest {

    @Test
    public void testFunctions() {
        FunctionManager fm = InstanceManager.getDefault(FunctionManager.class);
        
        for (Map.Entry<String, Function> entry : fm.getFunctions().entrySet()) {
            Assert.assertEquals(entry.getKey(), entry.getValue().getName());
            Assert.assertNotNull(entry.getValue().getName());
            Assert.assertNotEquals("", entry.getValue().getName());
            Assert.assertNotNull(entry.getValue().getModule());
            Assert.assertNotEquals("", entry.getValue().getModule());
            Assert.assertNotNull(entry.getValue().getDescription());
            Assert.assertNotEquals("", entry.getValue().getDescription());
        }
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
