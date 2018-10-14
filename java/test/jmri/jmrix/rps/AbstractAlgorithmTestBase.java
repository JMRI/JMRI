package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Common test scaffolding for Algorithm implementations.
 *
 * <p>
 * The default transmitter location for the 7, 13, 13, 13 readings is (0,0,12)
 *
 * <p>
 * Implementing subclasses should provide static "main" and "suite" methods.
 *
 * @author	Bob Jacobsen Copyright 2008
 */
abstract public class AbstractAlgorithmTestBase {

    abstract Calculator getAlgorithm(Point3d[] pts, double vs);

    double vs = 0.0344;  // SI default for testing

    Point3d[] receivers1 = new Point3d[]{
        null,
        new Point3d(0.0f, 0.0f, 10.0f),
        new Point3d(-3.0f, 4.0f, 5.0f),
        new Point3d(-3.0f, -4.0f, 5.0f),
        new Point3d(5.0f, 0.0f, 5.0f),
        new Point3d(0.0f, 0.0f, 9.0f)
    };
    Point3d result1 = new Point3d(3.0, 2.0, 1.0);
    double[] distances1 = distances(receivers1, vs, result1);

    @Test
    public void testCalc1() {
        testCalc(receivers1, distances1, result1, "Test 1", distances1.length, 3);
    }

    Point3d[] receivers2 = new Point3d[]{
        null,
        new Point3d(0.0f, 0.0f, 15.0f),
        new Point3d(-30.0f, 40.0f, 10.0f),
        new Point3d(-30.0f, -40.0f, 10.0f),
        new Point3d(50.0f, 0.0f, 10.0f),
        new Point3d(0.0f, 0.0f, 19.0f)
    };
    Point3d result2 = new Point3d(3.0, 2.0, 1.0);
    double[] distances2 = distances(receivers2, vs, result2);

    @Test
    public void testCalc2() {
        testCalc(receivers2, distances2, result2, "Test 2", distances2.length, 3);
    }

    Point3d[] receivers3 = new Point3d[]{
        null,
        new Point3d(0.0f, 0.0f, 15.0f),
        new Point3d(-30.0f, 40.0f, 10.0f),
        new Point3d(-30.0f, -40.0f, 10.0f),
        new Point3d(50.0f, 0.0f, 10.0f),
        new Point3d(23.0f, 0.0f, 19.0f),
        new Point3d(0.0f, 30.0f, 9.0f),
        new Point3d(-14.0f, 0.0f, 19.0f),
        new Point3d(33.0f, 23.0f, 18.0f),
        new Point3d(0.0f, 12.0f, 19.0f),
        new Point3d(100.0f, 0.0f, 19.0f),
        new Point3d(0.0f, 200.0f, 19.0f),
        new Point3d(50.0f, 50.0f, 19.0f)
    };
    Point3d result3 = new Point3d(3.0, 2.0, 1.0);
    double[] distances3 = distances(receivers3, vs, result3);

    @Test
    public void testCalc3() {
        testCalc(receivers3, distances3, result3, "Test 3", distances3.length, 3);
    }

    // bad point
    Point3d[] receivers4 = new Point3d[]{
        null,
        new Point3d(0.0f, 0.0f, 15.0f),
        new Point3d(-30.0f, 40.0f, 10.0f),
        new Point3d(-30.0f, -40.0f, 10.0f),
        new Point3d(50.0f, 0.0f, 10.0f),
        new Point3d(30.0f, 12.0f, 19.0f)
    };
    Point3d result4 = new Point3d(3.0, 2.0, 1.0);
    double[] distances4 = distances(receivers4, vs, result4);

    @Test
    public void testCalc4() {
        distances4[distances4.length - 1] = 9999.;
        testCalc(receivers4, distances4, result4, "Test 4", distances4.length - 1, 3);
    }

    Point3d[] receivers5 = new Point3d[]{
        null,
        new Point3d(-3.0f, 4.0f, 9.0f),
        new Point3d(-3.0f, -4.0f, 10.0f),
        new Point3d(5.0f, 0.0f, 11.0f),
        new Point3d(0.0f, 13.0f, 9.0f)
    };
    Point3d result5 = new Point3d(3.0, 2.0, 1.0);
    double[] distances5 = distances(receivers5, vs, result5);

    @Test
    public void testCalc5() {
        testCalc(receivers5, distances5, result5, "Test 5", distances5.length, 3);
    }

    Point3d[] receivers6 = new Point3d[]{
        null,
        new Point3d(10.0f, 0.0f, 12.0f),
        new Point3d(-3.0f, -4.0f, 10.0f),
        new Point3d(5.0f, 0.0f, 11.0f),
        new Point3d(0.0f, 13.0f, 9.0f)
    };
    Point3d result6 = new Point3d(3.0, 2.0, 1.0);
    double[] distances6 = distances(receivers6, vs, result6);

    @Test
    public void testCalc6() {
        testCalc(receivers6, distances6, result6, "Test 6", distances6.length, 3);
    }

    Point3d[] receivers7 = new Point3d[]{
        null,
        new Point3d(10.0f, 0.0f, 12.0f),
        new Point3d(-3.0f, 4.0f, 9.0f),
        new Point3d(5.0f, 0.0f, 11.0f),
        new Point3d(0.0f, 13.0f, 9.0f)
    };
    Point3d result7 = new Point3d(3.0, 2.0, 1.0);
    double[] distances7 = distances(receivers7, vs, result7);

    @Test
    public void testCalc7() {
        testCalc(receivers7, distances7, result7, "Test 7", distances7.length, 3);
    }

    // infrastructure
    double tolerance = 0.05;

    double distance(Point3d p, Point3d q) {
        return Math.sqrt(
                (p.x - q.x) * (p.x - q.x)
                + (p.y - q.y) * (p.y - q.y)
                + (p.z - q.z) * (p.z - q.z)
        );
    }

    double time(Point3d p) {
        Point3d q = new Point3d(3.0, 2.0, 1.0);
        return distance(p, q) / vs;
    }

    double[] distances(Point3d[] ps, double vs, Point3d location) {
        double[] retval = new double[ps.length];
        for (int i = 0; i < ps.length; i++) {
            if (ps[i] != null) {
                retval[i] = distance(ps[i], location) / vs;
            } else {
                retval[i] = 0.;
            }
        }
        return retval;
    }

    public void testCalc(Point3d[] receivers, double[] distances, Point3d result, String label,
            int codemax, int codemin) {
        Reading r = new Reading("21", distances);

        Calculator c = getAlgorithm(receivers, vs);

        Measurement m = c.convert(r, result);

        Assert.assertEquals(label + " ID ok", "21", m.getId());

        if (!(Math.abs(m.x - result.x) < tolerance)) {
            System.err.println(label + " x not close " + m.x + " " + result.x);
        }
        Assert.assertEquals(label + " x close", true, Math.abs(m.x - result.x) < tolerance);

        if (!(Math.abs(m.y - result.y) < tolerance)) {
            System.err.println(label + " y not close " + m.y + " " + result.y);
        }
        Assert.assertEquals(label + " y close", true, Math.abs(m.y - result.y) < tolerance);

        if (!(Math.abs(m.z - result.z) < tolerance)) {
            System.err.println(label + " z not close " + m.z + " " + result.z);
        }
        Assert.assertEquals(label + " z close", true, Math.abs(m.z - result.z) < tolerance);

        if (m.getCode() > codemax || m.getCode() < codemin) {
            System.err.println(label + " bad code " + m.getCode());
        }
        Assert.assertTrue(label + " code > min", m.getCode() >= codemin);
        Assert.assertTrue(label + " code <= max", m.getCode() <= codemax);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
