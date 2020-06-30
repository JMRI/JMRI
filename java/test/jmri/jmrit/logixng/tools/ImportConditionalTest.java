package jmri.jmrit.logixng.tools;

import java.awt.GraphicsEnvironment;
import jmri.Conditional;
import jmri.ConditionalManager;
import jmri.InstanceManager;
import jmri.Logix;
import jmri.LogixManager;
import jmri.jmrit.logixng.ConditionalNG_Manager;
import jmri.jmrit.logixng.LogixNG;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.util.JUnitAppender;
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
public class ImportConditionalTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Logix logix = InstanceManager.getDefault(LogixManager.class).createNewLogix("A new logix for test");  // NOI18N
        Conditional conditional = InstanceManager.getDefault(ConditionalManager.class).createNewConditional(logix.getSystemName()+"C1", "A new conditional for test");  // NOI18N
        LogixNG logixNG = InstanceManager.getDefault(LogixNG_Manager.class).createLogixNG("A new logixNG for test");  // NOI18N
        
        ImportConditional t =
                new ImportConditional(logix, conditional, logixNG,
                        InstanceManager.getDefault(ConditionalNG_Manager.class).getAutoSystemName());
        Assert.assertNotNull("exists",t);
        
        // Remove this when the import tool is completed.
        JUnitAppender.clearBacklog();
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
