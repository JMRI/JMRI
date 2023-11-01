package jmri.jmrit.operations.rollingstock.cars.tools;

import java.awt.GraphicsEnvironment;

import javax.swing.JTable;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.*;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class CarsSetFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        CarsSetFrame t = new CarsSetFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testCarsSetFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        // create cars table
        CarsSetFrame f = new CarsSetFrame();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        JTable ctm = ctf.carsTable;
        // select CP 888
        ctm.setRowSelectionInterval(2, 2);
        f.initComponents(ctm);
        f.setTitle("Test Cars Set Frame");
 
        // Save button is labeled "Apply"
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JUnitUtil.dispose(ctf);
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    @Test
    public void testCarsSetFrameApplyButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        // create cars table
        CarsSetFrame f = new CarsSetFrame();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        JTable ctm = ctf.carsTable;
        
        Thread initComp = new Thread(new Runnable() {
            @Override
            public void run() {
                f.initComponents(ctm);
            }
        });
        initComp.setName("cars set frame"); // NOI18N
        initComp.start();
        
        // no cars selected dialog should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("carNoneSelected"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);

        // Save button is labeled "Apply"
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // no cars selected dialog should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("carNoneSelected"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        JUnitUtil.dispose(ctf);
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testCarsSetFrameIgnoreAllButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        // create cars table
        CarsSetFrame f = new CarsSetFrame();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        JTable ctm = ctf.carsTable;

        Thread initComp = new Thread(new Runnable() {
            @Override
            public void run() {
                f.initComponents(ctm);
            }
        });
        initComp.setName("cars set frame"); // NOI18N
        initComp.start();
        
        // no cars selected dialog should appear
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("carNoneSelected"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        Assert.assertTrue("Ignore selected", f.ignoreStatusCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreLocationCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreDivisionCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreRWECheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreRWLCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreLoadCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreKernelCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreDestinationCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreFinalDestinationCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreTrainCheckBox.isSelected());

        JemmyUtil.enterClickAndLeave(f.ignoreAllButton);
        
        Assert.assertFalse("Ignore deselected", f.ignoreStatusCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreLocationCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreDivisionCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreRWECheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreRWLCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreLoadCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreKernelCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreDestinationCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreFinalDestinationCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreTrainCheckBox.isSelected());
        
        JemmyUtil.enterClickAndLeave(f.ignoreAllButton);
           
        Assert.assertTrue("Ignore selected", f.ignoreStatusCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreLocationCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreDivisionCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreRWECheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreRWLCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreLoadCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreKernelCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreDestinationCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreFinalDestinationCheckBox.isSelected());
        Assert.assertTrue("Ignore selected", f.ignoreTrainCheckBox.isSelected());

        
        JUnitUtil.dispose(ctf);
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testCarsSetFrameIgnoreCheckBoxes() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        // create cars table
        CarsSetFrame f = new CarsSetFrame();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        JTable ctm = ctf.carsTable;
        
        // select the first three cars CP 99, CP 777, and CP 888
        ctm.setRowSelectionInterval(0, 2);
        f.initComponents(ctm);
        
        JemmyUtil.enterClickAndLeave(f.ignoreAllButton);
        
        Assert.assertFalse("Ignore deselected", f.ignoreStatusCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreLocationCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreDivisionCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreRWECheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreRWLCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreLoadCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreKernelCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreDestinationCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreFinalDestinationCheckBox.isSelected());
        Assert.assertFalse("Ignore deselected", f.ignoreTrainCheckBox.isSelected());

        Assert.assertTrue(f.locationUnknownCheckBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreStatusCheckBox);
        Assert.assertFalse(f.locationUnknownCheckBox.isEnabled());
        
        Assert.assertTrue(f.locationBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreLocationCheckBox);
        Assert.assertFalse(f.locationBox.isEnabled());
                
        // check ignore
        JemmyUtil.enterClickAndLeave(f.ignoreDestinationCheckBox);
        Assert.assertFalse(f.destinationBox.isEnabled());
        
        // uncheck ignore
        JemmyUtil.enterClickAndLeave(f.ignoreDestinationCheckBox);
        Assert.assertTrue(f.destinationBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreDestinationCheckBox);
        Assert.assertFalse(f.destinationBox.isEnabled());
        
        Assert.assertTrue(f.finalDestinationBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreFinalDestinationCheckBox);
        Assert.assertFalse(f.finalDestinationBox.isEnabled());
     
        Assert.assertTrue(f.trainBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreTrainCheckBox);
        Assert.assertFalse(f.trainBox.isEnabled());
        
        // now restore
        Assert.assertFalse(f.locationUnknownCheckBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreStatusCheckBox);
        Assert.assertTrue(f.locationUnknownCheckBox.isEnabled());
        
        Assert.assertFalse(f.locationBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreLocationCheckBox);
        Assert.assertTrue(f.locationBox.isEnabled());
        
        Assert.assertFalse(f.destinationBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreDestinationCheckBox);
        Assert.assertTrue(f.destinationBox.isEnabled());
        
        Assert.assertFalse(f.finalDestinationBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreFinalDestinationCheckBox);
        Assert.assertTrue(f.finalDestinationBox.isEnabled());
     
        Assert.assertFalse(f.trainBox.isEnabled());
        JemmyUtil.enterClickAndLeave(f.ignoreTrainCheckBox);
        Assert.assertTrue(f.trainBox.isEnabled()); 
     
        JUnitUtil.dispose(ctf);
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testCarsSetFrameApplyButtonWithSelections() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        // get a cars table with data
        CarsSetFrame f = new CarsSetFrame();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        JTable ctm = ctf.carsTable;
        
        // select the first three cars CP 99, CP 777, and CP 888
        ctm.setRowSelectionInterval(0, 2);
        f.initComponents(ctm);
        f.setTitle("Test Cars Set Frame");
        
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c99 = cManager.getByRoadAndNumber("CP", "99");
        Assert.assertNotNull("car exists", c99);
        Car c777 = cManager.getByRoadAndNumber("CP", "777");
        Assert.assertNotNull("car exists", c777);
        Car c888 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c888);
        
        // check defaults
        Assert.assertFalse("Out of service", c99.isOutOfService());
        Assert.assertFalse("Out of service", c777.isOutOfService());
        Assert.assertFalse("Out of service", c888.isOutOfService());

        // change the 3 car's status
        JemmyUtil.enterClickAndLeave(f.ignoreStatusCheckBox);
        JemmyUtil.enterClickAndLeave(f.outOfServiceCheckBox);
        // Save button is labeled "Apply"
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        // confirm
        Assert.assertTrue("Out of service", c99.isOutOfService());
        Assert.assertTrue("Out of service", c777.isOutOfService());
        Assert.assertTrue("Out of service", c888.isOutOfService());

        JUnitUtil.dispose(ctf);
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
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
        
        // get a cars table with data
        CarsSetFrame f = new CarsSetFrame();
        CarsTableFrame ctf = new CarsTableFrame(true, null, null);
        JTable ctm = ctf.carsTable;
        
        // select CP 888
        ctm.setRowSelectionInterval(2, 2);
        
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Car c3 = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertNotNull("car exists", c3);
        Assert.assertEquals("Car is part of train", train1, c3.getTrain()); 

        // should cause dialog car in train to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.initComponents(ctm);
            }
        });
        load.setName("car set frame"); // NOI18N
        load.start();

        JemmyUtil.pressDialogButton(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonOK"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }
        
        JemmyUtil.waitFor(f);
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
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }

    // private final static Logger log = LoggerFactory.getLogger(CarsSetFrameTest.class);

}
