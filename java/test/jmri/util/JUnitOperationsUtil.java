package jmri.util;

import java.io.File;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.RollingStockLogger;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarColors;
import jmri.jmrit.operations.rollingstock.cars.CarLengths;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Consist;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineLengths;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineModels;
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
     * Reset the OperationsManager and set the files location for operations
     * file used during tests.
     */
    public static void resetOperationsManager() {

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
        InstanceManager.getDefault(LocationManagerXml.class).setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
        InstanceManager.getDefault(TrainManagerXml.class).setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

        // delete operations directory and all contents
        File file = new File(OperationsXml.getFileLocation(), OperationsSetupXml.getOperationsDirectoryName());
        FileUtil.delete(file);
        // create an empty operations directory
        FileUtil.createDirectory(file);

        // the following .dispose() calls are likely not needed
        // since new instances of these managers are recreated for each test
        InstanceManager.getDefault(TrainManager.class).dispose();
        InstanceManager.getDefault(LocationManager.class).dispose();
        InstanceManager.getDefault(RouteManager.class).dispose();
        InstanceManager.getDefault(ScheduleManager.class).dispose();
        InstanceManager.getDefault(CarTypes.class).dispose();
        InstanceManager.getDefault(CarColors.class).dispose();
        InstanceManager.getDefault(CarLengths.class).dispose();
        InstanceManager.getDefault(CarLoads.class).dispose();
        InstanceManager.getDefault(CarRoads.class).dispose();
        InstanceManager.getDefault(CarManager.class).dispose();

        InstanceManager.getDefault(RollingStockLogger.class).dispose();

        // dispose of the manager first, because otherwise
        // the models go away.
        InstanceManager.getDefault(EngineManager.class).dispose();
        InstanceManager.getDefault(EngineModels.class).dispose();
        InstanceManager.getDefault(EngineLengths.class).dispose();
    }

    /**
     * Populate the Operations Managers with a common set of data for tests.
     */
    public static void initOperationsData() {

        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        EngineTypes et = InstanceManager.getDefault(EngineTypes.class);

        // register the car and engine types used
        ct.addName("Boxcar");
        ct.addName("Caboose");
        ct.addName("Flat");
        et.addName("Diesel");

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

        // Set up two cabooses and six box cars
        Car c1 = new Car("CP", "C10099");
        c1.setTypeName("Caboose");
        c1.setLength("32");
        c1.setMoves(23);
        c1.setOwner("AT");
        c1.setBuilt("1980");
        c1.setCaboose(true);
        cmanager.register(c1);

        Car c2 = new Car("CP", "C20099");
        c2.setTypeName("Caboose");
        c2.setLength("32");
        c2.setMoves(54);
        c2.setOwner("DAB");
        c2.setBuilt("1984");
        c2.setCaboose(true);
        cmanager.register(c2);

        Car c3 = new Car("CP", "X10001");
        c3.setTypeName("Boxcar");
        c3.setLength("40");
        c3.setOwner("DAB");
        c3.setBuilt("1984");
        cmanager.register(c3);

        Car c4 = new Car("CP", "X10002");
        c4.setTypeName("Boxcar");
        c4.setLength("40");
        c4.setOwner("AT");
        c4.setBuilt("1-84");
        c4.setMoves(4444);
        cmanager.register(c4);

        Car c5 = new Car("CP", "X20001");
        c5.setTypeName("Boxcar");
        c5.setLength("40");
        c5.setOwner("DAB");
        c5.setBuilt("1980");
        cmanager.register(c5);

        Car c6 = new Car("CP", "X20002");
        c6.setTypeName("Boxcar");
        c6.setLength("40");
        c6.setOwner("DAB");
        c6.setBuilt("1978");
        cmanager.register(c6);

        Car c7 = new Car("CP", "777");
        c7.setTypeName("Flat");
        c7.setLength("50");
        c7.setOwner("AT");
        c7.setBuilt("1990");
        c7.setMoves(6);
        cmanager.register(c7);

        Car c8 = new Car("CP", "888");
        c8.setTypeName("Boxcar");
        c8.setLength("60");
        c8.setOwner("DAB");
        c8.setBuilt("1985");
        cmanager.register(c8);

        Car c9 = new Car("CP", "99");
        c9.setTypeName("Flat");
        c9.setLength("90");
        c9.setOwner("AT");
        c9.setBuilt("6-80");
        cmanager.register(c9);

        // Set up a route of 3 locations: North End Staging (2 tracks),
        // North Industries (1 track), and South End Staging (2 tracks).
        Location l1 = new Location("1", "North End");

        l1.setLocationOps(Location.STAGING);
        l1.setTrainDirections(DIRECTION_ALL);
        l1.setSwitchListEnabled(true);
        lmanager.register(l1);

        Track l1s1 = new Track("1s1", "North End 1", Track.STAGING, l1);
        l1s1.setLength(300);
        l1s1.setTrainDirections(DIRECTION_ALL);
        l1s1.setRoadOption(Track.ALL_ROADS);
        l1s1.setDropOption(Track.ANY);
        l1s1.setPickupOption(Track.ANY);

        Track l1s2 = new Track("1s2", "North End 2", Track.STAGING, l1);
        l1s2.setLength(400);

        l1s2.setTrainDirections(DIRECTION_ALL);
        l1s2.setRoadOption(Track.ALL_ROADS);
        l1s2.setDropOption(Track.ANY);
        l1s2.setPickupOption(Track.ANY);

        l1.addTrack("North End 1", Track.STAGING);
        l1.addTrack("North End 2", Track.STAGING);
        l1.register(l1s1);
        l1.register(l1s2);

        Location l2 = new Location("2", "North Industries");
        l2.setLocationOps(Location.NORMAL);
        l2.setTrainDirections(DIRECTION_ALL);
        l2.setSwitchListEnabled(true);
        lmanager.register(l2);

        Track l2s1 = new Track("2s1", "NI Yard", Track.YARD, l2);
        l2s1.setLength(432);
        l2s1.setTrainDirections(DIRECTION_ALL);

        l2.register(l2s1);

        Location l3 = new Location("3", "South End");
        l3.setLocationOps(Location.STAGING);
        l3.setTrainDirections(DIRECTION_ALL);
        l3.setSwitchListEnabled(true);
        lmanager.register(l3);

        Track l3s1 = new Track("3s1", "South End 1", Track.STAGING, l3);
        l3s1.setLength(300);
        l3s1.setTrainDirections(DIRECTION_ALL);
        l3s1.setRoadOption(Track.ALL_ROADS);
        l3s1.setDropOption(Track.ANY);
        l3s1.setPickupOption(Track.ANY);

        Track l3s2 = new Track("3s2", "South End 2", Track.STAGING, l3);
        l3s2.setLength(401);
        l3s2.setTrainDirections(DIRECTION_ALL);
        l3s2.setRoadOption(Track.ALL_ROADS);
        l3s2.setDropOption(Track.ANY);
        l3s2.setPickupOption(Track.ANY);

        l3.addTrack("South End 1", Track.STAGING);
        l3.addTrack("South End 2", Track.STAGING);
        l3.register(l3s1);
        l3.register(l3s2);

        // Place 4 Boxcars on Staging tracks
        c3.setLocation(l1, l1s1);
        c4.setLocation(l1, l1s1);
        c5.setLocation(l1, l1s2);
        c6.setLocation(l1, l1s2);

        // Place 2 Boxcars and Flat in yard
        c7.setLocation(l2, l2s1);
        c8.setLocation(l2, l2s1);
        c9.setLocation(l2, l2s1);

        // Place Cabooses on Staging tracks
        c1.setLocation(l1, l1s1);

        // Define the route.
        Route route1 = new Route("1", "Southbound Main Route");

        RouteLocation rl1 = new RouteLocation("1r1", l1);
        rl1.setSequenceId(1);
        rl1.setTrainDirection(RouteLocation.SOUTH);
        rl1.setMaxCarMoves(5);
        rl1.setMaxTrainLength(1000);
        rl1.setTrainIconX(25); // set the train icon coordinates
        rl1.setTrainIconY(25);

        RouteLocation rl2 = new RouteLocation("1r2", l2);
        rl2.setSequenceId(2);
        rl2.setTrainDirection(RouteLocation.SOUTH);
        // test for only 1 pickup and 1 drop
        rl2.setMaxCarMoves(2);
        rl2.setMaxTrainLength(1000);
        rl2.setTrainIconX(75); // set the train icon coordinates

        rl2.setTrainIconY(25);

        RouteLocation rl3 = new RouteLocation("1r3", l3);
        rl3.setSequenceId(3);
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
        train1.setRoadOption(Train.ALL_ROADS);
        train1.setRoute(route1);
        train1.setDepartureTime("6", "5");
        tmanager.register(train1);

        Train train2 = new Train("2", "SFF");
        // there are boxcars waiting in staging so build should fail
        train2.deleteTypeName("Boxcar");
        train2.deleteTypeName("Flat");
        train2.setRoute(route1);
        train2.setDepartureTime("22", "45");
        tmanager.register(train2);
    }

    // private final static Logger log = LoggerFactory.getLogger(JUnitOperationsUtil.class);
}
