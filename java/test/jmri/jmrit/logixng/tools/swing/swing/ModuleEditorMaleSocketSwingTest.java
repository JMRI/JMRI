package jmri.jmrit.logixng.tools.swing.swing;

import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test ModuleEditorMaleSocketSwing
 * 
 * @author Daniel Bergqvist 2021
 */
public class ModuleEditorMaleSocketSwingTest {

    @Test
    public void testCtor() {
        ModuleEditorMaleSocketSwing t = new ModuleEditorMaleSocketSwing();
        Assert.assertNotNull("not null", t);
    }
    
    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
    
}
