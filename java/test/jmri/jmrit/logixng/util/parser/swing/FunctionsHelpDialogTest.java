package jmri.jmrit.logixng.util.parser.swing;

import jmri.jmrit.logixng.*;
import jmri.util.JUnitUtil;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test FunctionsHelpDialog
 * 
 * @author Daniel Bergqvist 2021
 */
public class FunctionsHelpDialogTest {

    @Test
    public void testCtor() {
        FunctionsHelpDialog t = new FunctionsHelpDialog();
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
