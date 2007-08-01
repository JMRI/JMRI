// ReadingTest.java

package jmri.jmrix.rps;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the rps.Reading class.
 * @author	Bob Jacobsen Copyright 2006
 * @version	$Revision: 1.1 $
 */
public class ReadingTest extends TestCase {

	public void testCtorAndID() {
	    double [] v = new double[]{0., 0., 0.};
	    Reading r = new Reading(21, v);
	    Assert.assertEquals("ID ok", 21, r.getID());
	}


	public void testCopyCtor() {
	    Reading r1 = new Reading(21, new double[]{0., 0., 0.});
	    Reading r2 = new Reading(r1);
	    Assert.assertEquals("ID ok", 21, r2.getID());
	}
        
	// from here down is testing infrastructure

	public ReadingTest(String s) {
            super(s);
	}

	// Main entry point
	static public void main(String[] args) {
            String[] testCaseName = {ReadingTest.class.getName()};
            junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
            TestSuite suite = new TestSuite(ReadingTest.class);
            return suite;
	}

}
