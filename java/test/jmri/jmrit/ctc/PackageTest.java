package jmri.jmrit.ctc;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * tests for the jmri.jmrit.ctc package
 *
 * @author Dave Sand Copyright (C) 2019
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    BundleTest.class,
    CTCConstantsTest.class,
    CTCExceptionTest.class,
//     CTCFilesTest.class,
//     CTCJythonAccessInstanceManagerTest.class,
    CTCMainTest.class,
//     CallOnTest.class,
//     CodeButtonHandlerTest.class,
//     CodeButtonSimulatorTest.class,
    CtcRunActionTest.class,
    CtcRunStartupTest.class,
//     FleetingTest.class,
//     IndicationLockingSignalsTest.class,
//     LockedRouteTest.class,
//     LockedRoutesManagerTest.class,
//     NBHAbstractSignalCommonTest.class,
    NBHSensorTest.class,
    NBHSignalHeadTest.class,
    NBHSignalMastTest.class,
    NBHTurnoutTest.class,
//     RequestedDirectionObservedTest.class,
//     SignalDirectionIndicatorsInterfaceTest.class,
    SignalDirectionIndicatorsNullTest.class,
//     SignalDirectionIndicatorsTest.class,
//     SignalDirectionLeverTest.class,
//     SwitchDirectionIndicatorsTest.class,
//     SwitchDirectionLeverTest.class,
//     SwitchIndicatorsRouteTest.class,
//     TrafficLockingInfoTest.class,
//     TrafficLockingTest.class,
//     TurnoutLockTest.class,
    jmri.jmrit.ctc.ctcserialdata.PackageTest.class,
    jmri.jmrit.ctc.editor.PackageTest.class
})
public class PackageTest{
}