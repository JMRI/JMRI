package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JButtonOperator;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainConductorFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainConductorFrame t = new TrainConductorFrame(null);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testMoveButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Setup.setPrintLoadsAndEmptiesEnabled(true);
        
        JUnitOperationsUtil.initOperationsData();
        EngineManager emanager = InstanceManager.getDefault(EngineManager.class);
        Engine e1 = emanager.getByRoadAndNumber("PC", "5016");
        Engine e2 = emanager.getByRoadAndNumber("PC", "5019");

        Location northend = InstanceManager.getDefault(LocationManager.class).getLocationById("1");
        Track northendStaging1 = northend.getTrackById("1s1");

        // Place Engines on Staging tracks
        Assert.assertEquals("Place e1", Track.OKAY, e1.setLocation(northend, northendStaging1));
        Assert.assertEquals("Place e2", Track.OKAY, e2.setLocation(northend, northendStaging1));
        
        // improve test coverage by using utility car
        CarManager cmanager = InstanceManager.getDefault(CarManager.class);
        Car c1 = cmanager.getByRoadAndNumber("CP", "X10001");
        c1.setUtility(true);
        
        // Create local move
        Location locationNorthIndustries = InstanceManager.getDefault(LocationManager.class).getLocationById("20");
        Track spur = locationNorthIndustries.addTrack("Spur", Track.SPUR);
        spur.setLength(300);
        Car car = cmanager.getByRoadAndNumber("CP", "777");
        car.setFinalDestination(locationNorthIndustries);
        car.setFinalDestinationTrack(spur);
        car.setUtility(true);
        
        Train train2 = InstanceManager.getDefault(TrainManager.class).getTrainById("2");
        train2.setNumberEngines("2");
        
        TrainConductorFrame f = new TrainConductorFrame(train2);
        TrainConductorPanel p = (TrainConductorPanel) f.getContentPane();
        
        // update panel by building train
        Assert.assertTrue(train2.build());
        Assert.assertTrue(train2.isBuilt());
        
        // it can take awhile before the train is built and the GUI updated
        jmri.util.JUnitUtil.waitFor(() -> {
            return p.modifyButton.isEnabled();
        }, "wait for modify button to be enabled");
        
        // Find conductor window by name
        JFrameOperator jfoc = new JFrameOperator(
                Bundle.getMessage("TitleTrainConductor") + " (" + train2.getName() + ")");
        
        // Need to select all before moving train
        JButtonOperator jboSelectAll = new JButtonOperator(jfoc, Bundle.getMessage("SelectAll"));
        jmri.util.JUnitUtil.waitFor(() -> {
            return jboSelectAll.isEnabled();
        }, "wait for button to be enabled");
        jboSelectAll.doClick();
        
        JButtonOperator jboMove = new JButtonOperator(jfoc, Bundle.getMessage("Move"));
        jmri.util.JUnitUtil.waitFor(() -> {
            return jboMove.isEnabled();
        }, "wait for move button to be enabled 3");
        
        // Move train using conductor window       
        jboMove.doClick();
        Assert.assertEquals("Train moved", "North Industries", train2.getCurrentLocationName());
        
        // move to next location
        Assert.assertTrue("confirm button is enabled", jboSelectAll.isEnabled());
        jboSelectAll.doClick();
        jmri.util.JUnitUtil.waitFor(() -> {
            return jboMove.isEnabled();
        }, "wait for move button to be enabled 4");
        jboMove.doClick();
        Assert.assertEquals("Train moved", "South End Staging", train2.getCurrentLocationName());
        
        // terminate train
        Assert.assertTrue("confirm button is enabled", jboSelectAll.isEnabled());
        jboSelectAll.doClick();
        jmri.util.JUnitUtil.waitFor(() -> {
            return jboMove.isEnabled();
        }, "wait for move button to be enabled 5");
        jboMove.doClick();
        Assert.assertFalse(train2.isBuilt());

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testModifyButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        Setup.setPrintLoadsAndEmptiesEnabled(true);
        Setup.setTabEnabled(true);
        
        JUnitOperationsUtil.initOperationsData();
         
        Train train2 = InstanceManager.getDefault(TrainManager.class).getTrainById("2");
        
        // improve test coverage
        train2.setRailroadName("SFF Railroad Name");
        train2.setComment("SFF comment for testing");
        
        Assert.assertTrue(train2.build()); // build train
        Assert.assertTrue(train2.isBuilt());
        
        TrainConductorFrame f = new TrainConductorFrame(train2);        
        TrainConductorPanel p = (TrainConductorPanel) f.getContentPane();
        JemmyUtil.enterClickAndLeaveThreadSafe(p.modifyButton);
        
        // dialog window should appear
        JemmyUtil.pressDialogButton(Bundle.getMessage("AddCarsToTrain?"), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        
        Assert.assertFalse(p.selectButton.isEnabled());
        // the modify button text becomes "Done"
        Assert.assertEquals("Button text", Bundle.getMessage("Done"), p.modifyButton.getText());
        Assert.assertTrue("confirm button is enabled", p.modifyButton.isEnabled());
        JemmyUtil.enterClickAndLeave(p.modifyButton);
        Assert.assertTrue(p.selectButton.isEnabled());
        
        JUnitUtil.dispose(f);
    }
}
