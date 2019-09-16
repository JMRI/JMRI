package jmri;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the Path class
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 */
public class PathTest {

    @Test
    @SuppressWarnings("all")
    public void testCreate() {
        Path p = new Path();
        Assert.assertTrue("default to direction", p.getToBlockDirection() == Path.NONE);
        Assert.assertTrue("default from direction", p.getFromBlockDirection() == Path.NONE);

        // code requires, as a limitation, that NONE be zero
        Assert.assertTrue("NONE must be zero", 0 == Path.NONE);

    }

    @Test
    public void testLoad() {
        Path p = new Path();

        TurnoutManager sm = jmri.InstanceManager.turnoutManagerInstance();
        Turnout s = sm.provideTurnout("IT12");

        p.addSetting(new BeanSetting(s, "IT12", Turnout.CLOSED));

        Block b = new Block("IB1");
        p.setBlock(b);
        Assert.assertEquals("block added",b,p.getBlock());
    }

    @Test
    public void testEquals() {
        TurnoutManager sm = jmri.InstanceManager.turnoutManagerInstance();
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

        Assert.assertEquals("check block retreival", b.getSystemName(),
                p.getBlock().getSystemName());
    }

    @Test
    public void testCheck() throws JmriException {
        Path p = new Path();

        TurnoutManager sm = jmri.InstanceManager.turnoutManagerInstance();
        Turnout s = sm.provideTurnout("IT12");

        p.addSetting(new BeanSetting(s, "IT12", Turnout.CLOSED));

        Assert.assertTrue("check path not set", !p.checkPathSet());

        s.setState(Turnout.CLOSED);
        Assert.assertTrue("check path set", p.checkPathSet());

    }

    @Test
    public void testShortPathCheck() {
        Path p = new Path();
        // no elements; always true
        Assert.assertTrue("check path set", p.checkPathSet());

    }

    @Test
    public void testFormat() {
        //Path p = new Path();
        // default direction
        Assert.assertEquals("None", "None", Path.decodeDirection(Path.NONE));
        Assert.assertEquals("Left", "Left", Path.decodeDirection(Path.LEFT));
        Assert.assertEquals("Right", "Right", Path.decodeDirection(Path.RIGHT));
        Assert.assertEquals("Up", "Up", Path.decodeDirection(Path.UP));
        Assert.assertEquals("Down", "Down", Path.decodeDirection(Path.DOWN));
        Assert.assertEquals("CW", "CW", Path.decodeDirection(Path.CW));
        Assert.assertEquals("CCW", "CCW", Path.decodeDirection(Path.CCW));
        Assert.assertEquals("North", "North", Path.decodeDirection(Path.NORTH));
        Assert.assertEquals("East", "East", Path.decodeDirection(Path.EAST));
        Assert.assertEquals("West", "West", Path.decodeDirection(Path.WEST));
        Assert.assertEquals("South", "South", Path.decodeDirection(Path.SOUTH));
        Assert.assertEquals("North-East", "Northeast", Path.decodeDirection(Path.NORTH_EAST));
        Assert.assertEquals("South-East", "Southeast", Path.decodeDirection(Path.SOUTH_EAST));
        Assert.assertEquals("South-West", "Southwest", Path.decodeDirection(Path.SOUTH_WEST));
        Assert.assertEquals("North-West", "Northwest", Path.decodeDirection(Path.NORTH_WEST));
        Assert.assertEquals("Unknown", "Unknown: 0x100000", Path.decodeDirection(0x100000));
        Assert.assertEquals("South and Up", "South, Up", Path.decodeDirection(Path.SOUTH | Path.UP));

    }

    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.tearDown();
    }

}
