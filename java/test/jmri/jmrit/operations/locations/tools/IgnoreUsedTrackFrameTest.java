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
public class IgnoreUsedTrackFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrackEditFrame tf = new TrackEditFrame();
        IgnoreUsedTrackFrame t = new IgnoreUsedTrackFrame(tf);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testButtons() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        
        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location loc = lmanager.getLocationByName("North Industries");
        Assert.assertNotNull("exists", loc);

        Track track = loc.getTrackByName("NI Yard", null);

        TrackEditFrame tf = new TrackEditFrame();
        tf.initComponents(loc, track);
        IgnoreUsedTrackFrame iutf = new IgnoreUsedTrackFrame(tf);
        Assert.assertNotNull("exists",iutf);
        
        // confirm default
        Assert.assertEquals(0, track.getIgnoreUsedLengthPercentage());
        
        JemmyUtil.enterClickAndLeave(iutf.seventyfivePercent);
        JemmyUtil.enterClickAndLeave(iutf.saveButton);
        
        Assert.assertEquals(75, track.getIgnoreUsedLengthPercentage());
        
        JUnitUtil.dispose(iutf);
        JUnitUtil.dispose(tf);
    }

    // private final static Logger log = LoggerFactory.getLogger(IgnoreUsedTrackFrameTest.class);

}
