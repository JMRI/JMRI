package jmri.jmrit.logixng;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test Category
 *
 * @author Daniel Bergqvist 2018
 */
public class LogixNGCategoryTest {

    @Test
    public void testEnum() {
        assertEquals( "ITEM", LogixNG_Category.ITEM.name());
        assertEquals( "COMMON", LogixNG_Category.COMMON.name());
        assertEquals( "OTHER", LogixNG_Category.OTHER.name());
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

}
