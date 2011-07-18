// BlockManagerTest.java

package jmri;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the BlockManager class
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version $Revision$
 */
public class BlockManagerTest extends TestCase {

    /**
     * Normally, users create Block objects via a manager, 
     * but we test the direct create here.  If it works, we can 
     * use it for testing.
     */
	public void testDirectCreate() {
	    new Block("SystemName");
	}

/* 	public void testShortDelay() { */
/* 		SimpleTimebase p = new SimpleTimebase(); */
/* 		Date now = new Date(); */
/* 		p.setTime(now); */
/* 		p.setRate(100.); */
/* 		wait(100); */
/* 		Date then = p.getTime(); */
/* 		long delta = then.getTime()-now.getTime(); */
/* 		Assert.assertTrue("delta ge 50 (nominal value)", delta>=50); */
/* 		Assert.assertTrue("delta lt 150 (nominal value)", delta<150); */
/* 	} */

	// from here down is testing infrastructure

	public BlockManagerTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {BlockManagerTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(BlockManagerTest.class);
		return suite;
	}

}
