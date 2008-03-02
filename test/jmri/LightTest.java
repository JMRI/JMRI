// LightTest.java

package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the Light class
 * @author	Bob Jacobsen  Copyright (C) 2008
 * @version $Revision: 1.1 $
 */
public class LightTest extends TestCase {

	public void testConstants() {
	    Assert.assertTrue("On and INTERMEDIATE are bits", (Light.ON&Light.INTERMEDIATE) == 0);
        
	    Assert.assertTrue("TRANSITIONINGTOFULLON overlap", (Light.TRANSITIONINGTOFULLON&Light.TRANSITIONING) != 0);
	    Assert.assertTrue("TRANSITIONINGHIGHER overlap", (Light.TRANSITIONINGHIGHER&Light.TRANSITIONING) != 0);
	    Assert.assertTrue("TRANSITIONINGLOWER overlap", (Light.TRANSITIONINGLOWER&Light.TRANSITIONING) != 0);
	    Assert.assertTrue("TRANSITIONINGTOFULLOFF overlap", (Light.TRANSITIONINGTOFULLOFF&Light.TRANSITIONING) != 0);
	}


	// from here down is testing infrastructure

	public LightTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LightTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(LightTest.class);
		return suite;
	}

}
