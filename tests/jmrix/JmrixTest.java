/** 
 * JmriTest.java
 *
 * Description:	    tests for the Jmri package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix;

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
		jmri.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite("jmri.tests.jmrix");
		suite.addTest(jmri.tests.jmrix.loconet.LocoNetTest.suite());
		suite.addTest(jmri.tests.jmrix.nce.NceTest.suite());
		return suite;
	}
	
}
