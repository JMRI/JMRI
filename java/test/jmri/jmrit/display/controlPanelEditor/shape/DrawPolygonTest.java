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
public class DrawPolygonTest {

    private EditorScaffold editor;

    @Test
    public void testCTor() {
        ThreadingUtil.runOnGUI( () -> {
            editor.pack();
            editor.setVisible(true);
        });
        DrawPolygon t = new DrawPolygon("newShape", "Polygon", null, editor, false);
        assertNotNull( t, "exists");
        JUnitUtil.dispose(t);
    }

    @Test
    public void testCTorEdit() {
        ThreadingUtil.runOnGUI( () -> {
            editor.pack();
            editor.setVisible(true);
        });
        PositionablePolygon ps =  new PositionablePolygon(editor, null);
        DrawPolygon t = new DrawPolygon("editShape", "Polygon", ps, editor, true);
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

    // private final static Logger log = LoggerFactory.getLogger(DrawPolygonTest.class);

}
