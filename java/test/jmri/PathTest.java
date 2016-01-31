// PathTest.java
package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Path class
 *
 * @author	Bob Jacobsen Copyright (C) 2006
 * @version $Revision$
 */
public class PathTest extends TestCase {

    @SuppressWarnings("all")
    public void testCreate() {
        Path p = new Path();
        Assert.assertTrue("default to direction", p.getToBlockDirection() == Path.NONE);
        Assert.assertTrue("default from direction", p.getFromBlockDirection() == Path.NONE);

        // code requires, as a limitation, that NONE be zero
        Assert.assertTrue("NONE must be zero", 0 == Path.NONE);

    }

    public void testLoad() {
        Path p = new Path();

        TurnoutManager sm = new jmri.managers.InternalTurnoutManager();
        Turnout s = sm.provideTurnout("IT12");

        p.addSetting(new BeanSetting(s, "IT12", Turnout.CLOSED));

        Block b = new Block("IB1");
        p.setBlock(b);
    }

    public void testEquals() {
        TurnoutManager sm = new jmri.managers.InternalTurnoutManager();
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
    
    public void testBlockRetrieve() {
        Path p = new Path();

        Block b = new Block("IB1");
        p.setBlock(b);

        Assert.assertEquals("check block retreival", b.getSystemName(),
                p.getBlock().getSystemName());
    }

    public void testCheck() throws JmriException {
        Path p = new Path();

        TurnoutManager sm = new jmri.managers.InternalTurnoutManager();
        Turnout s = sm.provideTurnout("IT12");

        p.addSetting(new BeanSetting(s, "IT12", Turnout.CLOSED));

        Assert.assertTrue("check path not set", !p.checkPathSet());

        s.setState(Turnout.CLOSED);
        Assert.assertTrue("check path set", p.checkPathSet());

    }

    public void testShortPathCheck() {
        Path p = new Path();
        // no elements; always true
        Assert.assertTrue("check path set", p.checkPathSet());

    }

    public void testFormat() {
        //Path p = new Path();
        // default direction
        Assert.assertTrue("None", Path.decodeDirection(Path.NONE).equals("None"));
        Assert.assertTrue("Left", Path.decodeDirection(Path.LEFT).equals("Left"));
        Assert.assertTrue("Right", Path.decodeDirection(Path.RIGHT).equals("Right"));
        Assert.assertTrue("Up", Path.decodeDirection(Path.UP).equals("Up"));
        Assert.assertTrue("Down", Path.decodeDirection(Path.DOWN).equals("Down"));
        Assert.assertTrue("CW", Path.decodeDirection(Path.CW).equals("CW"));
        Assert.assertTrue("CCW", Path.decodeDirection(Path.CCW).equals("CCW"));
        Assert.assertTrue("East", Path.decodeDirection(Path.EAST).equals("East"));
        Assert.assertTrue("West", Path.decodeDirection(Path.WEST).equals("West"));
        Assert.assertTrue("North", Path.decodeDirection(Path.NORTH).equals("North"));
        Assert.assertTrue("South", Path.decodeDirection(Path.SOUTH).equals("South"));
        Assert.assertTrue("Unknown", Path.decodeDirection(0x100000).equals("Unknown: 0x100000"));
        Assert.assertEquals("South|Up", Path.decodeDirection(Path.SOUTH | Path.UP), "South, Up");

    }

    // from here down is testing infrastructure
    public PathTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {PathTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PathTest.class);
        return suite;
    }

    protected void setUp() {
        jmri.InstanceManager.store(new jmri.NamedBeanHandleManager(), jmri.NamedBeanHandleManager.class);
    }

}
