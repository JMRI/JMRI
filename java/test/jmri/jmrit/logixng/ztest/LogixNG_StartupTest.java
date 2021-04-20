package jmri.jmrit.logixng.ztest;

import java.awt.GraphicsEnvironment;
import java.beans.PropertyVetoException;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import jmri.InstanceManager;
import jmri.jmrit.logixng.LogixNG_Manager;
import jmri.jmrit.logixng.implementation.DefaultLogixNGManager;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.*;
import org.junit.rules.ExpectedException;

/**
 * Tests for the LogixNG_Startup Class
 * @author Dave Sand Copyright (C) 2018
 */
public class LogixNG_StartupTest {

    @Rule
    public final ExpectedException thrown = ExpectedException.none();
    
    @Test
    public void testCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("exists", new LogixNG_Startup());
    }

    @Test
    public void testAction() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        new LogixNG_StartupAction().actionPerformed(null);
        
        // Test that actionPerformed() throws PropertyVetoException
        InstanceManager.setDefault(LogixNG_Manager.class, new MyLogixNG_Manager());
        new LogixNG_StartupAction().actionPerformed(null);
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        try {
            new LogixNG_StartupAction().makePanel();
        } catch (IllegalArgumentException ex) {
            hasThrown.set(true);
        }
        Assert.assertTrue("Exception is thrown", hasThrown.get());
    }

    @Test
    public void testGetTitle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertEquals("LogixNG test action", new LogixNG_Startup().getTitle(LogixNG_StartupAction.class, Locale.US));  // NOI18N
        
        AtomicBoolean hasThrown = new AtomicBoolean(false);
        try {
            new LogixNG_Startup().getTitle(String.class, Locale.US);
        } catch (IllegalArgumentException ex) {
            hasThrown.set(true);
        }
        Assert.assertTrue("Exception is thrown", hasThrown.get());
    }

    @Test
    public void testGetClass() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull(new LogixNG_Startup().getActionClasses());
    }
    
    @Test
    public void testBundle() {
        Assert.assertEquals(
                "strings matches",
                "LogixNG test action",
                Bundle.getMessage("LogixNG_TestAction"));
        
        Assert.assertEquals(
                "strings matches",
                "Test bundle a1 b2",
                Bundle.getMessage("TestBundle", "a1", "b2"));
        
        Assert.assertEquals(
                "strings matches",
                "LogixNG test action",
                Bundle.getMessage(Locale.US, "LogixNG_TestAction"));
        
        Assert.assertEquals(
                "strings matches",
                "Test bundle a1 b2",
                Bundle.getMessage(Locale.US, "TestBundle", "a1", "b2"));
        
        // Test Bundle.retry(Locale, String)
        Assert.assertEquals(
                "strings matches",
                "Item",
                Bundle.getMessage("CategoryItem"));
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        
        JUnitUtil.initInternalSignalHeadManager();
        JUnitUtil.initDefaultSignalMastManager();
        JUnitUtil.initSignalMastLogicManager();
        JUnitUtil.initOBlockManager();
        JUnitUtil.initWarrantManager();
        
        JUnitUtil.initLogixNGManager();
   }

    @After
    public void tearDown() {
        jmri.jmrit.logixng.util.LogixNG_Thread.stopAllLogixNGThreads();
        JUnitUtil.tearDown();
    }
    
    
    
    private class MyLogixNG_Manager extends DefaultLogixNGManager {
        
        @Override
        protected void registerSelf() {
            // We don't want to save config for this class
        }
        
    }
    
}
