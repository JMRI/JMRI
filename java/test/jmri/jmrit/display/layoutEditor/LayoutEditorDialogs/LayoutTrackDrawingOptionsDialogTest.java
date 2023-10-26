package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import jmri.util.JUnitUtil;

import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTrackDrawingOptions;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class LayoutTrackDrawingOptionsDialogTest {

    private LayoutEditor le = null;

    @Test
    public void testCTor() {
        LayoutTrackDrawingOptions ltdo = new LayoutTrackDrawingOptions("test");
        LayoutTrackDrawingOptionsDialog t = new LayoutTrackDrawingOptionsDialog(le,false,ltdo);
        Assertions.assertNotNull(t, "exists");
        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        le = new LayoutEditor(this.getClass().getName());
        le.setVisible(true);
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(le);
        // new EditorFrameOperator(le).closeFrameWithConfirmations();
        le.dispose();
        le = null;

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
