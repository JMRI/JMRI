package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutWyeEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutWyeEditorTest extends LayoutTurnoutEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LayoutWyeEditor(null);
    }

    @Test
    public void testEditLHTurnoutDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        LayoutWyeEditor editor = new LayoutWyeEditor(layoutEditor);
        turnoutTestSequence(editor, layoutWye);
    }


    private LayoutEditor layoutEditor = null;
    private LayoutWye layoutWye = null;

    @BeforeEach
    public void setUp() {
        super.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initLayoutBlockManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalSensorManager();
        if (!GraphicsEnvironment.isHeadless()) {

            layoutEditor = new LayoutEditor();
            layoutEditor.setVisible(true);

            Point2D point = new Point2D.Double(150.0, 100.0);
            Point2D delta = new Point2D.Double(50.0, 10.0);

            // Wye
            point = MathUtil.add(point, delta);
            layoutWye = new LayoutWye("Wye", point, 33.0, 1.1, 1.2, layoutEditor);
        }
    }

    @AfterEach
    public void tearDown() {
    
        if (layoutWye != null) {
            layoutWye.remove();
            layoutWye.dispose();
        }

        if (layoutEditor != null) {
            EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
            efo.closeFrameWithConfirmations();
        }

        layoutWye = null;
        layoutEditor = null;

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        super.tearDown();
    }
    
    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutWyeEditorTest.class);
}
