/**
 * JmritTest.java
 *
 * Description:	    tests for the Jmrit package
 * @author			Bob Jacobsen
 * @version         $Revision: 1.3 $
 */

package jmri.jmrit;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class JmritTest extends TestCase {

    // The minimal setup is for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

	// from here down is testing infrastructure
	public JmritTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {JmritTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrit.JmritTest");   // no tests in this class itself
		suite.addTest(jmri.jmrit.display.DisplayTest.suite());
		suite.addTest(jmri.jmrit.AbstractIdentifyTest.suite());
		suite.addTest(jmri.jmrit.decoderdefn.DecoderDefnTest.suite());
		suite.addTest(jmri.jmrit.XmlFileTest.suite());
		suite.addTest(jmri.jmrit.symbolicprog.SymbolicProgTest.suite());
		suite.addTest(jmri.jmrit.powerpanel.PowerPanelTest.suite());
		suite.addTest(jmri.jmrit.roster.RosterTest.suite());
		return suite;
	}

}
