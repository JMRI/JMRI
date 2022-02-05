package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JFrameOperator;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainSwitchListEditFrame.TrainSwitchListCommentFrame;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrainSwitchListEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainSwitchListEditFrame t = new TrainSwitchListEditFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testTrainSwitchListEditFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // check defaults
        Assert.assertTrue("All Trains", Setup.isSwitchListAllTrainsEnabled());
        Assert.assertTrue("Page per Train", Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL));
        Assert.assertTrue("Real Time", Setup.isSwitchListRealTime());

        TrainSwitchListEditFrame f = new TrainSwitchListEditFrame();
        ThreadingUtil.runOnGUI(() -> {
            f.initComponents();
        });
        
        JUnitOperationsUtil.loadFiveLocations();

        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        List<Location> locations = lmanager.getLocationsByNameList();

        // default switch list will print all locations
        for (int i = 0; i < locations.size(); i++) {
            Location l = locations.get(i);
            Assert.assertTrue("print switchlist 1", l.isSwitchListEnabled());
        }
        // now clear all locations
        JemmyUtil.enterClickAndLeave(f.clearButton);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        for (int i = 0; i < locations.size(); i++) {
            Location l = locations.get(i);
            Assert.assertFalse("print switchlist 2", l.isSwitchListEnabled());
        }
        // now set all locations
        JemmyUtil.enterClickAndLeave(f.setButton);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        for (int i = 0; i < locations.size(); i++) {
            Location l = locations.get(i);
            Assert.assertTrue("print switchlist 3", l.isSwitchListEnabled());
        }

        // test the two check box options
        JemmyUtil.enterClickAndLeave(f.switchListRealTimeCheckBox);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // clear dialog box
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ResetSwitchLists"), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);

        Assert.assertTrue("All Trains", Setup.isSwitchListAllTrainsEnabled());
        Assert.assertTrue("Page per Train", Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL));
        Assert.assertFalse("Real Time", Setup.isSwitchListRealTime());

        JemmyUtil.enterClickAndLeave(f.switchListAllTrainsCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        Assert.assertFalse("All Trains", Setup.isSwitchListAllTrainsEnabled());
        Assert.assertTrue("Page per Train", Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL));
        Assert.assertFalse("Real Time", Setup.isSwitchListRealTime());

        // TODO add test for combo box
        // JemmyUtil.enterClickAndLeave(f.switchListPageComboBox);
        // JemmyUtil.enterClickAndLeave(f.saveButton);
        // Assert.assertFalse("All Trains", Setup.isSwitchListAllTrainsEnabled());
        // Assert.assertTrue("Page per Train", Setup.isSwitchListPagePerTrainEnabled());
        // Assert.assertFalse("Real Time", Setup.isSwitchListRealTime());
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testAddComment() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // check defaults
        Assert.assertTrue("All Trains", Setup.isSwitchListAllTrainsEnabled());
        Assert.assertTrue("Page per Train", Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL));
        Assert.assertTrue("Real Time", Setup.isSwitchListRealTime());
        
        JUnitOperationsUtil.loadFiveLocations();

        TrainSwitchListEditFrame f = new TrainSwitchListEditFrame();
        f.initComponents();
        
        JFrameOperator jfo = new JFrameOperator(f);
        JemmyUtil.pressButton(jfo, Bundle.getMessage("Add"));
        
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location locA = lmanager.getLocationByName("Test Loc A");
        Assert.assertNotNull("Confirm exists", locA);
        
        JmriJFrame cf = JmriJFrame.getFrame(locA.getName());
        Assert.assertNotNull("comment frame", cf);
        
        JFrameOperator jfoC = new JFrameOperator(cf);
        TrainSwitchListCommentFrame tscf = (TrainSwitchListCommentFrame)cf;
        tscf.commentTextArea.setText("Test Comment for Loc A");
        JemmyUtil.pressButton(jfoC, Bundle.getMessage("ButtonSave"));
        
        Assert.assertEquals("Confirm comment", "Test Comment for Loc A", locA.getSwitchListCommentWithColor());
        
        // close comment window
        JemmyUtil.pressButton(jfoC, Bundle.getMessage("ButtonCancel"));
        cf = JmriJFrame.getFrame(locA.getName());
        Assert.assertNull("comment frame", cf);       
        
        JUnitUtil.dispose(f);
    }

}
