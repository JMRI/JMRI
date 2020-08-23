package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.Assume;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrackEditCommentsFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location l = new Location("Location Test Attridutes id", "Location Test Name");
        Track tr = new Track("Test id", "Test Name", "Test Type", l);
        TrackEditCommentsFrame t = new TrackEditCommentsFrame(tr);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }
    
    @Test
    public void testFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);
        
        Track track = loc.getTrackByName("NI Yard", null);
        
        // confirm comment data hasn't changed see initOperationsData
        Assert.assertEquals("Test comment for NI Yard drops and pulls", track.getCommentBoth());
        Assert.assertEquals("Test comment for NI Yard pulls only", track.getCommentPickup());
        Assert.assertEquals("Test comment for NI Yard drops only", track.getCommentSetout());
        
        TrackEditCommentsFrame tecf = new TrackEditCommentsFrame(track);
        Assert.assertNotNull(tecf);
        Assert.assertTrue(tecf.isVisible());
        
        // confirm defaults
        Assert.assertTrue(tecf.printManifest.isSelected());
        Assert.assertFalse(tecf.printSwitchList.isSelected());
        
        // confirm comments
        Assert.assertEquals("Test comment for NI Yard drops and pulls", tecf.commentBothTextArea.getText());
        Assert.assertEquals("Test comment for NI Yard pulls only", tecf.commentPickupTextArea.getText());
        Assert.assertEquals("Test comment for NI Yard drops only", tecf.commentSetoutTextArea.getText());
        
        // test checkboxes
        JemmyUtil.enterClickAndLeave(tecf.printManifest);        
        JemmyUtil.enterClickAndLeave(tecf.saveButton);      
        Assert.assertFalse(track.isPrintManifestCommentEnabled());
        Assert.assertFalse(track.isPrintSwitchListCommentEnabled());
        
        JemmyUtil.enterClickAndLeave(tecf.printSwitchList);        
        JemmyUtil.enterClickAndLeave(tecf.saveButton);      
        Assert.assertFalse(track.isPrintManifestCommentEnabled());
        Assert.assertTrue(track.isPrintSwitchListCommentEnabled());
        
        // confirm comment data hasn't changed
        Assert.assertEquals("Test comment for NI Yard drops and pulls", track.getCommentBoth());
        Assert.assertEquals("Test comment for NI Yard pulls only", track.getCommentPickup());
        Assert.assertEquals("Test comment for NI Yard drops only", track.getCommentSetout());
        
        // change data
        tecf.commentBothTextArea.setText("Test Both");
        tecf.commentPickupTextArea.setText("Test Pull");
        tecf.commentSetoutTextArea.setText("Test Spot");
        JemmyUtil.enterClickAndLeave(tecf.saveButton);
        
        Assert.assertEquals("Test Both", track.getCommentBoth());
        Assert.assertEquals("Test Pull", track.getCommentPickup());
        Assert.assertEquals("Test Spot", track.getCommentSetout());
        
        JUnitUtil.dispose(tecf);

    }


    // private final static Logger log = LoggerFactory.getLogger(TrackEditCommentsFrameTest.class);
}
