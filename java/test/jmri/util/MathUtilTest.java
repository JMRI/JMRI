package jmri.util;

import static jmri.util.MathUtil.*;
import static org.python.modules.math.fabs;

import java.awt.geom.Point2D;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of MathUtil
 *
 * @author	George Warner Copyright (C) 2017
 */
public class MathUtilTest {

    // changed this comment so github commit would restart integration tests.
    static final double tolerance = 0.00001;

    @Test
    public void testDouble_lerp() {
        boolean passed = true;    // assume success (optimist!)
        for (double a = -66.6; a < +66.6; a += 11.1) {
            for (double b = -66.6; b < +66.6; b += 11.1) {
                for (double f = 0.0; f < 2.f; f += 0.15) {
                    double c = lerp(a, b, f);
                    double t = (c - a) / (a - b);
                    Assert.assertEquals("Double lerp", f, t);
                    passed = (fabs(t - f) <= tolerance);
                    if (c < a || c > f * b) {
                        passed = false;
                    }
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
        Assert.assertEquals("Double lerp", true, passed);
    }

    @Test
    public void testPoint2D_lerp() {
        boolean passed = true;    // assume success (optimist!)
        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);
        for (double f = 0.0; f < 2.f; f += 0.15) {
            Point2D pC = lerp(pA, pB, f);
            double distanceAC = pA.distance(pC);
            double t = distanceAC / distanceAB;
            Assert.assertEquals("Point2D lerp", f, t);
            passed = (fabs(t - f) <= tolerance);
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

        Point2D pC = third(pA, pB);
        double distanceAC = pA.distance(pC);
        double t = distanceAC / distanceAB;
        Assert.assertEquals("Point2D third", (1.0 / 3.0), t);
        passed = (fabs(t - (1.0/3.0)) <= tolerance);

        Assert.assertEquals("Point2D third is good", true, passed);
    }

    @Test
    public void testPoint2D_fourth() {
        boolean passed = true;    // assume success (optimist!)
        Point2D pA = new Point2D.Double(666.0, 999.0);
        Point2D pB = new Point2D.Double(999.0, 666.0);
        double distanceAB = pA.distance(pB);

        Point2D pC = MathUtil.fourth(pA, pB);
        double distanceAC = pA.distance(pC);
        double t = distanceAC / distanceAB;
        Assert.assertEquals("Point2D fourth", (1.0 / 4.0), t);

        passed = (fabs(t - (1.0/3.0)) <= tolerance);
        Assert.assertEquals("Point2D fourth is good", true, passed);
    }

    @Test
    public void testDouble_wrap() {
        boolean passed = true;    // assume success (optimist!)
        double limits = 666.6;
        for (double a = -3.0 * limits; a < +3.0 * limits; a += limits / 10.0) {
            double t = a;
            while (t > +limits) {t -= limits;};
            while (t < -limits) {t += limits;};
            double c = wrap(a, -limits, +limits);
            Assert.assertEquals("Double wrap", t, c);
            passed = (fabs(t - c) <= tolerance);
            if (!passed) {
                break;
            }
        }
        Assert.assertEquals("Double wrap is good", true, passed);
    }

    @Test
    public void testDouble_wrapPM180() {
        boolean passed = true;    // assume success (optimist!)
        double limits = 180.0;
        for (double a = -3.0 * limits; a < +3.0 * limits; a += limits / 10.0) {
            double t = a;
            while (t > +limits) {t -= limits;};
            while (t < -limits) {t += limits;};
            double c = wrapPM180(a);
            Assert.assertEquals("Double wrapPM180", t, c);
            passed = (fabs(t - c) <= tolerance);
            if (!passed) {
                break;
            }
        }
        Assert.assertEquals("Double wrapPM180 is good", true, passed);
    }

    @Test
    public void testDouble_wrapPM360() {
        boolean passed = true;    // assume success (optimist!)
        double limits = 360.0;
        for (double a = -3.0 * limits; a < +3.0 * limits; a += limits / 10.0) {
            double t = a;
            while (t > +limits) {t -= limits;};
            while (t < -limits) {t += limits;};
            double c = wrapPM360(a);
            Assert.assertEquals("Double wrapPM360", t, c);
            passed = (fabs(t - c) <= tolerance);
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
        for (double a = -3.0 * limits; a < +3.0 * limits; a += limits / 10.0) {
            double t = a;
            while (t > +limits) {t -= limits;};
            while (t < 0.0) {t += limits;};
            double c = wrap360(a);
            Assert.assertEquals("Double wrap360", t, c);
            passed = (fabs(t - c) <= tolerance);
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
            while (t > +limits) {t -= limits;};
            while (t < 0.0) {t += limits;};
            double c = normalizeAngle(a);
            Assert.assertEquals("Double normalizeAngle", t, c);
            passed = (fabs(t - c) <= tolerance);
            if (!passed) {
                break;
            }
        }
        Assert.assertEquals("Double normalizeAngle is good", true, passed);
    }

    @Test
    public void testDouble_diffAngle() {
        boolean passed = true;    // assume success (optimist!)
        double limits = 180.0;
        for (double a = -3.3 * limits; a < +3.3 * limits; a += limits / 15.0) {
            for (double b = -3.3 * limits; b < +3.3 * limits; b += limits / 15.0) {
                double t = a - b;
                while (t > +limits) {t -= limits;};
                while (t < -limits) {t += limits;};
                t = fabs(t);
                double c = diffAngle(a, b);
                Assert.assertEquals("Double diffAngle", t, c);
                passed = (fabs(t - c) <= tolerance);
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
    public void testDouble_pin() {
        boolean passed = true;    // assume success (optimist!)
        double limits = 180.0;
        for (double a = -3.3 * limits; a < +3.3 * limits; a += limits / 15.0) {
            for (double b = -3.3 * limits; b < +3.3 * limits; b += limits / 15.0) {
                for (double c = -3.3 * limits; c < +3.3 * limits; c += limits / 15.0) {
                    double t = a;
                    if (t < b) {t = b;};
                    if (t > c) {t = c;};
                    double d = pin(a, b, c);
                    Assert.assertEquals("Double pin", t, c);
                    passed = (fabs(t - d) <= tolerance);
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
    public void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        // reset the instance manager.
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() throws Exception {
        // reset the instance manager.
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }
}
