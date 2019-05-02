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
public class TrackDestinationEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);
        
        Track track = loc.getTrackByName("NI Yard", null);
        
        TrackDestinationEditFrame t = new TrackDestinationEditFrame();
        t.initComponents(track);
        
        Assert.assertNotNull("exists",t);
        
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);
        
        Track track = loc.addTrack("NI Interchange", Track.INTERCHANGE);
        
        TrackDestinationEditFrame tdef = new TrackDestinationEditFrame();
        tdef.initComponents(track);       
        Assert.assertNotNull("exists",tdef);
        
        JemmyUtil.enterClickAndLeave(tdef.checkDestinationsButton);       
        // the save should have opened a dialog window
        JemmyUtil.pressDialogButton(tdef, Bundle.getMessage("WarningCarMayNotMove"), "Cancel");
        
        // Confirm default
        Assert.assertFalse("Only cars with destinations", track.isOnlyCarsWithFinalDestinationEnabled());
        
        JemmyUtil.enterClickAndLeave(tdef.onlyCarsWithFD);
        JemmyUtil.enterClickAndLeave(tdef.saveTrackButton); 
        
        Assert.assertTrue("Only cars with destinations", track.isOnlyCarsWithFinalDestinationEnabled());
        
        JUnitUtil.dispose(tdef);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackDestinationEditFrameTest.class);

}
