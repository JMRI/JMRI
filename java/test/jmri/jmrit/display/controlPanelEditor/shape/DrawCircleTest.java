package jmri.jmrit.display.controlPanelEditor.shape;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import jmri.jmrit.display.EditorScaffold;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfHeadless
public class DrawCircleTest {

    private EditorScaffold editor;

    @Test
    public void testCTor() {

        ThreadingUtil.runOnGUI( () -> {
            editor.pack();
            editor.setVisible(true);
        });
        DrawCircle t = new DrawCircle("newShape", "Circle", null, editor, false);
        assertNotNull( t, "exists");
        JUnitUtil.dispose(t);

    }

    @Test
    public void testCTorEdit() {

        ThreadingUtil.runOnGUI( () -> {
            editor.pack();
            editor.setVisible(true);
        });
        PositionableCircle ps =  new PositionableCircle(editor);
        DrawCircle t = new DrawCircle("editShape", "Circle", ps, editor, true);
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

    // private final static Logger log = LoggerFactory.getLogger(DrawCircleTest.class);

}
