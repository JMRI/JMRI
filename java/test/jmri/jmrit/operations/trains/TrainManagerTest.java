//TrainManagerTest.java
package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the TrainManager class Last manually cross-checked on 20090131
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class TrainManagerTest extends OperationsTestCase {

    // test train manager
    public void testTrainManager() {
        TrainManager manager = TrainManager.instance();

        // test defaults
        Assert.assertTrue("Build Messages", manager.isBuildMessagesEnabled());
        Assert.assertFalse("Build Reports", manager.isBuildReportEnabled());
        Assert.assertFalse("Print Preview", manager.isPrintPreviewEnabled());

        // Swap them
        manager.setBuildMessagesEnabled(false);
        manager.setBuildReportEnabled(true);
        manager.setPrintPreviewEnabled(true);

        Assert.assertFalse("Build Messages", manager.isBuildMessagesEnabled());
        Assert.assertTrue("Build Reports", manager.isBuildReportEnabled());
        Assert.assertTrue("Print Preview", manager.isPrintPreviewEnabled());

    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        Setup.setBuildAggressive(false);
        Setup.setTrainIntoStagingCheckEnabled(true);
        Setup.setMaxTrainLength(1000);
        Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
    }

    public TrainManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", TrainManagerTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(TrainManagerTest.class);
        return suite;
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

}
