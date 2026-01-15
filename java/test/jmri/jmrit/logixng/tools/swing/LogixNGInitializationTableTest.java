package jmri.jmrit.logixng.tools.swing;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test LogixNGInitializationTable
 *
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class LogixNGInitializationTableTest {

    @Test
    @DisabledIfHeadless
    public void testCTor() {

        LogixNGInitializationTable b = new LogixNGInitializationTable();
        Assertions.assertNotNull( b, "exists");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @AfterEach
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeDiagramTest.class);

}
