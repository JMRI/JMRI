package jmri.jmrit.logixng.tools.swing;

import java.awt.GraphicsEnvironment;

import jmri.InstanceManager;
import jmri.jmrit.logixng.NamedTableManager;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test TimeDiagram
 * 
 * @author Daniel Bergqvist Copyright (C) 2018
 */
public class TableEditorTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        InstanceManager.getDefault(NamedTableManager.class).newInternalTable("IQT1", null, 2, 3);
        TableEditor b = new TableEditor(null, "IQT1");
        Assert.assertNotNull("exists", b);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initLogixNGManager();
    }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TimeDiagramTest.class);

}
