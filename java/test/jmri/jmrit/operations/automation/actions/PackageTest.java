package jmri.jmrit.operations.automation.actions;

import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Invokes complete set of tests in the jmri.jmrit.operations.automations.actions tree
 *
 * @author	Bob Jacobsen Copyright 2001, 2003, 2012
 */
public class PackageTest extends TestCase {

    // from here down is testing infrastructure
    public PackageTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", PackageTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite("jmri.jmrit.operations.automations.actions.PackageTest");   // no tests in this class itself

        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ActionCodesTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ActivateTimetableActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ApplyTimetableActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BuildTrainActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BuildTrainIfSelectedActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DeselectTrainActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(GotoActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(GotoFailureActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(GotoSuccessActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(HaltActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(IsTrainEnRouteActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(MessageYesNoActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(MoveTrainActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(NoActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintSwitchListActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintSwitchListChangesActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintTrainManifestActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(PrintTrainManifestIfSelectedActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ResetTrainActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ResumeAutomationActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RunAutomationActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RunSwitchListActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RunSwitchListChangesActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(RunTrainActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(SelectTrainActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(StopAutomationActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TerminateTrainActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(UpdateSwitchListActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(WaitSwitchListActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(WaitTrainActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(WaitTrainTerminatedActionTest.class));
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() {
        JUnitUtil.setUp();
    }

    @Override
    protected void tearDown() {
        JUnitUtil.tearDown();
    }
}
