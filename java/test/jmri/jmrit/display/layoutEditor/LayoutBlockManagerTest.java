package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LayoutBlockManager
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutBlockManagerTest {

    @Test
    public void testCtor() {
        LayoutBlockManager b = new LayoutBlockManager();
        Assert.assertNotNull("exists", b);
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
    // private final static Logger log = LoggerFactory.getLogger(LayoutEditorActionTest.class);
}
