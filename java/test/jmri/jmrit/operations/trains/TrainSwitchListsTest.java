package jmri.jmrit.operations.trains;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 * @author Dan Boudreau Copyright (C) 2018
 * 
 *         TODO data check of switch list files
 */
public class TrainSwitchListsTest extends OperationsTestCase {

    private TrainManager tmanager;
    private RouteManager rmanager;
    private LocationManager lmanager;
    private EngineManager emanager;
    private CarManager cmanager;

    private Location locationA;
    private Location locationAhyphen;

    @Test
    public void testCTor() {
        TrainSwitchLists t = new TrainSwitchLists();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testDefaults() {

        // the assumed defaults for this set of tests 
        Assert.assertFalse(Setup.isBuildAggressive());
        Assert.assertEquals("default moves for a route", 5, Setup.getCarMoves());
        Assert.assertTrue(Setup.isSwitchListRealTime());

    }

    @Test
    public void testNoSwichListFile() {

        loadLocationsEnginesAndCars();
        File switchListFile = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationA.getName());
        Assert.assertFalse(switchListFile.exists());
    }

    @Test
    public void testSwitchListRealTime() throws IOException {
        loadLocationsEnginesAndCars();

        Train train = tmanager.newTrain("Test switchlists train");
        Route route = rmanager.newRoute("Test switchlists route");
        route.addLocation(locationA);
        locationA.setSwitchListComment("Location A switch list comment");

        train.setRoute(route);

        train.setNumberEngines("1"); // request an engine

        Assert.assertTrue(train.build());

        TrainSwitchLists tsl = new TrainSwitchLists();
        tsl.buildSwitchList(locationA);

        File switchListFileA = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationA.getName());
        Assert.assertTrue(switchListFileA.exists());

        BufferedReader in = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 29, in.lines().count());
        in.close();

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testSwitchListTrain() throws IOException {

        Setup.setSwitchListPageFormat(Setup.PAGE_PER_VISIT);

        loadLocationsEnginesAndCars();

        Train train = tmanager.newTrain("Test switchlists train");
        Route route = rmanager.newRoute("Test switchlists route");

        Location locationB = lmanager.newLocation("Test Location B");
        Track yardB = locationB.addTrack("Yard at B", Track.YARD);
        yardB.setLength(400);

        Location locationC = lmanager.newLocation("Test Location C");
        Track yardC = locationC.addTrack("Yard at C", Track.YARD);
        yardC.setLength(180); // only enough room for one car and engine

        // create the route
        route.addLocation(locationA);
        route.addLocation(locationB);
        RouteLocation rlc = route.addLocation(locationC);

        rlc.setComment("Only one drop at location C");

        train.setRoute(route);

        train.setNumberEngines("1"); // request an engine

        // place one car at location B for pick up
        Car c6 = cmanager.getByRoadAndNumber("AA", "6");
        Assert.assertEquals("Place car on track", Track.OKAY, c6.setLocation(locationB, yardB));

        Assert.assertTrue(train.build());

        TrainSwitchLists tsl = new TrainSwitchLists();
        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);

        File switchListFileA = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationA.getName());
        Assert.assertTrue(switchListFileA.exists());
        File switchListFileB = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationB.getName());
        Assert.assertTrue(switchListFileB.exists());
        File switchListFileC = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationC.getName());
        Assert.assertTrue(switchListFileC.exists());

        BufferedReader inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 27, inA.lines().count());
        BufferedReader inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 20, inB.lines().count());
        BufferedReader inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 18, inC.lines().count());

        // this train has no work at all locations
        Train train2 = tmanager.newTrain("Test switchlists train 2");
        train2.setRoute(route);
        Assert.assertTrue(train2.build());

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);

        // no change in switch lists
        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 27, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 20, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 18, inC.lines().count());
        
        inA.close();
        inB.close();
        inC.close();

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    /**
     * Creates a switch list for a train that visits a location twice
     * @throws IOException BufferedReader close
     */
    @Test
    public void testSwitchListTrainTurn() throws IOException {

        Setup.setPrintLoadsAndEmptiesEnabled(true); // improve test coverage
        Setup.setSwitchListPageFormat(Setup.PAGE_PER_VISIT);
        Setup.setSwitchListAllTrainsEnabled(true);

        loadLocationsEnginesAndCars();

        Train train = tmanager.newTrain("Test switchlists train");
        Route route = rmanager.newRoute("Test switchlists route");

        Location locationB = lmanager.newLocation("Test Location B");
        Track yardB = locationB.addTrack("Yard at B", Track.YARD);
        yardB.setLength(400);

        Location locationC = lmanager.newLocation("Test Location C");
        Track yardC = locationC.addTrack("Yard at C", Track.YARD);
        yardC.setLength(80); // only enough room for one car

        Location locationD = lmanager.newLocation("Test Location D");

        // create the turn
        route.addLocation(locationA);
        route.addLocation(locationB);
        RouteLocation rlc = route.addLocation(locationC);
        route.addLocation(locationD); // no tracks at D, so no work
        RouteLocation rld = route.addLocation(locationD); // change direction
        route.addLocation(locationC);
        route.addLocation(locationB); // no work at B at this time
        route.addLocation(locationA);

        rlc.setComment("Only one drop at location C");

        // confirm that the default train direction was north
        Assert.assertEquals("default train direction", RouteLocation.NORTH, rlc.getTrainDirection());
        rld.setTrainDirection(RouteLocation.SOUTH);

        train.setRoute(route);

        train.setNumberEngines("1"); // request an engine

        // place one car at location B for pick up
        Car c6 = cmanager.getByRoadAndNumber("AA", "6");
        Assert.assertEquals("Place car on track", Track.OKAY, c6.setLocation(locationB, yardB));

        Assert.assertTrue(train.build());

        TrainSwitchLists tsl = new TrainSwitchLists();
        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        // improve test coverage, switch list for a location without work
        tsl.buildSwitchList(locationD);

        File switchListFileA = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationA.getName());
        Assert.assertTrue(switchListFileA.exists());
        File switchListFileB = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationB.getName());
        Assert.assertTrue(switchListFileB.exists());
        File switchListFileC = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationC.getName());
        Assert.assertTrue(switchListFileC.exists());
        File switchListFileD = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationD.getName());
        Assert.assertTrue(switchListFileD.exists());

        // note that any changes to switch list will cause the following tests to fail
        BufferedReader inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 32, inA.lines().count());
        BufferedReader inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 26, inB.lines().count());
        BufferedReader inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 20, inC.lines().count());
        BufferedReader inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 12, inD.lines().count());

        train.move(); // move train to B

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 19, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 23, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 20, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 12, inD.lines().count());

        train.move(rlc); // move train to C
        Assert.assertEquals("current train location", "Test Location C", train.getCurrentRouteLocation().getName());

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB); // should report that already serviced train
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 19, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 20, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 17, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 12, inD.lines().count());

        train.move(); // move train to D
        Assert.assertEquals("current train location", "Test Location D", train.getCurrentRouteLocation().getName());
        train.move(); // move train to D reverse direction
        Assert.assertEquals("current train location", "Test Location D", train.getCurrentRouteLocation().getName());

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 19, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 20, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 15, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 9, inD.lines().count());

        train.move(); // move train to C
        Assert.assertEquals("current train location", "Test Location C", train.getCurrentRouteLocation().getName());

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 19, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 20, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 14, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 9, inD.lines().count());

        train.move(); // move train to B
        Assert.assertEquals("current train location", "Test Location B", train.getCurrentRouteLocation().getName());

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 19, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 19, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 14, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 9, inD.lines().count());

        train.move(); // move train to A
        Assert.assertEquals("current train location", "Test Location A", train.getCurrentRouteLocation().getName());

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 18, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 16, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 14, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 9, inD.lines().count());

        train.move(); // terminate train

        tsl.buildSwitchList(locationA);
        tsl.buildSwitchList(locationB);
        tsl.buildSwitchList(locationC);
        tsl.buildSwitchList(locationD);

        inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 9, inA.lines().count());
        inB = JUnitOperationsUtil.getBufferedReader(switchListFileB);
        Assert.assertEquals("confirm number of lines in switch list", 10, inB.lines().count());
        inC = JUnitOperationsUtil.getBufferedReader(switchListFileC);
        Assert.assertEquals("confirm number of lines in switch list", 7, inC.lines().count());
        inD = JUnitOperationsUtil.getBufferedReader(switchListFileD);
        Assert.assertEquals("confirm number of lines in switch list", 4, inD.lines().count());
        
        inA.close();
        inB.close();
        inC.close();
        inD.close();

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testSwitchListTwoColumnFormat() throws IOException {

        Setup.setManifestFormat(Setup.TWO_COLUMN_FORMAT);
        Setup.setSwitchListPageFormat(Setup.PAGE_PER_TRAIN);

        loadLocationsEnginesAndCars();

        Train train = tmanager.newTrain("Test switchlists train");
        Route route = rmanager.newRoute("Test switchlists route");
        route.addLocation(locationA);
        train.setRoute(route);

        train.setNumberEngines("1"); // request an engine

        Assert.assertTrue(train.build());

        TrainSwitchLists tsl = new TrainSwitchLists();
        tsl.buildSwitchList(locationA);

        File switchListFileA = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationA.getName());
        Assert.assertTrue(switchListFileA.exists());

        BufferedReader inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 29, inA.lines().count());
        inA.close();

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testSwitchListTwoColumnTrackFormat() throws IOException {

        Setup.setManifestFormat(Setup.TWO_COLUMN_TRACK_FORMAT);

        loadLocationsEnginesAndCars();

        Train train = tmanager.newTrain("Test switchlists train");
        Route route = rmanager.newRoute("Test switchlists route");
        route.addLocation(locationA);
        train.setRoute(route);

        train.setNumberEngines("1"); // request an engine

        Assert.assertTrue(train.build());

        TrainSwitchLists tsl = new TrainSwitchLists();
        tsl.buildSwitchList(locationA);

        File switchListFileA = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationA.getName());
        Assert.assertTrue(switchListFileA.exists());

        BufferedReader inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 38, inA.lines().count());
        inA.close();

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testSwitchListNotRealTime() throws IOException {

        Setup.setSwitchListRealTime(false);
        loadLocationsEnginesAndCars();

        Setup.setSwitchListPageFormat(Setup.PAGE_PER_TRAIN); // for better test coverage

        Train train = tmanager.newTrain("Test switchlists train");
        Route route = rmanager.newRoute("Test switchlists route");
        route.addLocation(locationA);
        train.setRoute(route);

        Train train2 = tmanager.newTrain("Test switchlists train 2");
        train2.setRoute(route); // use the same route

        TrainSwitchLists tsl = new TrainSwitchLists();

        // test no new work
        tsl.buildSwitchList(locationA);

        // now create some work
        Assert.assertTrue(train.build());
        tsl.buildSwitchList(locationA);

        // now append new work to file
        Assert.assertTrue(train2.build());

        Assert.assertEquals("Confirm append mode", Location.SW_APPEND, locationA.getSwitchListState());
        tsl.buildSwitchList(locationA);

        File switchListFileA = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationA.getName());
        Assert.assertTrue(switchListFileA.exists());

        BufferedReader inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 23, inA.lines().count());
        inA.close();

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testSwitchListSwitcher() throws IOException {
        loadLocationsEnginesAndCars();

        Train train = tmanager.newTrain("Test switchlists train");
        Route route = rmanager.newRoute("Test switchlists route");
        route.addLocation(locationA);
        route.addLocation(locationAhyphen); // "similar" name for location A
        train.setRoute(route);

        Train train2 = tmanager.newTrain("Test switchlists train 2");
        train2.setRoute(route); // use the same route

        train.setNumberEngines("1"); // request an engine

        Assert.assertTrue(train.build());

        TrainSwitchLists tsl = new TrainSwitchLists();
        tsl.buildSwitchList(locationA);

        File switchListFileA = InstanceManager.getDefault(TrainManagerXml.class).getSwitchListFile(locationA.getName());
        Assert.assertTrue(switchListFileA.exists());

        BufferedReader inA = JUnitOperationsUtil.getBufferedReader(switchListFileA);
        Assert.assertEquals("confirm number of lines in switch list", 28, inA.lines().count());
        inA.close();

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    /**
     * Unique data for this test. Any changes below can affect the number of
     * lines in the switch list file
     */
    private void loadLocationsEnginesAndCars() {

        CarTypes ct = InstanceManager.getDefault(CarTypes.class);

        // register the car and engine types used
        ct.addName("Boxcar");

        locationA = lmanager.newLocation("Test Location A");
        Track spurA = locationA.addTrack("Spur at A", Track.SPUR);
        spurA.setLength(400);
        Track yardA = locationA.addTrack("Yard at A", Track.YARD);
        yardA.setLength(400);

        // create a "similar" location and track names with a hyphen added to it
        locationAhyphen = lmanager.newLocation(locationA.getName() + "-1");
        Track spurAA = locationAhyphen.addTrack("Spur at A-1", Track.SPUR);
        spurAA.setLength(400);
        Track yardAA = locationAhyphen.addTrack("Yard at A-1", Track.YARD);
        yardAA.setLength(400);

        // place 6 cars
        JUnitOperationsUtil.createAndPlaceCar("AA", "1", "Boxcar", "40", "DAB", "1984", spurA, 0);
        JUnitOperationsUtil.createAndPlaceCar("AA", "2", "Boxcar", "40", "DAB", "1984", spurA, 0);
        JUnitOperationsUtil.createAndPlaceCar("AA", "3", "Boxcar", "40", "DAB", "1984", spurA, 0);
        // 3 on yard tracks
        JUnitOperationsUtil.createAndPlaceCar("AA", "4", "Boxcar", "40", "DAB", "1984", yardA, 0);
        JUnitOperationsUtil.createAndPlaceCar("AA", "5", "Boxcar", "40", "DAB", "1984", yardA, 0);
        Car c6 = JUnitOperationsUtil.createAndPlaceCar("AA", "6", "Boxcar", "40", "DAB", "1984", yardA, 20);
        c6.setUtility(true); // make this car a utility car for better test coverage

        Engine e1 = emanager.newRS("NYC", "1");
        e1.setModel("E8");
        Assert.assertEquals("Place engine on track", Track.OKAY, e1.setLocation(locationA, yardA));
    }

    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        // setup new managers
        tmanager = InstanceManager.getDefault(TrainManager.class);
        rmanager = InstanceManager.getDefault(RouteManager.class);
        lmanager = InstanceManager.getDefault(LocationManager.class);
        emanager = InstanceManager.getDefault(EngineManager.class);
        cmanager = InstanceManager.getDefault(CarManager.class);

        // disable build messages
        tmanager.setBuildMessagesEnabled(false);
        // disable build reports
        tmanager.setBuildReportEnabled(false);

        Setup.setBuildAggressive(false);
        Setup.setStagingTrainCheckEnabled(true);
        Setup.setMaxTrainLength(1000);
        Setup.setRouterBuildReportLevel(Setup.BUILD_REPORT_VERY_DETAILED);
        Setup.setLocalInterchangeMovesEnabled(false);
        Setup.setLocalSpurMovesEnabled(false);
        Setup.setLocalYardMovesEnabled(false);
        Setup.setCarMoves(5);
        Setup.setMaxNumberEngines(6);
        Setup.setStagingTrainCheckEnabled(true);
        Setup.setStagingAllowReturnEnabled(false);
        Setup.setGenerateCsvManifestEnabled(false);
        Setup.setStagingPromptToEnabled(false);
        Setup.setCarRoutingEnabled(true);

        // improve test coverage
        Setup.setSwitchListAllTrainsEnabled(false);
        Setup.setPrintTrainScheduleNameEnabled(true);
        Setup.setSwitchListRouteLocationCommentEnabled(true);

        TrainScheduleManager tsmanager = InstanceManager.getDefault(TrainScheduleManager.class);
        tsmanager.setTrainScheduleActiveId(tsmanager.getSchedulesByIdList().get(0).getId());

    }

    @Override
    @AfterEach
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainSwitchListsTest.class);

}
