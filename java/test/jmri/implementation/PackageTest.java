package jmri.implementation;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        // fundamental aspects
        NamedBeanTest.class,

        // implementations
        AbstractAudioTest.class,
        AbstractSensorTest.class,
        AbstractStringIOTest.class,
        AccessoryOpsModeProgrammerFacadeTest.class,
        OpsModeDelayedProgrammerFacadeTest.class,
        AddressedHighCvProgrammerFacadeTest.class,
        DccSignalHeadTest.class,
        DccSignalMastTest.class,
        DefaultConditionalTest.class,
        DefaultConditionalActionTest.class,
        DefaultIdTagTest.class,
        DefaultLogixTest.class,
        ActiveLogixTest.class,
        DefaultSignalSystemTest.class,
        DefaultSignalAppearanceMapTest.class,
        MultiIndexProgrammerFacadeTest.class,
        VerifyWriteProgrammerFacadeTest.class,
        OffsetHighCvProgrammerFacadeTest.class,
        ResettingOffsetHighCvProgrammerFacadeTest.class,
        RouteTest.class,
        SE8cSignalHeadTest.class,
        SignalHeadSignalMastTest.class,
        SignalSpeedMapTest.class,
        SignalSystemFileCheckTest.class,
        SingleTurnoutSignalHeadTest.class,
        TwoIndexTcsProgrammerFacadeTest.class,
        BundleTest.class,
        DccConsistTest.class,
        NmraConsistTest.class,
        MatrixSignalMastTest.class,
        DefaultRailComTest.class,
        DefaultCabSignalTest.class,

        // sub-packages
        jmri.implementation.swing.PackageTest.class,
        ReporterTest.class,
        jmri.implementation.configurexml.PackageTest.class,
        LightControlTest.class,
        DccConsistManagerTest.class,
        DefaultClockControlTest.class,
        FileLocationsPreferencesTest.class,
        JmriConfigurationManagerTest.class,
        NmraConsistManagerTest.class,
        ProgrammerFacadeSelectorTest.class,
        AbstractRailComReporterTest.class,
        DefaultConditionalTest.class,
        DefaultSignalMastLogicTest.class,
        DoubleTurnoutSignalHeadTest.class,
        JmriClockPropertyListenerTest.class,
        JmriMemoryPropertyListenerTest.class,
        JmriMultiStatePropertyListenerTest.class,
        JmriSimplePropertyListenerTest.class,
        JmriTwoStatePropertyListenerTest.class,
        LsDecSignalHeadTest.class,
        MergSD2SignalHeadTest.class,
        NoFeedbackTurnoutOperatorTest.class,
        QuadOutputSignalHeadTest.class,
        RawTurnoutOperatorTest.class,
        SensorGroupConditionalTest.class,
        SensorTurnoutOperatorTest.class,
        SignalMastRepeaterTest.class,
        TripleOutputSignalHeadTest.class,
        TripleTurnoutSignalHeadTest.class,
        TurnoutSignalMastTest.class,
        VirtualSignalMastTest.class,
        AbstractInstanceInitializerTest.class,

})

/**
 * PackageTest.java
 *
 * Description:	tests for the jmri.implementation package
 *
 * @author	Bob Jacobsen 2009, 2017
 */
public class PackageTest {
}

