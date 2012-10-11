// OperationsTest.java

package jmri.jmrit.operations;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.operations package
 * @author		Bob Coleman
 * @version $Revision$
 */
public class OperationsTest extends TestCase {

	// from here down is testing infrastructure

	public OperationsTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {OperationsTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite("jmri.jmrit.operations.OperationsTest"); // no tests in class itself
		suite.addTest(jmri.jmrit.operations.setup.OperationsSetupTest.suite());

//		if (!System.getProperty("jmri.headlesstest","false").equals("true"))
		suite.addTest(jmri.jmrit.operations.locations.OperationsLocationsTest.suite()); // references Swing, so skipped

		suite.addTest(jmri.jmrit.operations.rollingstock.OperationsRollingStockTest.suite());
		suite.addTest(jmri.jmrit.operations.rollingstock.cars.OperationsCarsTest.suite());
		suite.addTest(jmri.jmrit.operations.rollingstock.engines.OperationsEnginesTest.suite());
		suite.addTest(jmri.jmrit.operations.routes.OperationsRoutesTest.suite());

//		if (!System.getProperty("jmri.headlesstest","false").equals("true"))
		suite.addTest(jmri.jmrit.operations.trains.OperationsTrainsTest.suite());  // references Swing, so skipped

//		if (!System.getProperty("jmri.headlesstest","false").equals("true")) 
		suite.addTest(jmri.jmrit.operations.router.OperationsCarRouterTest.suite());  // references Swing, so skipped


        if (!System.getProperty("jmri.headlesstest","false").equals("true")) {
            // GUI tests start here
            suite.addTest(jmri.jmrit.operations.setup.OperationsSetupGuiTest.suite());
            suite.addTest(jmri.jmrit.operations.locations.OperationsLocationsGuiTest.suite());
            suite.addTest(jmri.jmrit.operations.rollingstock.cars.OperationsCarsGuiTest.suite());
            suite.addTest(jmri.jmrit.operations.rollingstock.engines.OperationsEnginesGuiTest.suite());
            suite.addTest(jmri.jmrit.operations.routes.OperationsRoutesGuiTest.suite());
            suite.addTest(jmri.jmrit.operations.trains.OperationsTrainsGuiTest.suite());
        }
        
		// Last test, deletes log file if one exists
		suite.addTest(jmri.jmrit.operations.rollingstock.OperationsLoggerTest.suite());
		
		return suite;
	}

}
