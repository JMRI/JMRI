package jmri.jmrit.logixng.tools.debugger.swing;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DebuggerMaleDigitalExpressionSocketSwing
 * 
 * @author Daniel Bergqvist 2021
 */
public class DebuggerMaleDigitalExpressionSocketSwingTest {

    @Test
    public void testCtor() {
        DebuggerMaleDigitalExpressionSocketSwing t = new DebuggerMaleDigitalExpressionSocketSwing();
        Assert.assertNotNull("not null", t);
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
