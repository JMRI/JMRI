package jmri;

import java.awt.geom.Point2D;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests for the Path class
 *
 * @author Bob Jacobsen Copyright (C) 2006
 */
public class PathTest {

    @Test
    @SuppressWarnings("all")
    public void testCreate() {
        Path p = new Path();
        assertTrue( p.getToBlockDirection() == Path.NONE, "default to direction");
        assertTrue( p.getFromBlockDirection() == Path.NONE, "default from direction");

        // code requires, as a limitation, that NONE be zero
        assertTrue( 0 == Path.NONE, "NONE must be zero");

    }

    @Test
    public void testLoad() {
        Path p = new Path();

        TurnoutManager sm = InstanceManager.getDefault(TurnoutManager.class);
        Turnout s = sm.provideTurnout("IT12");

        p.addSetting(new BeanSetting(s, "IT12", Turnout.CLOSED));

        Block b = new Block("IB1");
        p.setBlock(b);
        assertEquals( b,p.getBlock(), "block added");
    }

    @Test
    public void testEquals() {
        TurnoutManager sm = InstanceManager.getDefault(TurnoutManager.class);
        Turnout s1 = sm.provideTurnout("IT12");
        Turnout s2 = sm.provideTurnout("IT14");
        
        Path p1 = new Path();
        Path p2 = new Path();
        Path p3 = new Path();
        Path p4 = new Path();
        
        assertTrue(p1.equals(p2));

        p1.addSetting(new BeanSetting(s1, "IT12", Turnout.CLOSED));
        assertFalse(p1.equals(p2));
        
        p2.addSetting(new BeanSetting(s1, "IT12", Turnout.CLOSED));
        assertTrue(p1.equals(p2));
        
        p3.addSetting(new BeanSetting(s1, "IT12", Turnout.THROWN));
        assertFalse(p1.equals(p3));

        p4.addSetting(new BeanSetting(s2, "IT14", Turnout.CLOSED));
        assertFalse(p1.equals(p4));

        Block b1 = new Block("IB1");
        p1.setBlock(b1);
        assertFalse(p1.equals(p2));

        p2.setBlock(b1);
        assertTrue(p1.equals(p2));

        Block b2 = new Block("IB2");
        p1.setBlock(b2);
        assertFalse(p1.equals(p2));
    }
    
    @Test
    public void testBlockRetrieve() {
        Path p = new Path();

        Block b = new Block("IB1");
        p.setBlock(b);

        assertEquals( b.getSystemName(), p.getBlock().getSystemName(),
            "check block retreival");
    }

    @Test
    public void testCheck() throws JmriException {
        Path p = new Path();

        TurnoutManager sm = InstanceManager.getDefault(TurnoutManager.class);
        Turnout s = sm.provideTurnout("IT12");

        p.addSetting(new BeanSetting(s, "IT12", Turnout.CLOSED));

        assertFalse( p.checkPathSet(), "check path not set");

        s.setState(Turnout.CLOSED);
        assertTrue( p.checkPathSet(), "check path set");
    }

    @Test
    public void testPathToString() throws JmriException {
        Path p = new Path();

        assertEquals("Path: <no block>: ", p.toString());

        TurnoutManager sm = InstanceManager.getDefault(TurnoutManager.class);
        Turnout s = sm.provideTurnout("IT12");

        p.addSetting(new BeanSetting(s, "IT12", Turnout.CLOSED));

        assertEquals("Path: <no block>: IT12 with state Closed", p.toString());
    }

    @Test
    public void testShortPathCheck() {
        Path p = new Path();
        // no elements; always true
        assertTrue( p.checkPathSet(), "check path set");

    }

    @Test
    public void testDirection() {
        int dir;
        
        dir = Path.computeDirection(new Point2D.Double(10.,10.), new Point2D.Double(10.,20.));
        assertEquals(Path.SOUTH, dir);

        dir = Path.computeDirection(new Point2D.Double(10.,10.), new Point2D.Double(20.,20.));
        assertEquals(Path.SOUTH_EAST, dir);
    }

    @Test
    public void testFormat() {
        //Path p = new Path();
        // default direction
        assertEquals( "None", Path.decodeDirection(Path.NONE), "None");
        assertEquals( "Left", Path.decodeDirection(Path.LEFT), "Left");
        assertEquals( "Right", Path.decodeDirection(Path.RIGHT), "Right");
        assertEquals( "Up", Path.decodeDirection(Path.UP), "Up");
        assertEquals( "Down", Path.decodeDirection(Path.DOWN), "Down");
        assertEquals( "CW", Path.decodeDirection(Path.CW), "CW");
        assertEquals( "CCW", Path.decodeDirection(Path.CCW), "CCW");
        assertEquals( "North", Path.decodeDirection(Path.NORTH), "North");
        assertEquals( "East", Path.decodeDirection(Path.EAST), "East");
        assertEquals( "West", Path.decodeDirection(Path.WEST), "West");
        assertEquals( "South", Path.decodeDirection(Path.SOUTH), "South");
        assertEquals( "Northeast", Path.decodeDirection(Path.NORTH_EAST), "North-East");
        assertEquals( "Southeast", Path.decodeDirection(Path.SOUTH_EAST), "South-East");
        assertEquals( "Southwest", Path.decodeDirection(Path.SOUTH_WEST), "South-West");
        assertEquals( "Northwest", Path.decodeDirection(Path.NORTH_WEST), "North-West");
        assertEquals( "Unknown: 0x100000", Path.decodeDirection(0x100000), "Unknown");
        assertEquals( "South, Up", Path.decodeDirection(Path.SOUTH | Path.UP), "South and Up");

    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        
        JUnitUtil.resetInstanceManager();
        InstanceManager.store(new NamedBeanHandleManager(), NamedBeanHandleManager.class);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
