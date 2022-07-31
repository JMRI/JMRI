package jmri.jmrit.operations.rollingstock.cars;

import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JCheckBoxOperator;
import org.netbeans.jemmy.operators.JComboBoxOperator;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.util.NameComponentChooser;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations CarSetFrame class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
@DisabledIfSystemProperty(named ="java.awt.headless", matches ="true")
public class CarSetFrameTest extends OperationsTestCase {

    @Test
    public void testCarSetFrame() {

        JUnitOperationsUtil.initOperationsData();

        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        ThreadingUtil.runOnGUI( ()-> {
            f.initComponents();
        });

        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        f.loadCar(c3);

        jfo.getQueueTool().waitEmpty();
        jfo.requestClose();
        jfo.waitClosed();

    }

    @Test
    public void testCarSetFrameSaveButton() {
        Assume.assumeFalse("Ignoring intermittent test", Boolean.getBoolean("jmri.skipTestsRequiringSeparateRunning"));

        JUnitOperationsUtil.initOperationsData();

        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        ThreadingUtil.runOnGUI( ()-> {
            f.initComponents();
        });
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        f.loadCar(c3);

        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        // check defaults
        Assert.assertFalse("Out of service", c3.isOutOfService());
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        // change car's status
        toggleCheckBoxThenClickSave(jfo, Bundle.getMessage("OutOfService"));
        JUnitUtil.waitFor(() -> {
            return c3.isOutOfService();
            }, "Out of service");
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        // change car's status
        toggleCheckBoxThenClickSave(jfo, Bundle.getMessage("OutOfService"));
        JUnitUtil.waitFor(() -> {
            return !c3.isOutOfService();
        }, "Not Out of service");
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        // change car's status
        toggleCheckBoxThenClickSave(jfo, Bundle.getMessage("LocationUnknown"));
        // location unknown checkbox also causes the car to be out of service
        JUnitUtil.waitFor(() -> {
            return c3.isOutOfService();
        }, "Out of service Again");
        Assert.assertTrue("Location unknown", c3.isLocationUnknown());
        
        // change car's status
        toggleCheckBoxThenClickSave(jfo, Bundle.getMessage("LocationUnknown"));
        // location unknown checkbox also causes the car to be out of service
        JUnitUtil.waitFor(() -> {
            return !c3.isOutOfService();
        }, "Not Out of service Again");
        Assert.assertFalse("Location unknown", c3.isLocationUnknown());

        jfo.requestClose();
        jfo.waitClosed();

    }
    
    @Test
    public void testKernel() {

        JUnitOperationsUtil.initOperationsData();

        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        ThreadingUtil.runOnGUI( ()-> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Car c4 = cManager.getByRoadAndNumber("CP", "99");
        Assert.assertNotNull("car exists", c3);
        Assert.assertNotNull("car exists", c4);
        f.loadCar(c3);
        
        Kernel k = InstanceManager.getDefault(KernelManager.class).newKernel("test");
        c3.setKernel(k);
        c4.setKernel(k);
        jfo.getQueueTool().waitEmpty();
        
        Assert.assertEquals("confirm kernel", "test",
            new JComboBoxOperator(jfo, new NameComponentChooser("kernelComboBox")).getItemAt(1));

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(
            MessageFormat.format(Bundle.getMessage("carPartKernel"), c3.getKernelName()), Bundle.getMessage("ButtonYes"));
        
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave") ).doClick();
        jfo.getQueueTool().waitEmpty();

        JUnitUtil.waitFor(() -> {return !t1.isAlive();});
        JemmyUtil.waitFor(f); // wait for frame to become active
        jfo.requestClose();
        jfo.waitClosed();

    }
    
    @Test
    public void testCarInTrain() {

        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        ThreadingUtil.runOnGUI( ()-> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));
        
        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.loadCar(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {return !t1.isAlive();});
        JUnitUtil.waitFor(() -> {return !load.isAlive();});

        jfo.getQueueTool().waitEmpty();
        
        // pressing "Save" when car has destination and train will cause dialog box to appear
        Assert.assertNotNull("car has destination", c3.getDestination());
        Assert.assertNotNull("car has destination track", c3.getDestinationTrack());

        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonNo"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave") ).doClick();
        JUnitUtil.waitFor(() -> {return !t2.isAlive();});

        // Confirm that car's destination is still there
        Assert.assertNotNull("car has destination", c3.getDestination());
        Assert.assertNotNull("car has destination track", c3.getDestinationTrack());

        Thread t3 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave") ).doClick();
        JUnitUtil.waitFor(() -> {return !t3.isAlive();});

        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());

        jfo.requestClose();
        jfo.waitClosed();

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testCarInTrainErrorType() {

        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        ThreadingUtil.runOnGUI( ()-> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));

        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.loadCar(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {return !t1.isAlive();});
        JUnitUtil.waitFor(() -> {return !load.isAlive();});

        jfo.getQueueTool().waitEmpty();

        // remove this type from train
        train1.deleteTypeName(c3.getTypeName());

        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsNotMove"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave") ).doClick();
        JUnitUtil.waitFor(() -> {return !t2.isAlive();});
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());
        
        jfo.requestClose();
        jfo.waitClosed();
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testCarInTrainErrorRoad() {

        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        ThreadingUtil.runOnGUI( ()-> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));

        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.loadCar(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {return !t1.isAlive();});
        JUnitUtil.waitFor(() -> {return !load.isAlive();});
        
        jfo.getQueueTool().waitEmpty();

        // Exclude car's road name from train
        train1.setRoadOption(Train.EXCLUDE_ROADS);
        train1.addRoadName(c3.getRoadName());

        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsNotMove"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave") ).doClick();
        JUnitUtil.waitFor(() -> {return !t2.isAlive();});
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());
        
        jfo.requestClose();
        jfo.waitClosed();
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testCarInTrainErrorBuilt() {

        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        ThreadingUtil.runOnGUI( ()-> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));

        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.loadCar(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {return !t1.isAlive();});
        JUnitUtil.waitFor(() -> {return !load.isAlive();});
        
        jfo.getQueueTool().waitEmpty();

        // Exclude car's built date from train
        train1.setBuiltEndYear("1984");
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsNotMove"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave") ).doClick();
        JUnitUtil.waitFor(() -> {return !t2.isAlive();});
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());

        jfo.requestClose();
        jfo.waitClosed();
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testCarInTrainErrorOwner() {

        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        ThreadingUtil.runOnGUI( ()-> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));

        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.loadCar(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {return !t1.isAlive();});
        JUnitUtil.waitFor(() -> {return !load.isAlive();});
        
        jfo.getQueueTool().waitEmpty();

        // Exclude car's owner name from train
        train1.setOwnerOption(Train.EXCLUDE_OWNERS);
        train1.addOwnerName(c3.getOwner());
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsNotMove"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave") ).doClick();
        JUnitUtil.waitFor(() -> {return !t2.isAlive();});
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());
        
        jfo.requestClose();
        jfo.waitClosed();
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testCarInTrainErrorLocation() {

        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        ThreadingUtil.runOnGUI( ()-> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));
        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.loadCar(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {return !t1.isAlive();});
        JUnitUtil.waitFor(() -> {return !load.isAlive();});
        
        jfo.getQueueTool().waitEmpty();
        
        // Set car's location outside of train's route
        Location westford = JUnitOperationsUtil.createOneNormalLocation("Westford");
        Track track = westford.getTracksList().get(0);
        Assert.assertEquals("Confirm car placement", Track.OKAY, c3.setLocation(westford, track));

        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsNotMove"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave") ).doClick();
        JUnitUtil.waitFor(() -> {return !t2.isAlive();});
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());

        jfo.requestClose();
        jfo.waitClosed();
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testCarInTrainErrorDestination() {

        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        Assert.assertTrue("Train builds", train1.build());
        
        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        ThreadingUtil.runOnGUI( ()-> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));
        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.loadCar(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {return !t1.isAlive();});
        JUnitUtil.waitFor(() -> {return !load.isAlive();});
        
        jfo.getQueueTool().waitEmpty();

        // Set car's destination outside of train's route
        Location westford = JUnitOperationsUtil.createOneNormalLocation("Westford");
        Track track = westford.getTracksList().get(0);
        Assert.assertEquals("Confirm car destination", Track.OKAY, c3.setDestination(westford, track));

        Assert.assertNotNull("Car's route location exists", c3.getRouteLocation());
        
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsNotMove"), Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave") ).doClick();
        JUnitUtil.waitFor(() -> {return !t2.isAlive();});
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        Assert.assertEquals("car has still has destination", westford, c3.getDestination());
        Assert.assertNull("Car's route location is removed", c3.getRouteLocation());
        JemmyUtil.waitFor(f); // wait for frame to become active
        jfo.requestClose();
        jfo.waitClosed();
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    private void toggleCheckBoxThenClickSave(JFrameOperator jfo, String jCheckBoxText){
        new JCheckBoxOperator(jfo, jCheckBoxText).doClick();
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave") ).doClick();
        jfo.getQueueTool().waitEmpty();
    }
    
}
