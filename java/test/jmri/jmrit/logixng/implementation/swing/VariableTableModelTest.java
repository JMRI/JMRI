package jmri.jmrit.logixng.implementation.swing;

import jmri.jmrit.logixng.tools.swing.LocalVariableTableModel;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test LocalVariableTableModel
 *
 * @author Daniel Bergqvist 2020
 */
public class VariableTableModelTest {

    @Test
    public void testCtor() {
        LocalVariableTableModel obj = new LocalVariableTableModel(null);
        Assertions.assertNotNull(obj);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
