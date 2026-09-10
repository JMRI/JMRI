package jmri.jmrit.display;

import java.awt.Rectangle;

import javax.swing.JLayeredPane;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests scaling in {@link Editor#repaintTargetPanel(Rectangle)}.
 * <p>
 * Child components of the target panel are laid out in unscaled coordinates
 * while painting is scaled by the paint scale, so the dirty region posted for
 * a child's bounds must be scaled to land on the pixels actually painted.
 *
 * @author Adrien Virolleaud Copyright (C) 2026
 */
@DisabledIfHeadless
public class EditorRepaintTargetPanelTest {

    /**
     * Layered pane recording the dirty region posted by repaintTargetPanel.
     */
    private static class RecordingPane extends JLayeredPane {

        Rectangle lastDirty = null;

        @Override
        public void repaint(int x, int y, int width, int height) {
            lastDirty = new Rectangle(x, y, width, height);
            super.repaint(x, y, width, height);
        }
    }

    private EditorScaffold editor = null;
    private RecordingPane pane = null;

    @Test
    public void testRepaintAtScale1() {
        editor.repaintTargetPanel(new Rectangle(10, 20, 30, 40));
        Rectangle dirty = pane.lastDirty;
        Assertions.assertNotNull( dirty, "dirty region posted");
        Assertions.assertTrue( dirty.contains(new Rectangle(10, 20, 30, 40)),
                "dirty region covers the rectangle");
        Assertions.assertTrue( dirty.width <= 30 + 6 && dirty.height <= 40 + 6,
                "dirty region stays close to the rectangle");
    }

    @Test
    public void testRepaintAtScale2() {
        editor.setPaintScale(2.0);
        editor.repaintTargetPanel(new Rectangle(10, 20, 30, 40));
        Rectangle dirty = pane.lastDirty;
        Assertions.assertNotNull( dirty, "dirty region posted");
        Assertions.assertTrue( dirty.contains(new Rectangle(20, 40, 60, 80)),
                "dirty region covers the rectangle scaled by the paint scale");
        Assertions.assertTrue( dirty.width <= 60 + 6 && dirty.height <= 80 + 6,
                "dirty region stays close to the scaled rectangle");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        editor = new EditorScaffold();
        pane = new RecordingPane();
        editor.setTargetPanel(pane, editor);
    }

    @AfterEach
    public void tearDown() {
        if (editor != null) {
            JUnitUtil.dispose(editor);
            editor = null;
        }
        pane = null;
        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
