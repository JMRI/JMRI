/** 
 * AllTest.java
 *
 * Description:	    Driver for JMRI project test classes
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.tests.symbolicprog.*;
import jmri.tests.*;


public class AllTest extends TestCase  {
	public AllTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) { 
		String[] testCaseName = {AllTest.class.getName()};
    	// initialize log4j
    	System.out.println("Initialize log4j");
    	org.apache.log4j.BasicConfigurator.configure();
    	org.apache.log4j.Category.getInstance("jmri.progdebugger.ProgDebugger").info("Test message");
		// initialize log4j
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// a simple test skeleton
	public void testDemo() {
		assert(true);
	}
	
	// test suite
	public static Test suite() {
		// all tests from here
		TestSuite suite = new TestSuite(AllTest.class);
		// all tests from other classes
		suite.addTest(JmriTest.suite());
		suite.addTest(SymbolicProgTest.suite());
		
		return suite;
	}
	
  static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance("jmri");

}
