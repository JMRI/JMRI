// DispatcherTest.java

package jmri.jmrit.dispatcher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.dispatcher package
 * @author			Dave Duchamp
 * @version $Revision$
 */
public class DispatcherTest extends TestCase {

	// from here down is testing infrastructure

	public DispatcherTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {DispatcherTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrit.dispatcher.DispatcherTest"); // no tests in class itself
		suite.addTest(jmri.jmrit.dispatcher.DispatcherTrainInfoTest.suite());
		suite.addTest(jmri.jmrit.dispatcher.DispatcherTrainInfoFileTest.suite());
        // GUI tests start here
        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
            suite.addTest(jmri.jmrit.dispatcher.DispatcherFrameTest.suite());
        }
		return suite;
	}

}
