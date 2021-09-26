package jmri.jmrit.logixng.util;

import jmri.InstanceManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DispatcherActiveTrainManager
 *
 * @author Dave Sand 2021
 */
public class DispatcherActiveTrainManagerTest {

    @Test
    public void testCtor() {
        DispatcherActiveTrainManager t = new DispatcherActiveTrainManager();
        Assert.assertNotNull("not null", t);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
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

//     private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DispatcherActiveTrainManagerTest.class);
}
