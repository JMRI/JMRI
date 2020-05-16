package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.*;

import jmri.JmriException;
import jmri.util.*;
import org.junit.*;

/**
 * Test simple functioning of PositionablePointView
 *
 * @author Bob Jacobsen Copyright (C) 2020
 */
public class PositionablePointViewTest extends LayoutTrackViewTest {

    @Test
    public void testGetCoordsCenter() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp1 = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        Assert.assertNotNull("exists", pp1);
        PositionablePointView pp1v = new PositionablePointView(pp1, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp1v);
        le.addLayoutTrack(pp1, pp1v);
        
        Assert.assertEquals("getCoordsCenter equal to zeroPoint2D", MathUtil.zeroPoint2D, pp1.getCoordsCenter());

        Point2D point2 = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp2 = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        Assert.assertNotNull("exists", pp2);
        PositionablePointView pp2v = new PositionablePointView(pp2, point2, le);
        Assert.assertNotNull("exists", pp2v);
        le.addLayoutTrack(pp2, pp2v);

        Assert.assertEquals("getCoordsCenter equal to {666.6, 999.9}", point2, pp2.getCoordsCenter());
    }

    @Test
    public void testSetCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        PositionablePointView ppv = new PositionablePointView(pp, MathUtil.zeroPoint2D, le);
        Assert.assertNotNull("exists", pp);
        le.addLayoutTrack(pp, ppv);
        Assert.assertEquals("getCoordsCenter equal to zeroPoint2D", MathUtil.zeroPoint2D, ppv.getCoordsCenter());

        Point2D point2 = new Point2D.Double(666.6, 999.9);
        ppv.setCoordsCenter(point2);
        Assert.assertEquals("getCoordsCenter equal to {666.6, 999.9}", point2, ppv.getCoordsCenter());
    }

    @Test
    public void testScaleCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        Point2D point = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        PositionablePointView ppv = new PositionablePointView(pp, point, le);
        Assert.assertNotNull("exists", pp);
        le.addLayoutTrack(pp, ppv);

        ppv.scaleCoords(2.F, 2.F);
        Point2D pointX2 = MathUtil.granulize(MathUtil.multiply(point, 2.0), 1.0);
        Assert.assertEquals("getCoordsCenter equal to {2000.0, 3000.0}", pointX2, ppv.getCoordsCenter());
    }

    @Test
    public void testTranslateCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        Point2D point = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        PositionablePointView ppv = new PositionablePointView(pp, point, le);
        le.addLayoutTrack(pp, ppv);
        Assert.assertNotNull("exists", pp);

        Point2D delta = new Point2D.Float(333.3F, 444.4F);
        ppv.translateCoords((float) delta.getX(), (float) delta.getY());
        Point2D pointX2 = MathUtil.add(point, delta);
        Assert.assertEquals("getCoordsCenter equal to {999.9, 1444.3}", pointX2, ppv.getCoordsCenter());
    }

    @Test
    public void testGetBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        Point2D point = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        PositionablePointView ppv = new PositionablePointView(pp, point, le);
        le.addLayoutTrack(pp, ppv);
        Assert.assertNotNull("exists", pp);

        Rectangle2D bounds = new Rectangle2D.Double(point.getX() - 0.5, point.getY() - 0.5, 1.0, 1.0);
        Assert.assertEquals("getBounds equal to {666.6, 999.9, 0.0, 0.0}", bounds, ppv.getBounds());
    }

    @Test
    public void testMaxWidthAndHeight() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        Point2D point = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.EDGE_CONNECTOR, le);
        PositionablePointView ppv = new PositionablePointView(pp, point, le);
        le.addLayoutTrack(pp, ppv);
        Assert.assertNotNull("exists", pp);

        Assert.assertTrue("maxWidth == 5", pp.maxWidth() == 5);
        Assert.assertTrue("maxHeight == 5", pp.maxHeight() == 5);
    }

    @Test
    public void testFindHitPointType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        Point2D thePoint = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        PositionablePointView ppv = new PositionablePointView(pp, thePoint, le);
        le.addLayoutTrack(pp, ppv);
        Assert.assertNotNull("exists", pp);

        // first, try hit
        HitPointType hitType = pp.findHitPointType(thePoint, true, false);
        Assert.assertTrue("pp.findHitPointType equals POS_POINT", hitType == HitPointType.POS_POINT);

        // Now, try miss
        hitType = pp.findHitPointType(MathUtil.zeroPoint2D, true, false);
        Assert.assertTrue("pp.findHitPointType equals NONE", hitType == HitPointType.NONE);
    }

    @Test
    public void testGetCoordsForConnectionType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("LayoutEditor exists", le);

        Point2D thePoint = new Point2D.Double(666.6, 999.9);
        PositionablePoint pp = new PositionablePoint("test", PositionablePoint.PointType.ANCHOR, le);
        PositionablePointView ppv = new PositionablePointView(pp, thePoint, le);
        le.addLayoutTrack(pp, ppv);
        Assert.assertNotNull("exists", pp);

        // test failure
        Assert.assertEquals("pp.getCoordsForConnectionType(LayoutEditor.HitPointTypes.NONE) == {666.6, 999.9}",
                thePoint, pp.getCoordsForConnectionType(HitPointType.NONE));
        JUnitAppender.assertErrorMessage("test.getCoordsForConnectionType(NONE); Invalid Connection Type");

        // test success
        Assert.assertEquals("pp.getCoordsForConnectionType(LayoutEditor.HitPointTypes.POS_POINT) == {666.6, 999.9}",
                thePoint, pp.getCoordsForConnectionType(HitPointType.POS_POINT));
    }




    LayoutEditor le;
    PositionablePoint pPoint;
    PositionablePointView pPointV;
    
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            le = new LayoutEditor();
            
            Point2D point2D = new Point2D.Double(150.0, 100.0);
 
            pPoint = new PositionablePoint("PP", PositionablePoint.PointType.ANCHOR, le);
            pPointV = new PositionablePointView(pPoint, point2D, le);
            le.addLayoutTrack(pPoint, pPointV);

        }
    }

    @After
    public void tearDown() {
        if (le != null) {
            JUnitUtil.dispose(le);
        }
        le = null;
        pPoint = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
