package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test simple functioning of LayoutEditorAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutEditorActionTest {

    @Test
    public void testCtor() {
        LayoutEditorAction  b = new LayoutEditorAction();
        Assert.assertNotNull("exists", b );
    }

    @Test
    public void testCtorWithParam() {
        LayoutEditorAction  b = new LayoutEditorAction("test");
        Assert.assertNotNull("exists", b );
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
        JUnitUtil.resetWindows(false);
        // reset the instance manager.
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
    private final static Logger log = LoggerFactory.getLogger(LayoutEditorActionTest.class.getName());
}
