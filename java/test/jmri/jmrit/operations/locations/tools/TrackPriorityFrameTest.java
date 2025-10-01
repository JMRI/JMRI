package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;

/**
 * @author Daniel Boudreau Copyright (C) 2025
 */
public class TrackPriorityFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        TrackPriorityFrame t = new TrackPriorityFrame(null);
        Assert.assertNotNull("exists", t);
    }

    @Test
    public void testFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        LocationManager lmanager = InstanceManager.getDefault(LocationManager.class);
        Location location = lmanager.getLocationByName("North Industries");
        Track track = location.getTrackByName("NI Yard", null);

        TrackPriorityFrame tpf = new TrackPriorityFrame(track);
        Assert.assertNotNull("exists", tpf);
        
        Assert.assertEquals("default", Track.PRIORITY_NORMAL, track.getTrackPriority());
        JemmyUtil.enterClickAndLeave(tpf.priorityHigh);
        JemmyUtil.enterClickAndLeave(tpf.saveButton);
        Assert.assertEquals("new priority", Track.PRIORITY_HIGH, track.getTrackPriority());
        
        JemmyUtil.enterClickAndLeave(tpf.priorityMedium);
        JemmyUtil.enterClickAndLeave(tpf.saveButton);
        Assert.assertEquals("new priority", Track.PRIORITY_MEDIUM, track.getTrackPriority());
        
        JemmyUtil.enterClickAndLeave(tpf.priorityLow);
        JemmyUtil.enterClickAndLeave(tpf.saveButton);
        Assert.assertEquals("new priority", Track.PRIORITY_LOW, track.getTrackPriority());
        
        JemmyUtil.enterClickAndLeave(tpf.priorityNormal);
        JemmyUtil.enterClickAndLeave(tpf.saveButton);
        Assert.assertEquals("new priority", Track.PRIORITY_NORMAL, track.getTrackPriority());
        
        JUnitUtil.dispose(tpf);
    }

    // private final static Logger log = LoggerFactory.getLogger(TrackLoadEditFrameTest.class);

}
