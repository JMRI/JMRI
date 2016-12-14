/**
 * PackageTest.java
 *
 * Description:	tests for the jmri.implementation package
 *
 * @author	Bob Jacobsen 2009
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
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.implementation");   // no tests in this class itself

        // fundamental aspects
        suite.addTest(NamedBeanTest.suite());

        // implementations
        suite.addTest(AbstractSensorTest.suite());
        suite.addTest(AccessoryOpsModeProgrammerFacadeTest.suite());
        suite.addTest(AddressedHighCvProgrammerFacadeTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(DccSignalHeadTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DccSignalMastTest.class));
        suite.addTest(DefaultConditionalTest.suite());
        suite.addTest(DefaultConditionalActionTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(DefaultIdTagTest.class));
        suite.addTest(DefaultLogixTest.suite());
        suite.addTest(DefaultSignalSystemTest.suite());
        suite.addTest(DefaultSignalAppearanceMapTest.suite());
        suite.addTest(MultiIndexProgrammerFacadeTest.suite());
        suite.addTest(OffsetHighCvProgrammerFacadeTest.suite());
        suite.addTest(ResettingOffsetHighCvProgrammerFacadeTest.suite());
        suite.addTest(RouteTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(SE8cSignalHeadTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SignalHeadSignalMastTest.class));
        suite.addTest(SignalSpeedMapTest.suite());
        suite.addTest(SignalSystemFileCheckTest.suite());
        suite.addTest(SingleTurnoutSignalHeadTest.suite());
        suite.addTest(TwoIndexTcsProgrammerFacadeTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DccConsistTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NmraConsistTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(MatrixSignalMastTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DefaultRailComTest.class));

        // sub-packages
        suite.addTest(jmri.implementation.swing.PackageTest.suite());
        suite.addTest(ReporterTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(jmri.implementation.configurexml.PackageTest.class));

        return suite;
    }

}
