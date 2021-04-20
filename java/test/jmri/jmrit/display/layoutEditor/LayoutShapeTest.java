package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import jmri.jmrit.display.layoutEditor.LayoutShape.LayoutShapeType;
import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutShape
 *
 * @author George Warner Copyright (C) 2019
 */
public class LayoutShapeTest {

    private static LayoutEditor layoutEditor = null;
    private LayoutShape ls = null;

    @Test
    public void testNew() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutShape not null", ls);
        Assert.assertEquals("ls.getNumberPoints() equals 4", 4, ls.getNumberPoints());
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutShape not null", ls);

        String lsString = ls.toString();
        Assert.assertNotNull("lsString not null", lsString);
        Assert.assertEquals("LayoutShape Yard Fence", lsString);
    }

    @Test
    public void testGetDisplayName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutShape not null", ls);

        Assert.assertEquals("Shape Yard Fence", ls.getDisplayName());
    }

    @Test
    public void testGetType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutShape not null", ls);

        Assert.assertTrue("ls.getType() is Open", ls.getType() == LayoutShapeType.Open);
    }

    @Test
    public void testSetType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutShape not null", ls);

// compiler won't let us pass invalid type (yay!)
//        ls.setType(LayoutTurnout.NONE); // invalid type
//        jmri.util.JUnitAppender.assertErrorMessage("Invalid Shape Type 0");
        ls.setType(LayoutShapeType.Open);
        Assert.assertTrue("ls.getType() is Open", ls.getType() == LayoutShapeType.Open);

        ls.setType(LayoutShapeType.Closed);
        Assert.assertTrue("ls.getType() is Closed", ls.getType() == LayoutShapeType.Closed);

        ls.setType(LayoutShapeType.Filled);
        Assert.assertTrue("ls.getType() is eFilled", ls.getType() == LayoutShapeType.Filled);
    }

    @Test
    public void testGetBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutShape not null", ls);

        Assert.assertEquals("ls.getBounds() equals...",
                new Rectangle2D.Double(50.0, 100.0, 50.0, 50.0),
                ls.getBounds());
    }

    @Test
    public void testSetCoordsCenter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutShape not null", ls);
        Assert.assertEquals("ls.getNumberPoints() equals 4", 4, ls.getNumberPoints());

        Point2D newCenterPoint = new Point2D.Double(75.0, 150.0);
        ls.setCoordsCenter(newCenterPoint);
        Assert.assertEquals("ls.getCoordsCenter equals...", newCenterPoint, ls.getCoordsCenter());

        ArrayList<LayoutShape.LayoutShapePoint> lspoints = ls.getPoints();

        Assert.assertEquals("ls.getPoint(0) equals...", new Point2D.Double(50.0, 125.0), lspoints.get(0).getPoint());
        Assert.assertEquals("ls.getPoint(1) equals...", new Point2D.Double(100.0, 125.0), lspoints.get(1).getPoint());
        Assert.assertEquals("ls.getPoint(2) equals...", new Point2D.Double(100.0, 175.0), lspoints.get(2).getPoint());
        Assert.assertEquals("ls.getPoint(3) equals...", new Point2D.Double(50.0, 175.0), lspoints.get(3).getPoint());
    }

    @Test
    public void testScaleCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutShape not null", ls);

        ls.scaleCoords(1.5F, 2.5F);
        Assert.assertEquals("ls.getCoordsCenter ",
                new Point2D.Double(112.5, 312.5), ls.getCoordsCenter());

        ArrayList<LayoutShape.LayoutShapePoint> lspoints = ls.getPoints();

        Assert.assertEquals("ls.getPoint(0) equals...", new Point2D.Double(75.0, 250.0), lspoints.get(0).getPoint());
        Assert.assertEquals("ls.getPoint(1) equals...", new Point2D.Double(150.0, 250.0), lspoints.get(1).getPoint());
        Assert.assertEquals("ls.getPoint(2) equals...", new Point2D.Double(150.0, 375.0), lspoints.get(2).getPoint());
        Assert.assertEquals("ls.getPoint(3) equals...", new Point2D.Double(75.0, 375.0), lspoints.get(3).getPoint());
    }

    @Test
    public void testTranslateCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutShape not null", ls);

        ls.translateCoords(15.5F, 25.5F);
        Assert.assertEquals("ls.getCoordsCenter ",
                new Point2D.Double(90.5, 150.5), ls.getCoordsCenter());

        ArrayList<LayoutShape.LayoutShapePoint> lspoints = ls.getPoints();

        Assert.assertEquals("ls.getPoint(0) equals...", new Point2D.Double(65.5, 125.5), lspoints.get(0).getPoint());
        Assert.assertEquals("ls.getPoint(1) equals...", new Point2D.Double(115.5, 125.5), lspoints.get(1).getPoint());
        Assert.assertEquals("ls.getPoint(2) equals...", new Point2D.Double(115.5, 175.5), lspoints.get(2).getPoint());
        Assert.assertEquals("ls.getPoint(3) equals...", new Point2D.Double(65.5, 175.5), lspoints.get(3).getPoint());
    }

    @Test
    public void testFindHitPointType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutShape not null", ls);

        // First: miss
        HitPointType hitType = ls.findHitPointType(MathUtil.zeroPoint2D, true);
        Assert.assertTrue("ls.findHitPointType equals NONE", hitType == HitPointType.NONE);

        // now try hit getCoordsLeft -> SHAPE_CENTER
        hitType = ls.findHitPointType(ls.getCoordsCenter(), true);
        Assert.assertEquals("ls.findHitPointType equals SHAPE_CENTER", HitPointType.SHAPE_CENTER, hitType);
        ///Assert.assertTrue("ls.findHitPointType equals SHAPE_CENTER", hitType == LayoutEditor.HitPointTypes.SHAPE_CENTER);

        ArrayList<LayoutShape.LayoutShapePoint> lspoints = ls.getPoints();

        hitType = ls.findHitPointType(lspoints.get(0).getPoint(), true);
        Assert.assertEquals("ls.findHitPointType(point[0]) equals SHAPE_POINT_OFFSET_MIN", HitPointType.SHAPE_POINT_0, hitType);

        hitType = ls.findHitPointType(lspoints.get(1).getPoint(), true);
        Assert.assertEquals("ls.findHitPointType(point[1]) equals SHAPE_POINT_1", HitPointType.SHAPE_POINT_1, hitType);

        hitType = ls.findHitPointType(lspoints.get(2).getPoint(), true);
        Assert.assertEquals("ls.findHitPointType(point[2]) equals SHAPE_POINT_2", HitPointType.SHAPE_POINT_2, hitType);

        hitType = ls.findHitPointType(lspoints.get(3).getPoint(), true);
        Assert.assertEquals("ls.findHitPointType(point[3]) equals SHAPE_POINT_3", HitPointType.SHAPE_POINT_3, hitType);
    }

    // from here down is testing infrastructure
    @BeforeAll
    public static void beforeClass() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            layoutEditor = new LayoutEditor();
        }
    }

    @AfterAll
    public static void afterClass() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
            layoutEditor = null;
        }
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.deregisterEditorManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            ls = new LayoutShape("Yard Fence", new Point2D.Double(50.0, 100.0), layoutEditor);
            ls.addPoint(new Point2D.Double(100.0, 100.0));
            ls.addPoint(new Point2D.Double(100.0, 150.0));
            ls.addPoint(new Point2D.Double(50.0, 150.0));
        }
    }

    @AfterEach
    public void tearDown() {
        if (ls != null) {
            ls.remove();
            ls.dispose();
            ls = null;
        }
    }
    //private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutShapeTest.class);
}
