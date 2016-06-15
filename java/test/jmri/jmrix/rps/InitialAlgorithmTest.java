package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.Measurement class.
 *
 * The default transmitter location for the 7, 13, 13, 13 readings is (0,0,12)
 *
 * @author	Bob Jacobsen Copyright 2006
 * @version	$Revision$
 */
public class InitialAlgorithmTest extends TestCase {

    double vs = 0.0344; // SI default for testing

    public void testCalc4() {
        Reading r = new Reading("21", new double[]{0., 7. / vs, 13. / vs, 13. / vs, 13. / vs});

        Point3d s1 = new Point3d(0.0f, 0.0f, 5.0f);
        Point3d s2 = new Point3d(-3.0f, 4.0f, 0.0f);
        Point3d s3 = new Point3d(-3.0f, -4.0f, 0.0f);
        Point3d s4 = new Point3d(5.0f, 0.0f, 0.0f);

        Calculator c = new InitialAlgorithm(s1, s2, s3, s4, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getID());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    public void testCalc3_not4() {
        Reading r = new Reading("21", new double[]{0., 7. / vs, 13. / vs, 13. / vs});

        Point3d s1 = new Point3d(0.0f, 0.0f, 5.0f);
        Point3d s2 = new Point3d(-3.0f, 4.0f, 0.0f);
        Point3d s3 = new Point3d(-3.0f, -4.0f, 0.0f);

        Calculator c = new InitialAlgorithm(s1, s2, s3, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getID());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    public void testCalc3_not1() {
        Reading r = new Reading("21", new double[]{0., 13. / vs, 13. / vs, 13. / vs});

        Point3d s2 = new Point3d(-3.0f, 4.0f, 0.0f);
        Point3d s3 = new Point3d(-3.0f, -4.0f, 0.0f);
        Point3d s4 = new Point3d(5.0f, 0.0f, 0.0f);

        Calculator c = new InitialAlgorithm(s2, s3, s4, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getID());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    public void testCalc3_not2() {
        Reading r = new Reading("21", new double[]{0., 7. / vs, 13. / vs, 13. / vs});

        Point3d s1 = new Point3d(0.0f, 0.0f, 5.0f);
        Point3d s3 = new Point3d(-3.0f, -4.0f, 0.0f);
        Point3d s4 = new Point3d(5.0f, 0.0f, 0.0f);

        Calculator c = new InitialAlgorithm(s1, s3, s4, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getID());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    public void testCalc3_not3() {
        Reading r = new Reading("21", new double[]{0., 7. / vs, 13. / vs, 13. / vs});

        Point3d s1 = new Point3d(0.0f, 0.0f, 5.0f);
        Point3d s2 = new Point3d(-3.0f, 4.0f, 0.0f);
        Point3d s4 = new Point3d(5.0f, 0.0f, 0.0f);

        Calculator c = new InitialAlgorithm(s1, s2, s4, vs);

        Measurement m = c.convert(r, new Point3d(1.f, 1.f, 10.f));
        Assert.assertEquals("ID ok", "21", m.getID());
        Assert.assertEquals("x close", true, Math.abs(m.x - 0.) < 0.001);
        Assert.assertEquals("y close", true, Math.abs(m.y - 0.) < 0.001);
        Assert.assertEquals("z close", true, Math.abs(m.z - 12.) < 0.001);
    }

    // from here down is testing infrastructure
    public InitialAlgorithmTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {InitialAlgorithmTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        apps.tests.AllTest.initLogging();
        TestSuite suite = new TestSuite(InitialAlgorithmTest.class);
        return suite;
    }

}
