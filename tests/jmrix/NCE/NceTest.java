/** 
 * NceTest.java
 *
 * Description:	    tests for the jmri.jmrix.nce package
 * @author			Bob Jacobsen
 * @version			
 */

package jmri.tests.jmrix.nce;

import java.io.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.*;
import org.jdom.output.*;

public class NceTest extends TestCase {

	// from here down is testing infrastructure
	
	public NceTest(String s) {
		super(s);
	}

	// a dummy test to avoid JUnit warning
	public void testDemo() {
		assert(true);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {NceTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}
	
	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.tests.jmrix.nce.NceTest");
		suite.addTest(jmri.jmrix.nce.packetgen.NcePacketGenFrameTest.suite());
		suite.addTest(jmri.jmrix.nce.NceTrafficControllerTest.suite());
		suite.addTest(jmri.tests.jmrix.nce.NceMessageTest.suite());
		suite.addTest(jmri.tests.jmrix.nce.NceReplyTest.suite());
		suite.addTest(jmri.tests.jmrix.nce.NcePowerManagerTest.suite());
		return suite;
	}
	
}
