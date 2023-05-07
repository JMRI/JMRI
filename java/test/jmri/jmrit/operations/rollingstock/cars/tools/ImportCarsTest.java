package jmri.jmrit.operations.rollingstock.cars.tools;

import java.io.File;
import java.text.MessageFormat;
import java.util.List;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.setup.Control;
import jmri.util.JUnitOperationsUtil;
import jmri.util.swing.JemmyUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
@Timeout(60)
public class ImportCarsTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        ImportCars t = new ImportCars();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testReadFile() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // export cars to create file
        exportCars();

        // do import
        importCars(false, null);

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // do import again
        importCars(false, null);

        // confirm import successful no new cars added
        Assert.assertEquals("cars", 9, cm.getNumEntries());
    }

    @Test
    public void testReadFileCarExtensions() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car c1 = cm.getByRoadAndNumber("CP", "X10001");
        c1.setFred(true);
        c1.setOwnerName("TestName");
        Car c2 = cm.getByRoadAndNumber("CP", "X10002");
        c2.setPassenger(true);
        c2.setBlocking(6);
        Car c3 = cm.getByRoadAndNumber("CP", "X20001");
        c3.setUtility(true);
        Car c4 = cm.getByRoadAndNumber("CP", "X20002");
        c4.setCarHazardous(true);

        // export cars to create file
        exportCars();

        // do import
        importCars(false, null);

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        c1 = cm.getByRoadAndNumber("CP", "X10001");
        Assert.assertTrue(c1.hasFred());
        Assert.assertEquals("owner name", "TestName", c1.getOwnerName());

        // confirm new owner name was added
        CarOwners co = InstanceManager.getDefault(CarOwners.class);
        Assert.assertTrue(co.containsName("TestName"));

        c2 = cm.getByRoadAndNumber("CP", "X10002");
        Assert.assertTrue(c2.isPassenger());
        Assert.assertEquals("blocking", 6, c2.getBlocking());

        c3 = cm.getByRoadAndNumber("CP", "X20001");
        Assert.assertTrue(c3.isUtility());

        c4 = cm.getByRoadAndNumber("CP", "X20002");
        Assert.assertTrue(c4.isHazardous());
    }

    @Test
    public void testReadFileNewTypeNo() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car c1 = cm.getByRoadAndNumber("CP", "X10001");
        c1.setTypeName("NewType");
        // need to remove car from track to avoid additional import messages
        c1.setLocation(null, null);

        // export cars to create file
        exportCars();

        // do import
        importCars(false, Bundle.getMessage("carAddType"), Bundle.getMessage("ButtonNo"));

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());
    }

    @Test
    public void testReadFileNewTypeYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car c1 = cm.getByRoadAndNumber("CP", "X10001");
        c1.setTypeName("NewType");
        // need to remove car from track to avoid additional import messages
        c1.setLocation(null, null);
        
        Car c2 = cm.getByRoadAndNumber("CP", "X10002");
        c2.setTypeName("NewerType");
        // need to remove car from track to avoid additional import messages
        c2.setLocation(null, null);

        // export cars to create file
        exportCars();

        // do import
        importCars(false, Bundle.getMessage("carAddType"), Bundle.getMessage("ButtonYes"),
                Bundle.getMessage("OnlyAskedOnce"), Bundle.getMessage("ButtonYes"));

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // confirm new car type added
        CarTypes ct = InstanceManager.getDefault(CarTypes.class);
        Assert.assertTrue(ct.containsName("NewType"));
        Assert.assertTrue(ct.containsName("NewerType"));
    }
    
    @Test
    public void testReadFileAddTypeNo() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // export cars to create file
        exportCars();
        
        // remove "Boxcar" as a valid car type
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location locationNorthEnd = lmanager.getLocationById("1");
        locationNorthEnd.deleteTypeName("Boxcar");

        // do import
        importCars(true, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("ServiceCarType"), Bundle.getMessage("ButtonNo"));

        // confirm import unsuccessful
        Assert.assertEquals("cars", 6, cm.getNumEntries());
    }
    
    @Test
    public void testReadFileAddTypeYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // export cars to create file
        exportCars();
        
        // remove "Boxcar" as a valid car type
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location locationNorthEnd = lmanager.getLocationById("1");
        locationNorthEnd.deleteTypeName("Boxcar");
        locationNorthEnd.deleteTypeName("Caboose");

        // do import
        importCars(false, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("ServiceCarType"), Bundle.getMessage("ButtonYes"),
                Bundle.getMessage("OnlyAskedOnce"), Bundle.getMessage("ButtonYes"));

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());
    }
    
    @Test
    public void testReadFileIncreaseTrackLengthNo() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // export cars to create file
        exportCars();
        
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");
        northEndStaging1.setLength(50);

        // do import
        importCars(true, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("TrackLength"), Bundle.getMessage("ButtonNo"));

        // confirm import unsuccessful
        Assert.assertEquals("cars", 5, cm.getNumEntries());
    }
    
    @Test
    public void testReadFileIncreaseTrackLengthYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // export cars to create file
        exportCars();
        
        // remove "Boxcar" as a valid car type
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");
        northEndStaging1.setLength(50);
        Track northEndStaging2 = locationNorthEnd.getTrackById("1s2");
        northEndStaging2.setLength(60);

        // do import
        importCars(false, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("TrackLength"), Bundle.getMessage("ButtonYes"),
                Bundle.getMessage("OnlyAskedOnce"), Bundle.getMessage("ButtonYes"));

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());
        Assert.assertEquals("track length increased 1000", 1050, northEndStaging1.getLength());
        Assert.assertEquals("track length increased 1000", 1060, northEndStaging2.getLength());
    }
    
    @Test
    public void testReadFileIncreaseTrackCapacityYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // export cars to create file
        exportCars();
        
        // remove "Boxcar" as a valid car type
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");
        northEndStaging1.setLength(0);
        Track northEndStaging2 = locationNorthEnd.getTrackById("1s2");
        northEndStaging2.setLength(0);

        // do import
        importCars(false, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("TrackLength"), Bundle.getMessage("ButtonYes"),
                Bundle.getMessage("OnlyAskedOnce"), Bundle.getMessage("ButtonYes"));

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());
        Assert.assertEquals("track length increased 1000", 1000, northEndStaging1.getLength());
        Assert.assertEquals("track length increased 1000", 1000, northEndStaging2.getLength());
    }
    
    @Test
    public void testReadFileForceNo() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // export cars to create file
        exportCars();
        
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");
        northEndStaging1.setRoadOption(Track.EXCLUDE_ROADS);
        northEndStaging1.addRoadName("CP");

        // do import
        importCars(true, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("OverRide"), Bundle.getMessage("ButtonNo"));

        // confirm import unsuccessful
        Assert.assertEquals("cars", 4, cm.getNumEntries());
    }
    
    @Test
    public void testReadFileForceYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // export cars to create file
        exportCars();
        
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location locationNorthEnd = lmanager.getLocationById("1");
        Track northEndStaging1 = locationNorthEnd.getTrackById("1s1");
        northEndStaging1.setRoadOption(Track.EXCLUDE_ROADS);
        northEndStaging1.addRoadName("CP");

        // do import
        importCars(false, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("OverRide"), Bundle.getMessage("ButtonYes"),
                Bundle.getMessage("OnlyAskedOnce"), Bundle.getMessage("ButtonYes"));

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());
    }

    @Test
    public void testReadFileNewLocationNo() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car c1 = cm.getByRoadAndNumber("CP", "X10001");
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location l = lmanager.newLocation("Test Location");
        Track t = l.addTrack("Test Track", Track.YARD);
        t.setLength(200);
        c1.setLocation(l, t);

        // export cars to create file
        exportCars();

        // remove the car's location
        lmanager.deregister(l);

        // do import
        importCars(true, Bundle.getMessage("carLocation"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("carLocation"), Bundle.getMessage("ButtonNo"));

        // confirm import unsuccessful
        Assert.assertEquals("cars", 5, cm.getNumEntries());
    }

    @Test
    public void testReadFileNewLocationYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car c1 = cm.getByRoadAndNumber("CP", "X10001");
        Car c2 = cm.getByRoadAndNumber("CP", "X10002");
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc1 = lmanager.newLocation("Test Location 1");
        Location loc2 = lmanager.newLocation("Test Location 2");
        // place car at location, no track.
        // A track would require 3 more dialog responses
        c1.setLocation(loc1, null);
        // 2nd car's location will be automatically created
        c2.setLocation(loc2, null);

        // export cars to create file
        exportCars();

        // remove the car's location
        lmanager.deregister(loc1);
        lmanager.deregister(loc2);

        // do import
        importCars(false, Bundle.getMessage("carLocation"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("carLocation"), Bundle.getMessage("ButtonYes"), Bundle.getMessage("OnlyAskedOnce"),
                Bundle.getMessage("ButtonYes"));

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());
        Assert.assertNotNull(lmanager.getLocationByName("Test Location 1"));
        Assert.assertNotNull(lmanager.getLocationByName("Test Location 2"));
    }
    
    @Test
    public void testReadFileNewTrackNo() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car c1 = cm.getByRoadAndNumber("CP", "X10001");
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location l = lmanager.newLocation("Test Location");
        Track t = l.addTrack("Test Track", Track.YARD);
        t.setLength(200);
        c1.setLocation(l, t);

        // export cars to create file
        exportCars();

        // remove the car's track
        l.deleteTrack(t);

        // do import
        importCars(true, Bundle.getMessage("carTrack"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("carTrack"), Bundle.getMessage("ButtonNo"));

        // confirm import unsuccessful
        Assert.assertEquals("cars", 5, cm.getNumEntries());
    }
    
    @Test
    public void testReadFileNewTrackYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car c1 = cm.getByRoadAndNumber("CP", "X10001");
        Car c2 = cm.getByRoadAndNumber("CP", "X10002");
        Car c3 = cm.getByRoadAndNumber("CP", "X20001");
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location l = lmanager.newLocation("Test Location");
        Track track1 = l.addTrack("Test Track 1", Track.YARD);
        track1.setLength(200);
        Track track2 = l.addTrack("Test Track 2", Track.YARD);
        track2.setLength(200);
        c1.setLocation(l, track1);
        c2.setLocation(l, track2);
        
        // create staging
        Location staging = lmanager.newLocation("Test Staging");
        Track track3 = staging.addTrack("Test Staging 3", Track.STAGING);
        track3.setLength(200);
        Track track4 = staging.addTrack("Test Staging 4", Track.STAGING);
        track4.setLength(200);
        c3.setLocation(staging, track3);

        // export cars to create file
        exportCars();

        // remove the car's track
        l.deleteTrack(track1);
        l.deleteTrack(track2);
        staging.deleteTrack(track3);

        // do import
        importCars(false, Bundle.getMessage("carTrack"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("carTrack"), Bundle.getMessage("ButtonYes"), Bundle.getMessage("OnlyAskedOnce"),
                Bundle.getMessage("ButtonYes"));

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());
        
        track1 = l.getTrackByName("Test Track 1", Track.YARD);
        Assert.assertNotNull(track1);
        Assert.assertEquals("track created", 1000, track1.getLength());
        track2 = l.getTrackByName("Test Track 2", Track.YARD);
        Assert.assertNotNull(track2);
        Assert.assertEquals("track created", 1000, track2.getLength());
        track3 = staging.getTrackByName("Test Staging 3", Track.STAGING);
        Assert.assertNotNull(track3);
        Assert.assertEquals("track created", 1000, track3.getLength());
    }
    
    @Test
    public void testReadFileNewStagingYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car c3 = cm.getByRoadAndNumber("CP", "X20001");
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        
        // create staging
        Location staging = lmanager.newLocation("Test Staging");
        Track track3 = staging.addTrack("Test Staging 3", Track.STAGING);
        track3.setLength(200);
        Track track4 = staging.addTrack("Test Staging 4", Track.STAGING);
        track4.setLength(200);
        c3.setLocation(staging, track3);

        // export cars to create file
        exportCars();

        // remove the car's track
        staging.deleteTrack(track3);

        // do import
        importCars(false, Bundle.getMessage("carTrack"), Bundle.getMessage("ButtonOK"),
                Bundle.getMessage("carTrack"), Bundle.getMessage("ButtonYes"), Bundle.getMessage("OnlyAskedOnce"),
                Bundle.getMessage("ButtonYes"));

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());
        
        track3 = staging.getTrackByName("Test Staging 3", Track.STAGING);
        Assert.assertNotNull(track3);
        Assert.assertEquals("track created", 1000, track3.getLength());
    }

    @Test
    public void testReadFileErrorNoCarNumber() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // Create car without a road number
        Car car = cm.getByRoadAndNumber("CP", "X10001");
        car.setNumber("");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, Bundle.getMessage("RoadNumberMissing"));
    }

    @Test
    public void testReadFileErrorNoCarRoadName() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        // Create car without a road name
        Car car = cm.getByRoadAndNumber("CP", "X10001");
        car.setRoadName("");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, Bundle.getMessage("RoadNameMissing"));
    }

    @Test
    public void testReadFileErrorNoCarTypeName() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        car.setTypeName("");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, Bundle.getMessage("CarTypeMissing"));
    }

    @Test
    public void testReadFileErrorNoCarLength() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        // need to remove car from track to avoid exception
        car.setLocation(null, null);
        car.setLength("");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, Bundle.getMessage("CarLengthMissing"));
    }

    @Test
    public void testReadFileErrorLengthNotNumber() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        // need to remove car from track to avoid exception
        car.setLocation(null, null);
        car.setLength("A");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, Bundle.getMessage("CarLengthMissing"));
    }

    @Test
    public void testReadFileErrorCarRoadNumberTooLong() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        car.setNumber("12345678901");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, MessageFormat.format(Bundle
                .getMessage("RoadNumMustBeLess"),
                new Object[]{Control.max_len_string_road_number + 1}));
    }

    @Test
    public void testReadFileErrorCarRoadTooLong() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        car.setRoadName("ABCDEFGHIJKLM");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, MessageFormat.format(Bundle
                .getMessage("carAttribute"), new Object[]{Control.max_len_string_attibute}));
    }

    @Test
    public void testReadFileErrorCarTypeTooLong() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        car.setTypeName("ABCDEFGHIJKLM");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, MessageFormat.format(Bundle
                .getMessage("carAttribute"), new Object[]{Control.max_len_string_attibute}));
    }

    @Test
    public void testReadFileErrorCarLengthTooLong() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        car.setLength("10000");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, MessageFormat.format(Bundle
                .getMessage("carAttribute"), new Object[]{Control.max_len_string_length_name}));
    }

    @Test
    public void testReadFileErrorCarWeightTooLong() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        car.setWeight("10000");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, MessageFormat.format(Bundle
                .getMessage("carAttribute"), new Object[]{Control.max_len_string_weight_name}));
    }

    @Test
    public void testReadFileCarWeightMissingNo() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car c1 = cm.getByRoadAndNumber("CP", "X10001");
        c1.setWeight("");
        c1.setWeightTons("");

        // export cars to create file
        exportCars();

        // do import
        importCars(false, Bundle.getMessage("CarWeightMissing"), Bundle.getMessage("ButtonNo"));

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());
        
        // car weight not updated
        c1 = cm.getByRoadAndNumber("CP", "X10001");
        Assert.assertEquals("Calculated weight", "", c1.getWeight());
        Assert.assertEquals("Calculated weight tons", "0", c1.getWeightTons());
    }
    
    @Test
    public void testReadFileCarWeightMissingYes() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car c1 = cm.getByRoadAndNumber("CP", "X10001");
        c1.setWeight("");
        c1.setWeightTons("");

        // export cars to create file
        exportCars();

        // do import
        importCars(false, Bundle.getMessage("CarWeightMissing"), Bundle.getMessage("ButtonYes"));

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());
        
        c1 = cm.getByRoadAndNumber("CP", "X10001");
        Assert.assertEquals("Calculated weight", "3.8", c1.getWeight());
        Assert.assertEquals("Calculated weight tons", "76", c1.getWeightTons());
    }
    
    @Test
    public void testReadFileCabooseWeightMissing() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car c1 = cm.getByRoadAndNumber("CP", "C10099");
        c1.setWeight("");
        c1.setWeightTons("");

        // export cars to create file
        exportCars();

        // do import
        importCars(false, Bundle.getMessage("CarWeightMissing"), Bundle.getMessage("ButtonYes"));

        // confirm import successful
        Assert.assertEquals("cars", 9, cm.getNumEntries());
        
        c1 = cm.getByRoadAndNumber("CP", "C10099");
        Assert.assertEquals("Calculated weight", "3.2", c1.getWeight());
        Assert.assertEquals("Calculated weight tons", "28", c1.getWeightTons());
    }

    @Test
    public void testReadFileErrorCarColorTooLong() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        car.setColor("ABCDEFGHIJKLM");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, MessageFormat.format(Bundle
                .getMessage("carAttribute"), new Object[]{Control.max_len_string_attibute}));
    }

    @Test
    public void testReadFileErrorCarOwnerNameTooLong() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        car.setOwnerName("ABCDEFGHIJKLM");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, MessageFormat.format(Bundle
                .getMessage("carAttribute"), new Object[]{Control.max_len_string_attibute}));
    }

    @Test
    public void testReadFileErrorCarBuiltTooLong() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        car.setBuilt("ABCDEF");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, MessageFormat.format(Bundle
                .getMessage("carAttribute"), new Object[]{Control.max_len_string_built_name}));
    }

    @Test
    public void testReadFileErrorCarLocationNameTooLong() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        Location l = car.getLocation();
        l.setName("ABCDEFGHIJKLMNOPQRSTUVWXYZA");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, MessageFormat.format(Bundle
                .getMessage("carAttribute"), new Object[]{Control.max_len_string_location_name}));
    }

    @Test
    public void testReadFileErrorCarTrackNameTooLong() {
        JUnitOperationsUtil.initOperationsData();
        // check number of cars in operations data
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("cars", 9, cm.getNumEntries());

        Car car = cm.getByRoadAndNumber("CP", "X10001");
        Track t = car.getTrack();
        t.setName("ABCDEFGHIJKLMNOPQRSTUVWXYZA");

        // export cars to create file
        exportCars();

        // do import
        importCars(true, MessageFormat.format(Bundle
                .getMessage("carAttribute"), new Object[]{Control.max_len_string_track_name}));
    }

    /*
     * Exports cars and then removes all of them from the roster.
     */
    private void exportCars() {
        // export cars to create file
        CarManager cm = InstanceManager.getDefault(CarManager.class);
        List<Car> carList = cm.getByIdList();
        ExportCars exportCars = new ExportCars(carList);
        Assert.assertNotNull("exists", exportCars);

        // should cause export complete dialog to appear
        Thread exportThread = new Thread(exportCars::writeOperationsCarFile);
        exportThread.setName("Export Cars"); // NOI18N
        exportThread.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return exportThread.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("ExportComplete"), Bundle.getMessage("ButtonOK"));

        try {
            exportThread.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        File file = new File(ExportCars.defaultOperationsFilename());
        Assert.assertTrue("Confirm file creation", file.exists());

        // delete all cars
        cm.deleteAll();
        Assert.assertEquals("cars", 0, cm.getNumEntries());
    }

    private void importCars(boolean failure, String title) {
        importCars(failure, title, Bundle.getMessage("ButtonOK"));
    }

    private void importCars(boolean failure, String title1, String button1) {
        importCars(failure, title1, button1, null, null);
    }

    private void importCars(boolean failure, String title1, String button1, String title2, String button2) {
        importCars(failure, title1, button1, title2, button2, null, null);
    }

    /*
     * title of error dialog, button name.
     */
    private void importCars(boolean failure, String title1, String button1, String title2, String button2,
            String title3, String button3) {
        // do import
        Thread importThread = new ImportCars() {
            @Override
            protected File getFile() {
                // replace JFileChooser with fixed file to avoid threading
                // issues
                return new File(OperationsXml.getFileLocation() +
                        OperationsXml.getOperationsDirectoryName() +
                        File.separator +
                        ExportCars.getOperationsFileName());
            }
        };
        importThread.setName("Test Import Cars"); // NOI18N
        importThread.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return importThread.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        // up to 3 dialog windows can appear
        if (title1 != null) {
            JemmyUtil.pressDialogButton(title1, button1);
        }
        if (title2 != null) {
            JemmyUtil.pressDialogButton(title2, button2);
        }
        if (title3 != null) {
            JemmyUtil.pressDialogButton(title3, button3);
        }

        if (failure) {
            // wait for import failed
            JemmyUtil.pressDialogButton(Bundle.getMessage("ImportFailed"), Bundle.getMessage("ButtonOK"));
        } else {
            // wait for import complete and acknowledge
            JemmyUtil.pressDialogButton(Bundle.getMessage("SuccessfulImport"), Bundle.getMessage("ButtonOK"));
        }

        try {
            importThread.join();
        } catch (InterruptedException e) {
            // do nothing
        }
    }
}
