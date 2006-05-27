// PathTest.java

package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Path class
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version $Revision: 1.1 $
 */
public class PathTest extends TestCase {

	public void testCreate() {
	    new Path();
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
