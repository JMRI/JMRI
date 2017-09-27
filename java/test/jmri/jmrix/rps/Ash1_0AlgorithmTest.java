package jmri.jmrix.rps;

import apps.tests.Log4JFixture;
import javax.vecmath.Point3d;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * JUnit tests for the rps.Ash1_0Algorithm class.
 *
 * This algorithm tends to pick arbitrary solutions with only three sensors, so
 * we test with four and more.
 *
 * The default transmitter location for the 7, 13, 13, 13 readings is (0,0,12)
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class Ash1_0AlgorithmTest {

    double vs = 0.0344;  // SI default for testing

    // right answer to these is 0,0,12
    @Test
    @Ignore // fails for unknown reasons
    public void testCalc5() {
        Reading r = new Reading("21", new double[]{7. / vs, 13. / vs, 13. / vs, 13. / vs, 3. / vs});

        Point3d s1 = new Point3d(0.0f, 0.0f, 5.0f);
        Point3d s2 = new Point3d(-3.0f, 4.0f, 0.0f);
        Point3d s3 = new Point3d(-3.0f, -4.0f, 0.0f);
        Point3d s4 = new Point3d(5.0f, 0.0f, 0.0f);
        Point3d s5 = new Point3d(0.0f, 0.0f, 15.0f);
        Point3d s[] = new Point3d[]{s1, s2, s3, s4, s5};

        Calculator c = new Ash1_0Algorithm(s, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getId());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    @Test
    @Ignore // fails for unknown reasons
    public void testCalc4_not1() {
        Reading r = new Reading("21", new double[]{13. / vs, 13. / vs, 13. / vs, 3. / vs});

        //Point3d s1 = new Point3d(0.0f,  0.0f, 5.0f);
        Point3d s2 = new Point3d(-3.0f, 4.0f, 0.0f);
        Point3d s3 = new Point3d(-3.0f, -4.0f, 0.0f);
        Point3d s4 = new Point3d(5.0f, 0.0f, 0.0f);
        Point3d s5 = new Point3d(0.0f, 0.0f, 15.0f);
        Point3d s[] = new Point3d[]{s2, s3, s4, s5};

        Calculator c = new Ash1_0Algorithm(s, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getId());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    @Test
    @Ignore // fails for unknown reasons
    public void testCalc4_not2() {
        Reading r = new Reading("21", new double[]{7. / vs, 13. / vs, 13. / vs, 3. / vs});

        Point3d s1 = new Point3d(0.0f, 0.0f, 5.0f);
        //Point3d s2 = new Point3d(-3.0f, 4.0f, 0.0f);
        Point3d s3 = new Point3d(-3.0f, -4.0f, 0.0f);
        Point3d s4 = new Point3d(5.0f, 0.0f, 0.0f);
        Point3d s5 = new Point3d(0.0f, 0.0f, 15.0f);
        Point3d s[] = new Point3d[]{s1, s3, s4, s5};

        Calculator c = new Ash1_0Algorithm(s, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getId());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    @Test
    @Ignore // fails for unknown reasons
    public void testCalc4_not3() {
        Reading r = new Reading("21", new double[]{7. / vs, 13. / vs, 13. / vs, 3. / vs});

        Point3d s1 = new Point3d(0.0f, 0.0f, 5.0f);
        Point3d s2 = new Point3d(-3.0f, 4.0f, 0.0f);
        //Point3d s3 = new Point3d(-3.0f,-4.0f, 0.0f);
        Point3d s4 = new Point3d(5.0f, 0.0f, 0.0f);
        Point3d s5 = new Point3d(0.0f, 0.0f, 15.0f);
        Point3d s[] = new Point3d[]{s1, s2, s4, s5};

        Calculator c = new Ash1_0Algorithm(s, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getId());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
    }
    
    @After
    public void tearDown() {
        Log4JFixture.tearDown();
    }
}
