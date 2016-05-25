package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.OperationsSwingTestCase;
import junit.extensions.jfcunit.eventdata.MouseEventData;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Tests for the Operations PoolTrackFrame class
 *
 * @author Gregory Madsen Copyright (C) 2012
 */
public class PoolTrackGuiTest extends OperationsSwingTestCase {

    //final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;
    private void CreateTestLocations() {
        // Clear out any previous locations
        LocationManager.instance().dispose();

        // Create 5 locations
        LocationManager lManager = LocationManager.instance();

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
//		LocationManager lManager = LocationManager.instance();
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
//		LocationManager lManager = LocationManager.instance();
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
//		LocationManager lManager = LocationManager.instance();
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
//		LocationManager lManager = LocationManager.instance();
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
    public void testPoolFrameCreate() throws Exception {
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
        f.dispose();
    }

//	public void testOpenWithNullTrack() throws Exception {
//		// See what happens when a null track is passed in.
//		try {
//			PoolTrackFrame f = new PoolTrackFrame((Track) null);
//			Assert.fail("NullPointerException not thrown");
//
//			f.initComponents();
//			f.setVisible(true);
//
//			// close windows
//			f.dispose();
//		} catch (NullPointerException e) {
//			// Here we don't do anything, as this was expected.
//		}
//
//	}
    public void testAddNewPoolName() throws Exception {
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
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));

        // Here the track's location should have a pool collection with one
        // item.
        int count;
        count = t.getLocation().getPoolsByNameList().size();
        Assert.assertEquals("Pool size", 1, count);

        // Try to add the same one again, and the count should remain at 1
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));

        count = t.getLocation().getPoolsByNameList().size();
        Assert.assertEquals("Pool size", 1, count);

        // Add a different name and it should go to 2
        f.trackPoolNameTextField.setText("Test Pool 2");
        getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));

        count = t.getLocation().getPoolsByNameList().size();
        Assert.assertEquals("Pool size", 2, count);

        // close window
        f.dispose();

    }

    public void testSelectPoolAndSaveTrack() throws Exception {
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
        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
        Assert.assertEquals("Updated Track Pool", desiredPool, t.getPool());

        f.setVisible(true);

        // close window
        f.dispose();
    }

    public void testSetMinLengthAndSaveTrack() throws Exception {
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
        getHelper().enterClickAndLeave(new MouseEventData(this, f.saveButton));
        //Assert.assertEquals("Updated Track Pool", desiredPool, t.getPool());
        Assert.assertEquals("Updated min track length", 23, t.getMinimumLength());

        f.setVisible(true);

        // close window
        f.dispose();
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
    // getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));
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
    // f.dispose();
    // lef.dispose();
    // elf.dispose();
    //
    // }
    public void testVerifyStatusPanel() {
        // Not sure if we need this one...
    }

	// ***************************************************
    // OLD Locations stuff below here, just for reference.....
    // public void testLocationsTableFrame() {
    // // Make sure the frames gets created OK and have the list of current
    // // locations.
    //
    // // // clear out previous locations
    // // LocationManager.instance().dispose();
    // // // create 5 locations
    // // LocationManager lManager = LocationManager.instance();
    // // Location l1 = lManager.newLocation("Test Loc E");
    // // l1.setLength(1001);
    // // Location l2 = lManager.newLocation("Test Loc D");
    // // l2.setLength(1002);
    // // Location l3 = lManager.newLocation("Test Loc C");
    // // l3.setLength(1003);
    // // Location l4 = lManager.newLocation("Test Loc B");
    // // l4.setLength(1004);
    // // Location l5 = lManager.newLocation("Test Loc A");
    // // l5.setLength(1005);
    //
    // CreateTestLocations();
    //
    // LocationsTableFrame f = new LocationsTableFrame();
    // f.setVisible(true);
    //
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
    // getHelper().enterClickAndLeave(new MouseEventData(this, f.addButton));
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
    // f.dispose();
    // lef.dispose();
    // elf.dispose();
    // }
    // public void testLocationEditFrame() {
    // // Tests the Location Edit frame - only tests changing the location
    // // name, but could also test changing Direction and other properties.
    //
    // CreateTestLocations();
    //
    // LocationEditFrame f = new LocationEditFrame();
    // f.setTitle("Test Add Location Frame");
    // f.initComponents(null);
    //
    // f.locationNameTextField.setText("New Test Location");
    //
    // // CLicking the Add button should add the new location to the
    // // collection.
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addLocationButton));
    //
    // LocationManager lManager = LocationManager.instance();
    // // Make sure we have one more location than before.
    // Assert.assertEquals("should be 6 locations", 6, lManager
    // .getLocationsByNameList().size());
    //
    // Location newLoc = lManager.getLocationByName("New Test Location");
    // Assert.assertNotNull(newLoc);
    //
    // // Not sure why these are here as there is nothing we can test.
    // // // add a yard track
    // // getHelper().enterClickAndLeave(
    // // new MouseEventData(this, f.addYardButton));
    // //
    // // // add an interchange track
    // // getHelper().enterClickAndLeave(
    // // new MouseEventData(this, f.addInterchangeButton));
    // //
    // // // add a staging track
    // // getHelper().enterClickAndLeave(
    // // new MouseEventData(this, f.addStagingButton));
    // //
    // // // add a yard track
    // // // f.addYardButton.doClick();
    // // getHelper().enterClickAndLeave(
    // // new MouseEventData(this, f.addYardButton));
    //
    // f.locationNameTextField.setText("Newer Test Location");
    //
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.saveLocationButton));
    //
    // // Assert.assertEquals("after adding yard track", 1,
    // // newLoc.getTrackCount());
    //
    // Assert.assertEquals("changed location name", "Newer Test Location",
    // newLoc.getName());
    //
    // // test delete button
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.deleteLocationButton));
    // Assert.assertEquals("should be 6 locations before Delete", 6, lManager
    // .getLocationsByNameList().size());
    //
    // // confirm delete dialog window should appear
    // pressDialogButton(f, "Yes");
    //
    // // location now deleted
    // Assert.assertEquals("should be 5 locations after Delete", 5, lManager
    // .getLocationsByNameList().size());
    //
    // f.dispose();
    // }
    //
    // public void testInterchangeEditFrame() {
    //
    // CreateTestLocations();
    //
    // // add UP road name
    // CarRoads cr = CarRoads.instance();
    // cr.addName("UP");
    //
    // LocationManager lManager = LocationManager.instance();
    // Location l = lManager.getLocationByName("Test Loc C");
    // Assert.assertNotNull("Sample location should not be null", l);
    //
    // InterchangeEditFrame f = new InterchangeEditFrame();
    // f.setTitle("Test Interchange Add Frame");
    // f.setLocation(0, 0); // entire panel must be visible for tests to work
    // // properly
    // f.initComponents(l, null);
    //
    // // create two interchange tracks
    // f.trackNameTextField.setText("new interchange track");
    // f.trackLengthTextField.setText("321");
    // // Assert.assertTrue("Add button is showing",
    // // f.addTrackButton.isShowing());
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // f.trackNameTextField.setText("2nd interchange track");
    // f.trackLengthTextField.setText("4331");
    // // f.addTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // // deselect east and south check boxes
    // // f.eastCheckBox.doClick();
    // getHelper()
    // .enterClickAndLeave(new MouseEventData(this, f.eastCheckBox));
    // // f.southCheckBox.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.southCheckBox));
    //
    // // accept only UP road
    // // f.roadNameInclude.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.roadNameInclude));
    // f.comboBoxRoads.setSelectedItem("UP");
    // // f.addRoadButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addRoadButton));
    //
    // // f.saveTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.saveTrackButton));
    //
    // Track t = l.getTrackByName("new interchange track", Track.INTERCHANGE);
    // Assert.assertNotNull("new interchange track", t);
    // Assert.assertEquals("interchange track length", 321, t.getLength());
    // // check that the defaults are correct
    // Assert.assertEquals("all directions", ALL, t.getTrainDirections());
    // Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
    //
    // t = l.getTrackByName("2nd interchange track", Track.INTERCHANGE);
    // Assert.assertNotNull("2nd interchange track", t);
    // Assert.assertEquals("2nd interchange track length", 4331, t.getLength());
    // Assert.assertEquals("west and north", Track.NORTH + Track.WEST,
    // t.getTrainDirections());
    // Assert.assertEquals("include roads", Track.INCLUDEROADS,
    // t.getRoadOption());
    // Assert.assertTrue("only UP road", t.acceptsRoadName("UP"));
    // Assert.assertFalse("2nd interchange Road2", t.acceptsRoadName("Road2"));
    //
    // // check track accepts Boxcars
    // Assert.assertTrue("2nd interchange track accepts Boxcars",
    // t.acceptsTypeName("Boxcar"));
    // // test clear car types button
    // // f.clearButton.doClick();
    // getHelper().enterClickAndLeave(new MouseEventData(this, f.clearButton));
    // // f.saveTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.saveTrackButton));
    // Assert.assertFalse("2nd interchange track doesn't accept Boxcars",
    // t.acceptsTypeName("Boxcar"));
    //
    // // f.setButton.doClick();
    // getHelper().enterClickAndLeave(new MouseEventData(this, f.setButton));
    // // f.saveTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.saveTrackButton));
    // Assert.assertTrue("2nd interchange track accepts Boxcars again",
    // t.acceptsTypeName("Boxcar"));
    //
    // f.dispose();
    // }
    //
    // public void testSidingEditFrame() {
    // CreateTestLocations();
    //
    // LocationManager lManager = LocationManager.instance();
    // Location l = lManager.getLocationByName("Test Loc C");
    // Assert.assertNotNull("Sample location should not be null", l);
    //
    // SidingEditFrame f = new SidingEditFrame();
    // f.setTitle("Test Siding Add Frame");
    // f.setLocation(0, 0); // entire panel must be visible for tests to work
    // // properly
    // f.initComponents(l, null);
    //
    // // create three siding tracks
    // f.trackNameTextField.setText("new siding track");
    // f.trackLengthTextField.setText("1223");
    // // f.addTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // f.trackNameTextField.setText("2nd siding track");
    // f.trackLengthTextField.setText("9999");
    // // f.addTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // f.trackNameTextField.setText("3rd siding track");
    // f.trackLengthTextField.setText("1010");
    // // f.addTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // // deselect east, west and north check boxes
    // // f.eastCheckBox.doClick();
    // getHelper()
    // .enterClickAndLeave(new MouseEventData(this, f.eastCheckBox));
    // // f.westCheckBox.doClick();
    // getHelper()
    // .enterClickAndLeave(new MouseEventData(this, f.westCheckBox));
    // // f.northCheckBox.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.northCheckBox));
    //
    // // exclude UP road
    // // f.roadNameExclude.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.roadNameExclude));
    // f.comboBoxRoads.setSelectedItem("UP");
    // // f.addRoadButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addRoadButton));
    //
    // // f.saveTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.saveTrackButton));
    //
    // Track t = l.getTrackByName("new siding track", null);
    // Assert.assertNotNull("new siding track", t);
    // Assert.assertEquals("siding track length", 1223, t.getLength());
    // // check that the defaults are correct
    // Assert.assertEquals("all directions", ALL, t.getTrainDirections());
    // Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
    //
    // t = l.getTrackByName("2nd siding track", null);
    // Assert.assertNotNull("2nd siding track", t);
    // Assert.assertEquals("2nd siding track length", 9999, t.getLength());
    // // check that the defaults are correct
    // Assert.assertEquals("all directions", ALL, t.getTrainDirections());
    // Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
    //
    // t = l.getTrackByName("3rd siding track", null);
    // Assert.assertNotNull("3rd siding track", t);
    // Assert.assertEquals("3rd siding track length", 1010, t.getLength());
    //
    // Assert.assertEquals("only south", Track.SOUTH, t.getTrainDirections());
    // Assert.assertEquals("exclude roads", Track.EXCLUDEROADS,
    // t.getRoadOption());
    // Assert.assertFalse("only UP road", t.acceptsRoadName("UP"));
    // Assert.assertTrue("3rd siding Road2", t.acceptsRoadName("Road2"));
    //
    // // create the schedule edit frame
    // // f.editScheduleButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.editScheduleButton));
    //
    // // confirm schedule add frame creation
    // JmriJFrame sef = JmriJFrame
    // .getFrame("Add Schedule for Spur 3rd siding track");
    // Assert.assertNotNull(sef);
    //
    // // kill all frames
    // f.dispose();
    // sef.dispose();
    // }
    //
    // /**
    // * Staging tracks needs its own location
    // */
    // public void testStagingEditFrame() {
    // CreateTestLocations();
    //
    // LocationManager lManager = LocationManager.instance();
    // Location l = lManager.getLocationByName("Test Loc A");
    // Assert.assertNotNull("Sample location should not be null", l);
    //
    // StagingEditFrame f = new StagingEditFrame();
    // f.setTitle("Test Staging Add Frame");
    // f.setLocation(0, 0); // entire panel must be visible for tests to work
    // // properly
    // f.initComponents(l, null);
    //
    // // create four staging tracks
    // f.trackNameTextField.setText("new staging track");
    // f.trackLengthTextField.setText("34");
    // // f.addTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // f.trackNameTextField.setText("2nd staging track");
    // f.trackLengthTextField.setText("3456");
    // // f.addTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // f.trackNameTextField.setText("3rd staging track");
    // f.trackLengthTextField.setText("1");
    // // f.addTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // f.trackNameTextField.setText("4th staging track");
    // f.trackLengthTextField.setText("12");
    // // f.addTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // // deselect east, west and south check boxes
    // // f.northCheckBox.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.northCheckBox));
    // // f.westCheckBox.doClick();
    // getHelper()
    // .enterClickAndLeave(new MouseEventData(this, f.westCheckBox));
    // // f.southCheckBox.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.southCheckBox));
    //
    // // f.saveTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.saveTrackButton));
    //
    // sleep(1); // for slow machines
    // Track t = l.getTrackByName("new staging track", null);
    // Assert.assertNotNull("new staging track", t);
    // Assert.assertEquals("staging track length", 34, t.getLength());
    // // check that the defaults are correct
    // Assert.assertEquals("all directions", ALL, t.getTrainDirections());
    // Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
    //
    // t = l.getTrackByName("2nd staging track", null);
    // Assert.assertNotNull("2nd staging track", t);
    // Assert.assertEquals("2nd staging track length", 3456, t.getLength());
    // // check that the defaults are correct
    // Assert.assertEquals("all directions", ALL, t.getTrainDirections());
    // Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
    //
    // t = l.getTrackByName("3rd staging track", null);
    // Assert.assertNotNull("3rd staging track", t);
    // Assert.assertEquals("3rd staging track length", 1, t.getLength());
    // // check that the defaults are correct
    // Assert.assertEquals("all directions", ALL, t.getTrainDirections());
    // Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
    //
    // t = l.getTrackByName("4th staging track", null);
    // Assert.assertNotNull("4th staging track", t);
    // Assert.assertEquals("4th staging track length", 12, t.getLength());
    // Assert.assertEquals("only east", Track.EAST, t.getTrainDirections());
    //
    // f.dispose();
    // }
    //
    // public void testYardEditFrame() {
    // CreateTestLocations();
    //
    // LocationManager lManager = LocationManager.instance();
    // Location l = lManager.getLocationByName("Test Loc C");
    // Assert.assertNotNull("Sample location should not be null", l);
    //
    // YardEditFrame f = new YardEditFrame();
    // f.setTitle("Test Yard Add Frame");
    // f.initComponents(l, null);
    //
    // // create four yard tracks
    // f.trackNameTextField.setText("new yard track");
    // f.trackLengthTextField.setText("43");
    // // f.addTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // f.trackNameTextField.setText("2nd yard track");
    // f.trackLengthTextField.setText("6543");
    // // f.addTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // f.trackNameTextField.setText("3rd yard track");
    // f.trackLengthTextField.setText("1");
    // // f.addTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // f.trackNameTextField.setText("4th yard track");
    // f.trackLengthTextField.setText("21");
    // // f.addTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTrackButton));
    //
    // // deselect east, west and south check boxes
    // // f.eastCheckBox.doClick();
    // getHelper()
    // .enterClickAndLeave(new MouseEventData(this, f.eastCheckBox));
    // // f.westCheckBox.doClick();
    // getHelper()
    // .enterClickAndLeave(new MouseEventData(this, f.westCheckBox));
    // // f.southCheckBox.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.southCheckBox));
    //
    // // f.saveTrackButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.saveTrackButton));
    //
    // Track t = l.getTrackByName("new yard track", null);
    // Assert.assertNotNull("new yard track", t);
    // Assert.assertEquals("yard track length", 43, t.getLength());
    // // check that the defaults are correct
    // Assert.assertEquals("all directions", ALL, t.getTrainDirections());
    // Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
    //
    // t = l.getTrackByName("2nd yard track", null);
    // Assert.assertNotNull("2nd yard track", t);
    // Assert.assertEquals("2nd yard track length", 6543, t.getLength());
    // // check that the defaults are correct
    // Assert.assertEquals("all directions", ALL, t.getTrainDirections());
    // Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
    //
    // t = l.getTrackByName("3rd yard track", null);
    // Assert.assertNotNull("3rd yard track", t);
    // Assert.assertEquals("3rd yard track length", 1, t.getLength());
    // // check that the defaults are correct
    // Assert.assertEquals("all directions", ALL, t.getTrainDirections());
    // Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
    //
    // t = l.getTrackByName("4th yard track", null);
    // Assert.assertNotNull("4th yard track", t);
    // Assert.assertEquals("4th yard track length", 21, t.getLength());
    // Assert.assertEquals("only north", Track.NORTH, t.getTrainDirections());
    //
    // f.dispose();
    // }
    //
    // /**
    // * This test builds on the previous testInterchangeEditFrame(),
    // * testSidingEditFrame(), and testYardEditFrame() tests.
    // *
    // * It doesn't really test much, but maybe I'm missing something. In any
    // * event, it now passes on its own.
    // */
    // public void testLocationEditFrameRead() {
    // CreateTestLocations();
    //
    // AddTestSidings();
    // AddTestInterchanges();
    // AddTestYardTracks();
    //
    // LocationManager lManager = LocationManager.instance();
    // Location l2 = lManager.getLocationByName("Test Loc C");
    // Assert.assertNotNull("Location exists", l2);
    //
    // LocationEditFrame f = new LocationEditFrame();
    // f.setTitle("Test Edit Location Frame");
    // f.initComponents(l2);
    //
    // // check location name
    // Assert.assertEquals("name", "Test Loc C",
    // f.locationNameTextField.getText());
    //
    // Assert.assertEquals("number of sidings", 3, f.sidingModel.getRowCount());
    //
    // Assert.assertEquals("number of interchanges", 2,
    // f.interchangeModel.getRowCount());
    //
    // Assert.assertEquals("number of yards", 4, f.yardModel.getRowCount());
    //
    // Assert.assertEquals("number of staging tracks", 0,
    // f.stagingModel.getRowCount());
    //
    // f.dispose();
    // }
    //
    // /**
    // * This seems to be dependent on other tests......
    // *
    // * It doens't seem to test anything of the GUI, but maybe I'm missing
    // * something.
    // */
    // public void testLocationEditFrameReadStaging() {
    // CreateTestLocations();
    // AddTestStagingTracks();
    //
    // LocationManager lManager = LocationManager.instance();
    // Location l2 = lManager.getLocationByName("Test Loc A");
    // Assert.assertNotNull("Sample location should not be null", l2);
    //
    // LocationEditFrame f = new LocationEditFrame();
    // f.setTitle("Test Edit Location Frame Staging");
    // f.initComponents(l2);
    //
    // // check location name
    // Assert.assertEquals("name", "Test Loc A",
    // f.locationNameTextField.getText());
    //
    // Assert.assertEquals("number of sidings", 0, f.sidingModel.getRowCount());
    // Assert.assertEquals("number of interchanges", 0,
    // f.interchangeModel.getRowCount());
    // Assert.assertEquals("number of yards", 0, f.yardModel.getRowCount());
    // Assert.assertEquals("number of staging tracks", 4,
    // f.stagingModel.getRowCount());
    //
    // // is the staging only button selected?
    // Assert.assertTrue("staging selected", f.stageRadioButton.isSelected());
    //
    // f.dispose();
    // }
    //
    // public void testScheduleEditFrame() {
    // CreateTestLocations();
    //
    // AddTestSidings();
    //
    // LocationManager lManager = LocationManager.instance();
    // Location l = lManager.getLocationByName("Test Loc C");
    // Assert.assertNotNull("Location exists", l);
    //
    // Track t = l.getTrackByName("3rd siding track", null);
    // Assert.assertNotNull("Track exists", t);
    //
    // ScheduleEditFrame f = new ScheduleEditFrame();
    // f.setTitle("Test Schedule Frame");
    // f.initComponents(null, l, t);
    // f.scheduleNameTextField.setText("Test Schedule A");
    // f.commentTextField.setText("Test Comment");
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addScheduleButton));
    //
    // // was the schedule created?
    // ScheduleManager m = ScheduleManager.instance();
    // Schedule s = m.getScheduleByName("Test Schedule A");
    // Assert.assertNotNull("Test Schedule A exists", s);
    //
    // // now add some car types to the schedule
    // f.typeBox.setSelectedItem("Boxcar");
    // // f.addTypeButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTypeButton));
    // f.typeBox.setSelectedItem("Flatcar");
    // // f.addTypeButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTypeButton));
    // f.typeBox.setSelectedItem("Coilcar");
    // // f.addTypeButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTypeButton));
    // // put Tank Food at start of list
    // f.typeBox.setSelectedItem("Tank Food");
    // // f.addLocAtTop.doClick();
    // getHelper().enterClickAndLeave(new MouseEventData(this, f.addLocAtTop));
    // // f.addTypeButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.addTypeButton));
    // // f.saveScheduleButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.saveScheduleButton));
    //
    // List<String> list = s.getItemsBySequenceList();
    // Assert.assertEquals("number of items", 4, list.size());
    //
    // ScheduleItem si = s.getItemById(list.get(0));
    // Assert.assertEquals("1st type", "Tank Food", si.getType());
    // si = s.getItemById(list.get(1));
    // Assert.assertEquals("2nd type", "Boxcar", si.getType());
    // si = s.getItemById(list.get(2));
    // Assert.assertEquals("3rd type", "Flatcar", si.getType());
    // si = s.getItemById(list.get(3));
    // Assert.assertEquals("3rd type", "Coilcar", si.getType());
    //
    // // f.deleteScheduleButton.doClick();
    // getHelper().enterClickAndLeave(
    // new MouseEventData(this, f.deleteScheduleButton));
    // // Yes to pop up
    // pressDialogButton(f, "Yes");
    // s = m.getScheduleByName("Test Schedule A");
    // Assert.assertNull("Test Schedule A exists", s);
    //
    // f.dispose();
    // }
    //
    // // This needs some asserts, otherwise it doesn't test anything....
    // public void testScheduleTableFrame() {
    // SchedulesTableFrame f = new SchedulesTableFrame();
    // f.setVisible(true);
    // f.dispose();
    // }

    // Ensure minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public PoolTrackGuiTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {"-noloading",
            PoolTrackGuiTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(PoolTrackGuiTest.class);
        return suite;
    }

    // The minimal setup for log4J
    @Override
    protected void tearDown() throws Exception {
        // apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
