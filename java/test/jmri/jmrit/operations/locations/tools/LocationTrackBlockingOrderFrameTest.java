package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

/**
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class LocationTrackBlockingOrderFrameTest extends OperationsTestCase {

    @Test
    public void testCTor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationTrackBlockingOrderFrame t = new LocationTrackBlockingOrderFrame();
        t.initComponents(null);
        Assert.assertNotNull("exists", t);
        JUnitUtil.dispose(t);
    }

    @Test
    public void testFrame() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationTrackBlockingOrderFrame ltbo = new LocationTrackBlockingOrderFrame();
        Assert.assertNotNull("exists", ltbo);
        Location westford = JUnitOperationsUtil.createOneNormalLocation("Westford");
        ltbo.initComponents(westford);
        JmriJFrame bof = JmriJFrame.getFrame(Bundle.getMessage("TitleTrackBlockingOrder"));
        Assert.assertNotNull("exists", bof);
        
        // confirm default track block order
        for (Track t : westford.getTrackList()) {
            Assert.assertEquals("Track blocking order", 0 , t.getBlockingOrder());
        }
        
        //test set order button
        JemmyUtil.enterClickAndLeave(ltbo.reorderButton);  
        int i = 1; // block order starts at 1
        for (Track t : westford.getTracksByBlockingOrderList(null)) {
            Assert.assertEquals("Track blocking order", i++ , t.getBlockingOrder());
        }
        
        // test reset button
        JemmyUtil.enterClickAndLeave(ltbo.resetButton);
        for (Track t : westford.getTrackList()) {
            Assert.assertEquals("Track blocking order", 0 , t.getBlockingOrder());
        }
        
        JemmyUtil.enterClickAndLeave(ltbo.saveButton);
        
        JUnitUtil.dispose(ltbo);
    }

    // private final static Logger log = LoggerFactory.getLogger(LocationTrackBlockingOrderFrameTest.class);

}
