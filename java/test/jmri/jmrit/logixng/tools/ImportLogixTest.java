package jmri.jmrit.logixng.tools;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test TimeDiagram
 * 
 * @author Daniel Bergqvist Copyright (C) 2019
 */
public class ImportLogixTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Logix logix = InstanceManager.getDefault(LogixManager.class).createNewLogix("A new logix for test");  // NOI18N
        
        ImportLogix b = new ImportLogix(logix);
        Assert.assertNotNull("exists", b);
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
        JUnitUtil.initLogixManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TimeDiagramTest.class);

}
