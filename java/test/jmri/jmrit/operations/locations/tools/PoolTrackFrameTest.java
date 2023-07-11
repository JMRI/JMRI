package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.util.JUnitOperationsUtil;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class PoolTrackFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        YardEditFrame tf = new YardEditFrame();
        PoolTrackFrame t = new PoolTrackFrame(tf);
        Assert.assertNotNull("exists",t);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location loc = JUnitOperationsUtil.createOneNormalLocation("Test Location");
        Track t = loc.addTrack("Test Close", Track.YARD);
        YardEditFrame tf = new YardEditFrame();
        tf.initComponents(loc, t);
        PoolTrackFrame f = new PoolTrackFrame(tf);
        f.initComponents();
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // private final static Logger log = LoggerFactory.getLogger(PoolTrackFrameTest.class);

}
