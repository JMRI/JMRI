/** 
 * LocoIOTest.java
 *
 * Description:	    tests for the jmri.jmrix.loconet package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.jmrix.loconet.locoio;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

import jmri.jmrix.loconet.*;

public class LocoIOTest extends TestCase {

	// from here down is testing infrastructure
	
	public LocoIOTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {LocoIOTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrix.loconet.LocoNetTest");  // no tests in this class itself
		suite.addTest(LocoIOFrameTest.suite());
		suite.addTest(LocoIOTableModelTest.suite());
		return suite;
	}
	 
	 static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(LocoIOTest.class.getName());

}
