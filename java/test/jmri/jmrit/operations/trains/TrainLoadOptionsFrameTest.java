package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.rollingstock.cars.CarLoads;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainLoadOptionsFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainLoadOptionsFrame t = new TrainLoadOptionsFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testNoTrain() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainLoadOptionsFrame f = new TrainLoadOptionsFrame();
        Assert.assertNotNull("exists", f);
        TrainEditFrame trainEditFrame = new TrainEditFrame(null);
        f.initComponents(trainEditFrame);
        Assert.assertFalse("Save button disabled", f.saveTrainButton.isEnabled());
        JUnitUtil.dispose(f);
    }

    @Test
    public void testSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainLoadOptionsFrame f = new TrainLoadOptionsFrame();
        Assert.assertNotNull("exists", f);
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.newTrain("Test");
        TrainEditFrame trainEditFrame = new TrainEditFrame(train);
        f.initComponents(trainEditFrame);
        Assert.assertTrue("Save button enabled", f.saveTrainButton.isEnabled());

        // only accept "E" load
        JemmyUtil.enterClickAndLeave(f.loadNameInclude);
        JemmyUtil.enterClickAndLeave(f.addLoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts E load", train.isLoadNameAccepted("E"));
        Assert.assertFalse("Train does not accepts L load", train.isLoadNameAccepted("L"));

        // add "L" load
        JemmyUtil.enterClickAndLeave(f.addLoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts E load", train.isLoadNameAccepted("E"));
        Assert.assertTrue("Train accepts L load", train.isLoadNameAccepted("L"));

        // delete "L" load
        JemmyUtil.enterClickAndLeave(f.deleteLoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts E load", train.isLoadNameAccepted("E"));
        Assert.assertFalse("Train does not accepts L load", train.isLoadNameAccepted("L"));

        // delete all
        JemmyUtil.enterClickAndLeave(f.deleteAllLoadsButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertFalse("Train does not accepts E load", train.isLoadNameAccepted("E"));
        Assert.assertFalse("Train does not accepts L load", train.isLoadNameAccepted("L"));

        // accept all loads
        JemmyUtil.enterClickAndLeave(f.loadNameAll);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts E load", train.isLoadNameAccepted("E"));
        Assert.assertTrue("Train accepts L load", train.isLoadNameAccepted("L"));

        // exclude "L" load
        JemmyUtil.enterClickAndLeave(f.loadNameExclude);
        JemmyUtil.enterClickAndLeave(f.addLoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts E load", train.isLoadNameAccepted("E"));
        Assert.assertFalse("Train does not accepts L load", train.isLoadNameAccepted("L"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testTypeAndLoadName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainLoadOptionsFrame f = new TrainLoadOptionsFrame();
        Assert.assertNotNull("exists", f);
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.newTrain("Test");
        TrainEditFrame trainEditFrame = new TrainEditFrame(train);
        f.initComponents(trainEditFrame);
        Assert.assertTrue("Save button enabled", f.saveTrainButton.isEnabled());

        // test property change
        Assert.assertEquals("Confirm baggage is 1st", "Baggage", f.comboBoxTypes.getItemAt(0));
        train.deleteTypeName("Baggage");
        Assert.assertEquals("Confirm baggage was removed", "Boxcar", f.comboBoxTypes.getItemAt(0));

        JemmyUtil.enterClickAndLeave(f.loadAndTypeCheckBox);

        // only accept "E" load
        JemmyUtil.enterClickAndLeave(f.loadNameInclude);
        JemmyUtil.enterClickAndLeave(f.addLoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts E load", train.isLoadNameAccepted("Boxcar & E"));
        Assert.assertFalse("Train does not accepts L load", train.isLoadNameAccepted("L"));

        // add "L" load
        JemmyUtil.enterClickAndLeave(f.addLoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts E load", train.isLoadNameAccepted("Boxcar & E"));
        Assert.assertTrue("Train accepts L load", train.isLoadNameAccepted("Boxcar & L"));

        // delete "L" load
        JemmyUtil.enterClickAndLeave(f.deleteLoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts E load", train.isLoadNameAccepted("Boxcar & E"));
        Assert.assertFalse("Train does not accepts L load", train.isLoadNameAccepted("Boxcar & L"));

        // delete all
        JemmyUtil.enterClickAndLeave(f.deleteAllLoadsButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertFalse("Train does not accepts E load", train.isLoadNameAccepted("Boxcar & E"));
        Assert.assertFalse("Train does not accepts L load", train.isLoadNameAccepted("L"));

        // accept all loads
        JemmyUtil.enterClickAndLeave(f.loadNameAll);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts E load", train.isLoadNameAccepted("Boxcar & E"));
        Assert.assertTrue("Train accepts L load", train.isLoadNameAccepted("L"));

        // exclude "L" load
        JemmyUtil.enterClickAndLeave(f.loadNameExclude);
        JemmyUtil.enterClickAndLeave(f.addLoadButton);
        JemmyUtil.enterClickAndLeave(f.saveTrainButton);
        Assert.assertTrue("Train accepts E load", train.isLoadNameAccepted("E"));
        Assert.assertFalse("Train does not accepts L load", train.isLoadNameAccepted("Boxcar & L"));

        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testPropertyChange() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainLoadOptionsFrame f = new TrainLoadOptionsFrame();
        Assert.assertNotNull("exists", f);
        TrainManager tmanager = InstanceManager.getDefault(TrainManager.class);
        Train train = tmanager.newTrain("Test");
        TrainEditFrame trainEditFrame = new TrainEditFrame(train);
        f.initComponents(trainEditFrame);
        Assert.assertTrue("Save button enabled", f.saveTrainButton.isEnabled());

        // test car type property change
        Assert.assertEquals("Confirm baggage is 1st", "Baggage", f.comboBoxTypes.getItemAt(0));
        train.deleteTypeName("Baggage");
        Assert.assertEquals("Confirm baggage was removed", "Boxcar", f.comboBoxTypes.getItemAt(0));

        // test load name property change
        JemmyUtil.enterClickAndLeave(f.loadNameInclude);
        CarLoads cl = InstanceManager.getDefault(CarLoads.class);
        cl.addName("Boxcar", "Aload");
        Assert.assertEquals("Confirm new load added", "Aload", f.comboBoxLoads.getItemAt(0));
        
        JUnitUtil.dispose(f);
    }
}
