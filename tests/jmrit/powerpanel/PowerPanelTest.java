/** 
 * PowerPanelTest.java
 *
 * Description:	    tests for the jmrit.PowerPanel package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrit;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PowerPanelTest extends TestCase {

	// from here down is testing infrastructure
	
	public PowerPanelTest(String s) {
		super(s);
	}

	// a dummy test to avoid JUnit warning
	public void testDemo() {
		assert(true);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {PowerPanelTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(PowerPanelTest.class);
		return suite;
	}
	
}
