package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class TrackRoadEditFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        TrackRoadEditFrame t = new TrackRoadEditFrame();
        Assert.assertNotNull("exists", t);

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
        
        JUnitOperationsUtil.initOperationsData();
        TrackRoadEditFrame tlef = new TrackRoadEditFrame();
        Assert.assertNotNull("exists", tlef);

        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);

        Track track = loc.getTrackByName("NI Yard", null);

        tlef.initComponents(loc, track);
        Assert.assertTrue(tlef.isVisible());

        JemmyUtil.enterClickAndLeave(tlef.roadNameInclude);
        JemmyUtil.enterClickAndLeaveThreadSafe(tlef.saveButton);
        // error dialog window show appear
        JemmyUtil.pressDialogButton(tlef, Bundle.getMessage("ErrorNoRoads"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(tlef);
        
        // only road "AA" is to be accepted
        JemmyUtil.enterClickAndLeave(tlef.addRoadButton);
        JemmyUtil.enterClickAndLeave(tlef.saveButton);

        Assert.assertTrue(track.isRoadNameAccepted("AA"));

        for (String roadName : InstanceManager.getDefault(CarRoads.class).getNames()) {
            if (roadName.equals("AA")) {
                continue; // the only road name accepted by this track
            }
            Assert.assertFalse("confirm road name not accepted", track.isRoadNameAccepted(roadName));
        }

        JUnitUtil.dispose(tlef);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        Track track = loc.addTrack("Yard", Track.YARD);
        TrackRoadEditFrame f = new TrackRoadEditFrame();
        f.initComponents(loc, track);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackRoadEditFrameTest.class);

}
