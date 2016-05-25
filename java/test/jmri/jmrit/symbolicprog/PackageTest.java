package jmri.jmrit.symbolicprog;

import jmri.InstanceManager;
import jmri.Programmer;
import jmri.managers.DefaultProgrammerManager;
import jmri.progdebugger.ProgDebugger;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test the jmri.jmrix.symbolicprog package.
 *
 * @author	Bob Jacobsen Copyright 2006
 */
public class PackageTest extends TestCase {

    // check configuring the programmer
    public void testConfigProgrammer() {
        // initialize the system
        Programmer p = new ProgDebugger();
        InstanceManager.setProgrammerManager(new DefaultProgrammerManager(p));
        assertTrue(InstanceManager.programmerManagerInstance().getGlobalProgrammer() == p);
    }

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests, including others in the package
    public static Test suite() {
        TestSuite suite = new TestSuite(PackageTest.class);
        suite.addTest(jmri.jmrit.symbolicprog.BundleTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.ArithmeticQualifierTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.QualifierAdderTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.FnMapPanelTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.CompositeVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.Pr1ImporterTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.ComboCheckBoxTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.ComboRadioButtonsTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.DecVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.EnumVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.HexVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.LongAddrVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.SplitVariableValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.CvValueTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.CvTableModelTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.VariableTableModelTest.suite());
        suite.addTest(jmri.jmrit.symbolicprog.CombinedLocoSelListPaneTest.suite());

        suite.addTest(jmri.jmrit.symbolicprog.tabbedframe.PackageTest.suite());
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
