/** 
 * JmriTest.java
 *
 * Description:	    tests for the Jmri package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

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
		AllTest.initLogging();
		TestSuite suite = new TestSuite("jmri.tests.JmriTest");  // no tests in this class itself
		suite.addTest(jmri.tests.NmraPacketTest.suite());
		suite.addTest(jmri.tests.symbolicprog.SymbolicProgTest.suite());
		return suite;
	}
	
}
