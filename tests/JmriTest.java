/** 
 * JmriTest.java
 *
 * Description:	
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.tests.symbolicprog.*;


public class JmriTest extends TestCase {
	public JmriTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {JmriTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// a simple test skeleton
	public void testDemo() {
		assert(true);
	}
	
	// test suite
	public static Test suite() {
		// all tests from here
		TestSuite suite = new TestSuite(JmriTest.class);
		// all tests from other classes
		suite.addTest(SymbolicProgTest.suite());
		
		return suite;
	}
	
}
