package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of TransitCreationTool
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class TransitCreationToolTest {

    @Test
    public void testCtor() {
        TransitCreationTool  t = new TransitCreationTool();
        Assert.assertNotNull("exists", t );
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        // reset the instance manager.
        JUnitUtil.resetInstanceManager();
    }
 
    @After
    public void tearDown() throws Exception {
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
