// PathTest.java

package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Path class
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version $Revision: 1.2 $
 */
public class PathTest extends TestCase {

	public void testCreate() {
	    Path p = new Path();
	    Assert.assertTrue("default to direction", p.getToBlockDirection()==Path.NONE);
	    Assert.assertTrue("default from direction", p.getFromBlockDirection()==Path.NONE);

        // code requires, as a limitation, that NONE be zero
	    Assert.assertTrue("NONE must be zero", 0==Path.NONE);
        
	}

    public void testLoad() {
        Path p = new Path();

	    TurnoutManager sm = new jmri.managers.InternalTurnoutManager();
	    Turnout s = sm.provideTurnout("IT12");

        p.addSetting(new BeanSetting(s, Turnout.CLOSED));
        
        Block b = new Block("IB1");
        p.setBlock(b);
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

        p.addSetting(new BeanSetting(s, Turnout.CLOSED));
        
        Assert.assertTrue("check path not set", !p.checkPathSet());
        
        s.setState(Turnout.CLOSED);
        Assert.assertTrue("check path set", p.checkPathSet());
        
    }
    
    public void testShortPathCheck() throws JmriException {
        Path p = new Path();
        // no elements; always true
        Assert.assertTrue("check path set", p.checkPathSet());
        
    }
    
    public void testFormat() throws JmriException {
        Path p = new Path();
        // default direction
        Assert.assertTrue("None", p.decodeDirection(Path.NONE).equals("None"));
        Assert.assertTrue("Left", p.decodeDirection(Path.LEFT).equals("Left"));
        Assert.assertTrue("Right", p.decodeDirection(Path.RIGHT).equals("Right"));
        Assert.assertTrue("Up", p.decodeDirection(Path.UP).equals("Up"));
        Assert.assertTrue("Down", p.decodeDirection(Path.DOWN).equals("Down"));
        Assert.assertTrue("CW", p.decodeDirection(Path.CW).equals("CW"));
        Assert.assertTrue("CCW", p.decodeDirection(Path.CCW).equals("CCW"));
        Assert.assertTrue("East", p.decodeDirection(Path.EAST).equals("East"));
        Assert.assertTrue("West", p.decodeDirection(Path.WEST).equals("West"));
        Assert.assertTrue("North", p.decodeDirection(Path.NORTH).equals("North"));
        Assert.assertTrue("South", p.decodeDirection(Path.SOUTH).equals("South"));
        Assert.assertTrue("Unknown", p.decodeDirection(0x100000).equals("Unknown: 0x100000"));
        Assert.assertEquals("South|Up", p.decodeDirection(Path.SOUTH|Path.UP), "South, Up");
        
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

}
