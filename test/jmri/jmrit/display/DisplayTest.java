/** 
 * DisplayTest.java
 *
 * Description:	    tests for the jmrit.display package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrit.display;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

public class DisplayTest extends TestCase {

	// from here down is testing infrastructure	
	public DisplayTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {DisplayTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrit.display");   // no tests in this class itself
		suite.addTest(jmri.jmrit.display.TurnoutIconTest.suite());
		return suite;
	}
	
}
