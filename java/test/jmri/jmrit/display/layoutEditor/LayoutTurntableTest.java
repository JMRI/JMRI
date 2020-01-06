package jmri.jmrit.display.layoutEditor;

import java.awt.GraphicsEnvironment;
import java.awt.geom.*;
import java.util.*;
import javax.annotation.CheckForNull;
import jmri.*;
import jmri.util.*;
import jmri.util.junit.rules.RetryRule;
import org.junit.*;
import org.junit.rules.Timeout;
import org.netbeans.jemmy.QueueTool;
import org.netbeans.jemmy.operators.Operator;

/**
 * Test simple functioning of LayoutTurntable
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class LayoutTurntableTest {

    @Rule   //10 second timeout for methods in this test class.
    public Timeout globalTimeout = Timeout.seconds(10);

    @Rule   //allow 2 retries of intermittent tests
    public RetryRule retryRule = new RetryRule(2);

    private static Operator.StringComparator stringComparator = null;

    private static LayoutEditor layoutEditor = null;
    private static LayoutTurntable layoutTurntable = null;

    private static LayoutBlock layoutBlock1 = null;
    private static LayoutBlock layoutBlock2 = null;
    private static LayoutBlock layoutBlock3 = null;
    private static LayoutBlock layoutBlock4 = null;

    private static PositionablePoint a1 = null;
    private static PositionablePoint a2 = null;
    private static PositionablePoint a3 = null;
    private static PositionablePoint a4 = null;

    private static TrackSegment ts1 = null;
    private static TrackSegment ts2 = null;
    private static TrackSegment ts3 = null;
    private static TrackSegment ts4 = null;

    private static String layoutTurntableName = "TUR1";
    private static Point2D layoutTurntablePoint = new Point2D.Double(200.0, 100.0);

    @Test
    public void testNew() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);
    }

    @Test
    public void testToString() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        String ltString = layoutTurntable.toString();
        Assert.assertNotNull("ltString is null", ltString);
        Assert.assertEquals("LayoutTurntable " + layoutTurntableName, ltString);
    }

    @Test
    public void testGetSetRadius() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        //the default is 25
        Assert.assertEquals("layoutTurntable.getRadius()", 25, layoutTurntable.getRadius(), 0.5);
        layoutTurntable.setRadius(33);
        Assert.assertEquals("layoutTurntable.getRadius()", 33, layoutTurntable.getRadius(), 0.5);
    }

    @Test
    public void testGetBounds() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        Assert.assertEquals("layoutTurntable.getBounds()",
                new Rectangle2D.Double(200.0, 67.95706005997576, 37.0, 50.54293994002424),
                layoutTurntable.getBounds());
    }

    @Test
    public void testGetRayConnectIndexed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        for (int idx = 0; idx < 4; idx++) {
            Assert.assertNull("layoutTurntable.getRayConnectIndexed(" + idx + ") is not null",
                    layoutTurntable.getRayConnectIndexed(idx));

        }

        setupTracks();

        Assert.assertEquals("layoutTurntable.getRayConnectIndexed(0)",
                ts1, layoutTurntable.getRayConnectIndexed(0));
        Assert.assertEquals("layoutTurntable.getRayConnectIndexed(1)",
                ts2, layoutTurntable.getRayConnectIndexed(1));
        Assert.assertEquals("layoutTurntable.getRayConnectIndexed(2)",
                ts3, layoutTurntable.getRayConnectIndexed(2));
        Assert.assertEquals("layoutTurntable.getRayConnectIndexed(3)",
                ts4, layoutTurntable.getRayConnectIndexed(3));
    }

    @Test
    public void testGetRayConnectOrdered() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        for (int idx = 0; idx < 4; idx++) {
            Assert.assertNull("layoutTurntable.getRayConnectOrdered(" + idx + ") is not null",
                    layoutTurntable.getRayConnectOrdered(idx));

        }

        setupTracks();

        Assert.assertEquals("layoutTurntable.getRayConnectOrdered(0)",
                ts1, layoutTurntable.getRayConnectOrdered(0));
        Assert.assertEquals("layoutTurntable.getRayConnectOrdered(1)",
                ts2, layoutTurntable.getRayConnectOrdered(1));
        Assert.assertEquals("layoutTurntable.getRayConnectOrdered(2)",
                ts3, layoutTurntable.getRayConnectOrdered(2));
        Assert.assertEquals("layoutTurntable.getRayConnectOrdered(3)",
                ts4, layoutTurntable.getRayConnectOrdered(3));
    }

    @Test
    public void testSetRayConnect() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        for (int idx = 0; idx < 4; idx++) {
            layoutTurntable.setRayConnect(null, idx);
            Assert.assertNull("layoutTurntable.getRayConnectOrdered(" + idx + ") is not null",
                    layoutTurntable.getRayConnectOrdered(idx));

        }
    }

    @Test
    public void testGetNumberRays() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        Assert.assertEquals("layoutTurntable.getNumberRays()",
                4, layoutTurntable.getNumberRays());
    }

    @Test
    public void testGetRayIndex() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        for (int idx = 0; idx < 4; idx++) {
            Assert.assertEquals("layoutTurntable.getRayIndex(" + idx + ")",
                    idx, layoutTurntable.getRayIndex(idx));
        }
    }

    @Test
    public void testGetRayAngle() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        for (int idx = 0; idx < 4; idx++) {
            Assert.assertEquals("layoutTurntable.getRayAngle(" + idx + ")",
                    (idx + 1) * 30, layoutTurntable.getRayAngle(idx), 0.5);
        }
    }

    @Test
    public void testGetSetRayTurnoutNameState() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        //invalid index
        layoutTurntable.setRayTurnout(5, null, Turnout.THROWN);
        JUnitAppender.assertErrorMessage("Attempt to add Turnout control to a non-existant ray track");

        for (int idx = 0; idx < 4; idx++) {
            layoutTurntable.setRayTurnout(idx, null, Turnout.THROWN);
            Assert.assertNull("layoutTurntable.getRayTurnoutName(" + idx + ")",
                    layoutTurntable.getRayTurnoutName(idx));
            Assert.assertNull("layoutTurntable.getRayTurnout(" + idx + ")",
                    layoutTurntable.getRayTurnout(idx));
            Assert.assertEquals("layoutTurntable.getRayTurnoutState(" + idx + ")",
                    Turnout.THROWN, layoutTurntable.getRayTurnoutState(idx));
        }
    }

    @Test
    public void testIsSetRayDisabled() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        //invalid index
        Assert.assertFalse("layoutTurntable.isRayDisabled(5)", layoutTurntable.isRayDisabled(5));

        for (int idx = 0; idx < 4; idx++) {
            layoutTurntable.setRayDisabled(idx, true);
            Assert.assertTrue("layoutTurntable.isRayDisabled(" + idx + ")", layoutTurntable.isRayDisabled(idx));
            layoutTurntable.setRayDisabled(idx, false);
            Assert.assertFalse("layoutTurntable.isRayDisabled(" + idx + ")", layoutTurntable.isRayDisabled(idx));
        }
    }

    @Test
    public void testIsSetRayDisabledWhenOccupied() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        //invalid index
        Assert.assertFalse("layoutTurntable.isRayDisabledWhenOccupied(5)", layoutTurntable.isRayDisabledWhenOccupied(5));

        for (int idx = 0; idx < 4; idx++) {
            layoutTurntable.setRayDisabledWhenOccupied(idx, true);
            Assert.assertTrue("layoutTurntable.isRayDisabledWhenOccupied(" + idx + ")", layoutTurntable.isRayDisabledWhenOccupied(idx));
            layoutTurntable.setRayDisabledWhenOccupied(idx, false);
            Assert.assertFalse("layoutTurntable.isRayDisabledWhenOccupied(" + idx + ")", layoutTurntable.isRayDisabledWhenOccupied(idx));
        }
    }

    @Test
    public void testGetRayCoordsIndexed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        //invalid index
        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(5)",
                MathUtil.zeroPoint2D, layoutTurntable.getRayCoordsIndexed(5));

        //valid indexes
        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(0)",
                new Point2D.Double(218.5, 67.95706005997576),
                layoutTurntable.getRayCoordsIndexed(0));
        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(1)",
                new Point2D.Double(232.0429399400242, 81.5),
                layoutTurntable.getRayCoordsIndexed(1));
        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(2)",
                new Point2D.Double(237.0, 100.0),
                layoutTurntable.getRayCoordsIndexed(2));
        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(3)",
                new Point2D.Double(232.04293994002424, 118.5),
                layoutTurntable.getRayCoordsIndexed(3));
    }

    @Test
    public void testGetRayCoordsOrdered() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        //invalid index
        Assert.assertEquals("layoutTurntable.getRayCoordsOrdered(5)",
                MathUtil.zeroPoint2D, layoutTurntable.getRayCoordsOrdered(5));

        //valid indexes
        Assert.assertEquals("layoutTurntable.getRayCoordsOrdered(0)",
                new Point2D.Double(218.5, 67.95706005997576),
                layoutTurntable.getRayCoordsOrdered(0));
        Assert.assertEquals("layoutTurntable.getRayCoordsOrdered(1)",
                new Point2D.Double(232.0429399400242, 81.5),
                layoutTurntable.getRayCoordsOrdered(1));
        Assert.assertEquals("layoutTurntable.getRayCoordsOrdered(2)",
                new Point2D.Double(237.0, 100.0),
                layoutTurntable.getRayCoordsOrdered(2));
        Assert.assertEquals("layoutTurntable.getRayCoordsOrdered(3)",
                new Point2D.Double(232.04293994002424, 118.5),
                layoutTurntable.getRayCoordsOrdered(3));
    }

    @Test
    public void testSetRayCoordsIndexed() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        Point2D center = layoutTurntable.getCoordsCenter();
        //invalid index
        layoutTurntable.setRayCoordsIndexed(0, 0, 5);
        JUnitAppender.assertErrorMessage("Attempt to move a non-existant ray track");

        for (int idx = 0; idx < 4; idx++) {
            double angleRad = Math.toRadians(idx * 33);
            Point2D p = new Point2D.Double(10 * Math.cos(angleRad), 10 * Math.sin(angleRad));
            p = MathUtil.add(p, center);
            layoutTurntable.setRayCoordsIndexed(p.getX(), p.getY(), idx);
            Assert.assertEquals("layoutTurntable.getRayAngle(" + idx + ")",
                    (idx * 33) + 90, layoutTurntable.getRayAngle(idx), 0.5);
        }
    }

    @Test
    public void testGetCoordsForConnectionType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        //invalid connection type
        Point2D testPoint = layoutTurntable.getCoordsForConnectionType(LayoutTrack.NONE);
        JUnitAppender.assertErrorMessage("Invalid connection type 0");
        Assert.assertEquals("layoutTurntable.getCoordsForConnectionType(NONE)",
                layoutTurntablePoint, testPoint);

        //valid connection types
        Assert.assertEquals("layoutTurntable.getCoordsForConnectionType(TURNTABLE_RAY_OFFSET)",
                new Point2D.Double(218.5, 67.95706005997576),
                layoutTurntable.getCoordsForConnectionType(LayoutTrack.TURNTABLE_RAY_OFFSET));
        Assert.assertEquals("layoutTurntable.getCoordsForConnectionType(TURNTABLE_RAY_OFFSET + 1)",
                new Point2D.Double(232.0429399400242, 81.5),
                layoutTurntable.getCoordsForConnectionType(LayoutTrack.TURNTABLE_RAY_OFFSET + 1));
        Assert.assertEquals("layoutTurntable.getCoordsForConnectionType(TURNTABLE_RAY_OFFSET + 2)",
                new Point2D.Double(237.0, 100.0),
                layoutTurntable.getCoordsForConnectionType(LayoutTrack.TURNTABLE_RAY_OFFSET + 2));
        Assert.assertEquals("layoutTurntable.getCoordsForConnectionType(TURNTABLE_RAY_OFFSET + 3)",
                new Point2D.Double(232.04293994002424, 118.5),
                layoutTurntable.getCoordsForConnectionType(LayoutTrack.TURNTABLE_RAY_OFFSET + 3));
    }

    @Test
    public void testGetConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        //invalid connection type
        try {
            layoutTurntable.getConnection(LayoutTrack.NONE);
            Assert.fail("layoutTurntable.getConnection didn't throw exception");
        } catch (JmriException ex) {
            JUnitAppender.assertWarnMessage("Invalid Turntable connection type 0");
        }

        setupTracks();

        try {
            //valid connection types
            Assert.assertEquals("layoutTurntable.getConnection(TURNTABLE_RAY_OFFSET)",
                    ts1, layoutTurntable.getConnection(LayoutTrack.TURNTABLE_RAY_OFFSET));
            Assert.assertEquals("layoutTurntable.getConnection(TURNTABLE_RAY_OFFSET + 1)",
                    ts2, layoutTurntable.getConnection(LayoutTrack.TURNTABLE_RAY_OFFSET + 1));
            Assert.assertEquals("layoutTurntable.getConnection(TURNTABLE_RAY_OFFSET + 2)",
                    ts3, layoutTurntable.getConnection(LayoutTrack.TURNTABLE_RAY_OFFSET + 2));
            Assert.assertEquals("layoutTurntable.getConnection(TURNTABLE_RAY_OFFSET + 3)",
                    ts4, layoutTurntable.getConnection(LayoutTrack.TURNTABLE_RAY_OFFSET + 3));
        } catch (JmriException ex) {
            Assert.fail("layoutTurntable.getConnection threw exception: " + ex);
        }
    }

    @Test
    public void testSetConnection() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        setupTracks();

        try {
            //invalid type
            layoutTurntable.setConnection(LayoutTrack.POS_POINT, null, LayoutTrack.POS_POINT);
            Assert.fail("layoutTurntable.setConnection(...) didn't throw exception");
        } catch (JmriException ex) {
            JUnitAppender.assertWarnMessage("unexpected type of connection to layoutTurntable - 1");
        }

        try {
            //invalid connection type
            layoutTurntable.setConnection(LayoutTrack.POS_POINT, null, LayoutTrack.TRACK);
            Assert.fail("layoutTurntable.setConnection(...) didn't throw exception");
        } catch (JmriException ex) {
            JUnitAppender.assertWarnMessage("Invalid Connection Type 1");
        }

        try {
            //valid connection types
            Assert.assertEquals("layoutTurntable.getConnection(TURNTABLE_RAY_OFFSET)",
                    ts1, layoutTurntable.getConnection(LayoutTrack.TURNTABLE_RAY_OFFSET));
            Assert.assertEquals("layoutTurntable.getConnection(TURNTABLE_RAY_OFFSET + 1)",
                    ts2, layoutTurntable.getConnection(LayoutTrack.TURNTABLE_RAY_OFFSET + 1));
            Assert.assertEquals("layoutTurntable.getConnection(TURNTABLE_RAY_OFFSET + 2)",
                    ts3, layoutTurntable.getConnection(LayoutTrack.TURNTABLE_RAY_OFFSET + 2));
            Assert.assertEquals("layoutTurntable.getConnection(TURNTABLE_RAY_OFFSET + 3)",
                    ts4, layoutTurntable.getConnection(LayoutTrack.TURNTABLE_RAY_OFFSET + 3));
        } catch (JmriException ex) {
            Assert.fail("layoutTurntable.getConnection threw exception: " + ex);
        }
    }

    @Test
    public void testIsMainlines() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        setupTracks();

        Assert.assertFalse("layoutTurntable.isMainline()", layoutTurntable.isMainline());

        for (int idx = 0; idx < 4; idx++) {
            ts1.setMainline(idx == 0);
            ts2.setMainline(idx == 1);
            ts3.setMainline(idx == 2);
            ts4.setMainline(idx == 3);

            Assert.assertTrue("layoutTurntable.isMainlineIndexed(0)", layoutTurntable.isMainlineIndexed(0) == (idx == 0));
            Assert.assertTrue("layoutTurntable.isMainlineIndexed(1)", layoutTurntable.isMainlineIndexed(1) == (idx == 1));
            Assert.assertTrue("layoutTurntable.isMainlineIndexed(2)", layoutTurntable.isMainlineIndexed(2) == (idx == 2));
            Assert.assertTrue("layoutTurntable.isMainlineIndexed(3)", layoutTurntable.isMainlineIndexed(3) == (idx == 3));

            Assert.assertTrue("layoutTurntable.isMainlineOrdered(0)", layoutTurntable.isMainlineOrdered(0) == (idx == 0));
            Assert.assertTrue("layoutTurntable.isMainlineOrdered(1)", layoutTurntable.isMainlineOrdered(1) == (idx == 1));
            Assert.assertTrue("layoutTurntable.isMainlineOrdered(2)", layoutTurntable.isMainlineOrdered(2) == (idx == 2));
            Assert.assertTrue("layoutTurntable.isMainlineOrdered(3)", layoutTurntable.isMainlineOrdered(3) == (idx == 3));
        }
    }

    @Test
    public void testScaleCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        layoutTurntable.scaleCoords(3, 4);

        Assert.assertEquals("layoutTurntable.getCoordsCenter()",
                new Point2D.Double(600, 400), layoutTurntable.getCoordsCenter());

        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(0)",
                new Point2D.Double(649.75, 313.83047232344836),
                layoutTurntable.getRayCoordsIndexed(0));
        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(1)",
                new Point2D.Double(686.1695276765516, 350.25),
                layoutTurntable.getRayCoordsIndexed(1));
        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(2)",
                new Point2D.Double(699.5, 400.0),
                layoutTurntable.getRayCoordsIndexed(2));
        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(3)",
                new Point2D.Double(686.1695276765516, 449.75),
                layoutTurntable.getRayCoordsIndexed(3));
    }

    @Test
    public void testTranslateCoords() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        layoutTurntable.translateCoords(50, 20);

        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(0)",
                new Point2D.Double(268.5, 87.95706005997576),
                layoutTurntable.getRayCoordsIndexed(0));
        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(1)",
                new Point2D.Double(282.0429399400242, 101.5),
                layoutTurntable.getRayCoordsIndexed(1));
        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(2)",
                new Point2D.Double(287.0, 120.0),
                layoutTurntable.getRayCoordsIndexed(2));
        Assert.assertEquals("layoutTurntable.getRayCoordsIndexed(3)",
                new Point2D.Double(282.0429399400242, 138.5),
                layoutTurntable.getRayCoordsIndexed(3));
    }

    @Test
    public void testIsGetTurnoutControlled() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        Assert.assertFalse("layoutTurntable.isMainline()", layoutTurntable.isTurnoutControlled());
        layoutTurntable.setTurnoutControlled(true);
        Assert.assertTrue("layoutTurntable.isMainline()", layoutTurntable.isTurnoutControlled());
        layoutTurntable.setTurnoutControlled(false);
        Assert.assertFalse("layoutTurntable.isMainline()", layoutTurntable.isTurnoutControlled());
    }

    @Test
    public void testGetSetPosition() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        //this is the default position (-1 == not set)
        Assert.assertEquals("layoutTurntable.getPosition()", -1, layoutTurntable.getPosition());

        //invalid position
        layoutTurntable.setPosition(5);
        //no error message because it's not turnout controlled

        //now try an invalid position with turnout controlled enabled
        layoutTurntable.setTurnoutControlled(true);

        //invalid position
        layoutTurntable.setPosition(5);
        JUnitAppender.assertErrorMessage("Attempt to set the position on a non-existant ray track");

        //this is still at the default position (-1 == not set)
        Assert.assertEquals("layoutTurntable.getPosition()", -1, layoutTurntable.getPosition());

        for (int idx = 0; idx < 4; idx++) {
            layoutTurntable.setPosition(idx);
            Assert.assertEquals("layoutTurntable.getPosition()", idx, layoutTurntable.getPosition());
        }
    }

    @Test
    public void testRemoveIsActive() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        Assert.assertTrue("layoutTurntable.isActive()", layoutTurntable.isActive());
        layoutTurntable.remove(); //this will clear the active flag
        Assert.assertFalse("layoutTurntable.isActive()", layoutTurntable.isActive());
    }

    @Test
    public void testCheckForFreeConnections() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        List l = layoutTurntable.checkForFreeConnections();
        Assert.assertEquals("Number of free connections", 4, l.size());
        Assert.assertEquals("connections[#0]", LayoutTrack.TURNTABLE_RAY_OFFSET, l.get(0));
        Assert.assertEquals("connections[#1]", LayoutTrack.TURNTABLE_RAY_OFFSET + 1, l.get(1));
        Assert.assertEquals("connections[#2]", LayoutTrack.TURNTABLE_RAY_OFFSET + 2, l.get(2));
        Assert.assertEquals("connections[#3]", LayoutTrack.TURNTABLE_RAY_OFFSET + 3, l.get(3));

        setupTracks();  //this should make all the connections

        l = layoutTurntable.checkForFreeConnections();
        Assert.assertEquals("Number of free connections", 0, l.size());

        //this one will be unconnected
        layoutTurntable.addRay(150.0);

        l = layoutTurntable.checkForFreeConnections();
        Assert.assertEquals("Number of free connections", 1, l.size());
        Assert.assertEquals("connections[#4]", LayoutTrack.TURNTABLE_RAY_OFFSET + 4, l.get(0));

    }

    @Test
    public void testCheckForUnAssignedBlocks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);

        //Always returns true… nothing to see here... move along...
        Assert.assertTrue("layoutTurntable.checkForUnAssignedBlocks()", layoutTurntable.checkForUnAssignedBlocks());
    }

    @Test
    public void testCheckForNonContiguousBlocks() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);
        Assert.assertNotNull("layoutBlock1 is null", layoutBlock1);
        Assert.assertNotNull("layoutBlock2 is null", layoutBlock2);

        setupTracks();

        ts1.setLayoutBlock(layoutBlock1);
        ts2.setLayoutBlock(layoutBlock2);
        ts3.setLayoutBlock(layoutBlock3);
        ts4.setLayoutBlock(layoutBlock4);

        HashMap<String, List<Set<String>>> blockNamesToTrackNameSetMaps = new HashMap<>();
        layoutTurntable.checkForNonContiguousBlocks(blockNamesToTrackNameSetMaps);
        Assert.assertEquals("number of noncontiguous blocks", 4, blockNamesToTrackNameSetMaps.size());

        Assert.assertNull("map['BOGUS'] not null", blockNamesToTrackNameSetMaps.get("BOGUS"));

        //layoutBlock1
        List<Set<String>> trackNameSets = blockNamesToTrackNameSetMaps.get(layoutBlock1.getUserName());
        Assert.assertNotNull("map['Test Block 1']", trackNameSets);
        Assert.assertEquals("trackNameSets.size()", 1, trackNameSets.size());

        Set<String> trackNameSet = trackNameSets.get(0);
        Assert.assertNotNull("trackNameSet", trackNameSet);
        Assert.assertEquals("trackNameSet.size()", 3, trackNameSet.size());

        Iterator<String> it = trackNameSet.iterator();
        Assert.assertEquals("layoutTurntable name", layoutTurntable.getName(), it.next());
        Assert.assertEquals("ts1 name", ts1.getName(), it.next());
        Assert.assertEquals("a1 name", a1.getName(), it.next());

        //layoutBlock2
        trackNameSets = blockNamesToTrackNameSetMaps.get(layoutBlock2.getUserName());
        Assert.assertNotNull("trackNameSet", trackNameSet);
        Assert.assertEquals("trackNameSets.size()", 1, trackNameSets.size());

        trackNameSet = trackNameSets.get(0);
        Assert.assertNotNull("trackNameSet", trackNameSet);
        Assert.assertEquals("trackNameSet.size()", 3, trackNameSet.size());

        it = trackNameSet.iterator();
        Assert.assertEquals("layoutTurntable name", layoutTurntable.getName(), it.next());
        Assert.assertEquals("ts2 name", ts2.getName(), it.next());
        Assert.assertEquals("a2 name", a2.getName(), it.next());

        //layoutBlock3
        trackNameSets = blockNamesToTrackNameSetMaps.get(layoutBlock3.getUserName());
        Assert.assertNotNull("trackNameSet", trackNameSet);
        Assert.assertEquals("trackNameSets.size()", 1, trackNameSets.size());

        trackNameSet = trackNameSets.get(0);
        Assert.assertNotNull("trackNameSet", trackNameSet);
        Assert.assertEquals("trackNameSet.size()", 3, trackNameSet.size());

        it = trackNameSet.iterator();
        Assert.assertEquals("layoutTurntable name", layoutTurntable.getName(), it.next());
        Assert.assertEquals("ts3 name", ts3.getName(), it.next());
        Assert.assertEquals("a3 name", a3.getName(), it.next());

        //layoutBlock4
        trackNameSets = blockNamesToTrackNameSetMaps.get(layoutBlock4.getUserName());
        Assert.assertNotNull("trackNameSet", trackNameSet);
        Assert.assertEquals("trackNameSets.size()", 1, trackNameSets.size());

        trackNameSet = trackNameSets.get(0);
        Assert.assertNotNull("trackNameSet", trackNameSet);
        Assert.assertEquals("trackNameSet.size()", 3, trackNameSet.size());

        it = trackNameSet.iterator();
        Assert.assertEquals("layoutTurntable name", layoutTurntable.getName(), it.next());
        Assert.assertEquals("ts4 name", ts4.getName(), it.next());
        Assert.assertEquals("a4 name", a4.getName(), it.next());
    }

    //
    //private methods
    //
    private void setupTracks() {
        Assert.assertNotNull("layoutEditor is null", layoutEditor);
        Assert.assertNotNull("layoutTurntable is null", layoutTurntable);
        List<LayoutTrack> layoutTracks = layoutEditor.getLayoutTracks();
        Assert.assertNotNull("layoutTracks is null", layoutTracks);

        //add 1st anchor
        a1 = new PositionablePoint("A1", PositionablePoint.ANCHOR, new Point2D.Double(10, 50), layoutEditor);
        Assert.assertNotNull("a1 is null", a1);
        layoutTracks.add(a1);

        //connect the 1st LayoutTurntable ray to 1st anchor
        int tsIdx = 1;
        ts1 = addNewTrackSegment(layoutTurntable, LayoutTrack.TURNTABLE_RAY_OFFSET, a1, LayoutTrack.POS_POINT, tsIdx++);

        //add 2nd anchor
        a2 = new PositionablePoint("A2", PositionablePoint.ANCHOR, new Point2D.Double(20, 80), layoutEditor);
        Assert.assertNotNull("a2 is null", a2);
        layoutTracks.add(a2);

        //connect the 2nd LayoutTurntable ray to 2nd anchor
        ts2 = addNewTrackSegment(layoutTurntable, LayoutTrack.TURNTABLE_RAY_OFFSET + 1, a2, LayoutTrack.POS_POINT, tsIdx++);

        //add 3rd anchor
        a3 = new PositionablePoint("A3", PositionablePoint.ANCHOR, new Point2D.Double(90, 50), layoutEditor);
        Assert.assertNotNull("a3 is null", a3);
        layoutTracks.add(a3);

        //connect the 3rd LayoutTurntable ray to 3rd anchor
        ts3 = addNewTrackSegment(layoutTurntable, LayoutTrack.TURNTABLE_RAY_OFFSET + 2, a3, LayoutTrack.POS_POINT, tsIdx++);

        //add 4th anchor
        a4 = new PositionablePoint("A4", PositionablePoint.ANCHOR, new Point2D.Double(80, 20), layoutEditor);
        Assert.assertNotNull("a4 is null", a4);
        layoutTracks.add(a4);

        //connect the 4th LayoutTurntable ray to 4th anchor
        ts4 = addNewTrackSegment(layoutTurntable, LayoutTrack.TURNTABLE_RAY_OFFSET + 3, a4, LayoutTrack.POS_POINT, tsIdx++);

        //wait for layout editor to finish setup and drawing
        new QueueTool().waitEmpty();
    }

    private static TrackSegment addNewTrackSegment(
            @CheckForNull LayoutTrack c1, int t1,
            @CheckForNull LayoutTrack c2, int t2,
            int idx) {
        TrackSegment result = null;
        if ((c1 != null) && (c2 != null)) {
            //create new track segment
            String name = layoutEditor.getFinder().uniqueName("T", idx);
            result = new TrackSegment(name, c1, t1, c2, t2,
                    false, true, layoutEditor);
            Assert.assertNotNull("new TrackSegment is null", result);
            layoutEditor.getLayoutTracks().add(result);
            //link to connected objects
            layoutEditor.setLink(c1, t1, result, LayoutTrack.TRACK);
            layoutEditor.setLink(c2, t2, result, LayoutTrack.TRACK);
        }
        return result;
    }

    //
    //from here down is testing infrastructure
    //
    @BeforeClass
    public static void setUpClass() throws Exception {
        JUnitUtil.setUp();
        if (!GraphicsEnvironment.isHeadless()) {
            JUnitUtil.resetProfileManager();

            stringComparator = Operator.getDefaultStringComparator();

            //set default string matching comparator to one that exactly matches and is case sensitive
            Operator.setDefaultStringComparator(new Operator.DefaultStringComparator(true, true));

            layoutEditor = new LayoutEditor("LayoutTurntable Tests Layout");
            layoutEditor.setPanelBounds(new Rectangle2D.Double(0, 0, 640, 480));
            layoutEditor.setVisible(true);

            //create a layout block
            layoutBlock1 = new LayoutBlock("ILB1", "Test Block 1");
            layoutBlock2 = new LayoutBlock("ILB2", "Test Block 2");
            layoutBlock3 = new LayoutBlock("ILB3", "Test Block 3");
            layoutBlock4 = new LayoutBlock("ILB4", "Test Block 4");

            //wait for layout editor to finish setup and drawing
            new QueueTool().waitEmpty();
        }
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        if (!GraphicsEnvironment.isHeadless()) {
            new QueueTool().waitEmpty();
            JUnitUtil.dispose(layoutEditor);
            layoutEditor = null;

            Operator.setDefaultStringComparator(stringComparator);
        }
        JUnitUtil.tearDown();
    }

    @Before
    public void setUp() {
        JUnitUtil.resetProfileManager();
        if (!GraphicsEnvironment.isHeadless()) {
            layoutTurntable = new LayoutTurntable(layoutTurntableName, layoutTurntablePoint, layoutEditor);
            layoutTurntable.addRay(30.0);
            layoutTurntable.addRay(60.0);
            layoutTurntable.addRay(90.0);
            layoutTurntable.addRay(120.0);

            //wait for layout editor to finish setup and drawing
            new QueueTool().waitEmpty();
        }
    }
}
