package jmri.jmrit.logixng.tools.debugger.swing;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DebuggerMaleAnalogActionSocketSwing
 * 
 * @author Daniel Bergqvist 2021
 */
public class DebuggerMaleAnalogActionSocketSwingTest {

    @Test
    public void testCtor() {
        DebuggerMaleAnalogActionSocketSwing t = new DebuggerMaleAnalogActionSocketSwing();
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
