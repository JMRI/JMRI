package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * JUnit tests for the rps.Measurement class.
 *
 * The default transmitter location for the 7, 13, 13, 13 readings is (0,0,12)
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class InitialAlgorithmTest {

    double vs = 0.0344; // SI default for testing

    @Test
    public void testCalc4() {
        Reading r = new Reading("21", new double[]{0., 7. / vs, 13. / vs, 13. / vs, 13. / vs});

        Point3d s1 = new Point3d(0.0f, 0.0f, 5.0f);
        Point3d s2 = new Point3d(-3.0f, 4.0f, 0.0f);
        Point3d s3 = new Point3d(-3.0f, -4.0f, 0.0f);
        Point3d s4 = new Point3d(5.0f, 0.0f, 0.0f);

        Calculator c = new InitialAlgorithm(s1, s2, s3, s4, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getId());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    @Test
    public void testCalc3_not4() {
        Reading r = new Reading("21", new double[]{0., 7. / vs, 13. / vs, 13. / vs});

        Point3d s1 = new Point3d(0.0f, 0.0f, 5.0f);
        Point3d s2 = new Point3d(-3.0f, 4.0f, 0.0f);
        Point3d s3 = new Point3d(-3.0f, -4.0f, 0.0f);

        Calculator c = new InitialAlgorithm(s1, s2, s3, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getId());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    @Test
    public void testCalc3_not1() {
        Reading r = new Reading("21", new double[]{0., 13. / vs, 13. / vs, 13. / vs});

        Point3d s2 = new Point3d(-3.0f, 4.0f, 0.0f);
        Point3d s3 = new Point3d(-3.0f, -4.0f, 0.0f);
        Point3d s4 = new Point3d(5.0f, 0.0f, 0.0f);

        Calculator c = new InitialAlgorithm(s2, s3, s4, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getId());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    @Test
    public void testCalc3_not2() {
        Reading r = new Reading("21", new double[]{0., 7. / vs, 13. / vs, 13. / vs});

        Point3d s1 = new Point3d(0.0f, 0.0f, 5.0f);
        Point3d s3 = new Point3d(-3.0f, -4.0f, 0.0f);
        Point3d s4 = new Point3d(5.0f, 0.0f, 0.0f);

        Calculator c = new InitialAlgorithm(s1, s3, s4, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getId());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    @Test
    public void testCalc3_not3() {
        Reading r = new Reading("21", new double[]{0., 7. / vs, 13. / vs, 13. / vs});

        Point3d s1 = new Point3d(0.0f, 0.0f, 5.0f);
        Point3d s2 = new Point3d(-3.0f, 4.0f, 0.0f);
        Point3d s4 = new Point3d(5.0f, 0.0f, 0.0f);

        Calculator c = new InitialAlgorithm(s1, s2, s4, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getId());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
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
