package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.geom.Point2D;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutRHTurnoutEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutRHTurnoutEditorTest extends LayoutTurnoutEditorTest  {

    @Test
    @Override
    public void testCtor() {

        LayoutRHTurnoutEditor t = new LayoutRHTurnoutEditor(layoutEditor);
        Assertions.assertNotNull(t);
    }

    @Test
    public void testEditRHTurnoutDone() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        LayoutTurnoutEditor editor = new LayoutRHTurnoutEditor(layoutEditor);
        turnoutTestSequence(editor, rightHandLayoutTurnoutView);
    }

    private LayoutRHTurnout rightHandLayoutTurnout = null;
    private LayoutRHTurnoutView rightHandLayoutTurnoutView = null;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        Point2D point = new Point2D.Double(150.0, 100.0);
        Point2D delta = new Point2D.Double(50.0, 10.0);

        // RH Turnout
        point = MathUtil.add(point, delta);
        rightHandLayoutTurnout = new LayoutRHTurnout("RH Turnout", layoutEditor); //  point, 33.0, 1.1, 1.2,
        rightHandLayoutTurnoutView = new LayoutRHTurnoutView(rightHandLayoutTurnout, point, 33.0, 1.1, 1.2, layoutEditor);
        layoutEditor.addLayoutTrack(rightHandLayoutTurnout, rightHandLayoutTurnoutView);

    }

    @AfterEach
    @Override
    public void tearDown() {

        if (rightHandLayoutTurnout != null) {
            rightHandLayoutTurnout.remove();
        }

        rightHandLayoutTurnout = null;

        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutRHTurnoutEditorTest.class);
}
