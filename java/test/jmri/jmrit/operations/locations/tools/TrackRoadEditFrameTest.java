package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;
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
        JemmyUtil.enterClickAndLeave(tlef.saveTrackButton);

        // error dialog window show appear
        JemmyUtil.pressDialogButton(tlef, Bundle.getMessage("ErrorNoRoads"), Bundle.getMessage("ButtonOK"));

        // only road "AA" is to be accepted
        JemmyUtil.enterClickAndLeave(tlef.addRoadButton);
        JemmyUtil.enterClickAndLeave(tlef.saveTrackButton);

        Assert.assertTrue(track.acceptsRoadName("AA"));

        for (String roadName : InstanceManager.getDefault(CarRoads.class).getNames()) {
            if (roadName.equals("AA")) {
                continue; // the only road name accepted by this track
            }
            Assert.assertFalse("confirm road name not accepted", track.acceptsRoadName(roadName));
        }

        JUnitUtil.dispose(tlef);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackRoadEditFrameTest.class);

}
