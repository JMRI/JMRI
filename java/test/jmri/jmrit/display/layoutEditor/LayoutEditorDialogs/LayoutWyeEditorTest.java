package jmri.jmrit.display.layoutEditor.LayoutEditorDialogs;

import java.awt.geom.Point2D;

import jmri.jmrit.display.layoutEditor.*;
import jmri.util.MathUtil;

import org.junit.Assume;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

/**
 * Test simple functioning of LayoutWyeEditor.
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class LayoutWyeEditorTest extends LayoutTurnoutEditorTest {

    @Test
    @Override
    public void testCtor() {
        LayoutWyeEditor t = new LayoutWyeEditor(layoutEditor);
        Assertions.assertNotNull(t);
    }

    @Test
    public void testEditLHTurnoutDone() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        LayoutWyeEditor editor = new LayoutWyeEditor(layoutEditor);
        turnoutTestSequence(editor, layoutWyeView);
    }

    private LayoutWye layoutWye = null;
    private LayoutWyeView layoutWyeView = null;

    @BeforeEach
    @Override
    public void setUp() {
        super.setUp();

        Point2D point = new Point2D.Double(150.0, 100.0);
        Point2D delta = new Point2D.Double(50.0, 10.0);

        // Wye
        point = MathUtil.add(point, delta);
        layoutWye = new LayoutWye("Wye", layoutEditor);
        layoutWyeView = new LayoutWyeView(layoutWye, point, 33.0, 1.1, 1.2, layoutEditor);
        layoutEditor.addLayoutTrack(layoutWye, layoutWyeView);

    }

    @AfterEach
    @Override
    public void tearDown() {

        if (layoutWye != null) {
            layoutWye.remove();
        }
        layoutWye = null;

        super.tearDown();
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutWyeEditorTest.class);
}
