package jmri.jmrit.operations.locations;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for the Operations Locations class Last manually cross-checked on
 * 20090131
 * <p>
 * Still to do: ScheduleItem: XML read/write Schedule: Register, List, XML
 * read/write Track: AcceptsDropTrain, AcceptsDropRoute Track:
 * AcceptsPickupTrain, AcceptsPickupRoute Track: CheckScheduleValid Track: XML
 * read/write Location: Track support <-- I am here Location: XML read/write
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class TrackTest extends OperationsTestCase {

    // test Track class
    // test Track public constants
    @Test
    public void testTrackConstants() {
        Assert.assertEquals("Location Track Constant ANY", "Any", Track.ANY);
        Assert.assertEquals("Location Track Constant TRAINS", "trains", Track.TRAINS);
        Assert.assertEquals("Location Track Constant ROUTES", "routes", Track.ROUTES);

        Assert.assertEquals("Location Track Constant STAGING", "Staging", Track.STAGING);
        Assert.assertEquals("Location Track Constant INTERCHANGE", "Interchange", Track.INTERCHANGE);
        Assert.assertEquals("Location track Constant YARD", "Yard", Track.YARD);
        Assert.assertEquals("Location Track Constant SIDING", "Siding", Track.SPUR);

        Assert.assertEquals("Location Track Constant EAST", 1, Track.EAST);
        Assert.assertEquals("Location Track Constant WEST", 2, Track.WEST);
        Assert.assertEquals("Location track Constant NORTH", 4, Track.NORTH);
        Assert.assertEquals("Location Track Constant SOUTH", 8, Track.SOUTH);

        Assert.assertEquals("Location Track Constant ALLROADS", "All", Track.ALL_ROADS);
        Assert.assertEquals("Location Track Constant INCLUDEROADS", "Include", Track.INCLUDE_ROADS);
        Assert.assertEquals("Location track Constant EXCLUDEROADS", "Exclude", Track.EXCLUDE_ROADS);

        Assert.assertEquals("Location Track Constant TYPES_CHANGED_PROPERTY", "trackRollingStockTypes",
                Track.TYPES_CHANGED_PROPERTY);
        Assert.assertEquals("Location Track Constant ROADS_CHANGED_PROPERTY", "trackRoads",
                Track.ROADS_CHANGED_PROPERTY);
        Assert.assertEquals("Location track Constant SCHEDULE_CHANGED_PROPERTY", "trackScheduleChange",
                Track.SCHEDULE_CHANGED_PROPERTY);
        Assert.assertEquals("Location track Constant DISPOSE_CHANGED_PROPERTY", "trackDispose",
                Track.DISPOSE_CHANGED_PROPERTY);
    }

    // test Track attributes
    @Test
    public void testTrackAttributes() {
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        Track t = new Track("Test id", "Test Name", "Test Type", l);
        Assert.assertEquals("Location Track id", "Test id", t.getId());
        Assert.assertEquals("Location Track Name", "Test Name", t.getName());
        Assert.assertEquals("Location Track Type", "Test Type", t.getTrackType());
        Assert.assertEquals("Location", l, t.getLocation());

        t.setName("New Test Name");
        Assert.assertEquals("Location Track set Name", "New Test Name", t.getName());

        t.setComment("New Test Comment");
        Assert.assertEquals("Location Track set Comment", "New Test Comment", t.getComment());

        t.setMoves(40);
        Assert.assertEquals("Location Track Moves", 40, t.getMoves());

        t.setLength(400);
        Assert.assertEquals("Location Track Length", 400, t.getLength());

        t.setReserved(200);
        Assert.assertEquals("Location Track Reserved", 200, t.getReserved());

        t.setUsedLength(100);
        Assert.assertEquals("Location Track Used Length", 100, t.getUsedLength());

        t.setTrainDirections(Track.NORTH);
        Assert.assertEquals("Location Track Direction North", Track.NORTH, t.getTrainDirections());

        t.setTrainDirections(Track.SOUTH);
        Assert.assertEquals("Location Track Direction South", Track.SOUTH, t.getTrainDirections());

        t.setTrainDirections(Track.EAST);
        Assert.assertEquals("Location Track Direction East", Track.EAST, t.getTrainDirections());

        t.setTrainDirections(Track.WEST);
        Assert.assertEquals("Location Track Direction West", Track.WEST, t.getTrainDirections());

        t.setTrainDirections(Track.NORTH + Track.SOUTH);
        Assert.assertEquals("Location Track Direction North+South", Track.NORTH + Track.SOUTH, t.getTrainDirections());

        t.setTrainDirections(Track.EAST + Track.WEST);
        Assert.assertEquals("Location Track Direction East+West", Track.EAST + Track.WEST, t.getTrainDirections());

        t.setTrainDirections(Track.NORTH + Track.SOUTH + Track.EAST + Track.WEST);
        Assert.assertEquals("Location Track Direction North+South+East+West", Track.NORTH + Track.SOUTH + Track.EAST
                + Track.WEST, t.getTrainDirections());

        t.setRoadOption("New Test Road Option");
        Assert.assertEquals("Location Track set Road Option", "New Test Road Option", t.getRoadOption());

        t.setDropOption("New Test Drop Option");
        Assert.assertEquals("Location Track set Drop Option", "New Test Drop Option", t.getDropOption());

        t.setPickupOption("New Test Pickup Option");
        Assert.assertEquals("Location Track set Pickup Option", "New Test Pickup Option", t.getPickupOption());
    }

    // test Track car support
    @Test
    public void testTrackCarSupport() {
        Location l = new Location("Location Test Car id", "Location Test Name");
        Track t = new Track("Test id", "Test Name", "Test Type", l);
        Assert.assertEquals("Location Track Car id", "Test id", t.getId());
        Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
        Assert.assertEquals("Location Track Car Type", "Test Type", t.getTrackType());
        Assert.assertEquals("Location", l, t.getLocation());

        Assert.assertEquals("Location Track Car Start Used Length", 0, t.getUsedLength());
        Assert.assertEquals("Location Track Car Start Number of Rolling Stock", 0, t.getNumberRS());
        Assert.assertEquals("Location Track Car Start Number of Cars", 0, t.getNumberCars());
        Assert.assertEquals("Location Track Car Start Number of Engines", 0, t.getNumberEngines());

        Car c1 = new Car("TESTROAD", "TESTNUMBER1");
        c1.setLength("40");
        t.addRS(c1);

        Assert.assertEquals("Location Track Car First Number of Rolling Stock", 1, t.getNumberRS());
        Assert.assertEquals("Location Track Car First Number of Cars", 1, t.getNumberCars());
        Assert.assertEquals("Location Track Car First Number of Engines", 0, t.getNumberEngines());
        Assert.assertEquals("Location Track Car First Used Length", 40 + 4, t.getUsedLength()); // Drawbar length is 4

        Car c2 = new Car("TESTROAD", "TESTNUMBER2");
        c2.setLength("33");
        t.addRS(c2);

        Assert.assertEquals("Location Track Car 2nd Number of Rolling Stock", 2, t.getNumberRS());
        Assert.assertEquals("Location Track Car 2nd Number of Cars", 2, t.getNumberCars());
        Assert.assertEquals("Location Track Car 2nd Number of Engines", 0, t.getNumberEngines());
        Assert.assertEquals("Location Track Car 2nd Used Length", 40 + 4 + 33 + 4, t.getUsedLength());

        jmri.jmrit.operations.rollingstock.engines.Engine e1 = new jmri.jmrit.operations.rollingstock.engines.Engine(
                "TESTROAD", "TESTNUMBERE1");
        e1.setModel("E8"); // Default length == 70
        t.addRS(e1);

        Assert.assertEquals("Location Track Car 3rd Number of Rolling Stock", 3, t.getNumberRS());
        Assert.assertEquals("Location Track Car 3rd Number of Cars", 2, t.getNumberCars());
        Assert.assertEquals("Location Track Car 3rd Number of Engines", 1, t.getNumberEngines());
        Assert.assertEquals("Location Track Car 3rd Used Length", 40 + 4 + 33 + 4 + 70 + 4, t.getUsedLength());

        Car c3 = new Car("TESTROAD", "TESTNUMBER3");
        c3.setLength("50");
        t.addRS(c3);

        Assert.assertEquals("Location Track Car 4th Number of Rolling Stock", 4, t.getNumberRS());
        Assert.assertEquals("Location Track Car 4th Number of Cars", 3, t.getNumberCars());
        Assert.assertEquals("Location Track Car 4th Number of Engines", 1, t.getNumberEngines());
        Assert.assertEquals("Location Track Car 4th Used Length", 40 + 4 + 33 + 4 + 70 + 4 + 50 + 4, t.getUsedLength());

        Engine e2 = new Engine("TESTROAD", "TESTNUMBERE2");
        e2.setModel("E8"); // Default length == 70
        t.addRS(e2);

        Assert.assertEquals("Location Track Car 5th Number of Rolling Stock", 5, t.getNumberRS());
        Assert.assertEquals("Location Track Car 5th Number of Cars", 3, t.getNumberCars());
        Assert.assertEquals("Location Track Car 5th Number of Engines", 2, t.getNumberEngines());
        Assert.assertEquals("Location Track Car 5th Used Length", 40 + 4 + 33 + 4 + 70 + 4 + 50 + 4 + 70 + 4, t
                .getUsedLength()); // Drawbar length is 4

        t.deleteRS(c2);

        Assert.assertEquals("Location Track Car 6th Number of Rolling Stock", 4, t.getNumberRS());
        Assert.assertEquals("Location Track Car 6th Number of Cars", 2, t.getNumberCars());
        Assert.assertEquals("Location Track Car 6th Number of Engines", 2, t.getNumberEngines());
        Assert.assertEquals("Location Track Car 6th Used Length", 40 + 4 + 70 + 4 + 50 + 4 + 70 + 4, t.getUsedLength());

        t.deleteRS(c1);

        Assert.assertEquals("Location Track Car 7th Number of Rolling Stock", 3, t.getNumberRS());
        Assert.assertEquals("Location Track Car 7th Number of Cars", 1, t.getNumberCars());
        Assert.assertEquals("Location Track Car 7th Number of Engines", 2, t.getNumberEngines());
        Assert.assertEquals("Location Track Car 7th Used Length", 70 + 4 + 50 + 4 + 70 + 4, t.getUsedLength());

        t.deleteRS(e2);

        Assert.assertEquals("Location Track Car 8th Number of Rolling Stock", 2, t.getNumberRS());
        Assert.assertEquals("Location Track Car 8th Number of Cars", 1, t.getNumberCars());
        Assert.assertEquals("Location Track Car 8th Number of Engines", 1, t.getNumberEngines());
        Assert.assertEquals("Location Track Car 8th Used Length", 70 + 4 + 50 + 4, t.getUsedLength());

        t.deleteRS(e1);

        Assert.assertEquals("Location Track Car 9th Number of Rolling Stock", 1, t.getNumberRS());
        Assert.assertEquals("Location Track Car 9th Number of Cars", 1, t.getNumberCars());
        Assert.assertEquals("Location Track Car 9th Number of Engines", 0, t.getNumberEngines());
        Assert.assertEquals("Location Track Car 9th Used Length", 50 + 4, t.getUsedLength()); // Drawbar length is 4

        t.deleteRS(c3);

        Assert.assertEquals("Location Track Car Last Number of Rolling Stock", 0, t.getNumberRS());
        Assert.assertEquals("Location Track Car Last Number of Cars", 0, t.getNumberCars());
        Assert.assertEquals("Location Track Car Last Number of Engines", 0, t.getNumberEngines());
        Assert.assertEquals("Location Track Car Last Used Length", 0, t.getUsedLength()); // Drawbar length is 4
    }

    // test Track pickup support
    @Test
    public void testTrackPickUpSupport() {
        Location l = new Location("Location Test Pickup id", "Location Test Name");
        Track t = new Track("Test id", "Test Name", "Test Type", l);
        Assert.assertEquals("Location Track Car id", "Test id", t.getId());
        Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
        Assert.assertEquals("Location Track Car Type", "Test Type", t.getTrackType());
        Assert.assertEquals("Location", l, t.getLocation());

        Assert.assertEquals("Location Track Pick Ups Start", 0, t.getPickupRS());
        Car c1 = new Car("TESTROAD", "TESTNUMBER1");
        c1.setLength("40");

        t.addPickupRS(c1);
        Assert.assertEquals("Location Track Pick Ups 1st", 1, t.getPickupRS());

        t.addPickupRS(c1);
        Assert.assertEquals("Location Track Pick Ups 2nd", 2, t.getPickupRS());

        t.deletePickupRS(c1);
        Assert.assertEquals("Location Track Pick Ups 3rd", 1, t.getPickupRS());

        t.deletePickupRS(c1);
        Assert.assertEquals("Location Track Pick Ups 4th", 0, t.getPickupRS());
    }

    // test Track drop support
    @Test
    public void testTrackDropSupport() {
        Location l = new Location("Location Test Drop id", "Location Test Name");
        Track t = new Track("Test id", "Test Name", "Test Type", l);
        Assert.assertEquals("Location Track Car id", "Test id", t.getId());
        Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
        Assert.assertEquals("Location Track Car Type", "Test Type", t.getTrackType());
        Assert.assertEquals("Location", l, t.getLocation());

        Assert.assertEquals("Location Track Drops Start", 0, t.getDropRS());
        Assert.assertEquals("Location Track Drops Start Reserved", 0, t.getReserved());

        Car c1 = new Car("TESTROAD", "TESTNUMBER1");
        c1.setLength("40");
        t.addDropRS(c1);
        Assert.assertEquals("Location Track Drops 1st", 1, t.getDropRS());
        Assert.assertEquals("Location Track Drops 1st Reserved", 40 + 4, t.getReserved());

        Car c2 = new Car("TESTROAD", "TESTNUMBER2");
        c2.setLength("50");
        t.addDropRS(c2);
        Assert.assertEquals("Location Track Drops 2nd", 2, t.getDropRS());
        Assert.assertEquals("Location Track Drops 2nd Reserved", 40 + 4 + 50 + 4, t.getReserved());

        t.deleteDropRS(c2);
        Assert.assertEquals("Location Track Drops 3rd", 1, t.getDropRS());
        Assert.assertEquals("Location Track Drops 3rd Reserved", 40 + 4, t.getReserved());

        t.deleteDropRS(c1);
        Assert.assertEquals("Location Track Drops 4th", 0, t.getDropRS());
        Assert.assertEquals("Location Track Drops 4th Reserved", 0, t.getReserved());
    }

    // test Track typename support
    @Test
    public void testTrackTypeNameSupport() {
        Location l = new Location("Location Test Name id", "Location Test Name");
        Track t = new Track("Test id", "Test Name", "Test Type", l);
        Assert.assertEquals("Location Track id", "Test id", t.getId());
        Assert.assertEquals("Location Track Name", "Test Name", t.getName());
        Assert.assertEquals("Location Track Type", "Test Type", t.getTrackType());
        Assert.assertEquals("Location", l, t.getLocation());

        /* Test Type Name */
        Assert.assertEquals("Location Track Accepts Type Name undefined", false, t.acceptsTypeName("TestTypeName"));

        t.addTypeName("TestTypeName");
        Assert.assertEquals("Location Track Accepts Type Name defined", false, t.acceptsTypeName("TestTypeName"));

        // now add to car types
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.addName("TestTypeName");
        t.addTypeName("TestTypeName");
        Assert.assertEquals("Location Track Accepts Type Name defined after ct", false, t
                .acceptsTypeName("TestTypeName"));

        // location must also accept the same type
        l.addTypeName("TestTypeName");
        Assert.assertEquals("Location Track Accepts Type Name defined after location", true, t
                .acceptsTypeName("TestTypeName"));

        t.deleteTypeName("TestTypeName");
        Assert.assertEquals("Location Track Accepts Type Name deleted", false, t.acceptsTypeName("TestTypeName"));

        /* Needed so later tests will behave correctly */
        ct.deleteName("TestTypeName");

        ct.addName("Baggager");
        t.addTypeName("Baggager");
        l.addTypeName("Baggager");

        Assert.assertEquals("Location Track Accepts Type Name Baggager", true, t.acceptsTypeName("Baggager"));

        /* Test Road Name */
        t.setRoadOption(Track.INCLUDE_ROADS);
        Assert.assertEquals("Location Track set Road Option INCLUDEROADS", "Include", t.getRoadOption());

        Assert.assertEquals("Location Track Accepts Road Name undefined", false, t.acceptsRoadName("TestRoadName"));

        t.addRoadName("TestRoadName");
        Assert.assertEquals("Location Track Accepts Road Name defined", true, t.acceptsRoadName("TestRoadName"));

        t.addRoadName("TestOtherRoadName");
        Assert.assertEquals("Location Track Accepts Road Name other defined", true, t.acceptsRoadName("TestRoadName"));

        t.deleteRoadName("TestRoadName");
        Assert.assertEquals("Location Track Accepts Road Name deleted", false, t.acceptsRoadName("TestRoadName"));

        t.setRoadOption(Track.ALL_ROADS);
        Assert.assertEquals("Location Track set Road Option AllROADS", "All", t.getRoadOption());
        Assert.assertEquals("Location Track Accepts All Road Names", true, t.acceptsRoadName("TestRoadName"));

        t.setRoadOption(Track.EXCLUDE_ROADS);
        Assert.assertEquals("Location Track set Road Option EXCLUDEROADS", "Exclude", t.getRoadOption());
        Assert.assertEquals("Location Track Excludes Road Names", true, t.acceptsRoadName("TestRoadName"));

        t.addRoadName("TestRoadName");
        Assert.assertEquals("Location Track Excludes Road Names 2", false, t.acceptsRoadName("TestRoadName"));

        /* Test Drop IDs */
        Assert.assertEquals("Location Track Accepts Drop ID undefined", false, t.containsDropId("TestDropId"));

        t.addDropId("TestDropId");
        Assert.assertEquals("Location Track Accepts Drop ID defined", true, t.containsDropId("TestDropId"));

        t.addDropId("TestOtherDropId");
        Assert.assertEquals("Location Track Accepts Drop ID other defined", true, t.containsDropId("TestDropId"));

        t.deleteDropId("TestDropId");
        Assert.assertEquals("Location Track Accepts Drop ID deleted", false, t.containsDropId("TestDropId"));

        /* Test Pickup IDs */
        Assert.assertEquals("Location Track Accepts Pickup ID undefined", false, t.containsPickupId("TestPickupId"));

        t.addPickupId("TestPickupId");
        Assert.assertEquals("Location Track Accepts Pickup ID defined", true, t.containsPickupId("TestPickupId"));

        t.addPickupId("TestOtherPickupId");
        Assert.assertEquals("Location Track Accepts Pickup ID other defined", true, t.containsPickupId("TestPickupId"));

        t.deletePickupId("TestPickupId");
        Assert.assertEquals("Location Track Accepts Pickup ID deleted", false, t.containsPickupId("TestPickupId"));
    }

    // test Track schedule support
    @Test
    public void testTrackScheduleSupport() {
        Location l = new Location("Location Test Schedule id", "Location Test Name");
        Track t = new Track("Test id", "Test Name", Track.SPUR, l);
        Assert.assertEquals("Location Track Car id", "Test id", t.getId());
        Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
        Assert.assertEquals("Location Track Car Type", Track.SPUR, t.getTrackType());
        Assert.assertEquals("Location", l, t.getLocation());

        t.setScheduleId("Test Schedule Id");
        Assert.assertEquals("Location Track set Schedule Name", "Test Schedule Id", t.getScheduleId());
        t.setScheduleItemId("Test Schedule Item Id");
        Assert.assertEquals("Location Track set Schedule Item Id", "Test Schedule Item Id", t.getScheduleItemId());
        t.setScheduleCount(2);
        Assert.assertEquals("Location Track set Schedule Count", 2, t.getScheduleCount());

        t.setScheduleMode(Track.SEQUENTIAL);
        Assert.assertEquals("Track mode sequential", Track.SEQUENTIAL, t.getScheduleMode());
        t.setScheduleMode(Track.MATCH);
        Assert.assertEquals("Track mode sequential", Track.MATCH, t.getScheduleMode());

    }

    // test Track load support
    @Test
    public void testTrackLoadSupport() {
        Location l = new Location("Location Test Load id", "Location Test Name");
        Track t = new Track("Test id", "Test Name", "Test Type", l);
        Assert.assertEquals("Location Track Car id", "Test id", t.getId());
        Assert.assertEquals("Location Track Car Name", "Test Name", t.getName());
        Assert.assertEquals("Location Track Car Type", "Test Type", t.getTrackType());
        Assert.assertEquals("Location", l, t.getLocation());

        /* Test Load Swapable */
        Assert.assertEquals("Location Track Load Swapable default", false, t.isLoadSwapEnabled());
        t.setLoadSwapEnabled(true);
        Assert.assertEquals("Location Track Load Swapable true", true, t.isLoadSwapEnabled());
        t.setLoadSwapEnabled(false);
        Assert.assertEquals("Location Track Load Swapable false", false, t.isLoadSwapEnabled());

        /* Test Remove Loads */
        Assert.assertEquals("Location Track Remove Loads default", false, t.isRemoveCustomLoadsEnabled());
        t.setRemoveCustomLoadsEnabled(true);
        Assert.assertEquals("Location Track Remove Loads true", true, t.isRemoveCustomLoadsEnabled());
        t.setRemoveCustomLoadsEnabled(false);
        Assert.assertEquals("Location Track Remove Loads false", false, t.isRemoveCustomLoadsEnabled());

        /* Test Add Loads */
        Assert.assertEquals("Location Track Add Loads default", false, t.isAddCustomLoadsEnabled());
        t.setAddCustomLoadsEnabled(true);
        Assert.assertEquals("Location Track Add Loads true", true, t.isAddCustomLoadsEnabled());
        t.setAddCustomLoadsEnabled(false);
        Assert.assertEquals("Location Track Add Loads false", false, t.isAddCustomLoadsEnabled());
    }

    @Test
    public void testSpurTrackOrder() {
        Location l = InstanceManager.getDefault(LocationManager.class).newLocation("TestOrder");
        Track t = l.addTrack("New track 1", Track.SPUR);
        Assert.assertEquals("Location", l, t.getLocation());

        // sidings and staging don't support this feature
        t.setServiceOrder(Track.FIFO);
        Assert.assertEquals("Track Order", Track.NORMAL, t.getServiceOrder());
        t.setServiceOrder(Track.LIFO);
        Assert.assertEquals("Track Order", Track.NORMAL, t.getServiceOrder());
    }

    @Test
    public void testYardTrackOrder() {
        Location l = InstanceManager.getDefault(LocationManager.class).newLocation("TestOrder");
        Track t = l.addTrack("New track 2", Track.YARD);
        Assert.assertEquals("Location", l, t.getLocation());

        // yards and interchanges do support this feature
        t.setServiceOrder(Track.FIFO);
        Assert.assertEquals("Track Order", Track.FIFO, t.getServiceOrder());
        t.setServiceOrder(Track.LIFO);
        Assert.assertEquals("Track Order", Track.LIFO, t.getServiceOrder());
    }

    @Test
    public void testStagingTrackOrder() {
        Location l = InstanceManager.getDefault(LocationManager.class).newLocation("TestOrder");
        Track t = l.addTrack("New track 3", Track.STAGING);
        Assert.assertEquals("Location", l, t.getLocation());

        t.setServiceOrder(Track.FIFO);
        Assert.assertEquals("Track Order", Track.NORMAL, t.getServiceOrder());
        t.setServiceOrder(Track.LIFO);
        Assert.assertEquals("Track Order", Track.NORMAL, t.getServiceOrder());
    }

    @Test
    public void testInterchangeTrackOrder() {
        Location l = InstanceManager.getDefault(LocationManager.class).newLocation("TestOrder");
        Track t = l.addTrack("New track 4", Track.INTERCHANGE);
        Assert.assertEquals("Location", l, t.getLocation());

        // yards and interchanges do support this feature
        t.setServiceOrder(Track.FIFO);
        Assert.assertEquals("Track Order", Track.FIFO, t.getServiceOrder());
        t.setServiceOrder(Track.LIFO);
        Assert.assertEquals("Track Order", Track.LIFO, t.getServiceOrder());
    }

}
