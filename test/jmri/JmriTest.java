// JmriTest.java

package jmri;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

/**
 * Invoke complete set of tests for the Jmri package
 * @author			Bob Jacobsen, Copyright (C) 2001, 2002
 * @version         $Revision: 1.5 $
 */
public class JmriTest extends TestCase {

	// from here down is testing infrastructure

	public JmriTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {JmriTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite("jmri.JmriTest");  // no tests in this class itself
		suite.addTest(jmri.progdebugger.ProgDebuggerTest.suite());
		suite.addTest(jmri.NmraPacketTest.suite());
		suite.addTest(jmri.configurexml.ConfigXmlTest.suite());
		suite.addTest(jmri.jmrit.JmritTest.suite());
		suite.addTest(jmri.jmrix.JmrixTest.suite());
		return suite;
	}

}
