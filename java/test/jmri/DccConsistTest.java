// DccConsistTest.java

package jmri;

import jmri.implementation.DccConsist;
import org.apache.log4j.Logger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert;

/**
 * Test simple functioning of DccConsist
 *
 * @author			Paul Copyright (C) 2011
 * @version			$Revision$
 */

public class DccConsistTest extends TestCase {

	public void testCtor() {
                // DccLocoAddress constructor test.
                DccConsist c = new DccConsist(new DccLocoAddress(12, true));
                Assert.assertNotNull(c);
	}

        public void testCtor2() {
               // integer constructor test.
                DccConsist c = new DccConsist(new DccLocoAddress(12, true));
                Assert.assertNotNull(c);
        }

	// from here down is testing infrastructure
	public DccConsistTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {DccConsistTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite(DccConsistTest.class);
		return suite;
	}

	static Logger log = Logger.getLogger(DccConsistTest.class.getName());

}
