package jmri.jmrit.operations.trains;

import java.io.BufferedReader;
import java.io.File;
import java.util.List;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleItem;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLengths;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
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
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import jmri.util.JUnitOperationsUtil;
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
public class TrainBuilderTest extends OperationsTestCase {

    private final int DIRECTION_ALL = Location.EAST + Location.WEST + Location.NORTH + Location.SOUTH;

    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.JmritOperationsBundle");

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

        // Change default for number of cars moved per location
        Setup.setCarMoves(2);

        // create train and give it a three location route
        Train train = tmanager.newTrain("TestBuildRequiresCars");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train.setRoute(route);

        // Build option require cars
        Control.fullTrainOnly = true;
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, requires cars", train.isBuilt());

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        // place two cars at start of route, changes where checks are made in TrainBuilder
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("A", "2", "Boxcar", "40", actonSpur1, 0);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, requires more cars", train.isBuilt());

        // only need three cars to build
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("A", "3", "Boxcar", "40", actonSpur1, 0);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, has the cars", train.isBuilt());

        // restore control
        Control.fullTrainOnly = false;
        c1.setLocation(null, null);
        c2.setLocation(null, null);
        c3.setLocation(null, null);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, build doesn't require cars", train.isBuilt());
    }

    @Test
    public void testAutoEnginesBuildFailNoEngines() {

        // improve test coverage by selecting meter lengths
        Setup.setLengthUnit(Setup.METER);

        Train train = tmanager.newTrain("TestAutoEnginesBuildFailNoEngines");
        train.setNumberEngines(Train.AUTO);

        Route route = getThreeLocationRouteWithFourEngines();
        train.setRoute(route);

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

        Train train = tmanager.newTrain("TestAutoEnginesSingleEngine");
        train.setNumberEngines(Train.AUTO);

        Route route = getThreeLocationRouteWithFourEngines();
        train.setRoute(route);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, only needs a single engine", train.isBuilt());

    }

    @Test
    public void testBunit() {

        Train train = tmanager.newTrain("BunitEngineTest");
        train.setNumberEngines("1");

        Route route = getThreeLocationRouteWithFourEngines();
        train.setRoute(route);

        Engine e1 = emanager.getByRoadAndNumber("E", "1");
        Engine e2 = emanager.getByRoadAndNumber("E", "2");
        Engine e3 = emanager.getByRoadAndNumber("E", "3");
        Engine e4 = emanager.getByRoadAndNumber("E", "4");

        // make all of them B units
        e1.setBunit(true);
        e2.setBunit(true);
        e3.setBunit(true);
        e4.setBunit(true);

        Consist consist = emanager.newConsist("Two engines");
        e4.setConsist(consist);
        e3.setConsist(consist);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, only b units available", train.isBuilt());

        // restore one engine
        e2.setBunit(false);

        Assert.assertTrue(e1.isBunit());
        Assert.assertFalse(e2.isBunit());
        Assert.assertTrue(e3.isBunit());
        Assert.assertTrue(e4.isBunit());

        // single non b unit engine now available
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build", train.isBuilt());

        // now request 2 locos, both engines are b units, should fail?
        train.setNumberEngines("2");
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build, there's a consist of b units", train.isBuilt());

        // get rid of consist
        e3.setConsist(null);
        e4.setConsist(null);

        // try again, should fail
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build, no conisit", train.isBuilt());

        // change build option to allow program build a consist
        train.setBuildConsistEnabled(true);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue("Train should now build", train.isBuilt());

        // make all b units should fail
        e2.setBunit(true);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build", train.isBuilt());
    }

    @Test
    public void testAutoEnginesTwoEngines() {

        Train train = tmanager.newTrain("TestAutoEnginesTwoEngines");
        train.setNumberEngines(Train.AUTO);

        Route route = getThreeLocationRouteWithFourEngines();
        train.setRoute(route);

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

        Train train = tmanager.newTrain("TestAutoEnginesGrade");
        train.setNumberEngines(Train.AUTO);

        Route route = getThreeLocationRouteWithFourEngines();
        train.setRoute(route);

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
        Route route = rmanager.newRoute("Route-depart-middle-terminate");

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

        Engine e1 = emanager.newRS("E", "1");
        e1.setModel("GP40");
        e1.setLocation(depart, departureTrack);

        Engine e2 = emanager.newRS("E", "2");
        e2.setModel("GP40");
        e2.setLocation(depart, departureTrack);

        Engine e3 = emanager.newRS("E", "3");
        e3.setModel("GP40");
        e3.setLocation(depart, departureTrack);

        Engine e4 = emanager.newRS("E", "4");
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

        Train train = tmanager.newTrain("TestMaxEngines");
        train.setNumberEngines(Train.AUTO);

        Route route = getThreeLocationRouteWithFourEngines();
        train.setRoute(route);

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

    @Test
    public void testEnginesToTerminal() {

        Train train = tmanager.newTrain("TestEnginesToTerminal");
        train.setNumberEngines("4");
        train.setBuildConsistEnabled(true);

        Route route = getThreeLocationRouteWithFourEngines();
        train.setRoute(route);

        new TrainBuilder().build(train);
        Assert.assertTrue("Train should build", train.isBuilt());

        // make track at location "C" not accept train's direction
        Track track = C.getTrackByName("track", null);
        track.setTrainDirections(Track.EAST);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build", train.isBuilt());

        // delete track at location "C"        
        C.deleteTrack(track);
        Assert.assertEquals("confirm no tracks at C", 0, C.getLength());

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertFalse("Train should not build", train.isBuilt());
    }

    @Test
    public void testEngineDestinations() {

        Train train1 = tmanager.newTrain("EngineDestinationsTest1");
        train1.setNumberEngines("2");

        Train train2 = tmanager.newTrain("EngineDestinationsTest2");

        Route route = getThreeLocationRouteWithFourEngines();
        train1.setRoute(route);
        train2.setRoute(route);

        Track tA = A.getTrackByName("track", null);
        Track tB = B.getTrackByName("track", null);
        Track tC = C.getTrackByName("track", null);

        Engine e1 = emanager.getByRoadAndNumber("E", "1");
        Engine e2 = emanager.getByRoadAndNumber("E", "2");

        Consist consist = emanager.newConsist("Two engines");
        e1.setConsist(consist);
        e2.setConsist(consist);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("Train status", train1.isBuilt());

        // add an engine
        train1.setNumberEngines("2");
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train built 8", train1.isBuilt());
        Assert.assertEquals("5 Destination e1", "C", e1.getDestinationName());
        Assert.assertEquals("5 Destination e2", "C", e2.getDestinationName());

        // assign lead engine to train 2
        train1.reset();
        e1.setTrain(train2);
        // should fail 
        new TrainBuilder().build(train1);
        Assert.assertFalse("train status", train1.isBuilt());

        // assign one of the consist engine to train 2
        e1.setTrain(train1);
        e2.setTrain(train2); // shouldn't pay attention to the other engine
        // should build
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train built 10", train1.isBuilt());
        train1.reset();

        // both engines should release
        Assert.assertEquals("6 Destination e1", "", e1.getDestinationName());
        Assert.assertEquals("6 Train e1", "", e1.getTrainName());
        Assert.assertEquals("6 Destination e2", "", e2.getDestinationName());
        Assert.assertEquals("6 Train e2", "", e2.getTrainName());

        // now try setting engine destination that isn't the terminal
        Assert.assertEquals("set destination e1", Track.OKAY, e1.setDestination(B, tB));
        // should fail
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("train built 11", train1.isBuilt());

        Assert.assertEquals(Track.OKAY, e1.setDestination(C, tC));
        Assert.assertEquals(Track.OKAY, e2.setDestination(B, tB)); // program should ignore
        // should build
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train built 12", train1.isBuilt());

        Assert.assertEquals("5 Destination e1", "C", e1.getDestinationName());
        Assert.assertEquals("5 Destination e2", "C", e2.getDestinationName());

        // set lead engine's track to null
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(A, null));
        // should not build
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("train will not build engine track is null", train1.isBuilt());
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(A, tA));

        // should now build
        train1.reset();
        new TrainBuilder().build(train1);
    }

    @Test
    public void testTrainLengthRestricions() {

        Train train1 = tmanager.newTrain("TrainLengthRestricions1");
        train1.setNumberEngines("2");

        Train train2 = tmanager.newTrain("TrainLengthRestricions2");

        Route route = getThreeLocationRouteWithFourEngines();
        RouteLocation rlA = route.getDepartsRouteLocation();
        train1.setRoute(route);
        train2.setRoute(route);

        Track tA = A.getTrackByName("track", null);

        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "32", tA, 10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", Bundle.getMessage("Caboose"), "32", tA, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", tA, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", tA, 13);

        c1.setCaboose(true);
        c2.setCaboose(true);
        c3.setFred(true);
        c4.setFred(true);

        Engine e1 = emanager.getByRoadAndNumber("E", "1");
        Engine e2 = emanager.getByRoadAndNumber("E", "2");

        Consist consist = emanager.newConsist("Two engines");
        e1.setConsist(consist);
        e2.setConsist(consist);

        // require 2 engines
        train1.setNumberEngines("2");
        // require car with FRED
        train1.setRequirements(Train.FRED);

        new TrainBuilder().build(train1);
        Assert.assertTrue("train built 8", train1.isBuilt());
        Assert.assertEquals("5 Destination e1", "C", e1.getDestinationName());
        Assert.assertEquals("5 Destination e2", "C", e2.getDestinationName());

        Assert.assertEquals("Engine consist total length", 120, consist.getTotalLength());
        // consist is 120' car with FRED 44' with couplers
        Assert.assertEquals("Train total length", 164, train1.getCurrentLocation().getTrainLength());

        rlA.setMaxTrainLength(155); // only enough for the two engines, train needs a car with FRED
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("Train length test, can't service car with FRED", train1.isBuilt());

        // build failed after engines were assigned to train 1
        Assert.assertEquals("Engine assignment ignores train length restrictions", train1, e1.getTrain());
        Assert.assertEquals("Engine assignment ignores train length restrictions", train1, e2.getTrain());
        Assert.assertEquals("Engine destination ignores train length restrictions", "C", e1
                .getDestinationName());
        Assert.assertEquals("Engine destination ignores train length restrictions", "C", e2
                .getDestinationName());

        Assert.assertEquals("Check CP30 engine length", "56", e1.getLength());
        Assert.assertEquals("Check CP 20 length", "32", c2.getLength());
        rlA.setMaxTrainLength(164); // enough for the two engines and a car with FRED

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train length test, just enough length for engines and car with FRED", train1.isBuilt());
    }

    /**
     * Test cars with destinations.
     */
    @Test
    public void testCarDestinationsA() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);
        Track actonYard2 = acton.getTrackByName("Acton Yard 2", null);

        Location chelmsford = route.getRouteLocationBySequenceNumber(3).getLocation();

        // Set up two cabooses and six box cars, two with FRED       
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "32", actonYard1, 10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", Bundle.getMessage("Caboose"), "32", actonYard1, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", carTypes[1], "40", actonYard1, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", carTypes[1], "40", actonYard1, 13);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", carTypes[1], "40", actonYard1, 14);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", carTypes[1], "40", actonYard2, 15);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", carTypes[1], "50", actonYard2, 16);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", carTypes[1], "60", actonYard2, 17);
        Car c9 = JUnitOperationsUtil.createAndPlaceCar("CP", "90", carTypes[5], "40", actonYard2, 18);
        Car c10 = JUnitOperationsUtil.createAndPlaceCar("CP", "100", carTypes[5], "40", actonYard2, 19);

        c1.setCaboose(true);
        c2.setCaboose(true);
        c3.setFred(true);
        c4.setFred(true);

        // define the train
        Train train1 = tmanager.newTrain("TestCarDestinations1");
        train1.setRoute(route);

        // don't allow through cars
        train1.setAllowThroughCarsEnabled(false);

        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        Assert.assertEquals("should be 6 cars", 6, train1.getNumberCarsWorked());

        // check car destinations
        Assert.assertEquals("Destination c1", "", c1.getDestinationName()); // caboose
        Assert.assertEquals("Destination c2", "", c2.getDestinationName()); // caboose
        Assert.assertEquals("Destination c3", "", c3.getDestinationName()); // car with FRED
        Assert.assertEquals("Destination c4", "", c4.getDestinationName()); // car with FRED
        Assert.assertEquals("Destination c5", "Boston", c5.getDestinationName());
        Assert.assertEquals("Destination c6", "Chelmsford", c6.getDestinationName());
        Assert.assertEquals("Destination c7", "Boston", c7.getDestinationName());
        Assert.assertEquals("Destination c8", "Chelmsford", c8.getDestinationName());
        Assert.assertEquals("Destination c9", "Boston", c9.getDestinationName());
        Assert.assertEquals("Destination c10", "Chelmsford", c10.getDestinationName());

        // release cars from train
        Assert.assertTrue("reset train", train1.reset());
        Assert.assertEquals("Train reset", Train.CODE_TRAIN_RESET, train1.getStatusCode());
        // set all car destinations to be Chelmsford
        Assert.assertEquals(Track.OKAY, c1.setDestination(chelmsford, null));
        Assert.assertEquals(Track.OKAY, c2.setDestination(chelmsford, null));
        Assert.assertEquals(Track.OKAY, c3.setDestination(chelmsford, null));
        Assert.assertEquals(Track.OKAY, c4.setDestination(chelmsford, null));
        Assert.assertEquals(Track.OKAY, c5.setDestination(chelmsford, null));
        Assert.assertEquals(Track.OKAY, c6.setDestination(chelmsford, null));
        Assert.assertEquals(Track.OKAY, c7.setDestination(chelmsford, null));
        Assert.assertEquals(Track.OKAY, c8.setDestination(chelmsford, null));
        Assert.assertEquals(Track.OKAY, c9.setDestination(chelmsford, null));
        Assert.assertEquals(Track.OKAY, c10.setDestination(chelmsford, null));

        // set c5 and c9 to be serviced by train 2
        Train train2 = tmanager.newTrain("TestCarDestinations2");
        c5.setTrain(train2);
        c9.setTrain(train2);
        // set c6 to be serviced by train 1
        c6.setTrain(train1);

        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // confirm train assignments
        Assert.assertEquals("train assignment", null, c1.getTrain()); // caboose, not assigned to train
        Assert.assertEquals("train assignment", null, c2.getTrain()); // caboose, not assigned to train
        Assert.assertEquals("train assignment", null, c3.getTrain()); // car with FRED, not assigned to train
        Assert.assertEquals("train assignment", null, c4.getTrain()); // car with FRED, not assigned to train
        Assert.assertEquals("train assignment", train1, c6.getTrain());
        Assert.assertEquals("train assignment", train1, c7.getTrain());
        Assert.assertEquals("train assignment", train1, c8.getTrain());
        Assert.assertEquals("train assignment", train1, c10.getTrain());

        Assert.assertEquals("train assignment", train2, c5.getTrain());
        Assert.assertEquals("train assignment", train2, c9.getTrain());

        // require a caboose, should fail, both cabooses have destination Chelmsford
        train1.setRequirements(Train.CABOOSE);
        new TrainBuilder().build(train1);
        Assert.assertFalse("train status", train1.isBuilt());

        // change destination for c2 caboose
        // Set caboose destination to be the terminal
        train1.reset();

        // train reset will clear a car's destination if it was assigned to the train
        Assert.assertEquals("2 Destination  c1", "Chelmsford", c1.getDestinationName()); // caboose
        Assert.assertEquals("2 Destination  c2", "Chelmsford", c2.getDestinationName());
        Assert.assertEquals("2 Destination  c3", "Chelmsford", c3.getDestinationName());
        Assert.assertEquals("2 Destination  c4", "Chelmsford", c4.getDestinationName());
        Assert.assertEquals("2 Destination  c5", "Chelmsford", c5.getDestinationName()); // assigned to train 2
        // destination for c6, c7, c8 were cleared by train reset
        Assert.assertEquals("2 Destination  c6", "", c6.getDestinationName());
        Assert.assertEquals("2 Destination  c7", "", c7.getDestinationName());
        Assert.assertEquals("2 Destination  c8", "", c8.getDestinationName());
        Assert.assertEquals("2 Destination  c9", "Chelmsford", c9.getDestinationName()); // assigned to train 2
        Assert.assertEquals("2 Destination  c10", "", c10.getDestinationName());

        Assert.assertEquals("set caboose destination", Track.OKAY, c2.setDestination(acton, actonYard2));
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // confirm train assignments
        Assert.assertEquals("train assignment", null, c1.getTrain()); // caboose, not assigned to train
        Assert.assertEquals("train assignment", train1, c2.getTrain()); // caboose
        Assert.assertEquals("train assignment", null, c3.getTrain()); // car with FRED, not assigned to train
        Assert.assertEquals("train assignment", null, c4.getTrain()); // car with FRED, not assigned to train
        Assert.assertEquals("train assignment", train1, c6.getTrain());
        Assert.assertEquals("train assignment", train1, c7.getTrain());
        Assert.assertEquals("train assignment", train1, c8.getTrain());
        Assert.assertEquals("train assignment", train1, c10.getTrain());

        Assert.assertEquals("train assignment", train2, c5.getTrain());
        Assert.assertEquals("train assignment", train2, c9.getTrain());

        // check car destinations
        Assert.assertEquals("2 Destination  c1", "Chelmsford", c1.getDestinationName()); // caboose
        Assert.assertEquals("2 Destination  c2", "Acton", c2.getDestinationName());
        Assert.assertEquals("2 Destination  c3", "Chelmsford", c3.getDestinationName());
        Assert.assertEquals("2 Destination  c4", "Chelmsford", c4.getDestinationName());
        Assert.assertEquals("2 Destination  c5", "Chelmsford", c5.getDestinationName());
        Assert.assertEquals("2 Destination  c6", "Boston", c6.getDestinationName());
        Assert.assertEquals("2 Destination  c7", "Chelmsford", c7.getDestinationName());
        Assert.assertEquals("2 Destination  c8", "Boston", c8.getDestinationName());
        Assert.assertEquals("2 Destination  c9", "Chelmsford", c9.getDestinationName());
        Assert.assertEquals("2 Destination  c10", "Chelmsford", c10.getDestinationName());

        // now try requesting car with FRED, should fail both cars with FRED have destination Chelmsford
        train1.setRequirements(Train.FRED);

        train1.reset();
        // train reset will clear a car's destination if it was assigned to the train
        Assert.assertEquals("2 Destination  c1", "Chelmsford", c1.getDestinationName()); // caboose
        Assert.assertEquals("2 Destination  c2", "", c2.getDestinationName()); // cleared by train reset
        Assert.assertEquals("2 Destination  c3", "Chelmsford", c3.getDestinationName());
        Assert.assertEquals("2 Destination  c4", "Chelmsford", c4.getDestinationName());
        Assert.assertEquals("2 Destination  c5", "Chelmsford", c5.getDestinationName()); // assigned to train 2
        // destination for c6, c7, c8 were cleared by train reset
        Assert.assertEquals("2 Destination  c6", "", c6.getDestinationName());
        Assert.assertEquals("2 Destination  c7", "", c7.getDestinationName());
        Assert.assertEquals("2 Destination  c8", "", c8.getDestinationName());
        Assert.assertEquals("2 Destination  c9", "Chelmsford", c9.getDestinationName()); // assigned to train 2
        Assert.assertEquals("2 Destination  c10", "", c10.getDestinationName());

        new TrainBuilder().build(train1);
        Assert.assertFalse("train status", train1.isBuilt());

        // Set car with FRED destination to be the terminal
        Assert.assertEquals("set caboose destination", Track.OKAY, c4.setDestination(acton, actonYard2));
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // confirm train assignments
        Assert.assertEquals("train assignment", null, c1.getTrain()); // caboose, not assigned to train
        Assert.assertEquals("train assignment", null, c2.getTrain()); // caboose, not assigned to train
        Assert.assertEquals("train assignment", null, c3.getTrain()); // car with FRED, not assigned to train
        Assert.assertEquals("train assignment", train1, c4.getTrain()); // car with FRED
        Assert.assertEquals("train assignment", train1, c6.getTrain());
        Assert.assertEquals("train assignment", train1, c7.getTrain());
        Assert.assertEquals("train assignment", train1, c8.getTrain());
        Assert.assertEquals("train assignment", train1, c10.getTrain());

        Assert.assertEquals("train assignment", train2, c5.getTrain());
        Assert.assertEquals("train assignment", train2, c9.getTrain());

        // check car destinations
        Assert.assertEquals("2 Destination  c1", "Chelmsford", c1.getDestinationName()); // caboose
        Assert.assertEquals("2 Destination  c2", "", c2.getDestinationName()); // cleared by train reset
        Assert.assertEquals("2 Destination  c3", "Chelmsford", c3.getDestinationName());
        Assert.assertEquals("2 Destination  c4", "Acton", c4.getDestinationName());
        Assert.assertEquals("2 Destination  c5", "Chelmsford", c5.getDestinationName());
        // destination for c6 and c8 was cleared by train reset
        Assert.assertEquals("2 Destination  c6", "Boston", c6.getDestinationName());
        Assert.assertEquals("2 Destination  c7", "Chelmsford", c7.getDestinationName());
        Assert.assertEquals("2 Destination  c8", "Boston", c8.getDestinationName());
        Assert.assertEquals("2 Destination  c9", "Chelmsford", c9.getDestinationName());
        Assert.assertEquals("2 Destination  c10", "Chelmsford", c10.getDestinationName());
    }

    /**
     * Test that a car with a destination not served by the train isn't added to
     * the train.
     */
    @Test
    public void testCarDestinationsB() {

        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford 1");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        Location texas = lmanager.newLocation("Texas");
        Track texasYard = texas.addTrack("Yard", Track.YARD);
        texasYard.setLength(100);

        // place car at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);

        // give the car a destination that isn't reachable by train
        Assert.assertEquals("set car destination", Track.OKAY, c1.setDestination(texas, texasYard));

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 not assigned to train", null, c1.getTrain());
        Assert.assertEquals("c1 destination", texasYard, c1.getDestinationTrack());
    }

    /**
     * Test car with destination that's the terminal.
     */
    @Test
    public void testCarDestinationsC() {

        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        // don't allow through cars
        train1.setAllowThroughCarsEnabled(false);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();

        // place car at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);

        // give the car a destination that's the terminal
        Assert.assertEquals("set car destination", Track.OKAY, c1.setDestination(chelmsford, null));

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 not assigned to train", null, c1.getTrain());
        Assert.assertEquals("c1 destination", chelmsford, c1.getDestination());

        // make c1 a passenger car, it can go to terminal
        c1.setPassenger(true);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 not assigned to train", train1, c1.getTrain());
        Assert.assertEquals("c1 destination", chelmsford, c1.getDestination());
    }

    /**
     * Test that car with destination is pulled at the right location in a
     * train's route
     */
    @Test
    public void testCarDestinationsD() {

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        Location acton = route.getDepartsRouteLocation().getLocation();

        RouteLocation rlBoston1 = route.getRouteLocationBySequenceNumber(2);
        RouteLocation rlBoston2 = route.getRouteLocationBySequenceNumber(5);

        Location boston = rlBoston1.getLocation();
        Track bostonYard1 = boston.getTrackByName("Boston Yard 1", null);

        // place car at Boston, train visits this location twice
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", bostonYard1, 12);

        // send car to Acton
        Assert.assertEquals("set car destination", Track.OKAY, c3.setDestination(acton, null));

        // define the train
        Train train1 = tmanager.newTrain("TestCarDestinations1");
        train1.setRoute(route);

        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // confirm that car is picked up at the second visit to Boston
        Assert.assertEquals("car pull RouteLocation", rlBoston2, c3.getRouteLocation());

        // don't allow pulls from the 2nd Boston visit
        rlBoston2.setPickUpAllowed(false);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // confirm that car is picked up at the 1st visit to Boston
        Assert.assertEquals("car pull RouteLocation", rlBoston1, c3.getRouteLocation());
    }

    /**
     * Test car with destination, train route length restrictions
     */
    @Test
    public void testCarDestinationsE() {

        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        RouteLocation rlBoston = route.getRouteLocationBySequenceNumber(2);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();

        // place car at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("A", "2", "Boxcar", "40", actonSpur1, 10);

        // give the cars a destination that's the terminal
        Assert.assertEquals("set car destination", Track.OKAY, c1.setDestination(chelmsford, null));
        Assert.assertEquals("set car destination", Track.OKAY, c2.setDestination(chelmsford, null));

        // limit the route train length to only one car
        rlBoston.setMaxTrainLength(60);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("car train assignment", train1, c1.getTrain());
        Assert.assertEquals("car destination", chelmsford, c1.getDestination());

        // code currently eliminates the car's destination  TODO is this correct?
        Assert.assertEquals("car train assignment", null, c2.getTrain());
        Assert.assertEquals("car destination", null, c2.getDestination());
    }

    /**
     * Test car with destination, train and track direction restrictions
     */
    @Test
    public void testCarDestinationsF() {

        // train travels North
        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();

        // place car at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);

        // give the car a destination that's the terminal
        Assert.assertEquals("set car destination", Track.OKAY, c1.setDestination(chelmsford, null));

        // configure Chelmsford to only service south bound trains
        chelmsford.setTrainDirections(Location.SOUTH);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("car train assignment", null, c1.getTrain());
        // code currently eliminates the car's destination  TODO is this correct?
        Assert.assertEquals("car destination", null, c1.getDestination());
    }

    /**
     * Test track services train when car has a destination
     */
    @Test
    public void testCarDestinationsG() {

        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);
        Track chelmsfordSpur2 = chelmsford.getTrackByName("Chelmsford Spur 2", null);

        // place car at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);

        // give the car a destination that's the terminal
        Assert.assertEquals("set car destination", Track.OKAY, c1.setDestination(chelmsford, null));

        // configure Chelmsford Spur 1 to not service this train
        chelmsfordSpur1.setDropOption(Track.EXCLUDE_ROUTES);
        chelmsfordSpur1.addDropId(route.getId());

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("car train assignment", train1, c1.getTrain());
        Assert.assertEquals("car destination", chelmsford, c1.getDestination());
        Assert.assertEquals("car destination", chelmsfordSpur2, c1.getDestinationTrack());
    }

    /**
     * Test car has destination, and track at destination has alternate
     */
    @Test
    public void testCarDestinationsH() {

        // train departs North bound
        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);
        Track chelmsfordSpur2 = chelmsford.getTrackByName("Chelmsford Spur 2", null);
        Track chelmsfordYard2 = chelmsford.getTrackByName("Chelmsford Yard 2", null);

        // place cars at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("A", "2", "Boxcar", "40", actonSpur1, 10);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("A", "3", "Boxcar", "40", actonSpur1, 20);

        // give the cars a destination that's the terminal
        Assert.assertEquals("set car destination", Track.OKAY, c1.setDestination(chelmsford, null));
        Assert.assertEquals("set car destination", Track.OKAY, c2.setDestination(chelmsford, null));
        Assert.assertEquals("set car destination", Track.OKAY, c3.setDestination(chelmsford, null));

        // limit Chelmsford spur 1 to one car
        chelmsfordSpur1.setLength(60);
        chelmsfordSpur1.setAlternateTrack(chelmsfordYard2);

        // limit the alternate to only one car
        chelmsfordYard2.setLength(50);

        // c1 to spur, c2 due to track length issue, to alternate
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("car train assignment", train1, c1.getTrain());
        Assert.assertEquals("car destination", chelmsfordSpur1, c1.getDestinationTrack());
        Assert.assertEquals("car train assignment", train1, c2.getTrain());
        Assert.assertEquals("car destination", chelmsfordYard2, c2.getDestinationTrack());
        Assert.assertEquals("car final destination track", chelmsfordSpur1, c2.getFinalDestinationTrack());

        // no room for c3 at spur1 or alternate yard 2, next option was spur 2
        Assert.assertEquals("car train assignment", train1, c3.getTrain());
        Assert.assertEquals("car destination", chelmsfordSpur2, c3.getDestinationTrack());
    }

    @Test
    public void testYardFIFO() {

        // train departs North bound
        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);

        Location boston = route.getRouteLocationBySequenceNumber(2).getLocation();
        Track bostonSpur1 = boston.getTrackByName("Boston Spur 1", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);
        //        Track chelmsfordSpur2 = chelmsford.getTrackByName("Chelmsford Spur 2", null);
        //        Track chelmsfordYard2 = chelmsford.getTrackByName("Chelmsford Yard 2", null);

        // place cars at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonYard1, 0);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("A", "2", "Boxcar", "40", actonYard1, 10);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("A", "3", "Boxcar", "40", actonYard1, 20);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("A", "4", "Boxcar", "40", actonYard1, 30);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("A", "5", "Boxcar", "40", actonYard1, 40);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("A", "6", "Boxcar", "40", actonYard1, 50);

        java.util.Calendar cal = java.util.Calendar.getInstance();
        java.util.Date start = cal.getTime();

        cal.setTime(start);
        cal.add(java.util.Calendar.HOUR_OF_DAY, -4);
        c1.setLastDate(cal.getTime()); // 4 hour ago

        cal.setTime(start);
        cal.add(java.util.Calendar.HOUR_OF_DAY, -2);
        c2.setLastDate(cal.getTime()); // 2 hour ago

        cal.setTime(start);
        cal.add(java.util.Calendar.HOUR_OF_DAY, -3);
        c3.setLastDate(cal.getTime()); // 3 hours ago

        cal.setTime(start);
        cal.set(java.util.Calendar.DAY_OF_MONTH, -2);
        c4.setLastDate(cal.getTime()); // 2 months ago.

        // the last car to be evaluated
        cal.setTime(start);
        cal.add(java.util.Calendar.HOUR_OF_DAY, -1);
        c5.setLastDate(cal.getTime()); // 1 hour ago.

        // the first car to be evaluated
        cal.setTime(start);
        cal.add(java.util.Calendar.YEAR, -1);
        c6.setLastDate(cal.getTime()); // one year ago.

        // put Acton yard track into FIFO mode
        actonYard1.setServiceOrder(Track.FIFO);

        // test car bypass on FIFO track
        c4.setTypeName("boxcar"); // lower case "boxcar" not serviced by any track
        train1.addTypeName("boxcar");

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination, order cars were evaluated
        Assert.assertEquals("car destination", chelmsfordSpur1, c6.getDestinationTrack());
        Assert.assertEquals("car destination", null, c4.getDestinationTrack());
        Assert.assertEquals("car destination", bostonSpur1, c1.getDestinationTrack());
        Assert.assertEquals("car destination", chelmsfordSpur1, c3.getDestinationTrack());
        Assert.assertEquals("car destination", chelmsfordSpur1, c2.getDestinationTrack());
        Assert.assertEquals("car destination", bostonSpur1, c5.getDestinationTrack());
    }

    /**
     * Test car with destination into staging
     */
    @Test
    public void testCarDestinationsStaging() {

        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford-WestfordStaging");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        // create staging
        Location westford = lmanager.newLocation("Westford Staging");
        westford.setLocationOps(Location.STAGING);
        Track westfordStaging1 = westford.addTrack("Staging 1", Track.STAGING);
        westfordStaging1.setLength(1000);
        Track westfordStaging2 = westford.addTrack("Staging 2", Track.STAGING);
        westfordStaging2.setLength(1000);
        westfordStaging2.setMoves(100); // don't choose this track

        route.addLocation(westford);

        // place car at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);

        // give the car a destination that's the terminal, but the wrong track in staging
        Assert.assertEquals("set car destination", Track.OKAY, c1.setDestination(westford, westfordStaging2));

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 assigned to train", train1, c1.getTrain());
        Assert.assertEquals("c1 destination", westfordStaging1, c1.getDestinationTrack());
    }

    /**
     * Test spurs with schedules, this test uses the car's default load of
     * empty. Cars are not routed to the spurs but found by the location by
     * location search. Spurs with schedule are given a higher priority over
     * tracks that don't have schedules.
     */
    @Test
    public void testSpursWithSchedules() {

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);
        Track actonYard2 = acton.getTrackByName("Acton Yard 2", null);

        Location boston = route.getRouteLocationBySequenceNumber(2).getLocation();
        Track bostonSpur1 = boston.getTrackByName("Boston Spur 1", null);
        Track bostonYard2 = boston.getTrackByName("Boston Yard 2", Track.YARD); // alternate
        bostonSpur1.setAlternateTrack(bostonYard2); // shouldn't be used in this test

        // note that the alternate track Boston yard 2 will not be used for cars with default loads, and the spur has a schedule

        Location chelmsford = route.getRouteLocationBySequenceNumber(3).getLocation();
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);

        // create schedules
        Schedule sch1 = smanager.newSchedule("Schedule 1");
        ScheduleItem sch1Item1 = sch1.addItem("Boxcar");
        sch1Item1.setShipLoadName("Toys");
        ScheduleItem sch1Item2 = sch1.addItem("Flat");
        sch1Item2.setShipLoadName("Boxes");
        ScheduleItem sch1Item3 = sch1.addItem("Boxcar");
        sch1Item3.setShipLoadName("Junk");
        ScheduleItem sch1Item4 = sch1.addItem("Flat");
        sch1Item4.setShipLoadName("Metal");
        bostonSpur1.setSchedule(sch1);

        // Set up two cabooses and six box cars, two with FRED       
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "32", actonYard1, 10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", Bundle.getMessage("Caboose"), "32", actonYard1, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", actonYard1, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", actonYard1, 13);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", "Boxcar", "40", actonYard1, 14);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", "Boxcar", "40", actonYard2, 15);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", "Boxcar", "50", actonYard2, 16);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", "Boxcar", "60", actonYard2, 17);
        Car c9 = JUnitOperationsUtil.createAndPlaceCar("CP", "90", "Flat", "40", actonYard2, 18);
        Car c10 = JUnitOperationsUtil.createAndPlaceCar("CP", "100", "Flat", "40", actonYard2, 19);

        c1.setCaboose(true);
        c2.setCaboose(true);

        // define the train
        Train train1 = tmanager.newTrain("TestSpursWithSchedules1");
        train1.setRoute(route);

        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", bostonSpur1, c3.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonSpur1, c5.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonSpur1, c6.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonSpur1, c8.getDestinationTrack());

        Assert.assertEquals("Car destination", chelmsfordSpur1, c4.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c7.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c9.getDestinationTrack());

        Assert.assertEquals("Car destination", null, c10.getDestinationTrack()); // max of 7 pulls from Acton
    }

    /**
     * Test car with final destination alternate, but spur and alternate track
     * directions arn't compatible.
     */
    @Test
    public void testAlternateTrackDirections() {

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);

        RouteLocation rlBoston1 = route.getRouteLocationBySequenceNumber(2);

        Location boston = rlBoston1.getLocation();
        Track bostonSpur2 = boston.getTrackByName("Boston Spur 2", Track.SPUR);
        Track bostonYard2 = boston.getTrackByName("Boston Yard 2", Track.YARD); // alternate

        // provide an alternate track for the spur
        bostonSpur2.setAlternateTrack(bostonYard2);

        // increase test code coverage by adding a schedule in sequential mode to the spur
        Schedule sch = smanager.newSchedule("Test schedule sequential");
        bostonSpur2.setSchedule(sch);
        bostonSpur2.setScheduleMode(Track.SEQUENTIAL);

        // confirm
        Assert.assertEquals("track is an alternate", bostonYard2, bostonSpur2.getAlternateTrack());
        Assert.assertEquals("track is an alternate", true, bostonYard2.isAlternate());

        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", actonYard1, 12);

        // send c3 to alternate track
        c3.setDestination(boston);
        c3.setDestinationTrack(bostonYard2);

        // set final destination Boston spur
        c3.setFinalDestination(boston);
        c3.setFinalDestinationTrack(bostonSpur2);

        // define the train
        Train train1 = tmanager.newTrain("TestAlternateTrack1");
        train1.setRoute(route);

        // now configure alternate and spur so local move not possible
        bostonSpur2.setTrainDirections(Track.SOUTH);
        bostonYard2.setTrainDirections(Track.NORTH);

        // should build, but c3 should not be assigned to train
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // destination to alternate track not acceptable
        Assert.assertEquals("Train assignment", null, c3.getTrain());
        Assert.assertEquals("Destination", null, c3.getDestination());
        Assert.assertEquals("Final destination", bostonSpur2, c3.getFinalDestinationTrack());
    }

    /**
     * test sending a car to a destination that isn't available
     */
    @Test
    public void testCarDestination() {

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);

        RouteLocation rlBoston1 = route.getRouteLocationBySequenceNumber(2);
        RouteLocation rlBoston2 = route.getRouteLocationBySequenceNumber(5);

        Location boston = rlBoston1.getLocation();
        Track bostonSpur2 = boston.getTrackByName("Boston Spur 2", Track.SPUR);

        // increase test code coverage by adding a schedule in sequential mode to the spur
        Schedule sch = smanager.newSchedule("Test schedule sequential");
        bostonSpur2.setSchedule(sch);
        bostonSpur2.setScheduleMode(Track.SEQUENTIAL);

        // confirm
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", actonYard1, 12);

        // send c3 to spur with schedule
        c3.setDestination(boston);
        c3.setDestinationTrack(bostonSpur2);

        // define the train
        Train train1 = tmanager.newTrain("TestAlternateTrack1");
        train1.setRoute(route);

        // train departs North bound
        bostonSpur2.setTrainDirections(Track.SOUTH);
        // and returns South bound, prevent set out
        rlBoston2.setDropAllowed(false); // no set outs

        // should build, but c3 should not be assigned to train
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // Confirm c3 destination has been removed
        Assert.assertEquals("Train assignment", null, c3.getTrain());
        Assert.assertEquals("Destination", null, c3.getDestination());
    }

    /**
     * test routeLocation move count. Train is a turn. Places 4 cars at Boston,
     * normally all pulled on the return to Acton.
     */
    @Test
    public void testRouteMoveCount() {

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        RouteLocation rlBoston1 = route.getRouteLocationBySequenceNumber(2);
        RouteLocation rlBoston2 = route.getRouteLocationBySequenceNumber(5);

        Location boston = rlBoston1.getLocation();
        Track bostonYard2 = boston.getTrackByName("Boston Yard 2", Track.YARD);

        Location chelmsford = route.getRouteLocationBySequenceNumber(3).getLocation();
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);

        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", bostonYard2, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", bostonYard2, 1);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", "Boxcar", "40", bostonYard2, 2);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", "Boxcar", "40", bostonYard2, 3);

        // define the train
        Train train1 = tmanager.newTrain("TestRouteMoveCount");
        train1.setRoute(route);

        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // all four cars are going to Acton
        Assert.assertEquals("car destination", actonSpur1, c3.getDestinationTrack());
        Assert.assertEquals("car destination", actonSpur1, c4.getDestinationTrack());
        Assert.assertEquals("car destination", actonSpur1, c5.getDestinationTrack());
        Assert.assertEquals("car destination", actonSpur1, c6.getDestinationTrack());

        // confirm where in the route the cars are pulled
        Assert.assertEquals("car pulled", rlBoston2, c3.getRouteLocation());
        Assert.assertEquals("car pulled", rlBoston2, c4.getRouteLocation());
        Assert.assertEquals("car pulled", rlBoston2, c5.getRouteLocation());
        Assert.assertEquals("car pulled", rlBoston2, c6.getRouteLocation());

        // now prevent the 2nd pull from Boston
        rlBoston2.setMaxCarMoves(0);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // all four cars are going to Acton
        Assert.assertEquals("car destination", actonSpur1, c3.getDestinationTrack());
        Assert.assertEquals("car destination", chelmsfordSpur1, c4.getDestinationTrack());
        Assert.assertEquals("car destination", actonSpur1, c5.getDestinationTrack());
        Assert.assertEquals("car destination", actonSpur1, c6.getDestinationTrack());

        // confirm where in the route the cars are pulled
        Assert.assertEquals("car pulled", rlBoston1, c3.getRouteLocation());
        Assert.assertEquals("car pulled", rlBoston1, c4.getRouteLocation());
        Assert.assertEquals("car pulled", rlBoston1, c5.getRouteLocation());
        Assert.assertEquals("car pulled", rlBoston1, c6.getRouteLocation());
    }

    /**
     * Test alternate track by restricting the spur and alternate track train
     * directions that can service them.
     */
    @Test
    public void testAlternateTrackTrainDirections() {

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);
        Track actonYard2 = acton.getTrackByName("Acton Yard 2", null);

        RouteLocation rlBoston1 = route.getRouteLocationBySequenceNumber(2);
        RouteLocation rlBoston2 = route.getRouteLocationBySequenceNumber(5);

        Location boston = rlBoston1.getLocation();
        Track bostonSpur1 = boston.getTrackByName("Boston Spur 1", Track.SPUR); // delete this track
        Track bostonSpur2 = boston.getTrackByName("Boston Spur 2", Track.SPUR);
        Track bostonYard1 = boston.getTrackByName("Boston Yard 1", Track.YARD); // delete this track
        Track bostonYard2 = boston.getTrackByName("Boston Yard 2", Track.YARD); // alternate
        Track bostonInterchange1 = boston.getTrackByName("Boston Interchange 1", Track.INTERCHANGE); // delete this track
        Track bostonInterchange2 = boston.getTrackByName("Boston Interchange 2", Track.INTERCHANGE); // delete this track

        // only use one spur and yard track
        boston.deleteTrack(bostonSpur1);
        boston.deleteTrack(bostonYard1);
        boston.deleteTrack(bostonInterchange1);
        boston.deleteTrack(bostonInterchange2);

        // limit the length of the spur to one boxcar 
        bostonSpur2.setLength(50);

        // provide an alternate track for the spur
        bostonSpur2.setAlternateTrack(bostonYard2);

        // confirm
        Assert.assertEquals("track is an alternate", bostonYard2, bostonSpur2.getAlternateTrack());
        Assert.assertEquals("track is an alternate", true, bostonYard2.isAlternate());

        Location chelmsford = route.getRouteLocationBySequenceNumber(3).getLocation();
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);
        Track chelmsfordSpur2 = chelmsford.getTrackByName("Chelmsford Spur 2", null);
        chelmsford.deleteTrack(chelmsfordSpur2);

        // Set up two cabooses and 8 boxcars, two with FRED       
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "32", actonYard1, 10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", Bundle.getMessage("Caboose"), "32", actonYard1, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", actonYard1, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", actonYard1, 23);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", "Boxcar", "40", actonYard1, 34);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", "Boxcar", "40", actonYard2, 45);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", "Boxcar", "40", actonYard2, 56);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", "Boxcar", "40", actonYard2, 67);
        Car c9 = JUnitOperationsUtil.createAndPlaceCar("CP", "90", "Flat", "40", actonYard2, 78);
        Car c10 = JUnitOperationsUtil.createAndPlaceCar("CP", "100", "Flat", "40", actonYard2, 89);

        c1.setCaboose(true);
        c2.setCaboose(true);
        c3.setFred(true);
        c4.setFred(true);

        // define the train
        Train train1 = tmanager.newTrain("TestAlternateTrack1");
        train1.setRoute(route);

        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        Assert.assertEquals("should be 6 cars", 6, train1.getNumberCarsWorked());

        // confirm car destinations
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c3.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", null, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", bostonSpur2, c5.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonYard2, c7.getDestinationTrack()); // alternate
        Assert.assertEquals("Car destination", bostonYard2, c9.getDestinationTrack()); // alternate

        Assert.assertEquals("Car destination", chelmsfordSpur1, c6.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c8.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c10.getDestinationTrack());

        // check that cars to the yard have a final destination Boston spur 2
        Assert.assertEquals("Car final destination", bostonSpur2, c7.getFinalDestinationTrack());
        Assert.assertEquals("Car final destination", bostonSpur2, c9.getFinalDestinationTrack());

        // train departs north bound for the first stop at Boston
        bostonYard2.setTrainDirections(Track.SOUTH);
        bostonSpur2.setTrainDirections(Track.SOUTH);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        Assert.assertEquals("should be 6 cars", 6, train1.getNumberCarsWorked());

        // confirm car destinations
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c3.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", null, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", bostonSpur2, c5.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c6.getDestinationTrack()); // alternate
        Assert.assertEquals("Car destination", chelmsfordSpur1, c8.getDestinationTrack()); // alternate
        Assert.assertEquals("Car destination", chelmsfordSpur1, c10.getDestinationTrack()); // alternate       

        Assert.assertEquals("Car destination", bostonYard2, c7.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonYard2, c9.getDestinationTrack());

        // check that cars to the yard have a final destination Boston spur 2
        Assert.assertEquals("Car final destination", bostonSpur2, c7.getFinalDestinationTrack());
        Assert.assertEquals("Car final destination", bostonSpur2, c9.getFinalDestinationTrack());

        // confirm where in the route cars are set out, all South bound
        Assert.assertEquals("Car route destination", rlBoston2, c7.getRouteDestination());
        Assert.assertEquals("Car route destination", rlBoston2, c9.getRouteDestination());
        Assert.assertEquals("Car route destination", rlBoston2, c5.getRouteDestination());

        // train departs north bound for the first stop at Boston
        bostonYard2.setTrainDirections(Track.NORTH);
        bostonSpur2.setTrainDirections(Track.NORTH);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        Assert.assertEquals("should be 6 cars", 6, train1.getNumberCarsWorked());

        // confirm car destinations
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c3.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", null, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", chelmsfordSpur1, c5.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonSpur2, c6.getDestinationTrack()); // alternate
        Assert.assertEquals("Car destination", bostonYard2, c8.getDestinationTrack()); // alternate
        Assert.assertEquals("Car destination", bostonYard2, c10.getDestinationTrack()); // alternate       

        Assert.assertEquals("Car destination", chelmsfordSpur1, c7.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c9.getDestinationTrack());

        // check that cars to the yard have a final destination Boston spur 2
        Assert.assertEquals("Car destination", bostonSpur2, c8.getFinalDestinationTrack());
        Assert.assertEquals("Car destination", bostonSpur2, c10.getFinalDestinationTrack());

        // confirm where in the route cars are set out, all North bound
        Assert.assertEquals("Car route destination", rlBoston1, c8.getRouteDestination());
        Assert.assertEquals("Car route destination", rlBoston1, c10.getRouteDestination());

        Assert.assertEquals("Car route destination", rlBoston1, c6.getRouteDestination());

        // train departs north bound for the first stop at Boston
        // program correctly uses 1st stop at Boston to drop cars off to alternate track
        bostonYard2.setTrainDirections(Track.NORTH);
        bostonSpur2.setTrainDirections(Track.SOUTH);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        Assert.assertEquals("should be 6 cars", 6, train1.getNumberCarsWorked());

        // confirm car destinations
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c3.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", null, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", bostonSpur2, c5.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonYard2, c6.getDestinationTrack()); // alternate
        Assert.assertEquals("Car destination", bostonYard2, c8.getDestinationTrack()); // alternate
        Assert.assertEquals("Car destination", bostonYard2, c10.getDestinationTrack()); // alternate       

        Assert.assertEquals("Car destination", chelmsfordSpur1, c7.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c9.getDestinationTrack());

        // check that cars to the yard have a final destination Boston spur 2
        Assert.assertEquals("Car final destination", bostonSpur2, c6.getFinalDestinationTrack());
        Assert.assertEquals("Car final destination", bostonSpur2, c8.getFinalDestinationTrack());
        Assert.assertEquals("Car final destination", bostonSpur2, c10.getFinalDestinationTrack());

        // confirm where in the route cars are set out, should be the first stop Boston for alternate yard
        Assert.assertEquals("Car route destination", rlBoston1, c6.getRouteDestination()); // North bound
        Assert.assertEquals("Car route destination", rlBoston1, c8.getRouteDestination());
        Assert.assertEquals("Car route destination", rlBoston1, c10.getRouteDestination());

        Assert.assertEquals("Car route destination", rlBoston2, c5.getRouteDestination()); // South bound

        // TODO this order should be a problem, program "fixed" issue by dropping cars to alternate north bound
        // Alternate tack yard2 shouldn't be serviced by the train due to train direction
        // The reverse works correctly, see above.
        bostonYard2.setTrainDirections(Track.SOUTH);
        bostonSpur2.setTrainDirections(Track.NORTH);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c3.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", null, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", chelmsfordSpur1, c5.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonSpur2, c6.getDestinationTrack()); // alternate
        Assert.assertEquals("Car destination", bostonYard2, c8.getDestinationTrack()); // alternate
        Assert.assertEquals("Car destination", bostonYard2, c10.getDestinationTrack()); // alternate       

        Assert.assertEquals("Car destination", chelmsfordSpur1, c7.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c9.getDestinationTrack());

        // check that cars to the yard have a final destination Boston spur 2
        Assert.assertEquals("Car final destination", bostonSpur2, c8.getFinalDestinationTrack());
        Assert.assertEquals("Car final destination", bostonSpur2, c10.getFinalDestinationTrack());

        // the yard tracks should only be serviced by South bound train
        Assert.assertEquals("Car route destination", rlBoston1, c6.getRouteDestination()); // North bound
        Assert.assertEquals("Car route destination", rlBoston1, c8.getRouteDestination());
        Assert.assertEquals("Car route destination", rlBoston1, c10.getRouteDestination());
    }

    /**
     * Test alternate track configured as an interchange track
     */
    @Test
    public void testAlternateTrackInterchange() {

        // custom load for testing
        cld.addName("Boxcar", "Bolts");

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);
        Track actonYard2 = acton.getTrackByName("Acton Yard 2", null);

        RouteLocation rlBoston1 = route.getRouteLocationBySequenceNumber(2);

        Location boston = rlBoston1.getLocation();
        Track bostonSpur1 = boston.getTrackByName("Boston Spur 1", Track.SPUR); // delete this track
        Track bostonSpur2 = boston.getTrackByName("Boston Spur 2", Track.SPUR);
        Track bostonYard1 = boston.getTrackByName("Boston Yard 1", Track.YARD); // delete this track
        Track bostonYard2 = boston.getTrackByName("Boston Yard 2", Track.YARD); // delete this track
        Track bostonInterchange1 = boston.getTrackByName("Boston Interchange 1", Track.INTERCHANGE); // delete this track
        Track bostonInterchange2 = boston.getTrackByName("Boston Interchange 2", Track.INTERCHANGE); // alternate

        // only use one spur and interchange track
        boston.deleteTrack(bostonSpur1);
        boston.deleteTrack(bostonYard1);
        boston.deleteTrack(bostonYard2);
        boston.deleteTrack(bostonInterchange1);

        // limit the length of the spur to one boxcar 
        bostonSpur2.setLength(50);

        // provide an alternate track for the spur
        bostonSpur2.setAlternateTrack(bostonInterchange2);

        // set option to hold cars with custom load
        bostonSpur2.setHoldCarsWithCustomLoadsEnabled(true);

        // confirm
        Assert.assertEquals("track is an alternate", bostonInterchange2, bostonSpur2.getAlternateTrack());
        Assert.assertEquals("track is an alternate", true, bostonInterchange2.isAlternate());

        Location chelmsford = route.getRouteLocationBySequenceNumber(3).getLocation();
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);
        Track chelmsfordSpur2 = chelmsford.getTrackByName("Chelmsford Spur 2", null);
        chelmsford.deleteTrack(chelmsfordSpur2);

        // Set up two cabooses and 8 boxcars, two with FRED       
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "32", actonYard1, 10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", Bundle.getMessage("Caboose"), "32", actonYard1, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", actonYard1, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", actonYard1, 23);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", "Boxcar", "40", actonYard1, 34);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", "Boxcar", "40", actonYard2, 45);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", "Boxcar", "40", actonYard2, 56);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", "Boxcar", "40", actonYard2, 67);
        Car c9 = JUnitOperationsUtil.createAndPlaceCar("CP", "90", "Flat", "40", actonYard2, 78);
        Car c10 = JUnitOperationsUtil.createAndPlaceCar("CP", "100", "Flat", "40", actonYard2, 89);

        c1.setCaboose(true);
        c2.setCaboose(true);
        c3.setFred(true);
        c4.setFred(true);

        // define the train
        Train train1 = tmanager.newTrain("TestAlternateTrack1");
        train1.setRoute(route);

        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        Assert.assertEquals("should be 6 cars", 6, train1.getNumberCarsWorked());

        // confirm car destinations
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c3.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", null, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", bostonSpur2, c5.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonInterchange2, c7.getDestinationTrack()); // alternate
        Assert.assertEquals("Car destination", bostonInterchange2, c9.getDestinationTrack()); // alternate

        Assert.assertEquals("Car destination", chelmsfordSpur1, c6.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c8.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c10.getDestinationTrack());

        // check that cars to the interchange have a final destination Boston spur 2
        Assert.assertEquals("Car final destination", bostonSpur2, c7.getFinalDestinationTrack());
        Assert.assertEquals("Car final destination", bostonSpur2, c9.getFinalDestinationTrack());

        // now restrict which trains can use the alternate interchange track
        bostonInterchange2.setDropOption(Track.EXCLUDE_TRAINS);
        bostonInterchange2.addDropId(train1.getId());

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        Assert.assertEquals("should be 6 cars", 6, train1.getNumberCarsWorked());

        // confirm car destinations
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c3.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", null, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", bostonSpur2, c5.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c7.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c9.getDestinationTrack());

        Assert.assertEquals("Car destination", chelmsfordSpur1, c6.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c8.getDestinationTrack());

        // now try sending cars with custom load to spur
        c5.setLoadName("Bolts");
        c6.setLoadName("Bolts");
        c7.setLoadName("Bolts");
        c8.setLoadName("Bolts");

        // create schedule requesting Boxcar with load "Bolts"
        Schedule schedule = smanager.newSchedule("test alternate");
        ScheduleItem si = schedule.addItem("Boxcar");
        si.setReceiveLoadName("Bolts");
        bostonSpur2.setSchedule(schedule);

        // program will try to send all 4 cars to spur, but only one can fit, alternate interchange can not service train
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // confirm car destinations, alternate track interchange not available
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c3.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", null, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", bostonSpur2, c5.getDestinationTrack()); // load = "Bolts"
        Assert.assertEquals("Car destination", null, c6.getDestinationTrack()); // load = "Bolts"
        Assert.assertEquals("Car destination", null, c7.getDestinationTrack()); // load = "Bolts"
        Assert.assertEquals("Car destination", null, c8.getDestinationTrack()); // load = "Bolts"

        Assert.assertEquals("Car destination", chelmsfordSpur1, c9.getDestinationTrack());

        // allow train to use interchange track
        bostonInterchange2.deleteDropId(train1.getId());

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // confirm car destinations, alternate track interchange not available
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c3.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", null, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", bostonSpur2, c5.getDestinationTrack()); // load = "Bolts"
        Assert.assertEquals("Car destination", bostonInterchange2, c6.getDestinationTrack()); // load = "Bolts"
        Assert.assertEquals("Car destination", bostonInterchange2, c7.getDestinationTrack()); // load = "Bolts"
        Assert.assertEquals("Car destination", bostonInterchange2, c8.getDestinationTrack()); // load = "Bolts"

        Assert.assertEquals("Car destination", chelmsfordSpur1, c9.getDestinationTrack());

        // limit alternate track to one car
        bostonInterchange2.setLength(50);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // confirm car destinations, alternate track interchange not available
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c3.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", null, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", bostonSpur2, c5.getDestinationTrack()); // load = "Bolts"
        Assert.assertEquals("Car destination", bostonInterchange2, c6.getDestinationTrack()); // load = "Bolts"
        Assert.assertEquals("Car destination", null, c7.getDestinationTrack()); // load = "Bolts"
        Assert.assertEquals("Car destination", null, c8.getDestinationTrack()); // load = "Bolts"

        Assert.assertEquals("Car destination", chelmsfordSpur1, c9.getDestinationTrack());
    }

    /**
     * Test alternate track aggressive mode. Checks to see if cars are
     * redirected from alternate track to spur.
     */
    @Test
    public void testAlternateTrackAggressiveMode() {

        Setup.setBuildAggressive(true);
        Setup.setCarMoves(8); // allow up to 8 moves

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        RouteLocation rlActon = route.getDepartsRouteLocation();
        // only allow 6 cars to depart, 2 are in a kernel
        rlActon.setMaxCarMoves(5); // causes one car to be stranded at Acton

        Location acton = rlActon.getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);
        Track actonYard2 = acton.getTrackByName("Acton Yard 2", null);

        RouteLocation rlBoston = route.getRouteLocationBySequenceNumber(5);
        // only allow 4 car to be pulled
        rlBoston.setMaxCarMoves(4); // causes one car to be stranded at Boston

        Location boston = rlBoston.getLocation();
        Track bostonSpur1 = boston.getTrackByName("Boston Spur 1", Track.SPUR); // delete this track
        Track bostonSpur2 = boston.getTrackByName("Boston Spur 2", Track.SPUR);
        Track bostonYard1 = boston.getTrackByName("Boston Yard 1", Track.YARD); // delete this track
        Track bostonYard2 = boston.getTrackByName("Boston Yard 2", Track.YARD); // alternate
        Track bostonInterchange1 = boston.getTrackByName("Boston Interchange 1", Track.INTERCHANGE); // delete this track
        Track bostonInterchange2 = boston.getTrackByName("Boston Interchange 2", Track.INTERCHANGE); // delete this track

        // only use one spur and yard track
        boston.deleteTrack(bostonSpur1);
        boston.deleteTrack(bostonYard1);
        boston.deleteTrack(bostonInterchange1);
        boston.deleteTrack(bostonInterchange2);

        // limit the length of the spur to four boxcars 
        bostonSpur2.setLength(200);

        // provide an alternate track for the spur
        bostonSpur2.setAlternateTrack(bostonYard2);

        // confirm
        Assert.assertEquals("track is an alternate", bostonYard2, bostonSpur2.getAlternateTrack());
        Assert.assertEquals("track is an alternate", true, bostonYard2.isAlternate());

        RouteLocation chelmsford1 = route.getRouteLocationBySequenceNumber(3);
        chelmsford1.setDropAllowed(false); // no set outs allowed
        RouteLocation chelmsford2 = route.getRouteLocationBySequenceNumber(4); // train reverses direction at Chelmsford
        chelmsford2.setDropAllowed(false); // no set outs allowed

        Location chelmsford = chelmsford1.getLocation();
        Track chelmsfordYard1 = chelmsford.getTrackByName("Chelmsford Yard 1", null);

        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", "Boxcar", "40", actonYard1, 10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", "Boxcar", "40", actonYard1, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", actonYard1, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", actonYard1, 13);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", "Boxcar", "40", actonYard2, 14);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", "Boxcar", "40", actonYard2, 15);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", "Boxcar", "40", bostonSpur2, 16);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", "Boxcar", "40", bostonSpur2, 17);
        Car c9 = JUnitOperationsUtil.createAndPlaceCar("CP", "90", "Boxcar", "40", bostonSpur2, 18);
        Car c10 = JUnitOperationsUtil.createAndPlaceCar("CP", "100", "Boxcar", "40", bostonSpur2, 19);

        // extra car for code coverage
        Car c11 = JUnitOperationsUtil.createAndPlaceCar("CP", "110", "Boxcar", "40", actonYard2, 20);
        Car c12 = JUnitOperationsUtil.createAndPlaceCar("CP", "120", "Boxcar", "40", bostonYard2, 21);

        // increase code coverage by using kernels
        Kernel k1 = cmanager.newKernel("2 cars");
        c3.setKernel(k1);
        c4.setKernel(k1);

        // increase code coverage by having a car with a final destination
        c7.setFinalDestination(chelmsford); // not reachable
        c7.setFinalDestinationTrack(chelmsfordYard1);

        // define the train
        Train train1 = tmanager.newTrain("TestAlternateTrack1");
        train1.setRoute(route);

        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        Assert.assertEquals("number of cars serviced by train", 10, train1.getNumberCarsWorked());

        // confirm car destinations
        Assert.assertEquals("Car destination", bostonSpur2, c1.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonSpur2, c2.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonSpur2, c3.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonSpur2, c4.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonYard2, c5.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonYard2, c6.getDestinationTrack());

        Assert.assertEquals("Car destination", actonSpur1, c7.getDestinationTrack());
        Assert.assertEquals("Car destination", actonSpur1, c8.getDestinationTrack());
        Assert.assertEquals("Car destination", actonSpur1, c9.getDestinationTrack());
        Assert.assertEquals("Car destination", actonSpur1, c10.getDestinationTrack());

        // There two cars are stranded, increases code coverage
        Assert.assertEquals("Car destination", null, c11.getDestinationTrack());
        Assert.assertEquals("Car destination", null, c12.getDestinationTrack());

        // check that cars in yard have a final destination Boston spur 2
        Assert.assertEquals("Car destination", bostonSpur2, c5.getFinalDestinationTrack());
        Assert.assertEquals("Car destination", bostonSpur2, c6.getFinalDestinationTrack());
    }

    /**
     * Test cars out of service and location unknown.
     */
    @Test
    public void testCarOutOfService() {

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        // train requires a caboose for this test
        Train train1 = tmanager.newTrain("TestCarOutOfService");
        train1.setRoute(route);
        train1.setRequirements(Train.CABOOSE);

        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);

        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", Bundle.getMessage("Caboose"), "32", actonYard1, 11);
        c2.setCaboose(true);

        // should build there's a caboose available at departure
        new TrainBuilder().build(train1);
        Assert.assertTrue("required car is available", train1.isBuilt());

        // test car out of service
        c2.setOutOfService(true);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("required car is out of service", train1.isBuilt());

        // test location unknown
        c2.setOutOfService(false);
        c2.setLocationUnknown(true);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("required car location is unknown", train1.isBuilt());

        c2.setLocationUnknown(false);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("required car is available", train1.isBuilt());
    }

    /**
     * Test cars wait feature.
     */
    @Test
    public void testCarWait() {

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        // train requires a caboose for this test
        Train train1 = tmanager.newTrain("TestCarOutOfService");
        train1.setRoute(route);
        train1.setRequirements(Train.CABOOSE);

        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);

        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", Bundle.getMessage("Caboose"), "32", actonYard1, 11);
        c2.setCaboose(true);

        // should build there's a caboose available at departure
        new TrainBuilder().build(train1);
        Assert.assertTrue("required car is available", train1.isBuilt());

        // now wait the caboose, should fail
        c2.setWait(1);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("required car will wait for next train", train1.isBuilt());
        Assert.assertEquals("car train assignment", null, c2.getTrain());
        Assert.assertEquals("car destination", "", c2.getDestinationName());

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("next train!", train1.isBuilt());
        Assert.assertEquals("car train assignment", train1, c2.getTrain());
        Assert.assertEquals("car destination", "Acton", c2.getDestinationName());
    }

    @Test
    public void testCarScheduledPickup() {

        TrainScheduleManager trainScheduleManager = InstanceManager.getDefault(TrainScheduleManager.class);

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);
        Track actonYard2 = acton.getTrackByName("Acton Yard 2", null);

        Location boston = route.getRouteLocationBySequenceNumber(2).getLocation();
        Track bostonSpur1 = boston.getTrackByName("Boston Spur 1", Track.SPUR); // delete this track

        Location chelmsford = route.getRouteLocationBySequenceNumber(3).getLocation();
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);

        // Set up two cabooses and six box cars, two with FRED       
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "32", actonYard1, 10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", Bundle.getMessage("Caboose"), "32", actonYard1, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", actonYard1, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", actonYard1, 13);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", "Boxcar", "40", actonYard1, 14);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", "Boxcar", "40", actonYard2, 15);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", "Boxcar", "50", actonYard2, 16);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", "Boxcar", "60", actonYard2, 17);
        Car c9 = JUnitOperationsUtil.createAndPlaceCar("CP", "90", "Flat", "40", actonYard2, 18);
        Car c10 = JUnitOperationsUtil.createAndPlaceCar("CP", "100", "Flat", "40", actonYard2, 19);

        c1.setCaboose(true);
        c2.setCaboose(true);
        c3.setFred(true);
        c4.setFred(true);

        List<TrainSchedule> schedules = trainScheduleManager.getSchedulesByNameList();

        // set 4 cars to be pull on Friday
        c1.setPickupScheduleId(schedules.get(0).getId());
        c3.setPickupScheduleId(schedules.get(0).getId());
        c5.setPickupScheduleId(schedules.get(0).getId());
        c6.setPickupScheduleId(schedules.get(0).getId());

        // set 2 cars to be pull on Monday
        c7.setPickupScheduleId(schedules.get(1).getId());
        c8.setPickupScheduleId(schedules.get(1).getId());

        Assert.assertEquals("Active schedule", "", trainScheduleManager.getTrainScheduleActiveId());

        // define the train
        Train train1 = tmanager.newTrain("TestCarScheduledPickup1");
        train1.setRoute(route);
        train1.setRequirements(Train.CABOOSE + Train.FRED); // request both caboose and car with FRED

        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        Assert.assertEquals("number of cars", 4, train1.getNumberCarsWorked());

        // confirm car destinations
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose pickup on Friday
        Assert.assertEquals("Car destination", actonSpur1, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c3.getDestinationTrack()); // car with FRED pickup on Friday
        Assert.assertEquals("Car destination", actonSpur1, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", null, c5.getDestinationTrack()); // pickup on Friday
        Assert.assertEquals("Car destination", null, c6.getDestinationTrack()); // pickup on Friday
        Assert.assertEquals("Car destination", null, c7.getDestinationTrack()); // pickup on Monday
        Assert.assertEquals("Car destination", null, c8.getDestinationTrack()); // pickup on Monday
        Assert.assertEquals("Car destination", bostonSpur1, c9.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c10.getDestinationTrack());

        // now set the day to Monday
        Assert.assertEquals("Train schedule day", "Monday", schedules.get(1).getName());
        trainScheduleManager.setTrainScheduleActiveId(schedules.get(1).getId());
        Assert.assertEquals("Active schedule", "2", trainScheduleManager.getTrainScheduleActiveId());

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("Car destination", null, c1.getDestinationTrack()); // caboose pickup on Friday
        Assert.assertEquals("Car destination", actonSpur1, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", null, c3.getDestinationTrack()); // car with FRED pickup on Friday
        Assert.assertEquals("Car destination", actonSpur1, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", null, c5.getDestinationTrack()); // pickup on Friday
        Assert.assertEquals("Car destination", null, c6.getDestinationTrack()); // pickup on Friday
        Assert.assertEquals("Car destination", bostonSpur1, c7.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c8.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonSpur1, c9.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c10.getDestinationTrack());

        // restore 2 cars to be pull on Monday
        c7.setPickupScheduleId(schedules.get(1).getId());
        c8.setPickupScheduleId(schedules.get(1).getId());

        // change pick up day to any day of the week
        trainScheduleManager.setTrainScheduleActiveId(TrainSchedule.ANY);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("Car destination", actonSpur1, c1.getDestinationTrack()); // caboose pickup on Friday
        Assert.assertEquals("Car destination", null, c2.getDestinationTrack()); // caboose
        Assert.assertEquals("Car destination", actonSpur1, c3.getDestinationTrack()); // car with FRED pickup on Friday
        Assert.assertEquals("Car destination", null, c4.getDestinationTrack()); // car with FRED
        Assert.assertEquals("Car destination", bostonSpur1, c5.getDestinationTrack()); // pickup on Friday
        Assert.assertEquals("Car destination", chelmsfordSpur1, c6.getDestinationTrack()); // pickup on Friday
        Assert.assertEquals("Car destination", bostonSpur1, c7.getDestinationTrack());
        Assert.assertEquals("Car destination", chelmsfordSpur1, c8.getDestinationTrack());
        Assert.assertEquals("Car destination", bostonSpur1, c9.getDestinationTrack());
        Assert.assertEquals("Car destination", null, c10.getDestinationTrack()); // limit of 7 pulls out of Acton
    }

    /**
     * Test cars assigned to a kernel.
     */
    @Test
    public void testCarKernels() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        // define the train
        Train train1 = tmanager.newTrain("TestCarKernels1");
        train1.setRoute(route);

        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);
        Track actonYard2 = acton.getTrackByName("Acton Yard 2", null);

        // Set up 4 cars, two in a kernel      
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", carTypes[1], "40", actonYard1, 12);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", carTypes[1], "40", actonYard2, 15);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", carTypes[1], "50", actonYard2, 16);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", carTypes[1], "60", actonYard2, 17);

        Kernel k1 = cmanager.newKernel("group of 3 cars");
        c8.setKernel(k1); // lead car
        c7.setKernel(k1); // on track Yard 2
        c3.setKernel(k1); // on track Yard 1

        new TrainBuilder().build(train1);
        Assert.assertFalse("train status", train1.isBuilt());

        train1.reset();

        // now build a kernel all on the same track
        c3.setKernel(null);
        c6.setKernel(k1); // on track Yard 2

        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // TODO test kernels with cabooses and cars with FRED
    }

    /**
     * Tests car already assigned to a train and has been assigned a route
     * location pickup and drop
     */
    @Test
    public void testTrainReset() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        // define the train
        Train train1 = tmanager.newTrain("TestTrainReset1");
        train1.setRoute(route);

        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);

        RouteLocation rlActon2 = route.getTerminatesRouteLocation();

        RouteLocation rlBoston1 = route.getRouteLocationBySequenceNumber(2);
        RouteLocation rlChelmsford = route.getRouteLocationBySequenceNumber(3);

        // Set up two cabooses and six box cars, two with FRED       
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "32", actonYard1, 10);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", carTypes[1], "40", actonYard1, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", carTypes[1], "40", actonYard1, 13);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", carTypes[1], "40", actonYard1, 14);

        c1.setCaboose(true);
        c3.setFred(true);

        train1.setRequirements(Train.FRED);
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        Assert.assertEquals("train assignment", null, c1.getTrain()); // caboose, not assigned to train
        Assert.assertEquals("train assignment", train1, c3.getTrain()); // car with FRED
        Assert.assertEquals("train assignment", train1, c4.getTrain());
        Assert.assertEquals("train assignment", train1, c5.getTrain());

        // confirm route locations assignments
        Assert.assertEquals("route assignment", rlActon, c3.getRouteLocation());
        Assert.assertEquals("route assignment", rlActon, c4.getRouteLocation());
        Assert.assertEquals("route assignment", rlActon, c5.getRouteLocation());

        Assert.assertEquals("route assignment", rlActon2, c3.getRouteDestination());
        Assert.assertEquals("route assignment", rlBoston1, c4.getRouteDestination());
        Assert.assertEquals("route assignment", rlChelmsford, c5.getRouteDestination());

        // build again without reset
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        train1.reset();
        // confirm route locations assignments
        Assert.assertEquals("route assignment", null, c4.getRouteLocation());
        Assert.assertEquals("route assignment", null, c5.getRouteLocation());

        Assert.assertEquals("route assignment", null, c3.getRouteDestination());
        Assert.assertEquals("route assignment", null, c4.getRouteDestination());
        Assert.assertEquals("route assignment", null, c5.getRouteDestination());
    }

    /**
     * Test car on track, train direction controls
     */
    @Test
    public void testTrainDirection() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // Route Acton-Boston-Chelmsford-Chelmsford-Boston-Acton
        Route route = JUnitOperationsUtil.createThreeLocationTurnRoute();

        // define the train
        Train train1 = tmanager.newTrain("TestTrainDirection1");
        train1.setRoute(route);

        // train departs north bound and reverse at Chelmsford and then travels south bound
        RouteLocation rlActon = route.getDepartsRouteLocation();
        Location acton = rlActon.getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", null);
        Track actonYard2 = acton.getTrackByName("Acton Yard 2", null);
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);
        Track actonSpur2 = acton.getTrackByName("Acton Spur 2", null);
        Track actonInterchange1 = acton.getTrackByName("Acton Interchange 1", null);
        Track actonInterchange2 = acton.getTrackByName("Acton Interchange 2", null);
        // remove four of the six tracks at Acton leaving only yard tracks
        acton.deleteTrack(actonSpur1);
        acton.deleteTrack(actonSpur2);
        acton.deleteTrack(actonInterchange1);
        acton.deleteTrack(actonInterchange2);

        // Set up two cabooses and six box cars, two with FRED, half the cars on yard 1, the others on yard 2      
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "32", actonYard1, 10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", Bundle.getMessage("Caboose"), "32", actonYard2, 21);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", carTypes[1], "40", actonYard1, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", carTypes[1], "40", actonYard2, 23);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", carTypes[1], "40", actonYard1, 14);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", carTypes[1], "40", actonYard2, 15);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", carTypes[1], "50", actonYard1, 16);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", carTypes[1], "60", actonYard2, 17);
        Car c9 = JUnitOperationsUtil.createAndPlaceCar("CP", "90", carTypes[5], "40", actonYard1, 18);
        Car c10 = JUnitOperationsUtil.createAndPlaceCar("CP", "100", carTypes[5], "40", actonYard2, 19);

        c1.setCaboose(true);
        c2.setCaboose(true);
        c3.setFred(true);
        c4.setFred(true);

        // request caboose and car with FRED, should get both
        train1.setRequirements(Train.CABOOSE + Train.FRED);
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // there are 10 cars, 2 cabooses, 2 with FRED, so 8 cars could be pulled, but limit set to 7
        Assert.assertEquals("number of cars assigned to train", 7, train1.getNumberCarsWorked());

        // confirm train assignment
        Assert.assertEquals("train assignment", train1, c1.getTrain());
        Assert.assertEquals("train assignment", null, c2.getTrain()); // caboose
        Assert.assertEquals("train assignment", train1, c3.getTrain());
        Assert.assertEquals("train assignment", null, c4.getTrain()); // car with FRED
        Assert.assertEquals("train assignment", train1, c5.getTrain());
        Assert.assertEquals("train assignment", train1, c6.getTrain());
        Assert.assertEquals("train assignment", train1, c7.getTrain());
        Assert.assertEquals("train assignment", train1, c8.getTrain());
        Assert.assertEquals("train assignment", train1, c9.getTrain());
        Assert.assertEquals("train assignment", null, c10.getTrain()); // Setup limits number to 7

        // train departs north bound, so can't pull caboose
        acton.setTrainDirections(Location.EAST + Location.WEST + Location.SOUTH);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("train status", train1.isBuilt());

        // don't request caboose, should build, but all cars are at departure, so no cars assigned
        train1.setRequirements(Train.NO_CABOOSE_OR_FRED);
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        Assert.assertEquals("number of cars assigned to train", 0, train1.getNumberCarsWorked());

        // allow north bound trains to pull from location, train is south bound when arriving, so no set outs
        acton.setTrainDirections(Location.EAST + Location.WEST + Location.NORTH);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // there are 10 cars, 2 cabooses, 2 with FRED, so 6 cars should be pulled
        Assert.assertEquals("number of cars assigned to train", 6, train1.getNumberCarsWorked());

        // request caboose and car with FRED, should try to get both, but terminate location won't accept
        // due to train direction south bound
        train1.setRequirements(Train.CABOOSE + Train.FRED);
        new TrainBuilder().build(train1);
        Assert.assertFalse("train status", train1.isBuilt());

        // restore acton location to service train
        acton.setTrainDirections(Location.SOUTH + Location.WEST + Location.EAST + Location.NORTH);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // now test at track level
        // train departs north bound, so car are pulled from yard 2
        actonYard1.setTrainDirections(Location.EAST + Location.WEST + Location.SOUTH);
        // and delivered to yard 1 since train is south bound when it arrives
        actonYard2.setTrainDirections(Location.EAST + Location.WEST + Location.NORTH);

        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertTrue("train status", train1.isBuilt());

        // there are 10 cars, 2 cabooses, 2 with FRED, half the cars on yard 1, so 5 cars pulled
        Assert.assertEquals("number of cars assigned to train", 5, train1.getNumberCarsWorked());

        // confirm train assignment
        Assert.assertEquals("train assignment", null, c1.getTrain()); // caboose on yard 1 lower move count
        Assert.assertEquals("train assignment", train1, c2.getTrain()); // caboose on yard 2
        Assert.assertEquals("train assignment", null, c3.getTrain()); // car with FRED on yard 2 lower move count
        Assert.assertEquals("train assignment", train1, c4.getTrain()); // car with FRED on yard 2
        Assert.assertEquals("train assignment", null, c5.getTrain());// yard 1
        Assert.assertEquals("train assignment", train1, c6.getTrain());
        Assert.assertEquals("train assignment", null, c7.getTrain());// yard 1
        Assert.assertEquals("train assignment", train1, c8.getTrain());
        Assert.assertEquals("train assignment", null, c9.getTrain());// yard 1
        Assert.assertEquals("train assignment", train1, c10.getTrain());

        // both caboose and car with FRED delivered to yard 1
        Assert.assertEquals("car destination", actonYard1, c2.getDestinationTrack());
        Assert.assertEquals("car destination", actonYard1, c4.getDestinationTrack());

        // don't allow south bound set out for yard 1
        actonYard1.setTrainDirections(Location.EAST + Location.WEST + Location.NORTH);

        // should fail, can't deliver caboose or car with FRED to terminal
        train1.reset();
        new TrainBuilder().build(train1);
        Assert.assertFalse("train status", train1.isBuilt());
    }

    /**
     * Test staging to staging, two trains, one can't service car type "Boxcar"
     */
    @Test
    public void testStagingtoStaging() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        // Try building without engines
        Assert.assertTrue(new TrainBuilder().build(train1));
        train2.deleteTypeName("Boxcar");

        // train 2 can't service car type "Boxcar" which is on staging track North End 2
        Assert.assertFalse(new TrainBuilder().build(train2));
        // Only train 1 should build
        Assert.assertTrue("Train 1 After 1st Build without engines", train1.isBuilt());
        Assert.assertFalse("Train 2 After 1st Build exclude Boxcar", train2.isBuilt());
    }

    /*
     * Test car road names
     */
    @Test
    public void testStagingtoStagingCarRoadNames() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");
        Car c8 = cmanager.getByRoadAndNumber("CP", "888");

        // Try building without engines
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 should build", train1.isBuilt());

        // exclude road name CP for train 2
        train2.setRoadOption(Train.EXCLUDE_ROADS);
        train2.addRoadName("CP");
        Assert.assertEquals("Number of road names for train", 1, train2.getRoadNames().length);

        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train 2 After Build but exclude road CP", train2.isBuilt());
        train2.setRoadOption(Train.ALL_ROADS);

        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
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
        Assert.assertEquals("Car c1 After Build without engines destination", "South End Staging", c1
                .getDestinationName());
        Assert.assertEquals("Car c3 After Build without engines destination", "North Industries", c3
                .getDestinationName());
        Assert.assertEquals("Car c4 After Build without engines destination", "South End Staging", c4
                .getDestinationName());
        Assert.assertEquals("Car c8 After Build without engines destination", "South End Staging", c8
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
     * Test engine road names departing staging
     */
    @Test
    public void testStagingtoStagingEngineRoadNamesA() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

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
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train 1 After Build with engines but exclude road CP", train1.isBuilt());

        train1.setRoadOption(Train.ALL_ROADS);
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 allow all roads", train1.isBuilt());
    }

    /*
     * Test engine road names when departing staging
     */
    @Test
    public void testStagingtoStagingEngineRoadNamesB() {

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // try different road
        train2.setEngineRoad("CP");
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertEquals("Train 2 After Build require road CP", false, train2.isBuilt());

        train2.setEngineRoad("PC");
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train 2 After Build require road PC", true, train2.isBuilt());
    }

    /*
     * Test car location unknown departing staging
     */
    @Test
    public void testStagingtoStagingCarLocationUnknown() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");

        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");

        // Try building without engines
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 should build", train1.isBuilt());

        // make c1's location unknown
        c1.setLocationUnknown(true);

        // can't build a train out of staging with a car's location unknown
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());
    }

    /*
     * Test car out of service departing staging
     */
    @Test
    public void testStagingtoStagingCarOutOfService() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");

        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");

        // Try building without engines
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 should build", train1.isBuilt());

        // make c1's location unknown
        c1.setOutOfService(true);

        // can't build a train out of staging with a car out of service
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());
    }

    /*
     * Test required number of engines departing staging
     */
    @Test
    public void testStagingtoStagingRequiredNumberOfEngines() {

        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");
        Car c5 = cmanager.getByRoadAndNumber("CP", "X20001");
        Car c6 = cmanager.getByRoadAndNumber("CP", "X20002");
        Car c7 = cmanager.getByRoadAndNumber("CP", "777");
        Car c8 = cmanager.getByRoadAndNumber("CP", "888");
        Car c9 = cmanager.getByRoadAndNumber("CP", "99");
        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location l1 = lmanager.getLocationById("1"); // staging
        Location l2 = lmanager.getLocationById("20");
        Location l3 = lmanager.getLocationById("3"); // staging
        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");
        Track l2s1 = l2.getTrackById("20s1");
        Track l3s1 = l3.getTrackById("3s1");
        Track l3s2 = l3.getTrackById("3s2");
        Route route1 = rmanager.getRouteById("1");
        RouteLocation rl1 = route1.getLocationById("1r1");
        RouteLocation rl2 = route1.getLocationById("1r2");
        RouteLocation rl3 = route1.getLocationById("1r3");

        train2.setRoadOption(Train.ALL_ROADS);

        // Try building without engines on staging tracks but require them
        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");

        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse(new TrainBuilder().build(train2));
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
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue(new TrainBuilder().build(train2));
        // Both should build
        Assert.assertTrue("Train 1 After Build with engines", train1.isBuilt());
        Assert.assertTrue("Train 2 After Build with engines", train2.isBuilt());

        // Check train 1
        Assert.assertEquals("Train 1 After Build Departs Name", "North End Staging", train1.getTrainDepartsName());
        Assert.assertEquals("Train 1 After Build Terminates Name", "South End Staging", train1
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
        Assert.assertEquals("Engine e1 After Build destination", "South End Staging", e1.getDestinationName());
        Assert.assertEquals("Engine e2 After Build destination", "South End Staging", e2.getDestinationName());
        Assert.assertEquals("Car c1 After Build destination", "South End Staging", c1.getDestinationName());
        Assert.assertEquals("Car c3 After Build destination", "North Industries", c3.getDestinationName());
        Assert.assertEquals("Car c4 After Build destination", "South End Staging", c4.getDestinationName());
        Assert.assertEquals("Car c8 After Build destination", "South End Staging", c8.getDestinationName());

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
        Assert.assertEquals("Train 1 After Reset Departs Name", "North End Staging", train1.getTrainDepartsName());
        Assert.assertEquals("Train 1 After Reset Terminates Name", "South End Staging", train1
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

        // now test failure conditions
        train1.reset();
        train2.reset();

        // make track in staging too short for engines
        l3s2.setLength(100);

        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train 1 staging track too short", train1.isBuilt());
    }

    /*
     * Test car type names departing staging
     */
    @Test
    public void testStagingtoStagingCarTypes() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

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
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build with engines but exclude Caboose", false, train1.isBuilt());

        train1.addTypeName(Bundle.getMessage("Caboose"));
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build with engines include Caboose", true, train1.isBuilt());
    }

    /*
     * Test cars with FRED departing staging
     */
    @Test
    public void testStagingtoStagingCarWithFredA() {

        JUnitOperationsUtil.initOperationsData();

        // get caboose on staging track and add FRED
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        c1.setCaboose(false);
        c1.setFred(true);

        Train train1 = tmanager.getTrainById("1");

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        Assert.assertEquals("c1 assigned to train", train1, c1.getTrain());

        // provide a destination for the car with FRED out of staging
        Location locationNI = lmanager.getLocationById("20");
        Track yardNI = locationNI.getTrackById("20s1");

        // car with FRED and no changes in trains route, has to go terminal
        train1.reset();
        Assert.assertEquals("Send car to 2nd location in route", Track.OKAY, c1.setDestination(locationNI, yardNI));

        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train 1 status", train1.isBuilt());

        // now send car with FRED to terminal, that should work
        train1.reset();
        Location southEndStaging = lmanager.getLocationById("3");
        Assert.assertEquals("Send car to staging", Track.OKAY, c1.setDestination(southEndStaging, null));

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());
    }

    /*
     * Test car departing staging requiring FRED
     */
    @Test
    public void testStagingtoStagingCarWithFredB() {

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");

        Car c5 = cmanager.getByRoadAndNumber("CP", "X20001");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // try requiring FRED, should fail
        train2.setRequirements(Train.FRED);
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertEquals("Train 2 After Build requires FRED", false, train2.isBuilt());
        // Add FRED to boxcar
        c5.setFred(true);
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train 2 After Build 2 requires FRED", true, train2.isBuilt());
    }

    /*
     * Test caboose with destination departing staging
     */
    @Test
    public void testStagingtoStagingCabooseWithDestination() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location l1 = lmanager.getLocationById("1");
        Location l2 = lmanager.getLocationById("20");
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
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train 1 build should fail, caboose destination isn't terminal", train1.isBuilt());

        // send caboose to last location which is staging
        c1.setDestination(l3, null);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 build, caboose destination is terminal", train1.isBuilt());
    }

    /*
     * Test car departing staging with destination not serviced by train
     */
    @Test
    public void testStagingtoStagingCarWithDestination() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");

        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        Location l2 = lmanager.getLocationById("20");

        // car with a destination not serviced by train
        Location nowhere = lmanager.newLocation("nowhere");
        c4.setDestination(nowhere, null);

        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After build with car to nowhere", false, train1.isBuilt());

        // Build the train again confirm will build if car's destination is serviced by train
        train1.reset();
        c4.setDestination(l2, null); // send car to NI
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());
    }

    /*
     * Test car departing staging with a car that has a final destination that
     * won't service the car.
     * 
     */
    @Test
    public void testStagingtoStagingCarWithFinalDestinationA() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");

        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        // car with a destination not serviced by train
        Location nowhere = lmanager.newLocation("nowhere");
        Track trackNowhere = nowhere.addTrack("track", Track.YARD);
        trackNowhere.setLength(100);

        c4.setFinalDestination(nowhere);
        c4.setFinalDestinationTrack(trackNowhere);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // don't allow track to accept car type Boxcar
        trackNowhere.deleteTypeName("Boxcar");

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train status", true, train1.isBuilt());

        // track doesn't accept Boxcar, so final destination should be removed
        Assert.assertEquals("car final destination", null, c4.getFinalDestination());
        Assert.assertEquals("car final destination track", null, c4.getFinalDestinationTrack());
    }

    /**
     * Test car departing staging that has a final destination that is also
     * staging.
     */
    @Test
    public void testStagingtoStagingCarWithFinalDestinationB() {

        JUnitOperationsUtil.initOperationsData();

        // Northend - NI - Southend
        Train train1 = tmanager.getTrainById("1");

        // cars departing staging
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001"); // Boxcar
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002"); // Boxcar

        Location locationNorthIndustries = lmanager.getLocationById("20");

        Location locationSouthEnd = lmanager.getLocationById("3");
        Track l3staging1 = locationSouthEnd.getTrackById("3s1");
        Track l3staging2 = locationSouthEnd.getTrackById("3s2");
        l3staging2.setMoves(100); // use staging track 1 for this test

        // give both cars final destination staging
        c3.setFinalDestination(locationSouthEnd);
        c4.setFinalDestination(locationSouthEnd);

        // should build
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm that both cars are sent to staging
        Assert.assertEquals("car destination track", l3staging1, c3.getDestinationTrack());
        Assert.assertEquals("car destination track", l3staging1, c4.getDestinationTrack());

        // create a problem, don't allow Boxcar into staging or NI
        l3staging1.deleteTypeName("Boxcar");
        Setup.setTrainIntoStagingCheckEnabled(false); // allow train to carry "Boxcar"
        locationNorthIndustries.deleteTypeName("Boxcar");

        train1.reset();
        c3.setDestination(locationSouthEnd); // this car should be reset
        c3.setFinalDestinationTrack(l3staging1);
        c4.setFinalDestinationTrack(l3staging1); // staging does not accept Boxcar

        // build should fail
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train status", false, train1.isBuilt());

        // confirm car c3 reset, it had a destination, now removed
        Assert.assertEquals("car destination track", null, c3.getDestinationTrack()); // car reset
        Assert.assertEquals("car destination track", null, c4.getDestinationTrack());

        Assert.assertEquals("car final destination track", l3staging1, c3.getFinalDestinationTrack());
        Assert.assertEquals("car final destination track", null, c4.getFinalDestinationTrack());
    }

    /*
     * Test location car type acceptance
     */
    @Test
    public void testStagingtoStagingLocationCarType() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

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
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train 1 build, Caboose not serviced by location", train1.isBuilt());

        // now allow location to service type name "Caboose"
        l3.addTypeName(Bundle.getMessage("Caboose"));
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 build, Caboose is allowed", train1.isBuilt());
    }

    /*
     * Test car built dates
     */
    @Test
    public void testStagingtoStagingCarBuiltDate() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

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
        Assert.assertFalse(new TrainBuilder().build(train1));
        // should fail, required engines have built dates after 1985
        Assert.assertEquals("Train 1 After Build with rs built before 1985", false, train1.isBuilt());
        // change the engine built date
        e1.setBuilt("7-84");
        e2.setBuilt("1984");
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 After 2nd Build with rs built before 1985", train1.isBuilt());
        // change one of the car's built date to after 1985
        c4.setBuilt("1-85");
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        // should fail
        Assert.assertFalse("Train 1 After 3rd Build with rs built before 1985", train1.isBuilt());
        train1.setBuiltEndYear("");
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 After 4th Build with rs built before 1985", train1.isBuilt());
    }

    /*
     * Test engine type names
     */
    @Test
    public void testStagingtoStagingEngineTypeNames() {
        String engineTypes[] = Bundle.getMessage("engineDefaultTypes").split(",");

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

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

        // exclude engine type Diesel
        train1.deleteTypeName(engineTypes[2]);
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After 1st Build type Diesel not serviced", false, train1.isBuilt());
        train1.addTypeName(engineTypes[2]);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After 2nd Build type Diesel serviced", true, train1.isBuilt());
    }

    /*
     * Test car owner
     */
    @Test
    public void testStagingtoStagingCarOwner() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

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
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After 1st Build owner DAB", false, train1.isBuilt());
        train1.addOwnerName("AT");
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After 2nd Build owners DAB and AT", true, train1.isBuilt());
        train1.setOwnerOption(Train.EXCLUDE_OWNERS);
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After 3rd Build exclude owners DAB and AT", false, train1.isBuilt());
        train1.deleteOwnerName("AT");
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After 4th Build exclude owner DAB", false, train1.isBuilt());
        train1.setOwnerOption(Train.ALL_OWNERS);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After 5th Build all owners", true, train1.isBuilt());
    }

    /*
     * Test car load restrictions departing staging, train load restrictions.
     */
    @Test
    public void testStagingtoStagingTrainCarLoadRestrications() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");

        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099"); // in staging track 1
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099"); // in staging track 1
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001"); // in staging track 1
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002"); // in staging track 1

        Car c8 = cmanager.getByRoadAndNumber("CP", "888");

        // restrict load carried by train
        train1.addLoadName("L");
        train1.setLoadOption(Train.INCLUDE_LOADS);

        Assert.assertFalse(new TrainBuilder().build(train1));
        // build should fail, cars in staging have E loads
        Assert.assertEquals("Train 1 After include load L", false, train1.isBuilt());

        train1.deleteLoadName("L");
        c8.setLoadName("L"); // this car shouldn't be picked up.
        train1.addLoadName("E");
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 After include load E", train1.isBuilt());

        Assert.assertEquals("car C10099 in staging should be assigned to train", train1, c1.getTrain());
        Assert.assertEquals("car C20099 in staging should be assigned to train", train1, c2.getTrain());
        Assert.assertEquals("car X1001 in staging should be assigned to train", train1, c3.getTrain());
        Assert.assertEquals("car X1002 in staging should be assigned to train", train1, c4.getTrain());

        Assert.assertEquals("car 888 at siding has load L, excluded", null, c8.getTrain());

        train1.addLoadName("L");
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 After include load L", train1.isBuilt());

        Assert.assertEquals("car C10099 in staging should be assigned to train", train1, c1.getTrain());
        Assert.assertEquals("car C20099 in staging should be assigned to train", train1, c2.getTrain());
        Assert.assertEquals("car X1001 in staging should be assigned to train", train1, c3.getTrain());
        Assert.assertEquals("car X1002 in staging should be assigned to train", train1, c4.getTrain());

        Assert.assertEquals("car 888 at siding has load L, now included", train1, c8.getTrain());

        train1.setLoadOption(Train.EXCLUDE_LOADS);
        // cars in staging have E loads, so build should fail
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());

        // allow train to carry E loads, but not L
        train1.deleteLoadName("E");
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train status", true, train1.isBuilt());

        Assert.assertEquals("car C10099 in staging should be assigned to train", train1, c1.getTrain());
        Assert.assertEquals("car C20099 in staging should be assigned to train", train1, c2.getTrain());
        Assert.assertEquals("car X1001 in staging should be assigned to train", train1, c3.getTrain());
        Assert.assertEquals("car X1002 in staging should be assigned to train", train1, c4.getTrain());

        Assert.assertEquals("car 888 at siding has load L, now excluded", null, c8.getTrain());

        // caboose and passenger cars can depart with any load
        c1.setCaboose(false);
        c1.setPassenger(true);
        c1.setLoadName("L");
        // remove the other two cars in staging
        c3.setLocation(null, null);
        c4.setLocation(null, null);

        // exclude load names "E" and "L"
        train1.addLoadName("E");
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train status", true, train1.isBuilt());

        Assert.assertEquals("car C10099 in staging should be assigned to train", train1, c1.getTrain());
        Assert.assertEquals("car C20099 in staging should be assigned to train", train1, c2.getTrain());
    }

    /*
     * Test car load restrictions departing staging, track load restrictions.
     */
    @Test
    public void testStagingtoStagingDepartureTrackCarLoadRestrications() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");

        // the four cars in staging, track 1
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        // get staging departure track
        Location northEnd = lmanager.getLocationByName("North End Staging");
        Track northEndStaging1 = northEnd.getTrackByName("North End 1", Track.STAGING);

        // train should build
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // restrict car load departing staging, all cars have "E" load
        northEndStaging1.setShipLoadOption(Track.EXCLUDE_LOADS);
        northEndStaging1.addShipLoadName("E");

        // should fail, cars have "E" loads
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());

        // the two cabooses are departing with load "E" but were assigned to the train
        // No ship load restriction for caboose
        Assert.assertEquals("train assignment", train1, c1.getTrain());
        Assert.assertEquals("train assignment", train1, c2.getTrain());

        // build failed due to the two boxcars attempting to depart with load "E"
        Assert.assertEquals("train assignment", null, c3.getTrain());
        Assert.assertEquals("train assignment", null, c4.getTrain());

        // Change one of the cabooses to car with FRED
        c2.setCaboose(false);
        c2.setFred(true);

        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());

        // No ship load restriction for caboose or car with FRED
        Assert.assertEquals("train assignment", train1, c1.getTrain());
        Assert.assertEquals("train assignment", train1, c2.getTrain()); // car with FRED, load restriction ignored

        // build failed due to the two boxcars attempting to depart with load "E"
        Assert.assertEquals("train assignment", null, c3.getTrain());
        Assert.assertEquals("train assignment", null, c4.getTrain());
    }

    /*
     * Test car load restrictions entering staging, track load restrictions.
     */
    @Test
    public void testStagingtoStagingTerminalTrackCarLoadRestrications() {

        // create some car loads for this test
        cld.addName("Boxcar", "Flour");
        cld.addName("Boxcar", "Bricks");
        cld.addName("Boxcar", "Coal");
        cld.addName("Boxcar", "Books");
        cld.addName("Boxcar", "Grain");

        cld.addName("Flat", "Coil");
        cld.addName("Flat", "Bricks");
        cld.addName("Flat", "Coal");
        cld.addName("Flat", "Books");
        cld.addName("Flat", "Tools");

        JUnitOperationsUtil.initOperationsData();

        // route North End - NI - South End
        Train train2 = tmanager.getTrainById("2");

        // get staging terminal track
        Location southEnd = lmanager.getLocationByName("South End Staging");
        Track southEndStaging1 = southEnd.getTrackByName("South End 1", Track.STAGING);
        Track southEndStaging2 = southEnd.getTrackByName("South End 2", Track.STAGING);
        southEnd.deleteTrack(southEndStaging2); // delete this track

        // train should build
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train status", train2.isBuilt());

        // restrict car load "Books" from entering staging
        southEndStaging1.setLoadOption(Track.EXCLUDE_LOADS);
        southEndStaging1.addLoadName("Books");

        // should fail, train can have "Books" loads, excludes grain in boxcars
        train2.setLoadOption(Train.EXCLUDE_LOADS);
        train2.addLoadName("Grain");
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train status", train2.isBuilt());

        // now exclude "Books" load in train, should build
        train2.addLoadName("Books");
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train status", train2.isBuilt());

        // now exclude "Books" carried by Boxcars
        train2.deleteLoadName("Books");
        train2.addLoadName("Boxcar" + CarLoad.SPLIT_CHAR + "Books");

        // should fail, "Books" could be carried by Flat cars
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train status", train2.isBuilt());

        // restrict car load entering staging to boxcar with "Books"
        southEndStaging1.deleteLoadName("Books");
        southEndStaging1.addLoadName("Boxcar" + CarLoad.SPLIT_CHAR + "Books");

        // should build, both train and staging do not accept Boxcar with "Books"
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train status", train2.isBuilt());

        // don't allow Flat with Books into staging
        southEndStaging1.addLoadName("Flat" + CarLoad.SPLIT_CHAR + "Books");

        // should fail, "Books" could be carried by Flat cars in train
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train status", train2.isBuilt());

        // don't allow any car to carry "Books" in train
        train2.addLoadName("Books");

        // should build, train doesn't allow Books on any car, staging doesn't allow Books carried by Boxcar or Flat
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train status", train2.isBuilt());

        // now try train include loads
        train2.setLoadOption(Train.INCLUDE_LOADS);
        train2.addLoadName("E"); // cars in departure staging have "E" loads

        // train can carry "E", and any car with Books or Grain (only Boxcar can carry Grain)
        // staging doesn't allow Boxcar or Flat with "Books"
        // build should fail
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train status", train2.isBuilt());

        // allow Boxcar with "Books} into staging
        southEndStaging1.deleteLoadName("Boxcar" + CarLoad.SPLIT_CHAR + "Books");

        // should not build, Flat can carry "Books"
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train status", train2.isBuilt());

        // allow Flat with "Books} into staging
        southEndStaging1.deleteLoadName("Flat" + CarLoad.SPLIT_CHAR + "Books");
        southEndStaging1.addLoadName("Coal"); // train can not carry Coal in any car

        // should build, train doesn't allow "Coal" to be carried
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train status", train2.isBuilt());

        // allow the train to carry Coal in any car
        train2.addLoadName("Coal");

        // should not build
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train status", train2.isBuilt());
    }

    /*
     * Test service direction departing staging
     */
    @Test
    public void testStagingtoStagingTrackDirection() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

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
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train 1 After 1st Build staging set to North", train1.isBuilt());

        l1s1.setTrainDirections(Location.SOUTH);
        l1s2.setTrainDirections(Location.SOUTH);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 After 2nd Build staging set to South", train1.isBuilt());
    }

    /**
     * Test track selection for trains departing staging.
     */
    @Test
    public void testStagingtoStagingTrainDepartureControls() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // only allow train 2 to depart staging
        l1s1.setPickupOption(Track.TRAINS);
        l1s1.addPickupId(train2.getId());

        l1s2.setPickupOption(Track.TRAINS);
        l1s2.addPickupId(train2.getId());

        // train 1 can't depart staging, no tracks available
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());

        // train 2 can depart on either track
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train status", train2.isBuilt());
    }

    /**
     * Test track selection for trains terminating into staging.
     */
    @Test
    public void testStagingtoStagingTrainTerminateControls() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Location l3 = lmanager.getLocationById("3"); // staging terminate

        Track l3s1 = l3.getTrackById("3s1");
        Track l3s2 = l3.getTrackById("3s2");

        // only allow train 2 to terminate into staging
        l3s1.setDropOption(Track.TRAINS);
        l3s1.addDropId(train2.getId());

        l3s2.setDropOption(Track.TRAINS);
        l3s2.addDropId(train2.getId());

        // train 1 can't terminate into staging, no tracks available
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());

        // train 2 can terminate to either track
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train status", train2.isBuilt());
    }

    /**
     * Test that build will take cars out of staging than don't exceed the
     * maximum departure train length.
     */
    @Test
    public void testStagingtoStagingTrainDepartureTrainLength() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        // two 40' cars
        Car c5 = cmanager.getByRoadAndNumber("CP", "X20001"); // on staging track 2
        Car c6 = cmanager.getByRoadAndNumber("CP", "X20002"); // on staging track 2

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // limit the length of the train out of staging
        RouteLocation rlStaging = train1.getTrainDepartsRouteLocation();

        // there are 4 cars on staging track 1, 32' + 32' + 40' + 40' + couplers
        Assert.assertEquals("rolling stock total length", 160, l1s1.getUsedLength());
        // there are 2 cars on staging track 2, 40' + 40' + couplers
        Assert.assertEquals("rolling stock total length", 88, l1s2.getUsedLength());

        rlStaging.setMaxTrainLength(100);

        // train can depart staging track 2
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train status", train2.isBuilt());

        // confirm that only two cars are part of train
        Assert.assertEquals("car has been assigned to train", train2, c5.getTrain());
        Assert.assertEquals("car has been assigned to train", train2, c6.getTrain());

        // only track left is staging 1, total length of cars is too long, build should fail
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());
    }

    /**
     * Test that an engine assigned to a train in staging can only depart on
     * that train.
     */
    @Test
    public void testStagingtoStagingEngineAssignedToTrain() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016"); // e1 + e2 in consist
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524"); // e3 + e4 in consist
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1"); // staging 1
        Track l1s2 = l1.getTrackById("1s2"); // staging 2

        // place one engine on each staging track
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // assign engines to train2
        e1.setTrain(train2);
        e3.setTrain(train2);

        // should build, engines are assigned to train 2
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train status", train2.isBuilt());

        // there's an engine assigned to train 2 on staging track 2, train 1 build should fail
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());
    }

    /**
     * Test that a car assigned to a train in staging can only depart on that
     * train.
     */
    @Test
    public void testStagingtoStagingCarAssignedToTrain() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002"); // car on staging track 1
        Car c5 = cmanager.getByRoadAndNumber("CP", "X20001"); // car on staging track 2

        // assign cars to train2
        c4.setTrain(train2);
        c5.setTrain(train2);

        // should build, cars are assigned to train 2
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train status", train2.isBuilt());

        // there's a car assigned to train 2 on staging track 2, train 1 build should fail
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());
    }

    /*
     * Test route car move counts out of staging, move and terminate trains.
     * Confirm cars go to correct locations and tracks.
     */
    @Test
    public void testStagingtoStagingRouteMoves() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");
        Car c5 = cmanager.getByRoadAndNumber("CP", "X20001");
        Car c6 = cmanager.getByRoadAndNumber("CP", "X20002");
        Car c7 = cmanager.getByRoadAndNumber("CP", "777");
        Car c8 = cmanager.getByRoadAndNumber("CP", "888");
        Car c9 = cmanager.getByRoadAndNumber("CP", "99");
        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location l1 = lmanager.getLocationById("1");
        Location l2 = lmanager.getLocationById("20");
        Location l3 = lmanager.getLocationById("3");
        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");
        Track l2s1 = l2.getTrackById("20s1");
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
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train 1 After Build limit train to 2 cars out of staging", train1.isBuilt());

        // try again but now the train can have four cars departing staging
        rl1.setMaxCarMoves(4); // there are four cars in staging
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 After Build limit train to 4 cars out of staging", train1.isBuilt());

        // Move the train #1
        train1.move();
        Assert.assertEquals("Train 1 After 1st Move Current Name", "North Industries", train1
                .getCurrentLocationName());
        Assert.assertEquals("Train 1 After 1st Move Next Location Name", "South End Staging", train1
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
        Assert.assertEquals("Train 1 After 2nd Move Current Name", "South End Staging", train1
                .getCurrentLocationName());
        Assert.assertEquals("Train 1 After 2nd Move Next Location Name", "", train1.getNextLocationName());
        // Is the train in route?
        Assert.assertEquals("Train 1 in route after 2nd", true, train1.isTrainEnRoute());

        // Are the engine and car locations correct?
        Assert.assertEquals("Engine e1 After After 2nd Move", "South End Staging", e1.getLocationName());
        Assert.assertEquals("Engine e2 After After 2nd Move", "South End Staging", e2.getLocationName());
        Assert.assertEquals("Car c1 After After 2nd Move", "South End Staging", c1.getLocationName());
        Assert.assertEquals("Car c3 After After 2nd Move", "North Industries", c3.getLocationName());
        Assert.assertEquals("Car c4 After After 2nd Move", "South End Staging", c4.getLocationName());
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
        Assert.assertEquals("Engine e1 After Terminate location", "South End Staging", e1.getLocationName());
        Assert.assertEquals("Engine e2 After Terminate location", "South End Staging", e2.getLocationName());
        Assert.assertEquals("Car c1 After Terminate location", "South End Staging", c1.getLocationName());
        Assert.assertEquals("Car c4 After Terminate location", "South End Staging", c4.getLocationName());
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
    public void testStagingtoStagingNumberOfEnginesA() {

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // this should fail, there are two engines in staging
        train2.setNumberEngines("1");
        // now build train 2 testing failure modes
        Assert.assertFalse(new TrainBuilder().build(train2));
        // build required 1 engine and there were two
        Assert.assertFalse("Train 2 After Build require 1 engine", train2.isBuilt());
        // take one engine out of the consist
        e4.setConsist(null);
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train 2 After Build require 1 engine, but 2 engines on staging track", train2.isBuilt());
        e4.setLocation(null, null); // remove engine from staging
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train 2 After Build require 1 engine, 1 consisted engine on staging track",
                train2.isBuilt());
        // take lead engine out of consist
        e3.setConsist(null);
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train 2 After Build require 1 engine, single engine on staging track", true,
                train2.isBuilt());
    }

    /*
     * Test number of engines departing staging
     */
    @Test
    public void testStagingtoStagingNumberOfEnginesB() {

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // should work for 0
        train2.setNumberEngines("0");
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train 2 After Build require 0 engine", train2.isBuilt());
        train2.setNumberEngines("3");
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train 2 After Build require 3 engines", train2.isBuilt());
        train2.setNumberEngines("2");
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train 2 After Build require 2 engine", train2.isBuilt());
    }

    /*
     * Test engine "out of service" departing staging
     */
    @Test
    public void testStagingtoStagingEngineOutOfService() {

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        train1.setEngineRoad("PC");
        train1.setEngineModel("GP40");
        train1.setNumberEngines("2");
        train2.setNumberEngines("2");
        Assert.assertTrue(new TrainBuilder().build(train1));

        // take engine out of service
        e3.setOutOfService(true);
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train 2 After Build engine out of service", train2.isBuilt());
        // put back into service
        e3.setOutOfService(false);
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train 2 After Build engine in service", train2.isBuilt());
    }

    /*
     * Test engine model departing staging
     */
    @Test
    public void testStagingtoStagingEngineModel() {

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");

        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");

        Car c5 = cmanager.getByRoadAndNumber("CP", "X20001");
        Car c6 = cmanager.getByRoadAndNumber("CP", "X20002");
        Car c7 = cmanager.getByRoadAndNumber("CP", "777");

        Car c9 = cmanager.getByRoadAndNumber("CP", "99");
        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location l1 = lmanager.getLocationById("1");

        Track l1s1 = l1.getTrackById("1s1");
        Track l1s2 = l1.getTrackById("1s2");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(l1, l1s1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(l1, l1s1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(l1, l1s2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(l1, l1s2));

        // try engine wrong model
        train2.setEngineModel("DS45");
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertEquals("Train 2 After Build 2 requires model DS45", false, train2.isBuilt());
        // try engine correct model
        train2.setEngineModel("SD45");
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
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
     * Test train departing staging adjust terminal staging track lengths.
     * Confirm car loads after arriving into staging.
     */
    @Test
    public void testStagingtoStagingTrackLength() {

        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");
        Car c5 = cmanager.getByRoadAndNumber("CP", "X20001");
        Car c6 = cmanager.getByRoadAndNumber("CP", "X20002");
        Car c7 = cmanager.getByRoadAndNumber("CP", "777");
        Car c8 = cmanager.getByRoadAndNumber("CP", "888");
        Car c9 = cmanager.getByRoadAndNumber("CP", "99");
        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

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

        c9.setLength("200");
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
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
        Assert.assertTrue(new TrainBuilder().build(train2));
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

    /**
     * Test two cars in kernel in staging but not able to depart.
     */
    @Test
    public void testStagingtoStagingCarsInKernel() {

        Setup.setSwitchTime(11);
        Setup.setTravelTime(111);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");

        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099"); // in staging 1, length 32
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099"); // in staging 1, length 32
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001"); // in staging 1, length 40
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002"); // in staging 1, length 40

        Kernel k2 = cmanager.newKernel("Two cars");
        c3.setKernel(k2);
        c4.setKernel(k2);

        // train builds without route restrictions
        Assert.assertTrue(new TrainBuilder().build(train1));

        // confirm train assignment
        Assert.assertEquals("car assigned to train", train1, c1.getTrain());
        Assert.assertEquals("car assigned to train", train1, c2.getTrain());
        Assert.assertEquals("car assigned to train", train1, c3.getTrain());
        Assert.assertEquals("car assigned to train", train1, c4.getTrain());

        // now limit the train length and don't allow set outs
        Route route1 = rmanager.getRouteById("1");
        RouteLocation rl2 = route1.getLocationById("1r2");
        rl2.setDropAllowed(false); // no set outs
        rl2.setMaxTrainLength(80); // only enough for the two cabooses

        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));

        // confirm train assignment
        Assert.assertEquals("car assigned to train", train1, c1.getTrain());
        Assert.assertEquals("car assigned to train", train1, c2.getTrain());
        Assert.assertEquals("car assigned to train", null, c3.getTrain());
        Assert.assertEquals("car assigned to train", null, c4.getTrain());
    }

    /*
     * Test train terminating into staging, tracks full, staging track size, car
     * type and road restrictions
     */
    @Test
    public void testStagingtoStagingU() {

        JUnitOperationsUtil.initOperationsData();

        Train train2 = tmanager.getTrainById("2");
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");
        Car c5 = cmanager.getByRoadAndNumber("CP", "X20001");
        Car c6 = cmanager.getByRoadAndNumber("CP", "X20002");
        Car c7 = cmanager.getByRoadAndNumber("CP", "777");
        Car c8 = cmanager.getByRoadAndNumber("CP", "888");
        Car c9 = cmanager.getByRoadAndNumber("CP", "99");
        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");
        Engine e3 = emanager.getByRoadAndNumber("PC", "5524");
        Engine e4 = emanager.getByRoadAndNumber("PC", "5559");

        Location l1 = lmanager.getLocationById("1");
        Location l2 = lmanager.getLocationById("20");
        Location l3 = lmanager.getLocationById("3");
        Track l1s1 = l1.getTrackById("1s1");

        Track l2s1 = l2.getTrackById("20s1");
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

        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(l1, l1s1));
        train2.setRequirements(Train.CABOOSE);
        train2.setNumberEngines("0");
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        // Should fail both staging tracks are full
        Assert.assertFalse("Train 2 not built", train2.isBuilt());

        // add a new staging track
        Track l3s3 = l3.addTrack("South End 3", Track.STAGING);
        l3s3.setLength(200);
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        // Should build
        Assert.assertTrue("Train 2 built", train2.isBuilt());

        // make staging track too small for caboose
        l3s3.setLength(20);
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        // Should not build
        Assert.assertFalse("Train 2 built", train2.isBuilt());
        l3s3.setLength(200); // restore

        // Car X10001 is a location North Industries, NI Yard, send boxcar X10001 to staging
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(l2, l2s1));
        Assert.assertEquals("set destination", Track.OKAY, c3.setDestination(l3, null));
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        // Should build
        Assert.assertTrue("Train 2 built", train2.isBuilt());
        Assert.assertEquals("Car X10001 to staging track", l3s3, c3.getDestinationTrack());
        Assert.assertEquals("Car X10001 assigned to train 2", train2, c3.getTrain());

        // Send car X10001 to staging and the only track available
        train2.reset();
        c3.setDestination(l3, l3s3); // this removes track, it is now reserved
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        // Should not build
        Assert.assertFalse("Train 2 built", train2.isBuilt());
        Assert.assertEquals("Car X10001 assigned to train 2", null, c3.getTrain());

        train2.setRequirements(Train.NO_CABOOSE_OR_FRED);
        train2.reset();

        train2.addTypeName("BOXCAR");
        c3.setTypeName("BOXCAR");
        l3.addTypeName("BOXCAR");
        c3.setDestination(l3, null);
        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        // Should Not build, staging track South End 3 doesn't service type BOXCAR
        Assert.assertFalse("Train 2 will not build due to BOXCAR", train2.isBuilt());
        Assert.assertEquals("Car X10001 NOT assigned to train 2", null, c3.getTrain());

        // turn off staging check
        Setup.setTrainIntoStagingCheckEnabled(false);
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train 2 will now build ignoring BOXCAR", train2.isBuilt());
        Assert.assertNull("Car X10001 NOT assigned to train 2", c3.getTrain());
        Setup.setTrainIntoStagingCheckEnabled(true);

        train2.deleteTypeName("BOXCAR");
        c3.setTypeName("Boxcar");
        // control which road will go into staging
        l3s3.setRoadOption(Track.INCLUDE_ROADS);

        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train 2 will NOT build road restriction", train2.isBuilt());

        train2.setRoadOption(Train.INCLUDE_ROADS);
        train2.addRoadName("CP");
        Assert.assertEquals("Number of road names for train", 1, train2.getRoadNames().length);

        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train 2 will NOT build road restriction CP", train2.isBuilt());

        l3s3.addRoadName("CP");
        Assert.assertEquals("Number of road names", 1, l3s3.getRoadNames().length);

        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train 2 will build road restriction CP removed", train2.isBuilt());

        train2.setRoadOption(Train.EXCLUDE_ROADS);
        train2.deleteRoadName("CP");
        Assert.assertEquals("Number of road names for train", 0, train2.getRoadNames().length);

        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train 2 will NOT build road restriction exclude road CP", train2.isBuilt());

        // now allow road into staging
        l3s3.setRoadOption(Track.EXCLUDE_ROADS);
        l3s3.deleteRoadName("CP");
        Assert.assertEquals("Number of road names", 0, l3s3.getRoadNames().length);

        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train 2 will build no road restrictions", train2.isBuilt());

        cr.addName("BM"); // BM is not a road in all languages, so add it.
        l3s3.addRoadName("BM");
        Assert.assertEquals("Number of road names", 1, l3s3.getRoadNames().length);

        train2.reset();
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train 2 will Not build, staging track will not accept road BM", train2.isBuilt());
    }

    /**
     * test cars returning to staging when train is a turn
     */
    @Test
    public void testStagingtoStagingTrainTurnA() {

        JUnitOperationsUtil.initOperationsData();

        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        Location northEndStaging = lmanager.getLocationById("1");
        Location northIndustries = lmanager.getLocationById("20");

        // add a third staging track since the first two have cars on them
        Track northEnd3 = northEndStaging.addTrack("North End 3", Track.STAGING);
        northEnd3.setLength(500);

        Track yard = northIndustries.getTrackById("20s1");

        // create a route that returns to staging
        Route route = rmanager.newRoute("NorthEnd-NI-NorthEnd");
        route.addLocation(northEndStaging);
        route.addLocation(northIndustries);
        route.addLocation(northEndStaging);

        Train train1 = tmanager.newTrain("Test turn to staging");
        train1.setRoute(route);

        // train should build, there's enough room at yard for 2 cars departing staging
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm c3 and c4 went to yard
        Assert.assertEquals("c3 destination track", yard, c3.getDestinationTrack());
        Assert.assertEquals("c4 destination track", yard, c4.getDestinationTrack());

        // there are three cars already on the yard track, total length 200' 
        yard.setLength(210);

        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train 1 should not build", train1.isBuilt());

        train1.setAllowReturnToStagingEnabled(true);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // no room for cars at yard, both returned to staging
        Assert.assertEquals("c3 destination track", northEnd3, c3.getDestinationTrack());
        Assert.assertEquals("c4 destination track", northEnd3, c4.getDestinationTrack());

        // there's also a global setting allowing trains to return to staging
        train1.setAllowReturnToStagingEnabled(false);
        Setup.setAllowReturnToStagingEnabled(true);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // no room for cars at yard, both returned to staging
        Assert.assertEquals("c3 destination track", northEnd3, c3.getDestinationTrack());
        Assert.assertEquals("c4 destination track", northEnd3, c4.getDestinationTrack());
    }

    /**
     * test cars returning to staging when train is a turn. Generate custom
     * loads
     */
    @Test
    public void testStagingtoStagingCustomLoadsG() {

        JUnitOperationsUtil.initOperationsData();

        // register the car loads used
        cld.addName("Boxcar", "Flour");
        cld.addName("Boxcar", "Bags");

        // Boxcars with "E" load in staging
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        Location northEndStaging = lmanager.getLocationById("1");
        Track northEndStaging1 = northEndStaging.getTrackByName("North End 1", Track.STAGING);

        Location northIndustries = lmanager.getLocationById("20");
        Track niYard = northIndustries.getTrackById("20s1");

        Location southEndStaging = lmanager.getLocationById("3");
        Track southEndStaging1 = southEndStaging.getTrackByName("South End 1", Track.STAGING);
        Track southEndStaging2 = southEndStaging.getTrackByName("South End 2", Track.STAGING);

        // add a third staging track since the first two have cars on them
        Track northEndStaging3 = northEndStaging.addTrack("North End 3", Track.STAGING);
        northEndStaging3.setLength(500);

        // create a route that returns to staging
        Route route = rmanager.newRoute("NorthEnd-NI-NorthEnd");
        route.addLocation(northEndStaging);
        route.addLocation(northIndustries);
        route.addLocation(northEndStaging);

        Train train1 = tmanager.newTrain("Test turn to staging");
        train1.setRoute(route);

        // allow staging to generate custom loads for cars
        northEndStaging1.setAddCustomLoadsEnabled(true);
        northEndStaging1.setAddCustomLoadsAnyStagingTrackEnabled(true);

        // bias staging track selection so the cars return
        southEndStaging1.deleteTypeName("Boxcar");
        southEndStaging2.deleteTypeName("Boxcar");

        train1.setAllowReturnToStagingEnabled(true);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        Assert.assertEquals("c3 destination track", northEndStaging3, c3.getDestinationTrack());
        Assert.assertEquals("c4 destination track", northEndStaging3, c4.getDestinationTrack());

        // check load
        Assert.assertNotEquals("car load is not", "E", c3.getLoadName());
        Assert.assertNotEquals("car load is not", "E", c4.getLoadName());

        // there's also a global setting allowing trains to return to staging
        train1.setAllowReturnToStagingEnabled(false);
        Setup.setAllowReturnToStagingEnabled(true);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        Assert.assertEquals("c3 destination track", northEndStaging3, c3.getDestinationTrack());
        Assert.assertEquals("c4 destination track", northEndStaging3, c4.getDestinationTrack());

        // check load
        Assert.assertNotEquals("car load is not", "E", c3.getLoadName());
        Assert.assertNotEquals("car load is not", "E", c4.getLoadName());

        // now test that returning to staging isn't acceptable
        Setup.setAllowReturnToStagingEnabled(false);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        Assert.assertEquals("c3 destination track", niYard, c3.getDestinationTrack());
        Assert.assertEquals("c4 destination track", niYard, c4.getDestinationTrack());

        // check load
        Assert.assertEquals("car load", "E", c3.getLoadName());
        Assert.assertEquals("car load", "E", c4.getLoadName());
    }

    /**
     * test cars returning to staging when train is a turn. Build mode
     * aggressive, which allows the train to return to the same depart track.
     */
    @Test
    public void testStagingtoStagingTrainTurnB() {

        Setup.setBuildAggressive(true);

        JUnitOperationsUtil.initOperationsData();

        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        Car c7 = cmanager.getByRoadAndNumber("CP", "777");
        Car c8 = cmanager.getByRoadAndNumber("CP", "888");
        Car c9 = cmanager.getByRoadAndNumber("CP", "99");

        // for better code coverage, we need a kernel out of staging
        Kernel k2 = cmanager.newKernel("TwoCars");
        c3.setKernel(k2);
        c4.setKernel(k2);

        Location northEndStaging = lmanager.getLocationById("1");
        Location northIndustries = lmanager.getLocationById("20");

        Track northEndStaging1 = northEndStaging.getTrackById("1s1");
        Track yardNI = northIndustries.getTrackById("20s1");

        // create a route that returns to staging
        Route route = rmanager.newRoute("NorthEnd-NI-NorthEnd");
        route.addLocation(northEndStaging);
        route.addLocation(northIndustries);
        route.addLocation(northEndStaging);

        Train train1 = tmanager.newTrain("TestStagingtoStagingTrainTurnB");
        train1.setRoute(route);

        // train not should build, option to return is false
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train 1 status", train1.isBuilt());

        // allow train to return to the same track
        Setup.setStagingTrackImmediatelyAvail(true);

        // limit staging length to the expected length of train terminating into staging
        northEndStaging1.setLength(284);
        northEndStaging1.setMoves(-10); // always use this track for this test

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("destination track", northEndStaging1, c1.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("destination track", yardNI, c3.getDestinationTrack());
        Assert.assertEquals("destination track", yardNI, c4.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c7.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c8.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c9.getDestinationTrack());

        // when a train can return, the cars on the staging track remain
        train1.setAllowReturnToStagingEnabled(true);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("destination track", northEndStaging1, c1.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("destination track", yardNI, c3.getDestinationTrack());
        Assert.assertEquals("destination track", yardNI, c4.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c7.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c8.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c9.getDestinationTrack());
    }

    /**
     * Test caboose generate custom load returning to same staging track
     */
    @Test
    public void testStagingtoStagingTrainTurnCabooseWithCustomLoad() {

        Setup.setBuildAggressive(true);

        JUnitOperationsUtil.initOperationsData();

        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        Car c7 = cmanager.getByRoadAndNumber("CP", "777");
        Car c8 = cmanager.getByRoadAndNumber("CP", "888");
        Car c9 = cmanager.getByRoadAndNumber("CP", "99");

        Location northEndStaging = lmanager.getLocationById("1");
        Location northIndustries = lmanager.getLocationById("20");

        Track northEndStaging1 = northEndStaging.getTrackById("1s1");
        // test requires only one staging track
        northEndStaging.deleteTrack(northEndStaging.getTrackById("1s2"));
        Assert.assertEquals("confirm only one staging track", 1, northEndStaging.getTrackList().size());

        Track yardNI = northIndustries.getTrackById("20s1");

        // create a route that returns to staging
        Route route = rmanager.newRoute("NorthEnd-NI-NorthEnd");
        route.addLocation(northEndStaging);
        route.addLocation(northIndustries);
        route.addLocation(northEndStaging);

        Train train1 = tmanager.newTrain("TestStagingtoStagingTrainTurn");
        train1.setRoute(route);

        // allow train to return to the same track
        Setup.setStagingTrackImmediatelyAvail(true);
        // allow staging to generate custom loads for cars
        northEndStaging1.setAddCustomLoadsEnabled(true);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("destination track", northEndStaging1, c1.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("destination track", yardNI, c3.getDestinationTrack());
        Assert.assertEquals("destination track", yardNI, c4.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c7.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c8.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c9.getDestinationTrack());

        // don't allow "E" or "L" loads into staging
        northEndStaging1.setLoadOption(Track.EXCLUDE_LOADS);
        northEndStaging1.addLoadName("E");
        northEndStaging1.addLoadName("L");

        cld.addName("Caboose", "Crew");

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        Assert.assertEquals("Confirm caboose load", "Crew", c1.getLoadName());
        Assert.assertEquals("Confirm caboose load", "Crew", c2.getLoadName());

        // confirm car destinations
        Assert.assertEquals("destination track", northEndStaging1, c1.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("destination track", yardNI, c3.getDestinationTrack());
        Assert.assertEquals("destination track", yardNI, c4.getDestinationTrack());
        Assert.assertEquals("destination track", null, c7.getDestinationTrack());
        Assert.assertEquals("destination track", null, c8.getDestinationTrack());
        Assert.assertEquals("destination track", null, c9.getDestinationTrack());

    }

    /**
     * Test to see if staging track becomes available once train is built in
     * aggressive mode
     */
    @Test
    public void testStagingtoStagingAggressiveMode() {

        Setup.setBuildAggressive(true);
        Setup.setStagingTrackImmediatelyAvail(true);

        JUnitOperationsUtil.initOperationsData();

        // route North End - NI - South End
        Train train2 = tmanager.getTrainById("2");

        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099");
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099");
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001");
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002");

        Car c7 = cmanager.getByRoadAndNumber("CP", "777");
        Car c8 = cmanager.getByRoadAndNumber("CP", "888");
        Car c9 = cmanager.getByRoadAndNumber("CP", "99");

        Location northEndStaging = lmanager.getLocationById("1");
        Location northIndustries = lmanager.getLocationById("20");
        Location southEndStaging = lmanager.getLocationById("3");

        Track yardNI = northIndustries.getTrackById("20s1");
        Track southEndStaging1 = southEndStaging.getTrackById("3s1");
        Track southEndStaging2 = southEndStaging.getTrackById("3s2");

        // add staging track to North End
        Track northEndStaging3 = northEndStaging.addTrack("North End Staging 3", Track.STAGING);
        northEndStaging3.setLength(300);

        // place cars on south end staging       
        Car c10 = JUnitOperationsUtil.createAndPlaceCar("A", "10", "Boxcar", "40", southEndStaging1, 0);
        Car c11 = JUnitOperationsUtil.createAndPlaceCar("A", "11", "Boxcar", "40", southEndStaging2, 1);

        // create a route that staging South end
        Route route = rmanager.newRoute("SouthEnd-NI-NorthEnd");
        route.addLocation(southEndStaging);
        route.addLocation(northIndustries);
        route.addLocation(northEndStaging);

        Train train3 = tmanager.newTrain("TestStagingtoStaging");
        train3.setRoute(route);

        // train not should build, no track available terminate staging
        Assert.assertFalse(new TrainBuilder().build(train2));
        Assert.assertFalse("Train status", train2.isBuilt());

        // now build a train departing south end staging, should free up a staging track
        Assert.assertTrue(new TrainBuilder().build(train3));
        Assert.assertTrue("Train status", train3.isBuilt());

        // train should now build, train3 built, freeing up staging
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train status", train2.isBuilt());

        // confirm car destinations
        Assert.assertEquals("destination track", southEndStaging1, c1.getDestinationTrack());
        Assert.assertEquals("destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("destination track", yardNI, c3.getDestinationTrack());
        Assert.assertEquals("destination track", southEndStaging1, c4.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging3, c7.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging3, c8.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging3, c9.getDestinationTrack());
        Assert.assertEquals("destination track", northEndStaging3, c10.getDestinationTrack());
        Assert.assertEquals("destination track", null, c11.getDestinationTrack());
    }

    /**
     * Tests normal build train option
     */
    @Test
    public void testStagingtoStagingTrainBuildNormal() {

        // confirm default
        Assert.assertFalse(Setup.isBuildAggressive());
        // and make true
        Setup.setBuildAggressive(true);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");

        RouteLocation rlNI = train1.getRoute().getRouteLocationBySequenceNumber(2);

        // confirm we got the right location
        Assert.assertEquals("2nd location in train's route", "North Industries", rlNI.getLocation().getName());
        rlNI.setMaxTrainLength(200); // train departs staging at 160 feet, so no room for any pulls

        // one car spotted at NI and one car pull from NI
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // cause a build failure by not allowing any set outs at North Industries, causes a train length issue
        rlNI.setDropAllowed(false);

        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());

        // should build if normal is used, cars out of staging are pulled first
        train1.setBuildTrainNormalEnabled(true);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());
    }

    /**
     * Tests staging in the middle of a route
     */
    @Test
    public void testStagingToStagingToStaging() {

        Setup.setBuildAggressive(true);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");

        Location lNI = train1.getRoute().getRouteLocationBySequenceNumber(2).getLocation();
        Location southEndStaging = train1.getRoute().getTerminatesRouteLocation().getLocation();

        // confirm we got the right location
        Assert.assertEquals("2nd location in train's route", "North Industries", lNI.getName());

        // make it staging
        lNI.setLocationOps(Location.STAGING);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm no cars set out or pulled from NI
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099"); // on staging track north end 1
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099"); // on staging track north end 1
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001"); // on staging track north end 1
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002"); // on staging track north end 1
        Car c5 = cmanager.getByRoadAndNumber("CP", "X20001"); // on staging track north end 2
        Car c6 = cmanager.getByRoadAndNumber("CP", "X20002"); // on staging track north end 2
        Car c7 = cmanager.getByRoadAndNumber("CP", "777"); // at NI, Flat not serviced by train
        Car c8 = cmanager.getByRoadAndNumber("CP", "888"); // at NI
        Car c9 = cmanager.getByRoadAndNumber("CP", "99"); // at NI, Flat not serviced by train

        Assert.assertEquals("confirm destination", southEndStaging, c1.getDestination());
        Assert.assertEquals("confirm destination", southEndStaging, c2.getDestination());
        Assert.assertEquals("confirm destination", southEndStaging, c3.getDestination());
        Assert.assertEquals("confirm destination", southEndStaging, c4.getDestination());
        Assert.assertEquals("confirm destination", null, c5.getDestination());
        Assert.assertEquals("confirm destination", null, c6.getDestination());
        Assert.assertEquals("confirm destination", null, c7.getDestination());
        Assert.assertEquals("confirm destination", null, c8.getDestination());
        Assert.assertEquals("confirm destination", null, c9.getDestination());
    }

    /**
     * Tests the creation of the CSV manifest file
     */
    @Test
    public void testCreateCsvManifest() {

        Setup.setGenerateCsvManifestEnabled(true);

        // note any changes to operations data could change the number of lines in the csv file
        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");

        Assert.assertTrue(new TrainBuilder().build(train1));

        File csvManifestFile =
                InstanceManager.getDefault(TrainManagerXml.class).getTrainCsvManifestFile(train1.getName());
        Assert.assertTrue(csvManifestFile.exists());

        BufferedReader in = JUnitOperationsUtil.getBufferedReader(csvManifestFile);
        Assert.assertEquals("confirm number of lines in csv manifest", 39, in.lines().count());

    }

    /**
     * Tests DISPLAY_CAR_LIMIT_20 which controls how many cars in staging are
     * displayed in the build report.
     * 
     */
    @Test
    public void testDisplayLimit20() {

        Assert.assertEquals("Confirm number of cars to display", 20, TrainBuilder.DISPLAY_CAR_LIMIT_20);

        // note any changes to operations data could change the number of lines in the build report
        JUnitOperationsUtil.initOperationsData();

        // place 30 cars in staging
        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");
        northEndStaging1.setLength(34 * 50); // 4 cars already + 30 more

        for (int i = 0; i < 30; i++) {
            JUnitOperationsUtil.createAndPlaceCar("BB", Integer.toString(i), "Boxcar", "40", northEndStaging1, 0);
        }

        Train train1 = tmanager.getTrainById("1");
        // need to increase train length and number of moves out of staging for staging track selection
        Route route = train1.getRoute();
        RouteLocation rlNorthEnd = route.getDepartsRouteLocation();
        rlNorthEnd.setMaxCarMoves(50);
        rlNorthEnd.setMaxTrainLength(1500);

        // build should fail, too many cars in staging
        Assert.assertFalse(new TrainBuilder().build(train1));

        // confirm by checking number of lines in the build report
        File buildReport =
                InstanceManager.getDefault(TrainManagerXml.class).getTrainBuildReportFile(train1.getName());
        Assert.assertTrue(buildReport.exists());
        BufferedReader in = JUnitOperationsUtil.getBufferedReader(buildReport);

        // any changes to the build report could cause this to fail
        Assert.assertEquals("confirm number of lines in build report", 459, in.lines().count());

        //TODO search and confirm limit message in build report
    }

    /**
     * Tests DISPLAY_CAR_LIMIT_50 which controls how many cars are displayed per
     * location in the build report.
     */
    @Test
    public void testDisplayLimit50() {

        Assert.assertEquals("Confirm number of cars to display", 50, TrainBuilder.DISPLAY_CAR_LIMIT_50);

        // note any changes to operations data could change the number of lines in the build report
        JUnitOperationsUtil.initOperationsData();

        // place 75 cars in NI yard 2
        Location locationNI = lmanager.getLocationById("20");
        Track NIyard2 = locationNI.addTrack("NI Yard 2", Track.YARD);
        NIyard2.setLength(75 * 50);

        for (int i = 0; i < 75; i++) {
            JUnitOperationsUtil.createAndPlaceCar("BB", Integer.toString(i), "Boxcar", "40", NIyard2, 0);
        }

        Train train1 = tmanager.getTrainById("1");
        Assert.assertTrue(new TrainBuilder().build(train1));

        // confirm by checking number of lines in the build report
        File buildReport =
                InstanceManager.getDefault(TrainManagerXml.class).getTrainBuildReportFile(train1.getName());
        Assert.assertTrue(buildReport.exists());
        BufferedReader in = JUnitOperationsUtil.getBufferedReader(buildReport);

        // any changes to the build report could cause this to fail
        Assert.assertEquals("confirm number of lines in build report", 250, in.lines().count());

        //TODO search and confirm limit message in build report
    }

    /**
     * Tests DISPLAY_CAR_LIMIT_100. Used to control how many cars are displayed
     * in the build report. DISPLAY_CAR_LIMIT_100 is used in two places, at the
     * start of the build report when listing the cars and removing the ones
     * that the train won't serve. And at the completion of finding destinations
     * for cars, lists how many cars were ignored due to move counts being used
     * up.
     */
    @Test
    public void testDisplayLimit100() {

        Assert.assertEquals("Confirm number of cars to display", 100, TrainBuilder.DISPLAY_CAR_LIMIT_100);

        // note any changes to operations data could change the number of lines in the build report
        JUnitOperationsUtil.initOperationsData();

        // place 125 Boxcar in NI yard 2
        Location locationNI = lmanager.getLocationById("20");
        Track NIyard2 = locationNI.addTrack("NI Yard 2", Track.YARD);
        NIyard2.setLength(125 * 50);

        for (int i = 0; i < 125; i++) {
            JUnitOperationsUtil.createAndPlaceCar("BB", Integer.toString(i), "Boxcar", "40", NIyard2, 0);
        }

        // place 125 "BOXCAR" in NI yard 3, "BOXCAR" isn't serviced by train
        Track NIyard3 = locationNI.addTrack("NI Yard 3", Track.YARD);
        NIyard3.setLength(125 * 50);
        locationNI.addTypeName("BOXCAR");
        NIyard3.addTypeName("BOXCAR");

        for (int i = 0; i < 125; i++) {
            JUnitOperationsUtil.createAndPlaceCar("BC", Integer.toString(i), "BOXCAR", "40", NIyard3, 0);
        }

        Train train1 = tmanager.getTrainById("1");
        Assert.assertTrue(new TrainBuilder().build(train1));

        // confirm by checking number of lines in the build report
        File buildReport =
                InstanceManager.getDefault(TrainManagerXml.class).getTrainBuildReportFile(train1.getName());
        Assert.assertTrue(buildReport.exists());
        BufferedReader in = JUnitOperationsUtil.getBufferedReader(buildReport);

        // any changes to the build report could cause this to fail
        Assert.assertEquals("confirm number of lines in build report", 375, in.lines().count());

        //TODO search and confirm limit message in build report
    }

    /**
     * Test prompt from staging, doesn't actually cause the user prompt to
     * appear, that would be part of the GUI tests.
     */
    @Test
    public void testStagingPromptFrom() {

        Setup.setPromptFromStagingEnabled(true);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");

        // only one of the staging tracks has engines and cars, so no prompt
        Assert.assertTrue(new TrainBuilder().build(train1));

        // request a staging track with 2 engines, doesn't exist, so build failure
        train1.setNumberEngines("2");
        Assert.assertFalse(new TrainBuilder().build(train1));

        // place two engines on departure track        
        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");

        Location locationNorthEnd = lmanager.getLocationById("1");
        Track l1staging1 = locationNorthEnd.getTrackById("1s1");

        Assert.assertEquals("place engine on staging track", Track.OKAY, e1.setLocation(locationNorthEnd, l1staging1));
        Assert.assertEquals("place engine on staging track", Track.OKAY, e2.setLocation(locationNorthEnd, l1staging1));

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));

        // cause build failure by making staging track too short
        Location locationSouthEnd = lmanager.getLocationById("3");
        Track l3staging1 = locationSouthEnd.getTrackById("3s1");

        l3staging1.setLength(80);

        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));

    }

    /**
     * Test prompt to staging, doesn't actually cause the user prompt to appear,
     * that would be part of the GUI tests.
     */
    @Test
    public void testStagingPromptTo() {

        Setup.setPromptToStagingEnabled(true);

        JUnitOperationsUtil.initOperationsData();

        Train train1 = tmanager.getTrainById("1");

        // there are two staging tracks available at the terminal
        // must disable one or the prompt would appear

        Location locationSouthEnd = lmanager.getLocationById("3");
        Track staging1 = locationSouthEnd.getTrackById("3s1");
        staging1.deleteTypeName("Boxcar");

        // only staging track 2 can receive the train
        Assert.assertTrue(new TrainBuilder().build(train1));

        // now disable track 2, should cause build failure
        Track staging2 = locationSouthEnd.getTrackById("3s2");
        staging2.deleteTypeName("Boxcar");

        Assert.assertTrue(train1.reset());
        Assert.assertFalse(new TrainBuilder().build(train1));

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

        // register the car loads used
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
        e1 = emanager.newRS("CP", "5501");
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
        e2 = emanager.newRS("CP", "5888");
        e2.setModel("GP40");
        Assert.assertEquals("Test Engine CP5888 SetLocation 2s4", "okay", e2.setLocation(loc2, loc2trke2));

        // Create cars used
        Car b1;
        b1 = cmanager.newRS("CP", "81234567");
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
        b2 = cmanager.newRS("CP", "81234568");
        b2.setTypeName(carTypes[1]);
        b2.setLength("40");
        b2.setMoves(5);
        Assert.assertEquals("Bob Test Test Car CP81234568 SetLocation 2s4", "okay", b2.setLocation(loc2,
                loc2trk4));

        Car b3;
        b3 = cmanager.newRS("CP", "81234569");
        b3.setTypeName(carTypes[1]);
        b3.setLength("40");
        b3.setLoadName("Flour");
        b3.setMoves(5);
        Assert.assertEquals("Bob Test Test Car CP81234569 SetLocation 2s4", "okay", b3.setLocation(loc2,
                loc2trk4));

        Car b4;
        b4 = cmanager.newRS("CP", "81234566");
        b4.setTypeName(carTypes[1]);
        b4.setLength("40");
        b4.setLoadName("Bags");
        b4.setMoves(5);
        Assert.assertEquals("Bob Test Test Car CP81234566 SetLocation 2s4", "okay", b4.setLocation(loc2,
                loc2trk4));

        Car b5;
        b5 = cmanager.newRS("CP", "71234567");
        b5.setTypeName(carTypes[1]);
        b5.setLength("40");
        // b5.setLoad("E");
        Assert.assertEquals("Bob Test Test Car CP71234567 SetLocation 2s4", "okay", b5.setLocation(loc2,
                loc2trk3));

        Car b6;
        b6 = cmanager.newRS("CP", "71234568");
        b6.setTypeName(carTypes[1]);
        b6.setLength("40");
        // b6.setLoad("E");
        Assert.assertEquals("Bob Test Test Car CP71234568 SetLocation 2s4", "okay", b6.setLocation(loc2,
                loc2trk3));

        Car b7;
        b7 = cmanager.newRS("CP", "71234569");
        b7.setTypeName(carTypes[1]);
        b7.setLength("40");
        // b7.setLoad("E");
        Assert.assertEquals("Bob Test Test Car CP71234569 SetLocation 2s4", "okay", b7.setLocation(loc2,
                loc2trk3));

        Car b8;
        b8 = cmanager.newRS("CP", "71234566");
        b8.setTypeName(carTypes[1]);
        b8.setLength("40");
        Assert.assertEquals("Bob Test Test Car CP71234566 SetLocation 2s4", "okay", b8.setLocation(loc2,
                loc2trk3));

        // Create cars used
        Car c1;
        c1 = cmanager.newRS("CP", "12345678");
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
        c2 = cmanager.newRS("CP", "12345679");
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
        Assert.assertTrue(new TrainBuilder().build(train1));
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));

        Assert.assertTrue("Bob test train1 built", train1.isBuilt());
        Assert.assertTrue("Bob test train2 built", train2.isBuilt());

    }

    /**
     * Test the generation of custom loads out staging. Case where no tracks
     * available, and only destination is staging requesting cars with custom
     * loads.
     */
    @Test
    public void testStagingtoStagingCustomLoadsA() {

        JUnitOperationsUtil.initOperationsData();

        // register the car loads used
        cld.addName("Boxcar", "Flour");
        cld.addName("Boxcar", "Bags");

        // Route Northend - NI - Southend
        Train train1 = tmanager.getTrainById("1");

        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");

        Location locationSouthEnd = lmanager.getLocationById("3");
        Track southEndStaging1 = locationSouthEnd.getTrackById("3s1");
        Track southEndStaging2 = locationSouthEnd.getTrackById("3s2");
        southEndStaging2.setMoves(100); // don't use this track

        // don't allow cars with the default "E" load to terminate into staging
        southEndStaging1.setLoadOption(Track.EXCLUDE_LOADS);
        southEndStaging1.addLoadName("E");
        southEndStaging1.addLoadName("Flour");
        southEndStaging2.setLoadOption(Track.EXCLUDE_LOADS);
        southEndStaging2.addLoadName("E");
        southEndStaging1.addLoadName("Flour");

        Location locationNI = lmanager.getLocationById("20");
        Track yardNI = locationNI.getTrackById("20s1");

        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099"); // on staging track north end 1
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099"); // on staging track north end 1
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001"); // on staging track north end 1
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002"); // on staging track north end 1

        // both cabooses need to have loads accepted by staging
        c1.setLoadName("L");
        c2.setLoadName("L");

        // allow staging to generate custom loads for cars
        northEndStaging1.setAddCustomLoadsEnabled(true);

        // build should fail, train can accept car load "E", but staging can't
        Assert.assertFalse(new TrainBuilder().build(train1));

        // now allow restrictive staging tracks
        Setup.setTrainIntoStagingCheckEnabled(false);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));

        Assert.assertEquals("car destination track", southEndStaging1, c1.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", yardNI, c3.getDestinationTrack());
        Assert.assertEquals("car destination track", yardNI, c4.getDestinationTrack());

        // now eliminate NI yard as a possible destination
        yardNI.deleteTypeName("Boxcar");

        // build should work
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));

        Assert.assertEquals("car destination track", southEndStaging1, c1.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c3.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c4.getDestinationTrack());

        // the only valid custom load is Bags, Flour is rejected
        Assert.assertEquals("car's load", "Bags", c3.getLoadName());
        Assert.assertEquals("car's load", "Bags", c4.getLoadName());

        // try other generate custom loads out of staging
        northEndStaging1.setAddCustomLoadsEnabled(false);
        northEndStaging1.setAddCustomLoadsAnySpurEnabled(true);

        // build should work
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));

        Assert.assertEquals("car destination track", southEndStaging1, c1.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c3.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c4.getDestinationTrack());

        // the only valid custom load is Bags, Flour is rejected
        Assert.assertEquals("car's load", "Bags", c3.getLoadName());
        Assert.assertEquals("car's load", "Bags", c4.getLoadName());

        // now eliminate the only custom load that is accepted by staging.
        southEndStaging1.addLoadName("Bags");

        // should fail
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());
    }

    /**
     * Test the generation of custom loads out staging. Case where no tracks
     * available, and only destination is staging.
     */
    @Test
    public void testStagingtoStagingCustomLoadsB() {

        // create extra staging locations that aren't reachable
        // improves test coverage
        JUnitOperationsUtil.createFourStagingLocations();

        JUnitOperationsUtil.initOperationsData();

        // register the car loads used
        cld.addName("Boxcar", "Flour");
        cld.addName("Boxcar", "Bags");

        // train route North End - NI - South End
        Train train1 = tmanager.getTrainById("1");
        Train train2 = tmanager.getTrainById("2");

        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");
        Track northEndStaging2 = locationNorthEnd.getTrackById("1s2");

        Location locationNI = lmanager.getLocationById("20");
        Track yardNI = locationNI.getTrackById("20s1");

        Location locationSouthEnd = lmanager.getLocationById("3");
        Track southEndStaging1 = locationSouthEnd.getTrackById("3s1");
        Track southEndStaging2 = locationSouthEnd.getTrackById("3s2");
        southEndStaging2.setMoves(100); // bias this track

        // don't allow cars with the load "Flour" to depart staging
        northEndStaging1.setShipLoadOption(Track.EXCLUDE_LOADS);
        northEndStaging1.addShipLoadName("Flour");

        // allow staging to generate custom loads for cars
        northEndStaging1.setAddCustomLoadsAnyStagingTrackEnabled(true);
        northEndStaging2.setAddCustomLoadsAnyStagingTrackEnabled(true); // used by train 2

        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099"); // on staging track north end 1
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099"); // on staging track north end 1
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001"); // on staging track north end 1
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002"); // on staging track north end 1
        Car c5 = cmanager.getByRoadAndNumber("CP", "X20001"); // on staging track north end 2
        Car c6 = cmanager.getByRoadAndNumber("CP", "X20002"); // on staging track north end 2

        Assert.assertTrue(new TrainBuilder().build(train1));

        Assert.assertEquals("car destination track", southEndStaging1, c1.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c3.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c4.getDestinationTrack());

        // the only valid custom load is Bags, Flour is not allowed
        Assert.assertEquals("car's load", "Bags", c3.getLoadName());
        Assert.assertEquals("car's load", "Bags", c4.getLoadName());

        // caboose should be able to depart with load "E"
        Assert.assertEquals("car's load", "E", c1.getLoadName());
        Assert.assertEquals("car's load", "E", c2.getLoadName());

        // now don't allow through cars
        train1.setAllowThroughCarsEnabled(false);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("train1 built", train1.isBuilt());

        Assert.assertEquals("car destination track", southEndStaging1, c1.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", yardNI, c3.getDestinationTrack());
        Assert.assertEquals("car destination track", yardNI, c4.getDestinationTrack());

        // only allow cars to travel to yardNI
        northEndStaging2.setDestinationOption(Track.INCLUDE_DESTINATIONS);
        northEndStaging2.addDestination(locationNI);

        // use train 2, it doesn't require a caboose
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("train1 built", train2.isBuilt());

        Assert.assertEquals("car destination track", yardNI, c5.getDestinationTrack());
        Assert.assertEquals("car destination track", yardNI, c6.getDestinationTrack());
    }

    /**
     * Test the generation of custom loads out staging. Case where no tracks
     * available, and only destination is staging. Train restrictions which
     * loads can be carried
     */
    @Test
    public void testStagingtoStagingCustomLoadsC() {

        JUnitOperationsUtil.initOperationsData();

        // register the car loads used
        cld.addName("Boxcar", "Flour");
        cld.addName("Boxcar", "Bags");

        // train route North End - NI - South End
        Train train1 = tmanager.getTrainById("1");

        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");

        Location locationNI = lmanager.getLocationById("20");
        Track yardNI = locationNI.getTrackById("20s1");
        // eliminate NI yard as a possible destination
        yardNI.deleteTypeName("Boxcar");

        Location locationSouthEnd = lmanager.getLocationById("3");
        Track southEndStaging1 = locationSouthEnd.getTrackById("3s1");
        Track southEndStaging2 = locationSouthEnd.getTrackById("3s2");
        southEndStaging2.setMoves(100); // don't use this track

        // don't allow cars with the default "E" load to depart staging
        northEndStaging1.setShipLoadOption(Track.EXCLUDE_LOADS);
        northEndStaging1.addShipLoadName("E");

        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099"); // on staging track north end 1
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099"); // on staging track north end 1
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001"); // on staging track north end 1
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002"); // on staging track north end 1

        // allow staging to generate custom loads for cars
        northEndStaging1.setAddCustomLoadsAnyStagingTrackEnabled(true);

        // configure train to not carry Flour
        train1.setLoadOption(Train.EXCLUDE_LOADS);
        train1.addLoadName("Flour");
        train1.addLoadName("E"); // should ignore caboose load of "E"
        Assert.assertTrue(new TrainBuilder().build(train1));

        Assert.assertEquals("car destination track", southEndStaging1, c1.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c3.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c4.getDestinationTrack());

        // the only valid custom load is Bags, Flour is not allowed by train
        Assert.assertEquals("car's load", "Bags", c3.getLoadName());
        Assert.assertEquals("car's load", "Bags", c4.getLoadName());

        // caboose should be able to depart with load "E"
        Assert.assertEquals("car's load", "E", c1.getLoadName());
        Assert.assertEquals("car's load", "E", c2.getLoadName());

        // now eliminate the only custom load left.
        northEndStaging1.addShipLoadName("Bags");

        // should fail
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
    }

    /*
     * Test the loading of custom load into cars departing staging and
     * terminating into staging. Note that the generation of custom loads is
     * random.
     */
    @Test
    public void testStagingtoStagingCustomLoadsD() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // register the car loads used
        cld.addName(carTypes[1], "Flour");
        cld.addName(carTypes[1], "Bags");
        cld.setPriority(carTypes[1], "Flour", CarLoad.PRIORITY_HIGH);

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

        // Create cars used
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", carTypes[1], "32", loc1trk1, 5);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", carTypes[1], "40", loc1trk1, 5);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", carTypes[1], "40", loc1trk1, 5);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", carTypes[1], "40", loc1trk1, 5);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", carTypes[1], "40", loc1trk1, 5);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", carTypes[1], "50", loc1trk1, 16);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", carTypes[1], "60", loc1trk1, 17);
        Car c9 = JUnitOperationsUtil.createAndPlaceCar("CP", "90", carTypes[5], "40", loc2trk1, 18);
        Car c10 = JUnitOperationsUtil.createAndPlaceCar("CP", "100", carTypes[5], "40", loc2trk1, 19);
        Car c11 = JUnitOperationsUtil.createAndPlaceCar("CP", "110", carTypes[5], "40", loc2trk1, 19);

        c3.setLoadName("L");
        c5.setLoadName("Flour");
        c6.setLoadName("Bags");

        // Create cabooses
        c1.setCaboose(true);

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
        train1.setRequirements(Train.CABOOSE);

        // Build trains
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("train1 built", train1.isBuilt());
        Assert.assertEquals("train1 built", Train.CODE_BUILT, train1.getStatusCode());

        // confirm that custom loads have been added to the cars departing staging
        Assert.assertEquals("load shouldn't change", "L", c3.getLoadName());
        Assert.assertEquals("load shouldn't change", "Flour", c5.getLoadName()); // departed with "Flour"
        Assert.assertEquals("load shouldn't change", "Bags", c6.getLoadName()); // departed with "Bags"
        Assert.assertEquals("load shouldn't change", "E", c9.getLoadName()); // not a 'Boxcar", so no custom load
        Assert.assertEquals("load shouldn't change", "E", c10.getLoadName()); // not a 'Boxcar"
        Assert.assertEquals("load shouldn't change", "E", c11.getLoadName()); // not a 'Boxcar"

        Assert.assertNotEquals("Generated custom load", "E", c4.getLoadName()); // Boxcar with random custom load
        Assert.assertNotEquals("Generated custom load", "E", c7.getLoadName()); // Boxcar with random custom load
        Assert.assertNotEquals("Generated custom load", "E", c8.getLoadName()); // Boxcar with random custom load
    }

    /**
     * Test cars departing staging with "E" load with a destination or final
     * destination do not get a custom load
     */
    @Test
    public void testStagingtoStagingCustomLoadsE() {

        JUnitOperationsUtil.initOperationsData();

        // register the car loads used
        cld.addName("Boxcar", "Flour");
        cld.addName("Boxcar", "Bags");

        // train route North End - NI - South End
        Train train1 = tmanager.getTrainById("1");

        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");

        Location locationNI = lmanager.getLocationById("20");
        Track yardNI = locationNI.getTrackById("20s1");

        Location locationSouthEnd = lmanager.getLocationById("3");
        Track southEndStaging1 = locationSouthEnd.getTrackById("3s1");

        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099"); // on staging track north end 1
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099"); // on staging track north end 1
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001"); // on staging track north end 1
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002"); // on staging track north end 1

        // give c3 a destination
        c3.setDestination(locationNI);
        // give c4 a final destination
        c4.setFinalDestination(locationNI);

        // allow staging to generate custom loads for cars
        northEndStaging1.setAddCustomLoadsAnyStagingTrackEnabled(true);
        // there aren't any spurs with schedules in this test
        northEndStaging1.setAddCustomLoadsAnySpurEnabled(true);

        Assert.assertTrue(new TrainBuilder().build(train1));

        Assert.assertEquals("car destination track", southEndStaging1, c1.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", yardNI, c3.getDestinationTrack());
        Assert.assertEquals("car destination track", yardNI, c4.getDestinationTrack());

        // Car's had a destination or final destination, so no custom load generation
        Assert.assertEquals("car's load", "E", c3.getLoadName());
        Assert.assertEquals("car's load", "E", c4.getLoadName());

        // caboose should be able to depart with load "E"
        Assert.assertEquals("car's load", "E", c1.getLoadName());
        Assert.assertEquals("car's load", "E", c2.getLoadName());
    }

    /**
     * Test custom loads into staging, exceptions car type, road.
     */
    @Test
    public void testStagingtoStagingCustomLoadsF() {

        JUnitOperationsUtil.initOperationsData();

        // register the car loads used
        cld.addName("Boxcar", "Flour");
        cld.addName("Boxcar", "Bags");

        cld.addName("Flat", "Bricks");
        cld.addName("Flat", "Steel");

        cld.addName(rb.getString("Caboose"), "Crew");

        // Route Northend - NI - Southend
        Train train2 = tmanager.getTrainById("2");
        Route route = train2.getRoute();

        // increase the number of moves to 5 for NI
        RouteLocation rlNI = route.getRouteLocationBySequenceNumber(2);
        rlNI.setMaxCarMoves(5);

        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");

        Location locationSouthEnd = lmanager.getLocationById("3");
        Track southEndStaging1 = locationSouthEnd.getTrackById("3s1");
        Track southEndStaging2 = locationSouthEnd.getTrackById("3s2");

        locationSouthEnd.deleteTrack(southEndStaging2); // don't use this track

        // don't allow cars with the default "E" load to terminate into staging
        southEndStaging1.setLoadOption(Track.EXCLUDE_LOADS);
        southEndStaging1.addLoadName("E");
        southEndStaging1.addLoadName("Flour");
        southEndStaging1.addLoadName("Steel");

        Location locationNI = lmanager.getLocationById("20");
        Track yardNI = locationNI.getTrackById("20s1");

        // cars in staging have "E" loads, inclusing caboose
        Car c1 = cmanager.getByRoadAndNumber("CP", "C10099"); // on staging track north end 1
        Car c2 = cmanager.getByRoadAndNumber("CP", "C20099"); // on staging track north end 1
        Car c3 = cmanager.getByRoadAndNumber("CP", "X10001"); // on staging track north end 1
        Car c4 = cmanager.getByRoadAndNumber("CP", "X10002"); // on staging track north end 1

        Car c10 = JUnitOperationsUtil.createAndPlaceCar("ABC", "10", "Flat", "40", northEndStaging1, 30);
        Car c11 = JUnitOperationsUtil.createAndPlaceCar("CBA", "11", "Flat", "40", northEndStaging1, 30);

        // remove one of the cabooses from staging, route only allows for 5 cars to depart staging
        c1.setLocation(null, null);

        // allow staging to generate custom loads for cars
        northEndStaging1.setAddCustomLoadsEnabled(true);
        northEndStaging1.setAddCustomLoadsAnyStagingTrackEnabled(true);

        // now allow restrictive staging tracks
        Setup.setTrainIntoStagingCheckEnabled(false);

        // train2 doesn't require caboose, so it will be handled like the other cars in staging
        Assert.assertTrue(new TrainBuilder().build(train2));

        // all cars should go to staging
        Assert.assertEquals("car destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c3.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c4.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c10.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c11.getDestinationTrack());

        // all cars should have custom loads
        Assert.assertEquals("car's load", "Crew", c2.getLoadName());
        Assert.assertEquals("car's load", "Bags", c3.getLoadName()); // custom load is Bags, Flour is rejected
        Assert.assertEquals("car's load", "Bags", c4.getLoadName());
        Assert.assertEquals("car's load", "Bricks", c10.getLoadName()); // custom load is Bricks, Steel is rejected
        Assert.assertEquals("car's load", "Bricks", c11.getLoadName());

        // try limiting how many cars can enter staging
        southEndStaging1.setLength(200); // only enough room for 4 cars

        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));

        // all cars should go to staging
        Assert.assertEquals("car destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c3.getDestinationTrack());
        Assert.assertEquals("car destination track", yardNI, c4.getDestinationTrack()); // last car to be processed
        Assert.assertEquals("car destination track", southEndStaging1, c10.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c11.getDestinationTrack());

        // all cars should have custom loads
        Assert.assertEquals("car's load", "Crew", c2.getLoadName());
        Assert.assertEquals("car's load", "Bags", c3.getLoadName()); // custom load is Bags, Flour is rejected
        Assert.assertEquals("car's load", "E", c4.getLoadName());
        Assert.assertEquals("car's load", "Bricks", c10.getLoadName()); // custom load is Bricks, Steel is rejected
        Assert.assertEquals("car's load", "Bricks", c11.getLoadName());

        // don't allow "Boxcar" into staging
        southEndStaging1.deleteTypeName("Boxcar");

        // build should work
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));

        // Check destinatios
        Assert.assertEquals("car destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", yardNI, c3.getDestinationTrack()); // Boxcar
        Assert.assertEquals("car destination track", yardNI, c4.getDestinationTrack()); // Boxcar
        Assert.assertEquals("car destination track", southEndStaging1, c10.getDestinationTrack());
        Assert.assertEquals("car destination track", southEndStaging1, c11.getDestinationTrack());

        // Check car loads
        Assert.assertEquals("car's load", "Crew", c2.getLoadName());
        Assert.assertEquals("car's load", "E", c3.getLoadName());
        Assert.assertEquals("car's load", "E", c4.getLoadName());
        Assert.assertEquals("car's load", "Bricks", c10.getLoadName()); // custom load is Bricks, Steel is rejected
        Assert.assertEquals("car's load", "Bricks", c11.getLoadName());

        // don't allow road name "ABC" into staging
        southEndStaging1.setRoadOption(Track.EXCLUDE_ROADS);
        southEndStaging1.addRoadName("ABC");

        // build should work
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));

        // Check destinatios
        Assert.assertEquals("car destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", yardNI, c3.getDestinationTrack()); // Boxcar
        Assert.assertEquals("car destination track", yardNI, c4.getDestinationTrack()); // Boxcar
        Assert.assertEquals("car destination track", yardNI, c10.getDestinationTrack()); // Flat road "ABC"
        Assert.assertEquals("car destination track", southEndStaging1, c11.getDestinationTrack());

        // Check car loads
        Assert.assertEquals("car's load", "Crew", c2.getLoadName());
        Assert.assertEquals("car's load", "E", c3.getLoadName());
        Assert.assertEquals("car's load", "E", c4.getLoadName());
        Assert.assertEquals("car's load", "E", c10.getLoadName()); // road "ABC"
        Assert.assertEquals("car's load", "Bricks", c11.getLoadName());

        // Eliminate custom loads for Flat cars.
        cld.deleteName("Flat", "Bricks");
        cld.deleteName("Flat", "Steel");

        // build should work
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));

        // Check destinatios
        Assert.assertEquals("car destination track", southEndStaging1, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", yardNI, c3.getDestinationTrack()); // Boxcar
        Assert.assertEquals("car destination track", yardNI, c4.getDestinationTrack()); // Boxcar
        Assert.assertEquals("car destination track", yardNI, c10.getDestinationTrack()); // Flat
        Assert.assertEquals("car destination track", yardNI, c11.getDestinationTrack()); // Flat

        // Check car loads
        Assert.assertEquals("car's load", "Crew", c2.getLoadName());
        Assert.assertEquals("car's load", "E", c3.getLoadName());
        Assert.assertEquals("car's load", "E", c4.getLoadName());
        Assert.assertEquals("car's load", "E", c10.getLoadName()); // Flat
        Assert.assertEquals("car's load", "E", c11.getLoadName()); // Flat

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
        Location westford;
        westford = lmanager.newLocation("Westford");

        Location chelmsford;
        chelmsford = lmanager.newLocation("Chelmsford");

        Track westfordYard1;
        westfordYard1 = westford.addTrack("Westford Yard 1", Track.YARD);
        westfordYard1.setTrainDirections(Track.WEST + Track.EAST);
        westfordYard1.setLength(500);

        Track westfordYard2;
        westfordYard2 = westford.addTrack("Westford Yard 2", Track.YARD);
        westfordYard2.setTrainDirections(Track.WEST + Track.EAST);
        westfordYard2.setLength(500);

        Track westfordSpur1;
        westfordSpur1 = westford.addTrack("Westford Siding 3", Track.SPUR);
        westfordSpur1.setTrainDirections(0); // Only local moves allowed
        westfordSpur1.setLength(300);

        Track westfordSpur2;
        westfordSpur2 = westford.addTrack("Westford Siding 4", Track.SPUR);
        westfordSpur2.setTrainDirections(0); // Only local moves allowed
        westfordSpur2.setLength(300);

        Track westfordInterchange1;
        westfordInterchange1 = westford.addTrack("Westford Interchange 5", Track.INTERCHANGE);
        westfordInterchange1.setTrainDirections(0); // Only local moves allowed
        westfordInterchange1.setLength(300);

        Track westfordInterchange2;
        westfordInterchange2 = westford.addTrack("Westford Interchange 6", Track.INTERCHANGE);
        westfordInterchange2.setTrainDirections(Track.WEST + Track.EAST);
        westfordInterchange2.setLength(300);

        Track westfordInterchange3;
        westfordInterchange3 = westford.addTrack("Westford Interchange 7", Track.INTERCHANGE);
        westfordInterchange3.setTrainDirections(0); // Only local moves allowed
        westfordInterchange3.setLength(300);

        Track chelmsfordYard1;
        chelmsfordYard1 = chelmsford.addTrack("Chelmsford Yard 1", Track.YARD);
        chelmsfordYard1.setTrainDirections(Track.WEST + Track.EAST);
        chelmsfordYard1.setLength(900);

        // now bias track selection by moves
        westfordYard1.setMoves(3); // no yard to yard moves expected
        westfordYard2.setMoves(4); // no yard to yard moves expected
        westfordSpur1.setMoves(10); // this will be the 5th location assigned
        westfordSpur2.setMoves(10); // this will be the 6th location assigned
        westfordInterchange1.setMoves(9); // this will be the 2nd location assigned
        westfordInterchange2.setMoves(9); // this will be the 3rd location assigned
        westfordInterchange3.setMoves(8); // this will be the first and 4th location assigned

        // Create route with only one location
        Route rte1 = rmanager.newRoute("Local Route");
        RouteLocation rl1 = rte1.addLocation(westford);

        // Create train
        Train train1 = tmanager.newTrain("TestLocal");
        train1.setRoute(rte1);

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

        // place the cars on tracks
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(westford, westfordYard1));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(westford, westfordYard1));
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(westford, westfordYard1));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(westford, westfordYard1));

        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(westford, westfordYard2));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(westford, westfordYard2));
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(westford, westfordYard2));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(westford, westfordYard2));

        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(westford, westfordSpur1));

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
        Assert.assertTrue(new TrainBuilder().build(train1));

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
        Assert.assertEquals("Drop count for Westford", 7, westford.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Yard 1", 0, westfordYard1.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Yard 2", 0, westfordYard2.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Siding 3", 1, westfordSpur1.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Siding 4", 1, westfordSpur2.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Interchange 5", 2,
                westfordInterchange1.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Interchange 6", 1,
                westfordInterchange2.getDropRS());
        Assert.assertEquals("Drop count for Westford track Westford Interchange 7", 2,
                westfordInterchange3.getDropRS());
        Assert.assertEquals("Pickup count for Westford", 7, westford.getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Yard 1", 4, westfordYard1.getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Yard 2", 3, westfordYard2.getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Siding 3", 0, westfordSpur1.getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Siding 4", 0, westfordSpur2.getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Interchange 5", 0, westfordInterchange1
                .getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Interchange 6", 0, westfordInterchange2
                .getPickupRS());
        Assert.assertEquals("Pickup count for Westford track Westford Interchange 7", 0, westfordInterchange3
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
        Assert.assertEquals("Move 1 Drop count for Westford", 0, westford.getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Yard 1", 0, westfordYard1.getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Yard 2", 0, westfordYard2.getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Siding 3", 0, westfordSpur1.getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Siding 4", 0, westfordSpur2.getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Interchange 5", 0, westfordInterchange1
                .getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Interchange 6", 0, westfordInterchange2
                .getDropRS());
        Assert.assertEquals("Move 1 Drop count for Westford track Westford Interchange 7", 0, westfordInterchange3
                .getDropRS());
        Assert.assertEquals("Move 1 Pickup count for Westford", 0, westford.getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Yard 1", 0, westfordYard1
                .getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Yard 2", 0, westfordYard2
                .getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Siding 3", 0, westfordSpur1
                .getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Siding 4", 0, westfordSpur2
                .getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Interchange 5", 0, westfordInterchange1
                .getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Interchange 6", 0, westfordInterchange2
                .getPickupRS());
        Assert.assertEquals("Move 1 Pickup count for Westford track Westford Interchange 7", 0, westfordInterchange3
                .getPickupRS());

        // Verify that an extra move will not change train status.
        train1.move();
        Assert.assertEquals("Train 1 After 2nd Move Status", Train.TERMINATED, getTrainStatus(train1));
        Assert.assertEquals("Train 1 After 2nd Move Status", Train.CODE_TERMINATED, train1.getStatusCode());

        // build the train again, now there are cars on all tracks
        rl1.setMaxCarMoves(10); // try and use all 9/10 of the cars
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
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
        RouteLocation rl2 = rte2.addLocation(westford);
        rl2.setMaxCarMoves(8); // move 8 of the 9 cars available
        // and assign the new route to train 1
        train1.setRoute(rte2);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
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
        RouteLocation rl3 = rte3.addLocation(westford);
        rl3.setTrainDirection(RouteLocation.WEST);
        rl3.setMaxCarMoves(10);
        RouteLocation rl4 = rte3.addLocation(chelmsford);
        rl4.setTrainDirection(RouteLocation.WEST);
        // and assign the new route to train 1
        train1.setRoute(rte3);
        rl4.setMaxCarMoves(10);
        rl4.setTrainIconX(175); // set the train icon coordinates
        rl4.setTrainIconY(50);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
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
        westfordSpur1.setMoves(2);
        westfordSpur2.setMoves(2);
        train1.reset();
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));

        Assert.assertTrue("local testing siding to siding", train1.isBuilt());
        Assert.assertEquals("car UP 4 at interchange, destination Westford Siding 3", westfordSpur1, c4
                .getDestinationTrack());
        Assert.assertEquals("car CP 2 at siding, destination Westford Yard 1", westfordYard1, c2
                .getDestinationTrack());

        // bias track selection to interchanges
        westfordSpur1.setMoves(12);
        westfordSpur2.setMoves(12);
        westfordInterchange1.setMoves(2);
        westfordInterchange2.setMoves(2);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));

        Assert.assertTrue("local testing siding to siding", train1.isBuilt());
        Assert.assertEquals("car UP 4 at interchange, destination", "Westford Yard 2", c4
                .getDestinationTrackName());
        Assert.assertEquals("car CP 2 at siding, destination", "Westford Interchange 5", c2
                .getDestinationTrackName());

        // set CP 2 destination, currently at Westford, Westford Siding 3
        train1.reset(); // release CP2 from train so we can set the car's destination
        c2.setDestination(westford, null);
        westfordSpur1.setMoves(1); // bias to same track
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));

        Assert.assertTrue("local testing siding to siding", train1.isBuilt());
        Assert.assertEquals("car UP 4 at interchange, destination", "Westford Siding 3", c4
                .getDestinationTrackName());
        Assert.assertEquals("car CP 2 at siding, destination", "Westford Interchange 6", c2
                .getDestinationTrackName());

        // CP 2 is at Westford Siding 3, set destination to be the same
        train1.reset();
        c2.setDestination(westford, westfordSpur1);
        Assert.assertTrue(new TrainBuilder().build(train1));

        Assert.assertTrue("local testing siding to siding", train1.isBuilt());
        Assert.assertEquals("car UP 4 at interchange, destination", "Westford Siding 3", c4
                .getDestinationTrackName());
        Assert.assertEquals("car CP 2 at siding, destination", "Westford Siding 3", c2
                .getDestinationTrackName());

        train1.move();
        Assert.assertEquals("Train 1 terminated", Train.TERMINATED, getTrainStatus(train1));
        Assert.assertEquals("Train 1 terminated", Train.CODE_TERMINATED, train1.getStatusCode());
    }

    /**
     * Test the track feature planned pickups. Frees up track space consumed by
     * rolling stock so movement can occur.
     */
    @Test
    public void testLocalPlannedPickups() {

        Setup.setBuildAggressive(true);

        Location westford = JUnitOperationsUtil.createOneNormalLocation("Westford");
        Track westfordSpur1 = westford.getTrackByName("Westford Spur 1", null);
        Track westfordSpur2 = westford.getTrackByName("Westford Spur 2", null);
        Track westfordYard1 = westford.getTrackByName("Westford Yard 1", null);
        Track westfordYard2 = westford.getTrackByName("Westford Yard 2", null);
        Track westfordInterchange1 = westford.getTrackByName("Westford Interchange 1", null);
        Track westfordInterchange2 = westford.getTrackByName("Westford Interchange 2", null);

        // remove 3 of the tracks at Westford
        westford.deleteTrack(westfordSpur2);
        westford.deleteTrack(westfordYard2);
        westford.deleteTrack(westfordInterchange2);

        // limit each track to two cars
        westfordSpur1.setLength(100);
        westfordYard1.setLength(100);
        westfordInterchange1.setLength(100);

        // place cars on track
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", westfordSpur1, 0);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("A", "2", "Boxcar", "40", westfordSpur1, 10);

        Car c3 = JUnitOperationsUtil.createAndPlaceCar("A", "3", "Boxcar", "40", westfordYard1, 20);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("A", "4", "Boxcar", "40", westfordYard1, 30);

        Car c5 = JUnitOperationsUtil.createAndPlaceCar("A", "5", "Boxcar", "40", westfordInterchange1, 40);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("A", "6", "Boxcar", "40", westfordInterchange1, 50);

        // create a local train
        Train train1 = tmanager.newTrain("TestLocalPlannedPickups");

        Route route = rmanager.newRoute("Route Wesford");
        route.addLocation(westford);
        train1.setRoute(route);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm that none of the cars are able to move
        Assert.assertEquals("car's train", null, c1.getTrain());
        Assert.assertEquals("car's train", null, c2.getTrain());
        Assert.assertEquals("car's train", null, c3.getTrain());
        Assert.assertEquals("car's train", null, c4.getTrain());
        Assert.assertEquals("car's train", null, c5.getTrain());
        Assert.assertEquals("car's train", null, c6.getTrain());

        // now use planned pickups for one track 100%
        westfordYard1.setIgnoreUsedLengthPercentage(100);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm train assignment based on car moves
        Assert.assertEquals("car's train", train1, c1.getTrain());
        Assert.assertEquals("car's train", train1, c2.getTrain());
        Assert.assertEquals("car's train", train1, c3.getTrain());
        Assert.assertEquals("car's train", train1, c4.getTrain());
        Assert.assertEquals("car's train", null, c5.getTrain());
        Assert.assertEquals("car's train", null, c6.getTrain());

        // now use planned pickups for one track 50%
        westfordYard1.setIgnoreUsedLengthPercentage(50);

        // should only be able to swap two cars
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm train assignment based on car moves
        Assert.assertEquals("car's train", train1, c1.getTrain());
        Assert.assertEquals("car's train", null, c2.getTrain());
        Assert.assertEquals("car's train", train1, c3.getTrain());
        Assert.assertEquals("car's train", null, c4.getTrain());
        Assert.assertEquals("car's train", null, c5.getTrain());
        Assert.assertEquals("car's train", null, c6.getTrain());
    }

    // Test TrainBuilder through the train's build method.
    // Test a route of one location (local train).
    // Locations that don't have a train direction assigned
    // can only be served by a local train.
    // Creates one locations Westford and 2 cars.
    // Westford has 2 yards, 2 sidings, 3 interchange tracks.
    @Test
    public void testLocalBuildOptions() {
        String roadNames[] = Bundle.getMessage("carRoadNames").split(",");
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // Create locations used
        Location westford;
        westford = lmanager.newLocation("Westford");

        Track westfordYard1;
        westfordYard1 = westford.addTrack("Westford Yard 1", Track.YARD);
        westfordYard1.setTrainDirections(Track.WEST + Track.EAST);
        westfordYard1.setLength(500);

        Track westfordYard2;
        westfordYard2 = westford.addTrack("Westford Yard 2", Track.YARD);
        westfordYard2.setTrainDirections(Track.WEST + Track.EAST);
        westfordYard2.setLength(500);

        Track westfordSpur3;
        westfordSpur3 = westford.addTrack("Westford Siding 3", Track.SPUR);
        westfordSpur3.setTrainDirections(0); // Only local moves allowed
        westfordSpur3.setLength(300);

        Track westfordSpur4;
        westfordSpur4 = westford.addTrack("Westford Siding 4", Track.SPUR);
        westfordSpur4.setTrainDirections(0); // Only local moves allowed
        westfordSpur4.setLength(300);

        Track westfordInterchange5;
        westfordInterchange5 = westford.addTrack("Westford Interchange 5", Track.INTERCHANGE);
        westfordInterchange5.setTrainDirections(0); // Only local moves allowed
        westfordInterchange5.setLength(300);

        Track westfordInterchange6;
        westfordInterchange6 = westford.addTrack("Westford Interchange 6", Track.INTERCHANGE);
        westfordInterchange6.setTrainDirections(Track.WEST + Track.EAST);
        westfordInterchange6.setLength(300);

        Track westfordInterchange7;
        westfordInterchange7 = westford.addTrack("Westford Interchange 7", Track.INTERCHANGE);
        westfordInterchange7.setTrainDirections(0); // Only local moves allowed
        westfordInterchange7.setLength(300);

        // now bias track selection by moves
        westfordYard1.setMoves(3);
        westfordYard2.setMoves(6);
        westfordSpur3.setMoves(10);
        westfordSpur4.setMoves(15);
        westfordInterchange5.setMoves(20);
        westfordInterchange6.setMoves(21);
        westfordInterchange7.setMoves(22);

        // Create route with only one location
        Route rte1 = rmanager.newRoute("Local Route");
        rte1.addLocation(westford);

        // Create train
        Train train1 = tmanager.newTrain("Test Local Defaults");
        train1.setRoute(rte1);

        Car c1 = JUnitOperationsUtil.createAndPlaceCar(roadNames[1], "1", carTypes[1], "90", westfordYard1, 0);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar(roadNames[1], "2", carTypes[2], "90", westfordYard1, 0);
        c2.setCaboose(true);

        // allow yard to yard moves
        Setup.setLocalYardMovesEnabled(true);

        // Build train
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 built", train1.isBuilt());

        // Yard track 2 has the least number of moves
        Assert.assertEquals("confirm destination", westfordYard2, c1.getDestinationTrack());
        // local should not move caboose unless required
        Assert.assertEquals("confirm destination", null, c2.getDestinationTrack());

        train1.reset();
        train1.setRequirements(Train.CABOOSE);
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 built", train1.isBuilt());

        Assert.assertEquals("confirm destination", westfordYard2, c1.getDestinationTrack());
        // a caboose can return to the same track it departed on
        Assert.assertEquals("confirm destination", westfordYard1, c2.getDestinationTrack());

        // allow spur to spur moves
        Setup.setLocalYardMovesEnabled(false);
        Setup.setLocalSpurMovesEnabled(true);

        train1.reset();

        // place c1 on spur
        Assert.assertEquals("place c1", Track.OKAY, c1.setLocation(westford, westfordSpur3));

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 built", train1.isBuilt());

        // Yard track 1 has the least number of moves
        Assert.assertEquals("confirm c1 destination", westfordYard1, c1.getDestinationTrack());
        // a caboose can return to the same track it departed on
        Assert.assertEquals("confirm destination", westfordYard1, c2.getDestinationTrack());

        // bias track counts so the spur has the least number of moves
        westfordYard1.setMoves(30);
        westfordYard2.setMoves(40);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 built", train1.isBuilt());

        // Spur track 2 has the least number of moves
        Assert.assertEquals("confirm c1 destination", westfordSpur4, c1.getDestinationTrack());
        Assert.assertEquals("confirm destination", westfordSpur3, c2.getDestinationTrack());

        // now test interchange to interchange local move
        Assert.assertEquals("place c1", Track.OKAY, c1.setLocation(westford, westfordInterchange5));

        // bias track moves so interchange has the least number of moves
        westfordInterchange7.setMoves(0);

        // allow interchange to interchange moves
        Setup.setLocalSpurMovesEnabled(false);
        Setup.setLocalInterchangeMovesEnabled(true);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 built", train1.isBuilt());

        // Interchange track 3 has the least number of moves
        Assert.assertEquals("confirm c1 destination", westfordInterchange7, c1.getDestinationTrack());
        Assert.assertEquals("confirm destination", westfordInterchange7, c2.getDestinationTrack());
    }

    @Test
    public void testScheduleLoads() {
        String roadNames[] = Bundle.getMessage("carRoadNames").split(",");
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

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
        loc1 = lmanager.newLocation("Westford");

        Location loc2;
        loc2 = lmanager.newLocation("Chelmsford");

        Location loc3;
        loc3 = lmanager.newLocation("Bedford");

        Track loc1trk1;
        loc1trk1 = loc1.addTrack("Westford Yard 1", Track.YARD);
        loc1trk1.setTrainDirections(Track.WEST + Track.EAST);
        loc1trk1.setLength(900);

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
        loc2trk1.setSchedule(sch1);
        loc2trk1.setScheduleMode(Track.SEQUENTIAL);
        // start the schedule with 2nd item Flat Car
        loc2trk1.setScheduleItemId(sch1.getItemsBySequenceList().get(1).getId());

        Track loc2trk2;
        loc2trk2 = loc2.addTrack("Chelmsford Freight 2", Track.SPUR);
        loc2trk2.setTrainDirections(Track.WEST + Track.EAST);
        loc2trk2.setLength(900);
        loc2trk2.deleteTypeName(carTypes[4]);
        loc2trk2.setSchedule(sch1);
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
        loc2trk4.setSchedule(sch2);
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

        Assert.assertTrue(new TrainBuilder().build(train1));

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
        Assert.assertTrue(new TrainBuilder().build(train1));

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
        Assert.assertTrue(new TrainBuilder().build(train1));

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

        // confirm no locations
        Assert.assertEquals("number of locations", 0, lmanager.getNumberOfLocations());
        // Create locations used
        Location loc1;
        loc1 = lmanager.newLocation("Westford");
        Assert.assertEquals("Default directions", DIRECTION_ALL, loc1.getTrainDirections());

        Location loc2;
        loc2 = lmanager.newLocation("Chelmsford");
        Assert.assertEquals("Default directions", DIRECTION_ALL, loc2.getTrainDirections());

        Location loc3;
        loc3 = lmanager.newLocation("Bedford");
        Assert.assertEquals("Default directions", DIRECTION_ALL, loc3.getTrainDirections());

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
        Assert.assertTrue(new TrainBuilder().build(train1));
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));

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
        Assert.assertTrue(new TrainBuilder().build(train1));
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));

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
        Assert.assertEquals("Check train 1 departure location name", "Westford", train1
                .getCurrentLocationName());
        Assert.assertEquals("Check train 1 departure location", r1l1, train1.getCurrentLocation());
        train1.move(); // #1
        Assert.assertEquals("Check train 1 location name", "Chelmsford", train1.getCurrentLocationName());
        Assert.assertEquals("Check train 1 location", r1l2, train1.getCurrentLocation());
        train1.move(); // #2
        Assert.assertEquals("Check train 1 location name", "Bedford", train1.getCurrentLocationName());
        Assert.assertEquals("Check train 1 location", r1l3, train1.getCurrentLocation());
        train1.move(); // #3 terminate
        Assert.assertEquals("Check train 1 location name", "", train1.getCurrentLocationName());
        Assert.assertEquals("Check train 1 location", null, train1.getCurrentLocation());

        Assert.assertEquals("Check train 2 departure location name", "Westford", train2
                .getCurrentLocationName());
        Assert.assertEquals("Check train 2 departure location", r1l1, train2.getCurrentLocation());
        train2.move(); // #1
        Assert.assertEquals("Check train 2 location name", "Chelmsford", train2.getCurrentLocationName());
        Assert.assertEquals("Check train 2 location", r1l2, train2.getCurrentLocation());
        train2.move(); // #2
        Assert.assertEquals("Check train 2 location name", "Bedford", train2.getCurrentLocationName());
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
        Assert.assertEquals("Depart Chelmsford length", 310, r1l2.getTrainLength());

        // In train 2 cars, c3 E and c4 E car weight 20/3 + 50/3 = 23
        Assert.assertEquals("Depart Westford tonnage", 23, r1l1.getTrainWeight());
        // In train 5 cars, c4 E, c5 L, c7 E, c9 L, c12 L = 40/3 + 50 + 70/3 + 90 + 120 = 296
        Assert.assertEquals("Depart Chelmsford tonnage", 296, r1l2.getTrainWeight());

        // test route pickup and drop controls
        train3.setRequirements(Train.CABOOSE);
        r1l1.setPickUpAllowed(false);
        train3.reset();
        // c1, c2, and c13 at start of train's route
        // c3 at Chelmsford, second stop
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
        r1l2.setDropAllowed(false); // Chelmsford
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
        Assert.assertEquals("c2 destination Chelmsford", "", c2.getDestinationTrackName());

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
        Assert.assertEquals("c2 destination Chelmsford, no moves", "", c2.getDestinationTrackName());

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

        // register the road names used
        cr.addName("UP");
        cr.addName("SP");
        cr.addName("NH");

        // place two engines in a consist
        Consist con1 = emanager.newConsist("C1");

        Engine e1 = emanager.newRS("UP", "1");
        e1.setModel("GP40");
        e1.setConsist(con1);
        Engine e2 = emanager.newRS("SP", "2");
        e2.setModel("GP40");
        e2.setConsist(con1);

        // Set up three cabooses and six box cars
        Car c1 = cmanager.newRS("UP", "1");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setMoves(10);
        c1.setCaboose(true);

        Car c2 = cmanager.newRS("SP", "2");
        c2.setTypeName(Bundle.getMessage("Caboose"));
        c2.setLength("30");
        c2.setMoves(5);
        c2.setCaboose(true);

        Car c3 = cmanager.newRS("NH", "3");
        c3.setTypeName(Bundle.getMessage("Caboose"));
        c3.setLength("33");
        c3.setCaboose(true);

        Car c4 = cmanager.newRS("UP", "4");
        c4.setTypeName(carTypes[1]);
        c4.setLength("40");
        c4.setMoves(16);
        c4.setFred(true);

        Car c5 = cmanager.newRS("SP", "5");
        c5.setTypeName(carTypes[1]);
        c5.setLength("40");
        c5.setMoves(8);
        c5.setFred(true);

        Car c6 = cmanager.newRS("NH", "6");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(2);
        c6.setFred(true);

        Car c7 = cmanager.newRS("UP", "7");
        c7.setTypeName(carTypes[5]);
        c7.setLength("40");
        c7.setMoves(5);

        Car c8 = cmanager.newRS("SP", "8");
        c8.setTypeName(carTypes[1]);
        c8.setLength("40");
        c8.setMoves(4);

        Car c9 = cmanager.newRS("NH", "9");
        c9.setTypeName(carTypes[1]);
        c9.setLength("40");
        c9.setMoves(3);

        Car c10 = cmanager.newRS("NH", "10");
        c10.setTypeName(carTypes[1]);
        c10.setLength("40");
        c10.setMoves(10);

        Car c11 = cmanager.newRS("SP", "11");
        c11.setTypeName(carTypes[1]);
        c11.setLength("40");
        c11.setMoves(3);

        // Create 3 locations
        Location harvard = lmanager.newLocation("Harvard");

        Track loc1trk1 = harvard.addTrack("Harvard Yard", Track.YARD);
        loc1trk1.setLength(1000);

        Location acton = lmanager.newLocation("Acton");

        Track actonYard = acton.addTrack("Acton Yard", Track.YARD);
        actonYard.setLength(1000);

        Location boston = lmanager.newLocation("Boston");

        Track bostonYard = boston.addTrack("Boston Yard", Track.YARD);
        bostonYard.setLength(1000);
        bostonYard.deleteTypeName("Diesel");

        Track bostonEngineYard = boston.addTrack("Boston Engine Yard", Track.YARD);
        bostonEngineYard.setLength(200);
        bostonEngineYard.deleteTypeName(carTypes[1]);
        bostonEngineYard.deleteTypeName(carTypes[5]);
        bostonEngineYard.deleteTypeName(Bundle.getMessage("Caboose"));

        // Create route with 3 location
        Route rte1 = rmanager.newRoute("Route Harvard-Acton-Boston");
        rte1.addLocation(harvard);
        RouteLocation rlArlinton = rte1.addLocation(acton);
        rlArlinton.setTrainIconX(75); // set the train icon coordinates
        rlArlinton.setTrainIconY(150);
        RouteLocation rl3 = rte1.addLocation(boston);
        rl3.setTrainIconX(125); // set the train icon coordinates
        rl3.setTrainIconY(150);

        // Create train
        Train train1 = tmanager.newTrain("TestCaboose");
        train1.setRoute(rte1);

        // Place cars
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(harvard, loc1trk1));

        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(harvard, loc1trk1));

        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(acton, actonYard));
        Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(acton, actonYard));

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(harvard, loc1trk1));

        // no requirements, so no caboose or FRED or engines
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 1", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 1", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 1", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 1", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 1", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 1", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 1", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 1", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 1", "Acton Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 1", "Boston Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 1", "Boston Yard", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 1", "Boston Yard", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 1", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 1", "", e2.getDestinationTrackName());

        // no engines, so the caboose with least moves should be used
        train1.setRequirements(Train.CABOOSE);
        // don't allow pickups at second location Acton
        rlArlinton.setPickUpAllowed(false);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
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

        Assert.assertEquals("c9 destination 2", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 2", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 2", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 2", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 2", "", e2.getDestinationTrackName());

        // there's a caboose c1 that matches lead engine
        train1.setNumberEngines("2"); // engine road name "UP"
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
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

        Assert.assertEquals("c9 destination 3", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 3", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 3", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 3", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 3", "Boston Engine Yard", e2.getDestinationTrackName());

        // now prevent c1 from getting placed at Boston Yard
        c1.setLength("1000"); // car exceeds train's maximum allowable length
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 3", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 3", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 3", "Boston Yard", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 3", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 3", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 3", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 3", "Boston Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 3", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 3", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 3", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 3", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 3", "Boston Engine Yard", e2.getDestinationTrackName());

        // restore
        c1.setLength("32");

        // caboose c1 road matches lead engine road
        train1.reset();
        // set c1 destination is now the terminal, should now choose this caboose
        c1.setDestination(boston, bostonYard);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train built", true, train1.isBuilt());

        Assert.assertEquals("c1 destination 3", "Boston Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c1 assigned to train", train1, c1.getTrain());

        Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 3", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 3", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 3", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 3", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 3", "Boston Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 3", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 3", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 3", "", c11.getDestinationTrackName());

        // now try to find a caboose with "similar" (hyphen feature) road name to engine
        c1.setRoadName("UP-1");
        train1.reset();

        Assert.assertTrue(new TrainBuilder().build(train1));
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

        Assert.assertEquals("c9 destination 3", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 3", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 3", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 3", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 3", "Boston Engine Yard", e2.getDestinationTrackName());

        // caboose c1 road matches lead engine road
        train1.reset();
        // set c1 destination not the terminal, should not choose this caboose
        c1.setDestination(acton, actonYard);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train built", true, train1.isBuilt());

        Assert.assertEquals("c1 destination 3", "Acton Yard", c1.getDestinationTrackName());
        Assert.assertNull("c1 not assigned to train", c1.getTrain());

        Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 3", "Boston Yard", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 3", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 3", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 3", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 3", "Boston Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 3", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 3", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 3", "", c11.getDestinationTrackName());

        train1.reset();
        // set c1 destination to the terminal, this should work
        c1.setDestination(boston, bostonYard);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train built", true, train1.isBuilt());

        Assert.assertEquals("c1 destination 3", "Boston Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c1 assigned to train", train1, c1.getTrain());

        Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 3", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 3", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 3", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 3", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 3", "Boston Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 3", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 3", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 3", "", c11.getDestinationTrackName());

        // restore
        c1.setDestination(null, null);
        c1.setRoadName("UP");

        // should default to the caboose with the least moves
        e1.setRoadName("X");
        // allow pickups at Acton
        rlArlinton.setPickUpAllowed(true);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
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

        Assert.assertEquals("c9 destination 4", "Acton Yard", c9.getDestinationTrackName());
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
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 5", true, train1.isBuilt());
        // check destinations
        Assert.assertEquals("c1 destination 5", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 5", "Boston Yard", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 5", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 5", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 5", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 5", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 5", "Acton Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 5", "Acton Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 5", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 5", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 5", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 5", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 5", "Boston Engine Yard", e2.getDestinationTrackName());
    }

    @Test
    public void testCarsWithFred() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // register the road names used
        cr.addName("UP");
        cr.addName("SP");
        cr.addName("NH");

        // place two engines in a consist
        Consist con1 = emanager.newConsist("C1");

        Engine e1 = emanager.newRS("NH", "1");
        e1.setModel("GP40");
        e1.setConsist(con1);
        Engine e2 = emanager.newRS("SP", "2");
        e2.setModel("GP40");
        e2.setConsist(con1);

        // Set up three cabooses and six box cars
        Car c1 = cmanager.newRS("UP", "1");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setMoves(10);
        c1.setCaboose(true);

        Car c2 = cmanager.newRS("SP", "2");
        c2.setTypeName(Bundle.getMessage("Caboose"));
        c2.setLength("30");
        c2.setMoves(5);
        c2.setCaboose(true);

        Car c3 = cmanager.newRS("NH", "3");
        c3.setTypeName(Bundle.getMessage("Caboose"));
        c3.setLength("33");
        c3.setCaboose(true);

        Car c4 = cmanager.newRS("UP", "4");
        c4.setTypeName(carTypes[1]);
        c4.setLength("40");
        c4.setMoves(16);
        c4.setFred(true);

        Car c5 = cmanager.newRS("SP", "5");
        c5.setTypeName(carTypes[1]);
        c5.setLength("40");
        c5.setMoves(8);
        c5.setFred(true);

        Car c6 = cmanager.newRS("NH", "6");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(2);
        c6.setFred(true);

        Car c7 = cmanager.newRS("UP", "7");
        c7.setTypeName(carTypes[5]);
        c7.setLength("40");
        c7.setMoves(5);

        Car c8 = cmanager.newRS("SP", "8");
        c8.setTypeName(carTypes[1]);
        c8.setLength("40");
        c8.setMoves(4);

        Car c9 = cmanager.newRS("NH", "9");
        c9.setTypeName(carTypes[1]);
        c9.setLength("40");
        c9.setMoves(3);

        Car c10 = cmanager.newRS("NH", "10");
        c10.setTypeName(carTypes[1]);
        c10.setLength("40");
        c10.setMoves(10);

        Car c11 = cmanager.newRS("SP", "11");
        c11.setTypeName(carTypes[1]);
        c11.setLength("40");
        c11.setMoves(3);

        // Create 3 locations
        Location harvard = lmanager.newLocation("Harvard");

        Track loc1trk1 = harvard.addTrack("Harvard Yard", Track.YARD);
        loc1trk1.setLength(1000);

        Location acton = lmanager.newLocation("Acton");

        Track actonYard = acton.addTrack("Acton Yard", Track.YARD);
        actonYard.setLength(1000);

        Location boston = lmanager.newLocation("Boston");

        Track bostonYard = boston.addTrack("Boston Yard", Track.YARD);
        bostonYard.setLength(1000);
        bostonYard.deleteTypeName("Diesel");

        Track bostonEngineYard = boston.addTrack("Boston Engine Yard", Track.YARD);
        bostonEngineYard.setLength(200);
        bostonEngineYard.deleteTypeName(carTypes[1]);
        bostonEngineYard.deleteTypeName(carTypes[5]);
        bostonEngineYard.deleteTypeName(Bundle.getMessage("Caboose"));

        // Create route with 3 location
        Route rte1 = rmanager.newRoute("Route Harvard-Acton-Boston");
        rte1.addLocation(harvard);
        RouteLocation rlArlinton = rte1.addLocation(acton);
        rlArlinton.setTrainIconX(75); // set the train icon coordinates
        rlArlinton.setTrainIconY(150);
        RouteLocation rl3 = rte1.addLocation(boston);
        rl3.setTrainIconX(125); // set the train icon coordinates
        rl3.setTrainIconY(150);

        // Create train
        Train train1 = tmanager.newTrain("TestCarsWithFred");
        train1.setRoute(rte1);

        // Place cars
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(harvard, loc1trk1));

        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(harvard, loc1trk1));

        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(acton, actonYard));
        Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(acton, actonYard));

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(harvard, loc1trk1));

        // no requirements, so no caboose or FRED or engines
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 1", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 1", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 1", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 1", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 1", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 1", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 1", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 1", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 1", "Acton Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 1", "Boston Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 1", "Boston Yard", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 1", "Boston Yard", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 1", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 1", "", e2.getDestinationTrackName());

        // should take car with FRED and road SP
        train1.setRequirements(Train.FRED);
        train1.setCabooseRoad("SP");
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
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

        Assert.assertEquals("c9 destination 6", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 6", "Boston Yard", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 6", "Boston Yard", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 1", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 1", "", e2.getDestinationTrackName());

        // should take car with FRED least number of moves
        train1.setCabooseRoad("");
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
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

        Assert.assertEquals("c9 destination 7", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 7", "Boston Yard", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 7", "Boston Yard", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 1", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 1", "", e2.getDestinationTrackName());

        train1.setNumberEngines("2"); // lead engine road name "NH"

        // now exclude road NH, engine road is NH and should be rejected
        train1.addRoadName("NH");
        train1.setRoadOption(Train.EXCLUDE_ROADS);
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 7a", false, train1.isBuilt());

        // now override by setting a road for the engine
        train1.setEngineRoad("NH");
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 8", true, train1.isBuilt());
        // check destinations
        Assert.assertEquals("c1 destination 8", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 8", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 8", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 8", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 8", "Boston Yard", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 8", "", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 8", "Boston Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 8", "Acton Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 8", "", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 8", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 8", "Boston Yard", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 1", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 1", "Boston Engine Yard", e2.getDestinationTrackName());

        // now only include NH
        train1.setRoadOption(Train.INCLUDE_ROADS);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
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

        Assert.assertEquals("c9 destination 9", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 9", "Boston Yard", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 9", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 1", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 1", "Boston Engine Yard", e2.getDestinationTrackName());

        // don't allow boxcar, car with FRED required, build should fail
        boston.deleteTypeName(carTypes[1]);
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 9a", false, train1.isBuilt());
    }

    @Test
    public void testCabooseAndCarsWithFredDepartingStaging() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // register the road names used
        cr.addName("UP");
        cr.addName("SP");
        cr.addName("NH");

        // place two engines in a consist
        Consist con1 = emanager.newConsist("C1");

        Engine e1 = emanager.newRS("UP", "1");
        e1.setModel("GP40");
        e1.setConsist(con1);
        Engine e2 = emanager.newRS("SP", "2");
        e2.setModel("GP40");
        e2.setConsist(con1);

        // Set up three cabooses and six box cars
        Car c1 = cmanager.newRS("NH", "1");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setMoves(10);
        c1.setCaboose(true);

        Car c2 = cmanager.newRS("SP", "2");
        c2.setTypeName(Bundle.getMessage("Caboose"));
        c2.setLength("30");
        c2.setMoves(5);
        c2.setCaboose(true);

        Car c3 = cmanager.newRS("NH", "3");
        c3.setTypeName(Bundle.getMessage("Caboose"));
        c3.setLength("33");
        c3.setCaboose(true);

        Car c4 = cmanager.newRS("UP", "4");
        c4.setTypeName(carTypes[1]);
        c4.setLength("40");
        c4.setMoves(16);
        c4.setFred(true);

        Car c5 = cmanager.newRS("SP", "5");
        c5.setTypeName(carTypes[1]);
        c5.setLength("40");
        c5.setMoves(8);
        c5.setFred(true);

        Car c6 = cmanager.newRS("NH", "6");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(2);
        c6.setFred(true);

        Car c7 = cmanager.newRS("UP", "7");
        c7.setTypeName(carTypes[5]);
        c7.setLength("40");
        c7.setMoves(5);

        Car c8 = cmanager.newRS("SP", "8");
        c8.setTypeName(carTypes[1]);
        c8.setLength("40");
        c8.setMoves(4);

        Car c9 = cmanager.newRS("NH", "9");
        c9.setTypeName(carTypes[1]);
        c9.setLength("40");
        c9.setMoves(3);

        Car c10 = cmanager.newRS("NH", "10");
        c10.setTypeName(carTypes[1]);
        c10.setLength("40");
        c10.setMoves(10);

        Car c11 = cmanager.newRS("SP", "11");
        c11.setTypeName(carTypes[1]);
        c11.setLength("40");
        c11.setMoves(3);

        // Create 3 locations
        Location harvard = lmanager.newLocation("Harvard");
        Track loc1trk2 = harvard.addTrack("Harvard Yard", Track.STAGING);
        loc1trk2.setLength(1000);

        Location acton = lmanager.newLocation("Acton");
        Track actonYard = acton.addTrack("Acton Yard", Track.YARD);
        actonYard.setLength(1000);

        Location boston = lmanager.newLocation("Boston");
        Track bostonYard = boston.addTrack("Boston Yard", Track.YARD);
        bostonYard.setLength(1000);
        bostonYard.deleteTypeName("Diesel");

        Track bostonEngineYard = boston.addTrack("Boston Engine Yard", Track.YARD);
        bostonEngineYard.setLength(200);
        bostonEngineYard.deleteTypeName(carTypes[1]);
        bostonEngineYard.deleteTypeName(carTypes[5]);
        bostonEngineYard.deleteTypeName(Bundle.getMessage("Caboose"));

        // Create route with 3 location
        Route rte1 = rmanager.newRoute("Route Harvard-Acton-Boston");
        RouteLocation rl1 = rte1.addLocation(harvard);
        RouteLocation rlArlinton = rte1.addLocation(acton);
        rlArlinton.setTrainIconX(75); // set the train icon coordinates
        rlArlinton.setTrainIconY(150);
        RouteLocation rl3 = rte1.addLocation(boston);
        rl3.setTrainIconX(125); // set the train icon coordinates
        rl3.setTrainIconY(150);

        // Create train
        Train train1 = tmanager.newTrain("TestDepartingStaging");
        train1.setRoute(rte1);

        // Place cars
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(harvard, loc1trk2));
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(harvard, loc1trk2));
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(harvard, loc1trk2));
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(harvard, loc1trk2));

        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(harvard, loc1trk2));
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(harvard, loc1trk2));
        Assert.assertEquals("Place c7", Track.OKAY, c7.setLocation(harvard, loc1trk2));
        Assert.assertEquals("Place c8", Track.OKAY, c8.setLocation(harvard, loc1trk2));

        Assert.assertEquals("Place c9", Track.OKAY, c9.setLocation(harvard, loc1trk2));
        Assert.assertEquals("Place c10", Track.OKAY, c10.setLocation(acton, actonYard));
        Assert.assertEquals("Place c11", Track.OKAY, c11.setLocation(acton, actonYard));

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(harvard, loc1trk2));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(harvard, loc1trk2));

        // now depart staging, must take all cars in staging
        rl1.setMaxCarMoves(9); // there are nine cars departing staging

        // All engines and cars in staging must move! Cabooses and cars with FRED to terminal
        train1.setNumberEngines("0");
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 After Build 10", train1.isBuilt());

        train1.reset();
        // now only include NH
        train1.setRoadOption(Train.INCLUDE_ROADS);
        train1.addRoadName("NH");

        Assert.assertFalse(new TrainBuilder().build(train1));
        // train only accepted engine and cars with NH road therefore build should fail
        Assert.assertEquals("Train 1 After Build from staging", false, train1.isBuilt());

        // try again but now accept all roads
        train1.setRoadOption(Train.ALL_ROADS);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 10", true, train1.isBuilt());
        // check destinations
        Assert.assertEquals("c1 destination 10", "Boston Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 10", "Boston Yard", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 10", "Boston Yard", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 10", "Boston Yard", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 10", "Boston Yard", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 10", "Boston Yard", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 10", "Acton Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 10", "Acton Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 10", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 10", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 10", "Boston Yard", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 10", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 10", "Boston Engine Yard", e2.getDestinationTrackName());

        // exclude road NH
        train1.setRoadOption(Train.EXCLUDE_ROADS);
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        // should fail since there are NH roads in staging
        Assert.assertEquals("Train 1 After Build 11", false, train1.isBuilt());

        // reduce Boston moves to 6, to force non caboose and FRED cars to Acton
        rl3.setMaxCarMoves(6);
        train1.setRoadOption(Train.ALL_ROADS);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 12", true, train1.isBuilt());
        // check destinations
        Assert.assertEquals("c1 destination 12", "Boston Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 12", "Boston Yard", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 12", "Boston Yard", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 12", "Boston Yard", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 12", "Boston Yard", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 12", "Boston Yard", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 12", "Acton Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 12", "Acton Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 12", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 12", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 12", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 12", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 12", "Boston Engine Yard", e2.getDestinationTrackName());

        // Reduce Acton to only two moves, this should cause train build to fail
        rlArlinton.setMaxCarMoves(2);

        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 13", false, train1.isBuilt());

        // restore number of moves
        rlArlinton.setMaxCarMoves(7);
        rl3.setMaxCarMoves(7);
        // don't allow drops at Boston
        rl3.setDropAllowed(false);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 14", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 14", "Boston Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 14", "Boston Yard", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 14", "Boston Yard", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 14", "Boston Yard", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 14", "Boston Yard", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 14", "Boston Yard", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 14", "Acton Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 14", "Acton Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 14", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 14", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 14", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 14", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 14", "Boston Engine Yard", e2.getDestinationTrackName());

        // Reduce Acton to only two moves, this should cause train build to fail
        rlArlinton.setMaxCarMoves(2);
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 15", false, train1.isBuilt());

        // Don't allow cabooses at Boston, should cause build failure
        rlArlinton.setMaxCarMoves(7);
        boston.deleteTypeName(Bundle.getMessage("Caboose"));
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 16", false, train1.isBuilt());

        // Don't allow boxcars, should also cause build failure
        boston.addTypeName(Bundle.getMessage("Caboose"));
        boston.deleteTypeName(carTypes[1]);
        train1.setRequirements(Train.NO_CABOOSE_OR_FRED);
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 17", false, train1.isBuilt());

        // allow the three road names we're testing
        boston.addTypeName(carTypes[1]);
        bostonYard.addRoadName("NH");
        bostonYard.addRoadName("SP");
        bostonYard.addRoadName("UP");
        bostonYard.setRoadOption(Track.INCLUDE_ROADS);
        bostonEngineYard.addRoadName("NH");
        bostonEngineYard.addRoadName("SP");
        bostonEngineYard.addRoadName("UP");
        bostonEngineYard.setRoadOption(Track.INCLUDE_ROADS);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 After Build 18", train1.isBuilt());

        // now remove type Diesel, this should cause a failure
        bostonEngineYard.deleteTypeName("Diesel");
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train 1 After Build 19", train1.isBuilt());

        // now restore type Diesel
        bostonEngineYard.addTypeName("Diesel");
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 After Build 20", train1.isBuilt());

        // Set the track length too short missing one set of couplers
        bostonEngineYard
                .setLength(Integer.parseInt(e1.getLength()) + Integer.parseInt(e2.getLength()) + Engine.COUPLERS);
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 20.1", false, train1.isBuilt());

        // restore track length
        bostonEngineYard
                .setLength(Integer.parseInt(e1.getLength()) + Integer.parseInt(e2.getLength()) + 2 * Engine.COUPLERS);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 20.2", true, train1.isBuilt());

        // change lead engine road name, should cause build failure since Boston only
        // accepts NH, SP, and UP.
        train1.setEngineRoad(""); // reset engine road requirements, was "NH"
        e1.setRoadName("X"); // was "NH"
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 21", false, train1.isBuilt());

        e1.setRoadName("UP");
        bostonYard.deleteRoadName("NH"); // this test that a caboose fails
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 22", false, train1.isBuilt());

        bostonYard.addRoadName("NH");
        c6.setRoadName("X"); // this test that a car with FRED fails
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 22", false, train1.isBuilt());

        bostonYard.addRoadName("NH");
        c6.setRoadName("X"); // this test that a car with FRED fails
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 23", false, train1.isBuilt());

        bostonYard.addRoadName("X");
        actonYard.deleteTypeName(carTypes[5]); // this test that an ordinary car must move
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 24", false, train1.isBuilt());

        actonYard.addTypeName(carTypes[5]); // restore
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 25", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 25", "Boston Yard", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 25", "Boston Yard", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 25", "Boston Yard", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 25", "Boston Yard", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 25", "Boston Yard", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 25", "Boston Yard", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 25", "Acton Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 25", "Acton Yard", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 25", "Acton Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 25", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 25", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 25", "Boston Engine Yard", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 25", "Boston Engine Yard", e2.getDestinationTrackName());

        train1.reset();
        // send caboose SP 2 from staging to track that will not service it
        bostonEngineYard.addTypeName(Bundle.getMessage("Caboose"));
        bostonEngineYard.setLength(200);
        c2.setDestination(boston, bostonEngineYard);
        bostonEngineYard.deleteTypeName(Bundle.getMessage("Caboose"));

        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build with caboose bad destination", false, train1.isBuilt());
        c2.setDestination(null, null);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));

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
        Assert.assertEquals("c7 location 24", "Acton Yard", c7.getTrackName());
        Assert.assertEquals("c8 location 24", "Acton Yard", c8.getTrackName());

        Assert.assertEquals("c9 location 24", "Acton Yard", c9.getTrackName());
        Assert.assertEquals("c10 location 24", "Acton Yard", c10.getTrackName());
        Assert.assertEquals("c11 location 24", "Acton Yard", c11.getTrackName());

        Assert.assertEquals("e1 location 24", "Boston Engine Yard", e1.getTrackName());
        Assert.assertEquals("e2 location 24", "Boston Engine Yard", e2.getTrackName());
    }

    @Test
    public void testTrainBuildOptions() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        // create 2 consists and a single engine for testing
        Consist con1 = emanager.newConsist("C1");

        Engine e1 = emanager.newRS("UP", "1");
        e1.setModel("GP30");
        e1.setOwner("AT");
        e1.setBuilt("1957");
        e1.setConsist(con1);
        e1.setMoves(5);
        Engine e2 = emanager.newRS("SP", "2");
        e2.setModel("GP30");
        e2.setOwner("AT");
        e2.setBuilt("1957");
        e2.setConsist(con1);
        e2.setMoves(5);

        // one engine
        Engine e3 = emanager.newRS("SP", "3");
        e3.setModel("GP40");
        e3.setOwner("DAB");
        e3.setBuilt("1957");

        Consist con2 = emanager.newConsist("C2");

        Engine e4 = emanager.newRS("UP", "10");
        e4.setModel("GP40");
        e4.setOwner("DAB");
        e4.setBuilt("1944");
        e4.setConsist(con2);
        e4.setMoves(20);
        Engine e5 = emanager.newRS("SP", "20");
        e5.setModel("GP40");
        e5.setOwner("DAB");
        e5.setBuilt("1944");
        e5.setConsist(con2);
        e5.setMoves(20);

        // 3 engine consist
        Consist con3 = emanager.newConsist("C3");

        Engine e6 = emanager.newRS("UP", "100");
        e6.setModel("GP40");
        e6.setOwner("DAB");
        e6.setBuilt("1944");
        e6.setConsist(con3);
        e6.setMoves(2);
        Engine e7 = emanager.newRS("SP", "200");
        e7.setModel("GP40");
        e7.setOwner("DAB");
        e7.setBuilt("1944");
        e7.setConsist(con3);
        e7.setMoves(2);
        Engine e8 = emanager.newRS("SP", "300");
        e8.setModel("GP40");
        e8.setOwner("DAB");
        e8.setBuilt("1944");
        e8.setConsist(con3);
        e8.setMoves(2);

        // Set up three cabooses and six box cars
        Car c1 = cmanager.newRS("PU", "1");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setMoves(10);
        c1.setOwner("AT");
        c1.setBuilt("1943");
        c1.setCaboose(true);

        Car c2 = cmanager.newRS("SP", "2");
        c2.setTypeName(Bundle.getMessage("Caboose"));
        c2.setLength("30");
        c2.setMoves(5);
        c2.setOwner("DAB");
        c2.setBuilt("1957");
        c2.setCaboose(true);

        Car c3 = cmanager.newRS("UP", "3");
        c3.setTypeName(Bundle.getMessage("Caboose"));
        c3.setLength("33");
        c3.setMoves(0);
        c3.setOwner("DAB");
        c3.setBuilt("1944");
        c3.setCaboose(true);

        Car c4 = cmanager.newRS("UP", "4");
        c4.setTypeName(carTypes[1]);
        c4.setLength("40");
        c4.setMoves(16);
        c4.setOwner("DAB");
        c4.setBuilt("1958");
        c4.setFred(true);

        Car c5 = cmanager.newRS("SP", "5");
        c5.setTypeName(carTypes[1]);
        c5.setLength("40");
        c5.setMoves(8);
        c5.setOwner("DAB");
        c5.setBuilt("1958");
        c5.setFred(true);

        Car c6 = cmanager.newRS("NH", "6");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(2);
        c6.setOwner("DAB");
        c6.setBuilt("1958");
        c6.setFred(true);

        Car c7 = cmanager.newRS("UP", "7");
        c7.setTypeName(carTypes[5]);
        c7.setLength("40");
        c7.setMoves(5);
        c7.setOwner("DAB");
        c7.setBuilt("1958");

        Car c8 = cmanager.newRS("SP", "8");
        c8.setTypeName(carTypes[1]);
        c8.setLength("40");
        c8.setMoves(4);
        c8.setOwner("DAB");
        c8.setBuilt("1958");

        Car c9 = cmanager.newRS("NH", "9");
        c9.setTypeName(carTypes[1]);
        c9.setLength("40");
        c9.setMoves(3);
        c9.setOwner("DAB");
        c9.setBuilt("1944");

        Car c10 = cmanager.newRS("NH", "10");
        c10.setTypeName(carTypes[1]);
        c10.setLength("40");
        c10.setMoves(10);
        c10.setOwner("DAB");
        c10.setBuilt("1958");

        Car c11 = cmanager.newRS("SP", "11");
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

        Location loc2 = lmanager.newLocation("Acton");

        Track loc2trk1 = loc2.addTrack("Acton Yard", Track.YARD);
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

        // don't allow pickup or drops at Acton
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
        Assert.assertEquals("Place c6", Track.OKAY, c6.setLocation(loc1, loc1trk2));
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
        Assert.assertTrue(new TrainBuilder().build(train1));
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
        Assert.assertTrue(new TrainBuilder().build(train1));
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

        // try again, but now require FRED
        train1.setRequirements(Train.FRED);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination", "", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination", "", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination", "Chelmsford Yard", c5.getDestinationTrackName()); // car with FRED
        Assert.assertEquals("c6 destination", "", c6.getDestinationTrackName()); // car with FRED
        Assert.assertEquals("c7 destination", "Chelmsford Yard", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination", "Chelmsford Yard 2", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination", "Chelmsford Yard", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination", "", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination", "", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination", "Chelmsford Yard 2", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination", "Chelmsford Yard 2", e2.getDestinationTrackName());
        Assert.assertEquals("e3 destination", "", e3.getDestinationTrackName());
        Assert.assertEquals("e4 destination", "", e4.getDestinationTrackName());
        Assert.assertEquals("e5 destination", "", e5.getDestinationTrackName());
        Assert.assertEquals("e6 destination", "", e6.getDestinationTrackName());
        Assert.assertEquals("e7 destination", "", e7.getDestinationTrackName());
        Assert.assertEquals("e8 destination", "", e8.getDestinationTrackName());

        // restore
        loc1trk2.setTrainDirections(Location.NORTH);
        train1.setRequirements(Train.CABOOSE);

        train1.addOwnerName("DAB");
        train1.setOwnerOption(Train.INCLUDE_OWNERS);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
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
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 3", false, train1.isBuilt());

        // restore type Diesel and allow all owners
        train1.addTypeName("Diesel");
        train1.setOwnerOption(Train.ALL_OWNERS);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 4", true, train1.isBuilt());

        // Only allow rolling stock built after 1956
        train1.setBuiltStartYear("1956");
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
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
    }

    /**
     * Test car routing option
     */
    @Test
    public void testTrainBuildOptionsRouting() {

        // confirm default
        Assert.assertTrue(Setup.isCarRoutingEnabled());

        // now turn if off
        Setup.setCarRoutingEnabled(false);

        Train train1 = tmanager.newTrain("testTrainBuildOptionsRouting");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);
        Track actonSpur2 = acton.getTrackByName("Acton Spur 2", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);

        Location texas = lmanager.newLocation("Texas");
        Track texasSpur = texas.addTrack("Texas Spur", Track.SPUR);
        texasSpur.setLength(200);

        // place two cars at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("A", "2", "Boxcar", "40", actonSpur1, 0);

        // send one car to the other spur, this does not require routing, only one train needed
        c1.setFinalDestination(acton);
        c1.setFinalDestinationTrack(actonSpur2);

        // send the other to Texas, not reachable
        c2.setFinalDestination(texas);
        c2.setFinalDestinationTrack(texasSpur);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("c1 destination", actonSpur2, c1.getDestinationTrack());
        Assert.assertEquals("c2 destination", chelmsfordSpur1, c2.getDestinationTrack());
    }

    /**
     * Test car routing option routing only with selected trains
     */
    @Test
    public void testTrainBuildOptionsRoutingRestrictedTrains() {

        // confirm default
        Assert.assertFalse(Setup.isOnlyActiveTrainsEnabled());

        // now turn if on
        Setup.setOnlyActiveTrainsEnabled(true);

        Train train1 = tmanager.newTrain("testTrainBuildOptionsRoutingRestrictedTrains1");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Train train2 = tmanager.newTrain("testTrainBuildOptionsRoutingRestrictedTrains2");
        train2.setRoute(route);

        // confirm default for train selected
        Assert.assertTrue(train1.isBuildEnabled());

        // now turn it off
        train1.setBuildEnabled(false);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);
        Track actonSpur2 = acton.getTrackByName("Acton Spur 2", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();
        Track chelmsfordInterchange1 = chelmsford.getTrackByName("Chelmsford Interchange 1", null);
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);

        Location texas = lmanager.newLocation("Texas");
        Track texasSpur = texas.addTrack("Texas Spur", Track.SPUR);
        texasSpur.setLength(200);

        // third train goes to Texas departs Chelmsford
        Train train3 = tmanager.newTrain("Train Chelmsford to Texas");
        Route rct = rmanager.newRoute("Route Chelmsford to Texas");
        rct.addLocation(chelmsford);
        rct.addLocation(texas);
        train3.setRoute(rct);

        // place two cars at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("A", "2", "Boxcar", "40", actonSpur1, 0);

        // send one car to the other spur, this does not require routing, only one train needed
        c1.setFinalDestination(acton);
        c1.setFinalDestinationTrack(actonSpur2);

        // send the other to Texas, using train 3
        c2.setFinalDestination(texas);
        c2.setFinalDestinationTrack(texasSpur);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("c1 destination", actonSpur2, c1.getDestinationTrack());
        Assert.assertEquals("c2 destination", chelmsfordInterchange1, c2.getDestinationTrack());

        // now disable the use of train 3, shouldn't be able to route car to Texas
        train3.setBuildEnabled(false);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("c1 destination", actonSpur2, c1.getDestinationTrack());
        Assert.assertEquals("c2 destination", chelmsfordSpur1, c2.getDestinationTrack());
    }

    /**
     * Test train build option to move car with a final destination, even if it
     * isn't the most efficient route. Causes extra car movement. Three trains
     * are created, the efficient route is train1 and train 3. Attempts to use
     * train 2 to move car.
     */
    @Test
    public void testTrainBuildOptionsServiceAllCarsWithFinalDestinationsA() {

        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        // train 2 only travels from Acton to Boston
        Train train2 = tmanager.newTrain("Train Acton-Boston");
        Location acton = route.getDepartsRouteLocation().getLocation();
        Location boston = route.getRouteLocationBySequenceNumber(2).getLocation();
        Route rAB = rmanager.newRoute("Route Acton to Boston");
        RouteLocation rlA = rAB.addLocation(acton);
        rAB.addLocation(boston);
        train2.setRoute(rAB);

        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);
        Track actonInterchange1 = acton.getTrackByName("Acton Interchange 1", null);
        Track bostonInterchange1 = boston.getTrackByName("Boston Interchange 1", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();

        Location texas = lmanager.newLocation("Texas");
        Track texasSpur = texas.addTrack("Texas Spur", Track.SPUR);
        texasSpur.setLength(200);

        // third train goes to Texas departs Chelmsford
        Train train3 = tmanager.newTrain("Train Chelmsford-Texas");
        Route rct = rmanager.newRoute("Route Chelmsford-Texas");
        rct.addLocation(chelmsford);
        rct.addLocation(texas);
        train3.setRoute(rct);

        // place car at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);

        // and send to Texas, using train 3
        c1.setFinalDestination(texas);
        c1.setFinalDestinationTrack(texasSpur);

        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train 2 status", train2.isBuilt());

        // c1 should not be part of train 2, the more efficient route is by train 1
        Assert.assertEquals("c1 not part of train", null, c1.getTrain());

        // now override and move c1, confirm default
        Assert.assertFalse(train2.isServiceAllCarsWithFinalDestinationsEnabled());
        // now turn if on
        train2.setServiceAllCarsWithFinalDestinationsEnabled(true);

        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train 2 status", train2.isBuilt());

        // confirm car destinations
        Assert.assertEquals("c1 assigned to train", train2, c1.getTrain());
        Assert.assertEquals("c1 destination", bostonInterchange1, c1.getDestinationTrack());

        // now configure train 2 to not be able to service c1
        rlA.setMaxTrainLength(40); // c1 length is 40 feet, 44 feet with couplers

        train2.reset();
        // now build allowing train length check
        train2.build();
        Assert.assertTrue("Train 2 status", train2.isBuilt());

        // confirm car destinations, a local move was the only option
        Assert.assertEquals("c1 assigned to train", train2, c1.getTrain());
        Assert.assertEquals("c1 destination", actonInterchange1, c1.getDestinationTrack());
    }

    /**
     * Test train build option to move car with a final destination, even if it
     * isn't the most efficient route. Causes extra car movement. Two trains are
     * created, the efficient route is train2 Attempts to use train 1 to move
     * car.
     */
    @Test
    public void testTrainBuildOptionsServiceAllCarsWithFinalDestinationsB() {

        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford 1");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Train train2 = tmanager.newTrain("Train Acton-Boston-Chelmsford 2");
        train2.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();
        Track chelmsfordInterchange2 = chelmsford.getTrackByName("Chelmsford Interchange 2", null);

        // place car at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);

        // and send to Chelmsford
        c1.setFinalDestination(chelmsford);
        c1.setFinalDestinationTrack(chelmsfordInterchange2);

        train2.setServiceAllCarsWithFinalDestinationsEnabled(true);

        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train 2 status", train2.isBuilt());

        // confirm car destinations
        Assert.assertEquals("c1 assigned to train", train2, c1.getTrain());
        Assert.assertEquals("c1 destination", chelmsfordInterchange2, c1.getDestinationTrack());

        RouteLocation rlB = route.getRouteLocationBySequenceNumber(2);
        Location boston = rlB.getLocation();
        Track bostonInterchange1 = boston.getTrackByName("Boston Interchange 1", null);
        // now configure train 2 to not be able to service c1
        rlB.setMaxTrainLength(40); // c1 length is 40 feet, 44 feet with couplers

        train2.reset();
        // now build allowing train length check
        train2.build();
        Assert.assertTrue("Train 2 status", train2.isBuilt());

        // confirm car destinations, a local move was the only option
        Assert.assertEquals("c1 assigned to train", train2, c1.getTrain());
        Assert.assertEquals("c1 destination", bostonInterchange1, c1.getDestinationTrack());
    }

    /**
     * Test that a car given a destination that isn't reachable, isn't moved if
     * the option "Service all cars with a final destination" is enabled.
     */
    @Test
    public void testTrainBuildOptionsServiceAllCarsWithFinalDestinationsC() {

        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford 1");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);

        Location texas = lmanager.newLocation("Can not get there");
        Track trackAtNowhere = texas.addTrack("Yard", Track.YARD);
        trackAtNowhere.setLength(100);

        // place car at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);

        // give the car a final destination that isn't reachable
        train1.reset();
        c1.setFinalDestination(texas);

        // build should attempt to move car to new location that might be routeable
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 assigned to train", train1, c1.getTrain());
        Assert.assertEquals("c1 destination", chelmsfordSpur1, c1.getDestinationTrack());

        // disable the option to move car, a bit counter intuitive
        train1.setServiceAllCarsWithFinalDestinationsEnabled(true);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 not assigned to train", null, c1.getTrain());
        Assert.assertEquals("c1 destination", null, c1.getDestinationTrack());
    }

    @Test
    public void testTrainBuildOptionsNoThroughCars() {

        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford 1");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        Location boston = route.getRouteLocationBySequenceNumber(2).getLocation();
        Track bostonSpur1 = boston.getTrackByName("Boston Spur 1", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);

        // place car at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 assigned to train", train1, c1.getTrain());
        Assert.assertEquals("c1 destination", chelmsfordSpur1, c1.getDestinationTrack());

        // now don't allow car to travel from departure to terminal
        train1.setAllowThroughCarsEnabled(false);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 assigned to train", train1, c1.getTrain());
        Assert.assertEquals("c1 destination", bostonSpur1, c1.getDestinationTrack());

        // give the car a final destination of Chelmsford
        train1.reset();
        c1.setFinalDestination(chelmsford);

        // build should ignore c1
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 not assigned to train", null, c1.getTrain());
        Assert.assertEquals("c1 destination", null, c1.getDestinationTrack());

        // test departing staging
        actonSpur1.setTrackType(Track.STAGING);

        // build should fail
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train status", train1.isBuilt());
    }

    /**
     * Tries to send a car with a final destination to a track that is too
     * short, track CAPACITY issue.
     */
    @Test
    public void testCarWithFinalDesinationA() {

        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford 1");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);

        Location boston = route.getRouteLocationBySequenceNumber(2).getLocation();
        Track bostonSpur1 = boston.getTrackByName("Boston Spur 1", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);

        // place car at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);

        // give the car a final destination to Boston spur 1
        train1.reset();
        c1.setFinalDestination(boston);
        c1.setFinalDestinationTrack(bostonSpur1);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 assigned to train", train1, c1.getTrain());
        Assert.assertEquals("c1 destination", bostonSpur1, c1.getDestinationTrack());

        // now make the track too short for c1
        bostonSpur1.setLength(40);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 train", train1, c1.getTrain());
        Assert.assertEquals("c1 destination", chelmsfordSpur1, c1.getDestinationTrack());

        // track was too short for c1, so final destination should have been removed
        Assert.assertEquals("c1 final destination", null, c1.getFinalDestinationTrack());
    }

    /**
     * Test car with final destination local move.
     */
    @Test
    public void testCarWithFinalDesinationB() {

        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        // confirm default
        Assert.assertTrue(train1.isAllowLocalMovesEnabled());

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);
        // any track at Acton should work
        Track actonInterchange1 = acton.getTrackByName("Acton Interchange 1", null);

        // place car at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonSpur1, 0);

        // give the car a final destination to acton
        train1.reset();
        c1.setFinalDestination(acton);
        c1.setFinalDestinationTrack(actonInterchange1);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 assigned to train", train1, c1.getTrain());
        Assert.assertEquals("c1 destination", actonInterchange1, c1.getDestinationTrack());

        // now don't the train to make a local move
        train1.setAllowLocalMovesEnabled(false);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train status", train1.isBuilt());

        // confirm car destination
        Assert.assertEquals("c1 not assigned to train", null, c1.getTrain());
        Assert.assertEquals("c1 destination", null, c1.getDestinationTrack());

        Assert.assertEquals("c1 final destination", acton, c1.getFinalDestination());
        Assert.assertEquals("c1 final destination track", actonInterchange1, c1.getFinalDestinationTrack());
    }

    @Test
    public void testTrainBuildOptionSendCarToTerminal() {

        Train train1 = tmanager.newTrain("testTrainBuildOptionsSendCarToTerminal");
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Location boston = route.getRouteLocationBySequenceNumber(2).getLocation();
        Location danvers = route.getRouteLocationBySequenceNumber(4).getLocation();
        Location essex = route.getTerminatesRouteLocation().getLocation();

        Track actonSpur1 = acton.getTrackByName("Acton Spur 1", null);
        Track bostonInterchange1 = boston.getTrackByName("Boston Interchange 1", null);
        Track daversSpur1 = danvers.getTrackByName("Danvers Spur 1", null);
        Track essexSpur1 = essex.getTrackByName("Essex Spur 1", null);

        // need to test "similar" terminal location names
        Location essex2 = lmanager.newLocation("Essex-2"); // "similar" name
        Track essex2Yard = essex2.addTrack("Yard X", Track.YARD);
        essex2Yard.deleteTypeName("Boxcar");
        essex2Yard.setLength(200);
        route.addLocation(essex2);

        Location essex3 = lmanager.newLocation("Essex-3"); // "similar" name
        Track essex3Yard = essex3.addTrack("Yard Y", Track.YARD);
        essex3Yard.deleteTypeName("Flat");
        essex3Yard.setLength(200);
        route.addLocation(essex3);

        // place cars
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Flat", "40", actonSpur1, 1);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("A", "2", "Boxcar", "40", actonSpur1, 2);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("A", "3", "Boxcar", "40", actonSpur1, 3);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("A", "4", "Flat", "40", actonSpur1, 4);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("B", "5", "Boxcar", "40", bostonInterchange1, 5);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("B", "6", "Boxcar", "40", bostonInterchange1, 6);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("B", "7", "Boxcar", "40", bostonInterchange1, 7);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("B", "8", "Flat", "40", bostonInterchange1, 8);
        Car c9 = JUnitOperationsUtil.createAndPlaceCar("B", "9", "Flat", "40", bostonInterchange1, 9);
        Car c10 = JUnitOperationsUtil.createAndPlaceCar("E", "10", "Boxcar", "40", essexSpur1, 5);

        // send all car picks to terminal, except cars at origin
        train1.setSendCarsToTerminalEnabled(true);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 2 status", train1.isBuilt());

        Assert.assertEquals("car destination track", essex2Yard, c1.getDestinationTrack());
        Assert.assertEquals("car destination track", essex3Yard, c2.getDestinationTrack());
        Assert.assertEquals("car destination track", essexSpur1, c3.getDestinationTrack());
        Assert.assertEquals("car destination track", daversSpur1, c4.getDestinationTrack());
        Assert.assertEquals("car destination track", essex3Yard, c5.getDestinationTrack());
        Assert.assertEquals("car destination track", essex3Yard, c6.getDestinationTrack());
        Assert.assertEquals("car destination track", essexSpur1, c7.getDestinationTrack());
        Assert.assertEquals("car destination track", essex2Yard, c8.getDestinationTrack());
        Assert.assertEquals("car destination track", essex2Yard, c9.getDestinationTrack());
        Assert.assertEquals("car destination track", null, c10.getDestinationTrack());
    }

    /**
     * Test car routing option extreme track destination restrictions
     */
    @Test
    public void testTrainBuildOptionsRestrictedDestinations() {

        // confirm default
        Assert.assertFalse(Setup.isCheckCarDestinationEnabled());

        // now turn if on
        Setup.setCheckCarDestinationEnabled(true);

        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford");
        Route route = JUnitOperationsUtil.createThreeLocationRoute();
        train1.setRoute(route);

        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonInterchange1 = acton.getTrackByName("Acton Interchange 1", null);

        Location chelmsford = route.getTerminatesRouteLocation().getLocation();
        Track chelmsfordInterchange1 = chelmsford.getTrackByName("Chelmsford Interchange 1", null);
        Track chelmsfordSpur1 = chelmsford.getTrackByName("Chelmsford Spur 1", null);

        Location texas = lmanager.newLocation("Texas");
        Track texasSpur = texas.addTrack("Texas Spur", Track.SPUR);
        texasSpur.setLength(200);

        // 2nd train goes to Texas departs Chelmsford
        Train train2 = tmanager.newTrain("Train Chelmsford to Texas");
        Route rct = rmanager.newRoute("Route Chelmsford to Texas");
        rct.addLocation(chelmsford);
        rct.addLocation(texas);
        train2.setRoute(rct);

        // place two cars at start of route
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("A", "1", "Boxcar", "40", actonInterchange1, 0);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("A", "2", "Boxcar", "40", actonInterchange1, 0);

        // send one car to Chelmsford
        c1.setFinalDestination(chelmsford);
        c1.setFinalDestinationTrack(chelmsfordSpur1);

        // send the other to Texas, using train 3
        c2.setFinalDestination(texas);
        c2.setFinalDestinationTrack(texasSpur);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("c1 destination", chelmsfordSpur1, c1.getDestinationTrack());
        Assert.assertEquals("c2 destination", chelmsfordInterchange1, c2.getDestinationTrack());

        // now place destination restrictions on departure track at Acton
        actonInterchange1.setDestinationOption(Track.INCLUDE_DESTINATIONS);

        // there aren't any valid destinations for the departure track, cars should be stuck there
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("c1 destination", null, c1.getDestinationTrack());
        Assert.assertEquals("c2 destination", null, c2.getDestinationTrack());

        // only allow Texas as a destination, shouldn't route cars to Chelmsford
        actonInterchange1.addDestination(texas);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("c1 destination", null, c1.getDestinationTrack());
        Assert.assertEquals("c2 destination", null, c2.getDestinationTrack());

        // now add Chelmsford as a destination, this should work
        actonInterchange1.addDestination(chelmsford);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("c1 destination", chelmsfordSpur1, c1.getDestinationTrack());
        Assert.assertEquals("c2 destination", chelmsfordInterchange1, c2.getDestinationTrack());

        // now remove Texas as a destination, should work
        actonInterchange1.deleteDestination(texas);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("c1 destination", chelmsfordSpur1, c1.getDestinationTrack());
        Assert.assertEquals("c2 destination", chelmsfordInterchange1, c2.getDestinationTrack());

        // now turn off extreme destination restrictions
        Setup.setCheckCarDestinationEnabled(false);
        actonInterchange1.deleteDestination(chelmsford);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 status", train1.isBuilt());

        // confirm car destinations
        Assert.assertEquals("c1 destination", chelmsfordSpur1, c1.getDestinationTrack());
        Assert.assertEquals("c2 destination", chelmsfordInterchange1, c2.getDestinationTrack());

    }

    /**
     * test car blocking out of staging. Matches the largest block of cars going
     * into staging with the largest route move requests out of staging.
     */
    @Test
    public void testCarBlockingFromStagingA() {

        // use 5 locations
        JUnitOperationsUtil.createSevenNormalLocations();

        Location acton = lmanager.newLocation("Acton");
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", Track.YARD);

        Location boston = lmanager.newLocation("Boston");
        Track bostonYard1 = boston.getTrackByName("Boston Yard 1", Track.YARD);

        Location chelmsford = lmanager.newLocation("Chelmsford");
        Track chelmsfordYard1 = chelmsford.getTrackByName("Chelmsford Yard 1", Track.YARD);

        Location danvers = lmanager.newLocation("Danvers");

        // Staging and enable car blocking
        Location westford = lmanager.newLocation("Westford Staging");
        westford.setLocationOps(Location.STAGING);
        Track westfordStaging = westford.addTrack("Staging", Track.STAGING);
        westfordStaging.setLength(1000);
        westfordStaging.setBlockCarsEnabled(true);

        // create two trains, one into staging, the other out of staging
        Setup.setCarMoves(20); // set default to 20 moves per location

        // Create route with 5 location
        Route rte1 = rmanager.newRoute("Route Acton-Boston-Chelmsford-Danvers-WestfordStaging");
        rte1.addLocation(acton);
        RouteLocation rl2 = rte1.addLocation(boston);
        rl2.setDropAllowed(false); // only do pulls for this test

        RouteLocation rl3 = rte1.addLocation(chelmsford);
        rl3.setDropAllowed(false); // only do pulls for this test

        RouteLocation rl4 = rte1.addLocation(danvers);
        rl4.setDropAllowed(false); // only do pulls for this test

        rte1.addLocation(westford); // staging, all cars go here

        // Create train
        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford-Danvers-WestfordStaging");
        train1.setRoute(rte1);
        train1.setRequirements(Train.CABOOSE);

        // create and place cars
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "40", actonYard1, 10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", "Boxcar", "40", actonYard1, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", actonYard1, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", actonYard1, 13);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", "Boxcar", "40", actonYard1, 14);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", "Boxcar", "40", actonYard1, 15);

        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", "Boxcar", "40", bostonYard1, 16);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", "Boxcar", "40", bostonYard1, 17);

        Car c9 = JUnitOperationsUtil.createAndPlaceCar("CP", "90", "Boxcar", "40", chelmsfordYard1, 18);
        Car c10 = JUnitOperationsUtil.createAndPlaceCar("CP", "100", "Boxcar", "40", chelmsfordYard1, 19);
        Car c11 = JUnitOperationsUtil.createAndPlaceCar("CP", "110", "Boxcar", "40", chelmsfordYard1, 20);

        c1.setCaboose(true);

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

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // terminate train into staging
        train1.terminate();

        // now check car's last known location id
        Assert.assertEquals("Car departed Acton", acton.getId(), c1.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c2.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c3.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c4.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c5.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c6.getLastLocationId());

        Assert.assertEquals("Car departed Boston", boston.getId(), c7.getLastLocationId());
        Assert.assertEquals("Car departed Boston", boston.getId(), c8.getLastLocationId());

        Assert.assertEquals("Car departed Chelmsford", chelmsford.getId(), c9.getLastLocationId());
        Assert.assertEquals("Car departed Chelmsford", chelmsford.getId(), c10.getLastLocationId());
        Assert.assertEquals("Car departed Chelmsford", chelmsford.getId(), c11.getLastLocationId());

        // now build a train that departs staging
        Route rte2 = rmanager.newRoute("Route WestfordStaging-Danvers-Chelmsford-Boston-Acton");
        rte2.addLocation(westford); // staging

        RouteLocation rlDanvers = rte2.addLocation(danvers);
        rlDanvers.setMaxCarMoves(4); // 2nd largest block goes here

        RouteLocation rlChelmsford = rte2.addLocation(chelmsford);
        rlChelmsford.setMaxCarMoves(2); // no cars go here

        RouteLocation rlBoston = rte2.addLocation(boston);
        rlBoston.setMaxCarMoves(7); // largest block of cars goes here

        RouteLocation rlActon = rte2.addLocation(acton);
        rlActon.setMaxCarMoves(3); // 3rd largest block goes here

        // Create train, there are 3 blocks of cars in staging that were picked up by train 1
        Train train2 = tmanager.newTrain("Train WestfordStaging-Danvers-Chelmsford-Boston-Acton");
        train2.setRoute(rte2);

        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // car's last known location id doesn't change when building train
        Assert.assertEquals("Car departed Acton", acton.getId(), c1.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c2.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c3.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c4.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c5.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c6.getLastLocationId());

        Assert.assertEquals("Car departed Boston", boston.getId(), c7.getLastLocationId());
        Assert.assertEquals("Car departed Boston", boston.getId(), c8.getLastLocationId());

        Assert.assertEquals("Car departed Chelmsford", chelmsford.getId(), c9.getLastLocationId());
        Assert.assertEquals("Car departed Chelmsford", chelmsford.getId(), c10.getLastLocationId());
        Assert.assertEquals("Car departed Chelmsford", chelmsford.getId(), c11.getLastLocationId());

        // now check to see if cars were blocked to the right locations
        Assert.assertEquals("Caboose must go to terminal", acton, c1.getDestination());

        // largest block 5 cars
        Assert.assertEquals("Car departed Acton destination Boston", boston, c2.getDestination());
        Assert.assertEquals("Car departed Acton destination Boston", boston, c3.getDestination());
        Assert.assertEquals("Car departed Acton destination Boston", boston, c4.getDestination());
        Assert.assertEquals("Car departed Acton destination Boston", boston, c5.getDestination());
        Assert.assertEquals("Car departed Acton destination Boston", boston, c6.getDestination());

        // smallest block, 2 cars
        Assert.assertEquals("Car departed Boston destination Acton", acton, c7.getDestination());
        Assert.assertEquals("Car departed Boston destination Acton", acton, c8.getDestination());

        // middle block, 3 cars
        Assert.assertEquals("Car departed Chelmsford destination Danvers", danvers, c9.getDestination());
        Assert.assertEquals("Car departed Chelmsford destination Danvers", danvers, c10.getDestination());
        Assert.assertEquals("Car departed Chelmsford destination Danvers", danvers, c11.getDestination());

        // now try the case where the largest block is greater than any requested moves for a location
        rlBoston.setMaxCarMoves(4);

        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // now check to see if cars were blocked to the right locations
        Assert.assertEquals("Caboose must go to terminal", acton, c1.getDestination());

        // largest block 5 cars was broken up since they all couldn't go to the same location
        Assert.assertEquals("Car orginally departed Acton", chelmsford, c2.getDestination());
        Assert.assertEquals("Car orginally departed Acton", acton, c3.getDestination());
        Assert.assertEquals("Car orginally departed Acton", acton, c4.getDestination());
        Assert.assertEquals("Car orginally departed Acton", chelmsford, c5.getDestination());
        Assert.assertEquals("Car orginally departed Acton", danvers, c6.getDestination());

        // smallest block, 2 cars
        Assert.assertEquals("Car orginally departed Boston", danvers, c7.getDestination());
        Assert.assertEquals("Car orginally departed Boston", danvers, c8.getDestination());

        // middle block, now the largest that can move as a group, 3 cars
        Assert.assertEquals("Car orginally departed Chelmsford", boston, c9.getDestination());
        Assert.assertEquals("Car orginally departed Chelmsford", boston, c10.getDestination());
        Assert.assertEquals("Car orginally departed Chelmsford", boston, c11.getDestination());

        // now check car last destination out of staging
        train2.terminate();

        Assert.assertEquals("Car orginally departed Acton", westford.getId(), c1.getLastLocationId());
        Assert.assertEquals("Car orginally departed Acton", westford.getId(), c2.getLastLocationId());
        Assert.assertEquals("Car orginally departed Acton", westford.getId(), c3.getLastLocationId());
        Assert.assertEquals("Car orginally departed Acton", westford.getId(), c4.getLastLocationId());
        Assert.assertEquals("Car orginally departed Acton", westford.getId(), c5.getLastLocationId());
        Assert.assertEquals("Car orginally departed Acton", westford.getId(), c6.getLastLocationId());

        Assert.assertEquals("Car orginally departed Boston", westford.getId(), c7.getLastLocationId());
        Assert.assertEquals("Car orginally departed Boston", westford.getId(), c8.getLastLocationId());

        Assert.assertEquals("Car orginally departed Chelmsford", westford.getId(), c9.getLastLocationId());
        Assert.assertEquals("Car orginally departed Chelmsford", westford.getId(), c10.getLastLocationId());
        Assert.assertEquals("Car orginally departed Chelmsford", westford.getId(), c11.getLastLocationId());
    }

    /**
     * test car blocking out of staging. All cars in staging are from one
     * location. Need at least two car blocks in staging for blocking to occur.
     */
    @Test
    public void testCarBlockingFromStagingB() {

        // use 5 locations
        JUnitOperationsUtil.createSevenNormalLocations();

        Location acton = lmanager.newLocation("Acton");
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", Track.YARD);

        Location boston = lmanager.newLocation("Boston");

        Location chelmsford = lmanager.newLocation("Chelmsford");

        Location danvers = lmanager.newLocation("Danvers");

        // Staging and enable car blocking
        Location westford = lmanager.newLocation("Westford Staging");
        westford.setLocationOps(Location.STAGING);
        Track westfordStaging = westford.addTrack("Staging", Track.STAGING);
        westfordStaging.setLength(1000);
        westfordStaging.setBlockCarsEnabled(true);

        // create two trains, one into staging, the other out of staging
        Setup.setCarMoves(20); // set default to 20 moves per location

        // Create route with 5 location
        Route rte1 = rmanager.newRoute("Route Acton-Boston-Chelmsford-Danvers-WestfordStaging");
        rte1.addLocation(acton);
        RouteLocation rl2 = rte1.addLocation(boston);
        rl2.setDropAllowed(false); // only do pulls for this test

        RouteLocation rl3 = rte1.addLocation(chelmsford);
        rl3.setDropAllowed(false); // only do pulls for this test

        RouteLocation rl4 = rte1.addLocation(danvers);
        rl4.setDropAllowed(false); // only do pulls for this test

        rte1.addLocation(westford); // staging, all cars go here

        // Create train
        Train train1 = tmanager.newTrain("Train Acton-Boston-Chelmsford-Danvers-WestfordStaging");
        train1.setRoute(rte1);
        train1.setRequirements(Train.CABOOSE);

        // create and place cars
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "40", actonYard1, 10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", "Boxcar", "40", actonYard1, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", actonYard1, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", actonYard1, 13);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", "Boxcar", "40", actonYard1, 14);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", "Boxcar", "40", actonYard1, 15);

        c1.setCaboose(true);

        // check car's last known location id
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c1.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c2.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c3.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c4.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c5.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c6.getLastLocationId());

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // terminate train into staging
        train1.terminate();

        // now check car's last known location id
        Assert.assertEquals("Car departed Acton", acton.getId(), c1.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c2.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c3.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c4.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c5.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c6.getLastLocationId());

        // now build a train that departs staging
        Route rte2 = rmanager.newRoute("Route WestfordStaging-Danvers-Chelmsford-Boston-Acton");
        rte2.addLocation(westford); // staging

        RouteLocation rlDanvers = rte2.addLocation(danvers);
        rlDanvers.setMaxCarMoves(4); // 2nd largest block goes here

        RouteLocation rlChelmsford = rte2.addLocation(chelmsford);
        rlChelmsford.setMaxCarMoves(2); // no cars go here

        RouteLocation rlBoston = rte2.addLocation(boston);
        rlBoston.setMaxCarMoves(7); // largest block of cars goes here

        RouteLocation rlActon = rte2.addLocation(acton);
        rlActon.setMaxCarMoves(3); // 3rd largest block goes here

        // Create train
        Train train2 = tmanager.newTrain("Train WestfordStaging-Danvers-Chelmsford-Boston-Acton");
        train2.setRoute(rte2);

        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // car's last known location id doesn't change when building train
        Assert.assertEquals("Car departed Acton", acton.getId(), c1.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c2.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c3.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c4.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c5.getLastLocationId());
        Assert.assertEquals("Car departed Acton", acton.getId(), c6.getLastLocationId());

        // now check to see if cars were blocked to the right locations
        Assert.assertEquals("Caboose must go to terminal", acton, c1.getDestination());

        // largest block 5 cars, broken up since there was only one block in staging
        Assert.assertEquals("Car orginally departed Acton", boston, c2.getDestination());
        Assert.assertEquals("Car orginally departed Acton", chelmsford, c3.getDestination());
        Assert.assertEquals("Car orginally departed Acton", danvers, c4.getDestination());
        Assert.assertEquals("Car orginally departed Acton", acton, c5.getDestination());
        Assert.assertEquals("Car orginally departed Acton", boston, c6.getDestination());
    }

    /**
     * test car blocking out of staging. All cars in staging were placed there,
     * so no blocking location ids. Blocking is not possible
     */
    @Test
    public void testCarBlockingFromStagingC() {

        // use 5 locations
        JUnitOperationsUtil.createSevenNormalLocations();

        Location acton = lmanager.newLocation("Acton");
        Location boston = lmanager.newLocation("Boston");
        Location chelmsford = lmanager.newLocation("Chelmsford");
        Location danvers = lmanager.newLocation("Danvers");

        // Staging and enable car blocking
        Location westford = lmanager.newLocation("Westford Staging");
        westford.setLocationOps(Location.STAGING);
        Track westfordStaging = westford.addTrack("Staging", Track.STAGING);
        westfordStaging.setLength(1000);
        westfordStaging.setBlockCarsEnabled(true);

        Setup.setCarMoves(20); // set default to 20 moves per location

        // create and place cars
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "40", westfordStaging,
                10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", "Boxcar", "40", westfordStaging, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", westfordStaging, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", westfordStaging, 13);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", "Boxcar", "40", westfordStaging, 14);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", "Boxcar", "40", westfordStaging, 15);

        c1.setCaboose(true);

        // check car's last known location id
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c1.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c2.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c3.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c4.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c5.getLastLocationId());
        Assert.assertEquals("Last location isn't known", Car.LOCATION_UNKNOWN, c6.getLastLocationId());

        // now build a train that departs staging
        Route rte2 = rmanager.newRoute("Route WestfordStaging-Danvers-Chelmsford-Boston-Acton");
        rte2.addLocation(westford); // staging

        RouteLocation rlDanvers = rte2.addLocation(danvers);
        rlDanvers.setMaxCarMoves(4); // 2nd largest block goes here

        RouteLocation rlChelmsford = rte2.addLocation(chelmsford);
        rlChelmsford.setMaxCarMoves(2); // no cars go here

        RouteLocation rlBoston = rte2.addLocation(boston);
        rlBoston.setMaxCarMoves(7); // largest block of cars goes here

        RouteLocation rlActon = rte2.addLocation(acton);
        rlActon.setMaxCarMoves(3); // 3rd largest block goes here

        // Create train
        Train train2 = tmanager.newTrain("Train WestfordStaging-Danvers-Chelmsford-Boston-Acton");
        train2.setRoute(rte2);

        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // car's last known location id doesn't change when building train
        Assert.assertEquals("Car departed Acton", Car.LOCATION_UNKNOWN, c1.getLastLocationId());
        Assert.assertEquals("Car departed Acton", Car.LOCATION_UNKNOWN, c2.getLastLocationId());
        Assert.assertEquals("Car departed Acton", Car.LOCATION_UNKNOWN, c3.getLastLocationId());
        Assert.assertEquals("Car departed Acton", Car.LOCATION_UNKNOWN, c4.getLastLocationId());
        Assert.assertEquals("Car departed Acton", Car.LOCATION_UNKNOWN, c5.getLastLocationId());
        Assert.assertEquals("Car departed Acton", Car.LOCATION_UNKNOWN, c6.getLastLocationId());

        // now check to see if cars were blocked to the right locations
        Assert.assertEquals("Caboose must go to terminal", acton, c1.getDestination());

        // largest block 5 cars, broken up since there was only one block in staging
        Assert.assertEquals("Car orginally departed Acton", boston, c2.getDestination());
        Assert.assertEquals("Car orginally departed Acton", chelmsford, c3.getDestination());
        Assert.assertEquals("Car orginally departed Acton", danvers, c4.getDestination());
        Assert.assertEquals("Car orginally departed Acton", acton, c5.getDestination());
        Assert.assertEquals("Car orginally departed Acton", boston, c6.getDestination());
    }

    /**
     * Test mixture of cars with block ids, and some without. Cars without an id
     * aren't blocked out of staging.
     */
    @Test
    public void testCarBlockingFromStagingD() {

        // use 5 locations
        JUnitOperationsUtil.createSevenNormalLocations();

        Location acton = lmanager.newLocation("Acton");
        Location boston = lmanager.newLocation("Boston");
        Location chelmsford = lmanager.newLocation("Chelmsford");
        Location danvers = lmanager.newLocation("Danvers");

        // Staging and enable car blocking
        Location westford = lmanager.newLocation("Westford Staging");
        westford.setLocationOps(Location.STAGING);
        Track westfordStaging = westford.addTrack("Staging", Track.STAGING);
        westfordStaging.setLength(1000);
        westfordStaging.setBlockCarsEnabled(true);

        Setup.setCarMoves(20); // set default to 20 moves per location

        // create and place cars
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "40", westfordStaging,
                10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", "Boxcar", "40", westfordStaging, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", westfordStaging, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", westfordStaging, 13);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", "Boxcar", "40", westfordStaging, 14);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", "Boxcar", "40", westfordStaging, 15);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", "Boxcar", "40", westfordStaging, 16);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", "Boxcar", "40", westfordStaging, 17);

        c1.setCaboose(true);

        // provide some blocking ids for cars in staging
        c2.setLastLocationId(chelmsford.getId());
        c3.setLastLocationId(chelmsford.getId());
        c4.setLastLocationId(danvers.getId());
        c5.setLastLocationId(danvers.getId());
        c6.setLastLocationId(boston.getId());

        // check car's last known location id
        Assert.assertEquals("Last location", Car.LOCATION_UNKNOWN, c1.getLastLocationId());
        Assert.assertEquals("Last location", chelmsford.getId(), c2.getLastLocationId());
        Assert.assertEquals("Last location", chelmsford.getId(), c3.getLastLocationId());
        Assert.assertEquals("Last location", danvers.getId(), c4.getLastLocationId());
        Assert.assertEquals("Last location", danvers.getId(), c5.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c6.getLastLocationId()); // only one car in this block
        Assert.assertEquals("Last location", Car.LOCATION_UNKNOWN, c7.getLastLocationId());
        Assert.assertEquals("Last location", Car.LOCATION_UNKNOWN, c8.getLastLocationId());

        // now build a train that departs staging
        Route rte2 = rmanager.newRoute("Route WestfordStaging-Danvers-Chelmsford-Boston-Acton");
        rte2.addLocation(westford); // staging

        RouteLocation rlDanvers = rte2.addLocation(danvers);
        rlDanvers.setMaxCarMoves(4); // 2nd largest block goes here

        RouteLocation rlChelmsford = rte2.addLocation(chelmsford);
        rlChelmsford.setMaxCarMoves(2); // no cars go here

        RouteLocation rlBoston = rte2.addLocation(boston);
        rlBoston.setMaxCarMoves(7); // largest block of cars goes here

        RouteLocation rlActon = rte2.addLocation(acton);
        rlActon.setMaxCarMoves(3); // 3rd largest block goes here

        // Create train
        Train train2 = tmanager.newTrain("Train WestfordStaging-Danvers-Chelmsford-Boston-Acton");
        train2.setRoute(rte2);

        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // car's last known location id doesn't change when building train
        Assert.assertEquals("Last location", Car.LOCATION_UNKNOWN, c1.getLastLocationId());
        Assert.assertEquals("Last location", chelmsford.getId(), c2.getLastLocationId());
        Assert.assertEquals("Last location", chelmsford.getId(), c3.getLastLocationId());
        Assert.assertEquals("Last location", danvers.getId(), c4.getLastLocationId());
        Assert.assertEquals("Last location", danvers.getId(), c5.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c6.getLastLocationId());
        Assert.assertEquals("Last location", Car.LOCATION_UNKNOWN, c7.getLastLocationId());
        Assert.assertEquals("Last location", Car.LOCATION_UNKNOWN, c8.getLastLocationId());

        // now check to see if cars were blocked to the right locations
        Assert.assertEquals("Caboose must go to terminal", acton, c1.getDestination());

        // largest block 4 cars, broken up since there last location is unknown
        Assert.assertEquals("Car destination", danvers, c2.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", danvers, c3.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", boston, c4.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", boston, c5.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", chelmsford, c6.getDestination());
        Assert.assertEquals("Car destination", acton, c7.getDestination());
        Assert.assertEquals("Car destination", boston, c8.getDestination());
    }

    /**
     * Test exceptions for blocking cars out of staging. Car with destination,
     * car with final destination, car with custom load, and car with "E" load
     * and generate custom load out of staging enabled are all exceptions to car
     * blocking.
     */
    @Test
    public void testCarBlockingFromStagingE() {

        // use 5 locations
        JUnitOperationsUtil.createSevenNormalLocations();

        Location acton = lmanager.newLocation("Acton");
        Location boston = lmanager.newLocation("Boston");
        Location chelmsford = lmanager.newLocation("Chelmsford");
        Location danvers = lmanager.newLocation("Danvers");

        // Staging and enable car blocking
        Location westford = lmanager.newLocation("Westford Staging");
        westford.setLocationOps(Location.STAGING);
        Track westfordStaging = westford.addTrack("Staging", Track.STAGING);
        westfordStaging.setLength(1000);
        westfordStaging.setBlockCarsEnabled(true);

        Setup.setCarMoves(20); // set default to 20 moves per location

        // create and place cars
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "40", westfordStaging,
                10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", "Boxcar", "40", westfordStaging, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", westfordStaging, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", westfordStaging, 13);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", "Boxcar", "40", westfordStaging, 14);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", "Boxcar", "40", westfordStaging, 15);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", "Boxcar", "40", westfordStaging, 16);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", "Boxcar", "40", westfordStaging, 17);

        c1.setCaboose(true);

        // provide some blocking ids for cars in staging
        c1.setLastLocationId(boston.getId());
        c2.setLastLocationId(chelmsford.getId());
        c3.setLastLocationId(chelmsford.getId());
        c4.setLastLocationId(danvers.getId());
        c5.setLastLocationId(danvers.getId());
        c6.setLastLocationId(boston.getId());
        c7.setLastLocationId(boston.getId());
        c8.setLastLocationId(boston.getId());

        // check car's last known location id
        Assert.assertEquals("Last location", boston.getId(), c1.getLastLocationId());
        Assert.assertEquals("Last location", chelmsford.getId(), c2.getLastLocationId());
        Assert.assertEquals("Last location", chelmsford.getId(), c3.getLastLocationId());
        Assert.assertEquals("Last location", danvers.getId(), c4.getLastLocationId());
        Assert.assertEquals("Last location", danvers.getId(), c5.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c6.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c7.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c8.getLastLocationId());

        // now build a train that departs staging
        Route rte2 = rmanager.newRoute("Route WestfordStaging-Danvers-Chelmsford-Boston-Acton");
        rte2.addLocation(westford); // staging

        RouteLocation rlDanvers = rte2.addLocation(danvers);
        rlDanvers.setMaxCarMoves(4); // 2nd largest block goes here

        RouteLocation rlChelmsford = rte2.addLocation(chelmsford);
        rlChelmsford.setMaxCarMoves(2); // no cars go here

        RouteLocation rlBoston = rte2.addLocation(boston);
        rlBoston.setMaxCarMoves(7); // largest block of cars goes here

        RouteLocation rlActon = rte2.addLocation(acton);
        rlActon.setMaxCarMoves(3); // 3rd largest block goes here

        // Create train
        Train train2 = tmanager.newTrain("Train WestfordStaging-Danvers-Chelmsford-Boston-Acton");
        train2.setRoute(rte2);

        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // car's last known location id doesn't change when building train
        Assert.assertEquals("Last location", boston.getId(), c1.getLastLocationId());
        Assert.assertEquals("Last location", chelmsford.getId(), c2.getLastLocationId());
        Assert.assertEquals("Last location", chelmsford.getId(), c3.getLastLocationId());
        Assert.assertEquals("Last location", danvers.getId(), c4.getLastLocationId());
        Assert.assertEquals("Last location", danvers.getId(), c5.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c6.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c7.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c8.getLastLocationId());

        // now check to see if cars were blocked to the right locations
        Assert.assertEquals("Caboose must go to terminal", acton, c1.getDestination());

        // confirm destinations
        Assert.assertEquals("Car destination", acton, c2.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", acton, c3.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", danvers, c4.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", danvers, c5.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", boston, c6.getDestination()); // largest block
        Assert.assertEquals("Car destination", boston, c7.getDestination());
        Assert.assertEquals("Car destination", boston, c8.getDestination());

        // now create exceptions for blocking cars
        train2.reset();
        // take two cars out of the boston block
        Assert.assertEquals("car destination", Track.OKAY, c6.setDestination(danvers, null));
        Assert.assertEquals("car destination", Track.OKAY, c7.setDestination(danvers, null));

        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // confirm destinations
        Assert.assertEquals("Car destination", acton, c2.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", acton, c3.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", danvers, c4.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", danvers, c5.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", danvers, c6.getDestination()); // diverted to Danvers
        Assert.assertEquals("Car destination", danvers, c7.getDestination()); // diverted to Danvers
        Assert.assertEquals("Car destination", boston, c8.getDestination()); // was part of largest block

        // train reset will clear c6 and c7 destination
        train2.reset();

        // give cars a final destination, divert from going to Danvers
        c4.setFinalDestination(boston);
        c5.setFinalDestination(boston);

        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // confirm destinations
        Assert.assertEquals("Car destination", acton, c2.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", acton, c3.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", boston, c4.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", boston, c5.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", boston, c6.getDestination()); // largest block from Boston
        Assert.assertEquals("Car destination", boston, c7.getDestination()); // largest block from Boston
        Assert.assertEquals("Car destination", boston, c8.getDestination()); // largest block from Boston

        train2.reset();
        // remove final destination
        c4.setFinalDestination(null);
        c5.setFinalDestination(null);

        // place custom loads in car
        c2.setLoadName("Nuts");
        c3.setLoadName("Nuts");

        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // confirm destinations
        Assert.assertEquals("Car destination", chelmsford, c2.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", acton, c3.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", danvers, c4.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", danvers, c5.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", boston, c6.getDestination()); // largest block from Boston
        Assert.assertEquals("Car destination", boston, c7.getDestination()); // largest block from Boston
        Assert.assertEquals("Car destination", boston, c8.getDestination()); // largest block from Boston

        // remove custom load names, use "L" loads, these should block
        c2.setLoadName("L");
        c3.setLoadName("L");

        // All cars in staging except c2 and c3 have "L" loads
        westfordStaging.setAddCustomLoadsEnabled(true);

        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // confirm destinations, only c2 and c3 are blocked
        Assert.assertEquals("Car destination", acton, c2.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", acton, c3.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", boston, c4.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", chelmsford, c5.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", danvers, c6.getDestination()); // largest block from Boston
        Assert.assertEquals("Car destination", boston, c7.getDestination()); // largest block from Boston
        Assert.assertEquals("Car destination", danvers, c8.getDestination()); // largest block from Boston

        // don't allow "Boxcar" to Acton  
        acton.deleteTypeName("Boxcar");

        // this will cause the block c2 and c3 to break up
        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // confirm destinations, no blocking on any set of cars
        Assert.assertEquals("Car destination", boston, c2.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", chelmsford, c3.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", danvers, c4.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", boston, c5.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", danvers, c6.getDestination()); // largest block from Boston
        Assert.assertEquals("Car destination", boston, c7.getDestination()); // largest block from Boston
        Assert.assertEquals("Car destination", boston, c8.getDestination()); // largest block from Boston
    }

    /**
     * Test more car blocks then available destinations
     */
    @Test
    public void testCarBlockingFromStagingF() {

        // use 4 locations
        JUnitOperationsUtil.createSevenNormalLocations();

        Location acton = lmanager.newLocation("Acton");
        Location boston = lmanager.newLocation("Boston");
        Location chelmsford = lmanager.newLocation("Chelmsford");
        Location danvers = lmanager.newLocation("Danvers");

        // Staging and enable car blocking
        Location westford = lmanager.newLocation("Westford Staging");
        westford.setLocationOps(Location.STAGING);
        Track westfordStaging = westford.addTrack("Staging", Track.STAGING);
        westfordStaging.setLength(1000);
        westfordStaging.setBlockCarsEnabled(true);

        Setup.setCarMoves(20); // set default to 20 moves per location

        // create and place cars
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("CP", "10", Bundle.getMessage("Caboose"), "40", westfordStaging,
                10);
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("CP", "20", "Boxcar", "40", westfordStaging, 11);
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("CP", "30", "Boxcar", "40", westfordStaging, 12);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("CP", "40", "Boxcar", "40", westfordStaging, 13);
        Car c5 = JUnitOperationsUtil.createAndPlaceCar("CP", "50", "Boxcar", "40", westfordStaging, 14);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("CP", "60", "Boxcar", "40", westfordStaging, 15);
        Car c7 = JUnitOperationsUtil.createAndPlaceCar("CP", "70", "Boxcar", "40", westfordStaging, 16);
        Car c8 = JUnitOperationsUtil.createAndPlaceCar("CP", "80", "Boxcar", "40", westfordStaging, 17);

        c1.setCaboose(true);

        // provide some blocking ids for cars in staging
        c1.setLastLocationId(boston.getId());
        c2.setLastLocationId(chelmsford.getId());
        c3.setLastLocationId(chelmsford.getId());
        c4.setLastLocationId(danvers.getId());
        c5.setLastLocationId(danvers.getId());
        c6.setLastLocationId(boston.getId());
        c7.setLastLocationId(boston.getId());
        c8.setLastLocationId(boston.getId());

        // check car's last known location id
        Assert.assertEquals("Last location", boston.getId(), c1.getLastLocationId());
        Assert.assertEquals("Last location", chelmsford.getId(), c2.getLastLocationId());
        Assert.assertEquals("Last location", chelmsford.getId(), c3.getLastLocationId());
        Assert.assertEquals("Last location", danvers.getId(), c4.getLastLocationId());
        Assert.assertEquals("Last location", danvers.getId(), c5.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c6.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c7.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c8.getLastLocationId());

        // now build a train that departs staging
        Route rte2 = rmanager.newRoute("Route WestfordStaging-Boston-Acton");
        rte2.addLocation(westford); // staging

        RouteLocation rlBoston = rte2.addLocation(boston);
        rlBoston.setMaxCarMoves(10); // largest block of cars goes here

        RouteLocation rlActon = rte2.addLocation(acton);
        rlActon.setMaxCarMoves(6); // 2nd largest block goes here

        // Create train
        Train train2 = tmanager.newTrain("Train WestfordStaging-Boston-Acton");
        train2.setRoute(rte2);

        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertEquals("Train should build", true, train2.isBuilt());

        // car's last known location id doesn't change when building train
        Assert.assertEquals("Last location", boston.getId(), c1.getLastLocationId());
        Assert.assertEquals("Last location", chelmsford.getId(), c2.getLastLocationId());
        Assert.assertEquals("Last location", chelmsford.getId(), c3.getLastLocationId());
        Assert.assertEquals("Last location", danvers.getId(), c4.getLastLocationId());
        Assert.assertEquals("Last location", danvers.getId(), c5.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c6.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c7.getLastLocationId());
        Assert.assertEquals("Last location", boston.getId(), c8.getLastLocationId());

        // now check to see if cars were blocked to the right locations
        Assert.assertEquals("Caboose must go to terminal", acton, c1.getDestination());

        // confirm destinations, block c2 and c3 was broken up
        Assert.assertEquals("Car destination", acton, c2.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", boston, c3.getDestination()); // block from Chelmsford
        Assert.assertEquals("Car destination", acton, c4.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", acton, c5.getDestination()); // block from Danvers
        Assert.assertEquals("Car destination", boston, c6.getDestination()); // largest block
        Assert.assertEquals("Car destination", boston, c7.getDestination());
        Assert.assertEquals("Car destination", boston, c8.getDestination());
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

        Location acton = lmanager.newLocation("Acton");
        Track loc2trk1 = acton.addTrack("Acton Yard", Track.YARD);
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

        Engine e1 = emanager.newRS("UP", "1");
        e1.setModel("GP30");
        e1.setOwner("AT");
        e1.setBuilt("1957");
        e1.setConsist(con1);
        e1.setMoves(5);

        Engine e2 = emanager.newRS("SP", "2");
        e2.setModel("GP30");
        e2.setOwner("AT");
        e2.setBuilt("1957");
        e2.setConsist(con1);
        e2.setMoves(5);

        // one engine
        Engine e3 = emanager.newRS("SP", "3");
        e3.setModel("GP40");
        e3.setBuilt("1957");

        Engine e4 = emanager.newRS("UP", "40");
        e4.setModel("GP40");
        e4.setBuilt("1944");
        e4.setMoves(20);

        Engine e5 = emanager.newRS("SP", "50");
        e5.setModel("GP40");
        e5.setBuilt("1944");
        e5.setMoves(20);

        Engine e6 = emanager.newRS("UP", "600");
        e6.setModel("GP40");
        e6.setBuilt("1944");
        e6.setMoves(2);

        Engine e7 = emanager.newRS("SP", "700");
        e7.setModel("GP40");
        e7.setBuilt("1944");
        e7.setMoves(2);

        Engine e8 = emanager.newRS("SP", "800");
        e8.setModel("GP40");
        e8.setBuilt("1944");
        e8.setMoves(20);

        Engine e9 = emanager.newRS("SP", "900");
        e9.setModel("GP30");
        e9.setBuilt("1944");
        e9.setMoves(2);

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(harvard, loc1trk1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(harvard, loc1trk1));

        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(acton, loc2trk1));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(acton, loc2trk1));

        Assert.assertEquals("Place e5", Track.OKAY, e5.setLocation(chelmsford, loc4trk1));
        Assert.assertEquals("Place e6", Track.OKAY, e6.setLocation(chelmsford, loc4trk1));
        Assert.assertEquals("Place e7", Track.OKAY, e7.setLocation(chelmsford, loc4trk1));
        Assert.assertEquals("Place e8", Track.OKAY, e8.setLocation(chelmsford, loc4trk1));
        Assert.assertEquals("Place e9", Track.OKAY, e9.setLocation(chelmsford, loc4trk1));

        Route rte1 = rmanager.newRoute("Route Harvard-Acton-Boston-Chelmsford-Westford");
        rte1.addLocation(harvard);
        RouteLocation rlActon = rte1.addLocation(acton);
        rte1.addLocation(boston);
        RouteLocation rlChelmsford = rte1.addLocation(chelmsford);
        rte1.addLocation(westford);

        // Create train
        Train train1 = tmanager.newTrain("TestEngineChanges");
        train1.setRoute(rte1);

        // depart with 2 engines
        train1.setBuildConsistEnabled(true);
        train1.setNumberEngines("2");

        // change out 2 engines with 1 engine at Acton
        train1.setSecondLegOptions(Train.CHANGE_ENGINES);
        train1.setSecondLegNumberEngines("1");
        train1.setSecondLegStartLocation(rlActon);
        train1.setSecondLegEngineRoad("UP");
        train1.setSecondLegEngineModel("GP40");

        // change out 1 engine with 3 "SP" engines at Chelmsford
        train1.setThirdLegOptions(Train.CHANGE_ENGINES);
        train1.setThirdLegNumberEngines("3");
        train1.setThirdLegStartLocation(rlChelmsford);
        train1.setThirdLegEngineRoad("SP");
        train1.setThirdLegEngineModel("GP40");

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 assigned to train", acton, e1.getDestination());
        Assert.assertEquals("e2 assigned to train", acton, e2.getDestination());

        Assert.assertEquals("e3 not assigned to train due to road name", null, e3.getDestination());
        Assert.assertEquals("e4 assigned to train", chelmsford, e4.getDestination());

        Assert.assertEquals("e5 assigned to train", westford, e5.getDestination());
        Assert.assertEquals("e6 not assigned to train due to road name", null, e6.getDestination());
        Assert.assertEquals("e7 assigned to train", westford, e7.getDestination());
        Assert.assertEquals("e8 assigned to train", westford, e8.getDestination());
        Assert.assertEquals("e9 not assigned to train due to model type", null, e9.getDestination());

        // remove needed engine at Acton
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(null, null));
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should not build", false, train1.isBuilt());

        // restore engine
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(acton, loc2trk1));
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // remove needed engine at Chelmsford
        Assert.assertEquals("Place e8", Track.OKAY, e8.setLocation(null, null));
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should not build", false, train1.isBuilt());

        // restore engine
        Assert.assertEquals("Place e8", Track.OKAY, e8.setLocation(chelmsford, loc4trk1));
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // test swap engines at location at Chelmsford
        train1.setSecondLegOptions(Train.NO_CABOOSE_OR_FRED); // disable swap

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 assigned to train", chelmsford, e1.getDestination());
        Assert.assertEquals("e2 assigned to train", chelmsford, e2.getDestination());
        Assert.assertEquals("e3 assigned to train", null, e3.getDestination());
        Assert.assertEquals("e4 assigned to train", null, e4.getDestination());
        Assert.assertEquals("e5 assigned to train", westford, e5.getDestination());
        Assert.assertEquals("e6 assigned to train", null, e6.getDestination());
        Assert.assertEquals("e7 assigned to train", westford, e7.getDestination());
        Assert.assertEquals("e8 assigned to train", westford, e8.getDestination());
        Assert.assertEquals("e9 assigned to train", null, e9.getDestination());
    }

    /**
     * The program allows up to two caboose changes in a train's route. Route
     * Acton to Westford has caboose changes at Boston and Harvard.
     */
    @Test
    public void testCabooseChanges() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        // test confirms the order cabooses are assigned to the train
        Setup.setBuildAggressive(true);

        // create 5 locations with tracks
        Location acton = lmanager.newLocation("Acton");
        Track actonYard = acton.addTrack("Acton Yard", Track.YARD);
        actonYard.setLength(200);

        Location boston = lmanager.newLocation("Boston");
        Track bostonYard = boston.addTrack("Boston Yard 1", Track.YARD);
        bostonYard.setLength(200);

        Location chelmsford = lmanager.newLocation("Chelmsford");
        Track chelmsfordYard1 = chelmsford.addTrack("Chelmsford Yard 1", Track.YARD);
        chelmsfordYard1.setLength(200);

        Location harvard = lmanager.newLocation("Harvard");
        Track harvardYard1 = harvard.addTrack("Harvard Yard 1", Track.YARD);
        harvardYard1.setLength(200);

        Location westford = lmanager.newLocation("Westford");
        Track westfordYard = westford.addTrack("Westford Yard", Track.YARD);
        westfordYard.setLength(200);

        // create and place cabooses
        Car c1 = cmanager.newRS("ABC", "1");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setCaboose(true);
        Assert.assertEquals("Place c1", Track.OKAY, c1.setLocation(acton, actonYard));

        // car with FRED at departure
        Car f1 = cmanager.newRS("CBA", "1");
        f1.setTypeName(carTypes[1]);
        f1.setLength("32");
        f1.setFred(true);
        Assert.assertEquals("Place f1", Track.OKAY, f1.setLocation(acton, actonYard));

        Car c2 = cmanager.newRS("ABC", "2");
        c2.setTypeName(Bundle.getMessage("Caboose"));
        c2.setLength("32");
        c2.setCaboose(true);
        Assert.assertEquals("Place c2", Track.OKAY, c2.setLocation(boston, bostonYard));

        Car c3 = cmanager.newRS("XYZ", "3");
        c3.setTypeName(Bundle.getMessage("Caboose"));
        c3.setLength("32");
        c3.setCaboose(true);
        c2.setMoves(10);
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(boston, bostonYard));

        Car c4 = cmanager.newRS("ABC", "4");
        c4.setTypeName(Bundle.getMessage("Caboose"));
        c4.setLength("32");
        c4.setCaboose(true);
        Assert.assertEquals("Place c4", Track.OKAY, c4.setLocation(harvard, harvardYard1));

        Car c5 = cmanager.newRS("STU", "5");
        c5.setTypeName(Bundle.getMessage("Caboose"));
        c5.setLength("32");
        c5.setCaboose(true);
        c5.setMoves(10);
        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(harvard, harvardYard1));

        Route rte1 = rmanager.newRoute("Route Acton-Boston-Chelmsford-Harvard-Westford");
        rte1.addLocation(acton);
        RouteLocation rlBoston = rte1.addLocation(boston);
        rte1.addLocation(chelmsford);
        RouteLocation rlHarvard = rte1.addLocation(harvard);
        rte1.addLocation(westford);

        // Create train
        Train train1 = tmanager.newTrain("TestCabooseChanges");
        train1.setRoute(rte1);

        // depart with caboose
        train1.setRequirements(Train.CABOOSE);

        // remove caboose at Harvard
        train1.setThirdLegOptions(Train.REMOVE_CABOOSE);
        train1.setThirdLegStartLocation(rlHarvard);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm caboose destinations
        Assert.assertEquals("Caboose is part of train", harvard, c1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c2.getDestination());
        Assert.assertEquals("Caboose is part of train", null, c3.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c4.getDestination());
        Assert.assertEquals("Caboose is part of train", null, c5.getDestination());

        // swap out caboose at Boston
        train1.setSecondLegOptions(Train.ADD_CABOOSE);
        train1.setSecondLegStartLocation(rlBoston);
        train1.setSecondLegCabooseRoad("XYZ");

        // swap out caboose at Harvard
        train1.setThirdLegOptions(Train.ADD_CABOOSE);
        train1.setThirdLegStartLocation(rlHarvard);
        train1.setThirdLegCabooseRoad("STU");

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm caboose destinations
        Assert.assertEquals("Caboose is part of train", boston, c1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c2.getDestination());
        Assert.assertEquals("Caboose is part of train", harvard, c3.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c4.getDestination());
        Assert.assertEquals("Caboose is part of train", westford, c5.getDestination());

        // now test failures by removing required cabooses
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(null, null));
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should not build", false, train1.isBuilt());

        train1.reset();
        // restore
        Assert.assertEquals("Place c3", Track.OKAY, c3.setLocation(boston, bostonYard));
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        train1.reset();
        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(null, null));
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should not build", false, train1.isBuilt());

        train1.reset();
        Assert.assertEquals("Place c5", Track.OKAY, c5.setLocation(harvard, harvardYard1));
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // now test if caboose had a destination
        train1.reset();
        Assert.assertEquals("c5 destination", Track.OKAY, c5.setDestination(acton, actonYard));
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should not build", false, train1.isBuilt());

        // restore
        c5.setDestination(null, null);

        // now test removing caboose from train
        train1.setSecondLegOptions(Train.REMOVE_CABOOSE);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm caboose destinations
        Assert.assertEquals("Caboose is part of train", boston, c1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c2.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c3.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c4.getDestination());
        Assert.assertEquals("Caboose is part of train", westford, c5.getDestination());

        // now depart without a caboose, add one, then remove it, and continue to destination
        train1.setRequirements(Train.NO_CABOOSE_OR_FRED);
        train1.setSecondLegOptions(Train.ADD_CABOOSE);
        train1.setThirdLegOptions(Train.REMOVE_CABOOSE);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm caboose destinations
        Assert.assertEquals("Caboose is not part of train", null, c1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c2.getDestination());
        Assert.assertEquals("Caboose is part of train", harvard, c3.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c4.getDestination());
        Assert.assertEquals("Caboose is part of train", null, c5.getDestination());

        // try departing with FRED and swapping it for a caboose
        train1.setRequirements(Train.FRED);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm destinations
        Assert.assertEquals("Boxcar is part of train", boston, f1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c1.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c2.getDestination());
        Assert.assertEquals("Caboose is part of train", harvard, c3.getDestination());
        Assert.assertEquals("Caboose is not part of train", null, c4.getDestination());
        Assert.assertEquals("Caboose is part of train", null, c5.getDestination());

    }

    /**
     * Test the automatic assignment of engines to a train based on HP
     * requirements.
     */
    @Test
    public void testAutoHPT() {
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");

        Assert.assertEquals("confirm default of 1 HPT", 1, Setup.getHorsePowerPerTon());

        // create 5 locations with tracks, Route = Acton-Boston-Chelmsford-Danvers-Essex
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", Track.YARD);

        Location boston = lmanager.getLocationByName("Boston");
        Track bostonYard1 = boston.getTrackByName("Boston Yard 1", Track.YARD);

        Location essex = route.getTerminatesRouteLocation().getLocation();

        // create 4 new engine models with different HP ratings
        Engine e1 = emanager.newRS("UP", "1");
        e1.setModel("GP30-200");
        e1.setTypeName("Diesel");
        e1.setHp("200");
        e1.setLength("50");
        e1.setWeightTons("100");
        e1.setMoves(20);

        Engine e2 = emanager.newRS("SP", "2");
        e2.setModel("GP30-400");
        e2.setTypeName("Diesel");
        e2.setHp("400");
        e2.setLength("50");
        e2.setWeightTons("110");
        e2.setMoves(15);

        Engine e3 = emanager.newRS("SP", "3");
        e3.setModel("GP40-800");
        e3.setTypeName("Diesel");
        e3.setHp("800");
        e3.setLength("50");
        e3.setWeightTons("120");
        e3.setMoves(10);

        Engine e4 = emanager.newRS("UP", "10");
        e4.setModel("GP40-1600");
        e4.setTypeName("Diesel");
        e4.setHp("1600");
        e4.setLength("50");
        e4.setWeightTons("130");
        e4.setMoves(5);

        // place this engine later in the route
        Engine e5 = emanager.newRS("SP", "5");
        e5.setModel("GP40-800");
        e5.setTypeName("Diesel");
        e5.setHp("800");
        e5.setLength("50");
        e5.setWeightTons("120");
        e5.setMoves(0); // the 1st engine to try, but at the wrong location      

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(acton, actonYard1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(acton, actonYard1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(acton, actonYard1));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(acton, actonYard1));

        Assert.assertEquals("Place e5", Track.OKAY, e5.setLocation(boston, bostonYard1));

        // add grade to route
        RouteLocation rlBoston = route.getRouteLocationBySequenceNumber(2);
        rlBoston.setGrade(1.0);

        // Create train
        Train train1 = tmanager.newTrain("TestAutoHP");
        train1.setRoute(route);

        // use auto HPT
        train1.setBuildConsistEnabled(true);
        train1.setNumberEngines(Train.AUTO_HPT);

        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 not assigned to train", null, e1.getDestination());
        Assert.assertEquals("e2 assigned to train, train only needs 400 HP", essex, e2.getDestination());
        Assert.assertEquals("e3 not assigned to train", null, e3.getDestination());
        Assert.assertEquals("e4 not assigned to train", null, e4.getDestination());

        // now increase the train's weight
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("UP", "1", carTypes[1], "40", actonYard1, 0);
        c1.setWeightTons("400"); // 400 tons loaded
        c1.setLoadName(cld.getDefaultLoadName());

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 not assigned to train", null, e1.getDestination());
        Assert.assertEquals("e2 not assigned to train", null, e2.getDestination());
        Assert.assertEquals("e3 not assigned to train", null, e3.getDestination());
        Assert.assertEquals("e4 assigned to train, train needs 1600 HP", essex, e4.getDestination());

        // now increase the train's weight
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("UP", "2", carTypes[1], "40", bostonYard1, 0);
        c2.setWeightTons("200"); // 200 tons loaded
        c2.setLoadName(cld.getDefaultLoadName());

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 not assigned to train", null, e1.getDestination());
        Assert.assertEquals("e2 not assigned to train", null, e2.getDestination());
        Assert.assertEquals("e3 assigned to train", essex, e3.getDestination());
        Assert.assertEquals("e4 assigned to train", essex, e4.getDestination());
    }

    /**
     * Test the automatic assignment of engines to a train based on HP
     * requirements. There can be two engine changes in a train's route
     */
    @Test
    public void testAutoHptEngineChanges() {

        Assert.assertEquals("confirm default of 1 HPT", 1, Setup.getHorsePowerPerTon());

        // create 5 locations with tracks, Route = Acton-Boston-Chelmsford-Danvers-Essex
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", Track.YARD);

        Location boston = lmanager.getLocationByName("Boston");
        Track bostonYard1 = boston.getTrackByName("Boston Yard 1", Track.YARD);

        Location danvers = lmanager.getLocationByName("Danvers");
        Track danversYard1 = danvers.getTrackByName("Danvers Yard 1", Track.YARD);

        Location essex = route.getTerminatesRouteLocation().getLocation();

        // create 4 new engine models with different HP ratings
        Engine e1 = emanager.newRS("UP", "1");
        e1.setModel("GP30-200");
        e1.setTypeName("Diesel");
        e1.setHp("200");
        e1.setLength("50");
        e1.setWeightTons("100");
        e1.setMoves(6);

        Engine e2 = emanager.newRS("SP", "2");
        e2.setModel("GP30-400");
        e2.setTypeName("Diesel");
        e2.setHp("400");
        e2.setLength("50");
        e2.setWeightTons("110");
        e2.setMoves(5);

        Engine e3 = emanager.newRS("SP", "3");
        e3.setModel("GP40-800");
        e3.setTypeName("Diesel");
        e3.setHp("800");
        e3.setLength("50");
        e3.setWeightTons("120");
        e3.setMoves(10);

        Engine e4 = emanager.newRS("UP", "4");
        e4.setModel("GP40-1600");
        e4.setTypeName("Diesel");
        e4.setHp("1600");
        e4.setLength("50");
        e4.setWeightTons("130");
        e4.setMoves(15);

        // place the next 4 engines later in the route at Boston
        Engine e5 = emanager.newRS("SP", "5");
        e5.setModel("GP30-400");
        e5.setMoves(10);

        Engine e6 = emanager.newRS("UP", "6");
        e6.setModel("GP30-400");
        e6.setMoves(5);

        Engine e7 = emanager.newRS("UP", "7");
        e7.setModel("GP30-400");
        e7.setMoves(2);

        Engine e8 = emanager.newRS("SP", "8");
        e8.setModel("GP30-400");
        e8.setMoves(1);

        // place the next 4 engines at Danvers
        Engine e9 = emanager.newRS("UP", "9");
        e9.setModel("GP30-200");
        e9.setMoves(2);

        Engine e10 = emanager.newRS("SP", "10");
        e10.setModel("GP30-200");
        e10.setMoves(5);

        Engine e11 = emanager.newRS("SP", "11");
        e11.setModel("GP30-200");
        e11.setMoves(10);

        Engine e12 = emanager.newRS("UP", "12");
        e12.setModel("GP30-200");
        e12.setMoves(15);

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(acton, actonYard1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(acton, actonYard1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(acton, actonYard1));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(acton, actonYard1));

        Assert.assertEquals("Place e5", Track.OKAY, e5.setLocation(boston, bostonYard1));
        Assert.assertEquals("Place e6", Track.OKAY, e6.setLocation(boston, bostonYard1));
        Assert.assertEquals("Place e7", Track.OKAY, e7.setLocation(boston, bostonYard1));
        Assert.assertEquals("Place e8", Track.OKAY, e8.setLocation(boston, bostonYard1));

        Assert.assertEquals("Place e9", Track.OKAY, e9.setLocation(danvers, danversYard1));
        Assert.assertEquals("Place e10", Track.OKAY, e10.setLocation(danvers, danversYard1));
        Assert.assertEquals("Place e11", Track.OKAY, e11.setLocation(danvers, danversYard1));
        Assert.assertEquals("Place e12", Track.OKAY, e12.setLocation(danvers, danversYard1));

        // add grade to route
        RouteLocation rlBoston = route.getRouteLocationBySequenceNumber(2);
        rlBoston.setGrade(1.0);

        // Create train
        Train train1 = tmanager.newTrain("TestAutoHpt");
        train1.setRoute(route);

        // use auto HPT
        train1.setBuildConsistEnabled(true);
        train1.setNumberEngines(Train.AUTO_HPT);

        train1.setSecondLegOptions(Train.CHANGE_ENGINES);
        train1.setSecondLegNumberEngines("1");
        train1.setSecondLegStartLocation(rlBoston);

        // 3rd engine change at Danvers
        RouteLocation rlDanvers = route.getRouteLocationBySequenceNumber(4);
        train1.setThirdLegOptions(Train.CHANGE_ENGINES);
        train1.setThirdLegNumberEngines("1");
        train1.setThirdLegStartLocation(rlDanvers);

        // increase the train's departure weight
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("UP", "1", "Boxcar", "40", actonYard1, 0);
        c1.setWeightTons("200"); // 200 tons loaded
        c1.setLoadName(cld.getDefaultLoadName());

        // increase the train's weight departing Boston
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("UP", "2", "Boxcar", "40", bostonYard1, 0);
        c2.setWeightTons("200"); // 200 tons loaded
        c2.setLoadName(cld.getDefaultLoadName());

        new TrainBuilder().build(train1);
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 not assigned to train", null, e1.getDestination());
        Assert.assertEquals("e2 assigned to train", boston, e2.getDestination());
        Assert.assertEquals("e3 not assigned to train", null, e3.getDestination());
        Assert.assertEquals("e4 not assigned to train", null, e4.getDestination());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e5 assigned to train", danvers, e5.getDestination());
        Assert.assertEquals("e6 assigned to train", danvers, e6.getDestination());
        Assert.assertEquals("e7 assigned to train", danvers, e7.getDestination());
        Assert.assertEquals("e8 assigned to train", danvers, e8.getDestination());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e9 assigned to train", essex, e9.getDestination());
        Assert.assertEquals("e10 assigned to train", essex, e10.getDestination());
        Assert.assertEquals("e11 not assigned to train", null, e11.getDestination());
        Assert.assertEquals("e12 not assigned to train", null, e12.getDestination());
    }

    @Test
    public void testAutoHptWithhelpers() {

        // create 5 locations with tracks
        Route route = JUnitOperationsUtil.createFiveLocationRoute();
        Location acton = route.getDepartsRouteLocation().getLocation();
        Track actonYard1 = acton.getTrackByName("Acton Yard 1", Track.YARD);

        Location boston = lmanager.getLocationByName("Boston");
        Track bostonYard1 = boston.getTrackByName("Boston Yard 1", Track.YARD);

        Location essex = route.getTerminatesRouteLocation().getLocation();

        // create 4 new engine models with different HP ratings
        Engine e1 = emanager.newRS("UP", "1");
        e1.setModel("GP30-200");
        e1.setTypeName("Diesel");
        e1.setHp("200");
        e1.setLength("50");
        e1.setWeightTons("100");
        e1.setMoves(20);

        Engine e2 = emanager.newRS("SP", "2");
        e2.setModel("GP30-400");
        e2.setTypeName("Diesel");
        e2.setHp("400");
        e2.setLength("50");
        e2.setWeightTons("110");
        e2.setMoves(15);

        Engine e3 = emanager.newRS("SP", "3");
        e3.setModel("GP40-800");
        e3.setTypeName("Diesel");
        e3.setHp("800");
        e3.setLength("50");
        e3.setWeightTons("120");
        e3.setMoves(10);

        Engine e4 = emanager.newRS("UP", "10");
        e4.setModel("GP40-1600");
        e4.setTypeName("Diesel");
        e4.setHp("1600");
        e4.setLength("50");
        e4.setWeightTons("130");
        e4.setMoves(5);

        // Place engines
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(acton, actonYard1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(acton, actonYard1));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(acton, actonYard1));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(acton, actonYard1));

        // add grade to route
        RouteLocation rlBoston = route.getRouteLocationBySequenceNumber(2);
        rlBoston.setGrade(1.0);

        // Create train
        Train train1 = tmanager.newTrain("TestAutoHptWithHelpers");
        train1.setRoute(route);
        train1.setSendCarsToTerminalEnabled(true); // send all car pulls to terminal

        // use auto HPT
        train1.setBuildConsistEnabled(true);
        train1.setNumberEngines(Train.AUTO_HPT);

        RouteLocation rlChelmsford = route.getRouteLocationBySequenceNumber(3);

        // add helpers at Boston remove at Chelmsford
        train1.setSecondLegOptions(Train.HELPER_ENGINES);
        train1.setSecondLegStartLocation(rlBoston);
        train1.setSecondLegEndLocation(rlChelmsford);

        // weight of train is ignored when helpers are used
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 assigned to train, least HP", essex, e1.getDestination());
        Assert.assertEquals("e2 not assigned to train", null, e2.getDestination());
        Assert.assertEquals("e3 not assigned to train", null, e3.getDestination());
        Assert.assertEquals("e4 not assigned to train", null, e4.getDestination());

        // now increase the train's weight
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("UP", "1", "Boxcar", "40", actonYard1, 0);
        c1.setWeightTons("400"); // 400 tons loaded
        c1.setLoadName(cld.getDefaultLoadName());

        // increase test code coverage use 3rd leg options
        // add helpers at Boston remove at Chelmsford
        train1.setSecondLegOptions(Train.NO_CABOOSE_OR_FRED);
        train1.setThirdLegOptions(Train.HELPER_ENGINES);
        train1.setThirdLegStartLocation(rlBoston);
        train1.setThirdLegEndLocation(rlChelmsford);

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 not assigned to train", null, e1.getDestination());
        Assert.assertEquals("e2 not assigned to train", null, e2.getDestination());
        Assert.assertEquals("e3 assigned to train", essex, e3.getDestination());
        Assert.assertEquals("e4 not assigned to train", null, e4.getDestination());

        // now increase the train's weight
        Car c2 = JUnitOperationsUtil.createAndPlaceCar("UP", "2", "Boxcar", "40", bostonYard1, 0);
        c2.setWeightTons("200"); // 200 tons loaded
        c2.setLoadName(cld.getDefaultLoadName());

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train should build", true, train1.isBuilt());

        // confirm that the specified engines were assigned to the train
        Assert.assertEquals("e1 not assigned to train", null, e1.getDestination());
        Assert.assertEquals("e2 not assigned to train", null, e2.getDestination());
        Assert.assertEquals("e3 assigned to train", essex, e3.getDestination());
        Assert.assertEquals("e4 assigned to train", null, e4.getDestination());
    }

    @Test
    public void testAggressiveBuildOption() {

        // improve test coverage
        Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_DETAILED);

        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        String engineTypes[] = Bundle.getMessage("engineDefaultTypes").split(",");
        Setup.setBuildAggressive(true);
        Setup.setStagingTrackImmediatelyAvail(false);

        // register the car and engine types used
        et.addName(engineTypes[2]);

        // create 2 consists and a single engine for testing
        Consist con1 = emanager.newConsist("C1");

        Engine e1 = emanager.newRS("UP", "1");
        e1.setModel("GP30");
        e1.setOwner("AT");
        e1.setBuilt("1957");
        e1.setConsist(con1);
        e1.setMoves(5);
        Engine e2 = emanager.newRS("SP", "2");
        e2.setModel("GP30");
        e2.setOwner("AT");
        e2.setBuilt("1957");
        e2.setConsist(con1);
        e2.setMoves(5);

        // one engine
        Engine e3 = emanager.newRS("SP", "3");
        e3.setModel("GP40");
        e3.setOwner("DAB");
        e3.setBuilt("1957");

        Consist con2 = emanager.newConsist("C2");

        Engine e4 = emanager.newRS("UP", "10");
        e4.setModel("GP40");
        e4.setOwner("DAB");
        e4.setBuilt("1944");
        e4.setConsist(con2);
        e4.setMoves(20);
        Engine e5 = emanager.newRS("SP", "20");
        e5.setModel("GP40");
        e5.setOwner("DAB");
        e5.setBuilt("1944");
        e5.setConsist(con2);
        e5.setMoves(20);

        // 3 engine consist
        Consist con3 = emanager.newConsist("C3");

        Engine e6 = emanager.newRS("UP", "100");
        e6.setModel("GP40");
        e6.setOwner("DAB");
        e6.setBuilt("1944");
        e6.setConsist(con3);
        e6.setMoves(2);
        Engine e7 = emanager.newRS("SP", "200");
        e7.setModel("GP40");
        e7.setOwner("DAB");
        e7.setBuilt("1944");
        e7.setConsist(con3);
        e7.setMoves(2);
        Engine e8 = emanager.newRS("SP", "300");
        e8.setModel("GP40");
        e8.setOwner("DAB");
        e8.setBuilt("1944");
        e8.setConsist(con3);
        e8.setMoves(2);

        // Set up cars
        Car c1 = cmanager.newRS("PU", "13");
        c1.setTypeName(Bundle.getMessage("Caboose"));
        c1.setLength("32");
        c1.setMoves(10);
        c1.setOwner("AT");
        c1.setBuilt("1943");
        c1.setCaboose(true);

        Car c2 = cmanager.newRS("SP", "23");
        c2.setTypeName(carTypes[1]);
        c2.setLength("30");
        c2.setMoves(5);
        c2.setOwner("DAB");
        c2.setBuilt("1957");

        Car c3 = cmanager.newRS("UP", "33");
        c3.setTypeName(carTypes[1]);
        c3.setLength("33");
        c3.setMoves(0);
        c3.setOwner("DAB");
        c3.setBuilt("1944");

        Car c4 = cmanager.newRS("UP", "43");
        c4.setTypeName(carTypes[1]);
        c4.setLength("40");
        c4.setMoves(16);
        c4.setOwner("DAB");
        c4.setBuilt("1958");

        Car c5 = cmanager.newRS("SP", "53");
        c5.setTypeName(carTypes[1]);
        c5.setLength("40");
        c5.setMoves(8);
        c5.setOwner("DAB");
        c5.setBuilt("1958");

        Car c6 = cmanager.newRS("NH", "63");
        c6.setTypeName(carTypes[1]);
        c6.setLength("40");
        c6.setMoves(2);
        c6.setOwner("DAB");
        c6.setBuilt("1958");

        Car c7 = cmanager.newRS("UP", "73");
        c7.setTypeName(carTypes[5]);
        c7.setLength("40");
        c7.setMoves(5);
        c7.setOwner("DAB");
        c7.setBuilt("1958");

        Car c8 = cmanager.newRS("SP", "83");
        c8.setTypeName(carTypes[1]);
        c8.setLength("40");
        c8.setMoves(4);
        c8.setOwner("DAB");
        c8.setBuilt("1958");

        Car c9 = cmanager.newRS("NH", "93");
        c9.setTypeName(carTypes[1]);
        c9.setLength("40");
        c9.setMoves(3);
        c9.setOwner("DAB");
        c9.setBuilt("1944");

        Car c10 = cmanager.newRS("NH", "103");
        c10.setTypeName(carTypes[1]);
        c10.setLength("40");
        c10.setMoves(10);
        c10.setOwner("DAB");
        c10.setBuilt("1958");

        Car c11 = cmanager.newRS("SP", "113");
        c11.setTypeName(carTypes[1]);
        c11.setLength("40");
        c11.setMoves(3);
        c11.setOwner("DAB");
        c11.setBuilt("1958");

        // Create 5 locations
        Location loc1 = lmanager.newLocation("Harvard");

        Track loc1trk1 = loc1.addTrack("Harvard Yard 1", Track.YARD);
        loc1trk1.setLength(1000);

        Track loc1trk2 = loc1.addTrack("Harvard Yard 2", Track.YARD);
        loc1trk2.setLength(1000);

        Location loc2 = lmanager.newLocation("Acton");
        Track loc2trk1 = loc2.addTrack("Acton Siding", Track.SPUR);
        loc2trk1.setLength(50);

        Location loc3 = lmanager.newLocation("Boston");

        Track loc3trk1 = loc3.addTrack("Boston Yard 1", Track.YARD);
        loc3trk1.setLength(50);

        Track loc3trk2 = loc3.addTrack("Boston Yard 2", Track.YARD);
        loc3trk2.setLength(50);

        Location loc4 = lmanager.newLocation("Chelmsford");

        Track loc4trk1 = loc4.addTrack("Chelmsford Yard 1", Track.YARD);
        loc4trk1.setLength(50);

        Track loc4trk2 = loc4.addTrack("Chelmsford Yard 2", Track.YARD);
        loc4trk2.setLength(50);

        Location loc5 = lmanager.newLocation("Westford");

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
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 After Build 1", true, train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 1", "Westford Yard 2", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 1", "Westford Yard 1", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 1", "Westford Yard 2", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 1", "Boston Yard 1", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 1", "Acton Siding", c5.getDestinationTrackName());
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
        Assert.assertFalse(new TrainBuilder().build(train1));
        Assert.assertFalse("Train 1 After Build from staging, eight loco on departure track", train1
                .isBuilt());

        // move locos to other departure track
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place e3", Track.OKAY, e3.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place e4", Track.OKAY, e4.setLocation(loc1, loc1trk2));
        Assert.assertEquals("Place e5", Track.OKAY, e5.setLocation(loc1, loc1trk2));

        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertTrue("Train 1 After Build from staging, three loco on departure track", train1.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 2", "Westford Yard 2", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 2", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 2", "Westford Yard 2", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 2", "Boston Yard 2", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 2", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 2", "Acton Siding", c6.getDestinationTrackName());
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
        Assert.assertFalse("Train departs Harvard already there", train1.move("Harvard"));
        Assert.assertTrue("Next location in train's route is Acton", train1.move("Acton"));
        Assert.assertFalse("Train is at Acton", train1.move("Acton"));
        // next location is Boston, skip it and go directly to Chelmsford
        Assert.assertTrue("Chelmsford is in train's route", train1.move("Chelmsford"));
        Assert.assertTrue("Next location in train's route is Westford", train1.move("Westford"));
        Assert.assertFalse("Train is at Westford last location in train's route", train1
                .move("Westford"));

        train1.move(); // terminate train

        // now try with a train returning to staging, test alternate track feature
        // Create train
        Train train2 = tmanager.newTrain("Westford to Harvard Aggressive");
        Route rte2 = rmanager.copyRoute(rte1, "Route 4 Harvard", true);
        train2.setRoute(rte2);
        train2.setRequirements(Train.CABOOSE);
        train2.setNumberEngines("3");

        // add 2 yard tracks to siding at Acton
        Track loc2trk2 = loc2.addTrack("Acton Yard 1", Track.YARD);
        loc2trk2.setLength(50); // only enough room for one car
        Track loc2trk3 = loc2.addTrack("Acton Alternate Track", Track.SPUR);
        loc2trk3.setLength(100); // only enough room for two cars

        // set the alternate for Acton siding
        loc2trk1.setAlternateTrack(loc2trk3);

        // send cars to Acton siding
        c3.setFinalDestination(loc2);
        c3.setFinalDestinationTrack(loc2trk1);
        c8.setFinalDestination(loc2);
        c8.setFinalDestinationTrack(loc2trk1);
        c9.setFinalDestination(loc2);
        c9.setFinalDestinationTrack(loc2trk1);
        c11.setFinalDestination(loc2);
        c11.setFinalDestinationTrack(loc2trk1);

        train2.reset();
        Assert.assertTrue(new TrainBuilder().build(train2));
        Assert.assertTrue("Train 2 returns to staging", train2.isBuilt());

        // check destinations
        Assert.assertEquals("c1 destination 3", "Harvard Yard 1", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 3", "Acton Alternate Track", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 3", "Harvard Yard 1", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 3", "Harvard Yard 1", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 3", "Chelmsford Yard 2", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 3", "Acton Yard 1", c8.getDestinationTrackName());

        Assert.assertEquals("c9 destination 3", "Acton Alternate Track", c9.getDestinationTrackName());
        Assert.assertEquals("c10 destination 3", "Boston Yard 1", c10.getDestinationTrackName());
        Assert.assertEquals("c11 destination 3", "Acton Siding", c11.getDestinationTrackName());

        Assert.assertEquals("e1 destination 3", "", e1.getDestinationTrackName());
        Assert.assertEquals("e2 destination 3", "", e2.getDestinationTrackName());
        Assert.assertEquals("e3 destination 3", "", e3.getDestinationTrackName());
        Assert.assertEquals("e4 destination 3", "", e4.getDestinationTrackName());
        Assert.assertEquals("e5 destination 3", "", e5.getDestinationTrackName());
        Assert.assertEquals("e6 destination 3", "Harvard Yard 1", e6.getDestinationTrackName());
        Assert.assertEquals("e7 destination 3", "Harvard Yard 1", e7.getDestinationTrackName());
        Assert.assertEquals("e8 destination 3", "Harvard Yard 1", e8.getDestinationTrackName());

        // check that cars on alternate track are sent to Acton Siding
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
        Assert.assertFalse("Harvard is the last location in this trains route", train2
                .moveToNextLocation("Harvard"));
        Assert.assertFalse("Boston is the 3rd to last location in this trains route", train2
                .moveToNextLocation("Boston"));
        Assert.assertFalse("Westford is the current location in this trains route", train2
                .moveToNextLocation("Westford"));
        Assert.assertFalse("Acton is the 2nd to last location in this trains route", train2
                .moveToNextLocation("Acton"));
        Assert.assertTrue("Chelmsford is the next location in this trains route", train2
                .moveToNextLocation("Chelmsford"));
        Assert.assertTrue("Boston is the next location in this trains route", train2
                .moveToNextLocation("Boston"));
        Assert.assertTrue("Acton is the next location in this trains route", train2
                .moveToNextLocation("Acton"));
        Assert.assertTrue("Harvard is the next location in this trains route", train2
                .moveToNextLocation("Harvard"));
        Assert.assertFalse("Train is at Harvard", train2.moveToNextLocation("Harvard"));

        train2.move(); // terminate train

        // now test train returning to staging
        rte1.addLocation(loc1);
        train1.reset();
        Assert.assertFalse(new TrainBuilder().build(train1));
        // should fail, can't return to staging track
        Assert.assertEquals("Train 1 departing and returning to staging", false, train1.isBuilt());
        // change mode
        Setup.setStagingTrackImmediatelyAvail(true);
        train1.reset();
        Assert.assertTrue(new TrainBuilder().build(train1));
        Assert.assertEquals("Train 1 departing and returning to staging", true, train1.isBuilt());
        Assert.assertEquals("check departure track name", "Harvard Yard 1", train1.getDepartureTrack().getName());
        Assert.assertEquals("check departure and arrival track", train1.getDepartureTrack(),
                train1.getTerminationTrack());

        // check destinations
        Assert.assertEquals("c1 destination 3", "Harvard Yard 1", c1.getDestinationTrackName());
        Assert.assertEquals("c2 destination 3", "", c2.getDestinationTrackName());
        Assert.assertEquals("c3 destination 3", "", c3.getDestinationTrackName());
        Assert.assertEquals("c4 destination 3", "Boston Yard 2", c4.getDestinationTrackName());

        Assert.assertEquals("c5 destination 3", "", c5.getDestinationTrackName());
        Assert.assertEquals("c6 destination 3", "Westford Yard 1", c6.getDestinationTrackName());
        Assert.assertEquals("c7 destination 3", "Harvard Yard 1", c7.getDestinationTrackName());
        Assert.assertEquals("c8 destination 3", "Acton Siding", c8.getDestinationTrackName());

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
        Assert.assertEquals("c3 location", "Acton Alternate Track", c3.getTrackName());
        Assert.assertEquals("c4 location", "Boston Yard 2", c4.getTrackName());

        Assert.assertEquals("c5 location", "Harvard Yard 2", c5.getTrackName());
        Assert.assertEquals("c6 location", "Westford Yard 1", c6.getTrackName());
        Assert.assertEquals("c7 location", "Harvard Yard 1", c7.getTrackName());
        Assert.assertEquals("c8 location", "Acton Siding", c8.getTrackName());

        Assert.assertEquals("c9 location", "Acton Alternate Track", c9.getTrackName());
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
        Car a = cmanager.newRS("ABC", "123");
        a.setTypeName(carTypes[1]);
        a.setLength("50");
        a.setLastDate(date);
        a.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car b = cmanager.newRS("ABC", "321");
        b.setTypeName(carTypes[1]);
        b.setLength("50");
        b.setLastDate(date);
        b.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car c = cmanager.newRS("ABC", "111");
        c.setTypeName(carTypes[1]);
        c.setLength("50");
        c.setLastDate(date);
        c.setLocation(A, interchangeTrack);

        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method getCarOrderMethod = null;
        try {
            getCarOrderMethod = tb.getClass().getDeclaredMethod("getCarServiceOrder", Car.class);
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
        Car a = cmanager.newRS("ABC", "123");
        a.setTypeName(carTypes[1]);
        a.setLength("50");
        a.setLastDate(date);
        a.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car b = cmanager.newRS("ABC", "321");
        b.setTypeName(carTypes[1]);
        b.setLength("50");
        b.setLastDate(date);
        b.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car c = cmanager.newRS("ABC", "111");
        c.setTypeName(carTypes[1]);
        c.setLength("50");
        c.setLastDate(date);
        c.setLocation(A, interchangeTrack);

        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method getCarOrderMethod = null;
        try {
            getCarOrderMethod = tb.getClass().getDeclaredMethod("getCarServiceOrder", Car.class);
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
        Car a = cmanager.newRS("ABC", "123");
        a.setTypeName(carTypes[1]);
        a.setLength("50");
        a.setLastDate(date);
        a.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car b = cmanager.newRS("ABC", "321");
        b.setTypeName(carTypes[1]);
        b.setLength("50");
        b.setLastDate(date);
        b.setLocation(A, interchangeTrack);

        cal.add(java.util.Calendar.MINUTE, 2);
        date = cal.getTime();

        Car c = cmanager.newRS("ABC", "111");
        c.setTypeName(carTypes[1]);
        c.setLength("50");
        c.setLastDate(date);
        c.setLocation(A, interchangeTrack);

        // NOTE: this test uses reflection to test a private method.
        java.lang.reflect.Method getCarOrderMethod = null;
        try {
            getCarOrderMethod = tb.getClass().getDeclaredMethod("getCarServiceOrder", Car.class);
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

    /**
     * Confirms that car with custom load in staging get routed to the correct
     * spur that has a schedule demanding the car's type and load.
     */
    @Test
    public void testFindFinalDestinationForCarLoadA() {

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        // newLocation(name) will return a location if it already exists
        Location westend = lmanager.newLocation("Westend");
        Location midtown = lmanager.newLocation("Midtown");
        Location eastend = lmanager.newLocation("Eastend");

        Track westendSpur1 = westend.getTrackByName("Westend spur 1", null);
        Track westendInterchange1 = westend.getTrackByName("Westend interchange 1", null);
        Track midtownSpur1 = midtown.getTrackByName("Midtown spur 1", null); // has schedule
        Track midtownSpur2 = midtown.getTrackByName("Midtown spur 2", null); // has schedule
        Track midtownYard = midtown.getTrackByName("Midtown yard", null);
        Track eastendSpur1 = eastend.getTrackByName("Eastend spur 1", null); // has schedule

        // confirm that all tracks exist
        Assert.assertNotNull(westendSpur1);
        Assert.assertNotNull(westendInterchange1);
        Assert.assertNotNull(midtownSpur1);
        Assert.assertNotNull(midtownSpur2);
        Assert.assertNotNull(midtownYard);
        Assert.assertNotNull(eastendSpur1);

        Car c1 = cmanager.getByRoadAndNumber("AA", "1"); // Boxcar has custom load "Bags"

        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car destination is Midtown spur 1", midtownSpur1, c1.getDestinationTrack());

        // confirm track move counts are correct
        Assert.assertEquals("no schedule for this spur", 0, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 21, midtownSpur1.getMoves());
        Assert.assertEquals("Midtown spur 2", 40, midtownSpur2.getMoves());
        Assert.assertEquals("Eastend spur 1", 60, eastendSpur1.getMoves());
    }

    /**
     * Test car on interchange track with destination restrictions.
     */
    @Test
    public void testFindFinalDestinationForCarLoadB() {

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        // newLocation(name) will return a location if it already exists
        Location westend = lmanager.newLocation("Westend");
        Location midtown = lmanager.newLocation("Midtown");
        Location eastend = lmanager.newLocation("Eastend");

        Track westendSpur1 = westend.getTrackByName("Westend spur 1", null);
        Track westendInterchange1 = westend.getTrackByName("Westend interchange 1", null);
        Track midtownSpur1 = midtown.getTrackByName("Midtown spur 1", null); // has schedule
        Track midtownSpur2 = midtown.getTrackByName("Midtown spur 2", null); // has schedule
        Track midtownYard = midtown.getTrackByName("Midtown yard", null);
        Track eastendSpur1 = eastend.getTrackByName("Eastend spur 1", null); // has schedule

        // confirm that all tracks exist
        Assert.assertNotNull(westendSpur1);
        Assert.assertNotNull(westendInterchange1);
        Assert.assertNotNull(midtownSpur1);
        Assert.assertNotNull(midtownSpur2);
        Assert.assertNotNull(midtownYard);
        Assert.assertNotNull(eastendSpur1);

        // car is on the Westend interchange track
        Car c1 = cmanager.getByRoadAndNumber("AA", "1"); // Boxcar has custom load "Bags"

        // now test the interchange destination restriction feature

        // don't allow the car to go to Midtown
        westendInterchange1.setDestinationOption(Track.INCLUDE_DESTINATIONS);
        westendInterchange1.addDestination(eastend); // only Eastend if valid

        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car destination is Eastend spur 1", eastendSpur1, c1.getDestinationTrack());

        // confirm track move counts are correct
        Assert.assertEquals("no schedule for this spur", 0, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 20, midtownSpur1.getMoves());
        Assert.assertEquals("Midtown spur 2", 40, midtownSpur2.getMoves());
        Assert.assertEquals("Eastend spur 1", 61, eastendSpur1.getMoves());
    }

    /**
     * Test destination restrictions, caboose and passenger cars should ignore
     * option to not allow through cars.
     */
    @Test
    public void testFindFinalDestinationForCarLoadC() {

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        // newLocation(name) will return a location if it already exists
        Location westend = lmanager.newLocation("Westend");
        Location midtown = lmanager.newLocation("Midtown");
        Location eastend = lmanager.newLocation("Eastend");

        Track westendSpur1 = westend.getTrackByName("Westend spur 1", null);
        Track westendInterchange1 = westend.getTrackByName("Westend interchange 1", null);
        Track midtownSpur1 = midtown.getTrackByName("Midtown spur 1", null); // has schedule
        Track midtownSpur2 = midtown.getTrackByName("Midtown spur 2", null); // has schedule
        Track midtownYard = midtown.getTrackByName("Midtown yard", null);
        Track eastendSpur1 = eastend.getTrackByName("Eastend spur 1", null); // has schedule

        // confirm that all tracks exist
        Assert.assertNotNull(westendSpur1);
        Assert.assertNotNull(westendInterchange1);
        Assert.assertNotNull(midtownSpur1);
        Assert.assertNotNull(midtownSpur2);
        Assert.assertNotNull(midtownYard);
        Assert.assertNotNull(eastendSpur1);

        // car is on the Westend interchange track
        Car c1 = cmanager.getByRoadAndNumber("AA", "1"); // Boxcar has custom load "Bags"

        // don't allow the car to go to Midtown
        westendInterchange1.setDestinationOption(Track.INCLUDE_DESTINATIONS);
        westendInterchange1.addDestination(eastend); // only Eastend
        // and don't allow through cars
        train.setAllowThroughCarsEnabled(false);

        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car was not added to train
        Assert.assertNull("car 1 not part of train", c1.getTrain());

        // confirm track move counts are correct
        Assert.assertEquals("no schedule for this spur", 0, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 20, midtownSpur1.getMoves());
        Assert.assertEquals("Midtown spur 2", 40, midtownSpur2.getMoves());
        Assert.assertEquals("Eastend spur 1", 60, eastendSpur1.getMoves());

        // make the car a passenger car, ignores train through car restrictions
        c1.setPassenger(true); // a passenger car with custom load of Bags

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car destination", eastendSpur1, c1.getDestinationTrack());

        // Caboose or car with FRED are exceptions to the through car restriction
        c1.setPassenger(false);
        c1.setFred(true);
        train.setRequirements(Train.FRED);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct, car with FRED or caboose can go directly to terminal
        Assert.assertEquals("car destination", eastendSpur1, c1.getDestinationTrack());
    }

    /**
     * Test car with custom load, local move, local move restrictions.
     */
    @Test
    public void testFindFinalDestinationForCarLoadD() {

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        // newLocation(name) will return a location if it already exists
        Location westend = lmanager.newLocation("Westend");
        Location midtown = lmanager.newLocation("Midtown");
        Location eastend = lmanager.newLocation("Eastend");

        Track westendSpur1 = westend.getTrackByName("Westend spur 1", null);
        Track westendInterchange1 = westend.getTrackByName("Westend interchange 1", null);
        Track midtownSpur1 = midtown.getTrackByName("Midtown spur 1", null);
        Track midtownSpur2 = midtown.getTrackByName("Midtown spur 2", null);
        Track midtownYard = midtown.getTrackByName("Midtown yard", null);
        Track eastendSpur1 = eastend.getTrackByName("Eastend spur 1", null);

        // confirm that all tracks exist
        Assert.assertNotNull(westendSpur1);
        Assert.assertNotNull(westendInterchange1);
        Assert.assertNotNull(midtownSpur1);
        Assert.assertNotNull(midtownSpur2);
        Assert.assertNotNull(midtownYard);
        Assert.assertNotNull(eastendSpur1);

        // car c1 is on the Westend interchange track
        Car c1 = cmanager.getByRoadAndNumber("AA", "1");

        // try local move, local track has the least number of moves, so tried first by program
        Schedule sch1 = smanager.getScheduleByName("Schedule for car load");
        westendSpur1.setSchedule(sch1); // now all spurs have schedules

        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car destination is Westend spur 1", westendSpur1, c1.getDestinationTrack());

        // confirm track move counts are correct
        Assert.assertEquals("Westend spur 1", 1, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 20, midtownSpur1.getMoves());
        Assert.assertEquals("Midtown spur 2", 40, midtownSpur2.getMoves());
        Assert.assertEquals("Eastend spur 1", 60, eastendSpur1.getMoves());

        // don't allow local moves, next choice is Midtown
        train.reset();
        train.setAllowLocalMovesEnabled(false);

        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car destination is midtown spur 1", midtownSpur1, c1.getDestinationTrack());

        // confirm track move counts are correct
        Assert.assertEquals("Westend spur 1", 1, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 21, midtownSpur1.getMoves());
        Assert.assertEquals("Midtown spur 2", 40, midtownSpur2.getMoves());
        Assert.assertEquals("Eastend spur 1", 60, eastendSpur1.getMoves());
    }

    /**
     * Test that car sent to a spur with a schedule that doesn't match removes
     * the car's final destination and track.
     */
    @Test
    public void testFinalDestinationForCarLoadNoMatch() {

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        Location midtown = lmanager.getLocationByName("Midtown");
        Track midtownSpur1 = midtown.getTrackByName("Midtown spur 1", null); // has schedule

        Car c1 = cmanager.getByRoadAndNumber("AA", "1");
        c1.setFinalDestination(midtown);
        c1.setFinalDestinationTrack(midtownSpur1);
        c1.setLoadName("BAGS"); // no match with this load

        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destinations are correct
        Assert.assertEquals("car destination", null, c1.getDestinationTrack());
        Assert.assertEquals("car final destination", null, c1.getFinalDestination());
        Assert.assertEquals("car final destination track", null, c1.getFinalDestinationTrack());

        // should still have custom load
        Assert.assertEquals("car load name", "BAGS", c1.getLoadName());
    }

    /**
     * Test car with custom load. Uses alternate track for spur with schedule.
     */
    @Test
    public void testFindFinalDestinationForCarLoadAlternateTrack() {

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        // newLocation(name) will return a location if it already exists
        Location westend = lmanager.newLocation("Westend");
        Location midtown = lmanager.newLocation("Midtown");
        Location eastend = lmanager.newLocation("Eastend");

        Track westendSpur1 = westend.getTrackByName("Westend spur 1", null);
        Track westendInterchange1 = westend.getTrackByName("Westend interchange 1", null);
        Track midtownSpur1 = midtown.getTrackByName("Midtown spur 1", null);
        Track midtownSpur2 = midtown.getTrackByName("Midtown spur 2", null);
        Track midtownYard = midtown.getTrackByName("Midtown yard", null);
        Track eastendSpur1 = eastend.getTrackByName("Eastend spur 1", null);

        // confirm that all tracks exist
        Assert.assertNotNull(westendSpur1);
        Assert.assertNotNull(westendInterchange1);
        Assert.assertNotNull(midtownSpur1);
        Assert.assertNotNull(midtownSpur2);
        Assert.assertNotNull(midtownYard);
        Assert.assertNotNull(eastendSpur1);

        // car c1 is on the Westend interchange track
        Car c1 = cmanager.getByRoadAndNumber("AA", "1");
        Car c2 = cmanager.getByRoadAndNumber("AA", "2"); // Midtown

        // test alternate track
        midtownSpur1.setLength(80); // 40' boxcar needs 44 feet, c2 is already on this track

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car destination", midtownYard, c1.getDestinationTrack());

        // confirm track move counts are correct
        Assert.assertEquals("Westend spur 1", 0, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 21, midtownSpur1.getMoves()); // bumped 
        Assert.assertEquals("Midtown spur 2", 40, midtownSpur2.getMoves());
        Assert.assertEquals("Midtown yard", 1, midtownYard.getMoves());
        Assert.assertEquals("Eastend spur 1", 60, eastendSpur1.getMoves());

        // test alternate track, needs at least one train direction to be the same
        midtownSpur1.setTrainDirections(Track.NORTH);
        midtownYard.setTrainDirections(Track.SOUTH);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car destination is now spur 2", midtownSpur2, c1.getDestinationTrack());

        // confirm track move counts are correct
        Assert.assertEquals("Westend spur 1", 0, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 21, midtownSpur1.getMoves());
        Assert.assertEquals("Midtown spur 2", 41, midtownSpur2.getMoves()); // bumped
        Assert.assertEquals("Midtown yard", 1, midtownYard.getMoves());
        Assert.assertEquals("Eastend spur 1", 60, eastendSpur1.getMoves());

        // restore and test
        midtownYard.setTrainDirections(Track.NORTH);
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car destination is now the yard", midtownYard, c1.getDestinationTrack());

        // confirm track move counts are correct
        Assert.assertEquals("Westend spur 1", 0, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 22, midtownSpur1.getMoves());
        Assert.assertEquals("Midtown spur 2", 41, midtownSpur2.getMoves());
        Assert.assertEquals("Midtown yard", 2, midtownYard.getMoves());
        Assert.assertEquals("Eastend spur 1", 60, eastendSpur1.getMoves());

        // test alternate track, don't allow boxcar
        midtownYard.deleteTypeName("Boxcar");

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car destination is now spur 2", midtownSpur2, c1.getDestinationTrack());

        // confirm track move counts are correct
        Assert.assertEquals("Westend spur 1", 0, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 22, midtownSpur1.getMoves());
        Assert.assertEquals("Midtown spur 2", 42, midtownSpur2.getMoves());
        Assert.assertEquals("Midtown yard", 2, midtownYard.getMoves());
        Assert.assertEquals("Eastend spur 1", 60, eastendSpur1.getMoves());

        // remove alternate
        midtownSpur1.setAlternateTrack(null);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car destination is now spur 2", midtownSpur2, c1.getDestinationTrack());

        // confirm track move counts are correct
        Assert.assertEquals("Westend spur 1", 0, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 22, midtownSpur1.getMoves());
        Assert.assertEquals("Midtown spur 2", 43, midtownSpur2.getMoves());
        Assert.assertEquals("Midtown yard", 2, midtownYard.getMoves());
        Assert.assertEquals("Eastend spur 1", 60, eastendSpur1.getMoves());

        // test track space available
        c2.setFinalDestination(midtown);
        c2.setFinalDestinationTrack(midtownSpur2);
        c2.setLoadName("Nuts");

        midtownSpur2.setLength(80); // too short for two cars

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car destination is eastend", eastendSpur1, c1.getDestinationTrack());

        // confirm track move counts are correct
        Assert.assertEquals("Westend spur 1", 0, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 22, midtownSpur1.getMoves());
        Assert.assertEquals("Midtown spur 2", 45, midtownSpur2.getMoves());
        Assert.assertEquals("Midtown yard", 2, midtownYard.getMoves());
        Assert.assertEquals("Eastend spur 1", 61, eastendSpur1.getMoves());
    }

    /**
     * Test car with custom load to staging.
     */
    @Test
    public void testFindFinalDestinationForCarLoadToStaging() {

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        // newLocation(name) will return a location if it already exists
        Location westend = lmanager.newLocation("Westend");
        Location midtown = lmanager.newLocation("Midtown");
        Location eastend = lmanager.newLocation("Eastend");

        Track westendSpur1 = westend.getTrackByName("Westend spur 1", null);
        Track westendInterchange1 = westend.getTrackByName("Westend interchange 1", null);
        Track midtownSpur1 = midtown.getTrackByName("Midtown spur 1", null);
        Track midtownSpur2 = midtown.getTrackByName("Midtown spur 2", null);
        Track midtownYard = midtown.getTrackByName("Midtown yard", null);
        Track eastendSpur1 = eastend.getTrackByName("Eastend spur 1", null);

        // confirm that all tracks exist
        Assert.assertNotNull(westendSpur1);
        Assert.assertNotNull(westendInterchange1);
        Assert.assertNotNull(midtownSpur1);
        Assert.assertNotNull(midtownSpur2);
        Assert.assertNotNull(midtownYard);
        Assert.assertNotNull(eastendSpur1);

        // car is on the Westend interchange track
        Car c1 = cmanager.getByRoadAndNumber("AA", "1");

        // now test car to staging, create four staging tracks since they are randomly selected
        Location staging = lmanager.newLocation("Staging");

        Track stagingTrack1 = staging.addTrack("Staging Track 1", Track.STAGING);
        stagingTrack1.setLength(500);

        Track stagingTrack2 = staging.addTrack("Staging Track 2", Track.STAGING);
        stagingTrack2.setLength(500);
        stagingTrack2.deleteTypeName("Boxcar");

        Track stagingTrack3 = staging.addTrack("Staging Track 3", Track.STAGING);
        stagingTrack3.setLength(500);
        stagingTrack3.deleteTypeName("Boxcar");

        Track stagingTrack4 = staging.addTrack("Staging Track 4", Track.STAGING);
        stagingTrack4.setLength(500);
        stagingTrack4.deleteTypeName("Boxcar");

        // get rid of other options for this car
        eastendSpur1.setLength(40); // kill this track. too short for car
        midtownSpur1.setLength(80); // kill this track. too short two cars, c2 on this track
        midtownSpur2.setLength(40); // kill this track. too short for car
        midtownSpur1.setAlternateTrack(null);

        // no spurs for c1, so option is to just move car
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("Midtown is the only available track", midtownYard, c1.getDestinationTrack());
        Assert.assertEquals("car's final destination", null, c1.getFinalDestination());

        // confirm track move counts are correct
        Assert.assertEquals("Westend spur 1", 0, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 20, midtownSpur1.getMoves());
        Assert.assertEquals("Midtown spur 2", 40, midtownSpur2.getMoves());
        Assert.assertEquals("Midtown yard", 1, midtownYard.getMoves());
        Assert.assertEquals("Eastend spur 1", 60, eastendSpur1.getMoves());

        // create a second train with a route to staging
        Train train2 = tmanager.newTrain("Train to Staging");

        Route route2 = rmanager.newRoute("Route to Staging");
        train2.setRoute(route2);

        // now add staging to train 2 route
        route2.addLocation(midtown);
        route2.addLocation(staging);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car's next destination is midtown", midtownYard, c1.getDestinationTrack());
        Assert.assertEquals("car's final destination is staging", staging, c1.getFinalDestination());

        // confirm track move counts are correct
        Assert.assertEquals("Westend spur 1", 0, westendSpur1.getMoves());
        Assert.assertEquals("Midtown spur 1", 20, midtownSpur1.getMoves());
        Assert.assertEquals("Midtown spur 2", 40, midtownSpur2.getMoves());
        Assert.assertEquals("Midtown yard", 2, midtownYard.getMoves());
        Assert.assertEquals("Eastend spur 1", 60, eastendSpur1.getMoves());

        // don't allow staging as a destination
        westendInterchange1.setDestinationOption(Track.INCLUDE_DESTINATIONS);
        westendInterchange1.addDestination(midtown);
        westendInterchange1.addDestination(eastend);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("Midtown is the only available track", midtownYard, c1.getDestinationTrack());
        Assert.assertEquals("car's final destination", null, c1.getFinalDestination());

        // restore and check again
        westendInterchange1.setDestinationOption(Track.ALL_DESTINATIONS);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car's next destination is midtown", midtownYard, c1.getDestinationTrack());
        Assert.assertEquals("car's final destination is staging", staging, c1.getFinalDestination());

        // don't allow train to send custom car loads to staging
        train.setSendCarsWithCustomLoadsToStagingEnabled(false);

        // we need at least one spur that is reachable
        Car c2 = cmanager.getByRoadAndNumber("AA", "2");
        c2.setFinalDestination(midtown);
        c2.setFinalDestinationTrack(midtownSpur2);
        c2.setLoadName("Nuts");

        midtownSpur2.setLength(80); // there's only room for one car

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("car is held", null, c1.getDestinationTrack());
        Assert.assertEquals("car's final destination not staging", null, c1.getFinalDestination());

        // now allow train to send custom car loads to staging
        train.setSendCarsWithCustomLoadsToStagingEnabled(true);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("Midtown is the only available track", midtownYard, c1.getDestinationTrack());
        Assert.assertEquals("car's final destination is staging", staging, c1.getFinalDestination());

        // now test staging at end of train's route
        tmanager.deregister(train2);

        train.getRoute().addLocation(staging);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("Car to staging", stagingTrack1, c1.getDestinationTrack());
        Assert.assertEquals("car's final destination", null, c1.getFinalDestination());

        // test staging car load control
        stagingTrack1.setLoadOption(Track.EXCLUDE_LOADS);
        stagingTrack1.addLoadName("Bags");

        train.reset();
        // need to change how staging tracks are selected or build will fail
        Setup.setTrainIntoStagingCheckEnabled(false);
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm that car destination is correct
        Assert.assertEquals("no staging tracks available", null, c1.getDestinationTrack());
        Assert.assertEquals("car's final destination", null, c1.getFinalDestination());
    }

    /**
     * Test car custom load generation from staging to spurs with schedules
     * demanding car type and load.
     */
    @Test
    public void testCustomCarLoadFromStagingA() {

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        Location westend = lmanager.newLocation("Westend");
        Location midtown = lmanager.newLocation("Midtown");
        Location eastend = lmanager.newLocation("Eastend");

        Track westendSpur1 = westend.getTrackByName("Westend spur 1", null);
        Track midtownSpur1 = midtown.getTrackByName("Midtown spur 1", null);
        Track midtownSpur2 = midtown.getTrackByName("Midtown spur 2", null);
        Track eastendSpur1 = eastend.getTrackByName("Eastend spur 1", null);

        // test car from staging, create one staging track
        Location staging = lmanager.newLocation("Staging");
        Track stagingTrack1 = staging.addTrack("Staging Track 1", Track.STAGING);
        stagingTrack1.setLength(500);
        stagingTrack1.setAddCustomLoadsEnabled(true);

        // add staging to the start of the route
        Route route = train.getRoute();
        route.addLocation(staging, 1);

        // change train name
        train.setName("Train Staging-Westend-Midtown-Eastend");

        // confirm staging at start of route
        Assert.assertEquals("1st location in route", staging, train.getTrainDepartsRouteLocation().getLocation());

        // place cars in staging
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("AA", "3", "Boxcar", "50", stagingTrack1, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("AA", "4", "Boxcar", "50", stagingTrack1, 1);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm car has custom load and destination
        Assert.assertEquals("car load", "Flour", c3.getLoadName());
        Assert.assertEquals("car destination track", midtownSpur1, c3.getDestinationTrack());
        Assert.assertEquals("car load", "Nuts", c4.getLoadName());
        Assert.assertEquals("car destination track", midtownSpur1, c4.getDestinationTrack());

        // make midtownSpur1 too short for c3, capacity issue rather than track length
        midtownSpur1.setLength(50); // there's a 40' car already on Midtown spur 1

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm car has custom load and destination
        Assert.assertEquals("car load", "Flour", c3.getLoadName());
        Assert.assertEquals("car destination track", midtownSpur2, c3.getDestinationTrack());
        Assert.assertEquals("car load", "Nuts", c4.getLoadName());
        Assert.assertEquals("car destination track", midtownSpur2, c4.getDestinationTrack());

        // now don't allow staging to service Midtowm
        stagingTrack1.setDestinationOption(Track.INCLUDE_DESTINATIONS);
        stagingTrack1.addDestination(westend);
        stagingTrack1.addDestination(eastend);

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm car has custom load and destination
        Assert.assertEquals("car load", "Flour", c3.getLoadName());
        Assert.assertEquals("car destination track", eastendSpur1, c3.getDestinationTrack());

        // Now configure Eastend spur to not allow any cars from staging
        eastendSpur1.setReservationFactor(0);

        // no spurs with schedule available, c3 send out of staging with "E" load
        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm car has custom load and destination
        Assert.assertEquals("car load", "E", c3.getLoadName());
        Assert.assertEquals("car destination track", westendSpur1, c3.getDestinationTrack());
    }

    /**
     * Test car custom load generation from staging. Not able to route car to
     * spur is tested. Tests that a "Flat" is rejected by car type when looking
     * for a spur with a schedule.
     * 
     */
    @Test
    public void testCustomCarLoadFromStagingB() {

        // register the car loads used
        cld.addName("Flat", "Bricks");
        cld.addName("Flat", "Steel");

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        Location eastend = lmanager.newLocation("Eastend");
        Track eastendSpur1 = eastend.getTrackByName("Eastend spur 1", null);

        Location westend = lmanager.newLocation("Westend");
        Track westendSpur1 = westend.getTrackByName("Westend spur 1", null);

        // test car from staging, create one staging track
        Location staging = lmanager.newLocation("Staging");
        Track stagingTrack1 = staging.addTrack("Staging Track 1", Track.STAGING);
        stagingTrack1.setLength(500);
        stagingTrack1.setAddCustomLoadsEnabled(true);

        // add staging to the start of the route
        Route route = train.getRoute();
        route.addLocation(staging, 1);

        // change train name
        train.setName("Train Staging-Westend-Midtown-Eastend");

        // confirm staging at start of route
        Assert.assertEquals("1st location in route", staging, train.getTrainDepartsRouteLocation().getLocation());

        // place cars in staging
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("AA", "3", "Boxcar", "40", stagingTrack1, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("AA", "4", "Flat", "40", stagingTrack1, 1);

        // don't allow Eastend spur to service Flat
        eastendSpur1.deleteTypeName("Flat");

        // don't allow car drops at Midtown, routing car to Midtown should fail
        RouteLocation rlMidtown = route.getRouteLocationBySequenceNumber(3);
        // confirm RouteLocation
        Assert.assertEquals("Midtown", "Midtown", rlMidtown.getLocation().getName());
        rlMidtown.setDropAllowed(false);

        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm car has custom load and destination
        Assert.assertEquals("car load", "Flour", c3.getLoadName());
        Assert.assertEquals("car destination track", eastendSpur1, c3.getDestinationTrack());
        Assert.assertEquals("car destination track", westendSpur1, c4.getDestinationTrack());
    }

    /**
     * Test generation of custom load for car out of staging, to a staging track
     * serviced by a different train. Create two train routes, one out of
     * staging, the other into staging. Note that the generation of custom loads
     * is random out from staging to another staging track. There are three
     * loads, "Nuts" which is excluded by the receiving staging location. "Bags"
     * which is excluded by the 2nd train. And 'Flour" which is the only load
     * that can travel from staging to staging.
     */
    @Test
    public void testCustomCarLoadFromStagingC() {

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        Location westend = lmanager.newLocation("Westend");
        Track westendSpur1 = westend.getTrackByName("Westend spur 1", null);
        Track westendInterchange1 = westend.getTrackByName("Westend interchange 1", null);

        // create two staging locations and tracks
        Location staging1 = lmanager.newLocation("Staging 1");
        Track stagingTrack1 = staging1.addTrack("Staging Track 1", Track.STAGING);
        stagingTrack1.setLength(500);
        stagingTrack1.setAddCustomLoadsAnyStagingTrackEnabled(true);

        Location staging2 = lmanager.newLocation("Staging 2");
        Track stagingTrack2 = staging2.addTrack("Staging Track 2", Track.STAGING);
        stagingTrack2.setLength(500);

        // don't accept "Bags" into staging
        stagingTrack2.setLoadOption(Track.EXCLUDE_LOADS);
        stagingTrack2.addLoadName("Bags");

        // add staging to the start of the route
        Route route = train.getRoute();
        route.addLocation(staging1, 1);

        // change train name
        train.setName("Train Staging-Westend-Midtown-Eastend");

        // confirm staging at start of route
        Assert.assertEquals("1st location in route", staging1, train.getTrainDepartsRouteLocation().getLocation());

        // place cars in staging
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("AA", "3", "Boxcar", "50", stagingTrack1, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("AA", "4", "Boxcar", "50", stagingTrack1, 1);

        // first test that staging 2 isn't reachable
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // failed to route cars to staging, so sent to a track that would accept them
        Assert.assertEquals("car load", "E", c3.getLoadName());
        Assert.assertEquals("car destination track", westendSpur1, c3.getDestinationTrack());
        Assert.assertEquals("car load", "E", c4.getLoadName());
        Assert.assertEquals("car destination track", westendSpur1, c4.getDestinationTrack());

        //there's an interchange track at Westend, so we'll create a 2nd train that departs there to staging 2
        Route rte2 = rmanager.newRoute("Route Westend-Staging2");
        rte2.addLocation(westend);
        rte2.addLocation(staging2);
        Train train2 = tmanager.newTrain("Train Westend-Staging2");
        train2.setRoute(rte2);

        // don't allow train2 to carry load "Nuts"
        train2.setLoadOption(Train.EXCLUDE_LOADS);
        train2.addLoadName("Nuts");

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm car has custom load and destination, only load option is "Flour"
        Assert.assertEquals("car load", "Flour", c3.getLoadName());
        Assert.assertEquals("car destination track", westendInterchange1, c3.getDestinationTrack());
        Assert.assertEquals("car final destination", staging2, c3.getFinalDestination());
        // track into staging should not be set
        Assert.assertEquals("car final destination track", null, c3.getFinalDestinationTrack());

        Assert.assertEquals("car load", "Flour", c4.getLoadName());
        Assert.assertEquals("car destination track", westendInterchange1, c4.getDestinationTrack());
        Assert.assertEquals("car final destination", staging2, c4.getFinalDestination());
        // track into staging should not be set
        Assert.assertEquals("car final destination track", null, c4.getFinalDestinationTrack());

        // now limit the length of the interchange track to one additional car
        westendInterchange1.setLength(100); // there's a 40' car already on this track

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm car has custom load and destination, only load option is "Flour"
        Assert.assertEquals("car load", "Flour", c3.getLoadName());
        Assert.assertEquals("car destination track", westendInterchange1, c3.getDestinationTrack());
        Assert.assertEquals("car final destination", staging2, c3.getFinalDestination());
        // track into staging should not be set
        Assert.assertEquals("car final destination track", null, c3.getFinalDestinationTrack());

        // no room at interchange track for c4, so sent to spur
        Assert.assertEquals("car load", "E", c4.getLoadName());
        Assert.assertEquals("car destination track", westendSpur1, c4.getDestinationTrack());
        Assert.assertEquals("car final destination", null, c4.getFinalDestination());
        // track into staging should not be set
        Assert.assertEquals("car final destination track", null, c4.getFinalDestinationTrack());
    }

    /**
     * Test generated custom load out of staging. Departure staging track
     * restricts ship load.
     */
    @Test
    public void testCustomCarLoadFromStagingD() {

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        Location midtown = lmanager.newLocation("Midtown");
        Track midtownSpur1 = midtown.getTrackByName("Midtown spur 1", null);

        // test car from staging, create one staging track
        Location staging = lmanager.newLocation("Staging");
        Track stagingTrack1 = staging.addTrack("Staging Track 1", Track.STAGING);
        stagingTrack1.setLength(500);
        stagingTrack1.setAddCustomLoadsEnabled(true);

        // add staging to the start of the route
        Route route = train.getRoute();
        route.addLocation(staging, 1);

        // change train name
        train.setName("Train Staging-Westend-Midtown-Eastend");

        // place cars in staging
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("AA", "3", "Boxcar", "40", stagingTrack1, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("AA", "4", "Boxcar", "50", stagingTrack1, 1);

        // don't allow staging to ship "Flour"
        stagingTrack1.setShipLoadOption(Track.EXCLUDE_LOADS);
        stagingTrack1.addShipLoadName("Flour");

        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm car has custom load and destination
        Assert.assertEquals("car load", "Nuts", c3.getLoadName());
        Assert.assertEquals("car destination track", midtownSpur1, c3.getDestinationTrack());

        Assert.assertEquals("car load", "Bags", c4.getLoadName());
        Assert.assertEquals("car destination track", midtownSpur1, c4.getDestinationTrack());
    }

    /**
     * Test generated custom load out of staging. Departure train restricts
     * which load can be serviced out of staging
     */
    @Test
    public void testCustomCarLoadFromStagingE() {

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        Location midtown = lmanager.newLocation("Midtown");
        Track midtownSpur1 = midtown.getTrackByName("Midtown spur 1", null);

        // test car from staging, create one staging track
        Location staging = lmanager.newLocation("Staging");
        Track stagingTrack1 = staging.addTrack("Staging Track 1", Track.STAGING);
        stagingTrack1.setLength(500);
        stagingTrack1.setAddCustomLoadsEnabled(true);

        // add staging to the start of the route
        Route route = train.getRoute();
        route.addLocation(staging, 1);

        // change train name
        train.setName("Train Staging-Westend-Midtown-Eastend");

        // place cars in staging
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("AA", "3", "Boxcar", "40", stagingTrack1, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("AA", "4", "Boxcar", "50", stagingTrack1, 1);

        // don't allow train to carry "Nuts"
        train.setLoadOption(Train.EXCLUDE_LOADS);
        train.addLoadName("Nuts");

        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm car has custom load and destination
        Assert.assertEquals("car load", "Flour", c3.getLoadName());
        Assert.assertEquals("car destination track", midtownSpur1, c3.getDestinationTrack());

        Assert.assertEquals("car load", "Bags", c4.getLoadName());
        Assert.assertEquals("car destination track", midtownSpur1, c4.getDestinationTrack());
    }

    /**
     * Test schedule drop off and pick up day options.
     */
    @Test
    public void testScheduleDayOptionsFromStaging() {

        TrainScheduleManager trainScheduleManager = InstanceManager.getDefault(TrainScheduleManager.class);
        List<TrainSchedule> schedules = trainScheduleManager.getSchedulesByNameList();

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        Location westend = lmanager.newLocation("Westend");
        Track westendSpur1 = westend.getTrackByName("Westend spur 1", Track.SPUR);

        Location midtown = lmanager.newLocation("Midtown");
        Track midtownSpur1 = midtown.getTrackByName("Midtown spur 1", null);

        // test car from staging, create one staging track
        Location staging = lmanager.newLocation("Staging");
        Track stagingTrack1 = staging.addTrack("Staging Track 1", Track.STAGING);
        stagingTrack1.setLength(500);
        stagingTrack1.setAddCustomLoadsEnabled(true);

        // add staging to the start of the route
        Route route = train.getRoute();
        route.addLocation(staging, 1);

        // change train name
        train.setName("Train Staging-Westend-Midtown-Eastend");

        // place cars in staging
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("AA", "3", "Boxcar", "40", stagingTrack1, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("AA", "4", "Boxcar", "50", stagingTrack1, 1);

        // modify schedule to only allow delivery of a certain load on a given  day
        Schedule schedule = smanager.getScheduleByName("Schedule for car load");
        ScheduleItem sch1Item1 = schedule.getItemBySequenceId(1); // requests Boxcar with "Bags"
        ScheduleItem sch1Item2 = schedule.getItemBySequenceId(2); // requests Boxcar with "Flour"
        ScheduleItem sch1Item3 = schedule.getItemBySequenceId(3); // requests Boxcar with "Nuts"

        // deliver "Bags" on Monday, pull on Saturday
        sch1Item1.setSetoutTrainScheduleId(schedules.get(1).getId());
        sch1Item1.setPickupTrainScheduleId(schedules.get(2).getId());

        // deliver "Flour" on Sunday, pull on Thursday
        sch1Item2.setSetoutTrainScheduleId(schedules.get(3).getId());
        sch1Item2.setPickupTrainScheduleId(schedules.get(4).getId());

        // deliver "Nuts" on Tuesday, pull on Wednesday
        sch1Item3.setSetoutTrainScheduleId(schedules.get(5).getId());
        sch1Item3.setPickupTrainScheduleId(schedules.get(6).getId());

        Assert.assertEquals("Active schedule", "", trainScheduleManager.getTrainScheduleActiveId());

        // no deliveries allowed
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm car sent to spur, no custom load
        Assert.assertEquals("car load", "E", c3.getLoadName());
        Assert.assertEquals("car destination track", westendSpur1, c3.getDestinationTrack());

        Assert.assertEquals("car load", "E", c4.getLoadName());
        Assert.assertEquals("car destination track", westendSpur1, c4.getDestinationTrack());

        // now set the day to Sunday
        Assert.assertEquals("Train schedule day", "Sunday", schedules.get(3).getName());
        trainScheduleManager.setTrainScheduleActiveId(schedules.get(3).getId());

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // confirm "Sunday" delivery, and pull on Thursday
        Assert.assertEquals("car load", "Flour", c3.getLoadName());
        Assert.assertEquals("car destination track", midtownSpur1, c3.getDestinationTrack());
        Assert.assertEquals("car next pickup id", schedules.get(4).getId(), c3.getNextPickupScheduleId());

        Assert.assertEquals("car load", "Flour", c4.getLoadName());
        Assert.assertEquals("car destination track", midtownSpur1, c4.getDestinationTrack());
        Assert.assertEquals("car next pickup id", schedules.get(4).getId(), c4.getNextPickupScheduleId());
    }

    /**
     * Test schedule Random feature
     */
    @Test
    public void testScheduleRandomFromStaging() {

        setupCustomCarLoad();

        // get tracks, train travels from west to east
        Train train = tmanager.getTrainByName("Train Westend-Midtown-Eastend");

        Location midtown = lmanager.newLocation("Midtown");
        Track midtownSpur1 = midtown.getTrackByName("Midtown spur 1", null);
        Track midtownSpur2 = midtown.getTrackByName("Midtown spur 2", null);

        Location eastend = lmanager.getLocationByName("Eastend");
        Track eastendSpur1 = eastend.getTrackByName("Eastend spur 1", Track.SPUR);

        Location westend = lmanager.getLocationByName("Westend");
        Track westendSpur1 = westend.getTrackByName("Westend spur 1", Track.SPUR);

        Location northend = lmanager.getLocationByName("Northend");
        Track northendSpur1 = northend.getTrackByName("Northend spur 1", Track.SPUR);

        // test car from staging, create one staging track
        Location staging = lmanager.newLocation("Staging");
        Track stagingTrack1 = staging.addTrack("Staging Track 1", Track.STAGING);
        stagingTrack1.setLength(500);
        stagingTrack1.setAddCustomLoadsEnabled(true);

        // add staging to the start of the route
        Route route = train.getRoute();
        route.addLocation(staging, 1);

        // change train name
        train.setName("Train Staging-Westend-Midtown-Eastend");

        // place cars in staging
        Car c3 = JUnitOperationsUtil.createAndPlaceCar("AA", "3", "Boxcar", "40", stagingTrack1, 0);
        Car c4 = JUnitOperationsUtil.createAndPlaceCar("AA", "4", "Boxcar", "50", stagingTrack1, 1);

        // modify schedule to only allow delivery of a certain load on a given  day
        Schedule schedule = smanager.getScheduleByName("Schedule for car load");
        ScheduleItem sch1Item1 = schedule.getItemBySequenceId(1); // requests Boxcar with "Bags"
        ScheduleItem sch1Item2 = schedule.getItemBySequenceId(2); // requests Boxcar with "Flour"
        ScheduleItem sch1Item3 = schedule.getItemBySequenceId(3); // requests Boxcar with "Nuts"

        sch1Item1.setRandom("0"); // 0% chance
        sch1Item2.setRandom("0");
        sch1Item3.setRandom("100"); // only choice 100% chance

        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        Assert.assertEquals("car load", "Nuts", c3.getLoadName());
        Assert.assertEquals("car destination track", midtownSpur1, c3.getDestinationTrack());

        Assert.assertEquals("car load", "Nuts", c4.getLoadName());
        Assert.assertEquals("car destination track", midtownSpur1, c4.getDestinationTrack());

        // configure test so only one spur reports error message
        midtownSpur1.deleteTypeName("Boxcar");
        midtownSpur2.deleteTypeName("Boxcar");
        northendSpur1.deleteTypeName("Boxcar");

        c4.setTypeName("Flat");

        // test bogus random number
        sch1Item1.setRandom("0"); // 0% chance
        sch1Item2.setRandom("A"); // random disabled, 100% chance 
        sch1Item3.setRandom("0"); // 0% chance

        train.reset();
        new TrainBuilder().build(train);
        Assert.assertTrue(train.isBuilt());

        // there are 4 spurs with this schedule, but only one error messages
        jmri.util.JUnitAppender.assertErrorMessage("Schedule item (1c2) random value (A) isn't a number");

        Assert.assertEquals("car load", "Flour", c3.getLoadName());
        Assert.assertEquals("car destination track", eastendSpur1, c3.getDestinationTrack());

        Assert.assertEquals("car load", "E", c4.getLoadName());
        Assert.assertEquals("car destination track", westendSpur1, c4.getDestinationTrack());
    }

    private void setupCustomCarLoad() {

        // register the car loads used
        cld.addName("Boxcar", "Flour");
        cld.addName("Boxcar", "Bags");
        cld.addName("Boxcar", "Nuts");

        // Create locations and tracks used
        Location westend = lmanager.newLocation("Westend");
        Location midtown = lmanager.newLocation("Midtown");
        Location eastend = lmanager.newLocation("Eastend");

        // this location isn't reachable, for test coverage
        Location northend = lmanager.newLocation("Northend");

        Track westendInterchange1 = westend.addTrack("Westend interchange 1", Track.INTERCHANGE);
        westendInterchange1.setLength(500);

        Track westendSpur1 = westend.addTrack("Westend spur 1", Track.SPUR);
        westendSpur1.setLength(500);

        Track midtownSpur1 = midtown.addTrack("Midtown spur 1", Track.SPUR);
        midtownSpur1.setLength(500);

        Track midtownSpur2 = midtown.addTrack("Midtown spur 2", Track.SPUR);
        midtownSpur2.setLength(500);

        Track northendSpur1 = northend.addTrack("Northend spur 1", Track.SPUR);
        northendSpur1.setLength(500);

        // alternate track for spur 1
        Track midtownYard = midtown.addTrack("Midtown yard", Track.YARD);
        midtownYard.setLength(500);
        midtownSpur1.setAlternateTrack(midtownYard);

        Track eastendSpur1 = eastend.addTrack("Eastend spur 1", Track.SPUR);
        eastendSpur1.setLength(500);

        // must set moves after all tracks are created
        westendInterchange1.setMoves(10);
        midtownSpur1.setMoves(20);
        midtownSpur2.setMoves(40);
        eastendSpur1.setMoves(60);

        // improve test coverage
        midtownSpur2.setHoldCarsWithCustomLoadsEnabled(true);

        // create the schedule, each spur should request loads in this order: Flour, Nuts, Bags, repeat
        Schedule schedule = smanager.newSchedule("Schedule for car load");
        ScheduleItem sch1Item1 = schedule.addItem("Boxcar");
        sch1Item1.setReceiveLoadName("Bags");
        ScheduleItem sch1Item2 = schedule.addItem("Boxcar");
        sch1Item2.setReceiveLoadName("Flour");
        ScheduleItem sch1Item3 = schedule.addItem("Boxcar");
        sch1Item3.setReceiveLoadName("Nuts");

        midtownSpur1.setSchedule(schedule);
        midtownSpur2.setSchedule(schedule);
        eastendSpur1.setSchedule(schedule);
        northendSpur1.setSchedule(schedule); // not reachable by this train

        // load cars
        Car c1 = JUnitOperationsUtil.createAndPlaceCar("AA", "1", "Boxcar", "40", westendInterchange1, 0);
        JUnitOperationsUtil.createAndPlaceCar("AA", "2", "Boxcar", "40", midtownSpur1, 0);

        c1.setLoadName("Bags");

        // create the route        
        Route route = rmanager.newRoute("Route Westend-Midtown-Eastend");
        route.addLocation(westend);
        route.addLocation(midtown);
        route.addLocation(eastend);

        Train train = tmanager.newTrain("Train Westend-Midtown-Eastend");
        train.setRoute(route);
    }

    private Route getThreeLocationRouteWithFourEngines() {
        Route route = rmanager.newRoute("Route A-B-C");

        A = lmanager.newLocation("A");
        B = lmanager.newLocation("B");
        C = lmanager.newLocation("C");
        Track tA = A.addTrack("track", Track.SPUR);
        Track tB = B.addTrack("track", Track.SPUR);
        Track tC = C.addTrack("track", Track.SPUR);
        tA.setLength(500);
        tB.setLength(500);
        tC.setLength(500);

        rA = route.addLocation(A);
        rB = route.addLocation(B);
        rC = route.addLocation(C);

        rA.setMaxCarMoves(5);
        rB.setMaxCarMoves(5);
        rC.setMaxCarMoves(5);

        Engine e1 = emanager.newRS("E", "1");
        e1.setModel("GP20");
        Engine e2 = emanager.newRS("E", "2");
        e2.setModel("GP30");
        Engine e3 = emanager.newRS("E", "3");
        e3.setModel("GP35");
        Engine e4 = emanager.newRS("E", "4");
        e4.setModel("GP40");

        e1.setLocation(A, tA);
        e2.setLocation(A, tA);
        e3.setLocation(A, tA);
        e4.setLocation(A, tA);

        return route;
    }

    private String getTrainStatus(Train train) {
        String[] status = train.getStatus().split(" ");
        return status[0];
    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();

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

        // register the car and engine types used
        ct.addName("Boxcar");
        ct.addName(rb.getString("Caboose"));
        ct.addName("Caboose");
        ct.addName("Flat");

        // load the first six car types
        String carTypes[] = Bundle.getMessage("carTypeNames").split(",");
        ct.addName(carTypes[1]);
        ct.addName(carTypes[2]);
        ct.addName(carTypes[3]);
        ct.addName(carTypes[4]);
        ct.addName(carTypes[5]);
        ct.addName(carTypes[6]);

        et.addName("Diesel");

        Setup.setCarMoves(7); // set default to 7 moves per location
        Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
        // increase test coverage
        Setup.setGenerateCsvManifestEnabled(true);
    }
}
