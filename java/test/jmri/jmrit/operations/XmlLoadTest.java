package jmri.jmrit.operations;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.util.JUnitUtil;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.junit.Assert;

/**
 * Tests to make sure the demo files load and the managers are properly
 * configured once they are loaded.
 * <p>
 * Still to do: Everything
 *
 * @author Paul Bender Copyright (C) 2015
 */
public class XmlLoadTest extends TestCase {

    // load a set of operations files.  These are the default
    // demo files.
    public void testDemoLoad() {
        runTest("java/test/jmri/jmrit/operations/xml/DemoFiles/", 12, 12, 10, 210, 19);
    }

    // load a set of operations files with trains that have been built.
    // these are the demo files, but they were stored after building trains.
    public void testDemoWithBuildLoad() {
        runTest("java/test/jmri/jmrit/operations/xml/DemoFilesWithBuiltTrains/", 12, 12, 10, 210, 19);
    }

    /*
      * Private function to actually run the test
      *
      * @param directory String directory location of the files to load
      * @param locs number of locations expected after load
      * @param routes number of routes expected after load
      * @param trains number of trains expected after load
      * @param cars number of cars expected after load
      * @param engines number of engines expected after load
     */
    private void runTest(String directory, int locs, int routes, int trains,
            int cars, int engines) {
        Assert.assertEquals("Before read Number of Locations", 0, InstanceManager.getDefault(LocationManager.class).getList().size());
        Assert.assertEquals("Before read Number of Routes", 0, InstanceManager.getDefault(RouteManager.class).getRoutesByNameList().size());
        Assert.assertEquals("Before read Number of Trains", 0, InstanceManager.getDefault(TrainManager.class).getTrainsByNameList().size());
        Assert.assertEquals("Before read Number of Cars", 0, InstanceManager.getDefault(CarManager.class).getList().size());
        Assert.assertEquals("Before read Number of Engines", 0, InstanceManager.getDefault(EngineManager.class).getList().size());

        InstanceManager.getDefault(OperationsSetupXml.class);
        OperationsSetupXml.setOperationsDirectoryName(directory.substring(0, directory.length() - 1));
        try {
            // use readFile, because load wraps readFile with a try catch.
            InstanceManager.getDefault(OperationsSetupXml.class).readFile(OperationsSetupXml.getOperationsDirectoryName() + "/"
                    + InstanceManager.getDefault(OperationsSetupXml.class).getOperationsFileName());
            InstanceManager.getDefault(LocationManagerXml.class).readFile(OperationsSetupXml.getOperationsDirectoryName() + "/"
                    + InstanceManager.getDefault(LocationManagerXml.class).getOperationsFileName());
            InstanceManager.getDefault(RouteManagerXml.class).readFile(OperationsSetupXml.getOperationsDirectoryName() + "/"
                    + InstanceManager.getDefault(RouteManagerXml.class).getOperationsFileName());
            InstanceManager.getDefault(TrainManagerXml.class).readFile(OperationsSetupXml.getOperationsDirectoryName() + "/"
                    + InstanceManager.getDefault(TrainManagerXml.class).getOperationsFileName());
            InstanceManager.getDefault(CarManagerXml.class).readFile(OperationsSetupXml.getOperationsDirectoryName() + "/"
                    + InstanceManager.getDefault(CarManagerXml.class).getOperationsFileName());
            InstanceManager.getDefault(EngineManagerXml.class).readFile(OperationsSetupXml.getOperationsDirectoryName() + "/"
                    + InstanceManager.getDefault(EngineManagerXml.class).getOperationsFileName());
        } catch (Exception e) {
            Assert.fail("Exception reading operations files");
            return;
        }
        // spot check to make sure the correct number of items were created.
        Assert.assertEquals("Number of Locations", locs, InstanceManager.getDefault(LocationManager.class).getList().size());
        Assert.assertEquals("Number of Routes", routes, InstanceManager.getDefault(RouteManager.class).getRoutesByNameList().size());
        Assert.assertEquals("Number of Trains", trains, InstanceManager.getDefault(TrainManager.class).getTrainsByNameList().size());
        Assert.assertEquals("Number of Cars", cars, InstanceManager.getDefault(CarManager.class).getList().size());
        Assert.assertEquals("Number of Engines", engines, InstanceManager.getDefault(EngineManager.class).getList().size());

    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        JUnitUtil.setUp();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initIdTagManager();

        // clear the operations directory name.
        OperationsSetupXml.setOperationsDirectoryName("operations");
        OperationsSetupXml.setFileLocation("temp" + java.io.File.separator);

        //dispose of any existing managers
        InstanceManager.getDefault(EngineManager.class).dispose();
        InstanceManager.getDefault(CarManager.class).dispose();
        InstanceManager.getDefault(TrainManager.class).dispose();
        InstanceManager.getDefault(RouteManager.class).dispose();
        InstanceManager.getDefault(LocationManager.class).dispose();
        InstanceManager.getDefault(EngineManagerXml.class).dispose();
        InstanceManager.getDefault(CarManagerXml.class).dispose();
        InstanceManager.getDefault(TrainManagerXml.class).dispose();
        InstanceManager.getDefault(RouteManagerXml.class).dispose();
        InstanceManager.getDefault(LocationManagerXml.class).dispose();

    }

    public XmlLoadTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XmlLoadTest.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XmlLoadTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        JUnitUtil.tearDown();

        super.tearDown();
    }

}
