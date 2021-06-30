package jmri.jmrit.logixng.implementation.swing;

import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.*;

/**
 * Test LogixNGPreferences
 * 
 * @author Daniel Bergqvist 2020
 */
public class DefaultMaleDigitalBooleanActionSocketSwingTest {

    @Test
    public void testCtor() {
        DefaultMaleDigitalBooleanActionSocketSwing obj = new DefaultMaleDigitalBooleanActionSocketSwing();
        Assert.assertNotNull(obj);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() throws IOException {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
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
