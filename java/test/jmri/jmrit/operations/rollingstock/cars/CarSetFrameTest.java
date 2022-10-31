package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.netbeans.jemmy.operators.*;
import org.netbeans.jemmy.util.NameComponentChooser;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.divisions.DivisionEditFrame;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations CarSetFrame class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
@DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
public class CarSetFrameTest extends OperationsTestCase {

    @Test
    public void testCarSetFrame() {
        JUnitOperationsUtil.initOperationsData();

        // improve test coverage
        Setup.setCarRoutingEnabled(false);

        CarSetFrame f = new CarSetFrame();
        f.setTitle("Test Car Set Frame");
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents();
        });

        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        f.load(c3);

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
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents();
        });
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        f.load(c3);

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
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Car c4 = cManager.getByRoadAndNumber("CP", "99");
        Assert.assertNotNull("car exists", c3);
        Assert.assertNotNull("car exists", c4);
        f.load(c3);

        Kernel k = InstanceManager.getDefault(KernelManager.class).newKernel("test");
        c3.setKernel(k);
        c4.setKernel(k);
        jfo.getQueueTool().waitEmpty();

        Assert.assertEquals("confirm kernel", "test",
                new JComboBoxOperator(jfo, new NameComponentChooser("kernelComboBox")).getItemAt(1));

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(
                MessageFormat.format(Bundle.getMessage("carPartKernel"), c3.getKernelName()),
                Bundle.getMessage("ButtonYes"));

        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        jfo.getQueueTool().waitEmpty();

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        });
        JemmyUtil.waitFor(f); // wait for frame to become active
        jfo.requestClose();
        jfo.waitClosed();
    }

    @Test
    public void testEditCarLoadButton() {
        JUnitOperationsUtil.initOperationsData();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");

        CarSetFrame f = new CarSetFrame();
        f.initComponents();
        f.load(c3);

        JemmyUtil.enterClickAndLeave(f.editLoadButton);
        // confirm edit boxcar loads frame creation
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame("Edit Boxcar Loads") != null;
        }, "ebl not null");
        JmriJFrame lef = JmriJFrame.getFrame("Edit Boxcar Loads");
        Assert.assertNotNull(lef);

        // for test coverage
        JemmyUtil.enterClickAndLeave(f.editLoadButton);

        JUnitUtil.dispose(f);
        lef = JmriJFrame.getFrame("Edit Boxcar Loads");
        Assert.assertNull(lef);
    }

    @Test
    public void testEditCarKernelButton() {
        JUnitOperationsUtil.initOperationsData();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");

        CarSetFrame f = new CarSetFrame();
        f.initComponents();
        f.load(c3);

        JemmyUtil.enterClickAndLeave(f.editKernelButton);
        // confirm edit boxcar loads frame creation
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame("Edit Car Kernel") != null;
        }, "not null");
        JmriJFrame lef = JmriJFrame.getFrame("Edit Car Kernel");
        Assert.assertNotNull(lef);

        // for test coverage
        JemmyUtil.enterClickAndLeave(f.editKernelButton);

        JUnitUtil.dispose(f);
        lef = JmriJFrame.getFrame("Edit Car Kernel");
        Assert.assertNull(lef);
    }

    @Test
    public void testEditDivisionButton() {
        JUnitOperationsUtil.initOperationsData();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");

        CarSetFrame f = new CarSetFrame();
        f.initComponents();
        f.load(c3);

        JemmyUtil.enterClickAndLeave(f.editDivisionButton);
        // confirm edit boxcar loads frame creation
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame("Add Division") != null;
        }, "not null");
        JmriJFrame adf = JmriJFrame.getFrame("Add Division");
        Assert.assertNotNull(adf);
        
        // no divisions yet (Null)
        Assert.assertEquals("number of items", 1, f.divisionComboBox.getItemCount());
        
        // add a division
        DivisionEditFrame def = (DivisionEditFrame)adf;
        JFrameOperator jfo = new JFrameOperator(def.getTitle());
        JTextFieldOperator nameOperator = new JTextFieldOperator(jfo, new NameComponentChooser("DivisionNameField"));
        nameOperator.setText("new division name");
        new JButtonOperator(jfo, "Add Division").doClick();
        Assert.assertEquals("number of items", 2, f.divisionComboBox.getItemCount());

        // for test coverage
        JemmyUtil.enterClickAndLeave(f.editDivisionButton);

        JUnitUtil.dispose(f);
        adf = JmriJFrame.getFrame("Add Division");
        Assert.assertNull(adf);
    }
    
    @Test
    public void testLoadChange() {
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        Track track1 = loc.getTrackByName("Test Location Spur 1", null);
        Car car = JUnitOperationsUtil.createAndPlaceCar("DB", "1", "Boxcar", "40", track1, 0);

        CarSetFrame f = new CarSetFrame();
        f.initComponents();
        f.load(car);
        
        // car has two loads "E" and "L"
        Assert.assertEquals("default loads", 2, f.loadComboBox.getItemCount());
        Assert.assertEquals("default loads", 1, f.loadReturnWhenEmptyBox.getItemCount());
        Assert.assertEquals("default loads", 1, f.loadReturnWhenLoadedBox.getItemCount());
        
        // create a new load for car, type "load"
        CarLoads carLoads = InstanceManager.getDefault(CarLoads.class);
        carLoads.addName("Boxcar", "NewLoad");
        
        Assert.assertEquals("loads", 3, f.loadComboBox.getItemCount());
        Assert.assertEquals("empty loads", 1, f.loadReturnWhenEmptyBox.getItemCount());
        Assert.assertEquals("load loads", 2, f.loadReturnWhenLoadedBox.getItemCount());
        
        // change new load to type "empty"
        carLoads.setLoadType("Boxcar", "NewLoad", CarLoad.LOAD_TYPE_EMPTY);
        Assert.assertEquals("loads", 3, f.loadComboBox.getItemCount());
        Assert.assertEquals("empty loads", 2, f.loadReturnWhenEmptyBox.getItemCount());
        Assert.assertEquals("load loads", 1, f.loadReturnWhenLoadedBox.getItemCount());
        
        // Confirm load change
        f.loadComboBox.setSelectedItem("NewLoad");
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertEquals("load change", "NewLoad", car.getLoadName());
        
        JUnitUtil.dispose(f);
    }

    @Test
    public void testAutoTrackCheckBoxes() {
        JUnitOperationsUtil.initOperationsData();
        // creates 6 tracks
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        Track track1 = loc.getTrackByName("Test Location Spur 1", null);
        Car car = JUnitOperationsUtil.createAndPlaceCar("DB", "1", "Boxcar", "40", track1, 0);

        CarSetFrame f = new CarSetFrame();
        f.initComponents();
        f.load(car);

        // don't allow Boxcar at spur 2
        Track track2 = loc.getTrackByName("Test Location Spur 2", null);
        track2.deleteTypeName("Boxcar");
        Assert.assertEquals("Items in track combobox", 7, f.trackLocationBox.getItemCount());

        JemmyUtil.enterClickAndLeave(f.autoTrackCheckBox);
        Assert.assertEquals("Items in track combobox", 6, f.trackLocationBox.getItemCount());

        // load destination and track ComboBoxes
        Assert.assertEquals("Items in track combobox", 0, f.trackDestinationBox.getItemCount());
        Track track3 = loc.getTrackByName("Test Location Yard 1", null);
        car.setDestination(loc, track3);
        Assert.assertEquals("Items in track combobox", 7, f.trackDestinationBox.getItemCount());

        // must enable destination fields
        f.setDestinationEnabled(true);
        JemmyUtil.enterClickAndLeave(f.autoDestinationTrackCheckBox);
        Assert.assertEquals("Items in track combobox", 6, f.trackDestinationBox.getItemCount());

        // final destination
        Assert.assertEquals("Items in final track combobox", 0, f.finalDestTrackBox.getItemCount());
        car.setFinalDestination(loc);
        Assert.assertEquals("Items in final track combobox", 7, f.finalDestTrackBox.getItemCount());
        JemmyUtil.enterClickAndLeave(f.autoFinalDestTrackCheckBox);
        Assert.assertEquals("Items in final track combobox", 6, f.finalDestTrackBox.getItemCount());
        car.setFinalDestination(null);
        Assert.assertEquals("Items in final track combobox", 0, f.finalDestTrackBox.getItemCount());
        
        // RWE
        Assert.assertEquals("Items in RWE track combobox", 0, f.trackReturnWhenEmptyBox.getItemCount());
        car.setReturnWhenEmptyDestination(loc);
        Assert.assertEquals("Items in RWE track combobox", 7, f.trackReturnWhenEmptyBox.getItemCount());
        JemmyUtil.enterClickAndLeave(f.autoReturnWhenEmptyTrackCheckBox);
        Assert.assertEquals("Items in RWE track combobox", 6, f.trackReturnWhenEmptyBox.getItemCount());
        car.setReturnWhenEmptyDestination(null);
        Assert.assertEquals("Items in RWE track combobox", 0, f.trackReturnWhenEmptyBox.getItemCount());
        
        // RWL
        Assert.assertEquals("Items in RWL track combobox", 0, f.trackReturnWhenLoadedBox.getItemCount());
        car.setReturnWhenLoadedDestination(loc);
        Assert.assertEquals("Items in RWL track combobox", 7, f.trackReturnWhenLoadedBox.getItemCount());
        JemmyUtil.enterClickAndLeave(f.autoReturnWhenLoadedTrackCheckBox);
        Assert.assertEquals("Items in RWL track combobox", 6, f.trackReturnWhenLoadedBox.getItemCount());
        car.setReturnWhenLoadedDestination(null);
        Assert.assertEquals("Items in RWL track combobox", 0, f.trackReturnWhenLoadedBox.getItemCount());

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testCarSetFrameIgnoreCheckBoxes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");

        CarSetFrame f = new CarSetFrame();
        f.initComponents();
        f.load(c3);

        Assert.assertFalse("Ignore deselected", f.ignoreRWECheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreRWLCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreLoadCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreDivisionCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreKernelCheckBox.isSelected());

        Assert.assertTrue(f.destReturnWhenEmptyBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreRWECheckBox);
        Assert.assertFalse(f.destReturnWhenEmptyBox.isEnabled());
        
        Assert.assertTrue(f.destReturnWhenLoadedBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreRWLCheckBox);
        Assert.assertFalse(f.destReturnWhenLoadedBox.isEnabled());
        
        Assert.assertTrue(f.loadComboBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreLoadCheckBox);
        Assert.assertFalse(f.loadComboBox.isEnabled());
        
        Assert.assertTrue(f.divisionComboBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreDivisionCheckBox);
        Assert.assertFalse(f.divisionComboBox.isEnabled());
     
        Assert.assertTrue(f.kernelComboBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreKernelCheckBox);
        Assert.assertFalse(f.kernelComboBox.isEnabled());
        
        // now restore
        Assert.assertFalse(f.destReturnWhenEmptyBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreRWECheckBox);
        Assert.assertTrue(f.destReturnWhenEmptyBox.isEnabled());
        
        Assert.assertFalse(f.destReturnWhenLoadedBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreRWLCheckBox);
        Assert.assertTrue(f.destReturnWhenLoadedBox.isEnabled());
        
        Assert.assertFalse(f.loadComboBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreLoadCheckBox);
        Assert.assertTrue(f.loadComboBox.isEnabled());
        
        Assert.assertFalse(f.divisionComboBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreDivisionCheckBox);
        Assert.assertTrue(f.divisionComboBox.isEnabled());
     
        Assert.assertFalse(f.kernelComboBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreKernelCheckBox);
        Assert.assertTrue(f.kernelComboBox.isEnabled()); 
     
        JUnitUtil.dispose(f);
    }

    @Test
    public void testChangeLocation() {
        JUnitOperationsUtil.initOperationsData();
        // creates 6 tracks
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        Track track1 = loc.getTrackByName("Test Location Spur 1", null);
        Car car = JUnitOperationsUtil.createAndPlaceCar("DB", "1", "Boxcar", "40", track1, 0);

        CarSetFrame f = new CarSetFrame();
        f.initComponents();
        f.load(car);

        // change car's track to track2
        Track track2 = loc.getTrackByName("Test Location Spur 2", null);
        f.trackLocationBox.setSelectedItem(track2);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertEquals("car's track", track2, car.getTrack());

        // cause an error by not allowing Boxcar on track 1
        track1.deleteTypeName("Boxcar");
        f.trackLocationBox.setSelectedItem(track1);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));
        // No
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("rsOverride"), new Object[]{"type (Boxcar)"}),
                Bundle.getMessage("ButtonNo"));
        
        // confirm car's track didn't change
        Assert.assertEquals("car's track", track2, car.getTrack());
        
        // again, but Yes
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("rsOverride"), new Object[]{"type (Boxcar)"}),
                Bundle.getMessage("ButtonYes"));
        Assert.assertEquals("car's track", track1, car.getTrack());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAutoTrainCheckBox() {
        JUnitOperationsUtil.initOperationsData();
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        Track track = loc.getTrackByName("Test Location Spur 1", null);
        Car car = JUnitOperationsUtil.createAndPlaceCar("DB", "1", "Boxcar", "40", track, 0);

        CarSetFrame f = new CarSetFrame();
        f.initComponents();
        f.load(car);

        // two trains and null are the comboBox choices
        Assert.assertEquals("Items in train combobox", 3, f.trainBox.getItemCount());

        // car is on a track that isn't serviced by the two trains
        JemmyUtil.enterClickAndLeave(f.autoTrainCheckBox);
        Assert.assertEquals("Items in train combobox", 1, f.trainBox.getItemCount());

        JUnitUtil.dispose(f);
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
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain());

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"),
                Bundle.getMessage("ButtonOK"));

        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.load(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        });
        JUnitUtil.waitFor(() -> {
            return !load.isAlive();
        });

        jfo.getQueueTool().waitEmpty();

        // pressing "Save" when car has destination and train will cause dialog
        // box to appear
        Assert.assertNotNull("car has destination", c3.getDestination());
        Assert.assertNotNull("car has destination track", c3.getDestinationTrack());

        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"),
                Bundle.getMessage("ButtonNo"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        });

        // Confirm that car's destination is still there
        Assert.assertNotNull("car has destination", c3.getDestination());
        Assert.assertNotNull("car has destination track", c3.getDestinationTrack());

        Thread t3 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"),
                Bundle.getMessage("ButtonYes"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        JUnitUtil.waitFor(() -> {
            return !t3.isAlive();
        });

        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        Assert.assertNull("car has destination removed", c3.getDestination());
        Assert.assertNull("car has destination track removed", c3.getDestinationTrack());

        jfo.requestClose();
        jfo.waitClosed();

        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testChangeTrain() {
        JUnitOperationsUtil.initOperationsData();

        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull(train1);
        Train train2 = tmanager.getTrainByName("SFF");
        Assert.assertNotNull(train2);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        c3.setTrain(train1);

        CarSetFrame f = new CarSetFrame();
        f.initComponents();
        f.load(c3);

        Assert.assertEquals("correct number of items", 3, f.trainBox.getItemCount());
        Assert.assertEquals("correct train selected", train1, f.trainBox.getSelectedItem());
        c3.setTrain(train2);
        Assert.assertEquals("correct train selected", train2, f.trainBox.getSelectedItem());

        // add a new train
        tmanager.newTrain("So New");
        Assert.assertEquals("correct number of items", 4, f.trainBox.getItemCount());

        JUnitUtil.dispose(f);
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
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain());

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"),
                Bundle.getMessage("ButtonOK"));

        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.load(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        });
        JUnitUtil.waitFor(() -> {
            return !load.isAlive();
        });

        jfo.getQueueTool().waitEmpty();

        // remove this type from train
        train1.deleteTypeName(c3.getTypeName());

        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsNotMove"),
                Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        });
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
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain());

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"),
                Bundle.getMessage("ButtonOK"));

        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.load(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        });
        JUnitUtil.waitFor(() -> {
            return !load.isAlive();
        });

        jfo.getQueueTool().waitEmpty();

        // Exclude car's road name from train
        train1.setCarRoadOption(Train.EXCLUDE_ROADS);
        train1.addCarRoadName(c3.getRoadName());

        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsNotMove"),
                Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        });
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
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain());

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"),
                Bundle.getMessage("ButtonOK"));

        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.load(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        });
        JUnitUtil.waitFor(() -> {
            return !load.isAlive();
        });

        jfo.getQueueTool().waitEmpty();

        // Exclude car's built date from train
        train1.setBuiltEndYear("1984");
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsNotMove"),
                Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        });
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
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain());

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"),
                Bundle.getMessage("ButtonOK"));

        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.load(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        });
        JUnitUtil.waitFor(() -> {
            return !load.isAlive();
        });

        jfo.getQueueTool().waitEmpty();

        // Exclude car's owner name from train
        train1.setOwnerOption(Train.EXCLUDE_OWNERS);
        train1.addOwnerName(c3.getOwnerName());
        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsNotMove"),
                Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        });
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
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain());

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"),
                Bundle.getMessage("ButtonOK"));
        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.load(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        });
        JUnitUtil.waitFor(() -> {
            return !load.isAlive();
        });

        jfo.getQueueTool().waitEmpty();

        // Set car's location outside of train's route
        Location westford = JUnitOperationsUtil.createOneNormalLocation("Westford");
        Track track = westford.getTracksList().get(0);
        Assert.assertEquals("Confirm car placement", Track.OKAY, c3.setLocation(westford, track));

        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsNotMove"),
                Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        });
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
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents();
        });
        JFrameOperator jfo = new JFrameOperator(f.getTitle());
        Assert.assertNotNull(jfo);

        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain());

        Thread t1 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsInRoute"),
                Bundle.getMessage("ButtonOK"));
        // should cause dialog car in train to appear
        Thread load = new Thread(() -> {
            f.load(c3);
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        });
        JUnitUtil.waitFor(() -> {
            return !load.isAlive();
        });

        jfo.getQueueTool().waitEmpty();

        // Set car's destination outside of train's route
        Location westford = JUnitOperationsUtil.createOneNormalLocation("Westford");
        Track track = westford.getTracksList().get(0);
        Assert.assertEquals("Confirm car destination", Track.OKAY, c3.setDestination(westford, track));

        Assert.assertNotNull("Car's route location exists", c3.getRouteLocation());

        Thread t2 = JemmyUtil.createModalDialogOperatorThread(Bundle.getMessage("rsNotMove"),
                Bundle.getMessage("ButtonOK"));
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        JUnitUtil.waitFor(() -> {
            return !t2.isAlive();
        });
        jfo.getQueueTool().waitEmpty();
        JemmyUtil.waitFor(f); // wait for frame to become active
        Assert.assertEquals("car has still has destination", westford, c3.getDestination());
        Assert.assertNull("Car's route location is removed", c3.getRouteLocation());
        JemmyUtil.waitFor(f); // wait for frame to become active
        jfo.requestClose();
        jfo.waitClosed();
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    private void toggleCheckBoxThenClickSave(JFrameOperator jfo, String jCheckBoxText) {
        new JCheckBoxOperator(jfo, jCheckBoxText).doClick();
        new JButtonOperator(jfo, Bundle.getMessage("ButtonSave")).doClick();
        jfo.getQueueTool().waitEmpty();
    }

}
