package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutEditorVerticalToolBarPanel
 *
 * @author George Warner Copyright (C) 2019
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class LayoutEditorVerticalToolBarPanelTest {

    @Test
    public void testCtor() {
        LayoutEditor le = new LayoutEditor();
        var t = new LayoutEditorVerticalToolBarPanel(le);
        Assertions.assertNotNull(t);
        t.dispose();
        JUnitUtil.dispose(le);
    }


    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorVerticalToolBarPanelTest.class);
}
