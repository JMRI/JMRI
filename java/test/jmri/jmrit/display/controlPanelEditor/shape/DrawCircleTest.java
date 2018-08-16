package jmri.jmrit.display.controlPanelEditor.shape;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.display.EditorScaffold;
import jmri.jmrit.display.controlPanelEditor.ControlPanelEditor;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class DrawCircleTest {

    EditorScaffold editor;

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ControlPanelEditor frame = new ControlPanelEditor();
        frame.pack();
        frame.setVisible(true);
        DrawCircle t = new DrawCircle("newShape", "Circle", null, frame, false);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(frame);
    }

    public void testCTorEdit() {
        ControlPanelEditor frame = new ControlPanelEditor();
        frame.pack();
        frame.setVisible(true);
        PositionableCircle ps =  new PositionableCircle(frame);
        DrawCircle t = new DrawCircle("editShape", "Circle", ps, frame, true);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(frame);
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        editor = new EditorScaffold();
    }

    @After
    public void tearDown() {
        editor = null;
        jmri.util.JUnitUtil.resetWindows(false, false);  // don't log here.  should be from this class.
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(DrawCircleTest.class);

}
