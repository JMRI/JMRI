package jmri.jmrit.display.layoutEditor;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutEditorFloatingToolBarPanel
 *
 * @author Bob Jacobsen Copyright (C) 2019
 */
@jmri.util.junit.annotations.DisabledIfHeadless
public class LayoutEditorFloatingToolBarPanelTest {

    @Test
    public void testCtor() {
        LayoutEditor le = new LayoutEditor();
        var t = new LayoutEditorFloatingToolBarPanel(le);
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
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutEditorFloatingToolBarPanelTest.class);
}
