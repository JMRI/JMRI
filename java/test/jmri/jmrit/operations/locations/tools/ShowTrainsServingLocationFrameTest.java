package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ShowTrainsServingLocationFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        ShowTrainsServingLocationFrame t = new ShowTrainsServingLocationFrame();
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData();
        Location ni = InstanceManager.getDefault(LocationManager.class).getLocationByName("North Industries");
        ShowTrainsServingLocationFrame stslf = new ShowTrainsServingLocationFrame();
        Track track = ni.getTrackList().get(0);
        stslf.initComponents(ni, track);
        Assert.assertNotNull("exists", stslf);
        JUnitUtil.dispose(stslf);
    }

    // private final static Logger log = LoggerFactory.getLogger(ShowTrainsServingLocationFrameTest.class);

}
