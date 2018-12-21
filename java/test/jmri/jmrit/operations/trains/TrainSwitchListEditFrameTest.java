package jmri.jmrit.operations.trains;

import java.awt.GraphicsEnvironment;
import java.util.List;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrainSwitchListEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrainSwitchListEditFrame t = new TrainSwitchListEditFrame();
        Assert.assertNotNull("exists",t);
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
        JemmyUtil.enterClickAndLeave(f.saveButton);

        Assert.assertTrue("All Trains", Setup.isSwitchListAllTrainsEnabled());
        Assert.assertTrue("Page per Train", Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL));
        Assert.assertFalse("Real Time", Setup.isSwitchListRealTime());

        JemmyUtil.enterClickAndLeave(f.switchListAllTrainsCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        Assert.assertFalse("All Trains", Setup.isSwitchListAllTrainsEnabled());
        Assert.assertTrue("Page per Train", Setup.getSwitchListPageFormat().equals(Setup.PAGE_NORMAL));
        Assert.assertFalse("Real Time", Setup.isSwitchListRealTime());

        // TODO add test for combo box
        //      JemmyUtil.enterClickAndLeave(f.switchListPageComboBox);
        //      JemmyUtil.enterClickAndLeave(f.saveButton);
        //      Assert.assertFalse("All Trains", Setup.isSwitchListAllTrainsEnabled());
        //      Assert.assertTrue("Page per Train", Setup.isSwitchListPagePerTrainEnabled());
        //      Assert.assertFalse("Real Time", Setup.isSwitchListRealTime());
        ThreadingUtil.runOnGUI(() -> {
            JUnitUtil.dispose(f);
        });
    }

    // private final static Logger log = LoggerFactory.getLogger(TrainSwitchListEditFrameTest.class);

}
