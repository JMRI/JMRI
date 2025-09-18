package jmri;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Tests for setting and changing path lengths of the Path class
 * Path lengths should be inherited from their parent block unless
 * explicitly set and must not exceed the length of their block.
 *
 * @author  Pete Cressman Copyright (C) 2015
 */
public class PathLengthTest {

    @Test
    public void testDefaultPathLength() {
        Path p = new Path();
        Block b = new Block("IB1");
        p.setBlock(b);
        b.setLength(100);

        assertEquals( 100f, p.getLengthMm(), 0.0, "check default path length millimeters");
        assertEquals( 10f, p.getLengthCm(), 0.0, "check default path length centimeters");
        assertEquals( 100/25.4f, p.getLengthIn(), 0.0, "check default path length inches");
        assertEquals( p.getLength(), 0f, 0.0, "check raw path length");
    }

    @Test
    public void testSetPathLength() {
        Path p = new Path();
        Block b = new Block("IB1");
        p.setBlock(b);
        b.addPath(p);
        b.setLength(100);
        assertEquals( 100f, b.getLengthMm(), 0.0, "check block length");
        assertEquals( p.getLengthMm(), b.getLengthMm(), 0.0, "check path length equals block length");

        p.setLength(50);
        assertEquals( 50f, p.getLengthMm(), 0.0, "check path length");
        p.setLength(150);
        assertEquals( 150f, p.getLengthMm(), 0.0, "check path length");
        assertEquals( 100f, b.getLengthMm(), 0.0, "check block length");
    }

    @Test
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

        assertEquals( 80f, p1.getLengthMm(), 0.0, "check path p1 length Millimeters");
        assertEquals( 50f, p2.getLengthMm(), 0.0, "check path p2 length Millimeters");
        assertEquals( 100f, p3.getLengthMm(), 0.0, "check path p3 length Millimeters");

        b.setLength(60);
        assertEquals( 60f, b.getLengthMm(), 0.0, "check change block length");
        assertEquals( 60f, p1.getLengthMm(), 0.0, "check change path p1 length");
        assertEquals( 0f, p1.getLength(), 0.0, "check raw path p1 length");
        assertEquals( 50f, p2.getLengthMm(), 0.0, "check change path p2 length");
        assertEquals( 50f, p2.getLength(), 0.0, "check raw path p2 length");
        assertEquals( 60f, p3.getLengthMm(), 0.0, "check change path p3 length");
        assertEquals( 0f, p1.getLength(), 0.0, "check raw path p3 length");
    }

    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @AfterEach
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
