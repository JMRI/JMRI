// QsiTest.java

package jmri.jmrix.qsi;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmri.jmrix.qsi package
 * @author			Bob Jacobsen
 * @version         $Revision: 1.1 $
 */
public class QsiTest extends TestCase {

	// from here down is testing infrastructure

	public QsiTest(String s) {
		super(s);
	}

    // The minimal setup is for log4J
    apps.tests.Log4JFixture log4jfixtureInst = new apps.tests.Log4JFixture(this);
    protected void setUp() { log4jfixtureInst.setUp(); }
    protected void tearDown() { log4jfixtureInst.tearDown(); }

	// a dummy test to avoid JUnit warning
	public void testDemo() {
		assertTrue(true);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {QsiTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		apps.tests.AllTest.initLogging();
		TestSuite suite = new TestSuite("jmri.jmrix.qsi.QsiTest");
		suite.addTest(jmri.jmrix.qsi.qsimon.QsiMonFrameTest.suite());
		suite.addTest(jmri.jmrix.qsi.packetgen.PacketGenFrameTest.suite());
		suite.addTest(jmri.jmrix.qsi.QsiTrafficControllerTest.suite());
		suite.addTest(jmri.jmrix.qsi.QsiMessageTest.suite());
		suite.addTest(jmri.jmrix.qsi.QsiReplyTest.suite());
		return suite;
	}

}
