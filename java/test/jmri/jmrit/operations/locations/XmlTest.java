package jmri.jmrit.operations.locations;

import java.awt.Point;
import java.io.File;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.divisions.Division;
import jmri.jmrit.operations.locations.divisions.DivisionManager;
import jmri.jmrit.operations.locations.schedules.Schedule;
import jmri.jmrit.operations.locations.schedules.ScheduleItem;
import jmri.jmrit.operations.locations.schedules.ScheduleManager;
import jmri.jmrit.operations.rollingstock.cars.CarTypes;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;

/**
 * Tests for the Operations Locations/Xml class Last manually cross-checked on
 * 20090131
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class XmlTest extends OperationsTestCase {

    /**
     * Test location Xml create and read support. Originally this was three test
     * that had to run in the order specified. Now changed on 8/29/2013 to be
     * one long test.
     *
     * @throws Exception exception
     */
    @Test
    public void testXMLCreate() throws Exception {
        LocationManager manager = InstanceManager.getDefault(LocationManager.class);
        manager.dispose();
        // dispose kills instance, so reload manager
        manager = InstanceManager.getDefault(LocationManager.class);

        // now load locations
        List<Location> locationList = manager.getLocationsByIdList();
        Assert.assertEquals("Starting Number of Locations", 0, locationList.size());
        Location l1 = manager.newLocation("Test Location 2");
        Location l2 = manager.newLocation("Test Location 1");
        Location l3 = manager.newLocation("Test Location 3");

        Track t1 = l1.addTrack("A Yard track 1", Track.YARD);
        Track t2 = l1.addTrack("A Spur track 2", Track.SPUR);
        Track t3 = l2.addTrack("An Interchange track 3", Track.INTERCHANGE);
        Track t4 = l3.addTrack("A Stage track 4", Track.STAGING);

        t1.addRoadName("Track 1 Road");
        t1.setRoadOption(Track.INCLUDE_ROADS);
        t2.addTypeName("Track 2 Type");
        t3.addRoadName("Track 3 Road");
        t3.setRoadOption(Track.EXCLUDE_ROADS);
        t4.addTypeName("Track 4 Type");
        
        // test icons
        l1.setTrainIconEast(new Point(124, 543));
        l1.setTrainIconWest(new Point(32, 52));
        l1.setTrainIconSouth(new Point(63, 81));
        l1.setTrainIconNorth(new Point(23, 632));
        l1.setTrainIconRangeX(45);
        l1.setTrainIconRangeY(54);
        
        // test default print
        l2.setDefaultPrinterName("Default Name For Printer");
        
        // test switch list state
        Setup.setSwitchListRealTime(false);
        
        // test load features
        t1.addLoadName("Track 1 Load");
        t1.setLoadOption(Track.INCLUDE_LOADS);
        t3.addLoadName("Track 3 Load");
        t3.setLoadOption(Track.EXCLUDE_LOADS);
        
        t4.addShipLoadName("Track 4 Ship Load");
        t4.setShipLoadOption(Track.INCLUDE_LOADS);
        
        // test blocking order
        t3.setBlockingOrder(2);
        
        // test drop ids
        t3.setDropOption(Track.TRAINS);
        t3.addDropId("Track 3 train id");
        
        // test pick up ids
        t3.setPickupOption(Track.ROUTES);
        t3.addPickupId("Track 3 route id");
        
        // test load options
        t4.setLoadSwapEnabled(true);
        
        // test block option
        t4.setBlockCarsEnabled(true);
        
        // test service order
        t3.setServiceOrder(Track.FIFO);

        // test pool features
        Pool pool = l1.addPool("Test Pool");
        t1.setPool(pool);
        t1.setMinimumLength(321);
        t2.setPool(pool);
        t2.setMinimumLength(123);

        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        ct.addName("Boxcar");
        ct.addName("boxCar");
        ct.addName("BoxCar");
        ct.addName("Track 2 Type");
        ct.addName("Track 4 Type");

        // also test schedules
        ScheduleManager sm = InstanceManager.getDefault(ScheduleManager.class);
        Schedule s1 = sm.newSchedule("Schedule 1 Name");
        s1.setComment("Schedule 1 Comment");
        ScheduleItem s1i1 = s1.addItem("Boxcar");
        s1i1.setRoadName("Schedule 1 Item 1 Road");
        s1i1.setReceiveLoadName("Schedule 1 Item 1 Load");
        s1i1.setShipLoadName("Schedule 1 Item 1 Ship");
        s1i1.setCount(321);
        s1i1.setDestination(l1);
        s1i1.setDestinationTrack(t2);
        s1i1.setComment("Schedule 1 Item 1 Comment");

        ScheduleItem s1i2 = s1.addItem("boxcar");
        s1i2.setRoadName("Schedule 1 Item 2 Road");
        s1i2.setReceiveLoadName("Schedule 1 Item 2 Load");
        s1i2.setShipLoadName("Schedule 1 Item 2 Ship");
        s1i2.setCount(222);
        s1i2.setDestination(l2);
        s1i2.setDestinationTrack(t3);
        s1i2.setComment("Schedule 1 Item 2 Comment");

        Schedule s2 = sm.newSchedule("Schedule 2 Name");
        s2.setComment("Schedule 2 Comment");
        ScheduleItem s2i1 = s2.addItem("BoxCar");
        s2i1.setRoadName("Schedule 2 Item 1 Road");
        s2i1.setReceiveLoadName("Schedule 2 Item 1 Load");
        s2i1.setShipLoadName("Schedule 2 Item 1 Ship");
        s2i1.setCount(123);
        s2i1.setComment("Schedule 2 Item 1 Comment");
        
        // test ignore percentage
        t2.setIgnoreUsedLengthPercentage(45);
        
        // test destination option
        t3.setDestinationOption(Track.INCLUDE_DESTINATIONS);
        t3.addDestination(l3);

        // test schedule and alternate track features
        t2.setSchedule(s1);
        t2.setAlternateTrack(t1);
        t2.setReservationFactor(33);
        t2.setScheduleMode(Track.MATCH);
        t2.setScheduleCount(2);
        
        // test divisions
        DivisionManager dm = InstanceManager.getDefault(DivisionManager.class);
        Division division = dm.newDivision("testDivisionName");
        division.setComment("divisionComment");
        l3.setDivision(division);
        
        // test comments
        t2.setComment("Track 2 comment");
        t3.setCommentBoth("Track 3 both comment");
        t1.setCommentPickup("Track 1 pick up comment");
        t4.setCommentSetout("Track 4 set out comment");
        t1.setPrintManifestCommentEnabled(false);
        t3.setPrintSwitchListCommentEnabled(true);

        locationList = manager.getLocationsByIdList();
        Assert.assertEquals("New Location by Id 1", "Test Location 2", locationList.get(0).getName());
        Assert.assertEquals("New Location by Id 2", "Test Location 1", locationList.get(1).getName());
        Assert.assertEquals("New Location by Id 3", "Test Location 3", locationList.get(2).getName());

        Assert.assertEquals("New Location by Name 1", "Test Location 1", manager.getLocationByName("Test Location 1")
                .getName());
        Assert.assertEquals("New Location by Name 2", "Test Location 2", manager.getLocationByName("Test Location 2")
                .getName());
        Assert.assertEquals("New Location by Name 3", "Test Location 3", manager.getLocationByName("Test Location 3")
                .getName());

        manager.getLocationByName("Test Location 1").setComment("Test Location 1 Comment");
        manager.getLocationByName("Test Location 1").setSwitchListEnabled(true);
        manager.getLocationByName("Test Location 1").setTrainDirections(Location.EAST);
        manager.getLocationByName("Test Location 1").addTypeName("Baggage");
        manager.getLocationByName("Test Location 1").addTypeName("BoxCar");
        manager.getLocationByName("Test Location 1").addTypeName(Bundle.getMessage("Caboose"));
        manager.getLocationByName("Test Location 1").addTypeName("Coal");
        manager.getLocationByName("Test Location 1").addTypeName("Engine");
        manager.getLocationByName("Test Location 1").addTypeName("Hopper");
        manager.getLocationByName("Test Location 2").setComment("Test Location 2 Comment");
        manager.getLocationByName("Test Location 2").setSwitchListEnabled(false);
        manager.getLocationByName("Test Location 2").setTrainDirections(Location.WEST);
        manager.getLocationByName("Test Location 2").addTypeName("Baggage");
        manager.getLocationByName("Test Location 2").addTypeName("Boxcar");
        manager.getLocationByName("Test Location 2").addTypeName(Bundle.getMessage("Caboose"));
        manager.getLocationByName("Test Location 2").addTypeName("Coal");
        manager.getLocationByName("Test Location 2").addTypeName("Engine");
        manager.getLocationByName("Test Location 2").addTypeName("Hopper");
        manager.getLocationByName("Test Location 2").addTypeName("Track 2 Type");
        manager.getLocationByName("Test Location 3").setComment("Test Location 3 Comment");
        manager.getLocationByName("Test Location 3").setSwitchListEnabled(true);
        manager.getLocationByName("Test Location 3").setTrainDirections(Location.EAST + Location.WEST + Location.NORTH);
        manager.getLocationByName("Test Location 3").addTypeName("Baggage");
        manager.getLocationByName("Test Location 3").addTypeName("boxCar");
        manager.getLocationByName("Test Location 3").addTypeName(Bundle.getMessage("Caboose"));
        manager.getLocationByName("Test Location 3").addTypeName("Coal");
        manager.getLocationByName("Test Location 3").addTypeName("Engine");
        manager.getLocationByName("Test Location 3").addTypeName("Hopper");
        manager.getLocationByName("Test Location 3").addTypeName("Track 4 Type");

        locationList = manager.getLocationsByIdList();
        Assert.assertEquals("New Number of Locations", 3, locationList.size());

        for (int i = 0; i < locationList.size(); i++) {
            Location loc = locationList.get(i);
            String locname = loc.getName();
            if (i == 0) {
                Assert.assertEquals("New Location by Id List 1", "Test Location 2", locname);
            }
            if (i == 1) {
                Assert.assertEquals("New Location by Id List 2", "Test Location 1", locname);
            }
            if (i == 2) {
                Assert.assertEquals("New Location by Id List 3", "Test Location 3", locname);
            }
        }

        List<Location> locationListByName = manager.getLocationsByNameList();
        Assert.assertEquals("New Number of Locations", 3, locationList.size());

        for (int i = 0; i < locationListByName.size(); i++) {
            Location loc = locationListByName.get(i);
            String locname = loc.getName();
            if (i == 0) {
                Assert.assertEquals("New Location by Name List 1", "Test Location 1", locname);
            }
            if (i == 1) {
                Assert.assertEquals("New Location by Name List 2", "Test Location 2", locname);
            }
            if (i == 2) {
                Assert.assertEquals("New Location by Name List 3", "Test Location 3", locname);
            }
        }

        InstanceManager.getDefault(LocationManagerXml.class).writeOperationsFile();

        manager.newLocation("Test Location 4");
        manager.newLocation("Test Location 5");
        manager.newLocation("Test Location 6");
        manager.getLocationByName("Test Location 2").setComment("Test Location 2 Changed Comment");

        InstanceManager.getDefault(LocationManagerXml.class).writeOperationsFile();

        locationList = manager.getLocationsByIdList();
        Assert.assertEquals("Number of Locations", 6, locationList.size());

        // Revert the main xml file back to the backup file.
        InstanceManager.getDefault(LocationManagerXml.class).revertBackupFile(
                "temp" + File.separator + OperationsSetupXml.getOperationsDirectoryName() + File.separator
                + InstanceManager.getDefault(LocationManagerXml.class).getOperationsFileName());

        // Need to dispose of the LocationManager's list and hash table
        manager.dispose();
        // delete all schedules
        InstanceManager.getDefault(ScheduleManager.class).dispose();
        // delete divisions
        dm.dispose();

        ct.addName("Boxcar");
        ct.addName("boxCar");
        ct.addName("BoxCar");
        ct.addName("Track 2 Type");
        ct.addName("Track 4 Type");

        // The dispose has removed all locations from the Manager.
        manager = InstanceManager.getDefault(LocationManager.class);
        locationListByName = manager.getLocationsByNameList();
        Assert.assertEquals("Starting Number of Locations", 0, locationListByName.size());
        Assert.assertEquals("Starting Number of Divisions", 0, dm.getNumberOfdivisions());

        // Need to force a re-read of the xml file.
        InstanceManager.getDefault(LocationManagerXml.class).readFile(
                "temp" + File.separator + OperationsSetupXml.getOperationsDirectoryName() + File.separator
                + InstanceManager.getDefault(LocationManagerXml.class).getOperationsFileName());

        // check locations
        locationListByName = manager.getLocationsByNameList();
        Assert.assertEquals("Starting Number of Locations", 3, locationListByName.size());
        Assert.assertEquals("Starting Number of Divisions", 1, dm.getNumberOfdivisions());

        for (int i = 0; i < locationListByName.size(); i++) {
            Location loc = locationListByName.get(i);

            if (i == 0) {
                Assert.assertEquals("New Location by Name List 1", "Test Location 1", loc.getName());
                Assert.assertEquals("Location 1 direction", Location.EAST, loc.getTrainDirections());
                Assert.assertEquals("Location 1 comment", "Test Location 1 Comment", loc.getCommentWithColor());
                Assert.assertEquals("Location 1 switchList", true, loc.isSwitchListEnabled());
                Assert.assertEquals("Location 1 car type", true, loc.acceptsTypeName("BoxCar"));
                Assert.assertEquals("Location 1 car type", false, loc.acceptsTypeName("boxCar"));
                Assert.assertEquals("Location 1 car type", true, loc.acceptsTypeName("Boxcar"));
                Assert.assertEquals("Location 1 printer name", "Default Name For Printer", loc.getDefaultPrinterName());
                List<Track> list = loc.getTracksByNameList(null);
                Assert.assertEquals("Location 1 has n tracks", 1, list.size());
                Track t = list.get(0);
                Assert.assertEquals("Location 1 first track name", "An Interchange track 3", t.getName());
                Assert.assertEquals("Location 1 track interchange", true, t.isInterchange());
                Assert.assertEquals("Location 1 track road option", Track.EXCLUDE_ROADS, t.getRoadOption());
                Assert.assertEquals("Location 1 track road", true, t.isRoadNameAccepted("Track 1 Road"));
                Assert.assertEquals("Location 1 track road", false, t.isRoadNameAccepted("Track 3 Road"));
                Assert.assertEquals("Location 1 track Load", false, t.isLoadNameAccepted("Track 3 Load"));
                Assert.assertEquals("Location 1 blocking order", 2, t.getBlockingOrder());
                Assert.assertEquals("Location 1 drop option", Track.TRAINS, t.getDropOption());
                Assert.assertEquals("Location 1 drop option id", "Track 3 train id", t.getDropIds()[0]);
                Assert.assertEquals("Location 1 pick up option", Track.ROUTES, t.getPickupOption());
                Assert.assertEquals("Location 1 pick up option id", "Track 3 route id", t.getPickupIds()[0]);
                Assert.assertEquals("Location 1 load swap", false, t.isLoadSwapEnabled());
                Assert.assertEquals("Location 1 service order", Track.FIFO, t.getServiceOrder());
                Assert.assertNull("Location 1 track pool", t.getPool());
                Assert.assertEquals("Location 1 comment", "Track 3 both comment", t.getCommentBoth());
                Assert.assertEquals("Location 1 print manifest", true, t.isPrintManifestCommentEnabled());
                Assert.assertEquals("Location 1 print switch lists", true, t.isPrintSwitchListCommentEnabled());
            }
            if (i == 1) {
                Assert.assertEquals("New Location by Name List 2", "Test Location 2", loc.getName());
                Assert.assertEquals("Location 2 direction", Location.WEST, loc.getTrainDirections());
                Assert.assertEquals("Location 2 comment", "Test Location 2 Comment", loc.getCommentWithColor());
                Assert.assertEquals("Location 2 switchList", false, loc.isSwitchListEnabled());
                Assert.assertEquals("Location 2 car type", true, loc.acceptsTypeName("Boxcar"));
                Assert.assertEquals("Location 2 car type", false, loc.acceptsTypeName("boxCar"));
                Assert.assertEquals("Location 2 car type", false, loc.acceptsTypeName("BoxCar"));
                
                // icons
                Assert.assertEquals("Location 2 Icon East", 124, loc.getTrainIconEast().x);
                Assert.assertEquals("Location 2 Icon East", 543, loc.getTrainIconEast().y);
                Assert.assertEquals("Location 2 Icon West", 32, loc.getTrainIconWest().x);
                Assert.assertEquals("Location 2 Icon West", 52, loc.getTrainIconWest().y);
                Assert.assertEquals("Location 2 Icon North", 23, loc.getTrainIconNorth().x);
                Assert.assertEquals("Location 2 Icon North", 632, loc.getTrainIconNorth().y);
                Assert.assertEquals("Location 2 Icon South", 63, loc.getTrainIconSouth().x);
                Assert.assertEquals("Location 2 Icon South", 81, loc.getTrainIconSouth().y);
                
                Assert.assertEquals("Location 2 Icon Range", 45, loc.getTrainIconRangeX());
                Assert.assertEquals("Location 2 Icon Range", 54, loc.getTrainIconRangeY());

                List<Track> list = loc.getTracksByNameList(null);
                Assert.assertEquals("Location 2 has n tracks", 2, list.size());
                Track t = list.get(0);
                Assert.assertEquals("Location 2 first track name", "A Spur track 2", t.getName());
                Assert.assertEquals("Location 2 first track spur", true, t.isSpur());
                Assert.assertEquals("Location 2 track 1 road option", Track.ALL_ROADS, t.getRoadOption());
                Assert.assertEquals("Location 2 track 1 road", true, t.isRoadNameAccepted("Track 1 Road"));
                Assert.assertEquals("Location 2 track 1 road", true, t.isRoadNameAccepted("Track 3 Road"));
                Assert.assertEquals("Location 2 track 1 type", true, t.isTypeNameAccepted("Track 2 Type"));
                Assert.assertEquals("Location 2 track 1 type", false, t.isTypeNameAccepted("Track 4 Type"));
                Assert.assertNotNull("Location 2 track 1 pool exists", t.getPool());
                Assert.assertEquals("Location 2 track 1 pool name", "Test Pool", t.getPool().getName());
                Assert.assertEquals("Location 2 track 1 pool name", "Test Pool", t.getPoolName());
                Assert.assertEquals("Location 2 track 1 min track length", 123, t.getMinimumLength());
                Assert.assertNotNull("Location 2 track 1 schedule", t.getSchedule());
                Assert.assertEquals("Location 2 track 1 schedule name", "Schedule 1 Name", t.getSchedule().getName());
                Assert.assertEquals("Location 2 track 1 schedule name", "Schedule 1 Name", t.getScheduleName());
                Assert.assertNotNull("Location 2 track 1 alternate track", t.getAlternateTrack());
                Assert.assertEquals("Location 2 track 1 alternate track name", "A Yard track 1", t.getAlternateTrack()
                        .getName());
                Assert.assertEquals("Location 2 track 1 schedule mode", Track.MATCH, t.getScheduleMode());
                Assert.assertEquals("Location 2 track 1 reservation factor", 33, t.getReservationFactor());
                Assert.assertEquals("Location 2 track 1 schedule count", 2, t.getScheduleCount());
                Assert.assertEquals("Location 2 track 1 comment", "Track 2 comment", t.getComment());
                Assert.assertEquals("Location 2 track 1 print manifests", true, t.isPrintManifestCommentEnabled());
                Assert.assertEquals("Location 2 print switch lists", false, t.isPrintSwitchListCommentEnabled());

                t = list.get(1);
                Assert.assertEquals("Location 2 2nd track name", "A Yard track 1", t.getName());
                Assert.assertEquals("Location 2 track 2 road option", Track.INCLUDE_ROADS, t.getRoadOption());
                Assert.assertEquals("Location 2 track 2 road", true, t.isRoadNameAccepted("Track 1 Road"));
                Assert.assertEquals("Location 2 track 2 road", false, t.isRoadNameAccepted("Track 3 Road"));
                Assert.assertEquals("Location 2 track 2 type", false, t.isTypeNameAccepted("Track 2 Type"));
                Assert.assertEquals("Location 2 track 2 type", false, t.isTypeNameAccepted("Track 4 Type"));
                Assert.assertNotNull("Location 2 track 2 pool exists", t.getPool());
                Assert.assertEquals("Location 2 track 2 pool name", "Test Pool", t.getPool().getName());
                Assert.assertEquals("Location 2 track 2 min track length", 321, t.getMinimumLength());
                Assert.assertEquals("Location 2 track 2 comment", "", t.getCommentBoth());
                Assert.assertEquals("Location 2 track 2 print manifests", false, t.isPrintManifestCommentEnabled());
                Assert.assertEquals("Location 2 print switch lists", false, t.isPrintSwitchListCommentEnabled());
            }
            if (i == 2) {
                Assert.assertEquals("New Location by Name List 3", "Test Location 3", loc.getName());
                Assert.assertEquals("Location 3 direction", Location.EAST + Location.WEST + Location.NORTH, loc
                        .getTrainDirections());
                Assert.assertEquals("Location 3 comment", "Test Location 3 Comment", loc.getCommentWithColor());
                Assert.assertEquals("Location 3 switchList", true, loc.isSwitchListEnabled());
                Assert.assertEquals("Location 3 car type", true, loc.acceptsTypeName("boxCar"));
                Assert.assertEquals("Location 3 car type", false, loc.acceptsTypeName("BoxCar"));
                Assert.assertEquals("Location 3 car type", true, loc.acceptsTypeName("Boxcar"));
                Assert.assertEquals("Location 3 division", "testDivisionName", loc.getDivisionName());

                List<Track> list = loc.getTracksByNameList(null);
                Assert.assertEquals("Location 3 has n tracks", 1, list.size());
                Track t = list.get(0);
                Assert.assertEquals("Location 3 first track name", "A Stage track 4", t.getName());
                Assert.assertEquals("Location 3 track 1", true, t.isStaging());
                Assert.assertEquals("Location 3 track 1 road option", Track.ALL_ROADS, t.getRoadOption());
                Assert.assertEquals("Location 3 track 1 road", true, t.isRoadNameAccepted("Track 1 Road"));
                Assert.assertEquals("Location 3 track 1 road", true, t.isRoadNameAccepted("Track 3 Road"));
                Assert.assertEquals("Location 3 track type", false, t.isTypeNameAccepted("Track 2 Type"));
                Assert.assertEquals("Location 3 track type", true, t.isTypeNameAccepted("Track 4 Type"));
                Assert.assertEquals("Location 3 ship option", Track.INCLUDE_LOADS, t.getShipLoadOption());
                Assert.assertEquals("Location 3 ship load", true, t.isLoadNameShipped("Track 4 Ship Load"));
                Assert.assertEquals("Location 3 load swap", true, t.isLoadSwapEnabled());
                Assert.assertEquals("Location 3 blocking", true, t.isBlockCarsEnabled());
                Assert.assertNull("Location 3 track pool", t.getPool());
                Assert.assertEquals("Location 3 comment", "Track 4 set out comment", t.getCommentSetout());
            }
        }

        // check Schedules
        sm = InstanceManager.getDefault(ScheduleManager.class);
        List<Schedule> list = sm.getSchedulesByNameList();

        Assert.assertEquals("There should be 2 schedules", 2, list.size());
        s1 = list.get(0);
        s2 = list.get(1);

        Assert.assertEquals("Schedule 1 name", "Schedule 1 Name", s1.getName());
        Assert.assertEquals("Schedule 2 name", "Schedule 2 Name", s2.getName());
        Assert.assertEquals("Schedule 1 comment", "Schedule 1 Comment", s1.getComment());
        Assert.assertEquals("Schedule 2 comment", "Schedule 2 Comment", s2.getComment());

        List<ScheduleItem> s1items = s1.getItemsBySequenceList();
        Assert.assertEquals("There should be 2 items", 2, s1items.size());
        ScheduleItem si1 = s1items.get(0);
        Assert.assertEquals("Item 1 type", "Boxcar", si1.getTypeName());
        Assert.assertEquals("Item 1 load", "Schedule 1 Item 1 Load", si1.getReceiveLoadName());
        Assert.assertEquals("Item 1 ship", "Schedule 1 Item 1 Ship", si1.getShipLoadName());
        Assert.assertEquals("Item 1 type", "Schedule 1 Item 1 Comment", si1.getComment());
        Assert.assertEquals("Item 1 road", "Schedule 1 Item 1 Road", si1.getRoadName());
        Assert.assertEquals("Item 1 count", 321, si1.getCount());
        Assert.assertEquals("Item 1 destination", "Test Location 2", si1.getDestinationName());
        Assert.assertEquals("Item 1 track", "A Spur track 2", si1.getDestinationTrackName());

        ScheduleItem si2 = s1items.get(1);
        Assert.assertEquals("Item 2 type", "boxcar", si2.getTypeName());
        Assert.assertEquals("Item 2 load", "Schedule 1 Item 2 Load", si2.getReceiveLoadName());
        Assert.assertEquals("Item 2 ship", "Schedule 1 Item 2 Ship", si2.getShipLoadName());
        Assert.assertEquals("Item 2 type", "Schedule 1 Item 2 Comment", si2.getComment());
        Assert.assertEquals("Item 2 road", "Schedule 1 Item 2 Road", si2.getRoadName());
        Assert.assertEquals("Item 2 count", 222, si2.getCount());
        Assert.assertEquals("Item 2 destination", "Test Location 1", si2.getDestinationName());
        Assert.assertEquals("Item 2 track", "An Interchange track 3", si2.getDestinationTrackName());

        List<ScheduleItem> s2items = s2.getItemsBySequenceList();
        Assert.assertEquals("There should be 1 items", 1, s2items.size());
        ScheduleItem si3 = s2items.get(0);
        Assert.assertEquals("Item 3 type", "BoxCar", si3.getTypeName());
        Assert.assertEquals("Item 3 load", "Schedule 2 Item 1 Load", si3.getReceiveLoadName());
        Assert.assertEquals("Item 3 ship", "Schedule 2 Item 1 Ship", si3.getShipLoadName());
        Assert.assertEquals("Item 3 type", "Schedule 2 Item 1 Comment", si3.getComment());
        Assert.assertEquals("Item 3 type", "Schedule 2 Item 1 Road", si3.getRoadName());
        Assert.assertEquals("Item 3 count", 123, si3.getCount());
        Assert.assertEquals("Item 3 destination", "", si3.getDestinationName());
        Assert.assertEquals("Item 3 track", "", si3.getDestinationTrackName());
        
        // confirm division
        Assert.assertEquals("Divsion Name", "testDivisionName", dm.getDivisionsByIdList().get(0).getName());
        Assert.assertEquals("Divsion Comment", "divisionComment", dm.getDivisionsByIdList().get(0).getComment());

        // delete all locations
        manager.dispose();
        // delete all schedules
        sm.dispose();
        // clear out the file
        InstanceManager.getDefault(LocationManagerXml.class).writeOperationsFile();
    }

    // TODO: Add tests for adding + deleting the same cars
    // TODO: Add tests for track locations
    // TODO: Add test to create xml file
    // TODO: Add test to read xml file
    @Override
    @BeforeEach
    public void setUp() {
        super.setUp();
        InstanceManager.getDefault(jmri.jmrit.operations.rollingstock.cars.CarTypes.class).addName("Boxcar");
    }
}
