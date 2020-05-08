package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LevelXingView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class LevelXingViewTest extends LayoutTrackViewTest {

    @Test
    public void testCtor() {
        new LevelXingView(xing, layoutEditor);
    }

    LayoutEditor layoutEditor;
    LevelXing xing;
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            layoutEditor = new LayoutEditor();
            
            Point2D point = new Point2D.Double(150.0, 100.0);
 
            xing = new LevelXing("X1", point, layoutEditor);

        }
    }

    @After
    public void tearDown() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        layoutEditor = null;
        xing = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
