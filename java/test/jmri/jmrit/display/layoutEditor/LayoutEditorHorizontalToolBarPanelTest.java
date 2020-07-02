package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;

import jmri.util.JUnitUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutEditorHorizontalToolBarPanel
 *
 * @author George Warner Copyright (C) 2019
 */
public class LayoutEditorHorizontalToolBarPanelTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LayoutEditor le = new LayoutEditor();
        new LayoutEditorHorizontalToolBarPanel(le);
    }


    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() throws Exception {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() throws Exception {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorHorizontalToolBarPanelTest.class);
}
