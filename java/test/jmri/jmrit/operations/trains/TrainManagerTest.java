//TrainManagerTest.java
package jmri.jmrit.operations.trains;

import java.io.File;
import java.util.Locale;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the TrainManager class Last manually cross-checked on 20090131
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 * @version $Revision$
 */
public class TrainManagerTest extends TestCase {

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
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();

        // set the locale to US English
        Locale.setDefault(Locale.ENGLISH);

        // Repoint OperationsSetupXml to JUnitTest subdirectory
        OperationsSetupXml.setOperationsDirectoryName("operations" + File.separator + "JUnitTest");
        // Change file names to ...Test.xml
        OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml");
        RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
        EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
        CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
        LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
        TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        // Need to clear out TrainManager global variables
        TrainManager.instance().dispose();
        CarManager.instance().dispose();
        LocationManager.instance().dispose();
        RouteManager.instance().dispose();
        CarRoads.instance().dispose();

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

    // The minimal setup for log4J
    @Override
    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }

}
