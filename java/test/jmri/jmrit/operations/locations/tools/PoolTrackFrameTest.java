package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.locations.gui.YardEditFrame;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PoolTrackFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        Track t = loc.addTrack("Test Yard", Track.YARD);
        PoolTrackFrame f = new PoolTrackFrame(t);
        Assert.assertNotNull("exists", f);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        Track t = loc.addTrack("Test Close", Track.YARD);
        YardEditFrame tf = new YardEditFrame();
        tf.initComponents(loc, t);
        PoolTrackFrame f = new PoolTrackFrame(t);
        f.initComponents();
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // private final static Logger log = LoggerFactory.getLogger(PoolTrackFrameTest.class);

}
