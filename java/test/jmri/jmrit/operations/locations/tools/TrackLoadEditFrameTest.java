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
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class TrackLoadEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrackLoadEditFrame t = new TrackLoadEditFrame();
        Assert.assertNotNull("exists",t);
        
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);
        
        t.initComponents(loc, null);
        Assert.assertTrue(t.isVisible());
        
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testFrameButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrackLoadEditFrame tlef = new TrackLoadEditFrame();
        Assert.assertNotNull("exists",tlef);
        
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);
        
        Track track = loc.getTrackByName("NI Yard", null);
        
        tlef.initComponents(loc, track);
        Assert.assertTrue(tlef.isVisible());
        
        JemmyUtil.enterClickAndLeave(tlef.loadNameInclude);
        JemmyUtil.enterClickAndLeave(tlef.loadAndTypeCheckBox);
        JemmyUtil.enterClickAndLeave(tlef.saveTrackButton);
        
        // error dialog window show appear
        JemmyUtil.pressDialogButton(tlef, Bundle.getMessage("ErrorNoLoads"), "OK");
        
        // now add a load "Flat & E"
        JemmyUtil.enterClickAndLeave(tlef.addLoadButton);
        JemmyUtil.enterClickAndLeave(tlef.saveTrackButton);
        
        Assert.assertTrue(track.acceptsLoad("E", "Flat"));
        Assert.assertFalse(track.acceptsLoad("L", "Flat"));
        
        Assert.assertFalse(track.acceptsLoadName("L"));
        Assert.assertFalse(track.acceptsLoadName("E"));
        
        JUnitUtil.dispose(tlef);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        super.setUp();
        
        JUnitOperationsUtil.initOperationsData();
    }

    @Override
    @After
    public void tearDown() {
        super.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackLoadEditFrameTest.class);

}
