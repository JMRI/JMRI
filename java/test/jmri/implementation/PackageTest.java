/**
 * PackageTest.java
 *
 * Description:	    tests for the jmri.implementation package
 * @author			Bob Jacobsen  2009
 * @version         $Revision$
 */

package jmri.implementation;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class PackageTest extends TestCase {

	// from here down is testing infrastructure
	public PackageTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {PackageTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.implementation");   // no tests in this class itself
		suite.addTest(NamedBeanTest.suite());
		suite.addTest(RouteTest.suite());
		suite.addTest(AbstractLocoAddressTest.suite());
		suite.addTest(DefaultSignalSystemTest.suite());
		suite.addTest(DefaultSignalAppearanceMapTest.suite());
		suite.addTest(DccSignalMastTest.suite());
		suite.addTest(DccSignalHeadTest.suite());
		suite.addTest(SE8cSignalHeadTest.suite());
        suite.addTest(SignalHeadSignalMastTest.suite());
        suite.addTest(SingleIndexProgrammerFacadeTest.suite());
        suite.addTest(SingleIndexMultiProgrammerFacadeTest.suite());
        suite.addTest(SingleTurnoutSignalHeadTest.suite());
        suite.addTest(SignalSystemFileCheckTest.suite());

        suite.addTest(jmri.implementation.swing.SwingShutDownTaskTest.suite());
        suite.addTest(ReporterTest.suite());
        suite.addTest(DefaultIdTagTest.suite());

		return suite;
	}

}
