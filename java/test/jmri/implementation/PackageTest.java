package jmri.implementation;

import junit.framework.JUnit4TestAdapter;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * PackageTest.java
 *
 * Description:	tests for the jmri.implementation package
 *
 * @author	Bob Jacobsen 2009, 2017
 */
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
        suite.addTest(new JUnit4TestAdapter(DccSignalHeadTest.class));
        suite.addTest(new JUnit4TestAdapter(DccSignalMastTest.class));
        suite.addTest(DefaultConditionalTest.suite());
        suite.addTest(DefaultConditionalActionTest.suite());
        suite.addTest(new JUnit4TestAdapter(DefaultIdTagTest.class));
        suite.addTest(DefaultLogixTest.suite());
        suite.addTest(new JUnit4TestAdapter(DefaultSignalSystemTest.class));
        suite.addTest(new JUnit4TestAdapter(DefaultSignalAppearanceMapTest.class));
        suite.addTest(MultiIndexProgrammerFacadeTest.suite());
        suite.addTest(VerifyWriteProgrammerFacadeTest.suite());
        suite.addTest(OffsetHighCvProgrammerFacadeTest.suite());
        suite.addTest(ResettingOffsetHighCvProgrammerFacadeTest.suite());
        suite.addTest(RouteTest.suite());
        suite.addTest(new JUnit4TestAdapter(SE8cSignalHeadTest.class));
        suite.addTest(new JUnit4TestAdapter(SignalHeadSignalMastTest.class));
        suite.addTest(SignalSpeedMapTest.suite());
        suite.addTest(new JUnit4TestAdapter(SignalSystemFileCheckTest.class));
        suite.addTest(new JUnit4TestAdapter(SingleTurnoutSignalHeadTest.class));
        suite.addTest(TwoIndexTcsProgrammerFacadeTest.suite());
        suite.addTest(new JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new JUnit4TestAdapter(DccConsistTest.class));
        suite.addTest(new JUnit4TestAdapter(NmraConsistTest.class));
        suite.addTest(new JUnit4TestAdapter(MatrixSignalMastTest.class));
        suite.addTest(new JUnit4TestAdapter(DefaultRailComTest.class));

        // sub-packages
        suite.addTest(jmri.implementation.swing.PackageTest.suite());
        suite.addTest(ReporterTest.suite());
        suite.addTest(new JUnit4TestAdapter(jmri.implementation.configurexml.PackageTest.class));
        suite.addTest(new JUnit4TestAdapter(LightControlTest.class));
        suite.addTest(new JUnit4TestAdapter(DccConsistManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(DefaultClockControlTest.class));
        suite.addTest(new JUnit4TestAdapter(FileLocationsPreferencesTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriConfigurationManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(NmraConsistManagerTest.class));
        suite.addTest(new JUnit4TestAdapter(ProgrammerFacadeSelectorTest.class));
        suite.addTest(new JUnit4TestAdapter(AbstractRailComReporterTest.class));
        suite.addTest(new JUnit4TestAdapter(DefaultConditionalTest.class));
        suite.addTest(new JUnit4TestAdapter(DefaultSignalMastLogicTest.class));
        suite.addTest(new JUnit4TestAdapter(DoubleTurnoutSignalHeadTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriClockPropertyListenerTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriMemoryPropertyListenerTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriMultiStatePropertyListenerTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriSimplePropertyListenerTest.class));
        suite.addTest(new JUnit4TestAdapter(JmriTwoStatePropertyListenerTest.class));
        suite.addTest(new JUnit4TestAdapter(LsDecSignalHeadTest.class));
        suite.addTest(new JUnit4TestAdapter(MergSD2SignalHeadTest.class));
        suite.addTest(new JUnit4TestAdapter(NoFeedbackTurnoutOperatorTest.class));
        suite.addTest(new JUnit4TestAdapter(QuadOutputSignalHeadTest.class));
        suite.addTest(new JUnit4TestAdapter(RawTurnoutOperatorTest.class));
        suite.addTest(new JUnit4TestAdapter(SensorGroupConditionalTest.class));
        suite.addTest(new JUnit4TestAdapter(SensorTurnoutOperatorTest.class));
        suite.addTest(new JUnit4TestAdapter(SignalMastRepeaterTest.class));
        suite.addTest(new JUnit4TestAdapter(TripleOutputSignalHeadTest.class));
        suite.addTest(new JUnit4TestAdapter(TripleTurnoutSignalHeadTest.class));
        suite.addTest(new JUnit4TestAdapter(TurnoutSignalMastTest.class));
        suite.addTest(new JUnit4TestAdapter(VirtualSignalMastTest.class));
        suite.addTest(new JUnit4TestAdapter(AbstractInstanceInitializerTest.class));


        return suite;
    }

}

