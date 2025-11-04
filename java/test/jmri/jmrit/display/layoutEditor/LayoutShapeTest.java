package jmri.jmrit.display.layoutEditor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import jmri.jmrit.display.layoutEditor.LayoutShape.LayoutShapeType;
import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Test simple functioning of LayoutShape
 *
 * @author George Warner Copyright (C) 2019
 */
@DisabledIfHeadless
public class LayoutShapeTest {

    private static LayoutEditor layoutEditor;
    private LayoutShape ls = null;

    @Test
    public void testNew() {
        assertNotNull( layoutEditor, "LayoutEditor exists");
        assertNotNull( ls, "LayoutShape not null");
        assertEquals( 4, ls.getNumberPoints(), "ls.getNumberPoints() equals 4");
    }

    @Test
    public void testLayoutShapeToString() {
        assertNotNull( layoutEditor, "LayoutEditor exists");
        assertNotNull( ls, "LayoutShape not null");

        String lsString = ls.toString();
        assertNotNull( lsString, "lsString not null");
        assertEquals("LayoutShape Yard Fence", lsString);
    }

    @Test
    public void testGetDisplayName() {
        assertNotNull( layoutEditor, "LayoutEditor exists");
        assertNotNull( ls, "LayoutShape not null");

        assertEquals("Shape Yard Fence", ls.getDisplayName());
    }

    @Test
    public void testGetType() {
        assertNotNull( layoutEditor, "LayoutEditor exists");
        assertNotNull( ls, "LayoutShape not null");

        assertSame( LayoutShapeType.Open, ls.getType(), "ls.getType() is Open");
    }

    @Test
    public void testSetType() {
        assertNotNull( layoutEditor, "LayoutEditor exists");
        assertNotNull( ls, "LayoutShape not null");

// compiler won't let us pass invalid type (yay!)
//        ls.setType(LayoutTurnout.NONE); // invalid type
//        jmri.util.JUnitAppender.assertErrorMessage("Invalid Shape Type 0");
        ls.setType(LayoutShapeType.Open);
        assertSame( LayoutShapeType.Open, ls.getType(), "ls.getType() is Open");

        ls.setType(LayoutShapeType.Closed);
        assertSame( LayoutShapeType.Closed, ls.getType(), "ls.getType() is Closed");

        ls.setType(LayoutShapeType.Filled);
        assertSame( LayoutShapeType.Filled, ls.getType(), "ls.getType() is eFilled");
    }

    @Test
    public void testGetBounds() {
        assertNotNull( layoutEditor, "LayoutEditor exists");
        assertNotNull( ls, "LayoutShape not null");

        assertEquals( new Rectangle2D.Double(50.0, 100.0, 50.0, 50.0),
            ls.getBounds(), "ls.getBounds() equals...");
    }

    @Test
    public void testSetCoordsCenter() {
        assertNotNull( layoutEditor, "LayoutEditor exists");
        assertNotNull( ls, "LayoutShape not null");
        assertEquals( 4, ls.getNumberPoints(), "ls.getNumberPoints() equals 4");

        Point2D newCenterPoint = new Point2D.Double(75.0, 150.0);
        ls.setCoordsCenter(newCenterPoint);
        assertEquals( newCenterPoint, ls.getCoordsCenter(), "ls.getCoordsCenter equals...");

        ArrayList<LayoutShape.LayoutShapePoint> lspoints = ls.getPoints();

        assertEquals( new Point2D.Double(50.0, 125.0), lspoints.get(0).getPoint(), "ls.getPoint(0) equals...");
        assertEquals( new Point2D.Double(100.0, 125.0), lspoints.get(1).getPoint(), "ls.getPoint(1) equals...");
        assertEquals( new Point2D.Double(100.0, 175.0), lspoints.get(2).getPoint(), "ls.getPoint(2) equals...");
        assertEquals( new Point2D.Double(50.0, 175.0), lspoints.get(3).getPoint(), "ls.getPoint(3) equals...");
    }

    @Test
    public void testScaleCoords() {
        assertNotNull( layoutEditor, "LayoutEditor exists");
        assertNotNull( ls, "LayoutShape not null");

        ls.scaleCoords(1.5F, 2.5F);
        assertEquals( new Point2D.Double(112.5, 312.5), ls.getCoordsCenter(),
            "ls.getCoordsCenter ");

        ArrayList<LayoutShape.LayoutShapePoint> lspoints = ls.getPoints();

        assertEquals( new Point2D.Double(75.0, 250.0), lspoints.get(0).getPoint(), "ls.getPoint(0) equals...");
        assertEquals( new Point2D.Double(150.0, 250.0), lspoints.get(1).getPoint(), "ls.getPoint(1) equals...");
        assertEquals( new Point2D.Double(150.0, 375.0), lspoints.get(2).getPoint(), "ls.getPoint(2) equals...");
        assertEquals( new Point2D.Double(75.0, 375.0), lspoints.get(3).getPoint(), "ls.getPoint(3) equals...");
    }

    @Test
    public void testTranslateCoords() {
        assertNotNull( layoutEditor, "LayoutEditor exists");
        assertNotNull( ls, "LayoutShape not null");

        ls.translateCoords(15.5F, 25.5F);
        assertEquals( new Point2D.Double(90.5, 150.5), ls.getCoordsCenter(),
            "ls.getCoordsCenter ");

        ArrayList<LayoutShape.LayoutShapePoint> lspoints = ls.getPoints();

        assertEquals( new Point2D.Double(65.5, 125.5), lspoints.get(0).getPoint(), "ls.getPoint(0) equals...");
        assertEquals( new Point2D.Double(115.5, 125.5), lspoints.get(1).getPoint(), "ls.getPoint(1) equals...");
        assertEquals( new Point2D.Double(115.5, 175.5), lspoints.get(2).getPoint(), "ls.getPoint(2) equals...");
        assertEquals( new Point2D.Double(65.5, 175.5), lspoints.get(3).getPoint(), "ls.getPoint(3) equals...");
    }

    @Test
    public void testFindHitPointType() {
        assertNotNull( layoutEditor, "LayoutEditor exists");
        assertNotNull( ls, "LayoutShape not null");

        // First: miss
        HitPointType hitType = ls.findHitPointType(MathUtil.zeroPoint2D, true);
        assertSame( HitPointType.NONE, hitType, "ls.findHitPointType equals NONE");

        // now try hit getCoordsLeft -> SHAPE_CENTER
        hitType = ls.findHitPointType(ls.getCoordsCenter(), true);
        assertEquals( HitPointType.SHAPE_CENTER, hitType, "ls.findHitPointType equals SHAPE_CENTER");
        ///Assert.assertTrue("ls.findHitPointType equals SHAPE_CENTER", hitType == LayoutEditor.HitPointTypes.SHAPE_CENTER);

        ArrayList<LayoutShape.LayoutShapePoint> lspoints = ls.getPoints();

        hitType = ls.findHitPointType(lspoints.get(0).getPoint(), true);
        assertEquals( HitPointType.SHAPE_POINT_0, hitType,
            "ls.findHitPointType(point[0]) equals SHAPE_POINT_OFFSET_MIN");

        hitType = ls.findHitPointType(lspoints.get(1).getPoint(), true);
        assertEquals( HitPointType.SHAPE_POINT_1, hitType,
            "ls.findHitPointType(point[1]) equals SHAPE_POINT_1");

        hitType = ls.findHitPointType(lspoints.get(2).getPoint(), true);
        assertEquals( HitPointType.SHAPE_POINT_2, hitType,
            "ls.findHitPointType(point[2]) equals SHAPE_POINT_2");

        hitType = ls.findHitPointType(lspoints.get(3).getPoint(), true);
        assertEquals( HitPointType.SHAPE_POINT_3, hitType,
            "ls.findHitPointType(point[3]) equals SHAPE_POINT_3");
    }

    // from here down is testing infrastructure
    @BeforeAll
    public static void beforeClass() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        layoutEditor = new LayoutEditor("LayoutShapeTest LE");
    }

    @AfterAll
    public static void afterClass() {
        JUnitUtil.dispose(layoutEditor);
        layoutEditor = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.resetProfileManager();

        ls = new LayoutShape("Yard Fence", new Point2D.Double(50.0, 100.0), layoutEditor);
        ls.addPoint(new Point2D.Double(100.0, 100.0));
        ls.addPoint(new Point2D.Double(100.0, 150.0));
        ls.addPoint(new Point2D.Double(50.0, 150.0));
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
