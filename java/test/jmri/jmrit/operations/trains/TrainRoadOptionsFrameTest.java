package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
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
    public void testSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainRoadOptionsFrame f = new TrainRoadOptionsFrame();
        Assert.assertNotNull("exists", f);
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.newTrain("Test");
        TrainEditFrame trainEditFrame = new TrainEditFrame(train);
        f.initComponents(trainEditFrame);
        Assert.assertTrue("Save button enabled", f.saveTrainButton.isEnabled());

        // only accept "AA" road
        JemmyUtil.enterClickAndLeave(f.roadNameInclude);
        JemmyUtil.enterClickAndLeave(f.addRoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts AA road", train.isRoadNameAccepted("AA"));
        Assert.assertFalse("Train does not accepts SP road", train.isRoadNameAccepted("SP"));

        // delete "AA" road
        f.comboBoxRoads.setSelectedItem("AA");
        JemmyUtil.enterClickAndLeave(f.deleteRoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertFalse("Train does not accepts AA road", train.isRoadNameAccepted("AA"));
        
        // each add road name bumps the road name displayed order AA, ACL, ADCX
        JemmyUtil.enterClickAndLeave(f.addRoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts ACL road", train.isRoadNameAccepted("ACL"));

        // delete all
        JemmyUtil.enterClickAndLeave(f.deleteAllRoadsButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertFalse("Train does not accepts ACL road", train.isRoadNameAccepted("ACL"));

        // accept all roads
        JemmyUtil.enterClickAndLeave(f.roadNameAll);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts AA road", train.isRoadNameAccepted("AA"));

        // exclude "AA" road
        JemmyUtil.enterClickAndLeave(f.roadNameExclude);
        JemmyUtil.enterClickAndLeave(f.addRoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertFalse("Train does not accepts ACCX road", train.isRoadNameAccepted("ADCX"));
        Assert.assertTrue("Train does acceptsroad", train.isRoadNameAccepted("SP"));

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
        
        Assert.assertEquals("Confirm new load added", "AAA", f.comboBoxRoads.getItemAt(1));
        
        JUnitUtil.dispose(f);
    }


    // private final static Logger log = LoggerFactory.getLogger(TrainRoadOptionsFrameTest.class);

}
