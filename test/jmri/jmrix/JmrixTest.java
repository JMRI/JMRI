//JmrixTest.java

package jmri.jmrix;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Set of tests for the jmri.jmrix package
 * @author	Bob Jacobsen  Copyright 2003
 * @version         $Revision: 1.9 $
 */
public class JmrixTest extends TestCase {

	// from here down is testing infrastructure

	public JmrixTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {JmrixTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite("jmri.jmrix.JmrixTest");
		suite.addTest(jmri.jmrix.AbstractProgrammerTest.suite());
		suite.addTest(jmri.jmrix.cmri.serial.SerialTest.suite());
		suite.addTest(jmri.jmrix.direct.DirectTest.suite());
		suite.addTest(jmri.jmrix.easydcc.EasyDccTest.suite());
		suite.addTest(jmri.jmrix.lenz.XNetTest.suite());
		suite.addTest(jmri.jmrix.loconet.LocoNetTest.suite());
		suite.addTest(jmri.jmrix.nce.NceTest.suite());
		suite.addTest(jmri.jmrix.pricom.PricomTest.suite());
		return suite;
	}

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
