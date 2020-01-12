package jmri.jmrix.rps;

import javax.vecmath.Point3d;
import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * JUnit tests for the rps.Region class.
 *
 * @author	Bob Jacobsen Copyright 2007
 */
public class RegionTest {

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
