package jmri.jmrit.operations.trains;

import jmri.InstanceManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleItem;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLengths;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.cars.Kernel;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the TrainBuilder class NOTE: Many of the tests here are nearly
 * identical to similarly named tests in the TrainTest class. The TrainTest
 * tests call the train's build function, which then calls the TrainBuilder's
 * build method. All tests in this file that build trains call the TrainBuilder
 * class' build method directly.
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class TrainBuilderTest {

    private final int DIRECTION_ALL = Location.EAST + Location.WEST + Location.NORTH + Location.SOUTH;

    private TrainManager tmanager;
    private RouteManager rmanager;
    private LocationManager lmanager;
    private EngineManager emanager;
    private CarManager cmanager;
    private ScheduleManager smanager;
    private CarColors cc;
    private CarLengths cl;
    private CarOwners co;
    private CarRoads cr;
    private CarLoads cld;
    private CarTypes ct;
    private EngineTypes et;
    private EngineModels em;
        
    private Location A;
    private Location B;
    private Location C;
    private RouteLocation rA;
    private RouteLocation rB;
    private RouteLocation rC;

    @Test
    public void testCtor() {
        TrainBuilder tb = new TrainBuilder();
        Assert.assertNotNull("Train Builder Constructor", tb);
    }

    @Test
    public void testNoRouteBuild() {
        Train train = tmanager.newTrain("TestNoRouteBuild");
        // build train without a route, should fail
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, no route", train.isBuilt());
        Assert.assertEquals("Train build failed", Train.CODE_BUILD_FAILED, train.getStatusCode());
    }

    @Test
    public void testNoRouteLocationsBuild() {
        Train train = tmanager.newTrain("TestNoRouteLocationsBuild");

        // now add a route that doesn't have any locations
        Route route = rmanager.newRoute("TestRoute");
        train.setRoute(route);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, no route locations", train.isBuilt());
        Assert.assertEquals("Train build failed", Train.CODE_BUILD_FAILED, train.getStatusCode());
    }

    @Test
    public void testRouteLocationsBuild() {
        Train train = tmanager.newTrain("TestRouteLocationsBuild");

        // now add a route that doesn't have any locations
        Route route = rmanager.newRoute("TestRoute");
        train.setRoute(route);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, no locations", train.isBuilt());
        Assert.assertEquals("Train build failed", Train.CODE_BUILD_FAILED, train.getStatusCode());

        // now add a location to the route
        Location depart = lmanager.newLocation("depart");
        RouteLocation rl = route.addLocation(depart);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build", train.isBuilt());
        Assert.assertEquals("Train built", Train.CODE_PARTIAL_BUILT, train.getStatusCode());

        // delete location
        lmanager.deregister(depart);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, departure location deleted", train.isBuilt());
        Assert.assertEquals("Train build failed", Train.CODE_BUILD_FAILED, train.getStatusCode());

        // recreate location
        depart = lmanager.newLocation("depart");
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, location recreated, but not part of route", train
                .isBuilt());

        route.deleteLocation(rl);
        route.addLocation(depart);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, route repaired", train.isBuilt());

        Location terminate = lmanager.newLocation("terminate");
        rl = route.addLocation(terminate);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, route has two locations", train.isBuilt());

        // delete terminal location
        lmanager.deregister(terminate);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, terminal location deleted", train.isBuilt());

        route.deleteLocation(rl);
        terminate = lmanager.newLocation("terminate");
        rl = route.addLocation(terminate);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, route has been repaired", train.isBuilt());

        Location middle = lmanager.newLocation("middle");
        // staging tracks in the middle of the route are ignored
        middle.addTrack("staging in the middle", Track.STAGING);
        rl = route.addLocation(middle, 2); // put location in middle of route

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, three location route", train.isBuilt());

        // delete location in the middle
        lmanager.deregister(middle);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, middle location deleted", train.isBuilt());

        // remove the middle location from the route
        route.deleteLocation(rl);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, two location route", train.isBuilt());
    }

    @Test
    public void testRouteRandomFeature() {
        Train train = tmanager.newTrain("TestRouteRandomFeature");

        // Create a three location route
        Route route = rmanager.newRoute("TestRouteRandom");

        Location depart = lmanager.newLocation("depart");
        RouteLocation rld = route.addLocation(depart);
        rld.setMaxCarMoves(10);

        Location middle = lmanager.newLocation("middle");
        RouteLocation rlm = route.addLocation(middle);
        rlm.setMaxCarMoves(10);

        Location terminate = lmanager.newLocation("terminate");
        RouteLocation rlt = route.addLocation(terminate);
        rlt.setMaxCarMoves(10);

        train.setRoute(route);
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build", train.isBuilt());

        // check the number of moves requested
        Assert.assertEquals("Requested moves", 0, rld.getCarMoves());
        Assert.assertEquals("Requested moves", 0, rlm.getCarMoves());
        Assert.assertEquals("Requested moves", 0, rlt.getCarMoves());

        // test bad random number
        rld.setRandomControl("Not A Number");

        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build", train.isBuilt());

        // if no requested moves, the random value is ignored
        rld.setMaxCarMoves(0);
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build", train.isBuilt());

        // try random value of 100
        rld.setRandomControl("100");
        rld.setMaxCarMoves(10);
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build", train.isBuilt());

        // check the number of moves requested
        Assert.assertTrue("Requested moves", rld.getCarMoves() >= 0);
        Assert.assertTrue("Requested moves", rld.getCarMoves() <= 10);
        Assert.assertEquals("Requested moves", 0, rlm.getCarMoves());
        Assert.assertEquals("Requested moves", 0, rlt.getCarMoves());

    }

    /*
     * The requires cars option isn't available to users
     */
    @Test
    public void testBuildRequiresCars() {
        Train train = tmanager.newTrain("TestBuildRequiresCars");

        // now add a route that doesn't have any locations
        Route route = rmanager.newRoute("TestRoute");
        train.setRoute(route);

        // now add a locations to the route
        Location depart = lmanager.newLocation("depart");
        route.addLocation(depart);

        Location terminate = lmanager.newLocation("terminate");
        route.addLocation(terminate);

        Location middle = lmanager.newLocation("middle");
        route.addLocation(middle, 2); // put location in middle of route

        // Build option require cars
        Control.fullTrainOnly = true;
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, requires cars", train.isBuilt());

        // restore control
        Control.fullTrainOnly = false;
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, build doesn't require cars", train.isBuilt());
    }

    @Test
    public void testAutoEnginesBuildFailNoEngines() {
        // This test uses the maximum length of a train in route
        Setup.setMaxTrainLength(1000);

        Train train = tmanager.newTrain("TestAutoEnginesBuildFailNoEngines");
        train.setNumberEngines(Train.AUTO);

        Route route = rmanager.newRoute("AutoEngineTest");
        train.setRoute(route);
        setUpRoute(route);

        // delete all the engines
        emanager.deregister(emanager.getByRoadAndNumber("E", "1"));
        emanager.deregister(emanager.getByRoadAndNumber("E", "2"));
        emanager.deregister(emanager.getByRoadAndNumber("E", "3"));
        emanager.deregister(emanager.getByRoadAndNumber("E", "4"));

        // Auto Engines calculates the number of engines based on requested moves in the route
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, no engines", train.isBuilt());
    }

    @Test
    public void testAutoEnginesSingleEngine() {
        // This test uses the maximum length of a train in route
        Setup.setMaxTrainLength(1000);

        Train train = tmanager.newTrain("TestAutoEnginesSingleEngine");
        train.setNumberEngines(Train.AUTO);

        Route route = rmanager.newRoute("AutoEngineTest");
        train.setRoute(route);
        setUpRoute(route);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, only needs a single engine", train.isBuilt());
    }

    @Test
    public void testAutoEnginesTwoEngines() {
        // This test uses the maximum length of a train in route
        Setup.setMaxTrainLength(1000);

        Train train = tmanager.newTrain("TestAutoEnginesTwoEngines");
        train.setNumberEngines(Train.AUTO);

        Route route = rmanager.newRoute("AutoEngineTest");
        train.setRoute(route);
        setUpRoute(route);

        Engine e1 = emanager.getByRoadAndNumber("E", "1");
        Engine e2 = emanager.getByRoadAndNumber("E", "2");

        // change requirements
        rA.setMaxCarMoves(12);
        rB.setMaxCarMoves(12);
        rC.setMaxCarMoves(12);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, only single engines", train.isBuilt());

        Consist consist = emanager.newConsist("c");
        e1.setConsist(consist);
        e2.setConsist(consist);

        // train should require two engines
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build", train.isBuilt());

        Assert.assertEquals("e1 should be assigned to train", train, e1.getTrain());
        Assert.assertEquals("e2 should be assigned to train", train, e2.getTrain());
    }
    
    @Test
    public void testAutoEnginesGrade() {
        // This test uses the maximum length of a train in route
        Setup.setMaxTrainLength(1000);
        Setup.setMaxNumberEngines(6);

        Train train = tmanager.newTrain("TestAutoEnginesGrade");
        train.setNumberEngines(Train.AUTO);

        Route route = rmanager.newRoute("AutoEngineTest");
        train.setRoute(route);
        setUpRoute(route);

        rA.setMaxCarMoves(12);
        rB.setMaxCarMoves(12);
        rC.setMaxCarMoves(12);

        rB.setGrade(2.5); // 2.5% grade!

        Engine e1 = emanager.getByRoadAndNumber("E", "1");
        Engine e2 = emanager.getByRoadAndNumber("E", "2");
        Engine e3 = emanager.getByRoadAndNumber("E", "3");
        Engine e4 = emanager.getByRoadAndNumber("E", "4");

        Consist consist = emanager.newConsist("c");
        e1.setConsist(consist);
        e2.setConsist(consist);

        // train should require four engines
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, needs four engines, only two", train.isBuilt());

        e3.setConsist(consist);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, needs four engines, only three", train.isBuilt());

        e4.setConsist(consist);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, four engines available", train.isBuilt());

    }

    @Test
    public void testBuildConsistFromSingleLocos() {
        // this has nothing to do with this test other than test coverage
        Setup.setComment("Test consists from Single Locos");

        Train train = tmanager.newTrain("TestBuildConsistFromSingleLocos");

        // Create a three location route
        Route route = rmanager.newRoute("TestRouteRandom");

        Location depart = lmanager.newLocation("depart");
        route.addLocation(depart);
        Track departureTrack = depart.addTrack("departure track", Track.YARD);
        departureTrack.setLength(300);

        Location middle = lmanager.newLocation("middle");
        route.addLocation(middle);
        Track middleTrack = middle.addTrack("Track in the middle", Track.YARD);
        middleTrack.setLength(300);

        Location terminate = lmanager.newLocation("terminate");
        route.addLocation(terminate);
        Track terminateTrack = terminate.addTrack("terminal track", Track.YARD);
        terminateTrack.setLength(300);

        train.setRoute(route);
        train.setNumberEngines("2");

        Engine e1 = emanager.newEngine("E", "1");
        e1.setModel("GP40");
        e1.setLocation(depart, departureTrack);

        Engine e2 = emanager.newEngine("E", "2");
        e2.setModel("GP40");
        e2.setLocation(depart, departureTrack);

        Engine e3 = emanager.newEngine("E", "3");
        e3.setModel("GP40");
        e3.setLocation(depart, departureTrack);

        Engine e4 = emanager.newEngine("E", "4");
        e4.setModel("GP40");
        e4.setLocation(depart, departureTrack);

        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build", train.isBuilt());

        // allow consist to be built out of single locos
        train.setBuildConsistEnabled(true);

        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build", train.isBuilt());

        train.setNumberEngines("5");
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, only 4 locos", train.isBuilt());

        train.setNumberEngines("4");
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build", train.isBuilt());

    }

    @Test
    public void testMaxEngines() {
        // This test uses the maximum length of a train in route
        Setup.setMaxTrainLength(1000);

        Train train = tmanager.newTrain("TestMaxEngines");
        train.setNumberEngines(Train.AUTO);

        Route route = rmanager.newRoute("AutoEngineTest");
        train.setRoute(route);
        setUpRoute(route);

        rA.setMaxCarMoves(12);
        rB.setMaxCarMoves(12);
        rC.setMaxCarMoves(12);
        rB.setGrade(2.5); // 2.5% grade!

        Engine e1 = emanager.getByRoadAndNumber("E", "1");
        Engine e2 = emanager.getByRoadAndNumber("E", "2");
        Engine e3 = emanager.getByRoadAndNumber("E", "3");
        Engine e4 = emanager.getByRoadAndNumber("E", "4");

        Consist consist = emanager.newConsist("c");
        e1.setConsist(consist);
        e2.setConsist(consist);
        e3.setConsist(consist);
        e4.setConsist(consist);

        Setup.setMaxNumberEngines(3); // limit the maximum to three engines
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, needs four engines, three is the maximum allowed", train
                .isBuilt());

        // remove one engine from consist, train should build
        consist.delete(e4);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, three engines available", train.isBuilt());
    }

    // testSidingsYards tests the build procedure
    // through the train's build method.
    // test siding and yard moves
    // tests manual setting of destinations and trains for engines, cars, and cabooses.
    // tests consists and kernels
    @Test
    public void testSidingsYards() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // register the car and engine types used
        ct.addName(carTypes[1]);
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName(carTypes[5]);

        // Set up two cabooses and six box cars
        Car c1 = cmanager.newCar("CP", "10");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setMoves(10);
        c1.setCaboose(true);

        Car c2 = cmanager.newCar("CP", "200");
        c2.setTypeName(Bundle.getMessage("Caboose"));
        c2.setLength("32");
        c2.setMoves(11);
        c2.setCaboose(true);

        Car c3 = cmanager.newCar("CP", "30");
        c3.setTypeName(carTypes[1]);
        c3.setLength("40");
        c2.setMoves(12);

        Car c4 = cmanager.newCar("CP", "4000");
        c4.setTypeName(carTypes[1]);
        c4.setLength("40");
        c4.setMoves(13);

        Car c5 = cmanager.newCar("CP", "5");
        c5.setTypeName(carTypes[1]);
        c5.setLength("40");
        c5.setMoves(14);

        Car c6 = cmanager.newCar("CP", "60");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(15);

        Car c7 = cmanager.newCar("CP", "700");
        c7.setTypeName(carTypes[5]);
        c7.setLength("50");
        c7.setMoves(16);

        Car c8 = cmanager.newCar("CP", "8000");
        c8.setTypeName(carTypes[1]);
        c8.setLength("60");
        c8.setMoves(17);

        Car c9 = cmanager.newCar("CP", "9");
        c9.setTypeName(carTypes[5]);
        c9.setLength("40");
        c9.setMoves(18);

        Car c10 = cmanager.newCar("CP", "1000");
        c10.setTypeName(carTypes[5]);
        c10.setLength("40");
        c10.setMoves(19);

        // place two engines in a consist
        Consist con1 = emanager.newConsist("CP");

        Engine e1 = emanager.newEngine("CP", "10");
        e1.setModel("GP30");
        e1.setConsist(con1);
        Engine e2 = emanager.newEngine("CP", "20");
        e2.setModel("GP30");
        e2.setConsist(con1);

        // Set up a route of 3 locations: Foxboro (2 tracks, yard and siding),
        // Acton (2 tracks, sidings), and Nashua (2 tracks, yards).
        Location l1 = lmanager.newLocation("Foxboro");
        Assert.assertEquals("Location 1 Name", "Foxboro", l1.getName());
        Assert.assertEquals("Location 1 Initial Length", 0, l1.getLength());

        Track l1s1 = l1.addTrack("Foxboro Siding", Track.SPUR);
        l1s1.setLength(600);
        Assert.assertEquals("Location 1s1 Name", "Foxboro Siding", l1s1.getName());
        Assert.assertEquals("Location 1s1 LocType", "Siding", l1s1.getTrackType());
        Assert.assertEquals("Location 1s1 Length", 600, l1s1.getLength());
        Assert.assertEquals("Default directions", DIRECTION_ALL, l1s1.getTrainDirections());

        Track l1s2 = l1.addTrack("Foxboro Yard", Track.YARD);
        l1s2.setLength(400);
        Assert.assertEquals("Location 1s2 Name", "Foxboro Yard", l1s2.getName());
        Assert.assertEquals("Location 1s2 LocType", "Yard", l1s2.getTrackType());
        Assert.assertEquals("Location 1s2 Length", 400, l1s2.getLength());

        Assert.assertEquals("Location 1 Length", 1000, l1.getLength());

        Location l2 = lmanager.newLocation("Acton");
        Assert.assertEquals("Location 2 Name", "Acton", l2.getName());

        Track l2s1 = l2.addTrack("Acton Siding 1", Track.SPUR);
        l2s1.setLength(543);
        l2s1.setMoves(1);
        Assert.assertEquals("Location 2s1 Name", "Acton Siding 1", l2s1.getName());
        Assert.assertEquals("Location 2s1 LocType", Track.SPUR, l2s1.getTrackType());
        Assert.assertEquals("Location 2s1 Length", 543, l2s1.getLength());

        Track l2s2 = l2.addTrack("Acton Siding 2", Track.SPUR);
        l2s2.setLength(345);

        Assert.assertEquals("Acton Length", 888, l2.getLength());

        Location l3 = lmanager.newLocation("Nashua");
        Track l3s1 = l3.addTrack("Nashua Yard 1", Track.YARD);
        l3s1.setLength(301);

        Track l3s2 = l3.addTrack("Nashua Yard 2", Track.YARD);
        l3s2.setLength(402);

        Assert.assertEquals("Location 3 Length", 703, l3.getLength());

        // define the route
        Setup.setCarMoves(6); // set default to 6 moves per location
        Route r1 = rmanager.newRoute("Foxboro-Acton-Nashua-Acton-Foxboro");
        RouteLocation rl1 = r1.addLocation(l1);
        rl1.setTrainIconX(25); // set the train icon coordinates
        rl1.setTrainIconY(225);
        RouteLocation rl2 = r1.addLocation(l2);
        rl2.setTrainIconX(75); // set the train icon coordinates
        rl2.setTrainIconY(225);
        RouteLocation rl3 = r1.addLocation(l3);
        rl3.setTrainIconX(125); // set the train icon coordinates
        rl3.setTrainIconY(225);
        RouteLocation rl4 = r1.addLocation(l2);
        rl4.setTrainIconX(175); // set the train icon coordinates
        rl4.setTrainIconY(225);
        RouteLocation rl5 = r1.addLocation(l1);
        rl5.setTrainIconX(225); // set the train icon coordinates
        rl5.setTrainIconY(225);
        rl5.setPickUpAllowed(false); // don't include cars at destination

        // define the train
        Train t1 = tmanager.newTrain("TestSidingsYards");
        t1.setRoute(r1);

        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("train built 1", t1.isBuilt());
        Assert.assertEquals("should be 0 cars", 0, t1.getNumberCarsWorked());

        // place the cars on the tracks
        // Place cars
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(l1, l1s1));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(l1, l1s1));
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(l1, l1s1));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(l1, l1s1));
        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(l1, l1s1));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(l1, l1s1));
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(l1, l1s1));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(l1, l1s1));
        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(l1, l1s1));
        Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(l1, l1s1));
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));

        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("train built 2", t1.isBuilt());
        Assert.assertEquals("should be 6 cars", 6, t1.getNumberCarsWorked());

        // check car destinations
        Assert.assertEquals("Destination c1", "", c1.getDestinationName());
        Assert.assertEquals("Destination c2", "", c2.getDestinationName());
        Assert.assertEquals("Destination c3", "Acton", c3.getDestinationName());
        Assert.assertEquals("Destination c4", "Nashua", c4.getDestinationName());
        Assert.assertEquals("Destination c5", "Acton", c5.getDestinationName());
        Assert.assertEquals("Destination c6", "Nashua", c6.getDestinationName());
        Assert.assertEquals("Destination c7", "Acton", c7.getDestinationName());
        Assert.assertEquals("Destination c8", "Nashua", c8.getDestinationName());
        Assert.assertEquals("Destination c9", "", c9.getDestinationName());
        Assert.assertEquals("Destination c10", "", c10.getDestinationName());
        Assert.assertEquals("Destination e1", "", e1.getDestinationName());

        // release cars from train
        Assert.assertTrue("reset train", t1.reset());
        Assert.assertEquals("Train reset", Train.CODE_TRAIN_RESET, t1.getStatusCode());
        // set c3, c5, c6, c8, c10 destination to be Nashua
        c3.setDestination(l3, l3s1);
        c5.setDestination(l3, l3s1);
        c6.setDestination(l3, l3s1);
        c8.setDestination(l3, l3s1);
        c10.setDestination(l3, l3s1);
        // set c5 and c9 to be serviced by train TT
        Train t2 = tmanager.newTrain("TT");
        c5.setTrain(t2);
        c9.setTrain(t2);
        // set c6 to be serviced by train FF
        c6.setTrain(t1);

        // require a caboose
        t1.setRequirements(Train.CABOOSE);
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("train built 3", t1.isBuilt());

        // check car destinations
        Assert.assertEquals("2 Destination  c1", "Foxboro", c1.getDestinationName());
        Assert.assertEquals("2 Destination  c2", "", c2.getDestinationName());
        Assert.assertEquals("2 Destination  c3", "Nashua", c3.getDestinationName());
        Assert.assertEquals("2 Destination  c4", "Acton", c4.getDestinationName());
        Assert.assertEquals("2 Destination  c5", "Nashua", c5.getDestinationName());
        Assert.assertEquals("2 Destination  c6", "Nashua", c6.getDestinationName());
        Assert.assertEquals("2 Destination  c7", "Acton", c7.getDestinationName());
        Assert.assertEquals("2 Destination  c8", "Nashua", c8.getDestinationName());
        Assert.assertEquals("2 Destination  c9", "", c9.getDestinationName());
        Assert.assertEquals("2 Destination  c10", "Nashua", c10.getDestinationName());

        // move and terminate the train
        t1.move(); // to Acton
        t1.move(); // to Nashua
        t1.move(); // to Acton
        t1.move(); // to Foxboro
        t1.move(); // terminate

        // check car destinations
        Assert.assertEquals("3 Destination c1", "", c1.getDestinationName());
        Assert.assertEquals("3 Destination c2", "", c2.getDestinationName());
        Assert.assertEquals("3 Destination c3", "", c3.getDestinationName());
        Assert.assertEquals("3 Destination c4", "", c4.getDestinationName());
        Assert.assertEquals("3 Destination c5", "Nashua", c5.getDestinationName());
        Assert.assertEquals("3 Destination c6", "", c6.getDestinationName());
        Assert.assertEquals("3 Destination c7", "", c7.getDestinationName());
        Assert.assertEquals("3 Destination c8", "", c8.getDestinationName());
        Assert.assertEquals("3 Destination c9", "", c9.getDestinationName());
        Assert.assertEquals("3 Destination c10", "Nashua", c10.getDestinationName());

        // check car locations
        Assert.assertEquals("Location c1", "Foxboro", c1.getLocationName());
        Assert.assertEquals("Location c2", "Foxboro", c2.getLocationName());
        Assert.assertEquals("Location c3", "Nashua", c3.getLocationName());
        Assert.assertEquals("Location c4", "Acton", c4.getLocationName());
        Assert.assertEquals("Location c5", "Foxboro", c5.getLocationName());
        Assert.assertEquals("Location c6", "Nashua", c6.getLocationName());
        Assert.assertEquals("Location c7", "Acton", c7.getLocationName());
        Assert.assertEquals("Location c8", "Nashua", c8.getLocationName());
        Assert.assertEquals("Location c9", "Foxboro", c9.getLocationName());
        Assert.assertEquals("Location c10", "Foxboro", c10.getLocationName());

        // now set caboose destinations that aren't the terminal
        Assert.assertEquals("set destination c1", Track.OKAY, c1.setDestination(l2, l2s1));
        Assert.assertEquals("set destination c2", Track.OKAY, c2.setDestination(l3, l3s1));

        // train requires a caboose, should fail
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("train built 4", t1.isBuilt());

        // Set caboose destination to be the terminal
        Assert.assertEquals("set caboose destination", Track.OKAY, c2.setDestination(l1, l1s2));
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("train built 5", t1.isBuilt());
        Assert.assertTrue("train reset 5", t1.reset());

        // set the cabooses to train FF
        c1.setTrain(t2);
        c2.setTrain(t2);

        // build should fail
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("train built 6", t1.isBuilt());

        // set caboose to train TT
        c1.setTrain(t1);
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("train built 7", t1.isBuilt());

        // check car destinations
        Assert.assertEquals("4 Destination c1", "Foxboro", c1.getDestinationName());
        Assert.assertEquals("4 Destination c2", "", c2.getDestinationName());
        Assert.assertEquals("4 Destination c3", "Acton", c3.getDestinationName());
        Assert.assertEquals("4 Destination c4", "Nashua", c4.getDestinationName());
        Assert.assertEquals("4 Destination c5", "Nashua", c5.getDestinationName());
        Assert.assertEquals("4 Destination c6", "Foxboro", c6.getDestinationName());
        Assert.assertEquals("4 Destination c7", "Foxboro", c7.getDestinationName());
        Assert.assertEquals("4 Destination c8", "Foxboro", c8.getDestinationName());
        Assert.assertEquals("4 Destination c9", "", c9.getDestinationName());
        Assert.assertEquals("4 Destination c10", "Acton", c10.getDestinationName());

        // add an engine
        t1.setNumberEngines("2");
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("train built 8", t1.isBuilt());
        Assert.assertEquals("5 Destination e1", "Foxboro", e1.getDestinationName());
        Assert.assertEquals("5 Destination e2", "Foxboro", e2.getDestinationName());

        // assign lead engine to train TT
        e1.setTrain(t2);
        // should fail
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("train built 9", t1.isBuilt());

        // assign one of the consist engine to train TT
        e1.setTrain(t1);
        e2.setTrain(t2); // shouldn't pay attention to the other engine
        // should build
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("train built 10", t1.isBuilt());
        t1.reset();

        // both engines should release
        Assert.assertEquals("6 Destination e1", "", e1.getDestinationName());
        Assert.assertEquals("6 Train e1", "", e1.getTrainName());
        Assert.assertEquals("6 Destination e2", "", e2.getDestinationName());
        Assert.assertEquals("6 Train e2", "", e2.getTrainName());

        // now try setting engine destination that isn't the terminal
        Assert.assertEquals("set destination e1", Track.OKAY, e1.setDestination(l2, l2s1));
        // should fail
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("train built 11", t1.isBuilt());

        e1.setDestination(l1, l1s2);
        e2.setDestination(l2, l2s1); // program should ignore
        // should build
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("train built 12", t1.isBuilt());

        // set lead engine's track to null
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, null));
        // should not build
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("train will not build engine track is null", t1.isBuilt());
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));

        // should now build
        t1.reset();
        new TrainBuilder().build(t1);

        // move and terminate the train
        t1.move(); // to Acton
        t1.move(); // to Nashua
        t1.move(); // to Acton
        t1.move(); // to Foxboro
        t1.move(); // terminate

        // check engine final locations
        Assert.assertEquals("Location e1", "Foxboro", e1.getLocationName());
        Assert.assertEquals("Location e2", "Foxboro", e2.getLocationName());

        // move c7 & c8 to Foxboro to help test kernels
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(l1, l1s1));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(l1, l1s1));
        // now test kernels
        Kernel k1 = cmanager.newKernel("group of cars");
        c8.setKernel(k1); // lead car
        c7.setKernel(k1);
        c7.setTrain(t2); // program should ignore
        c3.setLocation(l1, l1s1);
        c3.setKernel(k1);
        c3.setDestination(l1, l1s1); // program should ignore (produces debug warning)

        // should build
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("train built 12", t1.isBuilt());
        // Confirm partial build
        Assert.assertEquals("Train built", Train.CODE_PARTIAL_BUILT, t1.getStatusCode());
        Assert.assertEquals("12 Location c3", "Foxboro", c3.getLocationName());
        Assert.assertEquals("12 Location c7", "Foxboro", c7.getLocationName());
        Assert.assertEquals("12 Location c8", "Foxboro", c8.getLocationName());
        Assert.assertEquals("12 Destination c3", "Nashua", c3.getDestinationName());
        Assert.assertEquals("12 Destination c7", "Nashua", c7.getDestinationName());
        Assert.assertEquals("12 Destination c8", "Nashua", c8.getDestinationName());

        // move and terminate the train
        t1.move(); // to Acton
        Assert.assertEquals("Train en route", Train.CODE_TRAIN_EN_ROUTE, t1.getStatusCode());
        t1.move(); // to Nashua
        Assert.assertEquals("Train en route", Train.CODE_TRAIN_EN_ROUTE, t1.getStatusCode());
        t1.move(); // to Acton
        Assert.assertEquals("Train en route", Train.CODE_TRAIN_EN_ROUTE, t1.getStatusCode());
        t1.move(); // to Foxboro
        Assert.assertEquals("Train en route", Train.CODE_TRAIN_EN_ROUTE, t1.getStatusCode());
        t1.move(); // terminate
        Assert.assertEquals("Train Terminated", Train.CODE_TERMINATED, t1.getStatusCode());

        Assert.assertEquals("13 Location c3", "Nashua", c3.getLocationName());
        Assert.assertEquals("13 Location c7", "Nashua", c7.getLocationName());
        Assert.assertEquals("13 Location c8", "Nashua", c8.getLocationName());
        Assert.assertEquals("13 Destination c3", "", c3.getDestinationName());
        Assert.assertEquals("13 Destination c7", "", c7.getDestinationName());
        Assert.assertEquals("13 Destination c8", "", c8.getDestinationName());
        Assert.assertEquals("13 Train c3", "", c3.getTrainName());
        Assert.assertEquals("13 Train c7", "", c7.getTrainName());
        Assert.assertEquals("13 Train c8", "", c8.getTrainName());

        // now test car with FRED
        c1.setCaboose(false);
        c2.setCaboose(false);
        c1.setFred(true);
        c2.setFred(true);
        c1.setTypeName(carTypes[1]); // change the type, now Boxcar with FRED
        c2.setTypeName(carTypes[1]);
        c2.setTrain(null);

        // train requires a caboose, there are none, should fail
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("train built 14", t1.isBuilt());

        // change requirement to car with FRED
        t1.setRequirements(Train.FRED);

        // train requires a car with FRED, should pass
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("train built 15", t1.isBuilt());
        Assert.assertTrue("train reset 15", t1.reset()); // release cars

        // now set FRED destinations that aren't the terminal
        Assert.assertEquals("set destination c1", Track.OKAY, c1.setDestination(l2, l2s1));
        Assert.assertEquals("set destination c2", Track.OKAY, c2.setDestination(l3, l3s1));

        // train requires a car with FRED, should fail
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("train built 16", t1.isBuilt());

        // Set FRED destination to be the terminal
        Assert.assertEquals("set destination c2", Track.OKAY, c2.setDestination(l1, l1s2));
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("train built 17", t1.isBuilt());
        Assert.assertTrue("train reset 17", t1.reset());

        // set the cars with FRED to train FF
        c1.setTrain(t2);
        c2.setTrain(t2);

        // build should fail
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("train built 18", t1.isBuilt());

        // set car with FRED to train TT
        c2.setTrain(t1);
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("train built 19", t1.isBuilt());

        // test car out of service
        c2.setOutOfService(true);
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("required car is out of service", t1.isBuilt());

        // test location unknown
        c2.setOutOfService(false);
        c2.setLocationUnknown(true);
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("required car location is unknown", t1.isBuilt());

        c2.setLocationUnknown(false);
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("need car is available", t1.isBuilt());

        c2.setWait(1);
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("required car will wait for next train", t1.isBuilt());

        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("next train!", t1.isBuilt());
        Assert.assertEquals("CP 4000 destination", "Nashua", c4.getDestinationName());

        // test train and location direction controls
        c8.setLocation(l2, l2s1); // place led car of kernel in Action Siding 1
        l2.setTrainDirections(Location.EAST + Location.SOUTH + Location.WEST); // train is north bound
        t1.reset();
        new TrainBuilder().build(t1);

        // build should fail, cars c3 and c7 which is part of c8 kernel are on the wrong track
        Assert.assertFalse("Train direction test", t1.isBuilt());
        c3.setLocation(l2, l2s1); // place c3 Action Siding 1
        c7.setLocation(l2, l2s1); // place c7 Action Siding 1

        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("Train direction test", t1.isBuilt());
        Assert.assertEquals("CP 1000 destination is now Nashua", "Nashua", c10.getDestinationName());
        Assert.assertEquals("CP 30 at Acton, not serviced", null, c3.getTrain());
        Assert.assertEquals("CP 700 at Acton, not serviced", null, c7.getTrain());
        Assert.assertEquals("CP 8000 at Acton, not serviced", null, c8.getTrain());

        // restore Acton
        l2.setTrainDirections(Location.NORTH); // train is north bound
        t1.reset();
        new TrainBuilder().build(t1);

        Assert.assertEquals("CP 1000 destination is now", "Acton", c10.getDestinationName());
        Assert.assertEquals("CP 30 at Acton", t1, c3.getTrain());
        Assert.assertEquals("CP 700 at Acton", t1, c7.getTrain());
        Assert.assertEquals("CP 8000 at Acton", t1, c8.getTrain());

        // restrict train direction at the track level
        l2s2.setTrainDirections(Track.EAST + Track.SOUTH + Track.WEST);
        // take one car out of kernel
        c3.setKernel(null);
        c3.setLocation(l2, l2s1); // place car in Action Siding 1
        c8.setLocation(l2, l2s2); // place lead car in Action Yard
        c7.setLocation(l2, l2s2); // place c7 in Action Yard
        t1.reset();
        new TrainBuilder().build(t1);

        Assert.assertEquals("CP 1000 destination track", "Acton Siding 1", c1.getDestinationTrackName());
        Assert.assertEquals("CP 200 at Foxboro Siding", t1, c2.getTrain());
        Assert.assertEquals("CP 30 at Acton Siding 1", t1, c3.getTrain());
        Assert.assertEquals("CP 30 destination track", "Nashua Yard 2", c3.getDestinationTrackName());
        Assert.assertEquals("CP 4000 at Foxboro Siding", t1, c4.getTrain());
        Assert.assertEquals("CP 4000 destination", "Foxboro", c4.getDestinationName());
        Assert.assertEquals("CP 4000 destination track", "Foxboro Yard", c4.getDestinationTrackName());
        Assert.assertEquals("CP 60 destination track", "", c6.getDestinationTrackName());
        Assert.assertEquals("CP 700 at Acton, not serviced, part of kernel CP 8000", null, c7.getTrain());
        Assert.assertEquals("CP 8000 at Acton, Acton Siding 2 not serviced", null, c8.getTrain());

        // test train length restrictions
        rl1.setMaxTrainLength(155); // only enough for the two engines, train needs a car with FRED
        t1.reset();
        new TrainBuilder().build(t1);

        Assert.assertFalse("Train length test, can't service car with FRED", t1.isBuilt());
        // build failed after engines were assigned to train 1
        Assert.assertEquals("Engine assignment ignores train length restrictions", t1, e1.getTrain());
        Assert.assertEquals("Engine assignment ignores train length restrictions", t1, e2.getTrain());
        Assert.assertEquals("Engine destination ignores train length restrictions", "Foxboro", e1
                .getDestinationName());
        Assert.assertEquals("Engine destination ignores train length restrictions", "Foxboro", e2
                .getDestinationName());

        Assert.assertEquals("Check CP30 engine length", "56", e1.getLength());
        Assert.assertEquals("Check CP 200 length", "32", c2.getLength());
        rl1.setMaxTrainLength(156); // enough for the two engines and a car with FRED 56 + 56 + 32 + 12(couplers) = 156

        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("Train length test, just enough length for engines and car with FRED", t1.isBuilt());
        Assert.assertEquals("CP 200 at Foxboro Siding", t1, c2.getTrain());
        Assert.assertEquals("CP 200 destination track", "Foxboro Siding", c2.getDestinationTrackName());
        Assert.assertEquals("CP 30 at Acton Siding 1", t1, c3.getTrain());
        Assert.assertEquals("CP 30 destination track", "Nashua Yard 1", c3.getDestinationTrackName());
        Assert.assertEquals("CP 4000 at Foxboro Siding", t1, c4.getTrain());
        Assert.assertEquals("CP 60 destination track", "", c6.getDestinationTrackName());
        Assert.assertEquals("CP 700 at Acton, not serviced, part of kernel CP 8000", null, c7.getTrain());
        Assert.assertEquals("CP 8000 at Acton, Acton Siding 2 not serviced", null, c8.getTrain());
        Assert.assertEquals("CP 1000 not part of train", null, c10.getTrain());

        // Increase the train length from the departure location
        rl1.setMaxTrainLength(1000);
        rl2.setMaxTrainLength(156); // restrict train length from Acton
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("Train length test, just enough length for engines and car with FRED", t1.isBuilt());
        Assert.assertEquals("CP 200 at Foxboro Siding", t1, c2.getTrain());
        Assert.assertEquals("CP 200 destination track", "Foxboro Yard", c2.getDestinationTrackName());
        Assert.assertEquals("CP 30 at Acton Siding 1", t1, c3.getTrain());
        Assert.assertEquals("CP 30 destination track", "Foxboro Yard", c3.getDestinationTrackName());
        Assert.assertEquals("CP 4000 at Foxboro Yard", t1, c4.getTrain());
        Assert.assertEquals("CP 4000 destination track", "Foxboro Siding", c4.getDestinationTrackName());
        Assert.assertEquals("CP 60 destination track", "", c6.getDestinationTrackName());
        Assert.assertEquals("CP 1000 part of train", t1, c10.getTrain());
        Assert.assertEquals("CP 1000 destination track", "Acton Siding 1", c10.getDestinationTrackName());

        // test setting car's destination to Foxboro Siding
        c2.setDestination(l1, l1s1);
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("car with FRED has destination", t1.isBuilt());
        t1.reset();

        // again, but now change car type serviced by Foxboro Yard
        c2.setDestination(l1, l1s1);
        l1s1.deleteTypeName(carTypes[1]);
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("car with FRED has destination that won't accept it", t1.isBuilt());
        l1s1.addTypeName(carTypes[1]);

        c6.setDestination(l2, l2s2); // destination Action Siding 2
        l2s2.deleteTypeName(carTypes[1]); // don't allow Boxcar to drop
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertTrue("car with FRED has destination that will now accept it", t1.isBuilt());
        Assert.assertEquals("CP 60 can't be delivered", null, c6.getTrain());

        c2.setLocation(l1, null);
        t1.reset();
        new TrainBuilder().build(t1);
        Assert.assertFalse("need car doesn't have a track assignment", t1.isBuilt());

        // end testSidingsYards
    }

    // testStagingtoStaging tests the build procedure
    // through the train's build method.
    // test train staging to staging
    @Test
    public void testStagingtoStaging() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        // Try building without engines
        train1.reset();
        new TrainBuilder().build(train1);
        train2.reset();
        new TrainBuilder().build(train2);
        // Only train 1 should build
        Assert.assertTrue("Train 1 After 1st Build without engines", train1.isBuilt());
        Assert.assertFalse("Train 2 After 1st Build exclude Boxcar", train2.isBuilt());
    }

    /*
     * Test car road names
     */
    @Test
    public void testStagingtoStagingA() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP","C10099");

        Car c3 = cmanager.getByRoadAndNumber("CP","X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP","X10002");

        Car c8 = cmanager.getByRoadAndNumber("CP","888");

        // Try building without engines
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 should build", train1.isBuilt());

        // now allow train 2 to service Boxcars
        train2.addTypeName("Boxcar");
        // Try again, but exclude road name CP
        train2.setRoadOption(Train.EXCLUDE_ROADS);
        train2.addRoadName("CP");
        Assert.assertEquals("Number of road names for train", 1, train2.getRoadNames().length);

        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertEquals("Train 2 After Build but exclude road CP", false, train2.isBuilt());
        train2.setRoadOption(Train.ALL_ROADS);

        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertTrue("Train 2 After Build include Boxcar", train2.isBuilt());

        // check train 1
        Assert.assertEquals("Car c1 After Build without engines should be assigned to Train 1", train1, c1
                .getTrain());
        Assert.assertEquals("Car c3 After Build without engines should be assigned to Train 1", train1, c3
                .getTrain());
        Assert.assertEquals("Car c4 After Build without engines should be assigned to Train 1", train1, c4
                .getTrain());
        Assert.assertEquals("Car c8 After Build without engines should be assigned to Train 1", train1, c8
                .getTrain());

        // car destinations correct?
        Assert.assertEquals("Car c1 After Build without engines destination", "South End", c1
                .getDestinationName());
        Assert.assertEquals("Car c3 After Build without engines destination", "North Industries", c3
                .getDestinationName());
        Assert.assertEquals("Car c4 After Build without engines destination", "South End", c4
                .getDestinationName());
        Assert.assertEquals("Car c8 After Build without engines destination", "South End", c8
                .getDestinationName());

        // car destination track correct?
        Assert.assertEquals("Car c1 After without engines Build track", "South End 1", c1
                .getDestinationTrackName());
        Assert.assertEquals("Car c3 After without engines Build track", "NI Yard", c3
                .getDestinationTrackName());
        Assert.assertEquals("Car c4 After without engines Build track", "South End 1", c4
                .getDestinationTrackName());
        Assert.assertEquals("Car c8 After without engines Build track", "South End 1", c8
                .getDestinationTrackName());
    }

    /*
     * Test required number of engines departing staging
     */
    @Test
    public void testStagingtoStagingB() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP","C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP","C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP","X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP","X10002");
        Car c5 = cmanager.getByRoadAndNumber("CP","X20001");
        Car c6 = cmanager.getByRoadAndNumber("CP","X20002");
        Car c7 = cmanager.getByRoadAndNumber("CP","777");
        Car c8 = cmanager.getByRoadAndNumber("CP","888");
        Car c9 = cmanager.getByRoadAndNumber("CP","99");
        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");
        Location l2 = lmanager.getLocationById("2");
        Location l3 = lmanager.getLocationById("3");
        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");
        Track l2s1 = l2.getTrackById("2s1");
        Track l3s1 = l3.getTrackById("3s1");
        Track l3s2 = l3.getTrackById("3s2");
        Route route1 = rmanager.getRouteById("1");
        RouteLocation rl1 = route1.getLocationById("1r1");
        RouteLocation rl2 = route1.getLocationById("1r2");
        RouteLocation rl3 = route1.getLocationById("1r3");
        train2.addTypeName("Boxcar");
        train2.setRoadOption(Train.ALL_ROADS);

        // Try building without engines on staging tracks but require them
        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");

        new TrainBuilder().build(train1);
        new TrainBuilder().build(train2);
        Assert.assertFalse("Train 1 After 2nd Build without engines", train1.isBuilt());
        Assert.assertFalse("Train 2 After 2nd Build without engines", train2.isBuilt());

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));
        
        train1.reset();
        train2.reset();

        // Build the trains with engines
        new TrainBuilder().build(train1);
        new TrainBuilder().build(train2);
        // Both should build
        Assert.assertTrue("Train 1 After Build with engines", train1.isBuilt());
        Assert.assertTrue("Train 2 After Build with engines", train2.isBuilt());

        // Check train 1
        Assert.assertEquals("Train 1 After Build Departs Name", "North End", train1.getTrainDepartsName());
        Assert.assertEquals("Train 1 After Build Terminates Name", "South End", train1
                .getTrainTerminatesName());
        Assert.assertEquals("Train 1 After Build Next Location Name", "North Industries", train1
                .getNextLocationName());
        Assert.assertEquals("Train 1 After Build Built Status", true, train1.isBuilt());

        // Are the proper engines and cars assigned to train 1?
        Assert.assertEquals("Engine e1 After Build should be assigned to Train 1", train1, e1.getTrain());
        Assert.assertEquals("Engine e2 After Build should be assigned to Train 1", train1, e2.getTrain());
        Assert.assertEquals("Car c1 After Build should be assigned to Train 1", train1, c1.getTrain());
        Assert.assertEquals("Car c2 After Build should be assigned to Train 1", train1, c2.getTrain());
        Assert.assertEquals("Car c3 After Build should be assigned to Train 1", train1, c3.getTrain());
        Assert.assertEquals("Car c4 After Build should be assigned to Train 1", train1, c4.getTrain());

        // Are the proper engines and cars assigned to train 2?
        Assert.assertEquals("Engine e3 After Build should be assigned to Train 2", train2, e3.getTrain());
        Assert.assertEquals("Engine e4 After Build should be assigned to Train 2", train2, e4.getTrain());
        Assert.assertEquals("Car c5 After Build should be assigned to Train 2", train2, c5.getTrain());
        Assert.assertEquals("Car c7 After Build should be not be assigned", null, c7.getTrain());
        Assert.assertEquals("Car c9 After Build should be not be assigned", null, c9.getTrain());
        Assert.assertEquals("Car c8 After Build should be assigned to Train 2", train2, c8.getTrain());

        // Are the engine and car destinations correct?
        Assert.assertEquals("Engine e1 After Build destination", "South End", e1.getDestinationName());
        Assert.assertEquals("Engine e2 After Build destination", "South End", e2.getDestinationName());
        Assert.assertEquals("Car c1 After Build destination", "South End", c1.getDestinationName());
        Assert.assertEquals("Car c3 After Build destination", "North Industries", c3.getDestinationName());
        Assert.assertEquals("Car c4 After Build destination", "South End", c4.getDestinationName());
        Assert.assertEquals("Car c8 After Build destination", "South End", c8.getDestinationName());

        // Are the engine and car destination track correct?
        Assert.assertEquals("Engine e1 After Build track", "South End 1", e1.getDestinationTrackName());
        Assert.assertEquals("Engine e2 After Build track", "South End 1", e2.getDestinationTrackName());
        Assert.assertEquals("Car c1 After Build track", "South End 1", c1.getDestinationTrackName());
        Assert.assertEquals("Car c3 After Build track", "NI Yard", c3.getDestinationTrackName());
        Assert.assertEquals("Car c4 After Build track", "South End 1", c4.getDestinationTrackName());
        Assert.assertEquals("Car c8 After Build track", "South End 2", c8.getDestinationTrackName());

        // Are the location pickup and drop counts correct?
        Assert.assertEquals("Drop count for North End", 0, l1.getDropRS());
        // Each train has one drop at North Industries
        Assert.assertEquals("Drop count for North Industries", 2, l2.getDropRS());
        // Train1 has 5 drops and Train2 has 4 drops for South End
        Assert.assertEquals("Drop count for South End", 9, l3.getDropRS());
        // Each train has 5 pickups
        Assert.assertEquals("Pickup count for North End", 10, l1.getPickupRS());
        Assert.assertEquals("Pickup count for North Industries", 1, l2.getPickupRS());
        Assert.assertEquals("Pickup count for South End", 0, l3.getPickupRS());

        // Are the track pickup and drop counts correct?
        Assert.assertEquals("Drop count for North End, track North End 1", 0, l1s1.getDropRS());
        Assert.assertEquals("Drop count for North End, track North End 2", 0, l1s2.getDropRS());
        Assert.assertEquals("Pickup count for North End, track North End 1", 6, l1s1.getPickupRS());
        Assert.assertEquals("Pickup count for North End, track North End 2", 4, l1s2.getPickupRS());
        // Each train has one drop at NI Yard
        Assert.assertEquals("Drop count for North Industries, track NI Yard", 2, l2s1.getDropRS());
        Assert.assertEquals("Pickup count for North Industries, track NI Yard", 1, l2s1.getPickupRS());
        Assert.assertEquals("Drop count for South End, track South End 1", 5, l3s1.getDropRS());
        Assert.assertEquals("Drop count for South End, track South End 2", 4, l3s2.getDropRS());
        Assert.assertEquals("Pickup count for South End, track South End 1", 0, l3s1.getPickupRS());
        Assert.assertEquals("Pickup count for South End, track South End 2", 0, l3s2.getPickupRS());

        // Are the other engines and cars assigned correctly?
        Assert.assertEquals("Engine e3 After Build should NOT be assigned to Train 1", train2, e3.getTrain());
        Assert.assertEquals("Engine e4 After Build should NOT be assigned to Train 1", train2, e4.getTrain());
        Assert.assertEquals("Car c5 After Build should NOT be assigned to Train 1", train2, c5.getTrain());
        Assert.assertEquals("Car c6 After Build should NOT be assigned to Train 1", train2, c6.getTrain());
        Assert.assertEquals("Car c2 After Build should NOT be assigned to Train 2", train1, c2.getTrain());
        Assert.assertEquals("Car c7 After Build should NOT be assigned to Train 1", null, c7.getTrain());
        Assert.assertEquals("Car c9 After Build should NOT be assigned to Train 1", null, c9.getTrain());

        // Check expected arrival times
        Assert.assertEquals("Train 1 expected departure time", "06:05", train1.getExpectedArrivalTime(rl1));
        // Check time for car moves and train travel times
        Assert.assertEquals("Per Car move time", 11, Setup.getSwitchTime());
        Assert.assertEquals("Train travel time", 111, Setup.getTravelTime());

        Assert.assertEquals("Train 1 expected North End", "07:56", train1.getExpectedArrivalTime(rl2));
        // one car dropped and one is picked up at North End, so travel time + two car moves
        Assert.assertEquals("Train 1 expected North Industries", "09:58", train1.getExpectedArrivalTime(rl3));

        // Reset the train!
        Assert.assertEquals("Train 1 Reset should be true", true, train1.reset());
        Assert.assertEquals("Train 1 After Reset Departs Name", "North End", train1.getTrainDepartsName());
        Assert.assertEquals("Train 1 After Reset Terminates Name", "South End", train1
                .getTrainTerminatesName());
        Assert.assertEquals("Train 1 After Reset Next Location Name", "", train1.getNextLocationName());
        Assert.assertEquals("Train 1 After Reset Built Status", false, train1.isBuilt());

        // Are the engines and cars released from train 1?
        Assert.assertEquals("Engine e1 After Reset should NOT be assigned to Train 1", null, e1.getTrain());
        Assert.assertEquals("Engine e2 After Reset should NOT be assigned to Train 1", null, e2.getTrain());
        Assert.assertEquals("Car c1 After Reset should NOT be assigned to Train 1", null, c1.getTrain());
        Assert.assertEquals("Car c3 After Reset should NOT be assigned to Train 1", null, c3.getTrain());
        Assert.assertEquals("Car c4 After Reset should NOT be assigned to Train 1", null, c4.getTrain());
        Assert.assertNotEquals("Car c8 After Reset should NOT be assigned to Train 1", train1, c8.getTrain());

        // Are the location pickup and drop counts correct?
        Assert.assertEquals("Reset Drop count for North End", 0, l1.getDropRS());
        Assert.assertEquals("Reset Drop count for North Industries", 1, l2.getDropRS());
        Assert.assertEquals("Reset Drop count for South End", 4, l3.getDropRS());
        Assert.assertEquals("Reset Pickup count for North End", 4, l1.getPickupRS());
        Assert.assertEquals("Reset Pickup count for North Industries", 1, l2.getPickupRS());
        Assert.assertEquals("Reset Pickup count for South End", 0, l3.getPickupRS());

        // Are the track pickup and drop counts correct?
        Assert.assertEquals("Reset Drop count for North End, track North End 1", 0, l1s1.getDropRS());
        Assert.assertEquals("Reset Drop count for North End, track North End 2", 0, l1s2.getDropRS());
        Assert.assertEquals("Reset Pickup count for North End, track North End 1", 0, l1s1.getPickupRS());
        Assert.assertEquals("Reset Pickup count for North End, track North End 2", 4, l1s2.getPickupRS());
        Assert.assertEquals("Reset Drop count for North Industries, track NI Yard", 1, l2s1.getDropRS());
        Assert.assertEquals("Reset Pickup count for North Industries, track NI Yard", 1, l2s1.getDropRS());
        Assert.assertEquals("Reset Drop count for South End, track South End 1", 0, l3s1.getDropRS());
        Assert.assertEquals("Reset Drop count for South End, track South End 2", 4, l3s2.getDropRS());
        Assert.assertEquals("Reset Pickup count for South End, track South End 1", 0, l3s1.getPickupRS());
        Assert.assertEquals("Reset Pickup count for South End, track South End 2", 0, l3s2.getPickupRS());
    }

    /*
     * Test car type name departing staging
     */
    @Test
    public void testStagingtoStagingC() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // Try again, but exclude caboose
        // there are cabooses waiting in staging so build should fail
        train1.deleteTypeName(Bundle.getMessage("Caboose"));
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build with engines but exclude Caboose", false, train1.isBuilt());
        
        train1.addTypeName(Bundle.getMessage("Caboose"));
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build with engines include Caboose", true, train1.isBuilt());
    }

    /*
     * Test car road names departing staging
     */
    @Test
    public void testStagingtoStagingD() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));


        // Try again, but exclude road name CP
        train1.setRoadOption(Train.EXCLUDE_ROADS);
        train1.addRoadName("CP");
        Assert.assertEquals("Number of road names for train", 1, train1.getRoadNames().length);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("Train 1 After Build with engines but exclude road CP", train1.isBuilt());
       
        train1.setRoadOption(Train.ALL_ROADS);
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 allow all roads", train1.isBuilt());
    }

    /*
     * Test car with destination set departing staging
     */
    @Test
    public void testStagingtoStagingE() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP","C10099");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");
        Location l2 = lmanager.getLocationById("2");
        Location l3 = lmanager.getLocationById("3");
        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // try again, but set the caboose destination to NI yard
        c1.setDestination(l2, null);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("Train 1 build should fail, caboose destination isn't terminal", train1.isBuilt());

        // send caboose to last location which is staging
        c1.setDestination(l3, null);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 build, caboose destination is terminal", train1.isBuilt());
    }

    /*
     * Test location car type acceptance
     */
    @Test
    public void testStagingtoStagingF() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Location l3 = lmanager.getLocationById("3");
        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // don't allow type "Caboose" to be serviced
        l3.deleteTypeName(Bundle.getMessage("Caboose"));
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("Train 1 build, Caboose not serviced by location", train1.isBuilt());
        
        // now allow location to service type name "Caboose"
        l3.addTypeName(Bundle.getMessage("Caboose"));
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 build, Caboose is allowed", train1.isBuilt());
    }

    /*
     * Test car built dates
     */
    @Test
    public void testStagingtoStagingG() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Car c4 = cmanager.getByRoadAndNumber("CP","X10002");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // Try again, but only allow rolling stock built before 1985
        train1.setBuiltEndYear("1985");
        new TrainBuilder().build(train1);
        // should fail, required engines have built dates after 1985
        Assert.assertEquals("Train 1 After Build with rs built before 1985", false, train1.isBuilt());
        // change the engine built date
        e1.setBuilt("7-84");
        e2.setBuilt("1984");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 After 2nd Build with rs built before 1985", train1.isBuilt());
        // change one of the car's built date to after 1985
        c4.setBuilt("1-85");
        train1.reset();
        new TrainBuilder().build(train1);
        // should fail
        Assert.assertFalse("Train 1 After 3rd Build with rs built before 1985", train1.isBuilt());
        train1.setBuiltEndYear("");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 After 4th Build with rs built before 1985", train1.isBuilt());
    }

    /*
     * Test engine type names
     */
    @Test
    public void testStagingtoStagingH() {
        String engineTypes[] = Bundle.getMessage("engineDefaultTypes").split(",");
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // Try again, but now exclude engine type Diesel
        train1.deleteTypeName(engineTypes[2]);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After 1st Build type Diesel not serviced", false, train1.isBuilt());
        train1.addTypeName(engineTypes[2]);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After 2nd Build type Diesel serviced", true, train1.isBuilt());
    }

    /*
     * Test car owner
     */
    @Test
    public void testStagingtoStagingI() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // Try again, but now restrict owner
        train1.addOwnerName("DAB");
        train1.setOwnerOption(Train.INCLUDE_OWNERS);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After 1st Build owner DAB", false, train1.isBuilt());
        train1.addOwnerName("AT");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After 2nd Build owners DAB and AT", true, train1.isBuilt());
        train1.setOwnerOption(Train.EXCLUDE_OWNERS);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After 3rd Build exclude owners DAB and AT", false, train1.isBuilt());
        train1.deleteOwnerName("AT");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After 4th Build exclude owner DAB", false, train1.isBuilt());
        train1.setOwnerOption(Train.ALL_OWNERS);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After 5th Build all owners", true, train1.isBuilt());
    }

    /*
     * Test car load restrictions departing staging
     */
    @Test
    public void testStagingtoStagingJ() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP","C10099");

        Car c3 = cmanager.getByRoadAndNumber("CP","X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP","X10002");

        Car c8 = cmanager.getByRoadAndNumber("CP","888");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // Try again, but now restrict load
        train1.addLoadName("L");
        train1.setLoadOption(Train.INCLUDE_LOADS);
        train1.reset();
        new TrainBuilder().build(train1);
        // build should fail, cars in staging have E loads
        Assert.assertEquals("Train 1 After include load L", false, train1.isBuilt());

        train1.deleteLoadName("L");
        c8.setLoadName("L"); // this car shouldn't be picked up.
        train1.addLoadName("E");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 After include load E", train1.isBuilt());

        Assert.assertEquals("car C10099 in staging should be assigned to train", train1, c1.getTrain());
        Assert.assertEquals("car X1001 in staging should be assigned to train", train1, c3.getTrain());
        Assert.assertEquals("car X1002 in staging should be assigned to train", train1, c4.getTrain());

        Assert.assertEquals("car 888 at siding has load L, excluded", null, c8.getTrain());

        train1.addLoadName("L");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 After include load L", train1.isBuilt());

        Assert.assertEquals("car C10099 in staging should be assigned to train", train1, c1.getTrain());
        Assert.assertEquals("car X1001 in staging should be assigned to train", train1, c3.getTrain());
        Assert.assertEquals("car X1002 in staging should be assigned to train", train1, c4.getTrain());

        Assert.assertEquals("car 888 at siding has load L, now included", train1, c8.getTrain());

        train1.setLoadOption(Train.EXCLUDE_LOADS);
        // cars in staging have E loads, so build should fail
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("Train 1 After exclude loads", train1.isBuilt());

        // allow train to carry E loads
        train1.deleteLoadName("E");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After exclude loads L", true, train1.isBuilt());

        Assert.assertEquals("car C10099 in staging should be assigned to train", train1, c1.getTrain());
        Assert.assertEquals("car X1001 in staging should be assigned to train", train1, c3.getTrain());
        Assert.assertEquals("car X1002 in staging should be assigned to train", train1, c4.getTrain());

        Assert.assertEquals("car 888 at siding has load L, now excluded", null, c8.getTrain());

        // done
        train1.setLoadOption(Train.ALL_LOADS);
    }

    /*
     * Test service direction departing staging
     */
    @Test
    public void testStagingtoStagingK() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // Try again, but now set staging track service direction NORTH, train departs to the south
        l1s1.setTrainDirections(Location.NORTH);
        l1s2.setTrainDirections(Location.NORTH);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("Train 1 After 1st Build staging set to North", train1.isBuilt());

        l1s1.setTrainDirections(Location.SOUTH);
        l1s2.setTrainDirections(Location.SOUTH);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 After 2nd Build staging set to South", train1.isBuilt());

        // need to reset train to release cars
        train1.reset();
    }

    /*
     * Test car departing staging with destination
     */
    @Test
    public void testStagingtoStagingL() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Car c4 = cmanager.getByRoadAndNumber("CP","X10002");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // Try again, but now have a car with a destination not serviced by train
        Location nowhere = lmanager.newLocation("nowhere");
        c4.setDestination(nowhere, null);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build car to nowhere", false, train1.isBuilt());
        c4.setDestination(null, null);

        // Build the trains again!!
        train1.reset();
        new TrainBuilder().build(train1);
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertTrue("Train 1 After Build with engines include all roads", train1.isBuilt());
        Assert.assertEquals("Train 1 After Build Departs Name", "North End", train1.getTrainDepartsName());
        Assert.assertEquals("Train 1 After Build Terminates Name", "South End", train1
                .getTrainTerminatesName());
        Assert.assertEquals("Train 1 After Build Next Location Name", "North Industries", train1
                .getNextLocationName());
    }

    /*
     * Test route car move counts out of staging, move and terminate trains.
     * Confirm cars go to correct locations and tracks.
     */
    @Test
    public void testStagingtoStagingM() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP","C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP","C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP","X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP","X10002");
        Car c5 = cmanager.getByRoadAndNumber("CP","X20001");
        Car c6 = cmanager.getByRoadAndNumber("CP","X20002");
        Car c7 = cmanager.getByRoadAndNumber("CP","777");
        Car c8 = cmanager.getByRoadAndNumber("CP","888");
        Car c9 = cmanager.getByRoadAndNumber("CP","99");
        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");
        Location l2 = lmanager.getLocationById("2");
        Location l3 = lmanager.getLocationById("3");
        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");
        Track l2s1 = l2.getTrackById("2s1");
        Track l3s1 = l3.getTrackById("3s1");
        Track l3s2 = l3.getTrackById("3s2");
        Route route1 = rmanager.getRouteById("1");
        RouteLocation rl1 = route1.getLocationById("1r1");

        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // now trying limiting the number of cars that can depart staging
        rl1.setMaxCarMoves(2); // there are three cars in staging
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("Train 1 After Build limit train to 2 cars out of staging", train1.isBuilt());

        // try again but now the train can have four cars departing staging
        rl1.setMaxCarMoves(4); // there are four cars in staging
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 After Build limit train to 4 cars out of staging", train1.isBuilt());

        // Move the train #1
        train1.move();
        Assert.assertEquals("Train 1 After 1st Move Current Name", "North Industries", train1
                .getCurrentLocationName());
        Assert.assertEquals("Train 1 After 1st Move Next Location Name", "South End", train1
                .getNextLocationName());

        // Is the train in route?
        Assert.assertEquals("Train 1 in route after 1st", true, train1.isTrainEnRoute());

        // Try and reset the train
        Assert.assertEquals("Train 1 Reset should be false", false, train1.reset());

        // Are the engine and car locations correct?
        Assert.assertEquals("Engine e1 After After 1st Move", "North Industries", e1.getLocationName());
        Assert.assertEquals("Engine e2 After After 1st Move", "North Industries", e2.getLocationName());
        Assert.assertEquals("Car c1 After After 1st Move", "North Industries", c1.getLocationName());
        Assert.assertEquals("Car c3 After After 1st Move", "North Industries", c3.getLocationName());
        Assert.assertEquals("Car c4 After After 1st Move", "North Industries", c4.getLocationName());
        Assert.assertEquals("Car c8 After After 1st Move", "North Industries", c8.getLocationName());

        // Are the location pickup and drop counts correct?
        Assert.assertEquals("Move 1 Drop count for North End", 0, l1.getDropRS());
        Assert.assertEquals("Move 1 Drop count for North Industries", 1, l2.getDropRS());
        Assert.assertEquals("Move 1 Drop count for South End", 5, l3.getDropRS());
        Assert.assertEquals("Move 1 Pickup count for North End", 0, l1.getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for North Industries", 0, l2.getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for South End", 0, l3.getPickupRS());

        // Are the track pickup and drop counts correct?
        Assert.assertEquals("Move 1 Drop count for North End, track North End 1", 0, l1s1.getDropRS());
        Assert.assertEquals("Move 1 Drop count for North End, track North End 2", 0, l1s2.getDropRS());
        Assert.assertEquals("Move 1 Pickup count for North End, track North End 1", 0, l1s1.getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for North End, track North End 2", 0, l1s2.getPickupRS());
        Assert.assertEquals("Move 1 Drop count for North Industries, track NI Yard", 1, l2s1.getDropRS());
        Assert.assertEquals("Move 1 Pickup count for North Industries, track NI Yard", 0, l2s1.getPickupRS());
        Assert.assertEquals("Move 1 Drop count for South End, track South End 1", 5, l3s1.getDropRS());
        Assert.assertEquals("Move 1 Drop count for South End, track South End 2", 0, l3s2.getDropRS());
        Assert.assertEquals("Move 1 Pickup count for South End, track South End 1", 0, l3s1.getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for South End, track South End 2", 0, l3s2.getPickupRS());

        // Move the train #2
        train1.move();
        Assert.assertEquals("Train 1 After 2nd Move Current Name", "South End", train1
                .getCurrentLocationName());
        Assert.assertEquals("Train 1 After 2nd Move Next Location Name", "", train1.getNextLocationName());
        // Is the train in route?
        Assert.assertEquals("Train 1 in route after 2nd", true, train1.isTrainEnRoute());

        // Are the engine and car locations correct?
        Assert.assertEquals("Engine e1 After After 2nd Move", "South End", e1.getLocationName());
        Assert.assertEquals("Engine e2 After After 2nd Move", "South End", e2.getLocationName());
        Assert.assertEquals("Car c1 After After 2nd Move", "South End", c1.getLocationName());
        Assert.assertEquals("Car c3 After After 2nd Move", "North Industries", c3.getLocationName());
        Assert.assertEquals("Car c4 After After 2nd Move", "South End", c4.getLocationName());
        Assert.assertEquals("Car c8 After After 2nd Move", "North Industries", c8.getLocationName());

        // was c3 released from train?
        Assert.assertEquals("Car c3 After drop should NOT be assigned to Train 1", null, c3.getTrain());
        Assert.assertEquals("Car c3 destination After 2nd Move", "", c3.getDestinationTrackName());
        Assert.assertEquals("Car c3 After 2nd Move location", "North Industries", c3.getLocationName());
        Assert.assertEquals("Car c3 After 2nd Move", "NI Yard", c3.getTrackName());
        Assert.assertEquals("Car c3 Moves after drop should be 14", 1, c3.getMoves());

        // Are the location pickup and drop counts correct?
        Assert.assertEquals("Move 2 Drop count for North End", 0, l1.getDropRS());
        Assert.assertEquals("Move 2 Drop count for North Industries", 0, l2.getDropRS());
        Assert.assertEquals("Move 2 Drop count for South End", 5, l3.getDropRS());
        Assert.assertEquals("Move 2 Pickup count for North End", 0, l1.getPickupRS());
        Assert.assertEquals("Move 2 Pickup count for North Industries", 0, l2.getPickupRS());
        Assert.assertEquals("Move 2 Pickup count for South End", 0, l3.getPickupRS());

        // Are the track pickup and drop counts correct?
        Assert.assertEquals("Move 2 Drop count for North End, track North End 1", 0, l1s1.getDropRS());
        Assert.assertEquals("Move 2 Drop count for North End, track North End 2", 0, l1s2.getDropRS());
        Assert.assertEquals("Move 2 Pickup count for North End, track North End 1", 0, l1s1.getPickupRS());
        Assert.assertEquals("Move 2 Pickup count for North End, track North End 2", 0, l1s2.getPickupRS());
        Assert.assertEquals("Move 2 Drop count for North Industries, track NI Yard", 0, l2s1.getDropRS());
        Assert.assertEquals("Move 2 Pickup count for North Industries, track NI Yard", 0, l2s1.getPickupRS());
        Assert.assertEquals("Move 2 Drop count for South End, track South End 1", 5, l3s1.getDropRS());
        Assert.assertEquals("Move 2 Drop count for South End, track South End 2", 0, l3s2.getDropRS());
        Assert.assertEquals("Move 2 Pickup count for South End, track South End 1", 0, l3s1.getPickupRS());
        Assert.assertEquals("Move 2 Pickup count for South End, track South End 2", 0, l3s2.getPickupRS());

        // Move the train #3 (Terminate)
        train1.move();
        Assert.assertEquals("Train 1 After 3rd Move Current Name", "", train1.getCurrentLocationName());
        Assert.assertEquals("Train 1 After 3rd Move Next Location Name", "", train1.getNextLocationName());
        Assert.assertEquals("Train 1 After 3rd Move Status", Train.TERMINATED, getTrainStatus(train1));
        // Is the train in route?
        Assert.assertEquals("Train 1 in route after 3rd", false, train1.isTrainEnRoute());

        // Are the engine and car destinations correct?
        Assert.assertEquals("Engine e1 After 3rd Move", "", e1.getDestinationTrackName());
        Assert.assertEquals("Engine e2 After 3rd Move", "", e2.getDestinationTrackName());
        Assert.assertEquals("Car c1 After 3rd Move", "", c1.getDestinationTrackName());
        Assert.assertEquals("Car c4 After 3rd Move", "", c4.getDestinationTrackName());
        Assert.assertEquals("Car c8 After 3rd Move", "", c8.getDestinationTrackName());

        // Are the engine and car final locations correct?
        Assert.assertEquals("Engine e1 After Terminate location", "South End", e1.getLocationName());
        Assert.assertEquals("Engine e2 After Terminate location", "South End", e2.getLocationName());
        Assert.assertEquals("Car c1 After Terminate location", "South End", c1.getLocationName());
        Assert.assertEquals("Car c4 After Terminate location", "South End", c4.getLocationName());
        Assert.assertEquals("Car c8 After Terminate location", "North Industries", c8.getLocationName());

        // Are the engine and car final staging track correct?
        Assert.assertEquals("Engine e1 After Terminate track", "South End 1", e1.getTrackName());
        Assert.assertEquals("Engine e2 After Terminate track", "South End 1", e2.getTrackName());
        Assert.assertEquals("Car c1 After Terminate track", "South End 1", c1.getTrackName());
        Assert.assertEquals("Car c4 After Terminate track", "South End 1", c4.getTrackName());
        Assert.assertEquals("Car c8 After Terminate track", "NI Yard", c8.getTrackName());

        // Are the engine and car moves correct
        Assert.assertEquals("Engine e1 Moves after Terminate should be 124", 124, e1.getMoves());
        Assert.assertEquals("Engine e2 Moves after Terminate should be 322", 322, e2.getMoves());
        Assert.assertEquals("Car c1 Moves after Terminate should be 24", 24, c1.getMoves());
        Assert.assertEquals("Car c4 Moves after Terminate should be 4445", 4445, c4.getMoves());
        Assert.assertEquals("Car c8 Moves after Terminate should be 0", 0, c8.getMoves());

        // Are the location pickup and drop counts correct?
        Assert.assertEquals("Move 3 Drop count for North End", 0, l1.getDropRS());
        Assert.assertEquals("Move 3 Drop count for North Industries", 0, l2.getDropRS());
        Assert.assertEquals("Move 3 Drop count for South End", 0, l3.getDropRS());
        Assert.assertEquals("Move 3 Pickup count for North End", 0, l1.getPickupRS());
        Assert.assertEquals("Move 3 Pickup count for North Industries", 0, l2.getPickupRS());
        Assert.assertEquals("Move 3 Pickup count for South End", 0, l3.getPickupRS());

        // Are the track pickup and drop counts correct?
        Assert.assertEquals("Move 3 Drop count for North End, track North End 1", 0, l1s1.getDropRS());
        Assert.assertEquals("Move 3 Drop count for North End, track North End 2", 0, l1s2.getDropRS());
        Assert.assertEquals("Move 3 Pickup count for North End, track North End 1", 0, l1s1.getPickupRS());
        Assert.assertEquals("Move 3 Pickup count for North End, track North End 2", 0, l1s2.getPickupRS());
        Assert.assertEquals("Move 3 Drop count for North Industries, track NI Yard", 0, l2s1.getDropRS());
        Assert.assertEquals("Move 3 Pickup count for North Industries, track NI Yard", 0, l2s1.getPickupRS());
        Assert.assertEquals("Move 3 Drop count for South End, track South End 1", 0, l3s1.getDropRS());
        Assert.assertEquals("Move 3 Drop count for South End, track South End 2", 0, l3s2.getDropRS());
        Assert.assertEquals("Move 3 Pickup count for South End, track South End 1", 0, l3s1.getPickupRS());
        Assert.assertEquals("Move 3 Pickup count for South End, track South End 2", 0, l3s2.getPickupRS());

        // Move the train 1 for the forth time, this shouldn't change anything
        train1.move();
        Assert.assertEquals("Train 1 After 4th Move Current Name", "", train1.getCurrentLocationName());
        Assert.assertEquals("Train 1 After 4th Move Next Location Name", "", train1.getNextLocationName());
        Assert.assertEquals("Train 1 After 4th Move Status", Train.TERMINATED, getTrainStatus(train1));
        // Is the train in route?
        Assert.assertEquals("Train 1 sould not be in route", false, train1.isTrainEnRoute());

        // Are the engines and cars released from train 1?
        Assert.assertEquals("Engine e1 After Terminate should NOT be assigned to Train 1", null, e1
                .getTrain());
        Assert.assertEquals("Engine e2 After Terminate should NOT be assigned to Train 1", null, e2
                .getTrain());
        Assert.assertEquals("Car c1 After Terminate should NOT be assigned to Train 1", null, c1.getTrain());
        Assert.assertEquals("Car c4 After Terminate should NOT be assigned to Train 1", null, c4.getTrain());

        // do cars have the right loads?
        Assert.assertEquals("Car c1 load after Terminate", "E", c1.getLoadName());
        Assert.assertEquals("Car c2 load after Terminate", "E", c2.getLoadName());
        Assert.assertEquals("Car c3 load after Terminate", "E", c3.getLoadName());
        Assert.assertEquals("Car c4 load after Terminate", "E", c4.getLoadName());
        Assert.assertEquals("Car c5 load after Terminate", "E", c5.getLoadName());
        Assert.assertEquals("Car c6 load after Terminate", "E", c6.getLoadName());
        Assert.assertEquals("Car c7 load after Terminate", "E", c7.getLoadName());
        Assert.assertEquals("Car c8 load after Terminate", "E", c8.getLoadName());
        Assert.assertEquals("Car c9 load after Terminate", "E", c9.getLoadName());

        // reset train 2
        Assert.assertTrue("reset train2", train2.reset());

        // Are the location pickup and drop counts correct?
        Assert.assertEquals("Terminated Drop count for North End", 0, l1.getDropRS());
        Assert.assertEquals("Terminated Drop count for North Industries", 0, l2.getDropRS());
        Assert.assertEquals("Terminated Drop count for South End", 0, l3.getDropRS());
        Assert.assertEquals("Terminated Pickup count for North End", 0, l1.getPickupRS());
        Assert.assertEquals("Terminated Pickup count for North Industries", 0, l2.getPickupRS());
        Assert.assertEquals("Terminated Pickup count for South End", 0, l3.getPickupRS());

        // Are the track pickup and drop counts correct?
        Assert.assertEquals("Terminated Drop count for North End, track North End 1", 0, l1s1.getDropRS());
        Assert.assertEquals("Terminated Drop count for North End, track North End 2", 0, l1s2.getDropRS());
        Assert.assertEquals("Terminated Pickup count for North End, track North End 1", 0, l1s1.getPickupRS());
        Assert.assertEquals("Terminated Pickup count for North End, track North End 2", 0, l1s2.getPickupRS());
        Assert.assertEquals("Terminated Drop count for North Industries, track NI Yard", 0, l2s1.getDropRS());
        Assert.assertEquals("Terminated Pickup count for North Industries, track NI Yard", 0, l2s1
                .getDropRS());
        Assert.assertEquals("Terminated Drop count for South End, track South End 1", 0, l3s1.getDropRS());
        Assert.assertEquals("Terminated Drop count for South End, track South End 2", 0, l3s2.getDropRS());
        Assert.assertEquals("Terminated Pickup count for South End, track South End 1", 0, l3s1.getPickupRS());
        Assert.assertEquals("Terminated Pickup count for South End, track South End 2", 0, l3s2.getPickupRS());
    }

    /*
     * Test number of engines departing staging
     */
    @Test
    public void testStagingtoStagingN() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));
        train2.addTypeName("Boxcar");

        // this should fail, there are two engines in staging
        train2.setNumberEngines("1");
        // now build train 2 testing failure modes
        new TrainBuilder().build(train2);
        // build required 1 engine and there were two
        Assert.assertFalse("Train 2 After Build require 1 engine", train2.isBuilt());
        // take one engine out of the consist
        e4.setConsist(null);
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertFalse("Train 2 After Build require 1 engine, but 2 engines on staging track", train2.isBuilt());
        e4.setLocation(null, null); // remove engine from staging
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertTrue("Train 2 After Build require 1 engine, 1 consisted engine on staging track", train2.isBuilt());
        // take lead engine out of consist
        e3.setConsist(null);
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertEquals("Train 2 After Build require 1 engine, single engine on staging track", true,
                train2.isBuilt());
    }

    /*
     * Test number of engines departing staging
     */
    @Test
    public void testStagingtoStagingO() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));
        train2.addTypeName("Boxcar");

        // should work for 0
        train2.setNumberEngines("0");
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertTrue("Train 2 After Build require 0 engine", train2.isBuilt());
        train2.setNumberEngines("3");
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertFalse("Train 2 After Build require 3 engines", train2.isBuilt());
        train2.setNumberEngines("2");
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertEquals("Train 2 After Build require 2 engine", true, train2.isBuilt());
    } 

    /*
     * Test engine "out of service" departing staging
     */
    @Test
    public void testStagingtoStagingP() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");
        
        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));
        train2.addTypeName("Boxcar");
        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        new TrainBuilder().build(train1);

        // take engine out of service
        e3.setOutOfService(true);
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertFalse("Train 2 After Build engine out of service", train2.isBuilt());
        // put back into service
        e3.setOutOfService(false);
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertTrue("Train 2 After Build engine in service", train2.isBuilt());
    }

    /*
     * Test engine road names when departing staging
     */
    @Test
    public void testStagingtoStagingQ() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));
        train2.addTypeName("Boxcar");

        // try different road
        train2.setEngineRoad("CP");
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertEquals("Train 2 After Build require road CP", false, train2.isBuilt());
        
        train2.setEngineRoad("PC");
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertEquals("Train 2 After Build require road PC", true, train2.isBuilt());
    }

    /*
     * Test car departing staging requiring FRED
     */
    @Test
    public void testStagingtoStagingR() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");

        Car c5 = cmanager.getByRoadAndNumber("CP","X20001");

        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));
        train2.addTypeName("Boxcar");

        // try requiring FRED, should fail
        train2.setRequirements(Train.FRED);
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertEquals("Train 2 After Build requires FRED", false, train2.isBuilt());
        // Add FRED to boxcar
        c5.setFred(true);
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertEquals("Train 2 After Build 2 requires FRED", true, train2.isBuilt());
    }
 
    /*
     * Test engine model departing staging
     */
    @Test
    public void testStagingtoStagingS() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");

        Car c2 = cmanager.getByRoadAndNumber("CP","C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP","X10001");

        Car c5 = cmanager.getByRoadAndNumber("CP","X20001");
        Car c6 = cmanager.getByRoadAndNumber("CP","X20002");
        Car c7 = cmanager.getByRoadAndNumber("CP","777");

        Car c9 = cmanager.getByRoadAndNumber("CP","99");
        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));
        train2.addTypeName("Boxcar");

        // try engine wrong model
        train2.setEngineModel("DS45");
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertEquals("Train 2 After Build 2 requires model DS45", false, train2.isBuilt());
        // try engine correct model
        train2.setEngineModel("SD45");
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertEquals("Train 2 After Build 2 requires model SD45", true, train2.isBuilt());
 
        // Are the engines and cars assigned to train 2?
        Assert.assertEquals("Engine e3 After Build should be assigned to Train 2", train2, e3.getTrain());
        Assert.assertEquals("Engine e4 After Build should be assigned to Train 2", train2, e4.getTrain());
        Assert.assertEquals("Car c5 After Build should be assigned to Train 2", train2, c5.getTrain());
        Assert.assertEquals("Car c6 After Build should be assigned to Train 2", train2, c6.getTrain());
        // train 2 does not accept Flat
        Assert.assertNull("Car c7 After Build should NOT be assigned to Train 2", c7.getTrain());
        Assert.assertNull("Car c9 After Build should NOT be assigned to Train 2", c9.getTrain());
        Assert.assertNull("Car c2 After Build should NOT be assigned to Train 2", c2.getTrain());
        Assert.assertNull("Car c3 After Build should NOT be assigned to Train 2", c3.getTrain());

    }

    /*
     * Test train departing staging, car types, lengths, and loads
     */
    @Test
    public void testStagingtoStagingT() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP","C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP","C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP","X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP","X10002");
        Car c5 = cmanager.getByRoadAndNumber("CP","X20001");
        Car c6 = cmanager.getByRoadAndNumber("CP","X20002");
        Car c7 = cmanager.getByRoadAndNumber("CP","777");
        Car c8 = cmanager.getByRoadAndNumber("CP","888");
        Car c9 = cmanager.getByRoadAndNumber("CP","99");
        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");

        Location l3 = lmanager.getLocationById("3");
        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        Track l3s2 = l3.getTrackById("3s2");
        Route route1 = rmanager.getRouteById("1");
        RouteLocation rl1 = route1.getLocationById("1r1");
        RouteLocation rl2 = route1.getLocationById("1r2");
        RouteLocation rl3 = route1.getLocationById("1r3");
        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));
        
        train2.addTypeName("Boxcar");

        // now allow Flat
        train2.addTypeName("Flat");
        c9.setLength("200");
        train2.reset();
        new TrainBuilder().build(train2);
        // c9 and c7 have less moves than c3, but there's not enough room for c9 at destination
        Assert.assertEquals("Car c3 After Build 3 should be assigned to Train 2", train2, c3.getTrain());
        Assert.assertEquals("Car c7 After Build 3 should be assigned to Train 2", train2, c7.getTrain());
        Assert.assertEquals("Car c9 After Build 3 should NOT be assigned to Train 2", null, c9.getTrain());
        // c7 is assigned to Staging Track South End 1 its load will swap
        Assert.assertEquals("Car c7 After Build 3 destination", "South End 1", c7.getDestinationTrackName());
        Assert.assertEquals("Car c7 load After Build 3", "E", c7.getLoadName());
        // increase the size of staging
        l3s2.setLength(500);
        // allow default load swaps
        l3s2.setLoadSwapEnabled(true); // South End 2

        train2.reset();
        new TrainBuilder().build(train2);
        // Check expected arrival times
        Assert.assertEquals("Train 2 expected departure time", "22:45", train2.getExpectedArrivalTime(rl1));
        Assert.assertEquals("Train 2 expected North End", "1:00:36", train2.getExpectedArrivalTime(rl2));
        // one car dropped and one is picked up at North End, so travel time + two car moves
        Assert.assertEquals("Train 2 expected North Industries", "1:02:49", train2
                .getExpectedArrivalTime(rl3));

        // the build first resets which removes cars from the train, c3 load should NOT swap
        Assert.assertEquals("Car c3 load After Build 4", "E", c3.getLoadName());
        // c9 has less moves than c3 and c7, and now there's enough room for c9
        Assert.assertEquals("Car c3 After Build 4 should be assigned to Train 2", train2, c3.getTrain());
        Assert.assertEquals("Car c7 After Build 4 should NOT be assigned to Train 2", null, c7.getTrain());
        Assert.assertEquals("Car c9 After Build 4 should NOT be assigned to Train 2", null, c9.getTrain());
        // move the train #1
        train2.move();
        // Is the train in route?
        Assert.assertEquals("Train 2 in route after 1st", true, train2.isTrainEnRoute());
        train2.move(); // #2
        // Is the train in route?
        Assert.assertEquals("Train 2 in route after 2nd", true, train2.isTrainEnRoute());
        train2.move(); // #3
        // Is the train in route?
        Assert.assertEquals("Train 2 in route after 3rd", false, train2.isTrainEnRoute());

        // Are the engine and car final tracks correct?
        Assert.assertEquals("Engine e1 After Terminate track", "South End 2", e1.getTrackName());
        Assert.assertEquals("Engine e2 After Terminate track", "South End 2", e2.getTrackName());
        Assert.assertEquals("Engine e3 After Terminate track", "North End 2", e3.getTrackName());
        Assert.assertEquals("Engine e4 After Terminate track", "North End 2", e4.getTrackName());
        Assert.assertEquals("Car c1 After Terminate track", "South End 2", c1.getTrackName());
        Assert.assertEquals("Car c2 After Terminate track", "South End 2", c2.getTrackName());
        Assert.assertEquals("Car c3 After Terminate track", "NI Yard", c3.getTrackName());
        Assert.assertEquals("Car c4 After Terminate track", "South End 2", c4.getTrackName());
        Assert.assertEquals("Car c5 After Terminate track", "North End 2", c5.getTrackName());
        Assert.assertEquals("Car c6 After Terminate track", "North End 2", c6.getTrackName());
        Assert.assertEquals("Car c7 After Terminate track", "NI Yard", c7.getTrackName());
        Assert.assertEquals("Car c8 After Terminate track", "South End 2", c8.getTrackName());
        Assert.assertEquals("Car c9 After Terminate track", "NI Yard", c9.getTrackName());

        // do cars have the right loads?
        Assert.assertEquals("Car c1 load after Terminate Train 2", "L", c1.getLoadName());
        Assert.assertEquals("Car c2 load after Terminate Train 2", "L", c2.getLoadName());
        Assert.assertEquals("Car c3 load after Terminate Train 2", "E", c3.getLoadName());
        Assert.assertEquals("Car c4 load after Terminate Train 2", "L", c4.getLoadName());
        Assert.assertEquals("Car c5 load after Terminate Train 2", "E", c5.getLoadName());
        Assert.assertEquals("Car c6 load after Terminate Train 2", "E", c6.getLoadName());
        Assert.assertEquals("Car c7 load after Terminate Train 2", "E", c7.getLoadName());
        Assert.assertEquals("Car c8 load after Terminate Train 2", "L", c8.getLoadName());
        Assert.assertEquals("Car c9 load after Terminate Train 2", "E", c9.getLoadName());
    }
    
    /*
     * Test train terminating into staging, tracks full, staging track size, car type and road restrictions
     */
    @Test
    public void testStagingtoStagingU() {
        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP","C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP","C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP","X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP","X10002");
        Car c5 = cmanager.getByRoadAndNumber("CP","X20001");
        Car c6 = cmanager.getByRoadAndNumber("CP","X20002");
        Car c7 = cmanager.getByRoadAndNumber("CP","777");
        Car c8 = cmanager.getByRoadAndNumber("CP","888");
        Car c9 = cmanager.getByRoadAndNumber("CP","99");
        Engine e1 = emanager.getByRoadAndNumber("PC","5016");
        Engine e2 = emanager.getByRoadAndNumber("PC","5019");
        Engine e3 = emanager.getByRoadAndNumber("PC","5524");
        Engine e4 = emanager.getByRoadAndNumber("PC","5559");

        Location l1 = lmanager.getLocationById("1");
        Location l2 = lmanager.getLocationById("2");
        Location l3 = lmanager.getLocationById("3");
        Track l1s1 = l1.getTrackById("1s1");

        Track l2s1 = l2.getTrackById("2s1");
        Track l3s1 = l3.getTrackById("3s1");
        Track l3s2 = l3.getTrackById("3s2");

        // Place Engines and cars on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l3, l3s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l3, l3s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l3, l3s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l3, l3s2));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(l3, l3s2));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(l3, l3s2));
        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(l3, l3s1));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(l3, l3s1));
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(l3, l3s2));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(l3, l3s1));
        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(l3, l3s2));
        
        train2.addTypeName("Boxcar");
        train2.setRoadOption(Train.ALL_ROADS);

        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(l1, l1s1));
        train2.setRequirements(Train.CABOOSE);
        train2.setNumberEngines("0");
        train2.reset();
        new TrainBuilder().build(train2);
        // Should fail both staging tracks are full
        Assert.assertFalse("Train 2 not built", train2.isBuilt());

        // add a new staging track
        Track l3s3 = l3.addTrack("South End 3", Track.STAGING);
        l3s3.setLength(200);
        train2.reset();
        new TrainBuilder().build(train2);
        // Should build
        Assert.assertTrue("Train 2 built", train2.isBuilt());

        // make staging track too small for caboose
        l3s3.setLength(20);
        train2.reset();
        new TrainBuilder().build(train2);
        // Should not build
        Assert.assertFalse("Train 2 built", train2.isBuilt());
        l3s3.setLength(200); // restore

        // Car X10001 is a location North Industries, NI Yard, send boxcar X10001 to staging
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(l2, l2s1));
        Assert.assertEquals("set destination", Track.OKAY, c3.setDestination(l3, null));
        train2.reset();
        new TrainBuilder().build(train2);
        // Should build
        Assert.assertTrue("Train 2 built", train2.isBuilt());
        Assert.assertEquals("Car X10001 to staging track", l3s3, c3.getDestinationTrack());
        Assert.assertEquals("Car X10001 assigned to train 2", train2, c3.getTrain());

        // Send car X10001 to staging and the only track available
        train2.reset();
        c3.setDestination(l3, l3s3); // this removes track, it is now reserved
        train2.reset();
        new TrainBuilder().build(train2);
        // Should not build
        Assert.assertFalse("Train 2 built", train2.isBuilt());
        Assert.assertEquals("Car X10001 assigned to train 2", null, c3.getTrain());

        train2.setRequirements(Train.NO_CABOOSE_OR_FRED);
        train2.reset();

        ct.addName("BOXCARR");
        train2.addTypeName("BOXCAR");
        c3.setTypeName("BOXCAR");
        l3.addTypeName("BOXCAR");
        c3.setDestination(l3, null);
        train2.reset();
        new TrainBuilder().build(train2);
        // Should Not build, staging track South End 3 doesn't service type BOXCAR
        Assert.assertFalse("Train 2 will not build due to BOXCAR", train2.isBuilt());
        Assert.assertEquals("Car X10001 NOT assigned to train 2", null, c3.getTrain());

        // turn off staging check
        Setup.setTrainIntoStagingCheckEnabled(false);
        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertTrue("Train 2 will now build ignoring BOXCAR", train2.isBuilt());
        Assert.assertNull("Car X10001 NOT assigned to train 2", c3.getTrain());
        Setup.setTrainIntoStagingCheckEnabled(true);

        train2.deleteTypeName("BOXCAR");
        c3.setTypeName("Boxcar");
        // control which road will go into staging
        l3s3.setRoadOption(Track.INCLUDE_ROADS);

        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertFalse("Train 2 will NOT build road restriction", train2.isBuilt());

        train2.setRoadOption(Train.INCLUDE_ROADS);
        train2.addRoadName("CP");
        Assert.assertEquals("Number of road names for train", 1, train2.getRoadNames().length);

        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertFalse("Train 2 will NOT build road restriction CP", train2.isBuilt());

        l3s3.addRoadName("CP");
        Assert.assertEquals("Number of road names", 1, l3s3.getRoadNames().length);

        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertTrue("Train 2 will build road restriction CP removed", train2.isBuilt());

        train2.setRoadOption(Train.EXCLUDE_ROADS);
        train2.deleteRoadName("CP");
        Assert.assertEquals("Number of road names for train", 0, train2.getRoadNames().length);

        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertFalse("Train 2 will NOT build road restriction exclude road CP", train2.isBuilt());

        // now allow road into staging
        l3s3.setRoadOption(Track.EXCLUDE_ROADS);
        l3s3.deleteRoadName("CP");
        Assert.assertEquals("Number of road names", 0, l3s3.getRoadNames().length);

        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertTrue("Train 2 will build no road restrictions", train2.isBuilt());

        cr.addName("BM");  // BM is not a road in all languages, so add it.
        l3s3.addRoadName("BM");
        Assert.assertEquals("Number of road names", 1, l3s3.getRoadNames().length);

        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertFalse("Train 2 will Not build, staging track will not accept road BM", train2.isBuilt());

        // end staging to staging 1
    }

    // testStagingtoStaging2 tests the build procedure
    // through the train's build method.
    // test train staging to staging
    @Test
    public void testStagingtoStaging2() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // register the car colors used
        cc.addName("Silver");
        cc.addName("Black");
        cc.addName("Red");

        // register the car lengths used
        cl.addName("32");
        cl.addName("38");
        cl.addName("40");

        // register the car owners used
        co.addName("Owner1");
        co.addName("Owner2");
        co.addName("Owner3");

        // register the car roads used
        cr.addName("CP");
        cr.addName("Road2");
        cr.addName("Road3");

        // register the car types used
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName("Tanker");
        ct.addName(carTypes[1]);

        // register the car loads used
        cld.addType(carTypes[1]);
        cld.addName(carTypes[1], "Flour");
        cld.addName(carTypes[1], "Bags");

        // register the engine models used
        em.addName("GP40");
        em.addName("GP30");

        // Create locations used
        Location loc1;
        loc1 = lmanager.newLocation("Westend");
        loc1.setTrainDirections(Location.WEST + Location.EAST);

        Location loc2;
        loc2 = lmanager.newLocation("Midtown");
        loc2.setTrainDirections(Location.WEST + Location.EAST);

        Location loc3;
        loc3 = lmanager.newLocation("Eastend");
        loc3.setTrainDirections(Location.WEST + Location.EAST);

        Track loc1trk1;
        loc1trk1 = loc1.addTrack("Westend Staging 1", Track.YARD);
        loc1trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc1trk1.setLength(500);

        Track loc2trk1;
        loc2trk1 = loc2.addTrack("Midtown Inbound from West", Track.YARD);
        loc2trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk1.setLength(500);

        Track loc2trk2;
        loc2trk2 = loc2.addTrack("Midtown Inbound from East", Track.YARD);
        loc2trk2.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk2.setLength(500);

        Track loc2trk3;
        loc2trk3 = loc2.addTrack("Midtown Outbound to West", Track.YARD);
        loc2trk3.setTrainDirections(Track.WEST);
        loc2trk3.setLength(500);

        Track loc2trk4;
        loc2trk4 = loc2.addTrack("Midtown Outbound to East", Track.YARD);
        loc2trk4.setTrainDirections(Track.EAST);
        loc2trk4.setLength(500);

        Track loc2trkc1;
        loc2trkc1 = loc2.addTrack("Midtown Caboose to East", Track.YARD);
        loc2trkc1.setTrainDirections(Track.EAST);
        loc2trkc1.setLength(100);

        Track loc2trkc2;
        loc2trkc2 = loc2.addTrack("Midtown Caboose to West", Track.YARD);
        loc2trkc2.setTrainDirections(Track.WEST);
        loc2trkc2.setLength(100);

        Track loc2trke1;
        loc2trke1 = loc2.addTrack("Midtown Engine to East", Track.YARD);
        loc2trke1.setTrainDirections(Track.EAST);
        loc2trke1.setLength(200);

        Track loc2trke2;
        loc2trke2 = loc2.addTrack("Midtown Engine to West", Track.YARD);
        loc2trke2.setTrainDirections(Track.WEST);
        loc2trke2.setLength(200);

        Track loc3trk1;
        loc3trk1 = loc3.addTrack("Eastend Staging 1", Track.YARD);
        loc3trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc3trk1.setLength(500);

        // Create engines used
        Engine e1;
        e1 = emanager.newEngine("CP", "5501");
        e1.setModel("GP30");
        e1.setMoves(5);
        // Test that default engine type is an acceptable type at all locations and tracks
        Assert.assertEquals("Test Engine CP1801 SetLocation 1s1", "okay", e1.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Test Engine CP1801 SetLocation 2s1", "okay", e1.setLocation(loc2, loc2trk1));
        Assert.assertEquals("Test Engine CP1801 SetLocation 2s2", "okay", e1.setLocation(loc2, loc2trk2));
        Assert.assertEquals("Test Engine CP1801 SetLocation 2s3", "okay", e1.setLocation(loc2, loc2trk3));
        Assert.assertEquals("Test Engine CP1801 SetLocation 2s4", "okay", e1.setLocation(loc2, loc2trk4));
        Assert.assertEquals("Test Engine CP1801 SetLocation 3s1", "okay", e1.setLocation(loc3, loc3trk1));
        Assert.assertEquals("Test Engine CP1801 SetLocation 2s4 for real", "okay", e1.setLocation(loc2,
                loc2trke1));

        Engine e2;
        e2 = emanager.newEngine("CP", "5888");
        e2.setModel("GP40");
        Assert.assertEquals("Test Engine CP5888 SetLocation 2s4", "okay", e2.setLocation(loc2, loc2trke2));

        // Create cars used
        Car b1;
        b1 = cmanager.newCar("CP", "81234567");
        b1.setTypeName(carTypes[1]);
        b1.setLength("40");
        b1.setLoadName("L");
        b1.setMoves(5);
        // Test that first carTypes[1] is an acceptable type at all locations and tracks
        Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 1s1", "okay", b1.setLocation(loc1,
                loc1trk1));
        Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 2s1", "okay", b1.setLocation(loc2,
                loc2trk1));
        Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 2s2", "okay", b1.setLocation(loc2,
                loc2trk2));
        Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 2s3", "okay", b1.setLocation(loc2,
                loc2trk3));
        Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 2s4", "okay", b1.setLocation(loc2,
                loc2trk4));
        Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 3s1", "okay", b1.setLocation(loc3,
                loc3trk1));
        Assert.assertEquals("Bob Test Test Car CP81234567 SetLocation 2s4 for real", "okay", b1.setLocation(
                loc2, loc2trk4));

        Car b2;
        b2 = cmanager.newCar("CP", "81234568");
        b2.setTypeName(carTypes[1]);
        b2.setLength("40");
        b2.setMoves(5);
        Assert.assertEquals("Bob Test Test Car CP81234568 SetLocation 2s4", "okay", b2.setLocation(loc2,
                loc2trk4));

        Car b3;
        b3 = cmanager.newCar("CP", "81234569");
        b3.setTypeName(carTypes[1]);
        b3.setLength("40");
        b3.setLoadName("Flour");
        b3.setMoves(5);
        Assert.assertEquals("Bob Test Test Car CP81234569 SetLocation 2s4", "okay", b3.setLocation(loc2,
                loc2trk4));

        Car b4;
        b4 = cmanager.newCar("CP", "81234566");
        b4.setTypeName(carTypes[1]);
        b4.setLength("40");
        b4.setLoadName("Bags");
        b4.setMoves(5);
        Assert.assertEquals("Bob Test Test Car CP81234566 SetLocation 2s4", "okay", b4.setLocation(loc2,
                loc2trk4));

        Car b5;
        b5 = cmanager.newCar("CP", "71234567");
        b5.setTypeName(carTypes[1]);
        b5.setLength("40");
        // b5.setLoad("E");
        Assert.assertEquals("Bob Test Test Car CP71234567 SetLocation 2s4", "okay", b5.setLocation(loc2,
                loc2trk3));

        Car b6;
        b6 = cmanager.newCar("CP", "71234568");
        b6.setTypeName(carTypes[1]);
        b6.setLength("40");
        // b6.setLoad("E");
        Assert.assertEquals("Bob Test Test Car CP71234568 SetLocation 2s4", "okay", b6.setLocation(loc2,
                loc2trk3));

        Car b7;
        b7 = cmanager.newCar("CP", "71234569");
        b7.setTypeName(carTypes[1]);
        b7.setLength("40");
        // b7.setLoad("E");
        Assert.assertEquals("Bob Test Test Car CP71234569 SetLocation 2s4", "okay", b7.setLocation(loc2,
                loc2trk3));

        Car b8;
        b8 = cmanager.newCar("CP", "71234566");
        b8.setTypeName(carTypes[1]);
        b8.setLength("40");
        // b2.setLoad("E");
        Assert.assertEquals("Bob Test Test Car CP71234566 SetLocation 2s4", "okay", b8.setLocation(loc2,
                loc2trk3));

        // Create cars used
        Car c1;
        c1 = cmanager.newCar("CP", "12345678");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setCaboose(true);
        c1.setMoves(5);
        // Test that first Caboose is an acceptable type at all locations and tracks
        Assert.assertEquals("Bob Test Test Caboose CP12345678 SetLocation 1s1", "okay", c1.setLocation(loc1,
                loc1trk1));
        Assert.assertEquals("Bob Test Test Caboose CP12345678 SetLocation 3s1", "okay", c1.setLocation(loc3,
                loc3trk1));
        Assert.assertEquals("Bob Test Test Caboose CP12345678 SetLocation 2s5 for real", "okay", c1
                .setLocation(loc2, loc2trkc1));

        Car c2;
        c2 = cmanager.newCar("CP", "12345679");
        c2.setTypeName(Bundle.getMessage("Caboose"));
        c2.setLength("32");
        c2.setCaboose(true);
        Assert.assertEquals("Bob Test Test Caboose CP12345679 SetLocation 2s5 for real", "okay", c2
                .setLocation(loc2, loc2trkc2));

        // Create routes used
        Route rte1;
        rte1 = rmanager.newRoute("Midtown to Eastend Through");

        RouteLocation rte1rln1;
        rte1rln1 = rte1.addLocation(loc2);
        rte1rln1.setTrainDirection(RouteLocation.EAST);
        rte1rln1.setTrainIconX(175); // set the train icon coordinates
        rte1rln1.setTrainIconY(25);

        RouteLocation rte1rln2;
        rte1rln2 = rte1.addLocation(loc3);
        rte1rln2.setTrainDirection(RouteLocation.EAST);
        rte1rln2.setTrainIconX(25); // set the train icon coordinates
        rte1rln2.setTrainIconY(50);

        Route rte2;
        rte2 = rmanager.newRoute("Midtown to Westend Through");

        RouteLocation rte2rln1;
        rte2rln1 = rte2.addLocation(loc2);
        rte2rln1.setTrainDirection(RouteLocation.WEST);
        rte2rln1.setTrainIconX(75); // set the train icon coordinates
        rte2rln1.setTrainIconY(50);

        RouteLocation rte2rln2;
        rte2rln2 = rte2.addLocation(loc1);
        rte2rln2.setTrainDirection(RouteLocation.WEST);
        rte2rln2.setTrainIconX(125); // set the train icon coordinates
        rte2rln2.setTrainIconY(50);

        // Create trains used
        Train train1;
        train1 = tmanager.newTrain("TestStagingtoStaging2");
        train1.setRoute(rte1);
        train1.setNumberEngines("1");
        train1.setRequirements(Train.CABOOSE);

        Train train2;
        train2 = tmanager.newTrain("MWT");
        train2.setRoute(rte2);
        train2.setNumberEngines("1");
        train2.setRequirements(Train.CABOOSE);

        // Build trains
        train1.reset();
        new TrainBuilder().build(train1);
        train2.reset();
        new TrainBuilder().build(train2);

        Assert.assertTrue("Bob test train1 built", train1.isBuilt());
        Assert.assertTrue("Bob test train2 built", train2.isBuilt());

    }

    /*
     * Test the loading of custom load into cars departing staging
     */
    @Test
    public void testStagingtoStagingCustomLoads() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // register the car colors used
        cc.addName("Silver");
        cc.addName("Black");
        cc.addName("Red");

        // register the car lengths used
        cl.addName("32");
        cl.addName("38");
        cl.addName("40");

        // register the car owners used
        co.addName("Owner1");
        co.addName("Owner2");
        co.addName("Owner3");

        // register the car roads used
        cr.addName("CP");
        cr.addName("Road2");
        cr.addName("Road3");

        // register the car types used
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName("Tanker");
        ct.addName(carTypes[1]);

        // register the car loads used
        cld.addType(carTypes[1]);
        cld.addName(carTypes[1], "Flour");
        cld.addName(carTypes[1], "Bags");

        // register the engine models used
        em.addName("GP40");
        em.addName("GP30");

        // Create locations used
        Location loc1;
        loc1 = lmanager.newLocation("Westend");
        loc1.setLocationOps(Location.STAGING);
        loc1.setTrainDirections(Location.WEST + Location.EAST);

        Location loc2;
        loc2 = lmanager.newLocation("Midtown");
        loc2.setTrainDirections(Location.WEST + Location.EAST);

        Location loc3;
        loc3 = lmanager.newLocation("Eastend");
        loc3.setLocationOps(Location.STAGING);
        loc3.setTrainDirections(Location.WEST + Location.EAST);

        Track loc1trk1;
        loc1trk1 = loc1.addTrack("Westend Staging 1", Track.STAGING);
        loc1trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc1trk1.setLength(500);
        loc1trk1.setAddCustomLoadsAnySpurEnabled(true);
        loc1trk1.setAddCustomLoadsAnyStagingTrackEnabled(true);

        Track loc2trk1;
        loc2trk1 = loc2.addTrack("Midtown Inbound from West", Track.YARD);
        loc2trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk1.setLength(500);

        Track loc2trk2;
        loc2trk2 = loc2.addTrack("Midtown Inbound from East", Track.YARD);
        loc2trk2.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk2.setLength(500);

        Track loc2trk3;
        loc2trk3 = loc2.addTrack("Midtown Outbound to West", Track.YARD);
        loc2trk3.setTrainDirections(Track.WEST);
        loc2trk3.setLength(500);

        Track loc2trk4;
        loc2trk4 = loc2.addTrack("Midtown Outbound to East", Track.YARD);
        loc2trk4.setTrainDirections(Track.EAST);
        loc2trk4.setLength(500);

        Track loc2trkc1;
        loc2trkc1 = loc2.addTrack("Midtown Caboose to East", Track.YARD);
        loc2trkc1.setTrainDirections(Track.EAST);
        loc2trkc1.setLength(100);

        Track loc2trkc2;
        loc2trkc2 = loc2.addTrack("Midtown Caboose to West", Track.YARD);
        loc2trkc2.setTrainDirections(Track.WEST);
        loc2trkc2.setLength(100);

        Track loc2trke1;
        loc2trke1 = loc2.addTrack("Midtown Engine to East", Track.YARD);
        loc2trke1.setTrainDirections(Track.EAST);
        loc2trke1.setLength(200);

        Track loc2trke2;
        loc2trke2 = loc2.addTrack("Midtown Engine to West", Track.YARD);
        loc2trke2.setTrainDirections(Track.WEST);
        loc2trke2.setLength(200);

        Track loc3trk1;
        loc3trk1 = loc3.addTrack("Eastend Staging 1", Track.STAGING);
        loc3trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc3trk1.setLength(500);

        // Create engines used
        Engine e1;
        e1 = emanager.newEngine("CP", "5501");
        e1.setModel("GP30");
        e1.setMoves(5);
        // Test that default engine type is an acceptable type at all locations and tracks
        Assert.assertEquals("Staging Engine CP1801 SetLocation 1s1", "okay", e1.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Staging Engine CP1801 SetLocation 2s1", "okay", e1.setLocation(loc2, loc2trk1));
        Assert.assertEquals("Staging Engine CP1801 SetLocation 2s2", "okay", e1.setLocation(loc2, loc2trk2));
        Assert.assertEquals("Staging Engine CP1801 SetLocation 2s3", "okay", e1.setLocation(loc2, loc2trk3));
        Assert.assertEquals("Staging Engine CP1801 SetLocation 2s4", "okay", e1.setLocation(loc2, loc2trk4));
        Assert.assertEquals("Staging Engine CP1801 SetLocation 3s1", "okay", e1.setLocation(loc3, loc3trk1));
        Assert.assertEquals("Staging Engine CP1801 SetLocation 2s4 for real", "okay", e1.setLocation(loc2,
                loc2trke1));

        Engine e2;
        e2 = emanager.newEngine("CP", "5888");
        e2.setModel("GP40");
        Assert.assertEquals("Staging Engine CP5801 SetLocation 2s4", "okay", e2.setLocation(loc1, loc1trk1));

        // Create cars used
        Car b1;
        b1 = cmanager.newCar("CP", "81234567");
        b1.setTypeName(carTypes[1]);
        b1.setLength("40");
        b1.setLoadName("L");
        b1.setMoves(5);
        // Test that first carTypes[1] is an acceptable type at all locations and tracks
        Assert.assertEquals("Staging Test Car CP81234567 SetLocation 1s1", "okay", b1.setLocation(loc1,
                loc1trk1));
        Assert.assertEquals("Staging Test Car CP81234567 SetLocation 2s1", "okay", b1.setLocation(loc2,
                loc2trk1));
        Assert.assertEquals("Staging Test Car CP81234567 SetLocation 2s2", "okay", b1.setLocation(loc2,
                loc2trk2));
        Assert.assertEquals("Staging Test Car CP81234567 SetLocation 2s3", "okay", b1.setLocation(loc2,
                loc2trk3));
        Assert.assertEquals("Staging Test Car CP81234567 SetLocation 2s4", "okay", b1.setLocation(loc2,
                loc2trk4));
        Assert.assertEquals("Staging Test Car CP81234567 SetLocation 3s1", "okay", b1.setLocation(loc3,
                loc3trk1));
        Assert.assertEquals("Staging Test Car CP81234567 SetLocation 2s4 for real", "okay", b1.setLocation(
                loc1, loc1trk1));

        Car b2;
        b2 = cmanager.newCar("CP", "81234568");
        b2.setTypeName(carTypes[1]);
        b2.setLength("40");
        b2.setMoves(5);
        Assert.assertEquals("Staging Test Car CP81234568 SetLocation 2s4", "okay", b2.setLocation(loc1, loc1trk1));

        Car b3;
        b3 = cmanager.newCar("CP", "81234569");
        b3.setTypeName(carTypes[1]);
        b3.setLength("40");
        b3.setLoadName("Flour");
        b3.setMoves(5);
        Assert.assertEquals("Staging Test Car CP81234569 SetLocation 2s4", "okay", b3.setLocation(loc1, loc1trk1));

        Car b4;
        b4 = cmanager.newCar("CP", "81234566");
        b4.setTypeName(carTypes[1]);
        b4.setLength("40");
        b4.setLoadName("Bags");
        b4.setMoves(5);
        Assert.assertEquals("Staging Test Car CP81234566 SetLocation 2s4", "okay", b4.setLocation(loc1, loc1trk1));

        Car b5;
        b5 = cmanager.newCar("CP", "71234567");
        b5.setTypeName(carTypes[1]);
        b5.setLength("40");
        Assert.assertEquals("Staging Test Car CP71234567 SetLocation 2s4", "okay", b5.setLocation(loc1, loc1trk1));

        Car b6;
        b6 = cmanager.newCar("CP", "71234568");
        b6.setTypeName(carTypes[1]);
        b6.setLength("40");
        Assert.assertEquals("Staging Test Car CP71234568 SetLocation 2s4", "okay", b6.setLocation(loc1, loc1trk1));

        Car b7;
        b7 = cmanager.newCar("CP", "71234569");
        b7.setTypeName(carTypes[1]);
        b7.setLength("40");
        Assert.assertEquals("Staging Test Car CP71234569 SetLocation 2s4", "okay", b7.setLocation(loc2, loc2trk1));

        Car b8;
        b8 = cmanager.newCar("CP", "71234566");
        b8.setTypeName(carTypes[1]);
        b8.setLength("40");
        Assert.assertEquals("Staging Test Car CP71234566 SetLocation 2s4", "okay", b8.setLocation(loc2, loc2trk1));

        Car b9;
        b9 = cmanager.newCar("CP", "54326");
        b9.setTypeName(carTypes[1]);
        b9.setLength("40");
        Assert.assertEquals("Staging Test Car CP54326 SetLocation 2s4", "okay", b9.setLocation(loc2, loc2trk1));

        // Create cabooses
        Car c1;
        c1 = cmanager.newCar("CP", "12345678");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setCaboose(true);
        c1.setMoves(5);
        // Test that first Caboose is an acceptable type at all locations and tracks
        Assert.assertEquals("Staging Test Caboose CP12345678 SetLocation 1s1", "okay", c1.setLocation(loc1,
                loc1trk1));
        Assert.assertEquals("Staging Test Caboose CP12345678 SetLocation 3s1", "okay", c1.setLocation(loc3,
                loc3trk1));
        Assert.assertEquals("Staging Test Caboose CP12345678 SetLocation 2s5 for real", "okay", c1
                .setLocation(loc2, loc2trkc1));

        Car c2;
        c2 = cmanager.newCar("CP", "12345679");
        c2.setTypeName(Bundle.getMessage("Caboose"));
        c2.setLength("32");
        c2.setCaboose(true);
        Assert.assertEquals("Staging Test Caboose CP12345679 SetLocation 2s5 for real", "okay", c2
                .setLocation(loc1, loc1trk1));


        // Create routes used
        Route rte1 = rmanager.newRoute("Westend to Eastend Through");
        Assert.assertEquals("Staging Route rte1 Name", "Westend to Eastend Through", rte1.getName());

        RouteLocation rte1rln1 = rte1.addLocation(loc1);
        rte1rln1.setTrainDirection(RouteLocation.EAST);
        rte1rln1.setTrainIconX(175); // set the train icon coordinates
        rte1rln1.setTrainIconY(75);

        RouteLocation rte1rln2 = rte1.addLocation(loc2);
        rte1rln2.setTrainDirection(RouteLocation.EAST);
        rte1rln2.setTrainIconX(175); // set the train icon coordinates
        rte1rln2.setTrainIconY(25);
        rte1rln2.setMaxCarMoves(2); // this will cause the train build status to be "Built"

        RouteLocation rte1rln3 = rte1.addLocation(loc3);
        rte1rln3.setTrainDirection(RouteLocation.EAST);
        rte1rln3.setTrainIconX(25); // set the train icon coordinates
        rte1rln3.setTrainIconY(50);

        // Create trains used
        Train train1 = tmanager.newTrain("TestStagingtoStagingCustomLoads");
        train1.setRoute(rte1);
        train1.setNumberEngines("1");
        train1.setRequirements(Train.CABOOSE);

        // Build trains
        train1.reset();
        new TrainBuilder().build(train1);

        Assert.assertTrue("train1 built", train1.isBuilt());
        Assert.assertEquals("train1 built", Train.CODE_BUILT, train1.getStatusCode());

        // confirm that custom loads have been added to the cars departing staging
        Assert.assertEquals("load shouldn't change", "L", b1.getLoadName());
        Assert.assertEquals("load shouldn't change", "Flour", b3.getLoadName());
        Assert.assertEquals("load shouldn't change", "Bags", b4.getLoadName());
        Assert.assertEquals("load shouldn't change", "E", b7.getLoadName());
        Assert.assertEquals("load shouldn't change", "E", b8.getLoadName());
        Assert.assertEquals("load shouldn't change", "E", b9.getLoadName());

        Assert.assertNotEquals("Generated custom load", "E", b2.getLoadName());
        Assert.assertNotEquals("Generated custom load", "E", b5.getLoadName());
        Assert.assertNotEquals("Generated custom load", "E", b6.getLoadName());
    }

    // Test TrainBuilder through the train's build method.
    // Test a route of one location (local train).
    // Locations that don't have a train direction assigned
    // can only be served by a local train.
    // Creates two locations Westford and Chelmsford and 9 cars.
    // Westford has 2 yards, 2 sidings, 3 interchange tracks.
    // Chelmsford has 1 yard. Chelmsford is used to test that a
    // train with two locations will not service certain tracks.
    @Test
    public void testLocal() {
        String roadNames[] = Bundle.getMessage("carRoadNames").split(",");
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // Create locations used
        Location loc1;
        loc1 = lmanager.newLocation("Westford");
        loc1.setTrainDirections(DIRECTION_ALL);
        loc1.addTypeName(carTypes[2]);

        Location loc2;
        loc2 = lmanager.newLocation("Chelmsford");
        loc2.setTrainDirections(DIRECTION_ALL);
        loc2.addTypeName(carTypes[2]);

        Track loc1trk1;
        loc1trk1 = loc1.addTrack("Westford Yard 1", Track.YARD);
        loc1trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc1trk1.setLength(500);
        loc1trk1.addTypeName(carTypes[2]);

        Track loc1trk2;
        loc1trk2 = loc1.addTrack("Westford Yard 2", Track.YARD);
        loc1trk2.setTrainDirections(Track.WEST + Track.EAST);
        loc1trk2.setLength(500);
        loc1trk2.addTypeName(carTypes[2]);

        Track loc1trk3;
        loc1trk3 = loc1.addTrack("Westford Siding 3", Track.SPUR);
        loc1trk3.setTrainDirections(0); // Only local moves allowed
        loc1trk3.setLength(300);
        loc1trk3.addTypeName(carTypes[2]);

        Track loc1trk4;
        loc1trk4 = loc1.addTrack("Westford Siding 4", Track.SPUR);
        loc1trk4.setTrainDirections(0); // Only local moves allowed
        loc1trk4.setLength(300);
        loc1trk4.addTypeName(carTypes[2]);

        Track loc1trk5;
        loc1trk5 = loc1.addTrack("Westford Interchange 5", Track.INTERCHANGE);
        loc1trk5.setTrainDirections(0); // Only local moves allowed
        loc1trk5.setLength(300);
        loc1trk5.addTypeName(carTypes[2]);

        Track loc1trk6;
        loc1trk6 = loc1.addTrack("Westford Interchange 6", Track.INTERCHANGE);
        loc1trk6.setTrainDirections(Track.WEST + Track.EAST);
        loc1trk6.setLength(300);
        loc1trk6.addTypeName(carTypes[2]);

        Track loc1trk7;
        loc1trk7 = loc1.addTrack("Westford Interchange 7", Track.INTERCHANGE);
        loc1trk7.setTrainDirections(0); // Only local moves allowed
        loc1trk7.setLength(300);
        loc1trk7.addTypeName(carTypes[2]);

        Track loc2trk1;
        loc2trk1 = loc2.addTrack("Chelmsford Yard 1", Track.YARD);
        loc2trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk1.setLength(900);
        loc2trk1.addTypeName(carTypes[2]);

        // now bias track selection by moves
        loc1trk1.setMoves(3); // no yard to yard moves expected
        loc1trk2.setMoves(4); // no yard to yard moves expected
        loc1trk3.setMoves(10); // this will be the 5th location assigned
        loc1trk4.setMoves(10); // this will be the 6th location assigned
        loc1trk5.setMoves(9); // this will be the 2nd location assigned
        loc1trk6.setMoves(9); // this will be the 3rd location assigned
        loc1trk7.setMoves(8); // this will be the first and 4th location assigned

        // Create route with only one location
        Route rte1 = rmanager.newRoute("Local Route");
        RouteLocation rl1 = rte1.addLocation(loc1);

        // Create train
        Train train1 = tmanager.newTrain("TestLocal");
        train1.setRoute(rte1);
        // Flat Car isn't registered yet so add it now
        train1.addTypeName(carTypes[2]);

        // Set up 7 box cars and 2 flat cars
        Car c1 = new Car(roadNames[1], "1");
        c1.setTypeName(carTypes[1]);
        c1.setLength("90");
        c1.setMoves(17); // should be the 7th car assigned to train
        cmanager.register(c1);

        Car c2 = new Car(roadNames[2], "2");
        c2.setTypeName(carTypes[1]);
        c2.setLength("80");
        c2.setMoves(15); // should be the 6th car assigned to train
        cmanager.register(c2);

        Car c3 = new Car(roadNames[3], "3");
        c3.setTypeName(carTypes[1]);
        c3.setLength("70");
        // default c3 moves = 0 should be the 1st car assigned
        cmanager.register(c3);

        Car c4 = new Car(roadNames[0], "4");
        c4.setTypeName(carTypes[1]);
        c4.setLength("60");
        c4.setMoves(6); // should be the 5th car assigned to train
        cmanager.register(c4);

        Car c5 = new Car(roadNames[0], "5");
        c5.setTypeName(carTypes[1]);
        c5.setLength("50");
        c5.setMoves(1); // should be the 2nd car assigned to train
        cmanager.register(c5);

        Car c6 = new Car(roadNames[2], "6");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(3); // should be the 4th car assigned to train
        cmanager.register(c6);

        Car c7 = new Car(roadNames[5], "7");
        c7.setTypeName(carTypes[1]);
        c7.setLength("50");
        c7.setMoves(18);
        cmanager.register(c7);

        Car c8 = new Car(roadNames[0], "8");
        c8.setTypeName(carTypes[1]);
        c8.setLength("60");
        c8.setMoves(2); // should be the 2rd car assigned to train
        cmanager.register(c8);

        Car c9 = new Car(roadNames[5], "9");
        c9.setTypeName(carTypes[2]);
        c9.setLength("90");
        c9.setMoves(19);
        cmanager.register(c9);

        //		Assert.assertEquals("Westford should not accept Flat Car", false, loc1.acceptsTypeName(carTypes[2]));
        // add Flat Car as a valid type so Westford will accept
        ct.addName(carTypes[2]);
        Assert.assertEquals("Westford should now accepts Flat Car", true, loc1.acceptsTypeName(carTypes[2]));

        // place the cars in the yards
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(loc1, loc1trk1));

        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(loc1, loc1trk2));

        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(loc1, loc1trk3));

        // do cars have the right default loads?
        Assert.assertEquals("Car c1 load should be E", "E", c1.getLoadName());
        Assert.assertEquals("Car c2 load should be E", "E", c2.getLoadName());
        Assert.assertEquals("Car c3 load should be E", "E", c3.getLoadName());
        Assert.assertEquals("Car c4 load should be E", "E", c4.getLoadName());
        Assert.assertEquals("Car c5 load should be E", "E", c5.getLoadName());
        Assert.assertEquals("Car c6 load should be E", "E", c6.getLoadName());
        Assert.assertEquals("Car c7 load should be E", "E", c7.getLoadName());
        Assert.assertEquals("Car c8 load should be E", "E", c8.getLoadName());
        Assert.assertEquals("Car c9 load should be E", "E", c9.getLoadName());

        // Build train
        train1.reset();
        new TrainBuilder().build(train1);

        // check train status
        Assert.assertEquals("Train 1 built", Train.CODE_BUILT, train1.getStatusCode());

        Assert.assertEquals("Train 1 After Build Departs Name", "Westford", train1.getTrainDepartsName());
        Assert.assertEquals("Train 1 After Build Terminates Name", "Westford", train1
                .getTrainTerminatesName());
        Assert.assertEquals("Train 1 After Build Next Location Name", "", train1.getNextLocationName());
        Assert.assertEquals("Train 1 After Build Built Status", true, train1.isBuilt());

        // are the right cars assigned to the train?
        // the default moves is 7, therefore only 7 cars should be moved based on their move counts
        Assert.assertEquals("Car c1 After Build should be assigned to Train 1", train1, c1.getTrain());
        Assert.assertEquals("Car c2 After Build should be assigned to Train 1", train1, c2.getTrain());
        Assert.assertEquals("Car c3 After Build should be assigned to Train 1", train1, c3.getTrain());
        Assert.assertEquals("Car c4 After Build should NOT be assigned to Train 1", train1, c4.getTrain());
        Assert.assertEquals("Car c5 After Build should be assigned to Train 1", train1, c5.getTrain());
        Assert.assertEquals("Car c6 After Build should be assigned to Train 1", train1, c6.getTrain());
        Assert.assertEquals("Car c7 After Build should NOT be assigned to Train 1", null, c7.getTrain());
        Assert.assertEquals("Car c8 After Build should be assigned to Train 1", train1, c8.getTrain());
        Assert.assertEquals("Car c9 After Build should NOT be assigned to Train 1", null, c9.getTrain());

        // now check to see if cars are going to be delivered to the right places?
        Assert.assertEquals("Car c1 After Build destination", "Westford Interchange 5", c1
                .getDestinationTrackName());
        Assert.assertEquals("Car c2 After Build destination", "Westford Siding 4", c2
                .getDestinationTrackName());
        Assert.assertEquals("Car c3 After Build destination", "Westford Interchange 7", c3
                .getDestinationTrackName());
        Assert.assertEquals("Car c4 After Build destination", "Westford Siding 3", c4
                .getDestinationTrackName());
        Assert.assertEquals("Car c5 After Build destination", "Westford Interchange 5", c5
                .getDestinationTrackName());
        Assert.assertEquals("Car c6 After Build destination", "Westford Interchange 7", c6
                .getDestinationTrackName());
        Assert.assertEquals("Car c8 After Build destination", "Westford Interchange 6", c8
                .getDestinationTrackName());

        // are the pickup and drop counts correct?
        Assert.assertEquals("Drop count for Westford", 7, loc1.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Yard 1", 0, loc1trk1.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Yard 2", 0, loc1trk2.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Siding 3", 1, loc1trk3.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Siding 4", 1, loc1trk4.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Interchange 5", 2, loc1trk5.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Interchange 6", 1, loc1trk6.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Interchange 7", 2, loc1trk7.getDropRS());
        Assert.assertEquals("Pickup count for Westford", 7, loc1.getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Yard 1", 4, loc1trk1.getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Yard 2", 3, loc1trk2.getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Siding 3", 0, loc1trk3.getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Siding 4", 0, loc1trk4.getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Interchange 5", 0, loc1trk5
                .getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Interchange 6", 0, loc1trk6
                .getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Interchange 7", 0, loc1trk7
                .getPickupRS());

        train1.move();
        // Train should not be in route since there's only one location
        Assert.assertEquals("Train 1 not in route", false, train1.isTrainEnRoute());
        // check train status
        Assert.assertEquals("Train 1 not en route", Train.TERMINATED, getTrainStatus(train1));
        Assert.assertEquals("Train 1 not en route", Train.CODE_TERMINATED, train1.getStatusCode());
        // check for correct tracks
        Assert.assertEquals("Car c1 After Move location", "Westford Interchange 5", c1.getTrackName());
        Assert.assertEquals("Car c2 After Move location", "Westford Siding 4", c2.getTrackName());
        Assert.assertEquals("Car c3 After Move location", "Westford Interchange 7", c3.getTrackName());
        Assert.assertEquals("Car c4 After Move location", "Westford Siding 3", c4.getTrackName());
        Assert.assertEquals("Car c5 After Move location", "Westford Interchange 5", c5.getTrackName());
        Assert.assertEquals("Car c6 After Move location", "Westford Interchange 7", c6.getTrackName());
        Assert.assertEquals("Car c8 After Move location", "Westford Interchange 6", c8.getTrackName());

        // do cars have the right loads?
        Assert.assertEquals("Car c1 After Move load should be E", "E", c1.getLoadName());
        Assert.assertEquals("Car c2 After Move load should be L", "L", c2.getLoadName());
        Assert.assertEquals("Car c3 After Move load should be E", "E", c3.getLoadName());
        Assert.assertEquals("Car c4 After Move load should be L", "L", c4.getLoadName());
        Assert.assertEquals("Car c5 After Move load should be E", "E", c5.getLoadName());
        Assert.assertEquals("Car c6 After Move load should be E", "E", c6.getLoadName());
        Assert.assertEquals("Car c7 After Move load should be E", "E", c7.getLoadName());
        Assert.assertEquals("Car c8 After Move load should be E", "E", c8.getLoadName());
        Assert.assertEquals("Car c9 After Move load should be E", "E", c9.getLoadName());

        // are the pickup and drop counts correct?
        Assert.assertEquals("Move 1 Drop count for Westford", 0, loc1.getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Yard 1", 0, loc1trk1.getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Yard 2", 0, loc1trk2.getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Siding 3", 0, loc1trk3.getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Siding 4", 0, loc1trk4.getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Interchange 5", 0, loc1trk5
                .getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Interchange 6", 0, loc1trk6
                .getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Interchange 7", 0, loc1trk7
                .getDropRS());
        Assert.assertEquals("Move 1 Pickup count for Westford", 0, loc1.getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Yard 1", 0, loc1trk1
                .getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Yard 2", 0, loc1trk2
                .getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Siding 3", 0, loc1trk3
                .getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Siding 4", 0, loc1trk4
                .getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Interchange 5", 0, loc1trk5
                .getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Interchange 6", 0, loc1trk6
                .getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Interchange 7", 0, loc1trk7
                .getPickupRS());

        // Verify that an extra move will not change train status.
        train1.move();
        Assert.assertEquals("Train 1 After 2nd Move Status", Train.TERMINATED, getTrainStatus(train1));
        Assert.assertEquals("Train 1 After 2nd Move Status", Train.CODE_TERMINATED, train1.getStatusCode());

        // build the train again, now there are cars on all tracks
        rl1.setMaxCarMoves(10); // try and use all 9/10 of the cars
        train1.reset();
        new TrainBuilder().build(train1);
        // c1, c3, c5, c6, c8 are at interchange tracks and should not be assigned to train1
        Assert.assertEquals("Car c1 After Build 2 should NOT be assigned to Train 1", null, c1.getTrain());
        Assert.assertEquals("Car c2 After Build 2 should be assigned to Train 1", train1, c2.getTrain());
        Assert.assertEquals("Car c3 After Build 2 should NOT be assigned to Train 1", null, c3.getTrain());
        Assert.assertEquals("Car c4 After Build 2 should be assigned to Train 1", train1, c4.getTrain());
        Assert.assertEquals("Car c5 After Build 2 should NOT be assigned to Train 1", null, c5.getTrain());
        Assert.assertEquals("Car c6 After Build 2 should NOT be assigned to Train 1", null, c6.getTrain());
        Assert.assertEquals("Car c7 After Build 2 should be assigned to Train 1", train1, c7.getTrain());
        Assert.assertEquals("Car c8 After Build 2 should NOT be assigned to Train 1", null, c8.getTrain());
        Assert.assertEquals("Car c9 After Build 2 should be assigned to Train 1", train1, c9.getTrain());

        // now check to see if cars are going to be delivered to the right places?
        Assert.assertEquals("Car c2 After Build 2 destination", "Westford Yard 1", c2
                .getDestinationTrackName());
        Assert.assertEquals("Car c4 After Build 2 destination", "Westford Yard 1", c4
                .getDestinationTrackName());
        Assert.assertEquals("Car c7 After Build 2 destination", "Westford Interchange 6", c7
                .getDestinationTrackName());
        Assert.assertEquals("Car c9 After Build 2 destination", "Westford Yard 2", c9
                .getDestinationTrackName());
        // move and terminate
        train1.move();
        Assert.assertEquals("Train 1 After 2nd build Status", Train.TERMINATED, getTrainStatus(train1));
        Assert.assertEquals("Train 1 After 2nd build Status", Train.CODE_TERMINATED, train1.getStatusCode());

        // are cars at the right location?
        Assert.assertEquals("Car c2 After Move 2 location", "Westford Yard 1", c2.getTrackName());
        Assert.assertEquals("Car c4 After Move 2 location", "Westford Yard 1", c4.getTrackName());
        Assert.assertEquals("Car c7 After Move 2 location", "Westford Interchange 6", c7.getTrackName());
        Assert.assertEquals("Car c9 After Move 2 location", "Westford Yard 2", c9.getTrackName());

        // do cars have the right loads?
        Assert.assertEquals("Car c1 After Move 2 load should be E", "E", c1.getLoadName());
        Assert.assertEquals("Car c2 After Move 2 load should be L", "L", c2.getLoadName());
        Assert.assertEquals("Car c3 After Move 2 load should be E", "E", c3.getLoadName());
        Assert.assertEquals("Car c4 After Move 2 load should be L", "L", c4.getLoadName());
        Assert.assertEquals("Car c5 After Move 2 load should be E", "E", c5.getLoadName());
        Assert.assertEquals("Car c6 After Move 2 load should be E", "E", c6.getLoadName());
        Assert.assertEquals("Car c7 After Move 2 load should be E", "E", c7.getLoadName());
        Assert.assertEquals("Car c8 After Move 2 load should be E", "E", c8.getLoadName());
        Assert.assertEquals("Car c9 After Move 2 load should be E", "E", c9.getLoadName());

        // try a new route, this should allow cars to move from interchange
        // Create route with only one location
        Route rte2;
        rte2 = rmanager.newRoute("Local Route 2");
        RouteLocation rl2 = rte2.addLocation(loc1);
        rl2.setMaxCarMoves(8); // move 8 of the 9 cars available
        // and assign the new route to train 1
        train1.setRoute(rte2);
        train1.reset();
        new TrainBuilder().build(train1);
        // we should be able to pickup cars at the interchange tracks
        Assert.assertEquals("Car c1 After Build 3 should be assigned to Train 1", train1, c1.getTrain());
        Assert.assertEquals("Car c2 After Build 3 should be assigned to Train 1", train1, c2.getTrain());
        Assert.assertEquals("Car c3 After Build 3 should be assigned to Train 1", train1, c3.getTrain());
        Assert.assertEquals("Car c4 After Build 3 should be assigned to Train 1", train1, c4.getTrain());
        Assert.assertEquals("Car c5 After Build 3 should be assigned to Train 1", train1, c5.getTrain());
        Assert.assertEquals("Car c6 After Build 3 should be assigned to Train 1", train1, c6.getTrain());
        Assert.assertEquals("Car c7 After Build 3 should be assigned to Train 1", train1, c7.getTrain());
        Assert.assertEquals("Car c8 After Build 3 should be assigned to Train 1", train1, c8.getTrain());
        Assert.assertEquals("Car c9 After Build 3 should NOT be assigned to Train 1", null, c9.getTrain());

        // now check to see if cars are going to be delivered to the right places?
        Assert.assertEquals("Car c1 After Build 3 destination", "Westford Yard 1", c1
                .getDestinationTrackName());
        Assert.assertEquals("Car c2 After Build 3 destination", "Westford Siding 3", c2
                .getDestinationTrackName());
        Assert.assertEquals("Car c3 After Build 3 destination", "Westford Yard 1", c3
                .getDestinationTrackName());
        Assert.assertEquals("Car c4 After Build 3 destination", "Westford Interchange 7", c4
                .getDestinationTrackName());
        Assert.assertEquals("Car c5 After Build 3 destination", "Westford Yard 2", c5
                .getDestinationTrackName());
        Assert.assertEquals("Car c6 After Build 3 destination", "Westford Yard 2", c6
                .getDestinationTrackName());
        Assert.assertEquals("Car c7 After Build 3 destination", "Westford Yard 2", c7
                .getDestinationTrackName());
        Assert.assertEquals("Car c8 After Build 3 destination", "Westford Yard 1", c8
                .getDestinationTrackName());
        // move and terminate
        train1.move();
        Assert.assertEquals("Train 1 After 2nd build Status", Train.TERMINATED, getTrainStatus(train1));
        Assert.assertEquals("Train 1 After 2nd build Status", Train.CODE_TERMINATED, train1.getStatusCode());

        // Final check to see if cars were delivered.
        Assert.assertEquals("Car c1 After Move 3 location", "Westford Yard 1", c1.getTrackName());
        Assert.assertEquals("Car c2 After Move 3 location", "Westford Siding 3", c2.getTrackName());
        Assert.assertEquals("Car c3 After Move 3 location", "Westford Yard 1", c3.getTrackName());
        Assert.assertEquals("Car c4 After Move 3 location", "Westford Interchange 7", c4.getTrackName());
        Assert.assertEquals("Car c5 After Move 3 location", "Westford Yard 2", c5.getTrackName());
        Assert.assertEquals("Car c6 After Move 3 location", "Westford Yard 2", c6.getTrackName());
        Assert.assertEquals("Car c7 After Move 3 location", "Westford Yard 2", c7.getTrackName());
        Assert.assertEquals("Car c8 After Move 3 location", "Westford Yard 1", c8.getTrackName());

        // do cars have the right loads?
        Assert.assertEquals("Car c1 After Move 3 load should be E", "E", c1.getLoadName());
        Assert.assertEquals("Car c2 After Move 3 load should be E", "E", c2.getLoadName());
        Assert.assertEquals("Car c3 After Move 3 load should be E", "E", c3.getLoadName());
        Assert.assertEquals("Car c4 After Move 3 load should be L", "L", c4.getLoadName());
        Assert.assertEquals("Car c5 After Move 3 load should be E", "E", c5.getLoadName());
        Assert.assertEquals("Car c6 After Move 3 load should be E", "E", c6.getLoadName());
        Assert.assertEquals("Car c7 After Move 3 load should be E", "E", c7.getLoadName());
        Assert.assertEquals("Car c8 After Move 3 load should be E", "E", c8.getLoadName());
        Assert.assertEquals("Car c9 After Move 3 load should be E", "E", c9.getLoadName());

        // check car move counts
        Assert.assertEquals("Car c1 Move count", 19, c1.getMoves());
        Assert.assertEquals("Car c2 Move count", 18, c2.getMoves());
        Assert.assertEquals("Car c3 Move count", 2, c3.getMoves());
        Assert.assertEquals("Car c4 Move count", 9, c4.getMoves());
        Assert.assertEquals("Car c5 Move count", 3, c5.getMoves());
        Assert.assertEquals("Car c6 Move count", 5, c6.getMoves());
        Assert.assertEquals("Car c7 Move count", 20, c7.getMoves());
        Assert.assertEquals("Car c8 Move count", 4, c8.getMoves());
        Assert.assertEquals("Car c9 Move count", 20, c9.getMoves());

        // now try and use a train with more than one location
        // Create route with two locations
        Route rte3;
        rte3 = rmanager.newRoute("Westford to Chelmsford");
        RouteLocation rl3 = rte3.addLocation(loc1);
        rl3.setTrainDirection(RouteLocation.WEST);
        rl3.setMaxCarMoves(10);
        RouteLocation rl4 = rte3.addLocation(loc2);
        rl4.setTrainDirection(RouteLocation.WEST);
        // and assign the new route to train 1
        train1.setRoute(rte3);
        rl4.setMaxCarMoves(10);
        rl4.setTrainIconX(175); // set the train icon coordinates
        rl4.setTrainIconY(50);

        train1.reset();
        new TrainBuilder().build(train1);
        // should not pick up cars at Westford Siding 3, Westford Siding 4, Westford Interchange 5
        // and Westford Interchange 7
        Assert.assertEquals("Car c1 After Build 4 should be assigned to Train 1", train1, c1.getTrain());
        Assert.assertEquals("Car c2 After Build 4 should NOT be assigned to Train 1", null, c2.getTrain());
        Assert.assertEquals("Car c3 After Build 4 should be assigned to Train 1", train1, c3.getTrain());
        Assert.assertEquals("Car c4 After Build 4 should NOT be assigned to Train 1", null, c4.getTrain());
        Assert.assertEquals("Car c5 After Build 4 should be assigned to Train 1", train1, c5.getTrain());
        Assert.assertEquals("Car c6 After Build 4 should be assigned to Train 1", train1, c6.getTrain());
        Assert.assertEquals("Car c7 After Build 4 should be assigned to Train 1", train1, c7.getTrain());
        Assert.assertEquals("Car c8 After Build 4 should be assigned to Train 1", train1, c8.getTrain());
        Assert.assertEquals("Car c9 After Build 4 should be assigned to Train 1", train1, c9.getTrain());

        train1.move();
        // Train in route since there's two locations
        Assert.assertEquals("Train 1 in route to Chelmsford", true, train1.isTrainEnRoute());
        Assert.assertEquals("Train 1 in route to Chelmsford", Train.CODE_TRAIN_EN_ROUTE, train1.getStatusCode());
        train1.move();
        // 7 cars should in Chelmsford, the other 2 in Westford
        Assert.assertEquals("Car c1 After Move 4 location", "Chelmsford Yard 1", c1.getTrackName());
        Assert.assertEquals("Car c2 verify location", "Westford Siding 3", c2.getTrackName());
        Assert.assertEquals("Car c3 After Move 4 location", "Chelmsford Yard 1", c3.getTrackName());
        Assert.assertEquals("Car c4 verify location", "Westford Interchange 7", c4.getTrackName());
        Assert.assertEquals("Car c5 After Move 4 location", "Chelmsford Yard 1", c5.getTrackName());
        Assert.assertEquals("Car c6 After Move 4 location", "Chelmsford Yard 1", c6.getTrackName());
        Assert.assertEquals("Car c7 After Move 4 location", "Chelmsford Yard 1", c7.getTrackName());
        Assert.assertEquals("Car c8 After Move 4 location", "Chelmsford Yard 1", c8.getTrackName());
        Assert.assertEquals("Car c9 After Move 4 location", "Chelmsford Yard 1", c9.getTrackName());

        // do cars have the right loads?
        Assert.assertEquals("Car c1 After Move 4 load should be E", "E", c1.getLoadName());
        Assert.assertEquals("Car c2 After Move 4 load should be E", "E", c2.getLoadName());
        Assert.assertEquals("Car c3 After Move 4 load should be E", "E", c3.getLoadName());
        Assert.assertEquals("Car c4 After Move 4 load should be L", "L", c4.getLoadName());
        Assert.assertEquals("Car c5 After Move 4 load should be E", "E", c5.getLoadName());
        Assert.assertEquals("Car c6 After Move 4 load should be E", "E", c6.getLoadName());
        Assert.assertEquals("Car c7 After Move 4 load should be E", "E", c7.getLoadName());
        Assert.assertEquals("Car c8 After Move 4 load should be E", "E", c8.getLoadName());
        Assert.assertEquals("Car c9 After Move 4 load should be E", "E", c9.getLoadName());

        // check car move counts
        Assert.assertEquals("Car c1 Move count", 20, c1.getMoves());
        Assert.assertEquals("Car c2 Move count", 18, c2.getMoves());
        Assert.assertEquals("Car c3 Move count", 3, c3.getMoves());
        Assert.assertEquals("Car c4 Move count", 9, c4.getMoves());
        Assert.assertEquals("Car c5 Move count", 4, c5.getMoves());
        Assert.assertEquals("Car c6 Move count", 6, c6.getMoves());
        Assert.assertEquals("Car c7 Move count", 21, c7.getMoves());
        Assert.assertEquals("Car c8 Move count", 5, c8.getMoves());
        Assert.assertEquals("Car c9 Move count", 21, c9.getMoves());

        train1.move();
        Assert.assertEquals("Train 1 After 4th build Status", Train.TERMINATED, getTrainStatus(train1));
        Assert.assertEquals("Train 1 After 4th build Status", Train.CODE_TERMINATED, train1.getStatusCode());

        // test siding to siding
        train1.setRoute(rte1);

        // bias track selection to sidings
        loc1trk3.setMoves(2);
        loc1trk4.setMoves(2);
        train1.reset();
        train1.reset();
        new TrainBuilder().build(train1);

        Assert.assertTrue("local testing siding to siding", train1.isBuilt());
        Assert.assertEquals("car UP 4 at interchange, destination Westford Siding 3", loc1trk3, c4
                .getDestinationTrack());
        Assert.assertEquals("car CP 2 at siding, destination Westford Yard 1", loc1trk1, c2
                .getDestinationTrack());

        // bias track selection to interchanges
        loc1trk3.setMoves(12);
        loc1trk4.setMoves(12);
        loc1trk5.setMoves(2);
        loc1trk6.setMoves(2);
        train1.reset();
        new TrainBuilder().build(train1);

        Assert.assertTrue("local testing siding to siding", train1.isBuilt());
        Assert.assertEquals("car UP 4 at interchange, destination", "Westford Yard 2", c4
                .getDestinationTrackName());
        Assert.assertEquals("car CP 2 at siding, destination", "Westford Interchange 5", c2
                .getDestinationTrackName());

        // set CP 2 destination, currently at Westford, Westford Siding 3
        train1.reset(); // release CP2 from train so we can set the car's destination
        c2.setDestination(loc1, null);
        loc1trk3.setMoves(1); // bias to same track
        train1.reset();
        new TrainBuilder().build(train1);

        Assert.assertTrue("local testing siding to siding", train1.isBuilt());
        Assert.assertEquals("car UP 4 at interchange, destination", "Westford Siding 3", c4
                .getDestinationTrackName());
        Assert.assertEquals("car CP 2 at siding, destination", "Westford Interchange 6", c2
                .getDestinationTrackName());

        // CP 2 is at Westford Siding 3, set destination to be the same
        train1.reset();
        c2.setDestination(loc1, loc1trk3);
        new TrainBuilder().build(train1);

        Assert.assertTrue("local testing siding to siding", train1.isBuilt());
        Assert.assertEquals("car UP 4 at interchange, destination", "Westford Siding 3", c4
                .getDestinationTrackName());
        Assert.assertEquals("car CP 2 at siding, destination", "Westford Siding 3", c2
                .getDestinationTrackName());

        train1.move();
        Assert.assertEquals("Train 1 terminated", Train.TERMINATED, getTrainStatus(train1));
        Assert.assertEquals("Train 1 terminated", Train.CODE_TERMINATED, train1.getStatusCode());

    }

    @Test
    public void testScheduleLoads() {
        String roadNames[] = Bundle.getMessage("carRoadNames").split(",");
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        ct.addName(carTypes[4]);
        ct.addName(carTypes[3]);
        ct.addName(carTypes[2]);
        ct.addName(carTypes[1]);

        // create schedules
        Schedule sch1 = smanager.newSchedule("Schedule 1");
        ScheduleItem sch1Item1 = sch1.addItem(carTypes[1]);
        // request a UP Boxcar
        sch1Item1.setRoadName(roadNames[1]);
        ScheduleItem sch1Item2 = sch1.addItem(carTypes[2]);
        // request an empty car and load it with Scrap
        sch1Item2.setReceiveLoadName("E");
        sch1Item2.setShipLoadName("Scrap");
        ScheduleItem sch1Item3 = sch1.addItem(carTypes[3]);
        // request a loaded car and load it with Tin
        sch1Item3.setReceiveLoadName("L");
        sch1Item3.setShipLoadName("Tin");
        InstanceManager.getDefault(CarLoads.class).addName(carTypes[3], "Tin"); // Allows c13 which is part of a kernel to get a new load

        Schedule sch2 = smanager.newSchedule("Schedule 2");
        ScheduleItem sch2Item1 = sch2.addItem(carTypes[4]);
        sch2Item1.setCount(2);
        sch2.addItem(carTypes[1]);

        // Create locations used
        Location loc1;
        loc1 = lmanager.newLocation("New Westford");
        loc1.setTrainDirections(DIRECTION_ALL);
        //		loc1.addTypeName(carTypes[2]);

        Location loc2;
        loc2 = lmanager.newLocation("New Chelmsford");
        loc2.setTrainDirections(DIRECTION_ALL);

        Location loc3;
        loc3 = lmanager.newLocation("New Bedford");
        loc3.setTrainDirections(DIRECTION_ALL);

        Track loc1trk1;
        loc1trk1 = loc1.addTrack("Westford Yard 1", Track.YARD);
        loc1trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc1trk1.setLength(900);
        //		loc1trk1.addTypeName(carTypes[2]);

        Track loc1trk2;
        loc1trk2 = loc1.addTrack("Westford Yard 2", Track.YARD);
        loc1trk2.setTrainDirections(Track.WEST + Track.EAST);
        loc1trk2.setLength(500);
        loc1trk2.deleteTypeName(carTypes[4]);

        Track loc1trk3;
        loc1trk3 = loc1.addTrack("Westford Express 3", Track.SPUR);
        loc1trk3.setTrainDirections(Track.WEST + Track.EAST);
        loc1trk3.setLength(300);
        loc1trk3.deleteTypeName(carTypes[3]);
        loc1trk3.deleteTypeName(carTypes[4]);

        Track loc1trk4;
        loc1trk4 = loc1.addTrack("Westford Express 4", Track.SPUR);
        loc1trk4.setTrainDirections(Track.WEST + Track.EAST);
        loc1trk4.setLength(300);
        loc1trk4.deleteTypeName(carTypes[3]);
        loc1trk4.deleteTypeName(carTypes[4]);

        Track loc2trk1;
        loc2trk1 = loc2.addTrack("Chelmsford Freight 1", Track.SPUR);
        loc2trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk1.setLength(900);
        loc2trk1.deleteTypeName(carTypes[4]);
        loc2trk1.setScheduleId(sch1.getId());
        loc2trk1.setScheduleMode(Track.SEQUENTIAL);
        // start the schedule with 2nd item Flat Car
        loc2trk1.setScheduleItemId(sch1.getItemsBySequenceList().get(1).getId());

        Track loc2trk2;
        loc2trk2 = loc2.addTrack("Chelmsford Freight 2", Track.SPUR);
        loc2trk2.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk2.setLength(900);
        loc2trk2.deleteTypeName(carTypes[4]);
        loc2trk2.setScheduleId(sch1.getId());
        loc2trk2.setScheduleMode(Track.SEQUENTIAL);
        // start the schedule with 3rd item Gon
        loc2trk2.setScheduleItemId(sch1.getItemsBySequenceList().get(2).getId());

        Track loc2trk3;
        loc2trk3 = loc2.addTrack("Chelmsford Yard 3", Track.YARD);
        loc2trk3.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk3.setLength(900);
        loc2trk3.deleteTypeName(carTypes[3]);
        loc2trk3.deleteTypeName(carTypes[4]);

        Track loc2trk4;
        loc2trk4 = loc2.addTrack("Chelmsford Freight 4", Track.SPUR);
        loc2trk4.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk4.setLength(900);
        loc2trk4.setScheduleId(sch2.getId());
        loc2trk4.setScheduleMode(Track.SEQUENTIAL);

        Track loc3trk1;
        loc3trk1 = loc3.addTrack("Bedford Yard 1", Track.STAGING);
        loc3trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc3trk1.setLength(900);
        loc3trk1.setRemoveCustomLoadsEnabled(true);

        // Create route with 2 location
        Route rte1;
        rte1 = rmanager.newRoute("Two Location Route");
        RouteLocation rl1 = rte1.addLocation(loc1);
        rl1.setTrainDirection(RouteLocation.EAST);
        rl1.setMaxCarMoves(12);
        rl1.setTrainIconX(25); // set the train icon coordinates
        rl1.setTrainIconY(75);

        RouteLocation rl2 = rte1.addLocation(loc2);
        rl2.setTrainDirection(RouteLocation.EAST);
        rl2.setMaxCarMoves(12);
        rl2.setTrainIconX(75); // set the train icon coordinates
        rl2.setTrainIconY(75);

        // Create train
        Train train1;
        train1 = tmanager.newTrain("TestScheduleLoads");
        train1.setRoute(rte1);

        // Set up 13 cars
        Car c1 = new Car(roadNames[4], "S1");
        c1.setTypeName(carTypes[3]);
        c1.setLength("90");
        c1.setMoves(13);
        c1.setLoadName("L");
        cmanager.register(c1);

        Car c2 = new Car(roadNames[1], "S2");
        c2.setTypeName(carTypes[1]);
        c2.setLength("80");
        c2.setMoves(12);
        cmanager.register(c2);

        Car c3 = new Car(roadNames[3], "S3");
        c3.setTypeName(carTypes[2]);
        c3.setLength("70");
        c3.setMoves(0);
        c3.setLoadName("L");
        c3.setDestination(loc2, null); // force this car to Chelmsford
        cmanager.register(c3);

        Car c4 = new Car(roadNames[2], "S4");
        c4.setTypeName(carTypes[1]);
        c4.setLength("60");
        c4.setMoves(10);
        cmanager.register(c4);

        // place two cars in a kernel
        Kernel k1 = cmanager.newKernel("TwoCars");

        Car c5 = new Car(roadNames[1], "S5");
        c5.setTypeName(carTypes[3]);
        c5.setLength("50");
        c5.setMoves(9);
        c5.setLoadName("L");
        c5.setKernel(k1);
        cmanager.register(c5);

        Car c6 = new Car(roadNames[0], "S6");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(8);
        c6.setLoadName("L");
        cmanager.register(c6);

        Car c7 = new Car(roadNames[1], "S7");
        c7.setTypeName(carTypes[1]);
        c7.setLength("50");
        c7.setMoves(7);
        cmanager.register(c7);

        Car c8 = new Car(roadNames[3], "S8");
        c8.setTypeName(carTypes[3]);
        c8.setLength("60");
        c8.setMoves(6);
        cmanager.register(c8);

        Car c9 = new Car(roadNames[3], "S9");
        c9.setTypeName(carTypes[2]);
        c9.setLength("90");
        c9.setMoves(5);
        c9.setLoadName("E");
        cmanager.register(c9);

        Car c10 = new Car(roadNames[0], "S10");
        c10.setTypeName(carTypes[4]);
        c10.setLength("40");
        c10.setMoves(2);
        c10.setLoadName("L");
        cmanager.register(c10);

        Car c11 = new Car(roadNames[0], "S11");
        c11.setTypeName(carTypes[4]);
        c11.setLength("40");
        c11.setMoves(3);
        c11.setLoadName("Coils");
        cmanager.register(c11);

        Car c12 = new Car(roadNames[0], "S12");
        c12.setTypeName(carTypes[4]);
        c12.setLength("40");
        c12.setMoves(4);
        cmanager.register(c12);

        // place car in kernel with c5
        Car c13 = new Car(roadNames[1], "S13");
        c13.setTypeName(carTypes[3]);
        c13.setLength("50");
        c13.setMoves(1);
        c13.setLoadName("L");
        c13.setKernel(k1);
        cmanager.register(c13);

        // place the cars in the yards
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(loc1, loc1trk1));

        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(loc1, loc1trk2));

        Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c12", Track.OKAY, c12.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c13", Track.OKAY, c13.setLocation(loc1, loc1trk2));

        train1.reset();

        new TrainBuilder().build(train1);

        // Schedule sch1 should cause c2 to be delivered to Chelmsford Freight 2
        Assert.assertEquals("c2 destination", "Chelmsford Freight 2", c2.getDestinationTrackName());
        Assert.assertEquals("c2 next load", "", c2.getNextLoadName());
        // Schedule sch1 and sch2 should reject c3, to be delivered to Chelmsford Yard 3
        Assert.assertEquals("c3 destination", "Chelmsford Yard 3", c3.getDestinationTrackName());
        Assert.assertEquals("c3 next load", "", c3.getNextLoadName());
        Assert.assertEquals("c4 destination", "Chelmsford Yard 3", c4.getDestinationTrackName());
        // Schedule sch1 should cause c5 & c13 to be delivered to Chelmsford Freight 2
        Assert.assertEquals("c5 destination", "Chelmsford Freight 2", c5.getDestinationTrackName());
        Assert.assertEquals("c5 next load", "Tin", c5.getNextLoadName());
        Assert.assertEquals("c6 destination", "Chelmsford Yard 3", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination", "Chelmsford Freight 4", c7.getDestinationTrackName());
        Assert.assertEquals("c9 destination", "Chelmsford Freight 1", c9.getDestinationTrackName());
        Assert.assertEquals("c9 next load", "Scrap", c9.getNextLoadName());
        Assert.assertEquals("c10 destination", "Chelmsford Freight 4", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination", "Chelmsford Freight 4", c11.getDestinationTrackName());
        // C13 is part of kernel, load will flip between E and L
        Assert.assertEquals("c13 destination", "Chelmsford Freight 2", c13.getDestinationTrackName());
        Assert.assertEquals("c13 next load", "Tin", c13.getNextLoadName());

        // move and terminate train
        train1.move();
        train1.move();
        train1.move();

        Assert.assertEquals("c1 track", "Chelmsford Freight 1", c1.getTrackName());
        Assert.assertEquals("c1 load", "Tin", c1.getLoadName());
        Assert.assertEquals("c2 track", "Chelmsford Freight 2", c2.getTrackName());
        Assert.assertEquals("c2 load", "L", c2.getLoadName());
        Assert.assertEquals("c3 track", "Chelmsford Yard 3", c3.getTrackName());
        Assert.assertEquals("c3 load", "L", c3.getLoadName());
        Assert.assertEquals("c4 track", "Chelmsford Yard 3", c4.getTrackName());
        Assert.assertEquals("c4 load", "E", c4.getLoadName());
        Assert.assertEquals("c5 track", "Chelmsford Freight 2", c5.getTrackName());
        Assert.assertEquals("c5 load", "Tin", c5.getLoadName());
        Assert.assertEquals("c6 track", "Chelmsford Yard 3", c6.getTrackName());
        Assert.assertEquals("c6 load", "L", c6.getLoadName());
        Assert.assertEquals("c7 track", "Chelmsford Freight 4", c7.getTrackName());
        Assert.assertEquals("c7 load", "L", c7.getLoadName());
        Assert.assertEquals("c8 track", "Westford Yard 2", c8.getTrackName());
        Assert.assertEquals("c8 load", "E", c8.getLoadName());
        Assert.assertEquals("c9 track", "Chelmsford Freight 1", c9.getTrackName());
        Assert.assertEquals("c9 load", "Scrap", c9.getLoadName());
        Assert.assertEquals("c10 track", "Chelmsford Freight 4", c10.getTrackName());
        Assert.assertEquals("c10 load", "E", c10.getLoadName());
        Assert.assertEquals("c11 track", "Chelmsford Freight 4", c11.getTrackName());
        Assert.assertEquals("c11 load", "E", c11.getLoadName());
        Assert.assertEquals("c12 track", "Westford Yard 1", c12.getTrackName());
        Assert.assertEquals("c12 load", "E", c12.getLoadName());
        Assert.assertEquals("c13 track", "Chelmsford Freight 2", c13.getTrackName());
        Assert.assertEquals("c13 load", "Tin", c13.getLoadName());

        // create a route to staging to test remove schedule load
        // Create route with 2 location
        Route rte2;
        rte2 = rmanager.newRoute("Chelmsford to Staging");
        RouteLocation r2rl1 = rte2.addLocation(loc2);
        r2rl1.setTrainDirection(RouteLocation.EAST);
        r2rl1.setMaxCarMoves(12);
        r2rl1.setTrainIconX(125); // set the train icon coordinates
        r2rl1.setTrainIconY(75);
        RouteLocation r2rl3 = rte2.addLocation(loc3);
        r2rl3.setTrainDirection(RouteLocation.EAST);
        r2rl3.setMaxCarMoves(12);
        r2rl3.setTrainIconX(175); // set the train icon coordinates
        r2rl3.setTrainIconY(75);

        train1.setRoute(rte2);
        train1.setName("Chelmsford to Bedford");
        train1.reset();
        new TrainBuilder().build(train1);

        // move and terminate train
        train1.move();
        train1.move();
        train1.move();

        Assert.assertEquals("c1 track to staging", "Bedford Yard 1", c1.getTrackName());
        Assert.assertEquals("c1 load to staging", "E", c1.getLoadName());
        Assert.assertEquals("c2 track to staging", "Bedford Yard 1", c2.getTrackName());
        Assert.assertEquals("c2 load to staging", "L", c2.getLoadName());
        Assert.assertEquals("c3 track to staging", "Bedford Yard 1", c3.getTrackName());
        Assert.assertEquals("c3 load to staging", "L", c3.getLoadName());
        Assert.assertEquals("c4 track to staging", "Bedford Yard 1", c4.getTrackName());
        Assert.assertEquals("c4 load to staging", "E", c4.getLoadName());
        Assert.assertEquals("c5 track to staging", "Bedford Yard 1", c5.getTrackName());
        Assert.assertEquals("c5 load to staging", "E", c5.getLoadName());
        Assert.assertEquals("c6 track to staging", "Bedford Yard 1", c6.getTrackName());
        Assert.assertEquals("c6 load to staging", "L", c6.getLoadName());
        Assert.assertEquals("c7 track to staging", "Bedford Yard 1", c7.getTrackName());
        Assert.assertEquals("c7 load to staging", "L", c7.getLoadName());
        Assert.assertEquals("c8 track to staging", "Westford Yard 2", c8.getTrackName());
        Assert.assertEquals("c8 load to staging", "E", c8.getLoadName());
        Assert.assertEquals("c9 track to staging", "Bedford Yard 1", c9.getTrackName());
        Assert.assertEquals("c9 load to staging", "E", c9.getLoadName());
        Assert.assertEquals("c10 track to staging", "Bedford Yard 1", c10.getTrackName());
        Assert.assertEquals("c10 load to staging", "E", c10.getLoadName());
        Assert.assertEquals("c11 track to staging", "Bedford Yard 1", c11.getTrackName());
        Assert.assertEquals("c11 load to staging", "E", c11.getLoadName());
        Assert.assertEquals("c12 track to staging", "Westford Yard 1", c12.getTrackName());
        Assert.assertEquals("c12 load to staging", "E", c12.getLoadName());
        Assert.assertEquals("c13 track to staging", "Bedford Yard 1", c13.getTrackName());
        Assert.assertEquals("c13 load to staging", "E", c13.getLoadName());

        // create a route from staging to test generate schedule load
        // Create route with 3 locations
        Route rte3;
        rte3 = rmanager.newRoute("Staging to Chelmsford");
        RouteLocation r3rl1 = rte3.addLocation(loc3);
        r3rl1.setTrainDirection(RouteLocation.EAST);
        r3rl1.setMaxCarMoves(11); // there are 11 cars departing staging
        r3rl1.setTrainIconX(25); // set the train icon coordinates
        r3rl1.setTrainIconY(100);
        RouteLocation r3rl2 = rte3.addLocation(loc2);
        r3rl2.setTrainDirection(RouteLocation.EAST);
        r3rl2.setMaxCarMoves(12);
        RouteLocation r3rl3 = rte3.addLocation(loc1);
        r3rl3.setTrainDirection(RouteLocation.EAST);
        r3rl3.setMaxCarMoves(12);
        r3rl3.setTrainIconX(75); // set the train icon coordinates
        r3rl3.setTrainIconY(100);

        loc3trk1.setRemoveCustomLoadsEnabled(false);
        loc3trk1.setAddCustomLoadsEnabled(true); // generate schedule loads

        sch1Item1.setReceiveLoadName("Metal 1"); // request these loads from staging
        sch1Item2.setReceiveLoadName("Metal 2");
        sch1Item3.setReceiveLoadName("Metal 3");

        InstanceManager.getDefault(CarLoads.class).addName(carTypes[3], "Metal 3"); // Allows c13 which is part of a kernel to get a new load

        train1.setRoute(rte3);
        train1.setName("BCW");
        train1.reset();
        new TrainBuilder().build(train1);

        Assert.assertEquals("Train Bedford Chelmsford Westford build status", true, train1.isBuilt());
        Assert.assertEquals("c1 load from staging", "E", c1.getLoadName());
        Assert.assertEquals("c2 load from staging", "L", c2.getLoadName());
        Assert.assertEquals("c3 load from staging", "L", c3.getLoadName());
        Assert.assertEquals("c4 load from staging", "E", c4.getLoadName());
        Assert.assertEquals("c5 load from staging", "Metal 3", c5.getLoadName());
        Assert.assertEquals("c6 load from staging", "L", c6.getLoadName());
        Assert.assertEquals("c7 load from staging", "L", c7.getLoadName());
        Assert.assertEquals("c8 load from staging", "E", c8.getLoadName());
        Assert.assertEquals("c9 load from staging", "Metal 2", c9.getLoadName());
        Assert.assertEquals("c9 next load from staging", "Scrap", c9.getNextLoadName());
        Assert.assertEquals("c10 load from staging", "E", c10.getLoadName());
        Assert.assertEquals("c11 load from staging", "E", c11.getLoadName());
        Assert.assertEquals("c13 load from staging", "Metal 3", c13.getLoadName());

        // move and terminate train
        train1.move();
        train1.move();
        train1.move();
        train1.move();

        Assert.assertEquals("c1 track from staging terminated", "Westford Yard 1", c1.getTrackName());
        Assert.assertEquals("c1 load from staging terminated", "E", c1.getLoadName());
        Assert.assertEquals("c2 track from staging terminated", "Westford Yard 2", c2.getTrackName());
        Assert.assertEquals("c2 load from staging terminated", "L", c2.getLoadName());
        Assert.assertEquals("c3 track from staging terminated", "Westford Yard 1", c3.getTrackName());
        Assert.assertEquals("c3 load from staging terminated", "L", c3.getLoadName());
        Assert.assertEquals("c4 track from staging terminated", "Westford Express 4", c4.getTrackName());
        Assert.assertEquals("c4 load from staging terminated", "L", c4.getLoadName());
        Assert.assertEquals("c5 track from staging terminated", "Chelmsford Freight 2", c5.getTrackName());
        Assert.assertEquals("c5 load from staging terminated", "Tin", c5.getLoadName());
        Assert.assertEquals("c6 track from staging terminated", "Westford Express 3", c6.getTrackName());
        Assert.assertEquals("c6 load from staging terminated", "E", c6.getLoadName());
        Assert.assertEquals("c7 track from staging terminated", "Westford Yard 2", c7.getTrackName());
        Assert.assertEquals("c7 load from staging terminated", "L", c7.getLoadName());
        Assert.assertEquals("c8 track from staging terminated", "Westford Yard 2", c8.getTrackName());
        Assert.assertEquals("c8 load from staging terminated", "E", c8.getLoadName());
        Assert.assertEquals("c9 track from staging terminated", "Chelmsford Freight 2", c9.getTrackName());
        Assert.assertEquals("c9 load from staging terminated", "Scrap", c9.getLoadName());
        Assert.assertEquals("c10 track from staging terminated", "Chelmsford Freight 4", c10.getTrackName());
        Assert.assertEquals("c10 load from staging terminated", "L", c10.getLoadName());
        Assert.assertEquals("c11 track from staging terminated", "Westford Yard 1", c11.getTrackName());
        Assert.assertEquals("c11 load from staging terminated", "E", c11.getLoadName());
        Assert.assertEquals("c12 track from staging terminated", "Westford Yard 1", c12.getTrackName());
        Assert.assertEquals("c12 load from staging terminated", "E", c12.getLoadName());
        Assert.assertEquals("c13 track from staging terminated", "Chelmsford Freight 2", c13.getTrackName());
        Assert.assertEquals("c13 load from staging terminated", "Tin", c13.getLoadName());

    }

    @Test
    public void testInterchange() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        Setup.setMaxTrainLength(500);
        ct.addName(carTypes[4]);
        ct.addName(carTypes[3]);
        ct.addName(carTypes[2]);
        ct.addName(carTypes[6]);
        ct.addName(carTypes[1]);

        // confirm no locations
        Assert.assertEquals("number of locations", 0, lmanager.getNumberOfLocations());
        // Create locations used
        Location loc1;
        loc1 = lmanager.newLocation("Old Westford");
        loc1.setTrainDirections(DIRECTION_ALL);

        Location loc2;
        loc2 = lmanager.newLocation("Old Chelmsford");
        loc2.setTrainDirections(DIRECTION_ALL);

        Location loc3;
        loc3 = lmanager.newLocation("Old Bedford");
        loc3.setTrainDirections(DIRECTION_ALL);

        Track loc1trk1;
        loc1trk1 = loc1.addTrack("Westford Yard 1", Track.YARD);
        loc1trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc1trk1.setLength(900);

        Track loc1trk2;
        loc1trk2 = loc1.addTrack("Westford Yard 2", Track.YARD);
        loc1trk2.setTrainDirections(Track.WEST + Track.EAST);
        loc1trk2.setLength(500);
        loc1trk2.deleteTypeName(carTypes[3]);
        loc1trk2.deleteTypeName(carTypes[6]);

        Track loc2trk1;
        loc2trk1 = loc2.addTrack("Chelmsford Interchange 1", Track.INTERCHANGE);
        loc2trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk1.setLength(900);
        loc2trk1.deleteTypeName(carTypes[3]);
        loc2trk1.deleteTypeName(carTypes[6]);

        Track loc2trk2;
        loc2trk2 = loc2.addTrack("Chelmsford Interchange 2", Track.INTERCHANGE);
        loc2trk2.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk2.setLength(900);
        loc2trk2.deleteTypeName(carTypes[6]);

        Track loc2trk3;
        loc2trk3 = loc2.addTrack("Chelmsford Yard 3", Track.YARD);
        loc2trk3.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk3.setLength(900);
        loc2trk3.deleteTypeName(carTypes[4]);
        loc2trk3.deleteTypeName(carTypes[3]);
        loc2trk3.deleteTypeName(carTypes[6]);

        Track loc2trk4;
        loc2trk4 = loc2.addTrack("Chelmsford Freight 4", Track.SPUR);
        loc2trk4.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk4.setLength(900);
        loc2trk4.deleteTypeName(carTypes[4]);
        loc2trk4.deleteTypeName(carTypes[6]);

        loc2trk3.setMoves(20); // bias interchange tracks
        loc2trk4.setMoves(20);

        Track loc3trk1;
        loc3trk1 = loc3.addTrack("Bedford Yard 1", Track.YARD);
        loc3trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc3trk1.setLength(900);

        // Create route with 3 location
        Route rte1;
        rte1 = rmanager.newRoute("Route 1 East");
        RouteLocation r1l1 = rte1.addLocation(loc1);
        r1l1.setTrainDirection(RouteLocation.EAST);
        r1l1.setMaxCarMoves(4);
        r1l1.setTrainIconX(125); // set the train icon coordinates
        r1l1.setTrainIconY(100);
        RouteLocation r1l2 = rte1.addLocation(loc2);
        r1l2.setTrainDirection(RouteLocation.EAST);
        r1l2.setMaxCarMoves(3);
        r1l2.setTrainIconX(25); // set the train icon coordinates
        r1l2.setTrainIconY(125);
        RouteLocation r1l3 = rte1.addLocation(loc3);
        r1l3.setTrainDirection(RouteLocation.EAST);
        r1l3.setMaxCarMoves(3);
        r1l3.setTrainIconX(75); // set the train icon coordinates
        r1l3.setTrainIconY(125);

        // Create route with 3 location
        Route rte2;
        rte2 = rmanager.newRoute("Route 2 East");
        RouteLocation r2l1 = rte2.addLocation(loc1);
        r2l1.setTrainDirection(RouteLocation.EAST);
        r2l1.setMaxCarMoves(2);
        r2l1.setTrainIconX(125); // set the train icon coordinates
        r2l1.setTrainIconY(125);
        RouteLocation r2l2 = rte2.addLocation(loc2);
        r2l2.setTrainDirection(RouteLocation.EAST);
        r2l2.setMaxCarMoves(6);
        r2l2.setTrainIconX(175); // set the train icon coordinates
        r2l2.setTrainIconY(125);
        RouteLocation r2l3 = rte2.addLocation(loc3);
        r2l3.setTrainDirection(RouteLocation.EAST);
        r2l3.setMaxCarMoves(6);
        r2l3.setTrainIconX(25); // set the train icon coordinates
        r2l3.setTrainIconY(150);

        // Create trains
        Train train1;
        train1 = tmanager.newTrain("T1OWOB");
        train1.setRoute(rte1);

        Train train2;
        train2 = tmanager.newTrain("T2OWOB");
        train2.setRoute(rte1);

        Train train3;
        train3 = tmanager.newTrain("T3OWOB");
        train3.setRoute(rte1);

        // Set up 7 box cars and 2 flat cars
        Car c1 = new Car("BM", "Q1");
        c1.setTypeName(carTypes[4]);
        c1.setLength("100");
        c1.setMoves(100);
        c1.setLoadName("L");
        c1.setWeightTons("10");
        cmanager.register(c1);

        Car c2 = new Car("UP", "Q2");
        c2.setTypeName(carTypes[1]);
        c2.setLength("80");
        c2.setMoves(90);
        c2.setWeightTons("20");
        cmanager.register(c2);

        Car c3 = new Car("XP", "Q3");
        c3.setTypeName(carTypes[2]);
        c3.setLength("70");
        c3.setMoves(70);
        c3.setWeightTons("30");
        cmanager.register(c3);

        Car c4 = new Car("PU", "Q4");
        c4.setTypeName(carTypes[1]);
        c4.setLength("60");
        c4.setMoves(60);
        c4.setWeightTons("40");
        cmanager.register(c4);

        Car c5 = new Car("UP", "Q5");
        c5.setTypeName(carTypes[4]);
        c5.setLength("50");
        c5.setMoves(50);
        c5.setLoadName("L");
        c5.setWeightTons("50");
        cmanager.register(c5);

        Car c6 = new Car("CP", "Q6");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(40);
        c6.setLoadName("L");
        c6.setWeightTons("60");
        cmanager.register(c6);

        Car c7 = new Car("UP", "Q7");
        c7.setTypeName(carTypes[1]);
        c7.setLength("50");
        c7.setMoves(30);
        c7.setWeightTons("70");
        cmanager.register(c7);

        Car c8 = new Car("XP", "Q8");
        c8.setTypeName(carTypes[4]);
        c8.setLength("60");
        c8.setMoves(20);
        c8.setWeightTons("80");
        cmanager.register(c8);

        Car c9 = new Car("XP", "Q9");
        c9.setTypeName(carTypes[2]);
        c9.setLength("90");
        c9.setMoves(10);
        c9.setLoadName("L");
        c9.setWeightTons("90");
        cmanager.register(c9);

        Car c10 = new Car("CP", "Q10");
        c10.setTypeName(carTypes[3]);
        c10.setLength("40");
        c10.setMoves(8);
        c10.setLoadName("L");
        c10.setWeightTons("100");
        cmanager.register(c10);

        Car c11 = new Car("CP", "Q11");
        c11.setTypeName(carTypes[3]);
        c11.setLength("40");
        c11.setMoves(9);
        c11.setLoadName("Coils");
        c11.setWeightTons("110");
        cmanager.register(c11);

        Car c12 = new Car("CP", "Q12");
        c12.setTypeName(carTypes[3]);
        c12.setLength("40");
        c12.setMoves(10);
        c12.setWeightTons("120");
        cmanager.register(c12);

        Car c13 = new Car("CP", "Q13");
        c13.setTypeName(carTypes[6]);
        c13.setCaboose(true);
        c13.setLength("40");
        c13.setMoves(7);
        c13.setWeightTons("130");
        cmanager.register(c13);

        // place the cars in the yards
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(loc1, loc1trk1));

        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(loc1, loc1trk2));

        Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c12", Track.OKAY, c12.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c13", Track.OKAY, c13.setLocation(loc1, loc1trk1));

        train1.reset();
        new TrainBuilder().build(train1);
        train2.reset();
        new TrainBuilder().build(train2);

        // now check to where cars are going to be delivered
        Assert.assertEquals("c1 destination", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination", "", c4.getDestinationTrackName());

        Assert.assertEquals("c9 destination", "Chelmsford Interchange 1", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination", "Bedford Yard 1", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination", "Chelmsford Interchange 2", c11.getDestinationTrackName());
        Assert.assertEquals("c12 destination", "Bedford Yard 1", c12.getDestinationTrackName());

        Assert.assertEquals("c5 destination", "Chelmsford Interchange 2", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination", "Bedford Yard 1", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination", "Chelmsford Interchange 1", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination", "Bedford Yard 1", c8.getDestinationTrackName());

        // now check which trains
        Assert.assertEquals("c9 train", train1, c9.getTrain());
        Assert.assertEquals("c10 train", train1, c10.getTrain());
        Assert.assertEquals("c11 train", train1, c11.getTrain());
        Assert.assertEquals("c12 train", train1, c12.getTrain());

        Assert.assertEquals("c5 train", train2, c5.getTrain());
        Assert.assertEquals("c6 train", train2, c6.getTrain());
        Assert.assertEquals("c7 train", train2, c7.getTrain());
        Assert.assertEquals("c8 train", train2, c8.getTrain());

        // try restricting interchange 1 to train1 and interchange 2 to train2
        loc2trk1.setDropOption(Track.TRAINS);
        loc2trk1.addDropId(train1.getId());
        loc2trk2.setDropOption(Track.TRAINS);
        loc2trk2.addDropId(train2.getId());

        train1.reset();
        new TrainBuilder().build(train1);
        train2.reset();
        new TrainBuilder().build(train2);

        // now check to where cars are going to be delivered
        Assert.assertEquals("c1 destination", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination", "", c4.getDestinationTrackName());

        Assert.assertEquals("c9 destination 2", "Chelmsford Interchange 1", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 2", "Bedford Yard 1", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 2", "Bedford Yard 1", c11.getDestinationTrackName());
        Assert.assertEquals("c12 destination 2", "Chelmsford Freight 4", c12.getDestinationTrackName());

        Assert.assertEquals("c5 destination 2", "Chelmsford Interchange 2", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 2", "Bedford Yard 1", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 2", "Chelmsford Interchange 2", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 2", "Bedford Yard 1", c8.getDestinationTrackName());

        // now check which trains
        Assert.assertEquals("c9 train", train1, c9.getTrain());
        Assert.assertEquals("c10 train", train1, c10.getTrain());
        Assert.assertEquals("c11 train", train1, c11.getTrain());
        Assert.assertEquals("c12 train", train1, c12.getTrain());

        Assert.assertEquals("c5 train", train2, c5.getTrain());
        Assert.assertEquals("c6 train", train2, c6.getTrain());
        Assert.assertEquals("c7 train", train2, c7.getTrain());
        Assert.assertEquals("c8 train", train2, c8.getTrain());

        // move and terminate
        Assert.assertEquals("Check train 1 departure location name", "Old Westford", train1
                .getCurrentLocationName());
        Assert.assertEquals("Check train 1 departure location", r1l1, train1.getCurrentLocation());
        train1.move(); // #1
        Assert.assertEquals("Check train 1 location name", "Old Chelmsford", train1.getCurrentLocationName());
        Assert.assertEquals("Check train 1 location", r1l2, train1.getCurrentLocation());
        train1.move(); // #2
        Assert.assertEquals("Check train 1 location name", "Old Bedford", train1.getCurrentLocationName());
        Assert.assertEquals("Check train 1 location", r1l3, train1.getCurrentLocation());
        train1.move(); // #3 terminate
        Assert.assertEquals("Check train 1 location name", "", train1.getCurrentLocationName());
        Assert.assertEquals("Check train 1 location", null, train1.getCurrentLocation());

        Assert.assertEquals("Check train 2 departure location name", "Old Westford", train2
                .getCurrentLocationName());
        Assert.assertEquals("Check train 2 departure location", r1l1, train2.getCurrentLocation());
        train2.move(); // #1
        Assert.assertEquals("Check train 2 location name", "Old Chelmsford", train2.getCurrentLocationName());
        Assert.assertEquals("Check train 2 location", r1l2, train2.getCurrentLocation());
        train2.move(); // #2
        Assert.assertEquals("Check train 2 location name", "Old Bedford", train2.getCurrentLocationName());
        Assert.assertEquals("Check train 2 location", r1l3, train2.getCurrentLocation());
        train2.move(); // #3 terminate
        Assert.assertEquals("Check train 2 location name", "", train2.getCurrentLocationName());
        Assert.assertEquals("Check train 2 location", null, train2.getCurrentLocation());

        r1l1.setMaxCarMoves(2);
        r1l2.setMaxCarMoves(6);
        r1l3.setMaxCarMoves(6);
        train3.reset();
        new TrainBuilder().build(train3); // note that train3 uses rte1, should not pickup cars at interchange

        Assert.assertEquals("c1 destination 3", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 3", "Chelmsford Yard 3", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 3", "Bedford Yard 1", c4.getDestinationTrackName());
        Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 3", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 3", "", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 3", "", c8.getDestinationTrackName());
        Assert.assertEquals("c9 destination 3", "", c9.getDestinationTrackName());
        Assert.assertEquals("c12 destination 3", "Bedford Yard 1", c12.getDestinationTrackName());

        // Change the route to 2, should be able to pickup c4, c7, c9
        train3.reset();
        train3.setRoute(rte2);
        new TrainBuilder().build(train3);

        Assert.assertEquals("c1 destination 4", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 4", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 4", "Chelmsford Yard 3", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 4", "Bedford Yard 1", c4.getDestinationTrackName());
        Assert.assertEquals("c5 destination 4", "Bedford Yard 1", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 4", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 4", "Bedford Yard 1", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 4", "", c8.getDestinationTrackName());
        Assert.assertEquals("c9 destination 4", "Bedford Yard 1", c9.getDestinationTrackName());
        Assert.assertEquals("c12 destination 4", "Bedford Yard 1", c12.getDestinationTrackName());

        // Change back to route to 1, should be able to pickup c4, c7
        train3.reset();
        train3.setRoute(rte1);
        loc2trk2.setPickupOption(Track.TRAINS);
        loc2trk2.addPickupId(train3.getId());
        new TrainBuilder().build(train3);

        Assert.assertEquals("c1 destination 5", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 5", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 5", "Chelmsford Freight 4", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 5", "Bedford Yard 1", c4.getDestinationTrackName());
        Assert.assertEquals("c5 destination 5", "Bedford Yard 1", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 5", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 5", "Bedford Yard 1", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 5", "", c8.getDestinationTrackName());
        Assert.assertEquals("c9 destination 5", "", c9.getDestinationTrackName());
        Assert.assertEquals("c12 destination 5", "Bedford Yard 1", c12.getDestinationTrackName());

        // Change back to route to 1, should be able to pickup c4, c7, and c9
        train3.reset();
        train3.setRoute(rte1);
        loc2trk1.setPickupOption(Track.ROUTES);
        loc2trk1.addPickupId(rte1.getId());
        new TrainBuilder().build(train3);

        Assert.assertEquals("c1 destination 6", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 6", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 6", "Chelmsford Yard 3", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 6", "Bedford Yard 1", c4.getDestinationTrackName());
        Assert.assertEquals("c5 destination 6", "Bedford Yard 1", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 6", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 6", "Bedford Yard 1", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 6", "", c8.getDestinationTrackName());
        Assert.assertEquals("c9 destination 6", "Bedford Yard 1", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 6", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 6", "", c11.getDestinationTrackName());
        Assert.assertEquals("c12 destination 6", "Bedford Yard 1", c12.getDestinationTrackName());

        // now allow train 3 to drop
        train3.reset();
        loc2trk1.setDropOption(Track.ROUTES);
        loc2trk1.addDropId(rte1.getId());
        new TrainBuilder().build(train3);

        Assert.assertEquals("c1 destination 7", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 7", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 7", "Chelmsford Interchange 1", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 7", "Bedford Yard 1", c4.getDestinationTrackName());
        Assert.assertEquals("c5 destination 7", "Bedford Yard 1", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 7", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 7", "Bedford Yard 1", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 7", "", c8.getDestinationTrackName());
        Assert.assertEquals("c9 destination 7", "Bedford Yard 1", c9.getDestinationTrackName());
        Assert.assertEquals("c12 destination 7", "Bedford Yard 1", c12.getDestinationTrackName());

        // move and terminate
        train3.move();
        train3.move();
        train3.move();
        train3.move();

        // check tracks
        Assert.assertEquals("c1 track", "Westford Yard 1", c1.getTrackName());
        Assert.assertEquals("c2 track", "Westford Yard 1", c2.getTrackName());
        Assert.assertEquals("c3 track", "Chelmsford Interchange 1", c3.getTrackName());
        Assert.assertEquals("c4 track", "Bedford Yard 1", c4.getTrackName());
        Assert.assertEquals("c5 track", "Bedford Yard 1", c5.getTrackName());
        Assert.assertEquals("c6 track", "Bedford Yard 1", c6.getTrackName());
        Assert.assertEquals("c7 track", "Bedford Yard 1", c7.getTrackName());
        Assert.assertEquals("c8 track", "Bedford Yard 1", c8.getTrackName());
        Assert.assertEquals("c9 track", "Bedford Yard 1", c9.getTrackName());
        Assert.assertEquals("c10 track", "Bedford Yard 1", c10.getTrackName());
        Assert.assertEquals("c11 track", "Bedford Yard 1", c11.getTrackName());
        Assert.assertEquals("c12 track", "Bedford Yard 1", c12.getTrackName());

        // check train length and tonnage
        Assert.assertEquals("Depart Westford length", 138, r1l1.getTrainLength());
        Assert.assertEquals("Depart Old Chelmsford length", 310, r1l2.getTrainLength());

        // In train 2 cars, c3 E and c4 E car weight 20/3 + 50/3 = 23
        Assert.assertEquals("Depart Old Westford tonnage", 23, r1l1.getTrainWeight());
        // In train 5 cars, c4 E, c5 L, c7 E, c9 L, c12 L = 40/3 + 50 + 70/3 + 90 + 120 = 296
        Assert.assertEquals("Depart Old Chelmsford tonnage", 296, r1l2.getTrainWeight());

        // test route pickup and drop controls
        train3.setRequirements(Train.CABOOSE);
        r1l1.setPickUpAllowed(false);
        train3.reset();
        // c1, c2, and c13 at start of train's route
        // c3 at Old Chelmsford, second stop
        new TrainBuilder().build(train3);

        Assert.assertEquals("c1 destination 8", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 8", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 8", "Bedford Yard 1", c3.getDestinationTrackName());
        Assert.assertEquals("c13 destination 8", "Bedford Yard 1", c13.getDestinationTrackName());

        r1l1.setPickUpAllowed(true);
        r1l2.setPickUpAllowed(false);
        train3.reset();
        new TrainBuilder().build(train3);

        Assert.assertEquals("c1 destination 9", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 9", "Chelmsford Interchange 1", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 9", "", c3.getDestinationTrackName());
        Assert.assertEquals("c13 destination 9", "Bedford Yard 1", c13.getDestinationTrackName());

        r1l2.setPickUpAllowed(true);
        r1l2.setDropAllowed(false); // Old Chelmsford
        train3.reset();
        new TrainBuilder().build(train3);

        Assert.assertEquals("c1 destination 10", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 10", "Bedford Yard 1", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 10", "Bedford Yard 1", c3.getDestinationTrackName());
        Assert.assertEquals("c13 destination 10", "Bedford Yard 1", c13.getDestinationTrackName());

        // try forcing c2 to Chelmsford
        train3.reset();
        c2.setDestination(loc2, null);
        new TrainBuilder().build(train3);
        Assert.assertEquals("c2 destination Old Chelmsford", "", c2.getDestinationTrackName());

        // confirm that c2 isn't part of this train
        Assert.assertNull("c2 isn't assigned to a train", c2.getTrain());
        Assert.assertNull("c2 destination has been set to null", c2.getDestination());
        Assert.assertNull("c2 next destination should be null", c2.getFinalDestination());
        Assert.assertNull("c2 next destination track should be null", c2.getFinalDestinationTrack());

        // try without moves
        train3.reset();
        r1l2.setDropAllowed(true);
        r1l2.setMaxCarMoves(0);
        c2.setDestination(loc2, null);
        new TrainBuilder().build(train3);
        Assert.assertEquals("c2 destination Old Chelmsford, no moves", "", c2.getDestinationTrackName());

        c2.setDestination(null, null);
        r1l2.setMaxCarMoves(6);
        r1l3.setDropAllowed(false); // Should be able to drop off caboose
        train3.reset();
        new TrainBuilder().build(train3);

        Assert.assertEquals("c1 destination 11", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 11", "Chelmsford Interchange 1", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 11", "", c3.getDestinationTrackName());
        Assert.assertEquals("c13 destination 11", "Bedford Yard 1", c13.getDestinationTrackName());

        // test to see if FRED also get delivered
        train3.setRequirements(Train.FRED);
        c2.setFred(true);
        train3.reset();
        new TrainBuilder().build(train3);

        Assert.assertTrue("Train 3 built", train3.isBuilt());
        Assert.assertEquals("c1 destination 12", "Chelmsford Interchange 1", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 12", "Bedford Yard 1", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 12", "", c3.getDestinationTrackName());
        Assert.assertEquals("c13 destination 12", "", c13.getDestinationTrackName());

        // move and terminate
        train3.move();
        train3.move();
        train3.move();
        train3.move();

        Assert.assertEquals("c1 track 12", "Chelmsford Interchange 1", c1.getTrackName());
        Assert.assertEquals("c2 track 12", "Bedford Yard 1", c2.getTrackName());
        Assert.assertEquals("c3 track 12", "Chelmsford Interchange 1", c3.getTrackName());
        Assert.assertEquals("c13 track 12", "Westford Yard 1", c13.getTrackName());

        // test previous car delivered pickup interchange operation
        loc2trk1.setDropOption(Track.ANY);
        loc2trk1.setPickupOption(Track.TRAINS);
        loc2trk2.setDropOption(Track.ANY);
        loc2trk2.setPickupOption(Track.TRAINS);

        // Place car with FRED back at start of route
        Assert.assertEquals("Place c2 again", Track.OKAY, c2.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c3 at start of route", Track.OKAY, c3.setLocation(loc1, loc1trk2));

        train3.reset();
        new TrainBuilder().build(train3);
        Assert.assertTrue("train 3 should build", train3.isBuilt());
        Assert.assertEquals("car BM Q1 should not be part of train", null, c1.getTrain());
        Assert.assertEquals("car XP Q3 should be part of train", train3, c3.getTrain());

        // put some cars at start of Route
        Assert.assertEquals("Place c1 again", Track.OKAY, c1.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c2 again", Track.OKAY, c2.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c3 again", Track.OKAY, c3.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c4 again", Track.OKAY, c4.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c6 again", Track.OKAY, c6.setLocation(loc2, loc2trk1));
        Assert.assertEquals("Place c7 again", Track.OKAY, c7.setLocation(loc2, loc2trk2));

        r1l1.setMaxCarMoves(5);
        loc2trk1.setDropOption(Track.ROUTES);
        loc2trk1.setPickupOption(Track.ROUTES);
        loc2trk2.setDropOption(Track.ROUTES);
        loc2trk2.setPickupOption(Track.ROUTES);

        train3.reset();
        new TrainBuilder().build(train3);
        Assert.assertTrue("train 3 should build", train3.isBuilt());
        Assert.assertEquals("BM Q1 in train", null, c1.getTrain());
        Assert.assertEquals("UP Q2 in train", train3, c2.getTrain());
        Assert.assertEquals("XP Q3 in train", train3, c3.getTrain());
        Assert.assertEquals("PU Q4 in train", train3, c4.getTrain());
        Assert.assertEquals("UP Q5 in train", null, c5.getTrain());
        Assert.assertEquals("CP Q6 in train", null, c6.getTrain());
        Assert.assertEquals("UP Q7 in train", null, c7.getTrain());

        Assert.assertEquals("UP Q2 destination", "Bedford Yard 1", c2.getDestinationTrackName());
        Assert.assertEquals("XP Q3 destination", "Chelmsford Yard 3", c3.getDestinationTrackName());
        Assert.assertEquals("PU Q4 destination", "Chelmsford Freight 4", c4.getDestinationTrackName());
        Assert.assertEquals("UP Q5 destination", "", c5.getDestinationTrackName());

        // interchange testing done, now test replace car type and road
        Assert.assertTrue("loc1 should accept Boxcar", loc1.acceptsTypeName(carTypes[1]));
        Assert.assertTrue("loc2 should accept Boxcar", loc2.acceptsTypeName(carTypes[1]));

        // replace should modify locations and trains
        ct.replaceName(carTypes[1], "boxcar");

        Assert.assertFalse("loc1 should not accept Boxcar", loc1.acceptsTypeName(carTypes[1]));
        Assert.assertFalse("loc2 should not accept Boxcar", loc2.acceptsTypeName(carTypes[1]));
        Assert.assertFalse("track loc1trk1 should not accept Boxcar", loc1trk1.acceptsTypeName(carTypes[1]));
        Assert.assertFalse("track loc2trk1 should not accept Boxcar", loc2trk1.acceptsTypeName(carTypes[1]));
        Assert.assertFalse("train 1 should not accept Boxcar", train1.acceptsTypeName(carTypes[1]));
        Assert.assertFalse("train 2 should not accept Boxcar", train2.acceptsTypeName(carTypes[1]));
        Assert.assertFalse("train 3 should not accept Boxcar", train3.acceptsTypeName(carTypes[1]));

        Assert.assertTrue("loc1 should accept boxcar", loc1.acceptsTypeName("boxcar"));
        Assert.assertTrue("loc2 should accept boxcar", loc2.acceptsTypeName("boxcar"));
        Assert.assertTrue("track loc1trk1 should accept boxcar", loc1trk1.acceptsTypeName("boxcar"));
        Assert.assertTrue("track loc2trk1 should accept boxcar", loc2trk1.acceptsTypeName("boxcar"));
        Assert.assertTrue("train 1 should accept boxcar", train1.acceptsTypeName("boxcar"));
        Assert.assertTrue("train 2 should accept boxcar", train2.acceptsTypeName("boxcar"));
        Assert.assertTrue("train 3 should accept boxcar", train3.acceptsTypeName("boxcar"));

        ct.replaceName("boxcar", carTypes[1]);

        Assert.assertTrue("loc1 should accept Boxcar", loc1.acceptsTypeName(carTypes[1]));
        Assert.assertTrue("loc2 should accept Boxcar", loc2.acceptsTypeName(carTypes[1]));
        Assert.assertTrue("track loc1trk1 should accept Boxcar", loc1trk1.acceptsTypeName(carTypes[1]));
        Assert.assertTrue("track loc2trk1 should accept Boxcar", loc2trk1.acceptsTypeName(carTypes[1]));
        Assert.assertTrue("train 1 should accept Boxcar", train1.acceptsTypeName(carTypes[1]));
        Assert.assertTrue("train 2 should accept Boxcar", train2.acceptsTypeName(carTypes[1]));
        Assert.assertTrue("train 3 should accept Boxcar", train3.acceptsTypeName(carTypes[1]));

        // now test road name replace
        CarRoads cr = InstanceManager.getDefault(CarRoads.class);
        cr.addName("CP");

        loc1trk1.setRoadOption(Track.INCLUDE_ROADS);
        loc1trk1.addRoadName("CP");
        loc1trk1.addRoadName("PC");
        train1.setRoadOption(Train.INCLUDE_ROADS);
        train1.addRoadName("CP");
        train1.addRoadName("PC");
        train1.setCabooseRoad("CP");
        train1.setEngineRoad("CP");

        Assert.assertTrue("track loc1trk1 should accept road CP", loc1trk1.acceptsRoadName("CP"));
        Assert.assertTrue("track loc1trk1 should accept road PC", loc1trk1.acceptsRoadName("PC"));
        Assert.assertFalse("track loc1trk1 should Not accept road PC", loc1trk1.acceptsRoadName("UP"));
        Assert.assertTrue("Train 1 should accept road CP", train1.acceptsRoadName("CP"));
        Assert.assertTrue("Train 1 should accept road PC", train1.acceptsRoadName("PC"));
        Assert.assertFalse("Train 1 should Not accept road UP", train1.acceptsRoadName("UP"));
        Assert.assertEquals("Caboose road", "CP", train1.getCabooseRoad());
        Assert.assertEquals("Engine road", "CP", train1.getEngineRoad());

        cr.replaceName("CP", "UP");

        Assert.assertFalse("after replace track loc1trk1 should Not accept road CP", loc1trk1
                .acceptsRoadName("CP"));
        Assert.assertTrue("after replace track loc1trk1 should accept road PC", loc1trk1
                .acceptsRoadName("PC"));
        Assert.assertTrue("after replace track loc1trk1 should accept road PC", loc1trk1
                .acceptsRoadName("UP"));
        Assert.assertFalse("after replace Train 1 should Not accept road CP", train1.acceptsRoadName("CP"));
        Assert.assertTrue("after replace Train 1 should accept road PC", train1.acceptsRoadName("PC"));
        Assert.assertTrue("after replace Train 1 should accept road UP", train1.acceptsRoadName("UP"));
        Assert.assertEquals("Caboose road", "UP", train1.getCabooseRoad());
        Assert.assertEquals("Engine road", "UP", train1.getEngineRoad());

    }

    @Test
    public void testCaboose() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // register the car and engine types used
        ct.addName(carTypes[1]);
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName(carTypes[5]);
        et.addName("Diesel");

        // register the road names used
        cr.addName("UP");
        cr.addName("SP");
        cr.addName("NH");

        // place two engines in a consist
        Consist con1 = emanager.newConsist("C1");

        Engine e1 = emanager.newEngine("UP", "1");
        e1.setModel("GP40");
        e1.setConsist(con1);
        Engine e2 = emanager.newEngine("SP", "2");
        e2.setModel("GP40");
        e2.setConsist(con1);

        // Set up three cabooses and six box cars
        Car c1 = cmanager.newCar("UP", "1");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setMoves(10);
        c1.setCaboose(true);

        Car c2 = cmanager.newCar("SP", "2");
        c2.setTypeName(Bundle.getMessage("Caboose"));
        c2.setLength("30");
        c2.setMoves(5);
        c2.setCaboose(true);

        Car c3 = cmanager.newCar("NH", "3");
        c3.setTypeName(Bundle.getMessage("Caboose"));
        c3.setLength("33");
        c3.setCaboose(true);

        Car c4 = cmanager.newCar("UP", "4");
        c4.setTypeName(carTypes[1]);
        c4.setLength("40");
        c4.setMoves(16);
        c4.setFred(true);

        Car c5 = cmanager.newCar("SP", "5");
        c5.setTypeName(carTypes[1]);
        c5.setLength("40");
        c5.setMoves(8);
        c5.setFred(true);

        Car c6 = cmanager.newCar("NH", "6");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(2);
        c6.setFred(true);

        Car c7 = cmanager.newCar("UP", "7");
        c7.setTypeName(carTypes[5]);
        c7.setLength("40");
        c7.setMoves(5);

        Car c8 = cmanager.newCar("SP", "8");
        c8.setTypeName(carTypes[1]);
        c8.setLength("40");
        c8.setMoves(4);

        Car c9 = cmanager.newCar("NH", "9");
        c9.setTypeName(carTypes[1]);
        c9.setLength("40");
        c9.setMoves(3);

        Car c10 = cmanager.newCar("NH", "10");
        c10.setTypeName(carTypes[1]);
        c10.setLength("40");
        c10.setMoves(10);

        Car c11 = cmanager.newCar("SP", "11");
        c11.setTypeName(carTypes[1]);
        c11.setLength("40");
        c11.setMoves(3);

        // Create 3 locations
        Location loc1 = lmanager.newLocation("Harvard");

        Track loc1trk1 = loc1.addTrack("Harvard Yard", Track.YARD);
        loc1trk1.setLength(1000);

        Location loc2 = lmanager.newLocation("Arlington");

        Track loc2trk1 = loc2.addTrack("Arlington Yard", Track.YARD);
        loc2trk1.setLength(1000);

        Location loc3 = lmanager.newLocation("Boston");

        Track loc3trk1 = loc3.addTrack("Boston Yard", Track.YARD);
        loc3trk1.setLength(1000);
        loc3trk1.deleteTypeName("Diesel");

        Track loc3trk2 = loc3.addTrack("Boston Engine Yard", Track.YARD);
        loc3trk2.setLength(200);
        loc3trk2.deleteTypeName(carTypes[1]);
        loc3trk2.deleteTypeName(carTypes[5]);
        loc3trk2.deleteTypeName(Bundle.getMessage("Caboose"));

        // Create route with 3 location
        Route rte1 = rmanager.newRoute("Route 2 Boston");
        RouteLocation rl1 = rte1.addLocation(loc1);
        RouteLocation rl2 = rte1.addLocation(loc2);
        rl2.setTrainIconX(75); // set the train icon coordinates
        rl2.setTrainIconY(150);
        RouteLocation rl3 = rte1.addLocation(loc3);
        rl3.setTrainIconX(125); // set the train icon coordinates
        rl3.setTrainIconY(150);

        // Create train
        Train train1 = tmanager.newTrain("TestCaboose");
        train1.setRoute(rte1);

        // Place cars
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(loc1, loc1trk1));

        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(loc1, loc1trk1));

        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(loc2, loc2trk1));
        Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(loc2, loc2trk1));

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(loc1, loc1trk1));

        // no requirements, so no caboose or FRED or engines
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 1", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 1", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 1", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 1", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 1", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 1", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 1", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 1", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 1", "Arlington Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 1", "Boston Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 1", "Boston Yard", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 1", "Boston Yard", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 1", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 1", "", e2.getDestinationTrackName());

        // no engines, so the caboose with least moves should be used
        train1.setRequirements(Train.CABOOSE);
        // don't allow pickups at second location Arlington
        rl2.setPickUpAllowed(false);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 2", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 2", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 2", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 2", "Boston Yard", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 2", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 2", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 2", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 2", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 2", "Boston Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 2", "Arlington Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 2", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 2", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 2", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 2", "", e2.getDestinationTrackName());

        // there's a caboose c1 that matches lead engine
        train1.setNumberEngines("2");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 3", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 3", "Boston Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 3", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 3", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 3", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 3", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 3", "Boston Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 3", "Arlington Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 3", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 3", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 3", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 3", "Boston Engine Yard", e2.getDestinationTrackName());

        // now try to find a caboose with "similar" (hyphen feature) road name to engine
        c1.setRoadName("UP-1");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 3", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 3", "Boston Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 3", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 3", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 3", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 3", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 3", "Boston Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 3", "Arlington Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 3", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 3", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 3", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 3", "Boston Engine Yard", e2.getDestinationTrackName());
        c1.setRoadName("UP"); // done

        // should default to the caboose with the least moves
        e1.setRoadName("X");
        // allow pickups at Arlington
        rl2.setPickUpAllowed(true);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 4", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 4", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 4", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 4", "Boston Yard", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 4", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 4", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 4", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 4", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 4", "Boston Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 4", "Arlington Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 4", "Boston Yard", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 4", "Boston Yard", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 4", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 4", "Boston Engine Yard", e2.getDestinationTrackName());

        // don't allow drops at Boston, caboose and engines should still drop there
        rl3.setDropAllowed(false);
        // should not take NH caboose
        e1.setRoadName("NH");
        // now require a SP caboose
        train1.setCabooseRoad("SP");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 5", true, train1.isBuilt());
        // check destinations
        Assert.assertEquals("c1 destination 5", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 5", "Boston Yard", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 5", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 5", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 5", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 5", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 5", "Arlington Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 5", "Arlington Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 5", "Arlington Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 5", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 5", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 5", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 5", "Boston Engine Yard", e2.getDestinationTrackName());

        // allow drops at Boston
        rl3.setDropAllowed(true);
        // should take car with FRED and road SP
        train1.setRequirements(Train.FRED);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 6", true, train1.isBuilt());
        // check destinations
        Assert.assertEquals("c1 destination 6", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 6", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 6", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 6", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 6", "Boston Yard", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 6", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 6", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 6", "Boston Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 6", "Arlington Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 6", "Boston Yard", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 6", "Boston Yard", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 6", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 6", "Boston Engine Yard", e2.getDestinationTrackName());

        // should take car with FRED least number of moves
        train1.setCabooseRoad("");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 7", true, train1.isBuilt());
        // check destinations
        Assert.assertEquals("c1 destination 7", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 7", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 7", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 7", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 7", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 7", "Boston Yard", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 7", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 7", "Boston Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 7", "Arlington Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 7", "Boston Yard", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 7", "Boston Yard", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 7", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 7", "Boston Engine Yard", e2.getDestinationTrackName());

        // now exclude road NH, engine road is NH and should be rejected
        train1.addRoadName("NH");
        train1.setRoadOption(Train.EXCLUDE_ROADS);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 7a", false, train1.isBuilt());
        // now override by setting a road for the engine
        train1.setEngineRoad("NH");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 8", true, train1.isBuilt());
        // check destinations
        Assert.assertEquals("c1 destination 8", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 8", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 8", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 8", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 8", "Boston Yard", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 8", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 8", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 8", "Arlington Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 8", "", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 8", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 8", "Boston Yard", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 8", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 8", "Boston Engine Yard", e2.getDestinationTrackName());

        // now only include NH
        train1.setRoadOption(Train.INCLUDE_ROADS);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 9", true, train1.isBuilt());
        // check destinations
        Assert.assertEquals("c1 destination 9", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 9", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 9", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 9", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 9", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 9", "Boston Yard", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 9", "", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 9", "", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 9", "Arlington Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 9", "Boston Yard", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 9", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 9", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 9", "Boston Engine Yard", e2.getDestinationTrackName());

        // don't allow boxcar, car with FRED required, build should fail
        loc3.deleteTypeName(carTypes[1]);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 9a", false, train1.isBuilt());
        loc3.addTypeName(carTypes[1]);

        // add staging
        Track loc1trk2 = loc1.addTrack("Harvard Staging", Track.STAGING);
        loc1trk2.setLength(1000);
        // now depart staging, must take all cars in staging
        rl1.setMaxCarMoves(9); // there are nine cars departing staging
        // Place cars
        Assert.assertEquals("Move c1", Track.OKAY, c1.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Move c2", Track.OKAY, c2.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Move c3", Track.OKAY, c3.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Move c4", Track.OKAY, c4.setLocation(loc1, loc1trk2));

        Assert.assertEquals("Move c5", Track.OKAY, c5.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Move c6", Track.OKAY, c6.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Move c7", Track.OKAY, c7.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Move c8", Track.OKAY, c8.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Move c9", Track.OKAY, c9.setLocation(loc1, loc1trk2));

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(loc1, loc1trk2));

        // program requires only staging at any location, so we don't test with yard
        loc1.deleteTrack(loc1trk1);
        // All engines and cars in staging must move! Cabooses and cars with FRED to terminal
        train1.setNumberEngines("0");
        train1.reset();
        new TrainBuilder().build(train1);
        // train only accepted engine and cars with NH road therefore build should fail
        Assert.assertEquals("Train 1 After Build from staging", false, train1.isBuilt());
        // try again but now accept all roads
        train1.setRoadOption(Train.ALL_ROADS);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 10", true, train1.isBuilt());
        // check destinations
        Assert.assertEquals("c1 destination 10", "Boston Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 10", "Boston Yard", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 10", "Boston Yard", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 10", "Boston Yard", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 10", "Boston Yard", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 10", "Boston Yard", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 10", "Arlington Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 10", "Arlington Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 10", "Arlington Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 10", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 10", "Boston Yard", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 10", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 10", "Boston Engine Yard", e2.getDestinationTrackName());

        // exclude road NH
        train1.setRoadOption(Train.EXCLUDE_ROADS);
        train1.reset();
        new TrainBuilder().build(train1);
        // should fail since there are NH roads in staging
        Assert.assertEquals("Train 1 After Build 11", false, train1.isBuilt());

        // reduce Boston moves to 6, to force non caboose and FRED cars to Arlington
        rl3.setMaxCarMoves(6);
        train1.setRoadOption(Train.ALL_ROADS);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 12", true, train1.isBuilt());
        // check destinations
        Assert.assertEquals("c1 destination 12", "Boston Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 12", "Boston Yard", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 12", "Boston Yard", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 12", "Boston Yard", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 12", "Boston Yard", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 12", "Boston Yard", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 12", "Arlington Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 12", "Arlington Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 12", "Arlington Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 12", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 12", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 12", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 12", "Boston Engine Yard", e2.getDestinationTrackName());

        // Reduce Arlington to only two moves, this should cause train build to fail
        rl2.setMaxCarMoves(2);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 13", false, train1.isBuilt());

        // restore number of moves
        rl2.setMaxCarMoves(7);
        rl3.setMaxCarMoves(7);
        // don't allow drops at Boston
        rl3.setDropAllowed(false);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 14", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 14", "Boston Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 14", "Boston Yard", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 14", "Boston Yard", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 14", "Boston Yard", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 14", "Boston Yard", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 14", "Boston Yard", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 14", "Arlington Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 14", "Arlington Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 14", "Arlington Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 14", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 14", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 14", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 14", "Boston Engine Yard", e2.getDestinationTrackName());

        // Reduce Arlington to only two moves, this should cause train build to fail
        rl2.setMaxCarMoves(2);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 15", false, train1.isBuilt());

        // Don't allow cabooses at Boston, should cause build failure
        rl2.setMaxCarMoves(7);
        loc3.deleteTypeName(Bundle.getMessage("Caboose"));
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 16", false, train1.isBuilt());

        // Don't allow boxcars, should also cause build failure
        loc3.addTypeName(Bundle.getMessage("Caboose"));
        loc3.deleteTypeName(carTypes[1]);
        train1.setRequirements(Train.NO_CABOOSE_OR_FRED);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 17", false, train1.isBuilt());

        // allow the three road names we're testing
        loc3.addTypeName(carTypes[1]);
        loc3trk1.addRoadName("NH");
        loc3trk1.addRoadName("SP");
        loc3trk1.addRoadName("UP");
        loc3trk1.setRoadOption(Track.INCLUDE_ROADS);
        loc3trk2.addRoadName("NH");
        loc3trk2.addRoadName("SP");
        loc3trk2.addRoadName("UP");
        loc3trk2.setRoadOption(Track.INCLUDE_ROADS);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 After Build 18", train1.isBuilt());

        // now remove type Diesel, this should cause a failure
        loc3trk2.deleteTypeName("Diesel");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("Train 1 After Build 19", train1.isBuilt());

        // now restore type Diesel
        loc3trk2.addTypeName("Diesel");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 After Build 20", train1.isBuilt());

        // Set the track length too short missing one set of couplers
        loc3trk2.setLength(Integer.parseInt(e1.getLength()) + Integer.parseInt(e2.getLength()) + Engine.COUPLER);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 20.1", false, train1.isBuilt());

        // restore track length
        loc3trk2.setLength(Integer.parseInt(e1.getLength()) + Integer.parseInt(e2.getLength()) + 2 * Engine.COUPLER);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 20.2", true, train1.isBuilt());

        // change lead engine road name, should cause build failure since Boston only
        // accepts NH, SP, and UP.
        train1.setEngineRoad(""); // reset engine road requirements, was "NH"
        e1.setRoadName("X"); // was "NH"
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 21", false, train1.isBuilt());

        e1.setRoadName("UP");
        loc3trk1.deleteRoadName("NH"); // this test that a caboose fails
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 22", false, train1.isBuilt());

        loc3trk1.addRoadName("NH");
        c6.setRoadName("X"); // this test that a car with FRED fails
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 22", false, train1.isBuilt());

        loc3trk1.addRoadName("NH");
        c6.setRoadName("X"); // this test that a car with FRED fails
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 23", false, train1.isBuilt());

        loc3trk1.addRoadName("X");
        loc2trk1.deleteTypeName(carTypes[5]); // this test that an ordinary car must move
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 24", false, train1.isBuilt());

        loc2trk1.addTypeName(carTypes[5]); // restore
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 25", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 25", "Boston Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 25", "Boston Yard", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 25", "Boston Yard", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 25", "Boston Yard", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 25", "Boston Yard", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 25", "Boston Yard", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 25", "Arlington Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 25", "Arlington Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 25", "Arlington Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 25", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 25", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 25", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 25", "Boston Engine Yard", e2.getDestinationTrackName());

        train1.reset();
        // send caboose SP 2 from staging to track that will not service it
        loc3trk2.addTypeName(Bundle.getMessage("Caboose"));
        loc3trk2.setLength(200);
        c2.setDestination(loc3, loc3trk2);
        loc3trk2.deleteTypeName(Bundle.getMessage("Caboose"));
        new TrainBuilder().build(train1);

        Assert.assertEquals("Train 1 After Build with caboose bad destination", false, train1.isBuilt());
        c2.setDestination(null, null);
        train1.reset();
        new TrainBuilder().build(train1);

        train1.move();
        train1.move();
        train1.move();

        // check final locations
        Assert.assertEquals("c1 location 24", "Boston Yard", c1.getTrackName());
        Assert.assertEquals("c2 location 24", "Boston Yard", c2.getTrackName());
        Assert.assertEquals("c3 location 24", "Boston Yard", c3.getTrackName());
        Assert.assertEquals("c4 location 24", "Boston Yard", c4.getTrackName());

        Assert.assertEquals("c5 location 24", "Boston Yard", c5.getTrackName());
        Assert.assertEquals("c6 location 24", "Boston Yard", c6.getTrackName());
        Assert.assertEquals("c7 location 24", "Arlington Yard", c7.getTrackName());
        Assert.assertEquals("c8 location 24", "Arlington Yard", c8.getTrackName());

        Assert.assertEquals("c9 location 24", "Arlington Yard", c9.getTrackName());
        Assert.assertEquals("c10 location 24", "Arlington Yard", c10.getTrackName());
        Assert.assertEquals("c11 location 24", "Arlington Yard", c11.getTrackName());

        Assert.assertEquals("e1 location 24", "Boston Engine Yard", e1.getTrackName());
        Assert.assertEquals("e2 location 24", "Boston Engine Yard", e2.getTrackName());
    }

    @Test
    public void testTrainBuildOptions() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // register the car and engine types used
        ct.addName(carTypes[1]);
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName(carTypes[5]);
        et.addName("Diesel");

        // create 2 consists and a single engine for testing
        Consist con1 = emanager.newConsist("C1");

        Engine e1 = emanager.newEngine("UP", "1");
        e1.setModel("GP30");
        e1.setOwner("AT");
        e1.setBuilt("1957");
        e1.setConsist(con1);
        e1.setMoves(5);
        Engine e2 = emanager.newEngine("SP", "2");
        e2.setModel("GP30");
        e2.setOwner("AT");
        e2.setBuilt("1957");
        e2.setConsist(con1);
        e2.setMoves(5);

        // one engine
        Engine e3 = emanager.newEngine("SP", "3");
        e3.setModel("GP40");
        e3.setOwner("DAB");
        e3.setBuilt("1957");

        Consist con2 = emanager.newConsist("C2");

        Engine e4 = emanager.newEngine("UP", "10");
        e4.setModel("GP40");
        e4.setOwner("DAB");
        e4.setBuilt("1944");
        e4.setConsist(con2);
        e4.setMoves(20);
        Engine e5 = emanager.newEngine("SP", "20");
        e5.setModel("GP40");
        e5.setOwner("DAB");
        e5.setBuilt("1944");
        e5.setConsist(con2);
        e5.setMoves(20);

        // 3 engine consist
        Consist con3 = emanager.newConsist("C3");

        Engine e6 = emanager.newEngine("UP", "100");
        e6.setModel("GP40");
        e6.setOwner("DAB");
        e6.setBuilt("1944");
        e6.setConsist(con3);
        e6.setMoves(2);
        Engine e7 = emanager.newEngine("SP", "200");
        e7.setModel("GP40");
        e7.setOwner("DAB");
        e7.setBuilt("1944");
        e7.setConsist(con3);
        e7.setMoves(2);
        Engine e8 = emanager.newEngine("SP", "300");
        e8.setModel("GP40");
        e8.setOwner("DAB");
        e8.setBuilt("1944");
        e8.setConsist(con3);
        e8.setMoves(2);

        // Set up three cabooses and six box cars
        Car c1 = cmanager.newCar("PU", "1");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setMoves(10);
        c1.setOwner("AT");
        c1.setBuilt("1943");
        c1.setCaboose(true);

        Car c2 = cmanager.newCar("SP", "2");
        c2.setTypeName(Bundle.getMessage("Caboose"));
        c2.setLength("30");
        c2.setMoves(5);
        c2.setOwner("DAB");
        c2.setBuilt("1957");
        c2.setCaboose(true);

        Car c3 = cmanager.newCar("UP", "3");
        c3.setTypeName(Bundle.getMessage("Caboose"));
        c3.setLength("33");
        c3.setMoves(0);
        c3.setOwner("DAB");
        c3.setBuilt("1944");
        c3.setCaboose(true);

        Car c4 = cmanager.newCar("UP", "4");
        c4.setTypeName(carTypes[1]);
        c4.setLength("40");
        c4.setMoves(16);
        c4.setOwner("DAB");
        c4.setBuilt("1958");
        c4.setFred(true);

        Car c5 = cmanager.newCar("SP", "5");
        c5.setTypeName(carTypes[1]);
        c5.setLength("40");
        c5.setMoves(8);
        c5.setOwner("DAB");
        c5.setBuilt("1958");
        c5.setFred(true);

        Car c6 = cmanager.newCar("NH", "6");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(2);
        c6.setOwner("DAB");
        c6.setBuilt("1958");
        c6.setFred(true);

        Car c7 = cmanager.newCar("UP", "7");
        c7.setTypeName(carTypes[5]);
        c7.setLength("40");
        c7.setMoves(5);
        c7.setOwner("DAB");
        c7.setBuilt("1958");

        Car c8 = cmanager.newCar("SP", "8");
        c8.setTypeName(carTypes[1]);
        c8.setLength("40");
        c8.setMoves(4);
        c8.setOwner("DAB");
        c8.setBuilt("1958");

        Car c9 = cmanager.newCar("NH", "9");
        c9.setTypeName(carTypes[1]);
        c9.setLength("40");
        c9.setMoves(3);
        c9.setOwner("DAB");
        c9.setBuilt("1944");

        Car c10 = cmanager.newCar("NH", "10");
        c10.setTypeName(carTypes[1]);
        c10.setLength("40");
        c10.setMoves(10);
        c10.setOwner("DAB");
        c10.setBuilt("1958");

        Car c11 = cmanager.newCar("SP", "11");
        c11.setTypeName(carTypes[1]);
        c11.setLength("40");
        c11.setMoves(3);
        c11.setOwner("DAB");
        c11.setBuilt("1958");

        // Create 5 locations
        Location loc1 = lmanager.newLocation("Harvard");

        Track loc1trk1 = loc1.addTrack("Harvard Yard", Track.YARD);
        loc1trk1.setLength(1000);

        Track loc1trk2 = loc1.addTrack("Harvard Yard 2", Track.YARD);
        loc1trk2.setLength(1000);

        Location loc2 = lmanager.newLocation("Arlington");

        Track loc2trk1 = loc2.addTrack("Arlington Yard", Track.YARD);
        loc2trk1.setLength(1000);

        Location loc3 = lmanager.newLocation("Boston");

        Track loc3trk1 = loc3.addTrack("Boston Yard", Track.YARD);
        loc3trk1.setLength(1000);

        Track loc3trk2 = loc3.addTrack("Boston Yard 2", Track.YARD);
        loc3trk2.setLength(1000);

        Location loc4 = lmanager.newLocation("Chelmsford");

        Track loc4trk1 = loc4.addTrack("Chelmsford Yard", Track.YARD);
        loc4trk1.setLength(1000);

        Track loc4trk2 = loc4.addTrack("Chelmsford Yard 2", Track.YARD);
        loc4trk2.setLength(1000);

        Location loc5 = lmanager.newLocation("Westford");

        Track loc5trk1 = loc5.addTrack("Westford Yard", Track.YARD);
        loc5trk1.setLength(1000);

        Track loc5trk2 = loc5.addTrack("Westford Yard 2", Track.YARD);
        loc5trk2.setLength(1000);

        // Create route with 4 location
        Route rte1 = rmanager.newRoute("Route 2 Westford");
        rte1.addLocation(loc1);
        RouteLocation rl2 = rte1.addLocation(loc2);
        rl2.setTrainIconX(175); // set the train icon coordinates
        rl2.setTrainIconY(150);
        RouteLocation rl3 = rte1.addLocation(loc3);
        rl3.setTrainIconX(25); // set the train icon coordinates
        rl3.setTrainIconY(175);
        RouteLocation rl4 = rte1.addLocation(loc4);
        rl4.setTrainIconX(75); // set the train icon coordinates
        rl4.setTrainIconY(175);

        // don't allow pickup or drops at Arlington
        rl2.setDropAllowed(false);
        rl2.setPickUpAllowed(false);

        // Create train
        Train train1 = tmanager.newTrain("TestTrainBuildOptions");
        train1.setRoute(rte1);

        // train skips Boston
        train1.addTrainSkipsLocation(rl3.getId());

        // Place cars
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(loc2, loc2trk1));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(loc5, loc5trk1));
        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(loc1, loc1trk1));

        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(loc2, loc2trk1));
        Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(loc2, loc2trk1));

        // set c9 destination not part of train's route
        Assert.assertEquals("Destination c9", Track.OKAY, c9.setDestination(loc5, loc5trk1));

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e5", Track.OKAY, e5.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e6", Track.OKAY, e6.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e7", Track.OKAY, e7.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e8", Track.OKAY, e8.setLocation(loc1, loc1trk1));

        train1.setRequirements(Train.CABOOSE);
        train1.setNumberEngines("2");
        train1.setOwnerOption(Train.ALL_OWNERS);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 1", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 1", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 1", "Chelmsford Yard 2", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 1", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 1", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 1", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 1", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 1", "Chelmsford Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 1", "Chelmsford Yard 2", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 1", "Westford Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 1", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 1", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 1", "Chelmsford Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 1", "Chelmsford Yard", e2.getDestinationTrackName());
        Assert.assertEquals("e3 destination 1", "", e3.getDestinationTrackName());
        Assert.assertEquals("e4 destination 1", "", e4.getDestinationTrackName());
        Assert.assertEquals("e5 destination 1", "", e5.getDestinationTrackName());
        Assert.assertEquals("e6 destination 1", "", e6.getDestinationTrackName());
        Assert.assertEquals("e7 destination 1", "", e7.getDestinationTrackName());
        Assert.assertEquals("e8 destination 1", "", e8.getDestinationTrackName());

        // Allow c9 to be used
        // set c9 destination Chelmsford
        Assert.assertEquals("Destination c9", Track.OKAY, c9.setDestination(loc4, null));

        // check that train direction and track direction feature works properly
        loc1trk2.setTrainDirections(Location.SOUTH);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build test track direction", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 1a", "Chelmsford Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 1a", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 1a", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 1a", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 1a", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 1a", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 1a", "Chelmsford Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 1a", "Chelmsford Yard 2", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 1a", "Chelmsford Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 1a", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 1a", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 1a", "Chelmsford Yard 2", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 1a", "Chelmsford Yard 2", e2.getDestinationTrackName());
        Assert.assertEquals("e3 destination 1a", "", e3.getDestinationTrackName());
        Assert.assertEquals("e4 destination 1a", "", e4.getDestinationTrackName());
        Assert.assertEquals("e5 destination 1a", "", e5.getDestinationTrackName());
        Assert.assertEquals("e6 destination 1a", "", e6.getDestinationTrackName());
        Assert.assertEquals("e7 destination 1a", "", e7.getDestinationTrackName());
        Assert.assertEquals("e8 destination 1a", "", e8.getDestinationTrackName());

        loc1trk2.setTrainDirections(Location.NORTH);

        train1.addOwnerName("DAB");
        train1.setOwnerOption(Train.INCLUDE_OWNERS);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 2", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 2", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 2", "Chelmsford Yard", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 2", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 2", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 2", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 2", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 2", "Chelmsford Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 2", "Chelmsford Yard 2", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 2", "Chelmsford Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 2", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 2", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 2", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 2", "", e2.getDestinationTrackName());
        Assert.assertEquals("e3 destination 2", "", e3.getDestinationTrackName());
        Assert.assertEquals("e4 destination 2", "Chelmsford Yard 2", e4.getDestinationTrackName());
        Assert.assertEquals("e5 destination 2", "Chelmsford Yard 2", e5.getDestinationTrackName());
        Assert.assertEquals("e6 destination 2", "", e6.getDestinationTrackName());
        Assert.assertEquals("e7 destination 2", "", e7.getDestinationTrackName());
        Assert.assertEquals("e8 destination 2", "", e8.getDestinationTrackName());

        // test that build fails if Diesel type is not allowed
        train1.deleteTypeName("Diesel");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 3", false, train1.isBuilt());

        // restore type Diesel and allow all owners
        train1.addTypeName("Diesel");
        train1.setOwnerOption(Train.ALL_OWNERS);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 4", true, train1.isBuilt());

        // Only allow rolling stock built after 1956
        train1.setBuiltStartYear("1956");
        train1.reset();
        new TrainBuilder().build(train1);

        Assert.assertEquals("Train 1 After Build 5", true, train1.isBuilt());
        // check destinations
        Assert.assertEquals("c1 destination 5", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 5", "Chelmsford Yard", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 5", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 5", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 5", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 5", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 5", "Chelmsford Yard 2", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 5", "Chelmsford Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 5", "", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 5", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 5", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 5", "Chelmsford Yard 2", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 5", "Chelmsford Yard 2", e2.getDestinationTrackName());
        Assert.assertEquals("e3 destination 5", "", e3.getDestinationTrackName());
        Assert.assertEquals("e4 destination 5", "", e4.getDestinationTrackName());
        Assert.assertEquals("e5 destination 5", "", e5.getDestinationTrackName());
        Assert.assertEquals("e6 destination 5", "", e6.getDestinationTrackName());
        Assert.assertEquals("e7 destination 5", "", e7.getDestinationTrackName());
        Assert.assertEquals("e8 destination 5", "", e8.getDestinationTrackName());

        train1.reset();
    }

    @Test
    public void testCarBlockingFromStaging() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // create 5 locations with tracks
        Location harvard = lmanager.newLocation("Harvard");
        Track loc1trk1 = harvard.addTrack("Harvard Yard 1", Track.YARD);
        loc1trk1.setLength(1000);
        Track loc1trk2 = harvard.addTrack("Harvard Yard 2", Track.YARD);
        loc1trk2.setLength(1000);

        Location arlington = lmanager.newLocation("Arlington");
        Track loc2trk1 = arlington.addTrack("Arlington Siding", Track.YARD);
        loc2trk1.setLength(1000);

        Location boston = lmanager.newLocation("Boston");
        Track loc3trk1 = boston.addTrack("Boston Yard 1", Track.YARD);
        loc3trk1.setLength(1000);
        Track loc3trk2 = boston.addTrack("Boston Yard 2", Track.YARD);
        loc3trk2.setLength(1000);

        Location chelmsford = lmanager.newLocation("Chelmsford");
        Track loc4trk1 = chelmsford.addTrack("Chelmsford Yard 1", Track.YARD);
        loc4trk1.setLength(1000);
        Track loc4trk2 = chelmsford.addTrack("Chelmsford Yard 2", Track.YARD);
        loc4trk2.setLength(1000);

        // Staging and enable blocking
        Location westford = lmanager.newLocation("Westford Staging");
        westford.setLocationOps(Location.STAGING);
        Track loc5trk1 = westford.addTrack("Westford Staging", Track.STAGING);
        loc5trk1.setLength(1000);
        loc5trk1.setBlockCarsEnabled(true);

        // create two trains, one into staging, the other out of staging
        // Create route with 5 location
        Setup.setCarMoves(20); // set default to 20 moves per location

        Route rte1 = rmanager.newRoute("Route Harvard to Westford Staging");
        rte1.addLocation(harvard);
        RouteLocation rl2 = rte1.addLocation(arlington);
        rl2.setDropAllowed(false); // only do pulls for this test

        RouteLocation rl3 = rte1.addLocation(boston);
        rl3.setDropAllowed(false); // only do pulls for this test

        RouteLocation rl4 = rte1.addLocation(chelmsford);
        rl4.setDropAllowed(false); // only do pulls for this test

        rte1.addLocation(westford); // staging

        // Create train
        Train train1 = tmanager.newTrain("TestCarBlockingFromStaging");
        train1.setRoute(rte1);
        train1.setRequirements(Train.CABOOSE);

        // create and place cars
        Car c1 = cmanager.newCar("PU", "113");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setMoves(10);
        c1.setOwner("AT");
        c1.setBuilt("1943");
        c1.setCaboose(true);

        Car c2 = cmanager.newCar("SP", "123");
        c2.setTypeName(carTypes[1]);
        c2.setLength("30");
        c2.setMoves(5);
        c2.setOwner("DAB");
        c2.setBuilt("1957");

        Car c3 = cmanager.newCar("UP", "133");
        c3.setTypeName(carTypes[1]);
        c3.setLength("33");
        c3.setMoves(0);
        c3.setOwner("DAB");
        c3.setBuilt("1944");

        Car c4 = cmanager.newCar("UP", "143");
        c4.setTypeName(carTypes[1]);
        c4.setLength("40");
        c4.setMoves(16);
        c4.setOwner("DAB");
        c4.setBuilt("1958");

        Car c5 = cmanager.newCar("SP", "153");
        c5.setTypeName(carTypes[1]);
        c5.setLength("40");
        c5.setMoves(8);
        c5.setOwner("DAB");
        c5.setBuilt("1958");

        Car c6 = cmanager.newCar("NH", "163");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(2);
        c6.setOwner("DAB");
        c6.setBuilt("1958");

        Car c7 = cmanager.newCar("UP", "173");
        c7.setTypeName(carTypes[1]);
        c7.setLength("40");
        c7.setMoves(5);
        c7.setOwner("DAB");
        c7.setBuilt("1958");

        Car c8 = cmanager.newCar("SP", "183");
        c8.setTypeName(carTypes[1]);
        c8.setLength("40");
        c8.setMoves(4);
        c8.setOwner("DAB");
        c8.setBuilt("1958");

        Car c9 = cmanager.newCar("NH", "193");
        c9.setTypeName(carTypes[1]);
        c9.setLength("40");
        c9.setMoves(3);
        c9.setOwner("DAB");
        c9.setBuilt("1944");

        Car c10 = cmanager.newCar("NH", "1103");
        c10.setTypeName(carTypes[1]);
        c10.setLength("40");
        c10.setMoves(10);
        c10.setOwner("DAB");
        c10.setBuilt("1958");

        Car c11 = cmanager.newCar("SP", "1113");
        c11.setTypeName(carTypes[1]);
        c11.setLength("40");
        c11.setMoves(3);
        c11.setOwner("DAB");
        c11.setBuilt("1958");

        // Place cars
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(harvard, loc1trk2));
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(harvard, loc1trk2));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(harvard, loc1trk2));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(harvard, loc1trk1));

        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(boston, loc3trk1));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(boston, loc3trk2));

        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(chelmsford, loc4trk1));
        Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(chelmsford, loc4trk2));
        Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(chelmsford, loc4trk2));

        // check car's last known location id
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c1.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c2.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c3.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c4.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c5.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c6.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c7.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c8.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c9.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c10.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c11.getLastLocationId());

        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // terminate train into staging
        train1.terminate();

        // now check car's last known location id
        Assert.assertEquals("Car departed Harvard", harvard.getId(), c1.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", harvard.getId(), c2.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", harvard.getId(), c3.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", harvard.getId(), c4.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", harvard.getId(), c5.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", harvard.getId(), c6.getLastLocationId());

        Assert.assertEquals("Car departed Boston", boston.getId(), c7.getLastLocationId());
        Assert.assertEquals("Car departed Boston", boston.getId(), c8.getLastLocationId());

        Assert.assertEquals("Car departed Chelmsford", chelmsford.getId(), c9.getLastLocationId());
        Assert.assertEquals("Car departed Chelmsford", chelmsford.getId(), c10.getLastLocationId());
        Assert.assertEquals("Car departed Chelmsford", chelmsford.getId(), c11.getLastLocationId());

        // now build a train that departs staging
        Route rte2 = rmanager.newRoute("Route Westford Staging to Harvard");
        rte2.addLocation(westford); // staging

        RouteLocation rlArlington = rte2.addLocation(arlington);
        rlArlington.setMaxCarMoves(4);

        RouteLocation rlBoston = rte2.addLocation(boston);
        rlBoston.setMaxCarMoves(7); // largest block of cars goes here

        RouteLocation rlChelmsford = rte2.addLocation(chelmsford);
        rlChelmsford.setMaxCarMoves(2);

        RouteLocation rlHarvard = rte2.addLocation(harvard);
        rlHarvard.setMaxCarMoves(3);

        // Create train, there are 3 blocks of cars in staging that were picked up by train 1
        Train train2 = tmanager.newTrain("Westford Staging to Harvard");
        train2.setRoute(rte2);

        new TrainBuilder().build(train2);
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // car's last known location id doesn't change when building train
        Assert.assertEquals("Car departed Harvard", harvard.getId(), c1.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", harvard.getId(), c2.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", harvard.getId(), c3.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", harvard.getId(), c4.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", harvard.getId(), c5.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", harvard.getId(), c6.getLastLocationId());

        Assert.assertEquals("Car departed Boston", boston.getId(), c7.getLastLocationId());
        Assert.assertEquals("Car departed Boston", boston.getId(), c8.getLastLocationId());

        Assert.assertEquals("Car departed Chelmsford", chelmsford.getId(), c9.getLastLocationId());
        Assert.assertEquals("Car departed Chelmsford", chelmsford.getId(), c10.getLastLocationId());
        Assert.assertEquals("Car departed Chelmsford", chelmsford.getId(), c11.getLastLocationId());

        // now check to see if cars were blocked to the right locations
        Assert.assertEquals("Caboose must go to terminal", harvard, c1.getDestination());

        // largest block 5 cars
        Assert.assertEquals("Car departed Harvard destination Boston", boston, c2.getDestination());
        Assert.assertEquals("Car departed Harvard destination Boston", boston, c3.getDestination());
        Assert.assertEquals("Car departed Harvard destination Boston", boston, c4.getDestination());
        Assert.assertEquals("Car departed Harvard destination Boston", boston, c5.getDestination());
        Assert.assertEquals("Car departed Harvard destination Boston", boston, c6.getDestination());

        // smallest block, 2 cars
        Assert.assertEquals("Car departed Boston destination Harvard", harvard, c7.getDestination());
        Assert.assertEquals("Car departed Boston destination Harvard", harvard, c8.getDestination());

        // middle block, 3 cars
        Assert.assertEquals("Car departed Chelmsford destination Arlington", arlington, c9.getDestination());
        Assert.assertEquals("Car departed Chelmsford destination Arlington", arlington, c10.getDestination());
        Assert.assertEquals("Car departed Chelmsford destination Arlington", arlington, c11.getDestination());

        // now try the case where the largest block is greater than any requested moves for a location
        rlBoston.setMaxCarMoves(4);

        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // now check to see if cars were blocked to the right locations
        Assert.assertEquals("Caboose must go to terminal", harvard, c1.getDestination());

        // largest block 5 cars was broken up since they all couldn't go to the same location
        Assert.assertEquals("Car departed Harvard", harvard, c2.getDestination());
        Assert.assertEquals("Car departed Harvard", chelmsford, c3.getDestination());
        Assert.assertEquals("Car departed Harvard", arlington, c4.getDestination());
        Assert.assertEquals("Car departed Harvard", chelmsford, c5.getDestination());
        Assert.assertEquals("Car departed Harvard", harvard, c6.getDestination());

        // smallest block, 2 cars
        Assert.assertEquals("Car departed Boston destination Arlington", arlington, c7.getDestination());
        Assert.assertEquals("Car departed Boston destination Arlington", arlington, c8.getDestination());

        // middle block, now the largest that can move as a group, 3 cars
        Assert.assertEquals("Car departed Chelmsford destination Boston", boston, c9.getDestination());
        Assert.assertEquals("Car departed Chelmsford destination Boston", boston, c10.getDestination());
        Assert.assertEquals("Car departed Chelmsford destination Boston", boston, c11.getDestination());

        // now check car last destination out of staging
        train2.terminate();

        Assert.assertEquals("Car departed Harvard", westford.getId(), c1.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", westford.getId(), c2.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", westford.getId(), c3.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", westford.getId(), c4.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", westford.getId(), c5.getLastLocationId());
        Assert.assertEquals("Car departed Harvard", westford.getId(), c6.getLastLocationId());

        Assert.assertEquals("Car departed Boston", westford.getId(), c7.getLastLocationId());
        Assert.assertEquals("Car departed Boston", westford.getId(), c8.getLastLocationId());

        Assert.assertEquals("Car departed Chelmsford", westford.getId(), c9.getLastLocationId());
        Assert.assertEquals("Car departed Chelmsford", westford.getId(), c10.getLastLocationId());
        Assert.assertEquals("Car departed Chelmsford", westford.getId(), c11.getLastLocationId());

    }

    /**
     * The program allows up to two engine changes in a train's route.
     */
    @Test
    public void testEngineChanges() {

        et.addName("Diesel");

        // create 5 locations with tracks
        Location harvard = lmanager.newLocation("Harvard");
        Track loc1trk1 = harvard.addTrack("Harvard Yard 1", Track.YARD);
        loc1trk1.setLength(1000);
        Track loc1trk2 = harvard.addTrack("Harvard Yard 2", Track.YARD);
        loc1trk2.setLength(1000);

        Location arlington = lmanager.newLocation("Arlington");
        Track loc2trk1 = arlington.addTrack("Arlington Siding", Track.YARD);
        loc2trk1.setLength(1000);

        Location boston = lmanager.newLocation("Boston");
        Track loc3trk1 = boston.addTrack("Boston Yard 1", Track.YARD);
        loc3trk1.setLength(1000);
        Track loc3trk2 = boston.addTrack("Boston Yard 2", Track.YARD);
        loc3trk2.setLength(1000);

        Location chelmsford = lmanager.newLocation("Chelmsford");
        Track loc4trk1 = chelmsford.addTrack("Chelmsford Yard 1", Track.YARD);
        loc4trk1.setLength(1000);
        Track loc4trk2 = chelmsford.addTrack("Chelmsford Yard 2", Track.YARD);
        loc4trk2.setLength(1000);

        Location westford = lmanager.newLocation("Westford");
        Track loc5trk1 = westford.addTrack("Westford Yard", Track.YARD);
        loc5trk1.setLength(1000);

        // create a 2 engine consist for departure
        Consist con1 = emanager.newConsist("C1");

        Engine e1 = emanager.newEngine("UP", "1");
        e1.setModel("GP30");
        e1.setOwner("AT");
        e1.setBuilt("1957");
        e1.setConsist(con1);
        e1.setMoves(5);

        Engine e2 = emanager.newEngine("SP", "2");
        e2.setModel("GP30");
        e2.setOwner("AT");
        e2.setBuilt("1957");
        e2.setConsist(con1);
        e2.setMoves(5);

        // one engine
        Engine e3 = emanager.newEngine("SP", "3");
        e3.setModel("GP40");
        e3.setBuilt("1957");

        Engine e4 = emanager.newEngine("UP", "10");
        e4.setModel("GP40");
        e4.setBuilt("1944");
        e4.setMoves(20);

        Engine e5 = emanager.newEngine("SP", "20");
        e5.setModel("GP40");
        e5.setBuilt("1944");
        e5.setMoves(20);

        Engine e6 = emanager.newEngine("UP", "100");
        e6.setModel("GP40");
        e6.setBuilt("1944");
        e6.setMoves(2);

        Engine e7 = emanager.newEngine("SP", "200");
        e7.setModel("GP40");
        e7.setBuilt("1944");
        e7.setMoves(2);

        Engine e8 = emanager.newEngine("SP", "300");
        e8.setModel("GP40");
        e8.setBuilt("1944");
        e8.setMoves(20);

        Engine e9 = emanager.newEngine("SP", "400");
        e9.setModel("GP30");
        e9.setBuilt("1944");
        e9.setMoves(2);

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(harvard, loc1trk1));

        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(arlington, loc2trk1));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(arlington, loc2trk1));

        Assert.assertEquals("Place e5", Track.OKAY, e5.setLocation(chelmsford, loc4trk1));
        Assert.assertEquals("Place e6", Track.OKAY, e6.setLocation(chelmsford, loc4trk1));
        Assert.assertEquals("Place e7", Track.OKAY, e7.setLocation(chelmsford, loc4trk1));
        Assert.assertEquals("Place e8", Track.OKAY, e8.setLocation(chelmsford, loc4trk1));
        Assert.assertEquals("Place e9", Track.OKAY, e9.setLocation(chelmsford, loc4trk1));

        Route rte1 = rmanager.newRoute("Route Harvard to Westford");
        rte1.addLocation(harvard);
        RouteLocation rlArlington = rte1.addLocation(arlington);
        rte1.addLocation(boston);
        RouteLocation rlChelmsford = rte1.addLocation(chelmsford);
        rte1.addLocation(westford);

        // Create train
        Train train1 = tmanager.newTrain("TestEngineChanges");
        train1.setRoute(rte1);

        // depart with 2 engines
        train1.setBuildConsistEnabled(true);
        train1.setNumberEngines("2");

        // change out 2 engines with 1 engine at Arlington
        train1.setSecondLegOptions(Train.CHANGE_ENGINES);
        train1.setSecondLegNumberEngines("1");
        train1.setSecondLegStartLocation(rlArlington);
        train1.setSecondLegEngineRoad("UP");
        train1.setSecondLegEngineModel("GP40");

        // change out 1 engine with 3 "SP" engines at Chelmsford
        train1.setThirdLegOptions(Train.CHANGE_ENGINES);
        train1.setThirdLegNumberEngines("3");
        train1.setThirdLegStartLocation(rlChelmsford);
        train1.setThirdLegEngineRoad("SP");
        train1.setThirdLegEngineModel("GP40");

        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 assigned to train", arlington, e1.getDestination());
        Assert.assertEquals("e2 assigned to train", arlington, e2.getDestination());

        Assert.assertEquals("e3 not assigned to train due to road name", null, e3.getDestination());
        Assert.assertEquals("e4 assigned to train", chelmsford, e4.getDestination());

        Assert.assertEquals("e5 assigned to train", westford, e5.getDestination());
        Assert.assertEquals("e6 not assigned to train due to road name", null, e6.getDestination());
        Assert.assertEquals("e7 assigned to train", westford, e7.getDestination());
        Assert.assertEquals("e8 assigned to train", westford, e8.getDestination());
        Assert.assertEquals("e9 not assigned to train due to model type", null, e9.getDestination());

        // remove needed engine at Arlington
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(null, null));
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should not build", false, train1.isBuilt());

        // restore engine
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(arlington, loc2trk1));
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // remove needed engine at Chelmsford
        Assert.assertEquals("Place e8", Track.OKAY, e8.setLocation(null, null));
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should not build", false, train1.isBuilt());

        // restore engine
        Assert.assertEquals("Place e8", Track.OKAY, e8.setLocation(chelmsford, loc4trk1));
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

    }

    /**
     * The program allows up to two caboose changes in a train's route.
     */
    @Test
    public void testCabooseChanges() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        // test confirms the order cabooses are assigned to the train
        Setup.setBuildAggressive(true);

        // create 5 locations with tracks
        Location harvard = lmanager.newLocation("Harvard");
        Track loc1trk1 = harvard.addTrack("Harvard Yard 1", Track.YARD);
        loc1trk1.setLength(80);

        Location arlington = lmanager.newLocation("Arlington");
        Track loc2trk1 = arlington.addTrack("Arlington Siding", Track.YARD);
        loc2trk1.setLength(80);

        Location boston = lmanager.newLocation("Boston");
        Track loc3trk1 = boston.addTrack("Boston Yard 1", Track.YARD);
        loc3trk1.setLength(80);

        Location chelmsford = lmanager.newLocation("Chelmsford");
        Track loc4trk1 = chelmsford.addTrack("Chelmsford Yard 1", Track.YARD);
        loc4trk1.setLength(80);

        Location westford = lmanager.newLocation("Westford");
        Track loc5trk1 = westford.addTrack("Westford Yard", Track.YARD);
        loc5trk1.setLength(40);

        // create and place cabooses
        Car c1 = cmanager.newCar("ABC", "1");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setCaboose(true);
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(harvard, loc1trk1));

        Car c2 = cmanager.newCar("ABC", "2");
        c2.setTypeName(Bundle.getMessage("Caboose"));
        c2.setLength("32");
        c2.setCaboose(true);
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(arlington, loc2trk1));

        Car c3 = cmanager.newCar("XYZ", "3");
        c3.setTypeName(Bundle.getMessage("Caboose"));
        c3.setLength("32");
        c3.setCaboose(true);
        c2.setMoves(10);
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(arlington, loc2trk1));

        Car c4 = cmanager.newCar("ABC", "4");
        c4.setTypeName(Bundle.getMessage("Caboose"));
        c4.setLength("32");
        c4.setCaboose(true);
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(chelmsford, loc4trk1));

        Car c5 = cmanager.newCar("XYZ", "5");
        c5.setTypeName(Bundle.getMessage("Caboose"));
        c5.setLength("32");
        c5.setCaboose(true);
        c5.setMoves(10);
        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(chelmsford, loc4trk1));

        // car with FRED at departure
        Car f1 = cmanager.newCar("CBA", "1");
        f1.setTypeName(carTypes[1]);
        f1.setLength("32");
        f1.setFred(true);
        Assert.assertEquals("Place f1", Track.OKAY, f1.setLocation(harvard, loc1trk1));

        Route rte1 = rmanager.newRoute("Route Harvard to Westford");
        rte1.addLocation(harvard);
        RouteLocation rlArlington = rte1.addLocation(arlington);
        rte1.addLocation(boston);
        RouteLocation rlChelmsford = rte1.addLocation(chelmsford);
        rte1.addLocation(westford);

        // Create train
        Train train1 = tmanager.newTrain("TestCabooseChanges");
        train1.setRoute(rte1);

        // depart with caboose
        train1.setRequirements(Train.CABOOSE);

        // swap out caboose at Arlington
        train1.setSecondLegOptions(Train.ADD_CABOOSE);
        train1.setSecondLegStartLocation(rlArlington);
        train1.setSecondLegCabooseRoad("XYZ");

        // swap out caboose at Chelmsford
        train1.setThirdLegOptions(Train.ADD_CABOOSE);
        train1.setThirdLegStartLocation(rlChelmsford);
        train1.setThirdLegCabooseRoad("XYZ");

        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm caboose destinations
        Assert.assertEquals("Caboose is part of train", arlington, c1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c2.getDestination());
        Assert.assertEquals("Caboose is part of train", chelmsford, c3.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c4.getDestination());
        Assert.assertEquals("Caboose is part of train", westford, c5.getDestination());

        // now test failures by removing required cabooses
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(null, null));
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should not build", false, train1.isBuilt());

        train1.reset();
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(arlington, loc2trk1));
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        train1.reset();
        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(null, null));
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should not build", false, train1.isBuilt());

        train1.reset();
        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(chelmsford, loc4trk1));
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // now test removing caboose from train
        train1.setSecondLegOptions(Train.REMOVE_CABOOSE);

        // need room for 1st caboose at Arlington
        loc2trk1.setLength(150);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm caboose destinations
        Assert.assertEquals("Caboose is part of train", arlington, c1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c2.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c3.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c4.getDestination());
        Assert.assertEquals("Caboose is part of train", westford, c5.getDestination());

        // now depart without a caboose, add one, then remove it, and continue to destination
        train1.setRequirements(Train.NO_CABOOSE_OR_FRED);
        train1.setSecondLegOptions(Train.ADD_CABOOSE);
        train1.setThirdLegOptions(Train.REMOVE_CABOOSE);

        // need room for caboose at Chelmsford
        loc4trk1.setLength(150);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm caboose destinations
        Assert.assertEquals("Caboose is not part of train", null, c1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c2.getDestination());
        Assert.assertEquals("Caboose is part of train", chelmsford, c3.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c4.getDestination());
        Assert.assertEquals("Caboose is part of train", null, c5.getDestination());

        // try departing with FRED and swapping it for a caboose
        train1.setRequirements(Train.FRED);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm destinations
        Assert.assertEquals("Boxcar is part of train", arlington, f1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c2.getDestination());
        Assert.assertEquals("Caboose is part of train", chelmsford, c3.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c4.getDestination());
        Assert.assertEquals("Caboose is part of train", null, c5.getDestination());

    }

    @Test
    public void testAutoHP() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        Assert.assertEquals("confirm default of 1 HPT", 1, Setup.getHorsePowerPerTon());

        et.addName("");

        // create 5 locations with tracks
        Location harvard = lmanager.newLocation("Harvard");
        Track loc1trk1 = harvard.addTrack("Harvard Yard 1", Track.YARD);
        loc1trk1.setLength(1000);
        Track loc1trk2 = harvard.addTrack("Harvard Yard 2", Track.YARD);
        loc1trk2.setLength(1000);

        Location arlington = lmanager.newLocation("Arlington");
        Track loc2trk1 = arlington.addTrack("Arlington Siding", Track.YARD);
        loc2trk1.setLength(1000);

        Location boston = lmanager.newLocation("Boston");
        Track loc3trk1 = boston.addTrack("Boston Yard 1", Track.YARD);
        loc3trk1.setLength(1000);
        Track loc3trk2 = boston.addTrack("Boston Yard 2", Track.YARD);
        loc3trk2.setLength(1000);

        Location chelmsford = lmanager.newLocation("Chelmsford");
        Track loc4trk1 = chelmsford.addTrack("Chelmsford Yard 1", Track.YARD);
        loc4trk1.setLength(1000);
        Track loc4trk2 = chelmsford.addTrack("Chelmsford Yard 2", Track.YARD);
        loc4trk2.setLength(1000);

        Location westford = lmanager.newLocation("Westford");
        Track loc5trk1 = westford.addTrack("Westford Yard", Track.YARD);
        loc5trk1.setLength(1000);

        Engine e1 = emanager.newEngine("UP", "1");
        e1.setModel("GP30-200");
        e1.setHp("200");
        e1.setLength("50");
        e1.setWeightTons("100");
        e1.setMoves(20);

        Engine e2 = emanager.newEngine("SP", "2");
        e2.setModel("GP30-400");
        e2.setHp("400");
        e2.setLength("50");
        e2.setWeightTons("110");
        e2.setMoves(15);

        Engine e3 = emanager.newEngine("SP", "3");
        e3.setModel("GP40-800");
        e3.setHp("800");
        e3.setLength("50");
        e3.setWeightTons("120");
        e3.setMoves(10);

        Engine e4 = emanager.newEngine("UP", "10");
        e4.setModel("GP40-1600");
        e4.setHp("1600");
        e4.setLength("50");
        e4.setWeightTons("130");
        e4.setMoves(5);

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(harvard, loc1trk1));

        Route rte1 = rmanager.newRoute("Route Harvard to Westford");
        rte1.addLocation(harvard);
        RouteLocation rlArlington = rte1.addLocation(arlington);
        rte1.addLocation(boston);
        rte1.addLocation(chelmsford);
        rte1.addLocation(westford);

        // add grade to route
        rlArlington.setGrade(1.0);

        // Create train
        Train train1 = tmanager.newTrain("TestAutoHP");
        train1.setRoute(rte1);
        train1.setSendCarsToTerminalEnabled(true); // send all car pulls to terminal

        // use autoHP
        train1.setBuildConsistEnabled(true);
        train1.setNumberEngines(Train.AUTO_HPT);

        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 not assigned to train", null, e1.getDestination());
        Assert.assertEquals("e2 assigned to train, train only needs 400 HP", westford, e2.getDestination());
        Assert.assertEquals("e3 not assigned to train", null, e3.getDestination());
        Assert.assertEquals("e4 not assigned to train", null, e4.getDestination());

        // now increase the train's weight
        Car c1 = cmanager.newCar("UP", "43");
        c1.setTypeName(carTypes[1]);
        c1.setLength("40");
        c1.setWeightTons("400"); // 400 tons loaded
        c1.setLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultLoadName());
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(harvard, loc1trk1));

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 not assigned to train", null, e1.getDestination());
        Assert.assertEquals("e2 not assigned to train", null, e2.getDestination());
        Assert.assertEquals("e3 not assigned to train", null, e3.getDestination());
        Assert.assertEquals("e4 assigned to train, train needs 1600 HP", westford, e4.getDestination());

        // now increase the train's weight
        Car c2 = cmanager.newCar("UP", "123");
        c2.setTypeName(carTypes[1]);
        c2.setLength("40");
        c2.setWeightTons("200"); // 200 tons loaded
        c2.setLoadName(InstanceManager.getDefault(CarLoads.class).getDefaultLoadName());
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(arlington, loc2trk1));

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 not assigned to train", null, e1.getDestination());
        Assert.assertEquals("e2 not assigned to train", null, e2.getDestination());
        Assert.assertEquals("e3 assigned to train", westford, e3.getDestination());
        Assert.assertEquals("e4 assigned to train", westford, e4.getDestination());

    }

    @Test
    public void testAggressiveBuildOption() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        String engineTypes[] = Bundle.getMessage("engineDefaultTypes").split(",");
        Setup.setBuildAggressive(true);
        Setup.setStagingTrackImmediatelyAvail(false);

        // register the car and engine types used
        ct.addName(carTypes[1]);
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName(carTypes[5]);
        et.addName(engineTypes[2]);

        // create 2 consists and a single engine for testing
        Consist con1 = emanager.newConsist("C1");

        Engine e1 = emanager.newEngine("UP", "1");
        e1.setModel("GP30");
        e1.setOwner("AT");
        e1.setBuilt("1957");
        e1.setConsist(con1);
        e1.setMoves(5);
        Engine e2 = emanager.newEngine("SP", "2");
        e2.setModel("GP30");
        e2.setOwner("AT");
        e2.setBuilt("1957");
        e2.setConsist(con1);
        e2.setMoves(5);

        // one engine
        Engine e3 = emanager.newEngine("SP", "3");
        e3.setModel("GP40");
        e3.setOwner("DAB");
        e3.setBuilt("1957");

        Consist con2 = emanager.newConsist("C2");

        Engine e4 = emanager.newEngine("UP", "10");
        e4.setModel("GP40");
        e4.setOwner("DAB");
        e4.setBuilt("1944");
        e4.setConsist(con2);
        e4.setMoves(20);
        Engine e5 = emanager.newEngine("SP", "20");
        e5.setModel("GP40");
        e5.setOwner("DAB");
        e5.setBuilt("1944");
        e5.setConsist(con2);
        e5.setMoves(20);

        // 3 engine consist
        Consist con3 = emanager.newConsist("C3");

        Engine e6 = emanager.newEngine("UP", "100");
        e6.setModel("GP40");
        e6.setOwner("DAB");
        e6.setBuilt("1944");
        e6.setConsist(con3);
        e6.setMoves(2);
        Engine e7 = emanager.newEngine("SP", "200");
        e7.setModel("GP40");
        e7.setOwner("DAB");
        e7.setBuilt("1944");
        e7.setConsist(con3);
        e7.setMoves(2);
        Engine e8 = emanager.newEngine("SP", "300");
        e8.setModel("GP40");
        e8.setOwner("DAB");
        e8.setBuilt("1944");
        e8.setConsist(con3);
        e8.setMoves(2);

        // Set up cars
        Car c1 = cmanager.newCar("PU", "13");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setMoves(10);
        c1.setOwner("AT");
        c1.setBuilt("1943");
        c1.setCaboose(true);

        Car c2 = cmanager.newCar("SP", "23");
        c2.setTypeName(carTypes[1]);
        c2.setLength("30");
        c2.setMoves(5);
        c2.setOwner("DAB");
        c2.setBuilt("1957");

        Car c3 = cmanager.newCar("UP", "33");
        c3.setTypeName(carTypes[1]);
        c3.setLength("33");
        c3.setMoves(0);
        c3.setOwner("DAB");
        c3.setBuilt("1944");

        Car c4 = cmanager.newCar("UP", "43");
        c4.setTypeName(carTypes[1]);
        c4.setLength("40");
        c4.setMoves(16);
        c4.setOwner("DAB");
        c4.setBuilt("1958");

        Car c5 = cmanager.newCar("SP", "53");
        c5.setTypeName(carTypes[1]);
        c5.setLength("40");
        c5.setMoves(8);
        c5.setOwner("DAB");
        c5.setBuilt("1958");

        Car c6 = cmanager.newCar("NH", "63");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(2);
        c6.setOwner("DAB");
        c6.setBuilt("1958");

        Car c7 = cmanager.newCar("UP", "73");
        c7.setTypeName(carTypes[5]);
        c7.setLength("40");
        c7.setMoves(5);
        c7.setOwner("DAB");
        c7.setBuilt("1958");

        Car c8 = cmanager.newCar("SP", "83");
        c8.setTypeName(carTypes[1]);
        c8.setLength("40");
        c8.setMoves(4);
        c8.setOwner("DAB");
        c8.setBuilt("1958");

        Car c9 = cmanager.newCar("NH", "93");
        c9.setTypeName(carTypes[1]);
        c9.setLength("40");
        c9.setMoves(3);
        c9.setOwner("DAB");
        c9.setBuilt("1944");

        Car c10 = cmanager.newCar("NH", "103");
        c10.setTypeName(carTypes[1]);
        c10.setLength("40");
        c10.setMoves(10);
        c10.setOwner("DAB");
        c10.setBuilt("1958");

        Car c11 = cmanager.newCar("SP", "113");
        c11.setTypeName(carTypes[1]);
        c11.setLength("40");
        c11.setMoves(3);
        c11.setOwner("DAB");
        c11.setBuilt("1958");

        // Create 5 locations
        Location loc1 = lmanager.newLocation("New Harvard");

        Track loc1trk1 = loc1.addTrack("Harvard Yard 1", Track.YARD);
        loc1trk1.setLength(1000);

        Track loc1trk2 = loc1.addTrack("Harvard Yard 2", Track.YARD);
        loc1trk2.setLength(1000);

        Location loc2 = lmanager.newLocation("New Arlington");

        Track loc2trk1 = loc2.addTrack("Arlington Siding", Track.SPUR);
        loc2trk1.setLength(50);

        Location loc3 = lmanager.newLocation("New Boston");

        Track loc3trk1 = loc3.addTrack("Boston Yard 1", Track.YARD);
        loc3trk1.setLength(50);

        Track loc3trk2 = loc3.addTrack("Boston Yard 2", Track.YARD);
        loc3trk2.setLength(50);

        Location loc4 = lmanager.newLocation("New Chelmsford");

        Track loc4trk1 = loc4.addTrack("Chelmsford Yard 1", Track.YARD);
        loc4trk1.setLength(50);

        Track loc4trk2 = loc4.addTrack("Chelmsford Yard 2", Track.YARD);
        loc4trk2.setLength(50);

        Location loc5 = lmanager.newLocation("New Westford");

        Track loc5trk1 = loc5.addTrack("Westford Yard 1", Track.YARD);
        loc5trk1.setLength(1000);

        Track loc5trk2 = loc5.addTrack("Westford Yard 2", Track.YARD);
        loc5trk2.setLength(1000);

        // Create route with 5 location
        Route rte1 = rmanager.newRoute("Route 3 Westford");
        rte1.addLocation(loc1);
        rte1.addLocation(loc2);
        rte1.addLocation(loc3);
        rte1.addLocation(loc4);
        rte1.addLocation(loc5);

        // Create train
        Train train1 = tmanager.newTrain("TestAggressiveBuildOption");
        train1.setRoute(rte1);

        // Place cars
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(loc2, loc2trk1));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(loc1, loc1trk1));

        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(loc3, loc3trk1));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(loc3, loc3trk2));

        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(loc4, loc4trk1));
        Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(loc4, loc4trk2));
        Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(loc1, loc1trk1));

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e5", Track.OKAY, e5.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e6", Track.OKAY, e6.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e7", Track.OKAY, e7.setLocation(loc1, loc1trk1));
        Assert.assertEquals("Place e8", Track.OKAY, e8.setLocation(loc1, loc1trk1));

        train1.setRequirements(Train.CABOOSE);
        train1.setNumberEngines("3");
        train1.setOwnerOption(Train.ALL_OWNERS);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 After Build 1", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 1", "Westford Yard 2", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 1", "Westford Yard 1", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 1", "Westford Yard 2", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 1", "Boston Yard 1", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 1", "Arlington Siding", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 1", "Westford Yard 2", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 1", "Westford Yard 2", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 1", "Westford Yard 1", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 1", "", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 1", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 1", "Westford Yard 2", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 1", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 1", "", e2.getDestinationTrackName());
        Assert.assertEquals("e3 destination 1", "", e3.getDestinationTrackName());
        Assert.assertEquals("e4 destination 1", "", e4.getDestinationTrackName());
        Assert.assertEquals("e5 destination 1", "", e5.getDestinationTrackName());
        Assert.assertEquals("e6 destination 1", "Westford Yard 1", e6.getDestinationTrackName());
        Assert.assertEquals("e7 destination 1", "Westford Yard 1", e7.getDestinationTrackName());
        Assert.assertEquals("e8 destination 1", "Westford Yard 1", e8.getDestinationTrackName());

        // test departing from staging in aggressive mode
        Assert.assertTrue(train1.reset());
        loc1trk1.setTrackType(Track.STAGING);
        loc1trk2.setTrackType(Track.STAGING);
        loc1.setLocationOps(Location.STAGING);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("Train 1 After Build from staging, eight loco on departure track", train1
                .isBuilt());

        // move locos to other departure track
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place e5", Track.OKAY, e5.setLocation(loc1, loc1trk2));

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train 1 After Build from staging, three loco on departure track", train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 2", "Westford Yard 2", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 2", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 2", "Westford Yard 2", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 2", "Boston Yard 2", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 2", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 2", "Arlington Siding", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 2", "Westford Yard 1", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 2", "Westford Yard 2", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 2", "Westford Yard 2", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 2", "Westford Yard 1", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 2", "Chelmsford Yard 1", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 2", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 2", "", e2.getDestinationTrackName());
        Assert.assertEquals("e3 destination 2", "", e3.getDestinationTrackName());
        Assert.assertEquals("e4 destination 2", "", e4.getDestinationTrackName());
        Assert.assertEquals("e5 destination 2", "", e5.getDestinationTrackName());
        Assert.assertEquals("e6 destination 2", "Westford Yard 1", e6.getDestinationTrackName());
        Assert.assertEquals("e7 destination 2", "Westford Yard 1", e7.getDestinationTrackName());
        Assert.assertEquals("e8 destination 2", "Westford Yard 1", e8.getDestinationTrackName());

        // test train move to a specific location
        Assert.assertFalse("Old Harvard is not part of this trains route", train1.move("Old Harvard"));
        Assert.assertFalse("Train departs New Harvard already there", train1.move("New Harvard"));
        Assert.assertTrue("Next location in train's route is New Arlington", train1.move("New Arlington"));
        Assert.assertFalse("Train is at New Arlington", train1.move("New Arlington"));
        // next location is New Boston, skip it and go directly to New Chelmsford
        Assert.assertTrue("New Chelmsford is in train's route", train1.move("New Chelmsford"));
        Assert.assertTrue("Next location in train's route is New Westford", train1.move("New Westford"));
        Assert.assertFalse("Train is at New Westford last location in train's route", train1
                .move("New Westford"));

        train1.move(); // terminate train

        // now try with a train returning to staging, test alternate track feature
        // Create train
        Train train2 = tmanager.newTrain("Westford to Harvard Aggressive");
        Route rte2 = rmanager.copyRoute(rte1, "Route 4 Harvard", true);
        train2.setRoute(rte2);
        train2.setRequirements(Train.CABOOSE);
        train2.setNumberEngines("3");

        // add 2 yard tracks to siding at Arlington
        Track loc2trk2 = loc2.addTrack("Arlington Yard 1", Track.YARD);
        loc2trk2.setLength(50); // only enough room for one car
        Track loc2trk3 = loc2.addTrack("Arlington Alternate Track", Track.SPUR);
        loc2trk3.setLength(100); // only enough room for two cars

        // set the alternate for Arlington siding
        loc2trk1.setAlternateTrack(loc2trk3);

        // send cars to Arlington siding
        c3.setFinalDestination(loc2);
        c3.setFinalDestinationTrack(loc2trk1);
        c8.setFinalDestination(loc2);
        c8.setFinalDestinationTrack(loc2trk1);
        c9.setFinalDestination(loc2);
        c9.setFinalDestinationTrack(loc2trk1);
        c11.setFinalDestination(loc2);
        c11.setFinalDestinationTrack(loc2trk1);

        train2.reset();
        new TrainBuilder().build(train2);
        Assert.assertTrue("Train 2 returns to staging", train2.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 3", "Harvard Yard 1", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 3", "Arlington Alternate Track", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 3", "Harvard Yard 1", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 3", "Harvard Yard 1", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 3", "Chelmsford Yard 2", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 3", "Arlington Yard 1", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 3", "Arlington Alternate Track", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 3", "Boston Yard 1", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 3", "Arlington Siding", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 3", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 3", "", e2.getDestinationTrackName());
        Assert.assertEquals("e3 destination 3", "", e3.getDestinationTrackName());
        Assert.assertEquals("e4 destination 3", "", e4.getDestinationTrackName());
        Assert.assertEquals("e5 destination 3", "", e5.getDestinationTrackName());
        Assert.assertEquals("e6 destination 3", "Harvard Yard 1", e6.getDestinationTrackName());
        Assert.assertEquals("e7 destination 3", "Harvard Yard 1", e7.getDestinationTrackName());
        Assert.assertEquals("e8 destination 3", "Harvard Yard 1", e8.getDestinationTrackName());

        // check that cars on alternate track are sent to Arlington Siding
        Assert.assertEquals("next dest Arlingtion", loc2, c3.getFinalDestination());
        Assert.assertEquals("next dest track Arlingtion Siding", loc2trk1, c3.getFinalDestinationTrack());
        Assert.assertEquals("next dest Arlingtion", loc2, c8.getFinalDestination());
        Assert.assertEquals("next dest track Arlingtion Siding", loc2trk1, c8.getFinalDestinationTrack());
        Assert.assertEquals("next dest Arlingtion", loc2, c9.getFinalDestination());
        Assert.assertEquals("next dest track Arlingtion Siding", loc2trk1, c9.getFinalDestinationTrack());
        Assert.assertEquals("next dest null", null, c11.getFinalDestination());
        Assert.assertEquals("next dest track null", null, c11.getFinalDestinationTrack());

        // test train move to a an exact location
        Assert.assertFalse("Old Harvard is not part of this trains route", train2
                .moveToNextLocation("Old Harvard"));
        Assert.assertFalse("New Harvard is the last location in this trains route", train2
                .moveToNextLocation("New Harvard"));
        Assert.assertFalse("New Boston is the 3rd to last location in this trains route", train2
                .moveToNextLocation("New Boston"));
        Assert.assertFalse("New Westford is the current location in this trains route", train2
                .moveToNextLocation("New Westford"));
        Assert.assertFalse("New Arlington is the 2nd to last location in this trains route", train2
                .moveToNextLocation("New Arlington"));
        Assert.assertTrue("New Chelmsford is the next location in this trains route", train2
                .moveToNextLocation("New Chelmsford"));
        Assert.assertTrue("New Boston is the next location in this trains route", train2
                .moveToNextLocation("New Boston"));
        Assert.assertTrue("New Arlington is the next location in this trains route", train2
                .moveToNextLocation("New Arlington"));
        Assert.assertTrue("New Harvard is the next location in this trains route", train2
                .moveToNextLocation("New Harvard"));
        Assert.assertFalse("Train is at New Harvard", train2.moveToNextLocation("New Harvard"));

        train2.move(); // terminate train

        // now test train returning to staging
        rte1.addLocation(loc1);
        train1.reset();
        new TrainBuilder().build(train1);
        // should fail, can't return to staging track
        Assert.assertEquals("Train 1 departing and returning to staging", false, train1.isBuilt());
        // change mode
        Setup.setStagingTrackImmediatelyAvail(true);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertEquals("Train 1 departing and returning to staging", true, train1.isBuilt());
        Assert.assertEquals("check departure track name", "Harvard Yard 1", train1.getDepartureTrack().getName());
        Assert.assertEquals("check departure and arrival track", train1.getDepartureTrack(), train1.getTerminationTrack());
        
        // check destinations
        Assert.assertEquals("c1 destination 3", "Harvard Yard 1", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 3", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 3", "Boston Yard 2", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 3", "Westford Yard 1", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 3", "Harvard Yard 1", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 3", "Arlington Siding", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 3", "", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 3", "Chelmsford Yard 1", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 3", "Westford Yard 2", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 3", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 3", "", e2.getDestinationTrackName());
        Assert.assertEquals("e3 destination 3", "", e3.getDestinationTrackName());
        Assert.assertEquals("e4 destination 3", "", e4.getDestinationTrackName());
        Assert.assertEquals("e5 destination 3", "", e5.getDestinationTrackName());
        Assert.assertEquals("e6 destination 3", "Harvard Yard 1", e6.getDestinationTrackName());
        Assert.assertEquals("e7 destination 3", "Harvard Yard 1", e7.getDestinationTrackName());
        Assert.assertEquals("e8 destination 3", "Harvard Yard 1", e8.getDestinationTrackName());
        
        train1.terminate();

        // check car locations
        Assert.assertEquals("c1 location", "Harvard Yard 1", c1.getTrackName());
        Assert.assertEquals("c2 location", "Harvard Yard 2", c2.getTrackName());
        Assert.assertEquals("c3 location", "Arlington Alternate Track", c3.getTrackName());
        Assert.assertEquals("c4 location", "Boston Yard 2", c4.getTrackName());

        Assert.assertEquals("c5 location", "Harvard Yard 2", c5.getTrackName());
        Assert.assertEquals("c6 location", "Westford Yard 1", c6.getTrackName());
        Assert.assertEquals("c7 location", "Harvard Yard 1", c7.getTrackName());
        Assert.assertEquals("c8 location", "Arlington Siding", c8.getTrackName());

        Assert.assertEquals("c9 location", "Arlington Alternate Track", c9.getTrackName());
        Assert.assertEquals("c10 location", "Chelmsford Yard 1", c10.getTrackName());
        Assert.assertEquals("c11 location", "Westford Yard 2", c11.getTrackName());

        Assert.assertEquals("e1 location", "Harvard Yard 2", e1.getTrackName());
        Assert.assertEquals("e2 location", "Harvard Yard 2", e2.getTrackName());
        Assert.assertEquals("e3 location", "Harvard Yard 2", e3.getTrackName());
        Assert.assertEquals("e4 location", "Harvard Yard 2", e4.getTrackName());
        Assert.assertEquals("e5 location", "Harvard Yard 2", e5.getTrackName());
        Assert.assertEquals("e6 location", "Harvard Yard 1", e6.getTrackName());
        Assert.assertEquals("e7 location", "Harvard Yard 1", e7.getTrackName());
        Assert.assertEquals("e8 location", "Harvard Yard 1", e8.getTrackName());

    }

    // test private method getCarOrder
    @Test
    public void testCarOrderNORMAL() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        TrainBuilder tb = new TrainBuilder();
        // start by creating a track
        Location A = lmanager.newLocation("A");
        Track interchangeTrack = A.addTrack("track", Track.INTERCHANGE);
        interchangeTrack.setLength(1000);
        interchangeTrack.setServiceOrder(Track.NORMAL);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date date = cal.getTime();

        // place 3 cars on the track.
        Car a = cmanager.newCar("ABC", "123");
        a.setTypeName(carTypes[1]);
        a.setLength("50");
        a.setLastDate(date);
        a.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car b = cmanager.newCar("ABC", "321");
        b.setTypeName(carTypes[1]);
        b.setLength("50");
        b.setLastDate(date);
        b.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car c = cmanager.newCar("ABC", "111");
        c.setTypeName(carTypes[1]);
        c.setLength("50");
        c.setLastDate(date);
        c.setLocation(A, interchangeTrack);

        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method getCarOrderMethod = null;
        try {
            getCarOrderMethod = tb.getClass().getDeclaredMethod("getCarOrder", Car.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method getCarOrder in TrackBuilder class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(getCarOrderMethod);
        getCarOrderMethod.setAccessible(true);

        // and set the car list up.
        tb._carList = new java.util.ArrayList<>();
        tb._carList.add(a);
        tb._carList.add(b);
        tb._carList.add(c);

        try {
            // with Track.NORMAL order, the car you ask for is the
            // car you get.
            Car d = (Car) getCarOrderMethod.invoke(tb, a);
            Assert.assertEquals("NORMAL Order, 123 first", a, d);
            d = (Car) getCarOrderMethod.invoke(tb, b);
            Assert.assertEquals("NORMAL Order, 321 second", b, d);
            d = (Car) getCarOrderMethod.invoke(tb, c);
            Assert.assertEquals("NORMAL Order, 111 last", c, d);
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method getCarOrder in TrackBuilder class");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("getCarOrder  executon failed reason: " + cause.getMessage());
        }
    }

    @Test
    public void testCarOrderFIFO() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        TrainBuilder tb = new TrainBuilder();
        // start by creating a track
        Location A = lmanager.newLocation("A");
        Track interchangeTrack = A.addTrack("track", Track.INTERCHANGE);
        interchangeTrack.setLength(1000);
        interchangeTrack.setServiceOrder(Track.FIFO);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date date = cal.getTime();

        // and placing 3 cars on the track.
        Car a = cmanager.newCar("ABC", "123");
        a.setTypeName(carTypes[1]);
        a.setLength("50");
        a.setLastDate(date);
        a.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car b = cmanager.newCar("ABC", "321");
        b.setTypeName(carTypes[1]);
        b.setLength("50");
        b.setLastDate(date);
        b.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car c = cmanager.newCar("ABC", "111");
        c.setTypeName(carTypes[1]);
        c.setLength("50");
        c.setLastDate(date);
        c.setLocation(A, interchangeTrack);

        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method getCarOrderMethod = null;
        try {
            getCarOrderMethod = tb.getClass().getDeclaredMethod("getCarOrder", Car.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method getCarOrder in TrackBuilder class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(getCarOrderMethod);
        getCarOrderMethod.setAccessible(true);

        // and set the car list up.
        tb._carList = new java.util.ArrayList<>();
        tb._carList.add(a);
        tb._carList.add(b);
        tb._carList.add(c);

        try {
            // FIFO order should always return 123.
            Car d = (Car) getCarOrderMethod.invoke(tb, a);
            Assert.assertEquals("FIFO Order, 123 first", a, d);
            d = (Car) getCarOrderMethod.invoke(tb, b);
            Assert.assertEquals("FIFO Order, 123 second", a, d);
            d = (Car) getCarOrderMethod.invoke(tb, c);
            Assert.assertEquals("FIFO Order, 123 third", a, d);
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method getCarOrder in TrackBuilder class");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("getCarOrder  executon failed reason: " + cause.getMessage());
        }
    }

    @Test
    public void testCarOrderLIFO() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        TrainBuilder tb = new TrainBuilder();
        // start by creating a track
        Location A = lmanager.newLocation("A");
        Track interchangeTrack = A.addTrack("track", Track.INTERCHANGE);
        interchangeTrack.setLength(1000);
        interchangeTrack.setServiceOrder(Track.LIFO);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date date = cal.getTime();

        // and placing 3 cars on the track.
        Car a = cmanager.newCar("ABC", "123");
        a.setTypeName(carTypes[1]);
        a.setLength("50");
        a.setLastDate(date);
        a.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car b = cmanager.newCar("ABC", "321");
        b.setTypeName(carTypes[1]);
        b.setLength("50");
        b.setLastDate(date);
        b.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car c = cmanager.newCar("ABC", "111");
        c.setTypeName(carTypes[1]);
        c.setLength("50");
        c.setLastDate(date);
        c.setLocation(A, interchangeTrack);

        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method getCarOrderMethod = null;
        try {
            getCarOrderMethod = tb.getClass().getDeclaredMethod("getCarOrder", Car.class);
        } catch (java.lang.NoSuchMethodException nsm) {
            Assert.fail("Could not find method getCarOrder in TrackBuilder class: ");
        }

        // override the default permissions.
        Assert.assertNotNull(getCarOrderMethod);
        getCarOrderMethod.setAccessible(true);

        // and set the car list up.
        tb._carList = new java.util.ArrayList<>();
        tb._carList.add(a);
        tb._carList.add(b);
        tb._carList.add(c);

        try {
            // LIFO order should always return 111.
            Car d = (Car) getCarOrderMethod.invoke(tb, c);
            Assert.assertEquals("LIFO Order, 111 first", c, d);
            d = (Car) getCarOrderMethod.invoke(tb, b);
            Assert.assertEquals("LIFO Order, 111 second", c, d);
            d = (Car) getCarOrderMethod.invoke(tb, a);
            Assert.assertEquals("LIFO Order, 111 third", c, d);
        } catch (java.lang.IllegalAccessException iae) {
            Assert.fail("Could not access method getCarOrder in TrackBuilder class");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            Throwable cause = ite.getCause();
            Assert.fail("getCarOrder  executon failed reason: " + cause.getMessage());
        }
    }

    private void setUpRoute(Route route) {
        A = lmanager.newLocation("A");
        B = lmanager.newLocation("B");
        C = lmanager.newLocation("C");
        Track At = A.addTrack("track", Track.SPUR);
        Track Bt = B.addTrack("track", Track.SPUR);
        Track Ct = C.addTrack("track", Track.SPUR);
        At.setLength(300);
        At.acceptsTypeName(Bundle.getMessage("engineDefaultType"));
        Bt.setLength(300);
        Bt.acceptsTypeName(Bundle.getMessage("engineDefaultType"));
        Ct.setLength(300);
        Ct.acceptsTypeName(Bundle.getMessage("engineDefaultType"));

        rA = route.addLocation(A);
        rB = route.addLocation(B);
        rC = route.addLocation(C);

        rA.setMaxCarMoves(5);
        rB.setMaxCarMoves(5);
        rC.setMaxCarMoves(5);

        Engine e1 = emanager.newEngine("E", "1");
        e1.setModel("GP40");
        Engine e2 = emanager.newEngine("E", "2");
        e2.setModel("GP40");
        Engine e3 = emanager.newEngine("E", "3");
        e3.setModel("GP40");
        Engine e4 = emanager.newEngine("E", "4");
        e4.setModel("GP40");

        e1.setLocation(A, At);
        e2.setLocation(A, At);
        e3.setLocation(A, At);
        e4.setLocation(A, At);

    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Before
    public void setUp() throws Exception {
        JUnitUtil.setUp();

        JUnitOperationsUtil.resetOperationsManager();
        // setup new managers
        tmanager = InstanceManager.getDefault(TrainManager.class);
        rmanager = InstanceManager.getDefault(RouteManager.class);
        lmanager = InstanceManager.getDefault(LocationManager.class);
        emanager = InstanceManager.getDefault(EngineManager.class);
        cmanager = InstanceManager.getDefault(CarManager.class);
        smanager = InstanceManager.getDefault(ScheduleManager.class);
        cc = InstanceManager.getDefault(CarColors.class);
        cl = InstanceManager.getDefault(CarLengths.class);
        co = InstanceManager.getDefault(CarOwners.class);
        cr = InstanceManager.getDefault(CarRoads.class);
        cld = InstanceManager.getDefault(CarLoads.class);
        ct = InstanceManager.getDefault(CarTypes.class);
        et = InstanceManager.getDefault(EngineTypes.class);
        em = InstanceManager.getDefault(EngineModels.class);

        // disable build messages
        tmanager.setBuildMessagesEnabled(false);
        // disable build reports
        tmanager.setBuildReportEnabled(false);

        Setup.setBuildAggressive(false);
        Setup.setTrainIntoStagingCheckEnabled(true);
        Setup.setMaxTrainLength(1000);
        Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
        Setup.setLocalInterchangeMovesEnabled(false);
        Setup.setLocalSpurMovesEnabled(false);
        Setup.setLocalYardMovesEnabled(false);
        Setup.setCarMoves(7); // set default to 7 moves per location

    }

    // The minimal setup for log4J
    @After
    public void tearDown() throws Exception {
        JUnitUtil.tearDown();
    }

    private String getTrainStatus(Train train) {
        String[] status = train.getStatus().split(" ");
        return status[0];
    }

}
