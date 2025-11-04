package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import jmri.util.JUnitUtil;

import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutTrackDrawingOptions;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfHeadless
public class LayoutTrackDrawingOptionsDialogTest {

    private LayoutEditor le = null;

    @Test
    public void testCTor() {
        LayoutTrackDrawingOptions ltdo = new LayoutTrackDrawingOptions("test");
        LayoutTrackDrawingOptionsDialog t = new LayoutTrackDrawingOptionsDialog(le,false,ltdo);
        Assertions.assertNotNull(t, "exists");
        t.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        le = new LayoutEditor(this.getClass().getName());
        ThreadingUtil.runOnGUI( () -> le.setVisible(true));
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(le);
        // new EditorFrameOperator(le).closeFrameWithConfirmations();
        JUnitUtil.dispose(le);
        le = null;

        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
