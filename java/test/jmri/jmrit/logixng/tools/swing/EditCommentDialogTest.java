package jmri.jmrit.logixng.tools.swing;

import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test EditCommentDialog
 * 
 * @author Daniel Bergqvist 2021
 */
public class EditCommentDialogTest {

    @Test
    public void testCtor() {
        EditCommentDialog d = new EditCommentDialog();
        Assert.assertNotNull(d);
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
    
}
