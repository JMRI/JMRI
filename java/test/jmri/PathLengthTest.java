package jmri;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.*;

/**
 * Tests for setting and changing path lengths of the Path class
 * Path lengths should be inherited from their parent block unless
 * explicitly set and must not exceed the length of their block.
 *
 * @author  Pete Cressman Copyright (C) 2015
 */
public class PathLengthTest extends TestCase {

    public void testDefaultPathLength() {
        Path p = new Path();
        Block b = new Block("IB1");
        p.setBlock(b);
        b.setLength(100);

        Assert.assertEquals("check default path length millimeters", 100f, p.getLengthMm(), 0.0);
        Assert.assertEquals("check default path length centimeters", 10f, p.getLengthCm(), 0.0);
        Assert.assertEquals("check default path length inches", 100/25.4f, p.getLengthIn(), 0.0);
        Assert.assertEquals("check raw path length", p.getLength(), 0f, 0.0);
    }

    public void testSetPathLength() {
        Path p = new Path();
        Block b = new Block("IB1");
        p.setBlock(b);
        b.addPath(p);
        b.setLength(100);
        p.setLength(50);

        Assert.assertEquals("check path length", 50f, p.getLengthMm(), 0.0);
        Assert.assertEquals("check block length", 100f, b.getLengthMm(), 0.0);

        p.setLength(150);
        Assert.assertEquals("check path length", b.getLengthMm(), p.getLengthMm(), 0.0);
        Assert.assertEquals("check block length", 100f, b.getLengthMm(), 0.0);
    }

    public void testChangePathLength() {
        Path p1 = new Path();
        Path p2 = new Path();
        Path p3 = new Path();
        Block b = new Block("IB1");
        p1.setBlock(b);
        b.addPath(p1);
        p2.setBlock(b);
        b.addPath(p2);
        p3.setBlock(b);
        b.addPath(p3);
        b.setLength(100);
        p1.setLength(80);
        p2.setLength(50);

        Assert.assertEquals("check path p1 length Millimeters", 80f, p1.getLengthMm(), 0.0);
        Assert.assertEquals("check path p2 length Millimeters", 50f, p2.getLengthMm(), 0.0);
        Assert.assertEquals("check path p3 length Millimeters", 100f, p3.getLengthMm(), 0.0);
        
        b.setLength(60);
        Assert.assertEquals("check change block length", 60f, b.getLengthMm(), 0.0);
        Assert.assertEquals("check change path p1 length", 60f, p1.getLengthMm(), 0.0);
        Assert.assertEquals("check raw path p1 length", 0f, p1.getLength(), 0.0);
        Assert.assertEquals("check change path p2 length", 50f, p2.getLengthMm(), 0.0);
        Assert.assertEquals("check raw path p2 length", 50f, p2.getLength(), 0.0);
        Assert.assertEquals("check change path p3 length", 60f, p3.getLengthMm(), 0.0);
        Assert.assertEquals("check raw path p3 length", 0f, p1.getLength(), 0.0);
    }

    // from here down is testing infrastructure
    public PathLengthTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PathLengthTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PathLengthTest.class);
        return suite;
    }

    @Override
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @Override
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
