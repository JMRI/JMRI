// ConfigXmlTest.java

package jmri.configurexml;

import java.io.*;
import java.util.*;
import javax.swing.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

import jmri.*;
import jmri.progdebugger.*;

/**
 * Test the jmri.configxml package.
 * @author			Bob Jacobsen
 * @version         $Revision: 1.1 $
 */
public class ConfigXmlTest extends TestCase {

	// from here down is testing infrastructure

	public ConfigXmlTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {ConfigXmlTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests, including others in the package
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.config.ConfigXmlTest");  // no tests in this class itself
		suite.addTest(jmri.configurexml.ConfigXmlManagerTest.suite());
		return suite;
	}

    // The minimal setup for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

}
