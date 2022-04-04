package jmri.jmrit.operations.locations;

import jmri.Reporter;
import jmri.implementation.decorators.TimeoutReporter;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.divisions.Division;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import org.junit.jupiter.api.Timeout;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocationManagerTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        LocationManager t = new LocationManager();
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testHasWork() {
        JUnitOperationsUtil.initOperationsData();
        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
        Assert.assertNotNull("exists", lm);
        Assert.assertFalse(lm.hasWork());
        Train train1 = InstanceManager.getDefault(TrainManager.class).getTrainByName("STF");
        Assert.assertNotNull("train exists", train1);
        Assert.assertTrue(train1.build());
        Assert.assertTrue(lm.hasWork());
        
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testReplaceShipLoadNames() {
        JUnitOperationsUtil.createSevenNormalLocations();
        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
        Assert.assertNotNull("exists", lm);
        Location boston = lm.getLocationByName("Boston");
        Assert.assertNotNull("exists", boston);
        Track spur = boston.getTrackByName("Boston Spur 1", null);
        Assert.assertNotNull("exists", spur);
        spur.addShipLoadName("Boxes");
        spur.addShipLoadName("Boxcar" + CarLoad.SPLIT_CHAR + "Paper");
        spur.addShipLoadName("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts");
        spur.setShipLoadOption(Track.INCLUDE_LOADS);
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("Boxes"));
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "Paper"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("boxes"));
        
        lm.replaceLoad("Boxcar", "Boxes", "boxes");
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("Boxes"));
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "Paper"));
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts"));
        
        lm.replaceLoad("Boxcar", "Paper", "tissue");
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("Boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "Paper"));
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "tissue"));
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts"));
        
        lm.replaceLoad("Boxcar", "boxes", null);
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("Boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "Paper"));
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "tissue"));
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts"));

        lm.replaceLoad("boxcar", "Nuts", null);
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("Boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "Paper"));
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "tissue"));
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts"));

        lm.replaceLoad("Boxcar", "Nuts", null);
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("Boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "Paper"));
        Assert.assertTrue("load is accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "tissue"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameShipped("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts"));
    }
    
    @Test
    public void testReplaceLoadNames() {
        JUnitOperationsUtil.createSevenNormalLocations();
        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
        Assert.assertNotNull("exists", lm);
        Location boston = lm.getLocationByName("Boston");
        Assert.assertNotNull("exists", boston);
        Track spur = boston.getTrackByName("Boston Spur 1", null);
        Assert.assertNotNull("exists", spur);
        spur.addLoadName("Boxes");
        spur.addLoadName("Boxcar" + CarLoad.SPLIT_CHAR + "Paper");
        spur.addLoadName("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts");
        spur.setLoadOption(Track.INCLUDE_LOADS);
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("Boxes"));
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "Paper"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("boxes"));
        
        lm.replaceLoad("Boxcar", "Boxes", "boxes");
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("Boxes"));
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "Paper"));
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts"));
        
        lm.replaceLoad("Boxcar", "Paper", "tissue");
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("Boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "Paper"));
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "tissue"));
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts"));
        
        lm.replaceLoad("Boxcar", "boxes", null);
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("Boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "Paper"));
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "tissue"));
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts"));

        lm.replaceLoad("boxcar", "Nuts", null);
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("Boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "Paper"));
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "tissue"));
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts"));

        lm.replaceLoad("Boxcar", "Nuts", null);
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("Boxes"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "Paper"));
        Assert.assertTrue("load is accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "tissue"));
        Assert.assertFalse("load is not accepted", spur.isLoadNameAccepted("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts"));
    }
    
    @Test
    public void testHasReporters() {
        JUnitOperationsUtil.initOperationsData();
        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
        Assert.assertNotNull("exists", lm);
        Assert.assertFalse(lm.hasReporters());
    }
    
    @Test
    public void testHasDivisions() {
        JUnitOperationsUtil.createSevenNormalLocations();
        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
        Assert.assertNotNull("exists", lm);
        Location boston = lm.getLocationByName("Boston");
        Assert.assertNotNull("exists", boston);
        Assert.assertFalse(lm.hasDivisions());
        boston.setDivision(new Division("testId", "testName"));
        Assert.assertTrue(lm.hasDivisions());
    }

    @Test
    void getTrackByReporter() {
        JUnitOperationsUtil.initOperationsData();
        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
        Reporter baseReporter= Mockito.mock(Reporter.class);
        Mockito.when(baseReporter.getSystemName()).thenReturn("foo");
        TimeoutReporter timeoutReporter = new TimeoutReporter(baseReporter);
        Location expectedLocation = lm.getLocationByName("North Industries");
        Track expected = expectedLocation.getTracksList().get(0);
        expected.setReporter(timeoutReporter);
        Assert.assertEquals(expected,lm.getTrackByReporter(timeoutReporter));
        Assert.assertEquals(expected,lm.getTrackByReporter(baseReporter));
    }

    @Test
    void getLocationByReporter() {
        JUnitOperationsUtil.initOperationsData();
        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
        Reporter baseReporter= Mockito.mock(Reporter.class);
        Mockito.when(baseReporter.getSystemName()).thenReturn("foo");
        Location expected = lm.getLocationByName("North Industries");
        TimeoutReporter timeoutReporter = new TimeoutReporter(baseReporter);
        expected.setReporter(timeoutReporter);
        Assert.assertEquals(expected,lm.getLocationByReporter(timeoutReporter));
        Assert.assertEquals(expected,lm.getLocationByReporter(baseReporter));
    }

}
