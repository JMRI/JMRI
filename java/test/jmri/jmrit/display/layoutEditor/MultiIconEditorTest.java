package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Test simple functioning of MultiIconEditor
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class MultiIconEditorTest {

    public void testCtor() {
        MultiIconEditor  t = new MultiIconEditor(5);
        Assert.assertNotNull("exists", t);
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
        // reset the instance manager.
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
    private final static Logger log = LoggerFactory.getLogger(MultiIconEditorTest.class.getName());
}
