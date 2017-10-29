package jmri.util;

import java.awt.geom.Point2D;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.python.modules.math;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Test simple functioning of MathUtil
 *
 * @author	George Warner Copyright (C) 2017
 */
public class MathUtilTest extends TestCase {

    static final double tolerance = 0.000001;

    @Test
    public void testDouble_lerp() {
        boolean passed = true;    // assume success (optimist!)
        double theMin = -666.66, theMax = +999.99;
        for (double f = 0.0; f < 2.f; f += 0.15) {
            double c = MathUtil.lerp(theMin, theMax, f);
            double t = (c - theMin) / (theMax - theMin);
            Assert.assertEquals(t, f, tolerance);
            passed = (math.fabs(t - f) <= tolerance);
            if (!passed) {
                break;
            }
        }
        Assert.assertEquals("Double lerp", true, passed);
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
    public void testPoint2D_third() {
        boolean passed = true;    // assume success (optimist!)
        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);

        Point2D pC = MathUtil.oneThirdPoint(pA, pB);
        double distanceAC = pA.distance(pC);
        double t = distanceAC / distanceAB;
        Assert.assertEquals(1.0 / 3.0, t, tolerance);
        passed = (math.fabs(t - (1.0/3.0)) <= tolerance);

        Assert.assertEquals("Point2D third is good", true, passed);
    }

    @Test
    public void testPoint2D_fourth() {
        boolean passed = true;    // assume success (optimist!)
        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);

        Point2D pC = MathUtil.oneFourthPoint(pA, pB);
        double distanceAC = pA.distance(pC);
        double t = distanceAC / distanceAB;
        Assert.assertEquals(1.0 / 4.0, t, tolerance);

        passed = (math.fabs(t - (1.0/4.0)) <= tolerance);
        Assert.assertEquals("Point2D fourth is good", true, passed);
    }

    @Test
    public void testDouble_wrap() {
        boolean passed = true;    // assume success (optimist!)
        double theLimits = 180.0;
        double theMin = -theLimits, theMax = +theLimits;
        double theRange = theMax - theMin;
        for (double a = -3.0 * theLimits; a < +3.0 * theLimits; a += theLimits / 10.0) {
            double t = a;
            while (t >= theMax) {t -= theRange;}
            while (t < theMin) {t += theRange;}
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
            while (t >= theMax) {t -= theRange;}
            while (t < theMin) {t += theRange;}
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
            while (t >= theMax) {t -= theRange;}
            while (t < theMin) {t += theRange;}
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
            while (t < 0.0) {t += limits;}
            while (t >= +limits) {t -= limits;}
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
            while (t >= +limits) {t -= limits;}
            while (t < 0.0) {t += limits;}
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
                while (t >= theMax) {t -= theRange;}
                while (t < theMin) {t += theRange;}
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
    public void testDouble_absDiffAngle() {
        boolean passed = true;    // assume success (optimist!)

        double theLimits = 180.0;
        double theMin = -theLimits, theMax = +theLimits;
        double theRange = theMax - theMin;
        for (double a = -3.3 * theLimits; a < +3.3 * theLimits; a += theLimits / 15.0) {
            for (double b = -3.3 * theLimits; b < +3.3 * theLimits; b += theLimits / 15.0) {
                double t = a - b;
                while (t >= theMax) {t -= theRange;}
                while (t < theMin) {t += theRange;}
                if (t < 0.0) { t = -t;}
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
                    if (t < b) {t = b;}
                    if (t > c) {t = c;}
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

    // from here down is testing infrastructure
    @Before
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
    }

    @After
    protected void tearDown() throws Exception {
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", MathUtilTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static junit.framework.Test suite() {
        TestSuite suite = new TestSuite(MathUtilTest.class);
        return suite;
    }

    //private final static Logger log = LoggerFactory.getLogger(MathUtilTest.class);
}
