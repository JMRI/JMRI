
package jmri.jmrit.operations.trains;

import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the TrainManager class Last manually cross-checked on 20090131
 *
 * @author Bob Coleman Copyright (C) 2008, 2009
 */
public class TrainManagerTest extends OperationsTestCase {

    // test train manager
    @Test
    public void testTrainManager() {
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);

        // test defaults
        Assert.assertTrue("Build Messages", tmanager.isBuildMessagesEnabled());
        Assert.assertFalse("Build Reports", tmanager.isBuildReportEnabled());
        Assert.assertFalse("Print Preview", tmanager.isPrintPreviewEnabled());

        // Swap them
        tmanager.setBuildMessagesEnabled(false);
        tmanager.setBuildReportEnabled(true);
        tmanager.setPrintPreviewEnabled(true);

        Assert.assertFalse("Build Messages", tmanager.isBuildMessagesEnabled());
        Assert.assertTrue("Build Reports", tmanager.isBuildReportEnabled());
        Assert.assertTrue("Print Preview", tmanager.isPrintPreviewEnabled());

    }

    /**
     * Make sure we can retrieve a train from the manager by name.
     */
    @Test
    public void testGetTrainByName() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        Assert.assertNotNull("Retrieve Train", manager.getTrainByName("STF"));
    }

    /**
     * Make sure we can retrieve a train from the manager by name.
     */
    @Test
    public void testGetTrainById() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        Assert.assertNotNull("Retrieve Train", manager.getTrainById("1"));
    }

    @Test
    public void testTrainCopy() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        Train train = manager.getTrainById("1");
        Train copiedTrain = manager.copyTrain(train, "Copied train");

        Assert.assertEquals("Copied train", copiedTrain.getName());
        Assert.assertEquals(train.getRoute(), copiedTrain.getRoute());
    }

    @Test
    public void testReplaceLoad() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        Train train = manager.getTrainById("1");
        train.setLoadOption(Train.INCLUDE_LOADS);
        train.addLoadName("Nuts");
        train.addLoadName("Boxcar" + CarLoad.SPLIT_CHAR + "Nuts");
        train.addLoadName("Bolts");
        train.addLoadName("Boxcar" + CarLoad.SPLIT_CHAR + "Bolts");

        Assert.assertTrue("confirm load name", train.acceptsLoadName("Nuts"));
        Assert.assertTrue("confirm load name", train.acceptsLoad("Nuts", "Boxcar"));
        Assert.assertTrue("confirm load name", train.acceptsLoadName("Bolts"));
        Assert.assertTrue("confirm load name", train.acceptsLoad("Bolts", "Boxcar"));
        Assert.assertFalse("confirm load name", train.acceptsLoadName("NUTS"));
        Assert.assertFalse("confirm load name", train.acceptsLoadName("BOLTS"));

        manager.replaceLoad("Boxcar", "Nuts", "NUTS");

        Assert.assertTrue("confirm load name", train.acceptsLoadName("NUTS"));
        Assert.assertTrue("confirm load name", train.acceptsLoad("NUTS", "Boxcar"));
        Assert.assertTrue("confirm load name", train.acceptsLoadName("Bolts"));
        Assert.assertTrue("confirm load name", train.acceptsLoad("Bolts", "Boxcar"));
        Assert.assertFalse("confirm load name", train.acceptsLoadName("Nuts"));
        Assert.assertFalse("confirm load name", train.acceptsLoadName("BOLTS"));
        
        // change bolts for all cars except for boxcars and bolts
        manager.replaceLoad("Flat", "Bolts", "BOLTS");
        
        Assert.assertTrue("confirm load name", train.acceptsLoadName("NUTS"));
        Assert.assertTrue("confirm load name", train.acceptsLoad("NUTS", "Boxcar"));
        Assert.assertTrue("confirm load name", train.acceptsLoadName("BOLTS"));
        Assert.assertTrue("confirm load name", train.acceptsLoad("Bolts", "Boxcar")); // not changed
        Assert.assertFalse("confirm load name", train.acceptsLoadName("Nuts"));
        Assert.assertFalse("confirm load name", train.acceptsLoadName("bolts"));    
    }
    
    @Test
    public void testIsAnyTrainBuilt() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        Train train = manager.getTrainById("1");
        
        Assert.assertFalse("no built trains", manager.isAnyTrainBuilt());
        Assert.assertTrue("train built",train.build());
        Assert.assertTrue("One built train", manager.isAnyTrainBuilt());
    }
    
    @Test
    public void testGetTrainsArrivingThisLocationList() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = manager.getTrainById("1");
        Train train2 = manager.getTrainById("2");
        
        Location location = InstanceManager.getDefault(LocationManager.class).getLocationById("3");
        List<Train> trains = manager.getTrainsArrivingThisLocationList(location);
        
        // no trains have been built so no trains arriving South End
        Assert.assertEquals("list size", 0 , trains.size());
        
        Assert.assertTrue(train1.build());
        Assert.assertTrue(train2.build());
        
        trains = manager.getTrainsArrivingThisLocationList(location);
        Assert.assertEquals("list size", 2 , trains.size());
        Assert.assertEquals(train1, trains.get(0));
        Assert.assertEquals(train2, trains.get(1));
        
        // change arrival time
        train2.move();
        
        trains = manager.getTrainsArrivingThisLocationList(location);
        Assert.assertEquals("list size", 2 , trains.size());
        Assert.assertEquals(train2, trains.get(0));
        Assert.assertEquals(train1, trains.get(1));
    }
    
    @Test
    public void testGetTrainsByDepartureList() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
         
        List<Train> trains = manager.getTrainsByDepartureList();
        Assert.assertEquals("list size", 2 , trains.size());
        Assert.assertEquals("STF", trains.get(0).getName());
        Assert.assertEquals("SFF", trains.get(1).getName());
    }
    
    @Test
    public void testGetTrainsByDescriptionList() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
        
        Train train1 = manager.getTrainById("1");
        Train train2 = manager.getTrainById("2");
        train1.setDescription("Bad Train");
        train2.setDescription("A Good Train");
         
        List<Train> trains = manager.getTrainsByDescriptionList();
        Assert.assertEquals("list size", 2 , trains.size());
        Assert.assertEquals("SFF", trains.get(0).getName());
        Assert.assertEquals("STF", trains.get(1).getName());
    }
    
    @Test
    public void testGetTrainsByRouteList() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
         
        List<Train> trains = manager.getTrainsByRouteList();
        Assert.assertEquals("list size", 2 , trains.size());
        Assert.assertEquals("STF", trains.get(0).getName());
        Assert.assertEquals("SFF", trains.get(1).getName());
    }
    
    @Test
    public void testGetTrainsByStatusList() {
        TrainManager manager = InstanceManager.getDefault(TrainManager.class);
         
        List<Train> trains = manager.getTrainsByStatusList();
        Assert.assertEquals("list size", 2 , trains.size());
        Assert.assertEquals("STF", trains.get(0).getName());
        Assert.assertEquals("SFF", trains.get(1).getName());
        
        // change status
        Train train1 = manager.getTrainById("1");
        train1.reset();
        
        trains = manager.getTrainsByStatusList();
        Assert.assertEquals("list size", 2 , trains.size());
        Assert.assertEquals("SFF", trains.get(0).getName());
        Assert.assertEquals("STF", trains.get(1).getName());        
    }

    // from here down is testing infrastructure
    // Ensure minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();

        jmri.util.JUnitOperationsUtil.initOperationsData();
    }

    @After
    @Override
    public void tearDown() {
        super.tearDown();
    }

}
