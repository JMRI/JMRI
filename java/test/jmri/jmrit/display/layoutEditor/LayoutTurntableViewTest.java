package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;

import jmri.util.JUnitUtil;
import org.junit.*;

/**
 * Test simple functioning of LayoutTurntableView
 *
 * @author Bob Jacobsen Copyright (C) 2016
 */
public class LayoutTurntableViewTest extends LayoutTrackViewTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        new LayoutTurntableView(turntable, layoutEditor);
    }

    // from here down is testing infrastructure
    @BeforeClass
    public static void beforeClass() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            JUnitUtil.resetInstanceManager();
            JUnitUtil.initInternalTurnoutManager();
            JUnitUtil.initInternalSensorManager();
            JUnitUtil.initInternalSignalHeadManager();
        }
    }

    LayoutEditor layoutEditor;
    LayoutTurntable turntable;
    
    @AfterClass
    public static void afterClass() {
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            layoutEditor = new LayoutEditor();
            
            Point2D point = new Point2D.Double(150.0, 100.0);
            //Point2D delta = new Point2D.Double(50.0, 75.0);

            //ltRH = new LayoutRHTurnout("Right Hand", point, 33.0, 1.1, 1.2, layoutEditor);

            //point = MathUtil.add(point, delta);
            //ltLH = new LayoutLHTurnout("Left Hand", point, 66.0, 1.3, 1.4, layoutEditor);

            //point = MathUtil.add(point, delta);
            //ltWY = new LayoutWye("Wye", point, 99.0, 1.5, 1.6, layoutEditor);

            //point = MathUtil.add(point, delta);
            //ltDX = new LayoutDoubleXOver("Double XOver", point, 132.0, 1.7, 1.8, layoutEditor);

            //point = MathUtil.add(point, delta);
            //ltRX = new LayoutRHXOver("Right Hand XOver", point, 165.0, 1.9, 2.0, layoutEditor);

            //point = MathUtil.add(point, delta);
            //ltLX = new LayoutLHXOver("Left Hand XOver", point, 198.0, 2.1, 2.2, layoutEditor);

            turntable = new LayoutTurntable("T1", point, layoutEditor);

        }
    }

    @After
    public void tearDown() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
        }
        layoutEditor = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

}
