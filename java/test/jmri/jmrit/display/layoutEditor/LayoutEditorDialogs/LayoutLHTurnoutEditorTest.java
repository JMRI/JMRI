package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutLHTurnoutEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutLHTurnoutEditorTest extends LayoutTurnoutEditorTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LayoutLHTurnoutEditor(null);
    }

    @Test
    public void testEditLHTurnoutDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        LayoutTurnoutEditor editor = new LayoutLHTurnoutEditor(layoutEditor);
        turnoutTestSequence(editor, leftHandLayoutTurnoutView);
    }


    private LayoutEditor layoutEditor = null;
    private LayoutLHTurnout leftHandLayoutTurnout = null;
    private LayoutLHTurnoutView leftHandLayoutTurnoutView = null;

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

            // LH Turnout
            point = MathUtil.add(point, delta);
            leftHandLayoutTurnout = new LayoutLHTurnout("LH Turnout", layoutEditor); // point, 33.0, 1.1, 1.2,
            leftHandLayoutTurnoutView = new LayoutLHTurnoutView(leftHandLayoutTurnout,
                                                point, 33.0, 1.1, 1.2,
                                                layoutEditor);
            layoutEditor.addLayoutTrack(leftHandLayoutTurnout, leftHandLayoutTurnoutView);
        }
    }

    @AfterEach
    public void tearDown() {

        if (leftHandLayoutTurnout != null) {
            leftHandLayoutTurnout.remove();
        }

        if (layoutEditor != null) {
            EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
            efo.closeFrameWithConfirmations();
        }

        leftHandLayoutTurnout = null;
        leftHandLayoutTurnoutView = null;
        layoutEditor = null;

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutLHTurnoutEditorTest.class);
}
