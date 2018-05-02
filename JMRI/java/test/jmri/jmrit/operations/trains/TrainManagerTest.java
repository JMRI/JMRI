//TrainManagerTest.java
package jmri.jmrit.operations.trains;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.setup.Setup;
import junit.framework.Test;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests for the TrainManager class Last manually cross-checked on 20090131
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class TrainManagerTest extends OperationsTestCase {

    // test train manager
    public void testTrainManager() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);

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

    /**
     * Make sure we can retrieve a train from the manager by name.
     */
    public void testGetTrainByName() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        Assert.assertNotNull("Retrieve Train", manager.getTrainByName("STF"));
    }

    /**
     * Make sure we can retrieve a train from the manager by name.
     */
    public void testGetTrainById() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        Assert.assertNotNull("Retrieve Train", manager.getTrainById("1"));
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
        jmri.util.JUnitOperationsUtil.initOperationsData();
    }

    public TrainManagerTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", TrainManagerTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
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
