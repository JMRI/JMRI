package jmri.jmrit.operations;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

/**
 * Common setup and tear down for operation tests.
 *
 * @author Dan Boudreau Copyright (C) 2015
 * @author Paul Bender Copyright (C) 2016
 *
 */
public class OperationsSwingTestCase {

    public void loadTrain(Location l) {
        Assert.assertNotNull("Test Loc", l);
        TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
        Train trainA = trainManager.newTrain("Test Train A");
        // train needs to service location "l" or error message when saving track edit frame
        RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
        Route route = routeManager.newRoute("Route Train A");
        route.addLocation(l);
        trainA.setRoute(route);      
    }
    
    public void loadTrains() {
        // Add some cars for the various tests in this suite
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        String roadNames[] = Bundle.getMessage("carRoadNames").split(",");
        // add caboose to the roster
        Car c = cm.newCar(roadNames[2], "687");
        c.setCaboose(true);
        c = cm.newCar("CP", "435");
        c.setCaboose(true);

        // load engines
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Engine e1 = emanager.newEngine("SP", "1");
        e1.setModel("GP40");
        Engine e2 = emanager.newEngine("PU", "2");
        e2.setModel("GP40");
        Engine e3 = emanager.newEngine("UP", "3");
        e3.setModel("GP40");
        Engine e4 = emanager.newEngine("UP", "4");
        e4.setModel("FT");

        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        // turn off build fail messages
        tmanager.setBuildMessagesEnabled(true);
        // turn off print preview
        tmanager.setPrintPreviewEnabled(false);

        // load 5 trains
        for (int i = 0; i < 5; i++) {
            tmanager.newTrain("Test_Train " + i);
        }

        // load 6 locations
        for (int i = 0; i < 6; i++) {
            InstanceManager.getDefault(LocationManager.class).newLocation("Test_Location " + i);
        }

        // load 5 routes
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route A");
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route B");
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route C");
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route D");
        InstanceManager.getDefault(RouteManager.class).newRoute("Test Route E");
    }
    
    public void loadLocations() {
        // create 5 locations
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
        Location l1 = lManager.newLocation("Test Loc E");
        l1.addTrack("Test Track", Track.SPUR);
        l1.setLength(1001);
        Location l2 = lManager.newLocation("Test Loc D");
        l2.setLength(1002);
        Location l3 = lManager.newLocation("Test Loc C");
        l3.setLength(1003);
        Location l4 = lManager.newLocation("Test Loc B");
        l4.setLength(1004);
        Location l5 = lManager.newLocation("Test Loc A");
        l5.setLength(1005);
    }

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();


        // Set things up outside of operations
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initDebugThrottleManager();
        JUnitUtil.initIdTagManager();
        JUnitUtil.initShutDownManager();
        JUnitUtil.resetProfileManager();

        JUnitOperationsUtil.resetOperationsManager();
    }

    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }
}
