package jmri.jmrit.logixng.implementation.swing;

import jmri.jmrit.logixng.tools.swing.LocalVariableTableModel;

import java.io.IOException;

import jmri.util.JUnitUtil;

import org.junit.*;

/**
 * Test VariableTableModel
 * 
 * @author Daniel Bergqvist 2020
 */
public class VariableTableModelTest {

    @Test
    public void testCtor() {
        LocalVariableTableModel obj = new LocalVariableTableModel(null);
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
