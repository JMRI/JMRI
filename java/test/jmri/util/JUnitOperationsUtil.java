package jmri.util;

import java.io.File;
import java.util.ResourceBundle; // for access operations keys directly.
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
        ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.JmritOperationsBundle");

        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        RouteManager rmanager = InstanceManager.getDefault(RouteManager.class);
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        EngineTypes et = InstanceManager.getDefault(EngineTypes.class);

        // register the car and engine types used
        ct.addName("Boxcar");
        ct.addName(rb.getString("Caboose"));
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

        // Set up a route of 3 locations: North End Staging (2 tracks),
        // North Industries (1 track), and South End Staging (2 tracks).
        Location locationNorthEnd = new Location("1", "North End");

        locationNorthEnd.setLocationOps(Location.STAGING);
        Assert.assertEquals("confirm default", DIRECTION_ALL, locationNorthEnd.getTrainDirections());
        
        locationNorthEnd.setSwitchListEnabled(true);
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

        Location locationNorthIndustries = new Location("2", "North Industries");
        locationNorthIndustries.setLocationOps(Location.NORMAL);
        locationNorthIndustries.setTrainDirections(DIRECTION_ALL);
        locationNorthIndustries.setSwitchListEnabled(true);
        lmanager.register(locationNorthIndustries);

        Track l2yard1 = new Track("2s1", "NI Yard", Track.YARD, locationNorthIndustries);
        l2yard1.setLength(432);
        l2yard1.setCommentBoth("Test comment for NI Yard drops and pulls");
        l2yard1.setCommentSetout("Test comment for NI Yard drops only");
        l2yard1.setCommentPickup("Test comment for NI Yard pulls only");

        locationNorthIndustries.register(l2yard1);

        Location locationSouthEnd = new Location("3", "South End");
        locationSouthEnd.setLocationOps(Location.STAGING);
        locationSouthEnd.setSwitchListEnabled(true);
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
        
        // Set up two cabooses and six box cars
        // Place Cabooses on Staging tracks
        // Place 4 Boxcars on Staging tracks
        // Place 2 Boxcars and Flat in yard
        Car c1 = createAndPlaceCar("CP", "C10099", rb.getString("Caboose"), "32", "AT", "1980", l1staging1, 23);
        c1.setCaboose(true);
        Car c2 = createAndPlaceCar("CP", "C20099", rb.getString("Caboose"), "32", "DAB", "1984", l1staging1, 54);
        c2.setCaboose(true);
        createAndPlaceCar("CP", "X10001", "Boxcar", "40", "DAB", "1984", l1staging1, 0);
        createAndPlaceCar("CP", "X10002", "Boxcar", "40", "AT", "1-84", l1staging1, 4444);
        createAndPlaceCar("CP", "X20001", "Boxcar", "40", "DAB", "1980", l1staging2, 0);
        createAndPlaceCar("CP", "X20002", "Boxcar", "40", "DAB", "1978", l1staging2, 0);
        createAndPlaceCar("CP", "777", "Flat", "50", "AT", "1990", l2yard1, 6);
        createAndPlaceCar("CP", "888", "Boxcar", "60", "DAB", "1985", l2yard1, 0);
        createAndPlaceCar("CP", "99", "Flat", "90", "AT", "6-80", l2yard1, 0);

        // Define the route.
        Route route1 = new Route("1", "Southbound Main Route");
        route1.setComment("Comment for route id 1");

        RouteLocation rl1 = new RouteLocation("1r1", locationNorthEnd);
        rl1.setSequenceId(1);
        rl1.setTrainDirection(RouteLocation.SOUTH);
        rl1.setMaxCarMoves(5);
        rl1.setMaxTrainLength(1000);
        rl1.setTrainIconX(25); // set the train icon coordinates
        rl1.setTrainIconY(25);
        rl1.setComment("Test route location comment for North End");

        RouteLocation rl2 = new RouteLocation("1r2", locationNorthIndustries);
        rl2.setSequenceId(2);
        rl2.setTrainDirection(RouteLocation.SOUTH);
        // test for only 1 pickup and 1 drop
        rl2.setMaxCarMoves(2);
        rl2.setMaxTrainLength(1000);
        rl2.setTrainIconX(75); // set the train icon coordinates

        rl2.setTrainIconY(25);

        RouteLocation rl3 = new RouteLocation("1r3", locationSouthEnd);
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
        train1.setComment("Test comment for train STF");
        
        // increase test coverage by providing a manifest logo for this train
        java.net.URL url = FileUtil.findURL("resources/logo.gif", FileUtil.Location.INSTALLED);        
        train1.setManifestLogoURL(url.getPath());
        
        tmanager.register(train1);

        Train train2 = new Train("2", "SFF");
        train2.deleteTypeName("Boxcar");
        train2.deleteTypeName("Flat");
        train2.setRoute(route1);
        train2.setDepartureTime("22", "45");
        tmanager.register(train2);
        
        // improve test coverage
        Setup.setPrintLocationCommentsEnabled(true);
        Setup.setPrintRouteCommentsEnabled(true);
        
    }
    
    public static Car createAndPlaceCar(String road, String number, String type, String length, Track track, int moves) {
        return createAndPlaceCar(road, number, type, length, "",
                "", track, moves);
    }
    
    public static Car createAndPlaceCar(String road, String number, String type, String length, String owner,
            String built, Track track, int moves) {

        CarManager cmanager = InstanceManager.getDefault(CarManager.class);

        Car car = cmanager.newCar(road, number);
        car.setTypeName(type);
        car.setLength(length);
        car.setOwner(owner);
        car.setBuilt(built);
        car.setMoves(moves);

        if (track != null) {
            Assert.assertEquals("place car", Track.OKAY, car.setLocation(track.getLocation(), track));
        }

        return car;
    }

    // private final static Logger log = LoggerFactory.getLogger(JUnitOperationsUtil.class);
}
