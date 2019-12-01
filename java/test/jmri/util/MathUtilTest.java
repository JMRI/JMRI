package jmri.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.python.modules.math;

/**
 * Test simple functioning of MathUtil
 *
 * @author	George Warner Copyright (C) 2017
 */
public class MathUtilTest {

    static final double tolerance = 0.000001;

    @Test
    public void testPointToPoint2D() {
        Point p = new Point(314, 159);
        Point2D p2d = new Point2D.Double(314.D, 159.D);
        Assert.assertEquals("testPointToPoint", p2d, MathUtil.pointToPoint2D(p));
    }

    @Test
    public void testPoint2DToPoint() {
        Point2D p2d = new Point2D.Double(159.4, 314.6);
        Point p = new Point(159, 314);
        Assert.assertEquals("testPointToPoint", p, MathUtil.point2DToPoint(p2d));
    }

    @Test
    public void testMinMaxPin() {
        Point2D p1 = new Point2D.Double(159.4, 314.6);
        Point2D p2 = new Point2D.Double(314.6, 159.4);
        Assert.assertEquals("MathUtil.min", new Point2D.Double(159.4, 159.4),
                MathUtil.min(p1, p2));
        Assert.assertEquals("MathUtil.max", new Point2D.Double(314.6, 314.6),
                MathUtil.max(p1, p2));
        Assert.assertEquals("MathUtil.pin", new Point2D.Double(159.4, 159.4),
                MathUtil.pin(MathUtil.zeroPoint2D, p1, p2));
        Assert.assertEquals("MathUtil.pin", new Point2D.Double(314.6, 314.6),
                MathUtil.pin(MathUtil.infinityPoint2D, p1, p2));
    }

    @Test
    public void testAddPoint2D() {
        Point2D p1 = new Point2D.Double(159.4, 314.6);
        Point2D p2 = new Point2D.Double(314.6, 159.4);
        Assert.assertEquals("MathUtil.add", new Point2D.Double(474, 474),
                MathUtil.add(p1, p2));
    }

    @Test
    public void testSubtractPoint2D() {
        Point2D p1 = new Point2D.Double(159.4, 314.6);
        Point2D p2 = new Point2D.Double(314.6, 159.4);
        Assert.assertEquals("MathUtil.add",
                new Point2D.Double(-155.20000000000002, 155.20000000000002),
                MathUtil.subtract(p1, p2));
    }

    @Test
    public void testMultiplyPoint2D() {
        Point2D p1 = new Point2D.Double(159.4, 314.6);
        Point2D p2 = new Point2D.Double(314.6, 159.4);
        Assert.assertEquals("MathUtil.multiply(p, s)",
                new Point2D.Double(500.769446, 988.3442140000001),
                MathUtil.multiply(p1, 3.14159));
        Assert.assertEquals("MathUtil.multiply(s, p)",
                new Point2D.Double(988.3442140000001, 500.769446),
                MathUtil.multiply(3.14159, p2));
        Assert.assertEquals("MathUtil.multiply(p, x, y)",
                new Point2D.Double(500.516, 5002.14),
                MathUtil.multiply(p1, 3.14, 15.9));
        Assert.assertEquals("MathUtil.multiply(p, p)",
                new Point2D.Double(50147.240000000005, 50147.240000000005),
                MathUtil.multiply(p1, p2));
    }

    @Test
    public void testDividePoint2D() {
        Point2D p = new Point2D.Double(159.4, 314.6);
        Assert.assertEquals("MathUtil.divide(p, s)",
                new Point2D.Double(50.73863871479092, 100.14037477837657),
                MathUtil.divide(p, 3.14159));
        Assert.assertEquals("MathUtil.divide(p, x, y)",
                new Point2D.Double(50.76433121019108, 19.78616352201258),
                MathUtil.divide(p, 3.14, 15.9));
    }

    @Test
    public void testOffset() {
        Point2D p = new Point2D.Double(159.4, 314.6);
        Assert.assertEquals("MathUtil.offset(p, x, y)",
                new Point2D.Double(162.54, 330.5), MathUtil.offset(p, 3.14, 15.9));
    }

    @Test
    public void testRotate() {
        Assert.assertEquals("MathUtil.rotateRAD(x, y, a)",
                new Point2D.Double(-10.407653711754921, 33.62217637536562),
                MathUtil.rotateRAD(31.4, 15.9, 26.535));
        Assert.assertEquals("MathUtil.rotateDEG(x, y, a)",
                new Point2D.Double(30.63022962954949, 17.336638452741308),
                MathUtil.rotateDEG(31.4, 15.9, 2.6535));

        Point2D p = new Point2D.Double(159.4, 314.6);
        Assert.assertEquals("MathUtil.rotateRAD(p, a)",
                new Point2D.Double(-317.60286988977776, 153.32950478553346),
                MathUtil.rotateRAD(p, 1.59));
        Assert.assertEquals("MathUtil.rotateDEG(p, a)",
                new Point2D.Double(-27.951196946534395, 351.56827301287586),
                MathUtil.rotateDEG(p, 31.4159));

        Point2D c = new Point2D.Double(314.6, 159.4);
        Assert.assertEquals("MathUtil.rotateRAD(p, c, a)",
                new Point2D.Double(162.40884342950076, 1.2483896328153605),
                MathUtil.rotateRAD(p, c, 1.59));
        Assert.assertEquals("MathUtil.rotateDEG(p, c, a)",
                new Point2D.Double(101.25390735346647, 210.9511857521893),
                MathUtil.rotateDEG(p, c, 31.4159));

    }

    @Test
    public void testOrthogonal() {
        Point2D p = new Point2D.Double(159.4, 314.6);
        Assert.assertEquals("MathUtil.offset(p, x, y)",
                new Point2D.Double(-314.6, 159.4), MathUtil.orthogonal(p));
    }

    @Test
    public void testVectors() {
        Assert.assertEquals("MathUtil.vectorDEG(d, m)",
                new Point2D.Double(50.88327631306455, 31.071475530594356),
                MathUtil.vectorDEG(31.41, 59.62));
        Assert.assertEquals("MathUtil.vectorRAD(d, m)",
                new Point2D.Double(59.618952961759454, -0.35333800179572555),
                MathUtil.vectorRAD(31.41, 59.62));
    }

    @Test
    public void testDot() {
        Point2D p1 = new Point2D.Double(31.4, 15.9);
        Point2D p2 = new Point2D.Double(159.4, 314.6);
        Assert.assertEquals("MathUtil.Dot(p1, p2)", 10007.3,
                MathUtil.dot(p1, p2), tolerance);
    }

    @Test
    public void testLengthSquared() {
        Point2D p = new Point2D.Double(31.4, 15.9);
        Assert.assertEquals("MathUtil.lengthSquared(p1)", 1238.77,
                MathUtil.lengthSquared(p), tolerance);
    }

    @Test
    public void testLength() {
        Point2D p = new Point2D.Double(31.4, 15.9);
        Assert.assertEquals("MathUtil.length(p1)", 35.196164563770296,
                MathUtil.length(p), tolerance);
    }

    @Test
    public void testDistance() {
        Point2D p1 = new Point2D.Double(31.4, 15.9);
        Point2D p2 = new Point2D.Double(159.4, 314.6);
        Assert.assertEquals("MathUtil.distance(p1, p2)", 324.97029094980365,
                MathUtil.distance(p1, p2), tolerance);
    }

    @Test
    public void testNormalize() {
        Point2D p = new Point2D.Double(31.4, 15.9);
        Assert.assertEquals("MathUtil.normalize(p)",
                new Point2D.Double(0.8921426635310731, 0.45175376911286824),
                MathUtil.normalize(p));
        Assert.assertEquals("MathUtil.normalize(p, l)",
                new Point2D.Double(2.802746470322584, 1.4192251235072957),
                MathUtil.normalize(p, 3.14159));
    }

    @Test
    public void testComputeAngles() {
        Point2D p1 = new Point2D.Double(31.4, 15.9);
        Assert.assertEquals("MathUtil.computeAngleRAD(p)", 1.1020661694371947,
                MathUtil.computeAngleRAD(p1), tolerance);
        Assert.assertEquals("MathUtil.computeAngleDEG(p)", 63.143740252900734,
                MathUtil.computeAngleDEG(p1), tolerance);

        Point2D p2 = new Point2D.Double(159.4, 314.6);
        Assert.assertEquals("MathUtil.computeAngleRAD(p1, p2)", -2.7367412729776444,
                MathUtil.computeAngleRAD(p1, p2), tolerance);
        Assert.assertEquals("MathUtil.computeAngleDEG(p1, p2)", -156.80372456087935,
                MathUtil.computeAngleDEG(p1, p2), tolerance);
    }

    @Test
    public void testIntLerp() {
        Assert.assertEquals("MathUtil.lerp(i1, i2)", 36, MathUtil.lerp(31, 41, 0.59));
    }

    @Test
    public void testDoubleLerp() {
        boolean passed = true;    // assume success (optimist!)
        double minV = -666.66, maxV = +999.99;
        Double minD = new Double(minV);
        Double maxD = new Double(maxV);
        for (double theV = 0.0; theV < 2.f; theV += 0.15) {
            double c = MathUtil.lerp(minV, maxV, theV);
            double t = (c - minV) / (maxV - minV);
            Assert.assertEquals("MathUtil.lerp(min, max, v)", t, theV, tolerance);
            passed = (math.fabs(t - theV) <= tolerance);
            if (!passed) {
                break;
            }

            Double theD = new Double(theV);
            Double cD = MathUtil.lerp(minD, maxD, theD);
            Double tD = (cD - minD) / (maxD - minD);
            Assert.assertEquals("MathUtil.lerp(minD, maxD, vD)", tD, theD, tolerance);
            passed = (math.fabs(tD - theD) <= tolerance);
            if (!passed) {
                break;
            }
        }
        Assert.assertTrue("Double lerp", passed);
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
            Assert.assertEquals(f, t, tolerance);
            passed = (math.fabs(t - f) <= tolerance);
            if (!passed) {
                break;
            }
        }
        Assert.assertEquals("Point2D lerp is good", true, passed);
    }

    @Test
    public void testPoint2D_oneThird() {
        boolean passed = true;    // assume success (optimist!)
        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);

        Point2D pC = MathUtil.oneThirdPoint(pA, pB);
        double distanceAC = pA.distance(pC);
        double t = distanceAC / distanceAB;
        Assert.assertEquals(1.0 / 3.0, t, tolerance);
        passed = (math.fabs(t - (1.0 / 3.0)) <= tolerance);

        Assert.assertEquals("Point2D third is good", true, passed);
    }

    @Test
    public void testPoint2D_twoThirds() {
        boolean passed = true;    // assume success (optimist!)
        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);

        Point2D pC = MathUtil.twoThirdsPoint(pA, pB);
        double distanceAC = pA.distance(pC);
        double t = distanceAC / distanceAB;
        Assert.assertEquals(2.0 / 3.0, t, tolerance);
        passed = (math.fabs(t - (2.0 / 3.0)) <= tolerance);

        Assert.assertEquals("Point2D two third is good", true, passed);
    }

    @Test
    public void testPoint2D_oneFourth() {
        boolean passed = true;    // assume success (optimist!)
        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);

        Point2D pC = MathUtil.oneFourthPoint(pA, pB);
        double distanceAC = pA.distance(pC);
        double t = distanceAC / distanceAB;
        Assert.assertEquals(1.0 / 4.0, t, tolerance);

        passed = (math.fabs(t - (1.0 / 4.0)) <= tolerance);
        Assert.assertEquals("Point2D fourth is good", true, passed);
    }

    @Test
    public void testPoint2D_threeFourths() {
        boolean passed = true;    // assume success (optimist!)
        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);

        Point2D pC = MathUtil.threeFourthsPoint(pA, pB);
        double distanceAC = pA.distance(pC);
        double t = distanceAC / distanceAB;
        Assert.assertEquals(3.0 / 4.0, t, tolerance);

        passed = (math.fabs(t - (3.0 / 4.0)) <= tolerance);
        Assert.assertEquals("Point2D three fourths is good", true, passed);
    }

    @Test
    public void testGranulize() {
        Assert.assertEquals("MathUtil.granulize(v, g)", 314.2,
                MathUtil.granulize(314.15926, 0.1), tolerance);

        Point2D p = new Point2D.Double(31.4159, 15.926283);
        Assert.assertEquals("MathUtil.granulize(p, h, v)",
                new Point2D.Double(31.42, 15.93),
                MathUtil.granulize(p, 0.01, 0.01));
        Assert.assertEquals("MathUtil.granulize(p, h, v)",
                new Point2D.Double(31.400000000000002, 15.950000000000001),
                MathUtil.granulize(p, 0.05));
    }

    @Test
    public void testMidPoint() {
        Point2D p1 = new Point2D.Double(31.4, 15.9);
        Point2D p2 = new Point2D.Double(159.4, 314.6);
        Assert.assertEquals("MathUtil.midPoint(p1, p2)",
                new Point2D.Double(95.4, 165.25), MathUtil.midPoint(p1, p2));
    }

    @Test
    public void testInt_wrap() {
        Assert.assertEquals("MathUtil.wrap(int, min, max)", 313,
                MathUtil.wrap(623, 159, 314));
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
            Assert.assertEquals(t, c, tolerance);
            passed = (math.fabs(t - c) <= tolerance);
            if (!passed) {
                break;
            }
        }
        Assert.assertEquals("Double wrap is good", true, passed);
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
            Assert.assertEquals(t, c, tolerance);
            passed = (math.fabs(t - c) <= tolerance);
            if (!passed) {
                break;
            }
        }
        Assert.assertEquals("Double wrapPM180 is good", true, passed);
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
            Assert.assertEquals(t, c, tolerance);
            passed = (math.fabs(t - c) <= tolerance);
            if (!passed) {
                break;
            }
        }
        Assert.assertEquals("Double wrapPM360 is good", true, passed);
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
            Assert.assertEquals(t, c, tolerance);
            passed = (math.fabs(t - c) <= tolerance);
            if (!passed) {
                break;
            }
        }
        Assert.assertEquals("Double wrap360 is good", true, passed);
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
            Assert.assertEquals(t, c, tolerance);
            passed = (math.fabs(t - c) <= tolerance);
            if (!passed) {
                break;
            }
        }
        Assert.assertEquals("Double normalizeAngle is good", true, passed);
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
                Assert.assertEquals(t, c, tolerance);
                passed = (math.fabs(t - c) <= tolerance);
                if (!passed) {
                    break;
                }
            }
            if (!passed) {
                break;
            }
        }
        Assert.assertEquals("Double diffAngle is good", true, passed);
    }

    @Test
    public void testDouble_diffAngleRAD() {

        Assert.assertEquals("MathUtil.diffAngleRAD(a, b)", -1.2759999999999998,
                MathUtil.diffAngleRAD(0.314, 1.59), tolerance);
    }

    @Test
    public void testDouble_absDiffAngleRAD() {

        Assert.assertEquals("MathUtil.absDiffAngleRAD(a, b)", +1.2759999999999998,
                MathUtil.absDiffAngleRAD(0.314, 1.59), tolerance);
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
                Assert.assertEquals(t, c, tolerance);
                passed = (math.fabs(t - c) <= tolerance);
                if (!passed) {
                    break;
                }
            }
            if (!passed) {
                break;
            }
        }
        Assert.assertEquals("Double absDiffAngle is good", true, passed);
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
                    Assert.assertEquals(t, d, tolerance);
                    passed = (math.fabs(t - d) <= tolerance);
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
        Assert.assertEquals("Double pin is good", true, passed);
    }

    @Test
    public void test_rectangleToRectangle2D() {
        Rectangle r = new Rectangle(11, 22, 33, 44);
        Rectangle2D rD = new Rectangle2D.Double(11, 22, 33, 44);
        Assert.assertEquals("MathUtil.rectangleToRectangle2D(r)", rD,
                MathUtil.rectangleToRectangle2D(r));
    }

    @Test
    public void test_origin() {
        Rectangle2D rD = new Rectangle2D.Double(3.14, 1.59, 33.3, 44.4);
        Assert.assertEquals("MathUtil.origin(r)", new Point2D.Double(3.14, 1.59),
                MathUtil.origin(rD));
    }

    @Test
    public void test_size() {
        Rectangle2D rD = new Rectangle2D.Double(3.14, 1.59, 33.3, 44.4);
        Assert.assertEquals("MathUtil.size(r)", new Point2D.Double(33.3, 44.4),
                MathUtil.size(rD));
    }

    @Test
    public void test_center() {
        Rectangle2D rD = new Rectangle2D.Double(3.14, 1.59, 33.3, 44.4);
        Assert.assertEquals("MathUtil.center(r)", new Point2D.Double(19.79, 23.79),
                MathUtil.center(rD));
    }

    @Test
    public void test_midPoint() {
        Rectangle2D rD = new Rectangle2D.Double(3.14, 1.59, 33.3, 44.4);
        Assert.assertEquals("MathUtil.midPoint(r)", new Point2D.Double(19.79, 23.79),
                MathUtil.midPoint(rD));
    }

    @Test
    public void test_offset() {
        Rectangle2D rD = new Rectangle2D.Double(3.14, 1.59, 33.3, 44.4);
        Assert.assertEquals("MathUtil.offset(r, h, v)",
                new Rectangle2D.Double(36.44, 45.99, 33.3, 44.4),
                MathUtil.offset(rD, 33.3, 44.4));
        Point2D p = new Point2D.Double(31.41, 15.9);
        Assert.assertEquals("MathUtil.offset(r, p)",
                new Rectangle2D.Double(34.55, 17.490000000000002, 33.3, 44.4),
                MathUtil.offset(rD, p));
    }

    @Test
    public void test_inset() {
        Rectangle2D rD = new Rectangle2D.Double(31.4, 15.9, 33.3, 44.4);
        Assert.assertEquals("MathUtil.inset(r, i)",
                new Rectangle2D.Double(34.54159, 19.04159, 27.016819999999996, 38.11682),
                MathUtil.inset(rD, 3.14159));
        Assert.assertEquals("MathUtil.inset(r, h, v)",
                new Rectangle2D.Double(34.73, 20.34, 26.639999999999997, 35.519999999999996),
                MathUtil.inset(rD, 3.33, 4.44));
    }

    @Test
    public void test_scale() {
        Rectangle2D rD = new Rectangle2D.Double(31.4, 15.9, 33.3, 44.4);
        Assert.assertEquals("MathUtil.scale(r, s)",
                new Rectangle2D.Double(98.64592599999999, 49.951281, 104.61494699999999, 139.486596),
                MathUtil.scale(rD, 3.14159));
    }

    @Test
    public void test_centerRectangleOnPoint() {
        Rectangle2D rD = new Rectangle2D.Double(31.4, 15.9, 33.3, 44.4);
        Assert.assertEquals("MathUtil.centerRectangleOnPoint(r, p)",
                new Rectangle2D.Double(316.65, 422.19999999999993, 33.3, 44.4),
                MathUtil.centerRectangleOnPoint(rD, new Point2D.Double(333.3, 444.4)));
    }

    @Test
    public void test_centerRectangleOnRectangle() {
        Rectangle2D rD = new Rectangle2D.Double(31.4, 15.9, 33.3, 44.4);
        Rectangle2D rC = new Rectangle2D.Double(314.1, 59.6, 33.3, 44.4);
        Assert.assertEquals("MathUtil.centerRectangleOnRectangle(r1, r2)",
                new Rectangle2D.Double(314.09999999999997, 59.599999999999994, 33.3, 44.4),
                MathUtil.centerRectangleOnRectangle(rD, rC));
    }

    @Test
    public void test_rectangleAtPoint() {
        Point2D p = new Point2D.Double(31.41, 15.9);
        Assert.assertEquals("MathUtil.rectangleAtPoint(r1, r2)",
                new Rectangle2D.Double(31.41, 15.9, 22.2, 33.3),
                MathUtil.rectangleAtPoint(p, 22.2, 33.3));
    }

    @Test
    public void test_intersect() {
        Point2D p1 = new Point2D.Double(111.1, 222.2);
        Point2D p2 = new Point2D.Double(151.1, 252.2);
        Point2D p3 = new Point2D.Double(110.2, 210.5);
        Point2D p4 = new Point2D.Double(101.9, 252.4);
        Assert.assertEquals("MathUtil.intersect(p1, p2, p3, p4)",
                new Point2D.Double(-1399.3178701298561, -910.613402597392),
                MathUtil.intersect(p1, p2, p3, p4));

        // coliner lines (no solution)
        Assert.assertNull("MathUtil.intersect(p1, p2, p1, p2)",
                MathUtil.intersect(p1, p2, p1, p2));
        Assert.assertNull("MathUtil.intersect(p1, p1, p2, p2)",
                MathUtil.intersect(p1, p1, p2, p2));

        // parallel lines (no solution)
        Point2D p1P = new Point2D.Double(121.1, 232.2);
        Point2D p2P = new Point2D.Double(161.1, 262.2);
        Assert.assertNull("MathUtil.intersect(p1, p1, p2, p2)",
                MathUtil.intersect(p1, p2, p1P, p2P));
    }

    // from here down is testing infrastructure
    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
    }

    @After
    public void tearDown() throws Exception {
        jmri.util.JUnitUtil.tearDown();
    }

    //private final static Logger log = LoggerFactory.getLogger(MathUtilTest.class);
}
