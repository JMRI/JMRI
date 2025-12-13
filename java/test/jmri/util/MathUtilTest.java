package jmri.util;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.junit.jupiter.api.*;
import org.python.modules.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test simple functioning of MathUtil
 *
 * @author George Warner Copyright (C) 2017
 */
@SuppressFBWarnings( value = {"CNT_ROUGH_CONSTANT_VALUE", "FL_FLOATS_AS_LOOP_COUNTERS"},
    justification = "Many co-incidental usages of value near to PI constant."
                  + "Loop Increments do not need to be exact")
public class MathUtilTest {

    static final double TOLERANCE = 0.000001;

    @Test
    public void testPointToPoint2D() {
        Point p = new Point(314, 159);
        Point2D p2d = new Point2D.Double(314.D, 159.D);
        assertEquals( p2d, MathUtil.pointToPoint2D(p), "testPointToPoint");
    }

    @Test
    public void testPoint2DToPoint() {
        Point2D p2d = new Point2D.Double(159.4, 314.6);
        Point p = new Point(159, 314);
        assertEquals( p, MathUtil.point2DToPoint(p2d), "testPointToPoint");
    }

    @Test
    public void testMinMaxPin() {
        Point2D p1 = new Point2D.Double(159.4, 314.6);
        Point2D p2 = new Point2D.Double(314.6, 159.4);
        assertEquals( new Point2D.Double(159.4, 159.4), MathUtil.min(p1, p2),
            "MathUtil.min");
        assertEquals( new Point2D.Double(314.6, 314.6), MathUtil.max(p1, p2),
            "MathUtil.max");
        assertEquals( new Point2D.Double(159.4, 159.4),
            MathUtil.pin(MathUtil.zeroPoint2D, p1, p2),
            "MathUtil.pin");
        assertEquals( new Point2D.Double(314.6, 314.6),
            MathUtil.pin(MathUtil.infinityPoint2D, p1, p2),
            "MathUtil.pin");
    }

    @Test
    public void testAddPoint2D() {
        Point2D p1 = new Point2D.Double(159.4, 314.6);
        Point2D p2 = new Point2D.Double(314.6, 159.4);
        assertEquals( new Point2D.Double(474, 474),
            MathUtil.add(p1, p2), "MathUtil.add");
    }

    @Test
    public void testSubtractPoint2D() {
        Point2D p1 = new Point2D.Double(159.4, 314.6);
        Point2D p2 = new Point2D.Double(314.6, 159.4);
        assertEquals(
                new Point2D.Double(-155.20000000000002, 155.20000000000002),
                MathUtil.subtract(p1, p2),
                "MathUtil.add");
    }

    @Test
    public void testMultiplyPoint2D() {
        Point2D p1 = new Point2D.Double(159.4, 314.6);
        Point2D p2 = new Point2D.Double(314.6, 159.4);
        assertEquals( new Point2D.Double(500.769446, 988.3442140000001),
                MathUtil.multiply(p1, 3.14159),
                "MathUtil.multiply(p, s)");
        assertEquals( new Point2D.Double(988.3442140000001, 500.769446),
                MathUtil.multiply(3.14159, p2),
                "MathUtil.multiply(s, p)");
        assertEquals( new Point2D.Double(500.516, 5002.14),
                MathUtil.multiply(p1, 3.14, 15.9),
                "MathUtil.multiply(p, x, y)");
        assertEquals( new Point2D.Double(50147.240000000005, 50147.240000000005),
                MathUtil.multiply(p1, p2),
                "MathUtil.multiply(p, p)");
    }

    @Test
    public void testDividePoint2D() {
        Point2D p = new Point2D.Double(159.4, 314.6);
        assertEquals( new Point2D.Double(50.73863871479092, 100.14037477837657),
                MathUtil.divide(p, 3.14159),
                "MathUtil.divide(p, s)");
        assertEquals( new Point2D.Double(50.76433121019108, 19.78616352201258),
                MathUtil.divide(p, 3.14, 15.9),
                "MathUtil.divide(p, x, y)");
    }

    @Test
    public void testOffset() {
        Point2D p = new Point2D.Double(159.4, 314.6);
        assertEquals( new Point2D.Double(162.54, 330.5),
            MathUtil.offset(p, 3.14, 15.9),
            "MathUtil.offset(p, x, y)");
    }

    private void assertEquals2D(Point2D first, Point2D second, String message) {
        assertEquals(first.getX(), second.getX(), TOLERANCE, message);
        assertEquals(first.getY(), second.getY(), TOLERANCE, message);
    }
    
    @Test
    public void testRotate() {
        assertEquals2D( new Point2D.Double(-10.407653711754921, 33.62217637536562),
                MathUtil.rotateRAD(31.4, 15.9, 26.535),
                "MathUtil.rotateRAD(x, y, a)");
        assertEquals2D( new Point2D.Double(30.63022962954949, 17.336638452741308),
                MathUtil.rotateDEG(31.4, 15.9, 2.6535),
                "MathUtil.rotateDEG(x, y, a)");

        Point2D p = new Point2D.Double(159.4, 314.6);
        assertEquals2D( new Point2D.Double(-317.60286988977776, 153.32950478553346),
                MathUtil.rotateRAD(p, 1.59),
                "MathUtil.rotateRAD(p, a)");
        assertEquals2D( new Point2D.Double(-27.951196946534395, 351.56827301287586),
                MathUtil.rotateDEG(p, 31.4159),
                "MathUtil.rotateDEG(p, a)");

        Point2D c = new Point2D.Double(314.6, 159.4);
        assertEquals2D( new Point2D.Double(162.40884342950076, 1.2483896328153605),
                MathUtil.rotateRAD(p, c, 1.59),
                "MathUtil.rotateRAD(p, c, a)");
        assertEquals2D( new Point2D.Double(101.25390735346647, 210.9511857521893),
                MathUtil.rotateDEG(p, c, 31.4159),
                "MathUtil.rotateDEG(p, c, a)");

    }

    @Test
    public void testOrthogonal() {
        Point2D p = new Point2D.Double(159.4, 314.6);
        assertEquals( new Point2D.Double(-314.6, 159.4), MathUtil.orthogonal(p),
            "MathUtil.offset(p, x, y)");
    }

    @Test
    public void testVectors() {
        assertEquals( new Point2D.Double(50.88327631306455, 31.071475530594356),
                MathUtil.vectorDEG(31.41, 59.62),
                "MathUtil.vectorDEG(d, m)");
        assertEquals( new Point2D.Double(59.618952961759454, -0.35333800179572555),
                MathUtil.vectorRAD(31.41, 59.62),
                "MathUtil.vectorRAD(d, m)");
    }

    @Test
    public void testDot() {
        Point2D p1 = new Point2D.Double(31.4, 15.9);
        Point2D p2 = new Point2D.Double(159.4, 314.6);
        assertEquals( 10007.3, MathUtil.dot(p1, p2), TOLERANCE,
            "MathUtil.Dot(p1, p2)");
    }

    @Test
    public void testLengthSquared() {
        Point2D p = new Point2D.Double(31.4, 15.9);
        assertEquals( 1238.77, MathUtil.lengthSquared(p), TOLERANCE,
            "MathUtil.lengthSquared(p1)");
    }

    @Test
    public void testLength() {
        Point2D p = new Point2D.Double(31.4, 15.9);
        assertEquals( 35.196164563770296, MathUtil.length(p), TOLERANCE,
            "MathUtil.length(p1)");
    }

    @Test
    public void testDistance() {
        Point2D p1 = new Point2D.Double(31.4, 15.9);
        Point2D p2 = new Point2D.Double(159.4, 314.6);
        assertEquals( 324.97029094980365, MathUtil.distance(p1, p2),
            TOLERANCE, "MathUtil.distance(p1, p2)");
    }

    @Test
    public void testNormalize() {
        Point2D p = new Point2D.Double(31.4, 15.9);
        assertEquals( new Point2D.Double(0.8921426635310731, 0.45175376911286824),
                MathUtil.normalize(p), "MathUtil.normalize(p)");
        assertEquals( new Point2D.Double(2.802746470322584, 1.4192251235072957),
                MathUtil.normalize(p, 3.14159), "MathUtil.normalize(p, l)");
    }

    @Test
    public void testComputeAngles() {
        Point2D p1 = new Point2D.Double(31.4, 15.9);
        assertEquals( 1.1020661694371947, MathUtil.computeAngleRAD(p1),
            TOLERANCE, "MathUtil.computeAngleRAD(p)");
        assertEquals( 63.143740252900734, MathUtil.computeAngleDEG(p1),
            TOLERANCE, "MathUtil.computeAngleDEG(p)");

        Point2D p2 = new Point2D.Double(159.4, 314.6);
        assertEquals( -2.7367412729776444, MathUtil.computeAngleRAD(p1, p2),
            TOLERANCE, "MathUtil.computeAngleRAD(p1, p2)");
        assertEquals( -156.80372456087935, MathUtil.computeAngleDEG(p1, p2),
            TOLERANCE, "MathUtil.computeAngleDEG(p1, p2)");
    }

    @Test
    public void testIntLerp() {
        assertEquals( 36, MathUtil.lerp(31, 41, 0.59), "MathUtil.lerp(i1, i2)");
    }

    @Test
    public void testDoubleLerp() {
        boolean passed = true;    // assume success (optimist!)
        double minV = -666.66, maxV = +999.99;
        Double minD = minV;
        Double maxD = maxV;
        for (double theV = 0.0; theV < 2.f; theV += 0.15) {
            double c = MathUtil.lerp(minV, maxV, theV);
            double t = (c - minV) / (maxV - minV);
            assertEquals( t, theV, TOLERANCE, "MathUtil.lerp(min, max, v)");
            passed = (math.fabs(t - theV) <= TOLERANCE);
            if (!passed) {
                break;
            }

            Double theD = theV;
            Double cD = MathUtil.lerp(minD, maxD, theD);
            Double tD = (cD - minD) / (maxD - minD);
            assertEquals( tD, theD, TOLERANCE, "MathUtil.lerp(minD, maxD, vD)");
            passed = (math.fabs(tD - theD) <= TOLERANCE);
            if (!passed) {
                break;
            }
        }
        assertTrue( passed, "Double lerp");
    }

    @Test
    public void testPoint2D_lerp() {
        boolean passed = true;    // assume success (optimist!)
        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);
        for (double f = 0.0; f < 2.f; f += 0.15) {
            Point2D pC = MathUtil.lerp(pA, pB, f);
            double distanceAC = pA.distance(pC);
            double t = distanceAC / distanceAB;
            assertEquals(f, t, TOLERANCE);
            passed = (math.fabs(t - f) <= TOLERANCE);
            if (!passed) {
                break;
            }
        }
        assertTrue( passed, "Point2D lerp is good");
    }

    @Test
    public void testPoint2D_oneThird() {

        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);

        Point2D pC = MathUtil.oneThirdPoint(pA, pB);
        double distanceAC = pA.distance(pC);
        double t = distanceAC / distanceAB;
        assertEquals(1.0 / 3.0, t, TOLERANCE);
        boolean passed = (math.fabs(t - (1.0 / 3.0)) <= TOLERANCE);

        assertTrue( passed, "Point2D third is good");
    }

    @Test
    public void testPoint2D_twoThirds() {

        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);

        Point2D pC = MathUtil.twoThirdsPoint(pA, pB);
        double distanceAC = pA.distance(pC);
        double t = distanceAC / distanceAB;
        assertEquals(2.0 / 3.0, t, TOLERANCE);
        boolean passed = (math.fabs(t - (2.0 / 3.0)) <= TOLERANCE);

        assertTrue( passed, "Point2D two third is good");
    }

    @Test
    public void testPoint2D_oneFourth() {

        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);

        Point2D pC = MathUtil.oneFourthPoint(pA, pB);
        double distanceAC = pA.distance(pC);
        double t = distanceAC / distanceAB;
        assertEquals(1.0 / 4.0, t, TOLERANCE);

        boolean passed = (math.fabs(t - (1.0 / 4.0)) <= TOLERANCE);
        assertTrue( passed, "Point2D fourth is good");
    }

    @Test
    public void testPoint2D_threeFourths() {

        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);

        Point2D pC = MathUtil.threeFourthsPoint(pA, pB);
        double distanceAC = pA.distance(pC);
        double t = distanceAC / distanceAB;
        assertEquals(3.0 / 4.0, t, TOLERANCE);

        boolean passed = (math.fabs(t - (3.0 / 4.0)) <= TOLERANCE);
        assertTrue( passed, "Point2D three fourths is good");
    }

    @Test
    public void testGranulize() {
        assertEquals( 314.2, MathUtil.granulize(314.15926, 0.1),
            TOLERANCE, "MathUtil.granulize(v, g)");

        Point2D p = new Point2D.Double(31.4159, 15.926283);
        assertEquals( new Point2D.Double(31.42, 15.93),
            MathUtil.granulize(p, 0.01, 0.01), "MathUtil.granulize(p, h, v)");
        assertEquals( new Point2D.Double(31.400000000000002, 15.950000000000001),
            MathUtil.granulize(p, 0.05), "MathUtil.granulize(p, h, v)");
    }

    @Test
    public void testMidPoint() {
        Point2D p1 = new Point2D.Double(31.4, 15.9);
        Point2D p2 = new Point2D.Double(159.4, 314.6);
        assertEquals( new Point2D.Double(95.4, 165.25),
            MathUtil.midPoint(p1, p2), "MathUtil.midPoint(p1, p2)");
    }

    @Test
    public void testInt_wrap() {
        assertEquals( 313, MathUtil.wrap(623, 159, 314),
            "MathUtil.wrap(int, min, max)");
    }

    @Test
    public void testDouble_wrap() {
        boolean passed = true;    // assume success (optimist!)
        double theLimits = 180.0;
        double theMin = -theLimits, theMax = +theLimits;
        double theRange = theMax - theMin;
        for (double a = -3.0 * theLimits; a < +3.0 * theLimits; a += theLimits / 10.0) {
            double t = a;
            while (t >= theMax) {
                t -= theRange;
            }
            while (t < theMin) {
                t += theRange;
            }
            double c = MathUtil.wrap(a, theMin, theMax);
            assertEquals(t, c, TOLERANCE);
            passed = (math.fabs(t - c) <= TOLERANCE);
            if (!passed) {
                break;
            }
        }
        assertTrue( passed, "Double wrap is good");
    }

    @Test
    public void testDouble_wrapPM180() {
        boolean passed = true;    // assume success (optimist!)
        double theLimits = 180.0;
        double theMin = -theLimits, theMax = +theLimits;
        double theRange = theMax - theMin;
        for (double a = -3.0 * theLimits; a < +3.0 * theLimits; a += theLimits / 10.0) {
            double t = a;
            while (t >= theMax) {
                t -= theRange;
            }
            while (t < theMin) {
                t += theRange;
            }
            double c = MathUtil.wrapPM180(a);
            assertEquals(t, c, TOLERANCE);
            passed = (math.fabs(t - c) <= TOLERANCE);
            if (!passed) {
                break;
            }
        }
        assertTrue( passed, "Double wrapPM180 is good");
    }

    @Test
    public void testDouble_wrapPM360() {
        boolean passed = true;    // assume success (optimist!)
        double theLimits = 360.0;
        double theMin = -theLimits, theMax = +theLimits;
        double theRange = theMax - theMin;
        for (double a = -3.0 * theLimits; a < +3.0 * theLimits; a += theLimits / 10.0) {
            double t = a;
            while (t >= theMax) {
                t -= theRange;
            }
            while (t < theMin) {
                t += theRange;
            }
            double c = MathUtil.wrapPM360(a);
            assertEquals(t, c, TOLERANCE);
            passed = (math.fabs(t - c) <= TOLERANCE);
            if (!passed) {
                break;
            }
        }
        assertTrue( passed, "Double wrapPM360 is good");
    }

    @Test
    public void testDouble_wrap360() {
        boolean passed = true;    // assume success (optimist!)
        double limits = 360.0;
        for (double a = -3.3 * limits; a < +3.3 * limits; a += limits / 15.0) {
            double t = a;
            while (t < 0.0) {
                t += limits;
            }
            while (t >= +limits) {
                t -= limits;
            }
            double c = MathUtil.wrap360(a);
            assertEquals(t, c, TOLERANCE);
            passed = (math.fabs(t - c) <= TOLERANCE);
            if (!passed) {
                break;
            }
        }
        assertTrue( passed, "Double wrap360 is good");
    }

    @Test
    public void testDouble_normalizeAngle() {
        boolean passed = true;    // assume success (optimist!)
        double limits = 360.0;
        for (double a = -3.0 * limits; a < +3.0 * limits; a += limits / 10.0) {
            double t = a;
            while (t >= +limits) {
                t -= limits;
            }
            while (t < 0.0) {
                t += limits;
            }
            double c = MathUtil.normalizeAngleDEG(a);
            assertEquals(t, c, TOLERANCE);
            passed = (math.fabs(t - c) <= TOLERANCE);
            if (!passed) {
                break;
            }
        }
        assertTrue( passed, "Double normalizeAngle is good");
    }

    @Test
    public void testDouble_diffAngle() {
        boolean passed = true;    // assume success (optimist!)

        double theLimits = 180.0;
        double theMin = -theLimits, theMax = +theLimits;
        double theRange = theMax - theMin;
        for (double a = -3.3 * theLimits; a < +3.3 * theLimits; a += theLimits / 15.0) {
            for (double b = -3.3 * theLimits; b < +3.3 * theLimits; b += theLimits / 15.0) {
                double t = a - b;
                while (t >= theMax) {
                    t -= theRange;
                }
                while (t < theMin) {
                    t += theRange;
                }
                double c = MathUtil.diffAngleDEG(a, b);
                assertEquals(t, c, TOLERANCE);
                passed = (math.fabs(t - c) <= TOLERANCE);
                if (!passed) {
                    break;
                }
            }
            if (!passed) {
                break;
            }
        }
        assertTrue( passed, "Double diffAngle is good");
    }

    @Test
    public void testDouble_diffAngleRAD() {

        assertEquals( -1.2759999999999998, MathUtil.diffAngleRAD(0.314, 1.59),
            TOLERANCE, "MathUtil.diffAngleRAD(a, b)");
    }

    @Test
    public void testDouble_absDiffAngleRAD() {

        assertEquals( +1.2759999999999998, MathUtil.absDiffAngleRAD(0.314, 1.59),
            TOLERANCE, "MathUtil.absDiffAngleRAD(a, b)");
    }

    @Test
    public void testDouble_absDiffAngle() {
        boolean passed = true;    // assume success (optimist!)

        double theLimits = 180.0;
        double theMin = -theLimits, theMax = +theLimits;
        double theRange = theMax - theMin;
        for (double a = -3.3 * theLimits; a < +3.3 * theLimits; a += theLimits / 15.0) {
            for (double b = -3.3 * theLimits; b < +3.3 * theLimits; b += theLimits / 15.0) {
                double t = a - b;
                while (t >= theMax) {
                    t -= theRange;
                }
                while (t < theMin) {
                    t += theRange;
                }
                if (t < 0.0) {
                    t = -t;
                }
                double c = MathUtil.absDiffAngleDEG(a, b);
                assertEquals(t, c, TOLERANCE);
                passed = (math.fabs(t - c) <= TOLERANCE);
                if (!passed) {
                    break;
                }
            }
            if (!passed) {
                break;
            }
        }
        assertTrue( passed, "Double absDiffAngle is good");
    }

    @Test
    public void testDouble_pin() {
        boolean passed = true;    // assume success (optimist!)
        double limits = 180.0;
        for (double a = -3.3 * limits; a < +3.3 * limits; a += limits / 15.0) {
            for (double b = -3.3 * limits; b < +3.3 * limits; b += limits / 15.0) {
                for (double c = -3.3 * limits; c < +3.3 * limits; c += limits / 15.0) {
                    double t = a;
                    if (t < b) {
                        t = b;
                    }
                    if (t > c) {
                        t = c;
                    }
                    double d = MathUtil.pin(a, b, c);
                    assertEquals(t, d, TOLERANCE);
                    passed = (math.fabs(t - d) <= TOLERANCE);
                    if (!passed) {
                        break;
                    }
                }
                if (!passed) {
                    break;
                }
            }
            if (!passed) {
                break;
            }
        }
        assertTrue( passed, "Double pin is good");
    }

    @Test
    public void test_rectangleToRectangle2D() {
        Rectangle r = new Rectangle(11, 22, 33, 44);
        Rectangle2D rD = new Rectangle2D.Double(11, 22, 33, 44);
        assertEquals( rD, MathUtil.rectangleToRectangle2D(r),
            "MathUtil.rectangleToRectangle2D(r)");
    }

    @Test
    public void test_origin() {
        Rectangle2D rD = new Rectangle2D.Double(3.14, 1.59, 33.3, 44.4);
        assertEquals( new Point2D.Double(3.14, 1.59), MathUtil.getOrigin(rD),
            "MathUtil.origin(r)");
    }

    @Test
    public void test_size() {
        Rectangle2D rD = new Rectangle2D.Double(3.14, 1.59, 33.3, 44.4);
        assertEquals( new Dimension(33, 44), MathUtil.getSize(rD), "MathUtil.size(r)");
    }

    @Test
    public void test_center() {
        Rectangle2D rD = new Rectangle2D.Double(3.14, 1.59, 33.3, 44.4);
        assertEquals( new Point2D.Double(19.79, 23.79), MathUtil.center(rD),
            "MathUtil.center(r)");
    }

    @Test
    public void test_midPoint() {
        Rectangle2D rD = new Rectangle2D.Double(3.14, 1.59, 33.3, 44.4);
        assertEquals( new Point2D.Double(19.79, 23.79), MathUtil.midPoint(rD),
            "MathUtil.midPoint(r)");
    }

    @Test
    public void test_offset() {
        Rectangle2D rD = new Rectangle2D.Double(3.14, 1.59, 33.3, 44.4);
        assertEquals( new Rectangle2D.Double(36.44, 45.99, 33.3, 44.4),
            MathUtil.offset(rD, 33.3, 44.4), "MathUtil.offset(r, h, v)");
        Point2D p = new Point2D.Double(31.41, 15.9);
        assertEquals( new Rectangle2D.Double(34.55, 17.490000000000002, 33.3, 44.4),
            MathUtil.offset(rD, p), "MathUtil.offset(r, p)");
    }

    @Test
    public void test_inset() {
        Rectangle2D rD = new Rectangle2D.Double(31.4, 15.9, 33.3, 44.4);
        assertEquals( new Rectangle2D.Double(34.54159, 19.04159, 27.016819999999996, 38.11682),
            MathUtil.inset(rD, 3.14159), "MathUtil.inset(r, i)");
        assertEquals( new Rectangle2D.Double(34.73, 20.34, 26.639999999999997, 35.519999999999996),
            MathUtil.inset(rD, 3.33, 4.44), "MathUtil.inset(r, h, v)");
    }

    @Test
    public void test_scale() {
        Rectangle2D rD = new Rectangle2D.Double(31.4, 15.9, 33.3, 44.4);
        assertEquals( new Rectangle2D.Double(98.64592599999999, 49.951281, 104.61494699999999, 139.486596),
            MathUtil.scale(rD, 3.14159), "MathUtil.scale(r, s)");
    }

    @Test
    public void test_centerRectangleOnPoint() {
        Rectangle2D rD = new Rectangle2D.Double(31.4, 15.9, 33.3, 44.4);
        assertEquals( new Rectangle2D.Double(316.65, 422.19999999999993, 33.3, 44.4),
            MathUtil.centerRectangleOnPoint(rD, new Point2D.Double(333.3, 444.4)),
            "MathUtil.centerRectangleOnPoint(r, p)");
    }

    @Test
    public void test_centerRectangleOnRectangle() {
        Rectangle2D rD = new Rectangle2D.Double(31.4, 15.9, 33.3, 44.4);
        Rectangle2D rC = new Rectangle2D.Double(314.1, 59.6, 33.3, 44.4);
        assertEquals( new Rectangle2D.Double(314.09999999999997, 59.599999999999994, 33.3, 44.4),
            MathUtil.centerRectangleOnRectangle(rD, rC),
            "MathUtil.centerRectangleOnRectangle(r1, r2)");
    }

    @Test
    public void test_rectangleAtPoint() {
        Point2D p = new Point2D.Double(31.41, 15.9);
        assertEquals( new Rectangle2D.Double(31.41, 15.9, 22.2, 33.3),
            MathUtil.rectangleAtPoint(p, 22.2, 33.3),
            "MathUtil.rectangleAtPoint(r1, r2)");
    }

    @Test
    public void test_intersect() {
        Point2D p1 = new Point2D.Double(111.0, 222.0);
        Point2D p2 = new Point2D.Double(151.0, 252.0);
        Point2D p3 = new Point2D.Double(100.0, 350.0);
        Point2D p4 = new Point2D.Double(140.0, 320.0);
        assertEquals( new Point2D.Double(201.83333333333334, 290.12499999999994),
            MathUtil.intersect(p1, p2, p3, p4),
            "MathUtil.intersect(p1, p2, p3, p4)");

        // coliner lines (no solution)
        assertNull( MathUtil.intersect(p1, p2, p1, p2),
            "MathUtil.intersect(p1, p2, p1, p2)");
        assertNull( MathUtil.intersect(p1, p1, p2, p2),
            "MathUtil.intersect(p1, p1, p2, p2)");

        // parallel lines (no solution)
        Point2D p1P = new Point2D.Double(121.0, 232.0);
        Point2D p2P = new Point2D.Double(161.0, 262.0);
        assertNull( MathUtil.intersect(p1, p2, p1P, p2P),
            "MathUtil.intersect(p1, p1, p1P, p2P)");
    }

    // from here down is testing infrastructure
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(MathUtilTest.class);
}
