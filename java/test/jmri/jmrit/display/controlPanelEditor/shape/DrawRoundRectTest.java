package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.GraphicsEnvironment;

import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;
import org.junit.Assume;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DrawRoundRectTest {

    EditorScaffold editor;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor frame = new ControlPanelEditor();
        frame.pack();
        frame.setVisible(true);
        DrawRoundRect t = new DrawRoundRect("newShape", "roundRect", null, frame, false);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(frame);
    }

    public void testCTorEdit() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor frame = new ControlPanelEditor();
        frame.pack();
        frame.setVisible(true);
        PositionableRoundRect ps =  new PositionableRoundRect(frame);
        DrawRoundRect t = new DrawRoundRect("editShape", "roundRect", ps, frame, true);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(frame);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        editor = new EditorScaffold();
    }

    @AfterEach
    public void tearDown() {
        editor = null;
        jmri.util.JUnitUtil.resetWindows(false, false);  // don't log here.  should be from this class.
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DrawRoundRectTest.class);

}
