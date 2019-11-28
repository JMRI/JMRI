package jmri.jmrit.operations.locations;

import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleItem;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import org.junit.Assert;
import org.junit.Before;
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
public class LocationTest extends OperationsTestCase {

    // test Location Class (part one)
    // test Location creation
    @Test
    public void testCreate() {
        Location l = new Location("Test id", "Test Name");
        Assert.assertEquals("Location id", "Test id", l.getId());
        Assert.assertEquals("Location Name", "Test Name", l.getName());
        l.setName("New Test Name");
        Assert.assertEquals("New Location Name", "New Test Name", l.getName());
        l.setComment("Test Location Comment");
        Assert.assertEquals("Location Comment", "Test Location Comment", l.getComment());
    }

    // test Location public constants
    @Test
    public void testLocationConstants() {
        Location l = new Location("Test id", "Test Name");
        Assert.assertEquals("Location id", "Test id", l.getId());
        Assert.assertEquals("Location Name", "Test Name", l.getName());

        Assert.assertEquals("Location Constant NORMAL", 1, Location.NORMAL);
        Assert.assertEquals("Location Constant STAGING", 2, Location.STAGING);

        Assert.assertEquals("Location Constant EAST", 1, Location.EAST);
        Assert.assertEquals("Location Constant WEST", 2, Location.WEST);
        Assert.assertEquals("Location Constant NORTH", 4, Location.NORTH);
        Assert.assertEquals("Location Constant SOUTH", 8, Location.SOUTH);

        Assert.assertEquals("Location Constant YARDLISTLENGTH_CHANGED_PROPERTY", "trackListLength",
                Location.TRACK_LISTLENGTH_CHANGED_PROPERTY);
        Assert.assertEquals("Location Constant TYPES_CHANGED_PROPERTY", "locationTypes",
                Location.TYPES_CHANGED_PROPERTY);
        Assert.assertEquals("Location Constant TRAINDIRECTION_CHANGED_PROPERTY", "locationTrainDirection",
                Location.TRAINDIRECTION_CHANGED_PROPERTY);
        Assert.assertEquals("Location Constant LENGTH_CHANGED_PROPERTY", "locationTrackLengths",
                Location.LENGTH_CHANGED_PROPERTY);
        Assert.assertEquals("Location Constant USEDLENGTH_CHANGED_PROPERTY", "locationUsedLength",
                Location.USEDLENGTH_CHANGED_PROPERTY);
        Assert.assertEquals("Location Constant NAME_CHANGED_PROPERTY", "locationName", Location.NAME_CHANGED_PROPERTY);
        Assert.assertEquals("Location Constant SWITCHLIST_CHANGED_PROPERTY", "switchList",
                Location.SWITCHLIST_CHANGED_PROPERTY);
        Assert.assertEquals("Location Constant DISPOSE_CHANGED_PROPERTY", "locationDispose",
                Location.DISPOSE_CHANGED_PROPERTY);
    }

    // test Locations class (part two)
    // test length attributes
    @Test
    public void testLengthAttributes() {
        Location l = new Location("Test id", "Test Name");
        Assert.assertEquals("Location id", "Test id", l.getId());
        Assert.assertEquals("Location Name", "Test Name", l.getName());

        l.setLength(400);
        Assert.assertEquals("Location Length", 400, l.getLength());

        l.setUsedLength(200);
        Assert.assertEquals("Location Used Length", 200, l.getUsedLength());
    }

    // test operation attributes
    @Test
    public void testOperationAttributes() {
        Location l = new Location("Test id", "Test Name");
        Assert.assertEquals("Location id", "Test id", l.getId());
        Assert.assertEquals("Location Name", "Test Name", l.getName());

        l.setLocationOps(Location.STAGING);
        Assert.assertEquals("Location Ops Staging", Location.STAGING, l.getLocationOps());

        l.setLocationOps(Location.NORMAL);
        Assert.assertEquals("Location Ops Normal", Location.NORMAL, l.getLocationOps());

        Assert.assertFalse("Location isn't staging", l.isStaging());
    }

    // test direction attributes
    @Test
    public void testDirectionAttributes() {
        Location l = new Location("Test id", "Test Name");
        Assert.assertEquals("Location id", "Test id", l.getId());
        Assert.assertEquals("Location Name", "Test Name", l.getName());

        l.setTrainDirections(Location.NORTH);
        Assert.assertEquals("Location Direction North", Location.NORTH, l.getTrainDirections());

        l.setTrainDirections(Location.SOUTH);
        Assert.assertEquals("Location Direction South", Location.SOUTH, l.getTrainDirections());

        l.setTrainDirections(Location.EAST);
        Assert.assertEquals("Location Direction East", Location.EAST, l.getTrainDirections());

        l.setTrainDirections(Location.WEST);
        Assert.assertEquals("Location Direction West", Location.WEST, l.getTrainDirections());

        l.setTrainDirections(Location.NORTH + Location.SOUTH);
        Assert.assertEquals("Location Direction North+South", Location.NORTH + Location.SOUTH, l.getTrainDirections());

        l.setTrainDirections(Location.EAST + Location.WEST);
        Assert.assertEquals("Location Direction East+West", Location.EAST + Location.WEST, l.getTrainDirections());

        l.setTrainDirections(Location.NORTH + Location.SOUTH + Location.EAST + Location.WEST);
        Assert.assertEquals("Location Direction North+South+East+West", Location.NORTH + Location.SOUTH + Location.EAST
                + Location.WEST, l.getTrainDirections());
    }

    // test car attributes
    @Test
    public void testCarAttributes() {
        Location l = new Location("Test id", "Test Name");
        Assert.assertEquals("Location id", "Test id", l.getId());
        Assert.assertEquals("Location Name", "Test Name", l.getName());

        l.setNumberRS(8);
        Assert.assertEquals("Location Number of Cars", 8, l.getNumberRS());
    }

    // test switchlist attributes
    @Test
    public void testSwitchlistAttributes() {
        Location l = new Location("Test id", "Test Name");
        Assert.assertEquals("Location id", "Test id", l.getId());
        Assert.assertEquals("Location Name", "Test Name", l.getName());

        l.setSwitchListEnabled(true);
        Assert.assertEquals("Location Switch List True", true, l.isSwitchListEnabled());

        l.setSwitchListEnabled(false);
        Assert.assertEquals("Location Switch List True", false, l.isSwitchListEnabled());
    }

    // test typename support
    @Test
    public void testTypeNameSupport() {
        // use LocationManager to allow replace car type to work properly
        Location l = InstanceManager.getDefault(LocationManager.class).newLocation("Test Name");
        Assert.assertEquals("Location Name", "Test Name", l.getName());

        Assert.assertEquals("Location Accepts Type Name undefined", false, l.acceptsTypeName("TestTypeName"));

        // l.addTypeName("TestTypeName");
        // Assert.assertEquals("Location Accepts Type Name defined", false, l.acceptsTypeName("TestTypeName"));
        // now add to car types
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.addName("TestTypeName");
        l.addTypeName("TestTypeName");
        Assert.assertEquals("Location Accepts Type Name defined", true, l.acceptsTypeName("TestTypeName"));

        l.deleteTypeName("TestTypeName");
        Assert.assertEquals("Location Accepts Type Name undefined2", false, l.acceptsTypeName("TestTypeName"));

        ct.addName("Baggage");
        ct.addName("BoxCar");
        ct.addName(Bundle.getMessage("Caboose"));
        ct.addName("Coal");
        ct.addName("Engine");
        ct.addName("Hopper");
        ct.addName("MOW");
        ct.addName("Passenger");
        ct.addName("Reefer");
        ct.addName("Stock");
        ct.addName("Tank Oil");

        l.addTypeName("Baggage");
        l.addTypeName("BoxCar");
        l.addTypeName(Bundle.getMessage("Caboose"));
        l.addTypeName("Coal");
        l.addTypeName("Engine");
        l.addTypeName("Hopper");
        l.addTypeName("MOW");
        l.addTypeName("Passenger");
        l.addTypeName("Reefer");
        l.addTypeName("Stock");
        l.addTypeName("Tank Oil");

        Track t = l.addTrack("new track", Track.SPUR);

        Assert.assertEquals("Location Accepts Type Name BoxCar", true, l.acceptsTypeName("BoxCar"));
        Assert.assertEquals("Location Accepts Type Name boxCar", false, l.acceptsTypeName("boxCar"));
        Assert.assertEquals("Location Accepts Type Name MOW", true, l.acceptsTypeName("MOW"));
        Assert.assertEquals("Location Accepts Type Name Caboose", true, l.acceptsTypeName(Bundle.getMessage("Caboose")));
        Assert.assertEquals("Location Accepts Type Name BoxCar", true, l.acceptsTypeName("BoxCar"));
        Assert.assertEquals("Location Accepts Type Name undefined3", false, l.acceptsTypeName("TestTypeName"));

        Assert.assertEquals("Track Accepts Type Name BoxCar", true, t.acceptsTypeName("BoxCar"));
        Assert.assertEquals("Track Accepts Type Name boxCar", false, t.acceptsTypeName("boxCar"));
        Assert.assertEquals("Track Accepts Type Name MOW", true, t.acceptsTypeName("MOW"));
        Assert.assertEquals("Track Accepts Type Name Caboose", true, t.acceptsTypeName(Bundle.getMessage("Caboose")));
        Assert.assertEquals("Track Accepts Type Name undefined3", false, t.acceptsTypeName("undefined"));

        t.addTypeName("Baggage");
        t.addTypeName("BoxCar");
        t.addTypeName(Bundle.getMessage("Caboose"));
        t.addTypeName("Coal");
        t.addTypeName("Engine");
        t.addTypeName("Hopper");
        t.addTypeName("MOW");
        t.addTypeName("Passenger");
        t.addTypeName("Reefer");
        t.addTypeName("Stock");
        t.addTypeName("Tank Oil");

        Assert.assertEquals("Track Accepts Type Name BoxCar", true, t.acceptsTypeName("BoxCar"));
        Assert.assertEquals("Track Accepts Type Name boxCar", false, t.acceptsTypeName("boxCar"));
        Assert.assertEquals("Track Accepts Type Name MOW", true, t.acceptsTypeName("MOW"));
        Assert.assertEquals("Track Accepts Type Name Caboose", true, t.acceptsTypeName(Bundle.getMessage("Caboose")));
        Assert.assertEquals("Track Accepts Type Name BoxCar", true, t.acceptsTypeName("BoxCar"));
        Assert.assertEquals("Track Accepts Type Name undefined3", false, t.acceptsTypeName("undefined"));

        // test replace
        // also test replace type in schedules
        ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);
        Schedule s = sm.newSchedule("newest schedule");
        ScheduleItem i1 = s.addItem("BoxCar");
        ScheduleItem i2 = s.addItem(Bundle.getMessage("Caboose"));

        Assert.assertEquals("ScheudleItem i1 Type BoxCar", "BoxCar", i1.getTypeName());
        Assert.assertEquals("ScheudleItem i2 Type Caboose", Bundle.getMessage("Caboose"), i2.getTypeName());

        ct.replaceName("BoxCar", "boxcar");

        Assert.assertFalse("Location Does Not Accepts Type Name BoxCar", l.acceptsTypeName("BoxCar"));
        Assert.assertTrue("Location Accepts Type Name boxcar", l.acceptsTypeName("boxcar"));
        Assert.assertFalse("Track Does Not Accepts Type Name BoxCar", l.acceptsTypeName("BoxCar"));
        Assert.assertTrue("Track Accepts Type Name boxcar", t.acceptsTypeName("boxcar"));
        Assert.assertEquals("ScheudleItem i1 Type boxcar", "boxcar", i1.getTypeName());
        Assert.assertEquals("Check ScheudleItem i2 Type Caboose", Bundle.getMessage("Caboose"), i2.getTypeName());

        // remove all schedules
        sm.dispose();
    }

    @Test
    public void testRoadNameSupport() {
        // use LocationManager to allow replace car road to work properly
        Location l = InstanceManager.getDefault(LocationManager.class).newLocation("Test Name 2");
        Assert.assertEquals("Location Name", "Test Name 2", l.getName());

        Track t = l.addTrack("new track", Track.SPUR);
        Assert.assertEquals("Location", l, t.getLocation());

        t.setRoadOption(Track.INCLUDE_ROADS);
        t.addRoadName("Test Road Name");
        t.addRoadName("Test Road Name 2");

        ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);
        Schedule s = sm.newSchedule("test schedule");
        ScheduleItem i1 = s.addItem("BoxCar");
        ScheduleItem i2 = s.addItem("BoxCar");
        i1.setRoadName("Test Road Name");
        i2.setRoadName("Test Road Name 2");

        Assert.assertTrue("track should accept road Test Road Name", t.acceptsRoadName("Test Road Name"));
        Assert.assertTrue("track should accept road Test Road Name 2", t.acceptsRoadName("Test Road Name 2"));
        Assert.assertFalse("track should Not accept road New Test Road Name", t.acceptsRoadName("New Test Road Name"));
        Assert.assertEquals("ScheudleItem i1 Road Test Road Name", "Test Road Name", i1.getRoadName());
        Assert.assertEquals("ScheudleItem i2 Road Test Road Name", "Test Road Name 2", i2.getRoadName());

        CarRoads cr = InstanceManager.getDefault(CarRoads.class);
        cr.replaceName("Test Road Name", "New Test Road Name");

        Assert.assertFalse("track should Not accept road Test Road Name", t.acceptsRoadName("Test Road Name"));
        Assert.assertTrue("track should accept road Test Road Name 2", t.acceptsRoadName("Test Road Name 2"));
        Assert.assertTrue("track should accept road New Test Road Name", t.acceptsRoadName("New Test Road Name"));
        Assert.assertEquals("ScheudleItem i1 Road Test Road Name", "New Test Road Name", i1.getRoadName());
        Assert.assertEquals("Check ScheudleItem i2 Road Test Road Name", "Test Road Name 2", i2.getRoadName());

        // remove all schedules
        sm.dispose();
    }

    // test pickup support
    @Test
    public void testPickUpSupport() {
        Location l = new Location("Test id", "Test Name");
        Assert.assertEquals("Location id", "Test id", l.getId());
        Assert.assertEquals("Location Name", "Test Name", l.getName());

        Assert.assertEquals("Location Pick Ups Start Condition", 0, l.getPickupRS());

        l.addPickupRS();
        Assert.assertEquals("Location Pick Up 1", 1, l.getPickupRS());

        l.addPickupRS();
        Assert.assertEquals("Location Pick Up second", 2, l.getPickupRS());

        l.deletePickupRS();
        Assert.assertEquals("Location Delete Pick Up", 1, l.getPickupRS());

        l.deletePickupRS();
        Assert.assertEquals("Location Delete Pick Up second", 0, l.getPickupRS());
    }

    // test drop support
    @Test
    public void testDropSupport() {
        Location l = new Location("Test id", "Test Name");
        Assert.assertEquals("Location id", "Test id", l.getId());
        Assert.assertEquals("Location Name", "Test Name", l.getName());

        Assert.assertEquals("Location Drop Start Condition", 0, l.getPickupRS());

        l.addDropRS();
        Assert.assertEquals("Location Drop 1", 1, l.getDropRS());

        l.addDropRS();
        Assert.assertEquals("Location Drop second", 2, l.getDropRS());

        l.deleteDropRS();
        Assert.assertEquals("Location Delete Drop", 1, l.getDropRS());

        l.deleteDropRS();
        Assert.assertEquals("Location Delete Drop second", 0, l.getDropRS());
    }

    // test car support
    @Test
    public void testCarSupport() {
        Location l = new Location("Test id", "Test Name");
        Assert.assertEquals("Location id", "Test id", l.getId());
        Assert.assertEquals("Location Name", "Test Name", l.getName());

        Assert.assertEquals("Location Used Length", 0, l.getUsedLength());
        Assert.assertEquals("Location Number of Cars", 0, l.getNumberRS());

        Car c1 = new Car("TESTROAD", "TESTNUMBER1");
        c1.setLength("40");
        l.addRS(c1);

        Assert.assertEquals("Location Number of Cars", 1, l.getNumberRS());
        Assert.assertEquals("Location Used Length one car", 44, l.getUsedLength()); // Drawbar length is 4

        Car c2 = new Car("TESTROAD", "TESTNUMBER2");
        c2.setLength("33");
        l.addRS(c2);

        Assert.assertEquals("Location Number of Cars", 2, l.getNumberRS());
        Assert.assertEquals("Location Used Length one car", 40 + 4 + 33 + 4, l.getUsedLength()); // Drawbar length is 4

        Car c3 = new Car("TESTROAD", "TESTNUMBER3");
        c3.setLength("50");
        l.addRS(c3);

        Assert.assertEquals("Location Number of Cars", 3, l.getNumberRS());
        Assert.assertEquals("Location Used Length one car", 40 + 4 + 33 + 4 + 50 + 4, l.getUsedLength()); // Drawbar
        // length is
        // 4

        l.deleteRS(c2);

        Assert.assertEquals("Location Number of Cars", 2, l.getNumberRS());
        Assert.assertEquals("Location Used Length one car", 40 + 4 + 50 + 4, l.getUsedLength()); // Drawbar length is 4

        l.deleteRS(c1);

        Assert.assertEquals("Location Number of Cars", 1, l.getNumberRS());
        Assert.assertEquals("Location Used Length one car", 50 + 4, l.getUsedLength()); // Drawbar length is 4

        l.deleteRS(c3);

        Assert.assertEquals("Location Number of Cars", 0, l.getNumberRS());
        Assert.assertEquals("Location Used Length one car", 0, l.getUsedLength()); // Drawbar length is 4
    }

    // test car duplicates support
    @Test
    public void testCarDuplicatesSupport() {
        Location l = new Location("Test id", "Test Name");
        Assert.assertEquals("Location id", "Test id", l.getId());
        Assert.assertEquals("Location Name", "Test Name", l.getName());

        Assert.assertEquals("Location Used Length", 0, l.getUsedLength());
        Assert.assertEquals("Location Number of Cars", 0, l.getNumberRS());

        Car c1 = new Car("TESTROAD", "TESTNUMBER1");
        c1.setLength("40");
        l.addRS(c1);

        Assert.assertEquals("Location Number of Cars", 1, l.getNumberRS());
        Assert.assertEquals("Location Used Length one car", 44, l.getUsedLength()); // Drawbar length is 4

        Car c2 = new Car("TESTROAD", "TESTNUMBER2");
        c2.setLength("33");
        l.addRS(c2);

        Assert.assertEquals("Location Number of Cars", 2, l.getNumberRS());
        Assert.assertEquals("Location Used Length one car", 40 + 4 + 33 + 4, l.getUsedLength()); // Drawbar length is 4

        l.addRS(c1);

        Assert.assertEquals("Location Number of Cars", 3, l.getNumberRS());
        Assert.assertEquals("Location Used Length one car", 40 + 4 + 33 + 4 + 40 + 4, l.getUsedLength()); // Drawbar
        // length is
        // 4

    }

    // test track priority
    @Test
    public void testTrackPriority() {
        LocationManager locMan = new LocationManager();
        Location l = locMan.newLocation("TestPriority Location");
        Track t1 = l.addTrack("Yard 1", Track.YARD);
        Track t2 = l.addTrack("Yard 2", Track.YARD);
        Track t3 = l.addTrack("Siding 1", Track.SPUR);
        Track t4 = l.addTrack("Siding 2", Track.SPUR);
        Track t5 = l.addTrack("Interchange 1", Track.INTERCHANGE);
        Track t6 = l.addTrack("Interchange 2", Track.INTERCHANGE);
        Track t7 = l.addTrack("Interchange 3", Track.INTERCHANGE);

        // set the priority bias
        t1.setMoves(12);
        t2.setMoves(14);
        t3.setMoves(18); // lowest priority
        t4.setMoves(11);
        t5.setMoves(10); // highest priority
        t6.setMoves(16);
        t7.setMoves(15);

        // get all tracks ids
        List<Track> tracks = l.getTrackByMovesList(null);

        Assert.assertEquals("number of tracks", 7, tracks.size());
        Assert.assertEquals("1st track", t5, tracks.get(0));
        Assert.assertEquals("2nd track", t4, tracks.get(1));
        Assert.assertEquals("3rd track", t1, tracks.get(2));
        Assert.assertEquals("4th track", t2, tracks.get(3));
        Assert.assertEquals("5th track", t7, tracks.get(4));
        Assert.assertEquals("6th track", t6, tracks.get(5));
        Assert.assertEquals("7th track", t3, tracks.get(6));

        // get interchange tracks ids
        tracks = l.getTrackByMovesList(Track.INTERCHANGE);

        Assert.assertEquals("number of tracks", 3, tracks.size());
        Assert.assertEquals("1st track", t5, tracks.get(0));
        Assert.assertEquals("2nd track", t7, tracks.get(1));
        Assert.assertEquals("3rd track", t6, tracks.get(2));

        // get siding tracks ids
        tracks = l.getTrackByMovesList(Track.SPUR);

        Assert.assertEquals("number of tracks", 2, tracks.size());
        Assert.assertEquals("1st track", t4, tracks.get(0));
        Assert.assertEquals("2nd track", t3, tracks.get(1));

        // get yard tracks ids
        tracks = l.getTrackByMovesList(Track.YARD);

        Assert.assertEquals("number of tracks", 2, tracks.size());
        Assert.assertEquals("1st track", t1, tracks.get(0));
        Assert.assertEquals("2nd track", t2, tracks.get(1));

        // tracks with schedules get priority
        Schedule sch = InstanceManager.getDefault(ScheduleManager.class).newSchedule("dummy schedule");
        t3.setSchedule(sch);

        // get all tracks ids
        tracks = l.getTrackByMovesList(null);

        Assert.assertEquals("number of tracks", 7, tracks.size());
        Assert.assertEquals("1st track", t3, tracks.get(0));
        Assert.assertEquals("2nd track", t5, tracks.get(1));
        Assert.assertEquals("3rd track", t4, tracks.get(2));
        Assert.assertEquals("4th track", t1, tracks.get(3));
        Assert.assertEquals("5th track", t2, tracks.get(4));
        Assert.assertEquals("6th track", t7, tracks.get(5));
        Assert.assertEquals("7th track", t6, tracks.get(6));

        // t4 has less moves than t3 so it will move up in priority
        t4.setSchedule(sch);

        // get all tracks ids
        tracks = l.getTrackByMovesList(null);

        Assert.assertEquals("number of tracks", 7, tracks.size());
        Assert.assertEquals("1st track", t4, tracks.get(0));
        Assert.assertEquals("2nd track", t3, tracks.get(1));
        Assert.assertEquals("3rd track", t5, tracks.get(2));
        Assert.assertEquals("4th track", t1, tracks.get(3));
        Assert.assertEquals("5th track", t2, tracks.get(4));
        Assert.assertEquals("6th track", t7, tracks.get(5));
        Assert.assertEquals("7th track", t6, tracks.get(6));

        // remove dummy schedule
        InstanceManager.getDefault(ScheduleManager.class).deregister(sch);

    }

    @Test
    public void testPlannedPickUps() {
        LocationManager locMan = new LocationManager();
        Location l = locMan.newLocation("TestPlannedPickUps Location");
        Track t1 = l.addTrack("Yard 1", Track.YARD);
        Track t3 = l.addTrack("Siding 1", Track.SPUR);
        Track t5 = l.addTrack("Interchange 1", Track.INTERCHANGE);

        // also test staging
        l = locMan.newLocation("TestPlannedPickUps Staging");
        Track t7 = l.addTrack("Staging 1", Track.STAGING);

        testPLannedPickUps(t1);
        testPLannedPickUps(t3);
        testPLannedPickUps(t5);
        testPLannedPickUps(t7);
    }

    private void testPLannedPickUps(Track t1) {
        Location l = t1.getLocation();
        t1.setLength(100);

        Car c1 = new Car("C", "1");
        c1.setLength("46");
        c1.setTypeName("Boxcar");

        Car c2 = new Car("C", "2");
        c2.setLength("46");
        c2.setTypeName("Boxcar");

        Car c5 = new Car("C", "5");
        c5.setLength("21");
        c5.setTypeName("Boxcar");

        Car c6 = new Car("C", "6");
        c6.setLength("21");
        c6.setTypeName("Boxcar");

        Car c7 = new Car("C", "7");
        c7.setLength("46");
        c7.setTypeName("Boxcar");

        Car c8 = new Car("C", "8");
        c8.setLength("21");
        c8.setTypeName("Boxcar");

        Car c9 = new Car("C", "9");
        c9.setLength("21");
        c9.setTypeName("Boxcar");

        Car c10 = new Car("C", "10");
        c10.setLength("21");
        c10.setTypeName("Boxcar");

        // fill the track completely
        Assert.assertEquals("Place C1", Track.OKAY, c1.setLocation(l, t1));
        Assert.assertEquals("Place C2", Track.OKAY, c2.setLocation(l, t1));
        Assert.assertEquals("Track t1 full", 100, t1.getUsedLength());

        // try to over load track, should fail
        String status = c8.setLocation(l, t1);
        // Assert.assertEquals("Place C8", Track.LENGTH + " 25 " + Setup.getLengthUnit().toLowerCase(), );
        Assert.assertTrue("Length issue", status.startsWith(Track.LENGTH));
        // try setting car's destination
        status = c8.setDestination(l, t1);
        // Assert.assertEquals("Set Destination C8", Track.LENGTH + " 25 " + Setup.getLengthUnit().toLowerCase(), c8
        // .setDestination(l, t1));
        Assert.assertTrue("Length issue", status.startsWith(Track.LENGTH));

        // now use planned pickup feature
        t1.setIgnoreUsedLengthPercentage(25); // ignore 25% of used track length
        Assert.assertEquals("Set Destination C5", Track.OKAY, c5.setDestination(l, t1));
        Assert.assertEquals("Track t1 reserved", 25, t1.getReserved()); // C5 destination is t1
        status = c6.setLocation(l, t1);
        // Assert.assertEquals("Place C6", Track.LENGTH + " 25 " + Setup.getLengthUnit().toLowerCase(), c6.setLocation(
        // l, t1));
        Assert.assertTrue("Length issue", status.startsWith(Track.LENGTH));
        Assert.assertEquals("Remove Destination C5", Track.OKAY, c5.setDestination(null, null));
        Assert.assertEquals("Place C6", Track.OKAY, c6.setLocation(l, t1));
        status = c5.setDestination(l, t1);
        // Assert.assertEquals("Set Destination C5", Track.LENGTH + " 25 " + Setup.getLengthUnit().toLowerCase(), c5
        // .setDestination(l, t1));
        Assert.assertTrue("Length issue", status.startsWith(Track.LENGTH));

        Assert.assertEquals("Track t1 now over loaded by 25%", 125, t1.getUsedLength());

        // now try 75% planned pick ups
        t1.setIgnoreUsedLengthPercentage(75); // ignore 75% of used track length
        Assert.assertEquals("Set Destination C7", Track.OKAY, c7.setDestination(l, t1));
        status = c8.setLocation(l, t1);
        // Assert.assertEquals("Place C8", Track.LENGTH + " 25 " + Setup.getLengthUnit().toLowerCase(), c8.setLocation(
        // l, t1));
        Assert.assertTrue("Length issue", status.startsWith(Track.LENGTH));
        Assert.assertEquals("Remove Destination C7", Track.OKAY, c7.setDestination(null, null));
        Assert.assertEquals("Place C8", Track.OKAY, c8.setLocation(l, t1));
        status = c7.setDestination(l, t1);
        // Assert.assertEquals("Set Destination C7", Track.LENGTH + " 50 " + Setup.getLengthUnit().toLowerCase(), c7
        // .setDestination(l, t1));
        Assert.assertTrue("Length issue", status.startsWith(Track.LENGTH));
        Assert.assertEquals("Set Destination C9", Track.OKAY, c9.setDestination(l, t1));

        Assert.assertEquals("Track t1 now over loaded by 50%", 150, t1.getUsedLength());
        Assert.assertEquals("Track t1 reserved", 25, t1.getReserved()); // C9 destination is t1

        // now try 100% planned pick ups
        t1.setIgnoreUsedLengthPercentage(100); // ignore 100% of used track length
        Assert.assertEquals("Set Destination C10", Track.OKAY, c10.setDestination(l, t1));
        Assert.assertEquals("Track t1 reserved", 50, t1.getReserved()); // C9 and C10 destination is t1
        Assert.assertEquals("Set Destination C10", Track.OKAY, c10.setDestination(null, null));
        Assert.assertEquals("Track t1 reserved", 25, t1.getReserved()); // C9 destination is t1
        Assert.assertEquals("Track t1 over loaded by 50%", 150, t1.getUsedLength());
        c10.setLength("22"); // make car one foot longer
        // and try again, should fail
        status = c10.setDestination(l, t1);
        // Assert.assertEquals("Set Destination C10", Track.LENGTH + " 26 " + Setup.getLengthUnit().toLowerCase(), c10
        // .setDestination(l, t1));
        Assert.assertTrue("Length issue", status.startsWith(Track.LENGTH));
        // remove c8 length 21+4 = 25
        Assert.assertEquals("remove C8", Track.OKAY, c8.setLocation(null, null));
        c10.setLength("46"); // make car 46+4 = 50 foot
        Assert.assertEquals("Set Destination C10", Track.OKAY, c10.setDestination(l, t1));
        Assert.assertEquals("Track t1 reserved", 75, t1.getReserved()); // C9 and c10 destination is t1
        Assert.assertEquals("Track t1 over loaded by 25%", 125, t1.getUsedLength());
        // shouldn't be able to place c8 on track, 75 feet or cars in bound, and 125 used, so track is full
        status = c8.setLocation(l, t1);
        // Assert.assertEquals("Place C8", Track.LENGTH + " 25 " + Setup.getLengthUnit().toLowerCase(), c8.setLocation(
        // l, t1));
        Assert.assertTrue("Length issue", status.startsWith(Track.LENGTH));
        status = c8.setDestination(l, t1);
        // Assert.assertEquals("Set Destination C8", Track.LENGTH + " 25 " + Setup.getLengthUnit().toLowerCase(), c8
        // .setDestination(l, t1));
        Assert.assertTrue("Length issue", status.startsWith(Track.LENGTH));
        // allow full length of track for in bound cars, c6 length 21+4 = 25
        Assert.assertEquals("remove C6", Track.OKAY, c6.setLocation(null, null));
        Assert.assertEquals("Track t1 reserved", 75, t1.getReserved()); // C9 and c10 destination is t1
        Assert.assertEquals("Track t1 full", 100, t1.getUsedLength());
        Assert.assertEquals("Set Destination C8", Track.OKAY, c8.setDestination(l, t1));
        Assert.assertEquals("Track t1 reserved", 100, t1.getReserved()); // C8, C9 and c10 destination is t1

        // test track "capacity" warning when track is spur with schedule
        // add schedule to track
        // ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);
        // Schedule s1 = sm.newSchedule("Schedule 1 Name");
        // s1.setComment("Schedule 1 Comment");
        // s1.addItem("Boxcar");
        // t1.setScheduleId(s1.getId());
        //
        // // use aggressive mode for spur testing
        // Setup.setBuildAggressive(true);
        //
        // // c1 already sitting on track t1
        // Assert.assertEquals("Place C1", Track.OKAY, c1.setLocation(l, t1));
        // // now disable planned pick ups for this track
        // t1.setIgnoreUsedLengthPercentage(0);
        // // only spurs with schedules can have a capacity issue
        // if (t1.getTrackType().equals(Track.SPUR))
        // Assert.assertEquals("Place C1", Track.CAPACITY, c1.setLocation(l, t1));
        // else
        // Assert.assertEquals("Place C1", Track.OKAY, c1.setLocation(l, t1));
    }

    @Override
    @Before
    public void setUp() {
        super.setUp();
        InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarTypes.class).addName("Boxcar");
    }

}
