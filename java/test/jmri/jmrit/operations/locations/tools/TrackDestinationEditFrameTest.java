package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.locations.gui.YardEditFrame;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

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
        
        YardEditFrame yef = new YardEditFrame();
        yef.initComponents(track);
        
        TrackDestinationEditFrame t = new TrackDestinationEditFrame();
        t.initComponents(yef);
        
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
        
        YardEditFrame yef = new YardEditFrame();
        yef.initComponents(track);
        
        TrackDestinationEditFrame tdef = new TrackDestinationEditFrame();
        tdef.initComponents(yef);
        JemmyUtil.waitFor(tdef);
        
        JemmyUtil.enterClickAndLeaveThreadSafe(tdef.checkDestinationsButton);       
        // the save should have opened a dialog window
        JemmyUtil.pressDialogButton(tdef, Bundle.getMessage("WarningCarMayNotMove"), "Cancel");
        JemmyUtil.waitFor(tdef);
        
        // Confirm default
        Assert.assertFalse("Only cars with destinations", track.isOnlyCarsWithFinalDestinationEnabled());
        
        JemmyUtil.enterClickAndLeave(tdef.onlyCarsWithFD);
        JemmyUtil.enterClickAndLeave(tdef.saveButton); 
        
        Assert.assertTrue("Only cars with destinations", track.isOnlyCarsWithFinalDestinationEnabled());
        
        JUnitUtil.dispose(tdef);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        Track track = loc.addTrack("Interchange", Track.INTERCHANGE);
        YardEditFrame yef = new YardEditFrame();
        yef.initComponents(track);
        TrackDestinationEditFrame f = new TrackDestinationEditFrame();
        f.initComponents(yef);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackDestinationEditFrameTest.class);

}
