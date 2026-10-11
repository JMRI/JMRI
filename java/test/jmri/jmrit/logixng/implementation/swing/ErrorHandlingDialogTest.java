package jmri.jmrit.logixng.implementation.swing;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test ErrorHandlingDialog
 *
 * @author Daniel Bergqvist 2020
 */
public class ErrorHandlingDialogTest {

    @Test
    public void testCtor() {
        ErrorHandlingDialog obj = new ErrorHandlingDialog();
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
