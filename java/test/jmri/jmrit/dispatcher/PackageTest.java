package jmri.jmrit.dispatcher;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the jmrit.dispatcher package
 *
 * @author	Dave Duchamp
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
        TestSuite suite = new TestSuite("jmri.jmrit.dispatcher.PackageTest"); // no tests in class itself
        suite.addTest(jmri.jmrit.dispatcher.DispatcherTrainInfoTest.suite());
        suite.addTest(new junit.framework.JUnit4TestAdapter(DispatcherTrainInfoFileTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(BundleTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DispatcherFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(DispatcherActionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OptionsFileTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainInfoFileTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(TrainInfoTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ActivateTrainFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutoTrainsFrameTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutoAllocateTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutoTurnoutsTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(OptionsMenuTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(ActiveTrainTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AllocatedSectionTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AllocationRequestTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AllocationPlanTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutoActiveTrainTest.class));
        suite.addTest(new junit.framework.JUnit4TestAdapter(AutoTrainActionTest.class));
        return suite;
    }

}
