/**
 * JmriTest.java
 *
 * Description:	    tests for the Jmri package
 * @author			Bob Jacobsen
 * @version
 */

package jmri.jmrix;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

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
		TestSuite suite = new TestSuite("jmri.jmrix");
		suite.addTest(jmri.jmrix.AbstractProgrammerTest.suite());
		suite.addTest(jmri.jmrix.lenz.XNetTest.suite());
		suite.addTest(jmri.jmrix.loconet.LocoNetTest.suite());
		suite.addTest(jmri.jmrix.nce.NceTest.suite());
		suite.addTest(jmri.jmrix.easydcc.EasyDccTest.suite());
		return suite;
	}

}
