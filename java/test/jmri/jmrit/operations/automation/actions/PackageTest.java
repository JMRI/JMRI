package jmri.jmrit.operations.automation.actions;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BundleTest.class,
        ActionCodesTest.class,
        ActivateTrainScheduleActionTest.class,
        ApplyTrainScheduleActionTest.class,
        BuildTrainActionTest.class,
        BuildTrainIfSelectedActionTest.class,
        DeselectTrainActionTest.class,
        GotoActionTest.class,
        GotoFailureActionTest.class,
        GotoSuccessActionTest.class,
        HaltActionTest.class,
        IsTrainEnRouteActionTest.class,
        MessageYesNoActionTest.class,
        MoveTrainActionTest.class,
        NoActionTest.class,
        PrintSwitchListActionTest.class,
        PrintSwitchListChangesActionTest.class,
        PrintTrainManifestActionTest.class,
        PrintTrainManifestIfSelectedActionTest.class,
        ResetTrainActionTest.class,
        ResumeAutomationActionTest.class,
        RunAutomationActionTest.class,
        RunSwitchListActionTest.class,
        RunSwitchListChangesActionTest.class,
        RunTrainActionTest.class,
        SelectTrainActionTest.class,
        StopAutomationActionTest.class,
        TerminateTrainActionTest.class,
        UpdateSwitchListActionTest.class,
        WaitSwitchListActionTest.class,
        WaitTrainActionTest.class,
        WaitTrainTerminatedActionTest.class,
})

/**
 * Invokes complete set of tests in the jmri.jmrit.operations.automations.actions tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest  {
}
