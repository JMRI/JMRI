/** 
 * JmritTest.java
 *
 * Description:	    tests for the Jmrit package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrit;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

public class JmritTest extends TestCase {

	// from here down is testing infrastructure
	
	public JmritTest(String s) {
		super(s);
	}

	// a dummy test to avoid JUnit warning
	public void testDemo() {
		assert(true);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {JmritTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(JmritTest.class);
		suite.addTest(jmri.tests.jmrit.suite());
		return suite;
	}
	
}
