package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.Region class.
 *
 * @author	Bob Jacobsen Copyright 2007
 * @version	$Revision$
 */
public class RegionTest extends TestCase {

    public void testCtors() {
        // square
        new Region(new Point3d[]{
            new Point3d(0., 0., 0.),
            new Point3d(1., 0., 0.),
            new Point3d(1., 1., 0.),
            new Point3d(0., 1., 0.)}
        );

        // triangle
        new Region(new Point3d[]{
            new Point3d(0., 0., 0.),
            new Point3d(1., 0., 0.),
            new Point3d(1., 1., 0.)}
        );

    }

    public void testInside1() {
        // square
        Region r = new Region(new Point3d[]{
            new Point3d(0., 0., 0.),
            new Point3d(1., 0., 0.),
            new Point3d(1., 1., 0.),
            new Point3d(0., 1., 0.)}
        );

        Assert.assertTrue("inside", r.isInside(new Point3d(0.5, 0.5, 0.)));

        Assert.assertTrue("outside", !r.isInside(new Point3d(-0.5, 0.5, 0.)));
        Assert.assertTrue("outside", !r.isInside(new Point3d(0.5, -0.5, 0.)));
        Assert.assertTrue("outside", !r.isInside(new Point3d(1.5, 0.5, 0.)));
        Assert.assertTrue("outside", !r.isInside(new Point3d(0.5, 1.5, 0.)));

    }

    public void testInside2() {
        // C chape
        Region r = new Region(new Point3d[]{
            new Point3d(0., 0., 0.),
            new Point3d(3., 0., 0.),
            new Point3d(3., 1., 0.),
            new Point3d(1., 1., 0.),
            new Point3d(1., 2., 0.),
            new Point3d(2., 2., 0.),
            new Point3d(2., 3., 0.),
            new Point3d(0., 3., 0.),
            new Point3d(0., 0., 0.)}
        );

        Assert.assertTrue("inside", r.isInside(new Point3d(0.5, 0.5, 0.)));

        Assert.assertTrue("outside", !r.isInside(new Point3d(-0.5, 0.5, 0.)));
        Assert.assertTrue("outside", !r.isInside(new Point3d(0.5, -0.5, 0.)));
        Assert.assertTrue("outside", !r.isInside(new Point3d(1.5, 1.5, 0.)));
        Assert.assertTrue("outside", !r.isInside(new Point3d(3.0, 1.5, 0.)));

    }

    public void testEquals() {
        // square
        Region r1 = new Region(new Point3d[]{
            new Point3d(0., 0., 0.),
            new Point3d(1., 0., 0.),
            new Point3d(1., 1., 0.),
            new Point3d(0., 1., 0.)}
        );

        Region r2 = new Region(new Point3d[]{
            new Point3d(0., 0., 0.),
            new Point3d(1., 0., 0.),
            new Point3d(1., 1., 0.),
            new Point3d(0., 1., 0.)}
        );

        Region r3 = new Region(new Point3d[]{
            new Point3d(0., 0., 0.),
            new Point3d(2., 0., 0.),
            new Point3d(1., 1., 0.),
            new Point3d(0., 1., 0.)}
        );

        Region r4 = new Region(new Point3d[]{
            new Point3d(0., 0., 0.),
            new Point3d(1., 0., 0.),
            new Point3d(1., 1., 0.)}
        );

        Assert.assertTrue("r1==r2", r1.equals(r2));
        Assert.assertTrue("r1!=r3", !r1.equals(r3));
        Assert.assertTrue("r1!=r4", !r1.equals(r4));
        Assert.assertTrue("r4==r4", r4.equals(r4));

    }

    public void testStringCtor() {
        Region r1 = new Region(new Point3d[]{
            new Point3d(0., 0., 0.),
            new Point3d(1., 0., 0.),
            new Point3d(1., 1., 0.),
            new Point3d(0., 1., 0.)}
        );

        Region r1s = new Region("(0,0,0);(1,0,0);(1,1,0);(0,1,0)");
        Assert.assertTrue("r1==r1s", r1.equals(r1s));

    }

    // from here down is testing infrastructure
    public RegionTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", RegionTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(RegionTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
