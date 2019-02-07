package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.TrackEditFrame;
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
public class ChangeTrackFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);

        Track track = loc.getTrackByName("NI Yard", null);

        TrackEditFrame tf = new TrackEditFrame();
        tf.initComponents(loc, track);
        Assert.assertNotNull("exists", tf);

        ChangeTrackFrame t = new ChangeTrackFrame(tf);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
        JUnitUtil.dispose(tf);
    }

    @Test
    public void testFrameButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);

        Track track = loc.getTrackByName("NI Yard", null);

        TrackEditFrame tef = new TrackEditFrame();
        tef.initComponents(loc, track);
        Assert.assertNotNull("exists", tef);
        
        ChangeTrackFrame ctf = new ChangeTrackFrame(tef);
        Assert.assertNotNull("exists", ctf);
        
        // confirm default
        Assert.assertTrue(track.isYard());
        
        JemmyUtil.enterClickAndLeave(ctf.spurRadioButton);
        JemmyUtil.enterClickAndLeave(ctf.saveButton);
        
        // confirm change
        Assert.assertTrue(track.isSpur());
        
        JUnitUtil.dispose(ctf);
        JUnitUtil.dispose(tef);
    }

    // private final static Logger log = LoggerFactory.getLogger(ChangeTrackFrameTest.class);
}
