package jmri.jmrit.logixng.util;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test DispatcherActiveTrainManager
 *
 * @author Dave Sand 2021
 */
public class DispatcherActiveTrainManagerTest {

    @Test
    public void testCtor() {
        DispatcherActiveTrainManager t = new DispatcherActiveTrainManager();
        Assertions.assertNotNull( t, "not null");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
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

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DispatcherActiveTrainManagerTest.class);
}
