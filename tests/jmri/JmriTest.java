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

	// a simple test skeleton
	public void testDemo() {
		assert(true);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {JmriTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(JmriTest.class);
		return suite;
	}
	
}
