package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.geom.Point2D;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.*;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutLHTurnoutEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class LayoutLHTurnoutEditorTest extends LayoutTurnoutEditorTest {

    @Test
    @Override
    public void testCtor() {

        LayoutLHTurnoutEditor t = new LayoutLHTurnoutEditor(layoutEditor);
        Assertions.assertNotNull(t);
    }

    @Test
    public void testEditLHTurnoutDone() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        LayoutTurnoutEditor editor = new LayoutLHTurnoutEditor(layoutEditor);
        turnoutTestSequence(editor, leftHandLayoutTurnoutView);
    }

    private LayoutLHTurnout leftHandLayoutTurnout = null;
    private LayoutLHTurnoutView leftHandLayoutTurnoutView = null;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

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

    @AfterEach
    @Override
    public void tearDown() {

        if (leftHandLayoutTurnout != null) {
            leftHandLayoutTurnout.remove();
        }

        leftHandLayoutTurnout = null;
        leftHandLayoutTurnoutView = null;

        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutLHTurnoutEditorTest.class);
}
