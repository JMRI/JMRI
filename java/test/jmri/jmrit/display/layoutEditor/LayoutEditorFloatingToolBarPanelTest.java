package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.ShutDownManager;
import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of LayoutEditorFloatingToolBarPanel
 *
 * @author	Bob Jacobsen Copyright (C) 2019
 */
public class LayoutEditorFloatingToolBarPanelTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor le = new LayoutEditor();
        new LayoutEditorFloatingToolBarPanel(le);
    }


    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        InstanceManager.getDefault(ShutDownManager.class).deregister(InstanceManager.getDefault(BlockManager.class).shutDownTask);
        JUnitUtil.tearDown();
    }
    // private final static Logger log = LoggerFactory.getLogger(LayoutEditorFloatingToolBarPanelTest.class);
}
