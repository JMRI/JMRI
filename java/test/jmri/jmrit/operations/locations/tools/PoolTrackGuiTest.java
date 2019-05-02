package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Pool;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

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

        Track t = new Track("ID1", "TestTrack1", "Siding", l);
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

        Track t = new Track("ID1", "TestTrack1", "Siding", l);
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

        Track t = new Track("ID1", "TestTrack1", "Siding", l);
        Assert.assertEquals("Initial Track Pool", null, t.getPool());

        PoolTrackFrame f = new PoolTrackFrame(t);
        f.initComponents();

        f.comboBoxPools.setSelectedItem(desiredPool);
        Assert.assertEquals("ComboBox selection", desiredPool, f.comboBoxPools.getSelectedItem());

        // Now click the Save button and the Track should be updated with the selected Pool
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertEquals("Updated Track Pool", desiredPool, t.getPool());

        // close window
        JUnitUtil.dispose(f);
    }

    @Test
    public void testSetMinLengthAndSaveTrack() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        // Enter a new minimum length, click save and check that the Track is updated.
        Location l = new Location("LOC1", "Location One");

        Track t = new Track("ID1", "TestTrack1", "Siding", l);
        Assert.assertEquals("Minimum track length", 0, t.getMinimumLength());

        PoolTrackFrame f = new PoolTrackFrame(t);
        f.initComponents();

        f.trackMinLengthTextField.setText("23");

        // Now click the Save button and the Track should be updated with the selected Pool
        JemmyUtil.enterClickAndLeave(f.saveButton);
 
        Assert.assertEquals("Updated min track length", 23, t.getMinimumLength());

        // close window
        JUnitUtil.dispose(f);
    }
}
