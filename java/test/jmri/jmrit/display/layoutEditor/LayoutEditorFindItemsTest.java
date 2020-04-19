package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LayoutEditorFindItemsTest {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor e = new LayoutEditor();
        LayoutEditorFindItems t = new LayoutEditorFindItems(e);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(e);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetProfileManager();

        JUnitUtil.initLayoutBlockManager();
    }

    @After
    public void tearDown() {
        InstanceManager.getDefault(ShutDownManager.class).deregister(InstanceManager.getDefault(BlockManager.class).shutDownTask);
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(LayoutEditorFindItemsTest.class);
}
