package jmri.jmrit.logixng.util;

import jmri.InstanceManager;
// import jmri.Memory;
// import jmri.MemoryManager;
import jmri.jmrit.logixng.*;
import jmri.jmrit.logixng.implementation.DefaultConditionalNG;
import jmri.jmrit.logixng.implementation.DefaultSymbolTable;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test DispatcherTrainInfoManager
 *
 * @author Dave Sand 2021
 */
public class DispatcherTrainInfoManagerTest {

    @Test
    public void testCtor() {
        DispatcherTrainInfoManager t = new DispatcherTrainInfoManager();
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

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DispatcherTrainInfoManagerTest.class);
}
