package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.jupiter.api.Test;

import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.*;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitUtil;
import jmri.util.JmriJFrame;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations PoolTrackFrame class
 *
 * @author Gregory Madsen Copyright (C) 2012
 */
public class PoolTrackGuiTest extends OperationsTestCase {

    /*
     * Things to test with this frame:
     *
     * - Adding a new Pool name to the available pools list
     *
     * - What happens when a null track is passed to the frame
     *
     * - Selecting an existing pool and saving it to the track
     *
     * - Selecting a minimum length and saving it to the track
     *
     * - Not sure if we want to test the status display panel, as it doesn't do
     * anything.
     */
    @Test
    public void testPoolFrameCreate() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location l = new Location("LOC1", "Location One");

        Track t = new Track("ID1", "TestTrack1", "Spur", l);
        t.setLength(100);

        PoolTrackFrame f = new PoolTrackFrame(t);
        f.initComponents();

        Assert.assertTrue(f.isVisible());

        // close windows
        JUnitUtil.dispose(f);
    }

    @Test
    public void testOpenWithNullTrack() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // See what happens when a null track is passed in.
        try {
            PoolTrackFrame f = new PoolTrackFrame((Track) null);
            f.initComponents();

            // null track prevents frame from being displayed
            Assert.assertFalse(f.isVisible());

            // close windows
            JUnitUtil.dispose(f);
        } catch (Exception e) {
            Assert.fail("Exception thrown");
        }
    }

    @Test
    public void testAddNewPoolName() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // Enter a new Pool name and click Add, check that the Pool list count
        // is 1
        Location l = new Location("LOC1", "Location One");

        Track t = new Track("ID1", "TestTrack1", "Spur", l);
        t.setLength(100);

        PoolTrackFrame f = new PoolTrackFrame(t);
        f.initComponents();

        // Set the new Pool name
        f.trackPoolNameTextField.setText("Test Pool 1");

        // CLicking the Add button should add the new pool to the
        // collection.
        JemmyUtil.enterClickAndLeave(f.addButton);

        // Here the track's location should have a pool collection with one
        // item.
        Assert.assertEquals("Pool size", 1, t.getLocation().getPoolsByNameList().size());

        // Try to add the same one again, and the count should remain at 1
        JemmyUtil.enterClickAndLeave(f.addButton);

        Assert.assertEquals("Pool size", 1, l.getPoolsByNameList().size());

        // Add a different name and it should go to 2
        f.trackPoolNameTextField.setText("Test Pool 2");
        JemmyUtil.enterClickAndLeave(f.addButton);

        Assert.assertEquals("Pool size", 2, l.getPoolsByNameList().size());

        // close window
        JUnitUtil.dispose(f);
    }

    @Test
    public void testSelectPoolAndSaveTrack() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // This should change the pool track property of the Track under test.
        Location l = new Location("LOC1", "Location One");
        l.addPool("Pool 1");
        Pool desiredPool = l.addPool("Pool 2");
        l.addPool("Pool 3");

        Assert.assertEquals("Pool count", 3, l.getPoolsByNameList().size());

        Track t = new Track("ID1", "TestTrack1", "Spur", l);
        Assert.assertEquals("Initial Track Pool", null, t.getPool());

        PoolTrackFrame f = new PoolTrackFrame(t);
        f.initComponents();

        f.comboBoxPools.setSelectedItem(desiredPool);
        Assert.assertEquals("ComboBox selection", desiredPool, f.comboBoxPools.getSelectedItem());
        
        // improve test coverage by closing window on save
        Setup.setCloseWindowOnSaveEnabled(true);

        // Now click the Save button and the Track should be updated with the selected Pool
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertEquals("Updated Track Pool", desiredPool, t.getPool());

        // confirm window closed
        JmriJFrame pf = JmriJFrame.getFrame(Bundle.getMessage("MenuItemPoolTrack"));
        Assert.assertNull("frame gone", pf);
    }

    @Test
    public void testSetMinLengthAndSaveTrack() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // Enter a new minimum length, click save and check that the Track is updated.
        Location l = new Location("LOC1", "Location One");

        Track t = new Track("ID1", "TestTrack1", "Spur", l);
        Assert.assertEquals("Minimum track length", 0, t.getPoolMinimumLength());

        PoolTrackFrame f = new PoolTrackFrame(t);
        f.initComponents();

        f.trackMinLengthTextField.setText("23");

        // Now click the Save button and the Track should be updated with the selected Pool
        JemmyUtil.enterClickAndLeave(f.saveButton);
 
        Assert.assertEquals("Updated min track length", 23, t.getPoolMinimumLength());

        // close window
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testBadSetMinLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // Enter a bad minimum length, click save and check that the Track is updated.
        Location l = new Location("LOC1", "Location One");

        Track t = new Track("ID1", "TestTrack1", "Spur", l);
        Assert.assertEquals("Minimum track length", 0, t.getPoolMinimumLength());

        PoolTrackFrame f = new PoolTrackFrame(t);
        f.initComponents();

        f.trackMinLengthTextField.setText("X"); // should be a number

        // Now click the Save button error dialog should appear
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);

        // error dialog should have appeared
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("ErrorTrackLength"),
                Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);

        // close window
        JUnitUtil.dispose(f);
    }
    
    @Test
    public void testStagingTrack() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        Location l = new Location("LOC1", "Location One");

        Track t1 = new Track("ID1", "TestStagingTrack1", Track.STAGING, l);
        // add pool to 2 staging tracks
        Pool pool = l.addPool("Pool One");
        t1.setPool(pool);
        
        Track t2 = new Track("ID2", "TestStagingTrack2", Track.STAGING, l);
        t2.setPool(pool);
        // improve test coverage by setting a minimum
        t2.setPoolMinimumLength(100);
        
        PoolTrackFrame f = new PoolTrackFrame(t1);
        f.initComponents();

        Assert.assertEquals("Staging default mode", Track.NORMAL, t1.getServiceOrder());
        
        JemmyUtil.enterClickAndLeave(f.orderFIFO);
        Assert.assertEquals("Staging mode", Track.FIFO, t1.getServiceOrder());
        Assert.assertEquals("Staging mode", Track.FIFO, t2.getServiceOrder());
        
        JemmyUtil.enterClickAndLeave(f.orderLIFO);
        Assert.assertEquals("Staging mode", Track.LIFO, t1.getServiceOrder());
        Assert.assertEquals("Staging mode", Track.LIFO, t2.getServiceOrder());
        
        JemmyUtil.enterClickAndLeave(f.orderNormal);
        Assert.assertEquals("Staging mode", Track.NORMAL, t1.getServiceOrder());
        Assert.assertEquals("Staging mode", Track.NORMAL, t2.getServiceOrder());
        
        // saving will update service order from the other track in pool
        t2.setServiceOrder(Track.LIFO);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertEquals("Staging mode", Track.LIFO, t1.getServiceOrder());
        
        f.comboBoxPools.setSelectedItem(null);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        JemmyUtil.enterClickAndLeave(f.orderFIFO);
        // without pool service order doesn't change
        Assert.assertEquals("Staging mode", Track.NORMAL, t1.getServiceOrder());
        Assert.assertEquals("Staging mode", Track.LIFO, t2.getServiceOrder());

        // close window
        JUnitUtil.dispose(f);
    }

}
