package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LayoutEditorFloatingToolBarPanel
 *
 * @author	Bob Jacobsen Copyright (C) 2019
 */
public class LayoutEditorFloatingToolBarPanelTest {

    @Test
    public void testCtor() {
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
        JUnitUtil.tearDown();
    }
    // private final static Logger log = LoggerFactory.getLogger(LayoutEditorFloatingToolBarPanelTest.class);
}
