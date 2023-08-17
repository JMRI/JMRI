package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainRoadOptionsFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainRoadOptionsFrame t = new TrainRoadOptionsFrame();
        Assert.assertNotNull("exists",t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testNoTrain() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainRoadOptionsFrame f = new TrainRoadOptionsFrame();
        Assert.assertNotNull("exists", f);
        TrainEditFrame trainEditFrame = new TrainEditFrame(null);
        f.initComponents(trainEditFrame);
        Assert.assertFalse("Save button disabled", f.saveTrainButton.isEnabled());
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testSaveCarRoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainRoadOptionsFrame f = new TrainRoadOptionsFrame();
        Assert.assertNotNull("exists", f);
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.newTrain("Test");
        TrainEditFrame trainEditFrame = new TrainEditFrame(train);
        f.initComponents(trainEditFrame);
        Assert.assertTrue("Save button enabled", f.saveTrainButton.isEnabled());

        // only accept "AA" road
        JemmyUtil.enterClickAndLeave(f.carRoadNameInclude);
        JemmyUtil.enterClickAndLeave(f.addCarRoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts AA road", train.isCarRoadNameAccepted("AA"));
        Assert.assertTrue("Train accepts AA road for locos", train.isLocoRoadNameAccepted("AA"));
        Assert.assertFalse("Train does not accepts SP road", train.isCarRoadNameAccepted("SP"));

        // delete "AA" road
        f.comboBoxCarRoads.setSelectedItem("AA");
        JemmyUtil.enterClickAndLeave(f.deleteCarRoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertFalse("Train does not accepts AA road", train.isCarRoadNameAccepted("AA"));
        Assert.assertTrue("Train accepts AA road for locos", train.isLocoRoadNameAccepted("AA"));
        
        // each add road name bumps the road name displayed order AA, ACL, ADCX
        JemmyUtil.enterClickAndLeave(f.addCarRoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts ACL road", train.isCarRoadNameAccepted("ACL"));

        // delete all
        JemmyUtil.enterClickAndLeave(f.deleteCarAllRoadsButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertFalse("Train does not accepts ACL road", train.isCarRoadNameAccepted("ACL"));
        Assert.assertTrue("Train accepts ACL road for locos", train.isLocoRoadNameAccepted("ACL"));

        // accept all roads
        JemmyUtil.enterClickAndLeave(f.carRoadNameAll);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts AA road", train.isCarRoadNameAccepted("AA"));

        // exclude "ADCX" road
        JemmyUtil.enterClickAndLeave(f.carRoadNameExclude);
        JemmyUtil.enterClickAndLeave(f.addCarRoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertFalse("Train does not accepts ADCX road", train.isCarRoadNameAccepted("ADCX"));
        Assert.assertTrue("Train accepts AA road", train.isCarRoadNameAccepted("AA"));
        Assert.assertTrue("Train accepts ADCX road for locos", train.isLocoRoadNameAccepted("ADCX"));
        Assert.assertTrue("Train accepts SP road", train.isCarRoadNameAccepted("SP"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testSaveLocoRoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainRoadOptionsFrame f = new TrainRoadOptionsFrame();
        Assert.assertNotNull("exists", f);
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.newTrain("Test");
        TrainEditFrame trainEditFrame = new TrainEditFrame(train);
        f.initComponents(trainEditFrame);
        Assert.assertTrue("Save button enabled", f.saveTrainButton.isEnabled());

        // only accept "AA" road
        JemmyUtil.enterClickAndLeave(f.locoRoadNameInclude);
        JemmyUtil.enterClickAndLeave(f.addLocoRoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts AA road", train.isLocoRoadNameAccepted("AA"));
        Assert.assertTrue("Train accepts AA road for cars", train.isCarRoadNameAccepted("AA"));
        Assert.assertFalse("Train does not accepts SP road", train.isLocoRoadNameAccepted("SP"));

        // delete "AA" road
        f.comboBoxLocoRoads.setSelectedItem("AA");
        JemmyUtil.enterClickAndLeave(f.deleteLocoRoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertFalse("Train does not accepts AA road", train.isLocoRoadNameAccepted("AA"));
        Assert.assertTrue("Train accepts AA road for cars", train.isCarRoadNameAccepted("AA"));
        
        // each add road name bumps the road name displayed order AA, ACL, ADCX
        JemmyUtil.enterClickAndLeave(f.addLocoRoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts ACL road", train.isLocoRoadNameAccepted("ACL"));

        // delete all
        JemmyUtil.enterClickAndLeave(f.deleteLocoAllRoadsButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertFalse("Train does not accepts ACL road", train.isLocoRoadNameAccepted("ACL"));
        Assert.assertTrue("Train accepts ACL road for cars", train.isCarRoadNameAccepted("ACL"));

        // accept all roads
        JemmyUtil.enterClickAndLeave(f.locoRoadNameAll);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts AA road", train.isLocoRoadNameAccepted("AA"));

        // exclude "ADCX" road
        JemmyUtil.enterClickAndLeave(f.locoRoadNameExclude);
        JemmyUtil.enterClickAndLeave(f.addLocoRoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertFalse("Train does not accepts ADCX road", train.isLocoRoadNameAccepted("ADCX"));
        Assert.assertTrue("Train accepts AA road", train.isLocoRoadNameAccepted("AA"));
        Assert.assertTrue("Train accepts ADCX road for cars", train.isCarRoadNameAccepted("ADCX"));
        Assert.assertTrue("Train accepts SP road", train.isLocoRoadNameAccepted("SP"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testPropertyChange() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainRoadOptionsFrame f = new TrainRoadOptionsFrame();
        Assert.assertNotNull("exists", f);
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.newTrain("Test");
        TrainEditFrame trainEditFrame = new TrainEditFrame(train);
        f.initComponents(trainEditFrame);
        Assert.assertTrue("Save button enabled", f.saveTrainButton.isEnabled());
        
        // test road property change
        CarRoads cr = InstanceManager.getDefault(CarRoads.class);
        cr.addName("AAA");
        
        Assert.assertEquals("Confirm new load added", "AAA", f.comboBoxCarRoads.getItemAt(1));
        
        JUnitUtil.dispose(f);
    }

    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.newTrain("Test");
        TrainEditFrame trainEditFrame = new TrainEditFrame(train);
        TrainRoadOptionsFrame f = new TrainRoadOptionsFrame();
        f.initComponents(trainEditFrame);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }
    // private final static Logger log = LoggerFactory.getLogger(TrainRoadOptionsFrameTest.class);

}
