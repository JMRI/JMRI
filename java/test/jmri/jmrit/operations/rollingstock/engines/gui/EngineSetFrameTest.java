package jmri.jmrit.operations.rollingstock.engines.gui;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations EnginesSetFrame class
 *
 * @author Dan Boudreau Copyright (C) 2010
 *
 */
public class EngineSetFrameTest extends OperationsTestCase {

    @Test
    public void testEngineSetFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        EngineSetFrame f = new EngineSetFrame();
        f.setTitle("Test Engine Set Frame");
        f.initComponents();
        EngineManager eManager = InstanceManager.getDefault(EngineManager.class);
        Engine e3 = eManager.getByRoadAndNumber("PC", "5016");
        f.load(e3);
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testEnginesInTrain() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        
        // build train
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train1 = tmanager.getTrainByName("STF");
        Assert.assertNotNull("Train exists", train1);
        
        EngineSetFrame f = new EngineSetFrame();
        f.setTitle("Test Engine Set Frame");
        f.initComponents();
        
        // place two engines at start of train's route
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location locationNorthEnd = lmanager.getLocationByName("North End Staging");
        Track trackNorthEnd = locationNorthEnd.getTrackByName("North End 1", null);
        EngineManager eManager = InstanceManager.getDefault(EngineManager.class);
        Engine e1 = eManager.getByRoadAndNumber("PC", "5016");
        Assert.assertNotNull("engine exists", e1);
        e1.setLocation(locationNorthEnd, trackNorthEnd);
        
        Engine e2 = eManager.getByRoadAndNumber("PC", "5019");
        Assert.assertNotNull("engine exists", e2);
        e2.setLocation(locationNorthEnd, trackNorthEnd);
        
        Assert.assertTrue("Train builds", train1.build());
        Assert.assertEquals("Engine is part of train", train1, e1.getTrain()); 
        Assert.assertEquals("Engine is part of train", train1, e2.getTrain()); 

        // should cause dialog engine in train to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(e1);
            }
        });
        load.setName("engine set frame"); // NOI18N
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
        
        JemmyUtil.waitFor(f);
        // pressing "Save" when engine has destination and train will cause dialog box to appear
        Assert.assertNotNull("engine has destination", e1.getDestination());
        Assert.assertNotNull("engine has destination track", e1.getDestinationTrack());

        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonNo"));
        
        // Confirm that engine's destination is still there
        Assert.assertNotNull("engine has destination", e1.getDestination());
        Assert.assertNotNull("engine has destination track", e1.getDestinationTrack());
        
        JemmyUtil.pressDialogButton(Bundle.getMessage("enginePartConsist"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(Bundle.getMessage("rsInRoute"), Bundle.getMessage("ButtonYes"));

        Assert.assertNull("engine has destination removed", e1.getDestination());
        Assert.assertNull("engine has destination track removed", e1.getDestinationTrack());
        
        JemmyUtil.pressDialogButton(Bundle.getMessage("enginePartConsist"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        JUnitUtil.dispose(f);
        JUnitOperationsUtil.checkOperationsShutDownTask();
    }
    
    @Test
    public void testEditConsistButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        EngineSetFrame f = new EngineSetFrame();
        f.initComponents();
        EngineManager eManager = InstanceManager.getDefault(EngineManager.class);
        Engine e3 = eManager.getByRoadAndNumber("PC", "5016");
        f.load(e3);

        JemmyUtil.enterClickAndLeave(f.editConsistButton);
        JUnitUtil.waitFor(() -> {
            return JmriJFrame.getFrame("Edit Locomotive Consist") != null;
        }, "not null");
        JmriJFrame lef = JmriJFrame.getFrame("Edit Locomotive Consist");
        Assert.assertNotNull(lef);

        // for test coverage
        JemmyUtil.enterClickAndLeave(f.editConsistButton);

        JUnitUtil.dispose(f);
        lef = JmriJFrame.getFrame("Edit Locomotive Consist");
        Assert.assertNull(lef);
    }

    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        EngineManager eManager = InstanceManager.getDefault(EngineManager.class);
        Engine e3 = eManager.newRS("DB", "03");
        EngineSetFrame f = new EngineSetFrame();
        f.initComponents();
        f.load(e3);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }
}
