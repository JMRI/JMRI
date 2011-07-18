/**
 * ConfigureXmlTest.java
 *
 * Description:	    tests for the jmrit.display.configurexml package
 * @author			Bob Jacobsen   Copyright 2009
 * @version         $Revision$
 */

package jmri.jmrit.display.configurexml;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class ConfigureXmlTest extends TestCase {

	// from here down is testing infrastructure
	public ConfigureXmlTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {ConfigureXmlTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrit.display.configurexml");   // no tests in this class itself
		suite.addTest(LoadFileTest.suite());
		return suite;
	}

}
