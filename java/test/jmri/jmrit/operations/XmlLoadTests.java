package jmri.jmrit.operations;

import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.trains.TrainManager;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import jmri.util.JUnitUtil;


/**
 * Tests to make sure the demo files load and the managers are properly configured once they are loaded.
 *
 * Still to do: Everything
 *
 * @author Paul Bender Copyright (C) 2015
 */
public class XmlLoadTests extends TestCase {

     
     // load a set of operations files.  These are the default
     // demo files.
     public void testDemoLoad(){
          runTest("java/test/jmri/jmrit/operations/xml/DemoFiles/", 12, 12, 10, 210, 19);
     }


     // load a set of operations files with trains that have been built.
     // these are the demo files, but they were stored after building trains.
     public void testDemoWithBuildLoad(){
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
     private void runTest(String directory,int locs,int routes,int trains,
                     int cars, int engines) {
         Assert.assertEquals("Before read Number of Locations",0,LocationManager.instance().getList().size());
         Assert.assertEquals("Before read Number of Routes",0,RouteManager.instance().getRoutesByNameList().size());
         Assert.assertEquals("Before read Number of Trains",0,TrainManager.instance().getTrainsByNameList().size());
         Assert.assertEquals("Before read Number of Cars",0,CarManager.instance().getList().size());
         Assert.assertEquals("Before read Number of Engines",0,EngineManager.instance().getList().size());

         OperationsSetupXml.instance();
          OperationsSetupXml.setOperationsDirectoryName(directory.substring(0,directory.length()-1));
         try {
            // use readFile, because load wraps readFile with a try catch.
            OperationsSetupXml.instance().readFile(OperationsSetupXml.getOperationsDirectoryName() + "/" + 
                  OperationsSetupXml.instance().getOperationsFileName());
            LocationManagerXml.instance().readFile(OperationsSetupXml.getOperationsDirectoryName() +  "/" +
                  LocationManagerXml.instance().getOperationsFileName());
            RouteManagerXml.instance().readFile(OperationsSetupXml.getOperationsDirectoryName() +  "/" +
                  RouteManagerXml.instance().getOperationsFileName());
            TrainManagerXml.instance().readFile(OperationsSetupXml.getOperationsDirectoryName() +  "/" +
                  TrainManagerXml.instance().getOperationsFileName());
            CarManagerXml.instance().readFile(OperationsSetupXml.getOperationsDirectoryName() +  "/" +
                  CarManagerXml.instance().getOperationsFileName());
            EngineManagerXml.instance().readFile(OperationsSetupXml.getOperationsDirectoryName() +  "/" +
                  EngineManagerXml.instance().getOperationsFileName());
         } catch(Exception e){
           Assert.fail("Exception reading operations files");
           return;
         } 
         // spot check to make sure the correct number of items were created.
         Assert.assertEquals("Number of Locations",locs,LocationManager.instance().getList().size());
         Assert.assertEquals("Number of Routes",routes,RouteManager.instance().getRoutesByNameList().size());
         Assert.assertEquals("Number of Trains",trains,TrainManager.instance().getTrainsByNameList().size());
         Assert.assertEquals("Number of Cars",cars,CarManager.instance().getList().size());
         Assert.assertEquals("Number of Engines",engines,EngineManager.instance().getList().size());

     }
   
    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        apps.tests.Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initIdTagManager();
        jmri.InstanceManager.setShutDownManager( new
                 jmri.managers.DefaultShutDownManager() {
                    @Override
                    public void register(jmri.ShutDownTask s){
                       // do nothing with registered shutdown tasks for testing.
                    }
                 });
    
     // clear the operations directory name.
         OperationsSetupXml.setOperationsDirectoryName("operations");
         OperationsSetupXml.setFileLocation("temp"+ java.io.File.separator);

         //dispose of any existing managers
         EngineManager.instance().dispose();
         CarManager.instance().dispose();
         TrainManager.instance().dispose();
         RouteManager.instance().dispose();
         LocationManager.instance().dispose();
         OperationsSetupXml.instance().dispose();
         EngineManagerXml.instance().dispose();
         CarManagerXml.instance().dispose();
         TrainManagerXml.instance().dispose();
         RouteManagerXml.instance().dispose();
         LocationManagerXml.instance().dispose();


    }

    public XmlLoadTests(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading", XmlLoadTests.class.getName()};
        junit.textui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(XmlLoadTests.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
       JUnitUtil.resetInstanceManager();
       apps.tests.Log4JFixture.tearDown();
         
       //dispose of any existing managers
       EngineManager.instance().dispose();
       CarManager.instance().dispose();
       TrainManager.instance().dispose();
       RouteManager.instance().dispose();
       LocationManager.instance().dispose();
       OperationsSetupXml.instance().dispose();
       EngineManagerXml.instance().dispose();
       CarManagerXml.instance().dispose();
       TrainManagerXml.instance().dispose();
       RouteManagerXml.instance().dispose();
       LocationManagerXml.instance().dispose();

       // clear the operations directory name.
       OperationsSetupXml.setOperationsDirectoryName("operations");
         OperationsSetupXml.setFileLocation("temp"+ java.io.File.separator);

       super.tearDown();
    }

}
