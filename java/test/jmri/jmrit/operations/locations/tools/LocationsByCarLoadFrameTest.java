package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocationsByCarLoadFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        
        LocationsByCarLoadFrame t = new LocationsByCarLoadFrame();
        Assert.assertNotNull("exists", t);  
        t.initComponents();
        Assert.assertTrue("frame visible", t.isVisible());
        
        JUnitUtil.dispose(t);
    }

    @Test
    public void testFrameLocation() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);

        LocationsByCarLoadFrame lclf = new LocationsByCarLoadFrame();
        Assert.assertNotNull("exists", lclf);
        
        lclf.initComponents(loc);
        Assert.assertTrue("frame visible", lclf.isVisible());
        
        JUnitUtil.dispose(lclf);
    }
    
    @Test
    public void testFrameSaveButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();

        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);
        Track track = loc.getTrackByName("NI Yard", null);
        Assert.assertNotNull("exists", track);
        
        // confirm default load
        Assert.assertTrue(track.acceptsLoad("E", "Flat"));

        LocationsByCarLoadFrame lclf = new LocationsByCarLoadFrame();
        Assert.assertNotNull("exists", lclf);
        
        lclf.initComponents(loc);
        Assert.assertTrue("frame visible", lclf.isVisible());
        
        JemmyUtil.enterClickAndLeave(lclf.clearButton);
        JemmyUtil.enterClickAndLeave(lclf.saveButton);
        
        // confirm change
        Assert.assertFalse(track.acceptsLoad("E", "Flat"));
        
        JUnitUtil.dispose(lclf);
    }

    // private final static Logger log = LoggerFactory.getLogger(LocationsByCarLoadFrameTest.class);

}
