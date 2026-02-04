package jmri.jmrit.display.controlPanelEditor.shape;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrit.display.EditorScaffold;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfHeadless
public class DrawEllipseTest {

    EditorScaffold editor;

    @Test
    public void testCTor() {
        ThreadingUtil.runOnGUI( () -> {
            editor.pack();
            editor.setVisible(true);
        });
        DrawEllipse t = new DrawEllipse("newShape", "Ellipse", null, editor, false);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testCTorEdit() {
        ThreadingUtil.runOnGUI( () -> {
            editor.pack();
            editor.setVisible(true);
        });
        PositionableEllipse ps =  new PositionableEllipse(editor);
        DrawEllipse t = new DrawEllipse("editShape", "Ellipse", ps, editor, true);
        assertNotNull( t, "exists");
        JUnitUtil.dispose(t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        editor = new EditorScaffold();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.dispose(editor);
        editor = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DrawEllipseTest.class);

}
