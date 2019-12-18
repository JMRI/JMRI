package jmri.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ResourceBundle; // for access operations keys directly.
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.automation.Automation;
import jmri.jmrit.operations.automation.AutomationItem;
import jmri.jmrit.operations.automation.AutomationManager;
import jmri.jmrit.operations.automation.actions.ActivateTrainScheduleAction;
import jmri.jmrit.operations.automation.actions.BuildTrainAction;
import jmri.jmrit.operations.automation.actions.GotoAction;
import jmri.jmrit.operations.automation.actions.MoveTrainAction;
import jmri.jmrit.operations.automation.actions.RunAutomationAction;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleItem;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarOwners;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineTypes;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.routes.RouteManager;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.trains.schedules.TrainSchedule;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import org.junit.Assert;

/**
 * Common utility methods for working with Operations related JUnit tests.
 * Portions of this code adapted from the operations tests written by Bob
 * Coleman and Dan Boudreau
 *
 * @author Paul Bender Copyright 2017
 * @since 2.7.1
 */
public class JUnitOperationsUtil {

    private final static int DIRECTION_ALL = Location.EAST + Location.WEST + Location.NORTH + Location.SOUTH;

    /**
     * Setup the operations test file names and test locations.
     * 
     */
    public static void setupOperationsTests() {

        //shut down the AutoSave thread if it is running.
        Setup.setAutoSaveEnabled(false);

        // set the file location to temp (in the root of the build directory).
        OperationsSetupXml.setFileLocation("temp" + File.separator);

        // Repoint OperationsSetupXml to JUnitTest subdirectory
        String tempstring = OperationsSetupXml.getOperationsDirectoryName();
        if (!tempstring.contains(File.separator + "JUnitTest")) {
            OperationsSetupXml.setOperationsDirectoryName("operations" + File.separator + "JUnitTest");
        }
        // Change file names to ...Test.xml
        InstanceManager.getDefault(OperationsSetupXml.class).setOperationsFileName("OperationsJUnitTest.xml");
        InstanceManager.getDefault(RouteManagerXml.class).setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
        InstanceManager.getDefault(EngineManagerXml.class).setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
        InstanceManager.getDefault(CarManagerXml.class).setOperationsFileName("OperationsJUnitTestCarRoster.xml");
        InstanceManager.getDefault(LocationManagerXml.class)
                .setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
        InstanceManager.getDefault(TrainManagerXml.class).setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        // delete operations directory and all contents
        File file = new File(OperationsXml.getFileLocation(), OperationsSetupXml.getOperationsDirectoryName());
        FileUtil.delete(file);
        // create an empty operations directory
        FileUtil.createDirectory(file);

        // there can be test concurrency issues if auto save is on
        Assert.assertFalse("Confirm disabled", Setup.isAutoSaveEnabled());
    }

    /**
     * Populate the Operations Managers with a common set of data for tests.
     * Creates and places 9 cars on tracks. 2 Cabooses on staging track 1, 2
     * Boxcars on staging track 1, 2 Boxcars on staging track 2, 1 Boxcar and 2
     * Flats in NI yard. Also creates 4 engines, and places them into two
     * separate consists. Engines are not on a track.
     */
    public static void initOperationsData() {
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.JmritOperationsBundle");

        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        EngineTypes et = InstanceManager.getDefault(EngineTypes.class);
        CarOwners co = InstanceManager.getDefault(CarOwners.class);

        // register the car and engine types used
        ct.addName("Boxcar");
        ct.addName(rb.getString("Caboose"));
        ct.addName("Flat");
        et.addName("Diesel");

        co.addName("AT");
        co.addName("DAB");

        // Set up four engines in two consists
        Consist con1 = emanager.newConsist("C16");

        Consist con2 = emanager.newConsist("C14");

        Engine e1 = new Engine("PC", "5016");
        e1.setModel("GP40");
        e1.setConsist(con1);
        e1.setMoves(123);
        e1.setOwner("AT");
        e1.setBuilt("1990");
        emanager.register(e1);

        Engine e2 = new Engine("PC", "5019");
        e2.setModel("GP40");
        e2.setConsist(con1);
        e2.setMoves(321);
        e2.setOwner("AT");
        e2.setBuilt("1990");
        emanager.register(e2);

        Engine e3 = new Engine("PC", "5524");
        e3.setModel("SD45");
        e3.setConsist(con2);
        e3.setOwner("DAB");
        e3.setBuilt("1980");
        emanager.register(e3);

        Engine e4 = new Engine("PC", "5559");
        e4.setModel("SD45");
        e4.setConsist(con2);
        e4.setOwner("DAB");
        e4.setBuilt("1980");
        emanager.register(e4);

        // Set up a route of 3 locations: North End Staging (2 tracks),
        // North Industries (1 track), and South End Staging (2 tracks).
        createTwoStagingLocations();

        Location locationNorthIndustries = new Location("20", "North Industries");
        locationNorthIndustries.setSwitchListEnabled(true);
        lmanager.register(locationNorthIndustries);

        Track l20yard1 = new Track("20s1", "NI Yard", Track.YARD, locationNorthIndustries);
        l20yard1.setLength(432);
        l20yard1.setCommentBoth("Test comment for NI Yard drops and pulls");
        l20yard1.setCommentSetout("Test comment for NI Yard drops only");
        l20yard1.setCommentPickup("Test comment for NI Yard pulls only");

        locationNorthIndustries.register(l20yard1);

        // get departure staging and tracks
        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");
        Track northEndStaging2 = locationNorthEnd.getTrackById("1s2");
        Assert.assertNotNull(northEndStaging1);
        Assert.assertNotNull(northEndStaging2);

        // termination staging
        Location locationSouthEnd = lmanager.getLocationById("3");
        Assert.assertNotNull(locationSouthEnd);

        // Create 2 cabooses, 5 Boxcars, 2 Flats
        // Place Cabooses on Staging tracks
        // Place 4 Boxcars on Staging tracks
        // Place 1 Boxcars and 2 Flats in yard
        Car c1 = createAndPlaceCar("CP", "C10099", rb.getString("Caboose"), "32", "AT", "1980", northEndStaging1, 23);
        c1.setCaboose(true);
        Car c2 = createAndPlaceCar("CP", "C20099", rb.getString("Caboose"), "32", "DAB", "1984", northEndStaging1, 54);
        c2.setCaboose(true);
        createAndPlaceCar("CP", "X10001", "Boxcar", "40", "DAB", "1984", northEndStaging1, 0);
        createAndPlaceCar("CP", "X10002", "Boxcar", "40", "AT", "1-84", northEndStaging1, 4444);
        createAndPlaceCar("CP", "X20001", "Boxcar", "40", "DAB", "1980", northEndStaging2, 0);
        createAndPlaceCar("CP", "X20002", "Boxcar", "40", "DAB", "1978", northEndStaging2, 0);
        createAndPlaceCar("CP", "777", "Flat", "50", "AT", "1990", l20yard1, 6);
        createAndPlaceCar("CP", "888", "Boxcar", "60", "DAB", "1985", l20yard1, 0);
        createAndPlaceCar("CP", "99", "Flat", "90", "AT", "6-80", l20yard1, 0);

        c1.setColor("Red");
        // make sure the ID tags exist before we
        // try to add it to a car.
        jmri.InstanceManager.getDefault(jmri.IdTagManager.class).provideIdTag("RFID 3");
        c1.setRfid("RFID 3");
        c1.setComment("Test Car CP C10099 Comment");

        // Define the route.
        Route route1 = new Route("1", "Southbound Main Route");
        route1.setComment("Comment for route id 1");

        RouteLocation rl1 = new RouteLocation("1r1", locationNorthEnd);
        rl1.setSequenceNumber(1);
        rl1.setTrainDirection(RouteLocation.SOUTH);
        rl1.setMaxCarMoves(5);
        rl1.setMaxTrainLength(1000);
        rl1.setTrainIconX(25); // set the train icon coordinates
        rl1.setTrainIconY(25);
        rl1.setComment("Test route location comment for North End");

        RouteLocation rl2 = new RouteLocation("1r2", locationNorthIndustries);
        rl2.setSequenceNumber(2);
        rl2.setTrainDirection(RouteLocation.SOUTH);
        // test for only 1 pickup and 1 drop
        rl2.setMaxCarMoves(2);
        rl2.setMaxTrainLength(1000);
        rl2.setTrainIconX(75); // set the train icon coordinates

        rl2.setTrainIconY(25);

        RouteLocation rl3 = new RouteLocation("1r3", locationSouthEnd);
        rl3.setSequenceNumber(3);
        rl3.setTrainDirection(RouteLocation.SOUTH);
        rl3.setMaxCarMoves(5);
        rl3.setMaxTrainLength(1000);
        rl3.setTrainIconX(125); // set the train icon coordinates
        rl3.setTrainIconY(25);

        route1.register(rl1);
        route1.register(rl2);
        route1.register(rl3);

        rmanager.register(route1);

        // Finally ready to define the trains.
        Train train1 = new Train("1", "STF");
        train1.setRequirements(Train.CABOOSE);
        train1.setCabooseRoad("CP");
        train1.deleteTypeName("Flat");
        train1.setRoute(route1);
        train1.setDepartureTime("6", "5");
        train1.setComment("Test comment for train STF");
        train1.setDescription("Train STF");

        // increase test coverage by providing a manifest logo for this train
        String path = FileUtil.getExternalFilename(FileUtil.PROGRAM + "resources/logo.gif");
        train1.setManifestLogoPathName(path);

        tmanager.register(train1);

        Train train2 = new Train("2", "SFF");
        train2.setRoute(route1);
        train2.setDepartureTime("22", "45");
        train2.setDescription("Train SFF");
        tmanager.register(train2);

        // improve test coverage
        Setup.setPrintLocationCommentsEnabled(true);
        Setup.setPrintRouteCommentsEnabled(true);
    }

    /**
     * Creates two staging locations for common testing each with two tracks
     */
    public static void createTwoStagingLocations() {

        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        Location locationNorthEnd = new Location("1", "North End Staging");

        locationNorthEnd.setLocationOps(Location.STAGING);
        Assert.assertEquals("confirm default", DIRECTION_ALL, locationNorthEnd.getTrainDirections());

        locationNorthEnd.setComment("Test comment for location North End");
        lmanager.register(locationNorthEnd);

        Track l1staging1 = new Track("1s1", "North End 1", Track.STAGING, locationNorthEnd);

        // confirm defaults
        Assert.assertEquals("confirm default", DIRECTION_ALL, l1staging1.getTrainDirections());
        Assert.assertEquals("confirm default", Track.ALL_ROADS, l1staging1.getRoadOption());
        Assert.assertEquals("confirm default", Track.ANY, l1staging1.getDropOption());
        Assert.assertEquals("confirm default", Track.ANY, l1staging1.getPickupOption());

        l1staging1.setLength(300);
        l1staging1.setCommentBoth("Test comment for North End 1 drops and pulls");
        l1staging1.setCommentSetout("Test comment for North End 1 drops only");
        l1staging1.setCommentPickup("Test comment for North End 1 pulls only");

        Track l1staging2 = new Track("1s2", "North End 2", Track.STAGING, locationNorthEnd);
        l1staging2.setLength(400);

        locationNorthEnd.register(l1staging1);
        locationNorthEnd.register(l1staging2);

        Location locationSouthEnd = new Location("3", "South End Staging");
        locationSouthEnd.setLocationOps(Location.STAGING);
        lmanager.register(locationSouthEnd);

        Track l3s1 = new Track("3s1", "South End 1", Track.STAGING, locationSouthEnd);
        l3s1.setLength(300);
        l3s1.setCommentBoth("Test comment for South End 1 drops and pulls");
        l3s1.setCommentSetout("Test comment for South End 1 drops only");
        l3s1.setCommentPickup("Test comment for South End 1 pulls only");

        Track l3s2 = new Track("3s2", "South End 2", Track.STAGING, locationSouthEnd);
        l3s2.setLength(401);

        locationSouthEnd.register(l3s1);
        locationSouthEnd.register(l3s2);
    }

    /**
     * Creates four staging locations for common testing
     */
    public static void createFourStagingLocations() {

        createTwoStagingLocations();

        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        Location locationWestEnd = new Location("5", "West End Staging");
        locationWestEnd.setLocationOps(Location.STAGING);
        lmanager.register(locationWestEnd);

        Track l5s1 = new Track("5s1", "West End 1", Track.STAGING, locationWestEnd);
        l5s1.setLength(600);

        Track l5s2 = new Track("5s2", "West End 2", Track.STAGING, locationWestEnd);
        l5s2.setLength(600);

        locationWestEnd.register(l5s1);
        locationWestEnd.register(l5s2);

        Location locationEastEnd = new Location("7", "East End Staging");
        locationEastEnd.setLocationOps(Location.STAGING);
        lmanager.register(locationEastEnd);

        Track l7s1 = new Track("7s1", "East End 1", Track.STAGING, locationEastEnd);
        l7s1.setLength(600);

        Track l7s2 = new Track("7s2", "East End 2", Track.STAGING, locationEastEnd);
        l7s2.setLength(600);

        locationEastEnd.register(l7s1);
        locationEastEnd.register(l7s2);
    }

    public static Route createThreeLocationRoute() {

        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        createSevenNormalLocations();

        Route route = rmanager.newRoute("Route Acton-Boston-Chelmsford");

        Location acton = lmanager.getLocationByName("Acton");
        Location boston = lmanager.getLocationByName("Boston");
        Location chelmsford = lmanager.getLocationByName("Chelmsford");

        route.addLocation(acton);
        route.addLocation(boston);
        route.addLocation(chelmsford);

        return route;
    }

    /**
     * Creates a three location route that is also a turn. Train departs North
     * bound and returns South bound.
     * 
     * @return Route
     */
    public static Route createThreeLocationTurnRoute() {

        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        createSevenNormalLocations();

        Route route = rmanager.newRoute("Route Acton-Boston-Chelmsford-Boston-Acton");

        Location acton = lmanager.getLocationByName("Acton");
        Location boston = lmanager.getLocationByName("Boston");
        Location chelmsford = lmanager.getLocationByName("Chelmsford");

        // default train direction is North
        route.addLocation(acton);
        route.addLocation(boston);
        route.addLocation(chelmsford);
        RouteLocation rlC = route.addLocation(chelmsford); // enter 2nd time for train reversal
        rlC.setTrainDirection(RouteLocation.SOUTH);
        RouteLocation rlB = route.addLocation(boston);
        rlB.setTrainDirection(RouteLocation.SOUTH);
        RouteLocation rlA = route.addLocation(acton);
        rlA.setTrainDirection(RouteLocation.SOUTH);
        rlA.setPickUpAllowed(false); // don't include cars at destination

        return route;
    }

    public static Route createFiveLocationRoute() {

        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        Route route = createThreeLocationRoute();

        Location danvers = lmanager.getLocationByName("Danvers");
        Location essex = lmanager.getLocationByName("Essex");

        route.addLocation(danvers);
        route.addLocation(essex);

        route.setName("Route Acton-Boston-Chelmsford-Davers-Essex");

        return route;
    }

    /**
     * Creates a location with 2 spurs, 2 interchanges, and 2 yards
     * 
     * @param name the name of the location and the tracks there.
     * @return the location created
     */
    public static Location createOneNormalLocation(String name) {
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);

        Location location = lmanager.newLocation(name);
        Track trackSpur1 = location.addTrack(name + " Spur 1", Track.SPUR);
        trackSpur1.setLength(200);
        Track trackSpur2 = location.addTrack(name + " Spur 2", Track.SPUR);
        trackSpur2.setLength(200);
        Track trackYard1 = location.addTrack(name + " Yard 1", Track.YARD);
        trackYard1.setLength(500);
        Track trackYard2 = location.addTrack(name + " Yard 2", Track.YARD);
        trackYard2.setLength(500);
        Track trackInterchange1 = location.addTrack(name + " Interchange 1", Track.INTERCHANGE);
        trackInterchange1.setLength(500);
        Track trackInterchange2 = location.addTrack(name + " Interchange 2", Track.INTERCHANGE);
        trackInterchange2.setLength(500);

        // must set track move counts after all tracks are created
        trackSpur1.setMoves(10);
        trackSpur2.setMoves(20);
        trackYard1.setMoves(30);
        trackYard2.setMoves(40);
        trackInterchange1.setMoves(50);
        trackInterchange2.setMoves(60);

        return location;
    }

    /**
     * Creates 7 locations each with 2 spurs, 2 interchanges, and 2 yards Acton,
     * Boston, Chelmsford, Danvers, Essex, Foxboro, Gulf
     */
    public static void createSevenNormalLocations() {

        createOneNormalLocation("Acton");
        createOneNormalLocation("Boston");
        createOneNormalLocation("Chelmsford");
        createOneNormalLocation("Danvers");
        createOneNormalLocation("Essex");
        createOneNormalLocation("Foxboro");
        createOneNormalLocation("Gulf");
    }

    public static Car createAndPlaceCar(String road, String number, String type, String length, Track track,
            int moves) {
        return createAndPlaceCar(road, number, type, length, "", "", track, moves);
    }

    public static Car createAndPlaceCar(String road, String number, String type, String length, String owner,
            String built, Track track, int moves) {

        CarManager cmanager = InstanceManager.getDefault(CarManager.class);

        Car car = cmanager.newRS(road, number);
        car.setTypeName(type);
        car.setLength(length);
        car.setOwner(owner);
        car.setBuilt(built);
        car.setMoves(moves);
        car.setColor("Black");

        if (track != null) {
            Assert.assertEquals("place car", Track.OKAY, car.setLocation(track.getLocation(), track));
        }

        return car;
    }

    public static void loadTrain(Location l) {
        Assert.assertNotNull("Test Loc", l);
        TrainManager trainManager = InstanceManager.getDefault(TrainManager.class);
        Train trainA = trainManager.newTrain("Test Train A");
        // train needs to service location "l" or error message when saving track edit frame
        RouteManager routeManager = InstanceManager.getDefault(RouteManager.class);
        Route route = routeManager.newRoute("Route Train A");
        route.addLocation(l);
        trainA.setRoute(route);
    }

    public static void loadTrains() {
        // Add some cars for the various tests in this suite
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        ResourceBundle rb = ResourceBundle
                .getBundle("jmri.jmrit.operations.JmritOperationsBundle");
        String roadNames[] = rb.getString("carRoadNames").split(",");
        //        String roadNames[] = Bundle.getMessage("carRoadNames").split(",");
        // add caboose to the roster
        Car c = cm.newRS(roadNames[2], "687");
        c.setCaboose(true);
        c = cm.newRS("CP", "435");
        c.setCaboose(true);

        // load engines
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Engine e1 = emanager.newRS("SP", "1");
        e1.setModel("GP40");
        Engine e2 = emanager.newRS("PU", "2");
        e2.setModel("GP40");
        Engine e3 = emanager.newRS("UP", "3");
        e3.setModel("GP40");
        Engine e4 = emanager.newRS("UP", "4");
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
        loadFiveRoutes();
    }

    public static void loadFiveLocations() {
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

    public static void loadFiveRoutes() {
        RouteManager rManager = InstanceManager.getDefault(RouteManager.class);
        Route r1 = rManager.newRoute("Test Route E");
        r1.setComment("Comment test route E");
        Route r2 = rManager.newRoute("Test Route D");
        r2.setComment("Comment test route D");
        Route r3 = rManager.newRoute("Test Route C");
        r3.setComment("Comment test route C");
        Route r4 = rManager.newRoute("Test Route B");
        r4.setComment("Comment test route B");
        Route r5 = rManager.newRoute("Test Route A");
        r5.setComment("Comment test route A");
    }

    public static Schedule createSchedules() {
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location location = lmanager.getLocationByName("North Industries");

        Assert.assertNotNull("Must initOperationsData() before creating schedule", location);
        Track spur1 = location.addTrack("Test Spur 1", Track.SPUR);
        Track spur2 = location.addTrack("Test Spur 2", Track.SPUR);
        Track alternate = location.addTrack("Test Alternate", Track.YARD);
        Track yard = location.getTrackByName("NI Yard", null);

        ScheduleManager scheduleManager = InstanceManager.getDefault(ScheduleManager.class);
        Schedule schedule = scheduleManager.newSchedule("Test Schedule");
        ScheduleItem schAItem1 = schedule.addItem("Boxcar");
        schAItem1.setReceiveLoadName("Empty");
        schAItem1.setShipLoadName("Metal");
        schAItem1.setDestination(location);
        schAItem1.setDestinationTrack(yard);
        ScheduleItem schAItem2 = schedule.addItem("Flat");
        schAItem2.setReceiveLoadName("Junk");
        schAItem2.setShipLoadName("Metal");
        schAItem2.setDestination(location);
        schAItem2.setDestinationTrack(yard);

        // Add schedule to track
        spur1.setSchedule(schedule);
        spur1.setScheduleMode(Track.MATCH); // set schedule into match mode
        spur1.setAlternateTrack(alternate);
        spur1.setReservationFactor(60);
        
        spur2.setSchedule(schedule);
        spur2.setScheduleMode(Track.SEQUENTIAL);
        return schedule;
    }

    public static Automation createAutomation() {
        AutomationManager manager = InstanceManager.getDefault(AutomationManager.class);
        Assert.assertNotNull("test creation", manager);
        Automation automation = manager.newAutomation("TestAutomation");
        automation.setComment("test comment for automation");
        Assert.assertEquals(1, manager.getSize());

        AutomationItem item1 = automation.addItem();
        item1.setAction(new BuildTrainAction());
        item1.setTrain(new Train("trainId", "trainName1"));
        item1.setMessage("item1 OK message");
        item1.setMessageFail("item1 fail message");
        item1.setHaltFailureEnabled(false);

        AutomationItem item2 = automation.addItem();
        item2.setAction(new GotoAction());
        item2.setGotoAutomationItem(item1);

        AutomationItem item3 = automation.addItem();
        item3.setAction(new MoveTrainAction());
        item3.setTrain(new Train("trainId", "trainName2"));
        item3.setRouteLocation(new RouteLocation("id", new Location("id", "testLocationName")));

        AutomationItem item4 = automation.addItem();
        item4.setAction(new ActivateTrainScheduleAction());
        TrainSchedule trainSchedule =
                InstanceManager.getDefault(TrainScheduleManager.class).newSchedule("train schedule name");
        item4.setOther(trainSchedule);

        AutomationItem item5 = automation.addItem();
        item5.setAction(new RunAutomationAction());

        // 2nd automation created here
        Automation automationToRun = manager.newAutomation("A TestAutomation2");
        item5.setOther(automationToRun);
        item5.setMessage("item5 OK message");
        item5.setMessageFail("item5 fail message");
        item5.setHaltFailureEnabled(false);

        return automation;
    }

    public static BufferedReader getBufferedReader(File file) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8")); // NOI18N
        } catch (FileNotFoundException e) {

        } catch (UnsupportedEncodingException e) {

        }
        Assert.assertNotNull(in);
        return in;
    }

    // private final static Logger log = LoggerFactory.getLogger(JUnitOperationsUtil.class);
}
