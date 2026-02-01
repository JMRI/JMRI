package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * @author Paul Bender Copyright (C) 2017
 */
public class LocationsByQuickServiceFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationsByQuickServiceFrame t = new LocationsByQuickServiceFrame();
        Assert.assertNotNull("exists", t);
        t.initComponents();
        Assert.assertTrue("frame visible", t.isVisible());

        JUnitUtil.dispose(t);
    }

    @Test
    public void testFrameButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);
        
        Track t = loc.getTrackByName("NI Yard", null);
        Assert.assertFalse("default is false", t.isQuickServiceEnabled());

        LocationsByQuickServiceFrame f = new LocationsByQuickServiceFrame();
        f.initComponents(loc);
        Assert.assertNotNull("exists", f);
        
        JemmyUtil.enterClickAndLeave(f.setButton);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertTrue("new state", t.isQuickServiceEnabled());
        

        JemmyUtil.enterClickAndLeave(f.clearButton);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertFalse("new state", t.isQuickServiceEnabled());

        JUnitUtil.dispose(f);

    }

    @Test
    public void testFrameCopyCheckBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);

        LocationsByCarTypeFrame lctf = new LocationsByCarTypeFrame();
        Assert.assertNotNull("exists", lctf);
        lctf.initComponents(loc);
        Assert.assertTrue("frame visible", lctf.isVisible());

        Assert.assertTrue("accepts", loc.acceptsTypeName("Boxcar"));
        Assert.assertTrue("accepts", loc.acceptsTypeName("Flat"));

        // Boxcar is the 1st car type in the selection box
        JemmyUtil.enterClickAndLeave(lctf.clearButton);
        JemmyUtil.enterClickAndLeave(lctf.saveButton);
        Assert.assertFalse("accepts", loc.acceptsTypeName("Boxcar"));
        Assert.assertTrue("accepts", loc.acceptsTypeName("Flat"));

        JemmyUtil.enterClickAndLeave(lctf.setButton);
        JemmyUtil.enterClickAndLeave(lctf.saveButton);
        Assert.assertTrue("accepts 2", loc.acceptsTypeName("Boxcar"));
        Assert.assertTrue("accepts 2", loc.acceptsTypeName("Flat"));

        lctf.typeComboBox.setSelectedItem("Flat");
        JemmyUtil.enterClickAndLeave(lctf.clearButton);
        JemmyUtil.enterClickAndLeave(lctf.saveButton);
        lctf.copyComboBox.setSelectedItem("Flat");
        JemmyUtil.enterClickAndLeave(lctf.copyCheckBox);
        lctf.typeComboBox.setSelectedItem("Boxcar");

        JemmyUtil.enterClickAndLeaveThreadSafe(lctf.saveButton);
        // the save should have opened a dialog window
        JemmyUtil.pressDialogButton(lctf, Bundle.getMessage("CopyCarTypeTitle"), Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(lctf);

        Assert.assertFalse("accepts 3", loc.acceptsTypeName("Boxcar"));
        Assert.assertFalse("accepts 3", loc.acceptsTypeName("Flat"));

        JUnitUtil.dispose(lctf);

    }

    // private final static Logger log = LoggerFactory.getLogger(LocationsByCarTypeFrameTest.class);

}
