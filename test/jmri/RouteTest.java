// RouteTest.java

package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Route interface
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version $Revision: 1.1 $
 */
public class RouteTest extends TestCase {

	public void testConstants() {
	    Assert.assertTrue("ACTIVE not TOGGLE", Sensor.ACTIVE != Route.TOGGLE);
	    Assert.assertTrue("INACTIVE not TOGGLE", Sensor.INACTIVE != Route.TOGGLE);
	    Assert.assertTrue("CLOSED not TOGGLE", Turnout.THROWN != Route.TOGGLE);
	    Assert.assertTrue("THROWN not TOGGLE", Turnout.CLOSED != Route.TOGGLE);
	}

    
	// from here down is testing infrastructure

	public RouteTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {RouteTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(RouteTest.class);
		return suite;
	}

}
