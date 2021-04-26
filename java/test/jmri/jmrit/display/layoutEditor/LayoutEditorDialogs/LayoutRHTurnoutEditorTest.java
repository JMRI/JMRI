package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import jmri.jmrit.display.EditorFrameOperator;
import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutRHTurnoutEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LayoutRHTurnoutEditorTest extends LayoutTurnoutEditorTest  {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        new LayoutRHTurnoutEditor(null);
    }

    @Test
    public void testEditRHTurnoutDone() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        LayoutTurnoutEditor editor = new LayoutRHTurnoutEditor(layoutEditor);
        turnoutTestSequence(editor, rightHandLayoutTurnoutView);
    }


    private LayoutEditor layoutEditor = null;
    private LayoutRHTurnout rightHandLayoutTurnout = null;
    private LayoutRHTurnoutView rightHandLayoutTurnoutView = null;

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

            // RH Turnout
            point = MathUtil.add(point, delta);
            rightHandLayoutTurnout = new LayoutRHTurnout("RH Turnout", layoutEditor); //  point, 33.0, 1.1, 1.2,
            rightHandLayoutTurnoutView = new LayoutRHTurnoutView(rightHandLayoutTurnout, point, 33.0, 1.1, 1.2, layoutEditor);
            layoutEditor.addLayoutTrack(rightHandLayoutTurnout, rightHandLayoutTurnoutView);
        }
    }

    @AfterEach
    public void tearDown() {

        if (rightHandLayoutTurnout != null) {
            rightHandLayoutTurnout.remove();
        }

        if (layoutEditor != null) {
            EditorFrameOperator efo = new EditorFrameOperator(layoutEditor);
            efo.closeFrameWithConfirmations();
        }

        rightHandLayoutTurnout = null;
        layoutEditor = null;

        JUnitUtil.resetWindows(false, false);
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHTurnoutEditorTest.class);
}
