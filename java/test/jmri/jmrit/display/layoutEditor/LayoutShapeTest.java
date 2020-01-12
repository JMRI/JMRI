package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import jmri.jmrit.display.layoutEditor.LayoutShape.LayoutShapeType;
import jmri.util.JUnitUtil;
import jmri.util.MathUtil;
import org.junit.*;

/**
 * Test simple functioning of LayoutShape
 *
 * @author	George Warner Copyright (C) 2019
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

        Assert.assertTrue("ls.getType() is eOpen", ls.getType() == LayoutShapeType.eOpen);
    }

    @Test
    public void testSetType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", layoutEditor);
        Assert.assertNotNull("LayoutShape not null", ls);

// compiler won't let us pass invalid type (yay!)
//        ls.setType(LayoutTurnout.NONE); // invalid type
//        jmri.util.JUnitAppender.assertErrorMessage("Invalid Shape Type 0");
        ls.setType(LayoutShapeType.eOpen);
        Assert.assertTrue("ls.getType() is eOpen", ls.getType() == LayoutShapeType.eOpen);

        ls.setType(LayoutShapeType.eClosed);
        Assert.assertTrue("ls.getType() is eClosed", ls.getType() == LayoutShapeType.eClosed);

        ls.setType(LayoutShapeType.eFilled);
        Assert.assertTrue("ls.getType() is eFilled", ls.getType() == LayoutShapeType.eFilled);
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
        int hitType = ls.findHitPointType(MathUtil.zeroPoint2D, true);
        Assert.assertTrue("ls.findHitPointType equals NONE", hitType == LayoutTrack.NONE);

        // now try hit getCoordsLeft -> SHAPE_CENTER
        hitType = ls.findHitPointType(ls.getCoordsCenter(), true);
        Assert.assertEquals("ls.findHitPointType equals SHAPE_CENTER", LayoutTrack.SHAPE_CENTER, hitType);
        ///Assert.assertTrue("ls.findHitPointType equals SHAPE_CENTER", hitType == LayoutTrack.SHAPE_CENTER);


        ArrayList<LayoutShape.LayoutShapePoint> lspoints = ls.getPoints();

        hitType = ls.findHitPointType(lspoints.get(0).getPoint(), true);
        Assert.assertEquals("ls.findHitPointType(point[0]) equals SHAPE_POINT_OFFSET_MIN", LayoutTrack.SHAPE_POINT_OFFSET_MIN, hitType);

        hitType = ls.findHitPointType(lspoints.get(1).getPoint(), true);
        Assert.assertEquals("ls.findHitPointType(point[1]) equals SHAPE_POINT_OFFSET_MIN + 1", LayoutTrack.SHAPE_POINT_OFFSET_MIN + 1, hitType);

        hitType = ls.findHitPointType(lspoints.get(2).getPoint(), true);
        Assert.assertEquals("ls.findHitPointType(point[2]) equals SHAPE_POINT_OFFSET_MIN + 2", LayoutTrack.SHAPE_POINT_OFFSET_MIN + 2, hitType);

        hitType = ls.findHitPointType(lspoints.get(3).getPoint(), true);
        Assert.assertEquals("ls.findHitPointType(point[3]) equals SHAPE_POINT_OFFSET_MIN + 3", LayoutTrack.SHAPE_POINT_OFFSET_MIN + 3, hitType);
    }

    // from here down is testing infrastructure
    @BeforeClass
    public static void beforeClass() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();
            layoutEditor = new LayoutEditor();
        }
    }

    @AfterClass
    public static void afterClass() {
        if (layoutEditor != null) {
            JUnitUtil.dispose(layoutEditor);
            layoutEditor = null;
        }
        JUnitUtil.tearDown();
    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            ls = new LayoutShape("Yard Fence", new Point2D.Double(50.0, 100.0), layoutEditor);
            ls.addPoint(new Point2D.Double(100.0, 100.0));
            ls.addPoint(new Point2D.Double(100.0, 150.0));
            ls.addPoint(new Point2D.Double(50.0, 150.0));
        }
    }

    @After
    public void tearDown() {
        if (ls != null) {
            ls.remove();
            ls.dispose();
            ls = null;
        }
    }
    //private final static Logger log = LoggerFactory.getLogger(LayoutShapeTest.class);
}
