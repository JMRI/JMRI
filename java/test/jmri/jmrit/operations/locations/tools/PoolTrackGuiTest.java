package jmri.jmrit.operations.locations.tools;

import java.awt.GraphicsEnvironment;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsSwingTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Pool;
import jmri.jmrit.operations.locations.Track;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests for the Operations PoolTrackFrame class
 *
 * @author Gregory Madsen Copyright (C) 2012
 */
public class PoolTrackGuiTest extends OperationsSwingTestCase {

    //final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;
    private void CreateTestLocations() {
        // Clear out any previous locations
        InstanceManager.getDefault(LocationManager.class).dispose();

        // Create 5 locations
        LocationManager lManager = InstanceManager.getDefault(LocationManager.class);

        Location l1 = lManager.newLocation("Test Loc E");
        l1.setLength(1001);

        Location l2 = lManager.newLocation("Test Loc D");
        l2.setLength(1002);

        Location l3 = lManager.newLocation("Test Loc C");
        l3.setLength(1003);

        Location l4 = lManager.newLocation("Test Loc B");
        l4.setLength(1004);

        Location l5 = lManager.newLocation("Test Loc A");
        l5.setLength(1005);
    }

//	private void AddTestSidings() {
//		LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
//
//		Location l1 = lManager.getLocationByName("Test Loc C");
//		Track t;
//		t = l1.addTrack("new siding track", "Siding");
//
//		t = l1.addTrack("2nd siding track", "Siding");
//
//		t = l1.addTrack("3rd siding track", "Siding");
//	}
//
//	private void AddTestInterchanges() {
//		LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
//		Location l1 = lManager.getLocationByName("Test Loc C");
//
//		Track t;
//		t = l1.addTrack("new interchange track", "Interchange");
//		// t.setLength(321);
//
//		t = l1.addTrack("2nd interchange track", "Interchange");
//		// t.setLength(4331);
//
//	}
//
//	private void AddTestYardTracks() {
//		LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
//		Location l1 = lManager.getLocationByName("Test Loc C");
//
//		Track t;
//		t = l1.addTrack("new yard track", "Yard");
//		// t.setLength(43);
//
//		t = l1.addTrack("2nd yard track", "Yard");
//		// t.setLength(6543);
//
//		t = l1.addTrack("3rd yard track", "Yard");
//		// t.setLength(1);
//
//		t = l1.addTrack("4th yard track", "Yard");
//		t.setLength(21);
//
//	}
//
//	private void AddTestStagingTracks() {
//		LocationManager lManager = InstanceManager.getDefault(LocationManager.class);
//		Location l1 = lManager.getLocationByName("Test Loc A");
//
//		Track t;
//		t = l1.addTrack("new staging track", "Staging");
//		// t.setLength(43);
//
//		t = l1.addTrack("2nd staging track", "Staging");
//		// t.setLength(6543);
//
//		t = l1.addTrack("3rd staging track", "Staging");
//		// t.setLength(1);
//
//		t = l1.addTrack("4th staging track", "Staging");
//		// t.setLength(21);
//
//	}

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
    public void testPoolFrameCreate() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }

        // Make sure the frame gets created OK and has
        //CreateTestLocations();
        // Maybe this should use the LocationManager instead????
        Location l = new Location("LOC1", "Location One");

        Track t = new Track("ID1", "TestTrack1", "Siding", l);
        t.setLength(100);

        PoolTrackFrame f = new PoolTrackFrame(t);
        f.initComponents();
        //f.setVisible(true);

        // close windows
        JUnitUtil.dispose(f);
    }

    @Test
    @Ignore("commented out as JUnit3 Test")
    public void testOpenWithNullTrack() throws Exception {
        // See what happens when a null track is passed in.
        try {
            PoolTrackFrame f = new PoolTrackFrame((Track) null);
            Assert.fail("NullPointerException not thrown");

            f.initComponents();
            f.setVisible(true);

            // close windows
            JUnitUtil.dispose(f);
        } catch (NullPointerException e) {
            // Here we don't do anything, as this was expected.
        }
    }

    @Test
    public void testAddNewPoolName() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        // Enter a new Pool name and click Add, check that the Pool list count
        // is 1
        CreateTestLocations();

        // Maybe this should use the LocationManager instead????
        Location l = new Location("LOC1", "Location One");

        Track t = new Track("ID1", "TestTrack1", "Siding", l);
        t.setLength(100);

        // The track should really be passed to InitComponents, or should
        // it??????? Nope! The JMRI standard is for initComponents() without any
        // args. Data should be passed to the constructor, which will just store
        // it for initComponents() to use.
        PoolTrackFrame f = new PoolTrackFrame(t);
        f.setTitle("Test Pool Track Edit Frame");

        f.initComponents();

        f.setVisible(true);

        // Set the new Pool name
        f.trackPoolNameTextField.setText("Test Pool 1");

        // CLicking the Add button should add the new pool to the
        // collection.
        enterClickAndLeave(f.addButton);

        // Here the track's location should have a pool collection with one
        // item.
        int count;
        count = t.getLocation().getPoolsByNameList().size();
        Assert.assertEquals("Pool size", 1, count);

        // Try to add the same one again, and the count should remain at 1
        enterClickAndLeave(f.addButton);

        count = t.getLocation().getPoolsByNameList().size();
        Assert.assertEquals("Pool size", 1, count);

        // Add a different name and it should go to 2
        f.trackPoolNameTextField.setText("Test Pool 2");
        enterClickAndLeave(f.addButton);

        count = t.getLocation().getPoolsByNameList().size();
        Assert.assertEquals("Pool size", 2, count);

        // close window
        JUnitUtil.dispose(f);

    }

    @Test
    public void testSelectPoolAndSaveTrack() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
        // This should change the pool track property of the Track under test.
        Location l = new Location("LOC1", "Location One");
        l.addPool("Pool 1");
        Pool desiredPool = l.addPool("Pool 2");
        l.addPool("Pool 3");

        Assert.assertEquals("Pool count", 3, l.getPoolsByNameList().size());

        Track t = new Track("ID1", "TestTrack1", "Siding", l);
        Assert.assertEquals("Initial Track Pool", null, t.getPool());

        PoolTrackFrame f = new PoolTrackFrame(t);
        f.setTitle("Test Pool Track Select Pool and Save Frame");

        f.initComponents();

        f.comboBoxPools.setSelectedItem(desiredPool);
        Assert.assertEquals("ComboBox selection", desiredPool, f.comboBoxPools.getSelectedItem());

        // Now click the Save button and the Track should be updated with the selected Pool
        enterClickAndLeave(f.saveButton);
        Assert.assertEquals("Updated Track Pool", desiredPool, t.getPool());

        f.setVisible(true);

        // close window
        JUnitUtil.dispose(f);
    }

    @Test
    public void testSetMinLengthAndSaveTrack() throws Exception {
        if (GraphicsEnvironment.isHeadless()) {
            return; // can't use Assume in TestCase subclasses
        }
// Enter a new minimum length, click save and check that the Track is updated.
        Location l = new Location("LOC1", "Location One");
//		l.addPool("Pool 1");
//		Pool desiredPool = l.addPool("Pool 2");
//		l.addPool("Pool 3");

        Track t = new Track("ID1", "TestTrack1", "Siding", l);
        Assert.assertEquals("Minimum track length", 0, t.getMinimumLength());

        //Assert.assertEquals("Initial Track Pool", null, t.getPool());
        PoolTrackFrame f = new PoolTrackFrame(t);
        f.setTitle("Test Pool Track Enter min length and Save Frame");

        f.initComponents();

//		f.comboBoxPools.setSelectedItem(desiredPool);
//		Assert.assertEquals("ComboBox selection", desiredPool, f.comboBoxPools.getSelectedItem());
        f.trackMinLengthTextField.setText("23");

        // Now click the Save button and the Track should be updated with the selected Pool
        enterClickAndLeave(f.saveButton);
        //Assert.assertEquals("Updated Track Pool", desiredPool, t.getPool());
        Assert.assertEquals("Updated min track length", 23, t.getMinimumLength());

        f.setVisible(true);

        // close window
        JUnitUtil.dispose(f);
    }

    // // should be 5 rows
    // Assert.assertEquals("number of rows", 5, f.locationsModel.getRowCount());
    //
    // // default is sort by name
    // Assert.assertEquals("1st loc", "Test Loc A",
    // f.locationsModel.getValueAt(0, LocationsTableModel.NAMECOLUMN));
    // Assert.assertEquals("2nd loc", "Test Loc B",
    // f.locationsModel.getValueAt(1, LocationsTableModel.NAMECOLUMN));
    // Assert.assertEquals("3rd loc", "Test Loc C",
    // f.locationsModel.getValueAt(2, LocationsTableModel.NAMECOLUMN));
    // Assert.assertEquals("4th loc", "Test Loc D",
    // f.locationsModel.getValueAt(3, LocationsTableModel.NAMECOLUMN));
    // Assert.assertEquals("5th loc", "Test Loc E",
    // f.locationsModel.getValueAt(4, LocationsTableModel.NAMECOLUMN));
    //
    // // check location lengths
    // Assert.assertEquals("1st loc length", "1005", f.locationsModel
    // .getValueAt(0, LocationsTableModel.LENGTHCOLUMN));
    // Assert.assertEquals("2nd loc length", "1004", f.locationsModel
    // .getValueAt(1, LocationsTableModel.LENGTHCOLUMN));
    // Assert.assertEquals("3rd loc length", "1003", f.locationsModel
    // .getValueAt(2, LocationsTableModel.LENGTHCOLUMN));
    // Assert.assertEquals("4th loc length", "1002", f.locationsModel
    // .getValueAt(3, LocationsTableModel.LENGTHCOLUMN));
    // Assert.assertEquals("5th loc length", "1001", f.locationsModel
    // .getValueAt(4, LocationsTableModel.LENGTHCOLUMN));
    //
    // // create add location frame by clicking add button
    // // f.addButton.doClick();
    // enterClickAndLeave(f.addButton);
    //
    // // confirm location add frame creation
    // JmriJFrame lef = JmriJFrame.getFrame("Add Location");
    // Assert.assertNotNull(lef);
    //
    // // create edit location frame
    // f.locationsModel.setValueAt(null, 2, LocationsTableModel.EDITCOLUMN);
    //
    // // Need to check if it was created OK
    // JmriJFrame elf = JmriJFrame.getFrame("Edit Location");
    // Assert.assertNotNull("EditFrame", elf);
    //
    // // close windows
    // JUnitUtil.dispose(f);
    // JUnitUtil.dispose(lef);
    // JUnitUtil.dispose(elf);
    //
    // }
    @Test
    @Ignore("Not sure if we need this one....")
    public void testVerifyStatusPanel() {
    }

    // Ensure minimal setup for log4J
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    // The minimal setup for log4J
    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }
}
