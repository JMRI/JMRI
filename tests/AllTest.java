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
		// initialize junit
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// a simple test skeleton
	public void testDemo() {
		assert(true);
	}
	
	// test suite
	public static Test suite() {
		boolean log4jinit = true;
		if (log4jinit) {
			log4jinit = false;
	   		org.apache.log4j.BasicConfigurator.configure();
	   		// only log warnings and above
	   		org.apache.log4j.Category.getRoot().setPriority(org.apache.log4j.Priority.INFO);
		}
		// all tests from here
		TestSuite suite = new TestSuite(AllTest.class);
		// all tests from other classes
		suite.addTest(JmriTest.suite());
		suite.addTest(SymbolicProgTest.suite());
		
		return suite;
	}
	
  static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance("jmri");

}
