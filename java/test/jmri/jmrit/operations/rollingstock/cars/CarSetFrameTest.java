package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;

import javax.swing.JCheckBox;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.QueueTool;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations CarSetFrame class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class CarSetFrameTest extends OperationsTestCase {

    @Test
    public void testCarSetFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        f.loadCar(c3);

        JUnitUtil.dispose(f);

    }

    @Test
    public void testCarSetFrameSaveButton() {
//        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        f.loadCar(c3);

        // check defaults
        Assert.assertFalse("Out of service", c3.isOutOfService());
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        // change car's status
        toggleCheckBoxThenClickSave(f,f.outOfServiceCheckBox);
        JUnitUtil.waitFor(() -> {
            return c3.isOutOfService();
            }, "Out of service");
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        // change car's status
        toggleCheckBoxThenClickSave(f,f.outOfServiceCheckBox);
        JUnitUtil.waitFor(() -> {
            return !c3.isOutOfService();
        }, "Not Out of service");
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        // change car's status
        toggleCheckBoxThenClickSave(f,f.locationUnknownCheckBox);
        // location unknown checkbox also causes the car to be out of service
        JUnitUtil.waitFor(() -> {
            return c3.isOutOfService();
        }, "Out of service Again");
        Assert.assertTrue("Location unknown", c3.isLocationUnknown());
        
        // change car's status
        toggleCheckBoxThenClickSave(f,f.locationUnknownCheckBox);
        // location unknown checkbox also causes the car to be out of service
        JUnitUtil.waitFor(() -> {
            return !c3.isOutOfService();
        }, "Not Out of service Again");
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testKernel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Car c4 = cManager.getByRoadAndNumber("CP", "99");
        Assert.assertNotNull("car exists", c3);
        Assert.assertNotNull("car exists", c4);
        f.loadCar(c3);
        
        Kernel k = InstanceManager.getDefault(KernelManager.class).newKernel("test");
        c3.setKernel(k);
        c4.setKernel(k);
        
        Assert.assertEquals("confirm kernel", "test", f.kernelComboBox.getItemAt(1));
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carPartKernel"), c3.getKernelName()), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCarInTrain() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        // should cause dialog car in train to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.loadCar(c3);
            }
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        
        // pressing "Save" when car has destination and train will cause dialog box to appear
        Assert.assertNotNull("car has destination", c3.getDestination());
        Assert.assertNotNull("car has destination track", c3.getDestinationTrack());

        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        
        // Confirm that car's destination is still there
        Assert.assertNotNull("car has destination", c3.getDestination());
        Assert.assertNotNull("car has destination track", c3.getDestinationTrack());
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);

        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCarInTrainErrorType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        // should cause dialog car in train to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.loadCar(c3);
            }
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        
        // remove this type from train
        train1.deleteTypeName(c3.getTypeName());

        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(Bundle.getMessage("rsNotMove"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCarInTrainErrorRoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        // should cause dialog car in train to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.loadCar(c3);
            }
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        
        // Exclude car's road name from train
        train1.setRoadOption(Train.EXCLUDE_ROADS);
        train1.addRoadName(c3.getRoadName());

        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(Bundle.getMessage("rsNotMove"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCarInTrainErrorBuilt() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        // should cause dialog car in train to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.loadCar(c3);
            }
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        
        // Exclude car's built date from train
        train1.setBuiltEndYear("1984");

        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(Bundle.getMessage("rsNotMove"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCarInTrainErrorOwner() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        // should cause dialog car in train to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.loadCar(c3);
            }
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        
        // Exclude car's owner name from train
        train1.setOwnerOption(Train.EXCLUDE_OWNERS);
        train1.addOwnerName(c3.getOwner());

        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(Bundle.getMessage("rsNotMove"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCarInTrainErrorLocation() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        // should cause dialog car in train to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.loadCar(c3);
            }
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        
        // Set car's location outside of train's route
        Location westford = JUnitOperationsUtil.createOneNormalLocation("Westford");
        Track track = westford.getTracksList().get(0);
        Assert.assertEquals("Confirm car placement", Track.OKAY, c3.setLocation(westford, track));

        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(Bundle.getMessage("rsNotMove"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());
        
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCarInTrainErrorDestination() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        f.initComponents();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        // should cause dialog car in train to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.loadCar(c3);
            }
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        
        // Set car's destination outside of train's route
        Location westford = JUnitOperationsUtil.createOneNormalLocation("Westford");
        Track track = westford.getTracksList().get(0);
        Assert.assertEquals("Confirm car destination", Track.OKAY, c3.setDestination(westford, track));

        Assert.assertNotNull("Car's route location exists", c3.getRouteLocation());
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(Bundle.getMessage("rsNotMove"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        Assert.assertEquals("car has still has destination", westford, c3.getDestination());
        Assert.assertNull("Car's route location is removed", c3.getRouteLocation());
        
        JUnitUtil.dispose(f);
    }

    private void toggleCheckBoxThenClickSave(CarSetFrame frame, JCheckBox box){
        JemmyUtil.enterClickAndLeave(box);
        JemmyUtil.enterClickAndLeave(frame.saveButton);
        new QueueTool().waitEmpty(100);
    }
    
}
