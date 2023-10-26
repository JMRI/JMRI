package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;
import org.netbeans.jemmy.operators.JFrameOperator;
import org.netbeans.jemmy.operators.JTableOperator;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.util.*;
import jmri.util.swing.JemmyUtil;

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
        for (Track t : westford.getTracksList()) {
            Assert.assertEquals("Track blocking order", 0 , t.getBlockingOrder());
        }
        
        //test set order button
        JemmyUtil.enterClickAndLeave(ltbo.reorderButton);  
        int i = 1; // block order starts at 1
        for (Track t : westford.getTracksByBlockingOrderList(null)) {
            Assert.assertEquals("Track blocking order", i++ , t.getBlockingOrder());
        }
        
        // test move up button
        Track t1 = westford.getTracksByBlockingOrderList(null).get(1);
        Assert.assertEquals("Track blocking order", 2 , t1.getBlockingOrder());
        JFrameOperator jfo = new JFrameOperator(ltbo);
        JTableOperator jto = new JTableOperator(jfo);
        jto.clickOnCell(1, jto.findColumn(Bundle.getMessage("Up")));
        Assert.assertEquals("Track blocking order", 1 , t1.getBlockingOrder());
        
        // test move down button
        jto.clickOnCell(0, jto.findColumn(Bundle.getMessage("Down")));
        Assert.assertEquals("Track blocking order", 2 , t1.getBlockingOrder());
        
        // test move down last row (there are 6 rows)
        t1 = westford.getTracksByBlockingOrderList(null).get(5);
        jto.clickOnCell(5, jto.findColumn(Bundle.getMessage("Down")));
        Assert.assertEquals("Track blocking order", 1 , t1.getBlockingOrder());
        
        // test move up 1st row
        jto.clickOnCell(0, jto.findColumn(Bundle.getMessage("Up")));
        Assert.assertEquals("Track blocking order", 6 , t1.getBlockingOrder());
        
        // test reset button
        JemmyUtil.enterClickAndLeave(ltbo.resetButton);
        for (Track t : westford.getTracksList()) {
            Assert.assertEquals("Track blocking order", 0 , t.getBlockingOrder());
        }
        
        JemmyUtil.enterClickAndLeave(ltbo.saveButton);
        
        JUnitUtil.dispose(ltbo);
    }
    
    @Test
    public void testCloseWindowOnSave() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LocationTrackBlockingOrderFrame f = new LocationTrackBlockingOrderFrame();
        Location westford = JUnitOperationsUtil.createOneNormalLocation("Westford");
        f.initComponents(westford);
        JUnitOperationsUtil.testCloseWindowOnSave(f.getTitle());
    }

    // private final static Logger log = LoggerFactory.getLogger(LocationTrackBlockingOrderFrameTest.class);

}
