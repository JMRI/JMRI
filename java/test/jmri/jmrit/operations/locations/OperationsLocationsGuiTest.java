//OperationsLocationsGuiTest.java

package jmri.jmrit.operations.locations;

import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarRoads;

import jmri.util.JmriJFrame;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.jfcunit.eventdata.*;
import junit.extensions.jfcunit.finder.AbstractButtonFinder;
import junit.extensions.jfcunit.finder.DialogFinder;

import java.io.File;
import java.util.List;
import java.util.Locale;

import javax.swing.JComboBox;

/**
 * Tests for the Operations Locations GUI class
 *  
 * @author	Dan Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class OperationsLocationsGuiTest extends jmri.util.SwingTestCase {
	
	final static int ALL = Track.EAST + Track.WEST + Track.NORTH + Track.SOUTH;

	public void testLocationsTableFrame(){
		// clear out previous locations
		LocationManager.instance().dispose();	
		// create 5 locations
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
		LocationsTableFrame f = new LocationsTableFrame();
		f.setVisible(true);
		
		// should be 5 rows
		Assert.assertEquals("number of rows", 5, f.locationsModel.getRowCount());
		// default is sort by name
		Assert.assertEquals("1st loc", "Test Loc A", f.locationsModel.getValueAt(0, LocationsTableModel.NAMECOLUMN));
		Assert.assertEquals("2nd loc", "Test Loc B", f.locationsModel.getValueAt(1, LocationsTableModel.NAMECOLUMN));
		Assert.assertEquals("3rd loc", "Test Loc C", f.locationsModel.getValueAt(2, LocationsTableModel.NAMECOLUMN));
		Assert.assertEquals("4th loc", "Test Loc D", f.locationsModel.getValueAt(3, LocationsTableModel.NAMECOLUMN));
		Assert.assertEquals("5th loc", "Test Loc E", f.locationsModel.getValueAt(4, LocationsTableModel.NAMECOLUMN));
	
		// check location lengths
		Assert.assertEquals("1st loc length", "1005", f.locationsModel.getValueAt(0, LocationsTableModel.LENGTHCOLUMN));
		Assert.assertEquals("2nd loc length", "1004", f.locationsModel.getValueAt(1, LocationsTableModel.LENGTHCOLUMN));
		Assert.assertEquals("3rd loc length", "1003", f.locationsModel.getValueAt(2, LocationsTableModel.LENGTHCOLUMN));
		Assert.assertEquals("4th loc length", "1002", f.locationsModel.getValueAt(3, LocationsTableModel.LENGTHCOLUMN));
		Assert.assertEquals("5th loc length", "1001", f.locationsModel.getValueAt(4, LocationsTableModel.LENGTHCOLUMN));
		
		// create add location frame by clicking add button
		//f.addButton.doClick();
        getHelper().enterClickAndLeave( new MouseEventData( this, f.addButton ) );
		
        // confirm location add frame creation
        JmriJFrame lef = JmriJFrame.getFrame("Add Location");
        Assert.assertNotNull(lef);
        
        // create edit location frame
		f.locationsModel.setValueAt(null, 2, LocationsTableModel.EDITCOLUMN);
		
		// close windows
		f.dispose();
		lef.dispose();
	}

	public void testLocationEditFrame(){
		LocationEditFrame f = new LocationEditFrame();
		f.setTitle("Test Add Location Frame");
		f.initComponents(null);
		
		f.locationNameTextField.setText("New Test Location");
		//f.addLocationButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addLocationButton ) );

		
		LocationManager lManager = LocationManager.instance();
		Assert.assertEquals("should be 6 locations", 6, lManager.getLocationsByNameList().size());
		Location newLoc = lManager.getLocationByName("New Test Location");
		
		Assert.assertNotNull(newLoc);
		
		// add a yard track
		//f.addYardButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addYardButton ) );
		
		// add an interchange track
		//f.addInterchangeButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addInterchangeButton ) );
		
		// add a staging track
		//f.addStagingButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addStagingButton ) );
		
		// add a yard track
		//f.addYardButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addYardButton ) );
		
		f.locationNameTextField.setText("Newer Test Location");
		//f.saveLocationButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveLocationButton ) );
		
		Assert.assertEquals("changed location name", "Newer Test Location", newLoc.getName());
		
		// test delete button
		//f.deleteLocationButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.deleteLocationButton ) );
		Assert.assertEquals("should be 6 locations", 6, lManager.getLocationsByNameList().size());
		// confirm delete dialog window should appear
		pressDialogButton(f, "Yes");
		// location now deleted
		Assert.assertEquals("should be 5 locations", 5, lManager.getLocationsByNameList().size());
		
		f.dispose();
	}
	
	public void testInterchangeEditFrame(){
		// add UP road name
		CarRoads cr = CarRoads.instance();
		cr.addName("UP");
		
		LocationManager lManager = LocationManager.instance();
		Location l = lManager.getLocationByName("Test Loc C");
		InterchangeEditFrame f = new InterchangeEditFrame();
		f.setTitle("Test Interchange Add Frame");
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents(l, null);
		
		// create two interchange tracks
		f.trackNameTextField.setText("new interchange track");
		f.trackLengthTextField.setText("321");
		//Assert.assertTrue("Add button is showing", f.addTrackButton.isShowing());
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("2nd interchange track");
		f.trackLengthTextField.setText("4331");
		//f.addTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		// deselect east and south check boxes
		//f.eastCheckBox.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.eastCheckBox ) );
		//f.southCheckBox.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.southCheckBox ) );
		
//		// accept only UP road
//		//f.roadNameInclude.doClick();
//		getHelper().enterClickAndLeave( new MouseEventData( this, f.roadNameInclude ) );
//		f.comboBoxRoads.setSelectedItem("UP");
//		//f.addRoadButton.doClick();
//		getHelper().enterClickAndLeave( new MouseEventData( this, f.addRoadButton ) );
		
		//f.saveTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrackButton ) );
		
		Track t = l.getTrackByName("new interchange track", Track.INTERCHANGE);	
		Assert.assertNotNull("new interchange track", t);
		Assert.assertEquals("interchange track length", 321, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
				
		t = l.getTrackByName("2nd interchange track", Track.INTERCHANGE);	
		Assert.assertNotNull("2nd interchange track", t);
		Assert.assertEquals("2nd interchange track length", 4331, t.getLength());
		Assert.assertEquals("west and north", Track.NORTH+Track.WEST, t.getTrainDirections());
//		Assert.assertEquals("include roads", Track.INCLUDEROADS, t.getRoadOption());
//		Assert.assertTrue("only UP road", t.acceptsRoadName("UP"));
//		Assert.assertFalse("2nd interchange Road2", t.acceptsRoadName("Road2"));
		
		// check track accepts Boxcars
		Assert.assertTrue("2nd interchange track accepts Boxcars", t.acceptsTypeName("Boxcar"));
		// test clear car types button
		//f.clearButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.clearButton ) );
		//f.saveTrackButton.doClick();	
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrackButton ) );
		Assert.assertFalse("2nd interchange track doesn't accept Boxcars", t.acceptsTypeName("Boxcar"));
		
		//f.setButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.setButton ) );
		//f.saveTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrackButton ) );
		Assert.assertTrue("2nd interchange track accepts Boxcars again", t.acceptsTypeName("Boxcar"));	
		
		f.dispose();
	}
	
	public void testSidingEditFrame(){		
		LocationManager lManager = LocationManager.instance();
		Location l = lManager.getLocationByName("Test Loc C");
		SidingEditFrame f = new SidingEditFrame();
		f.setTitle("Test Siding Add Frame");
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents(l, null);
		
		// create three siding tracks
		f.trackNameTextField.setText("new siding track");
		f.trackLengthTextField.setText("1223");
		//f.addTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("2nd siding track");
		f.trackLengthTextField.setText("9999");
		//f.addTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("3rd siding track");
		f.trackLengthTextField.setText("1010");
		//f.addTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		// deselect east, west and north check boxes
		//f.eastCheckBox.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.eastCheckBox ) );
		//f.westCheckBox.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.westCheckBox ) );
		//f.northCheckBox.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.northCheckBox ) );
		
		// exclude UP road
		//f.roadNameExclude.doClick();
//		getHelper().enterClickAndLeave( new MouseEventData( this, f.roadNameExclude ) );
//		f.comboBoxRoads.setSelectedItem("UP");
//		//f.addRoadButton.doClick();
//		getHelper().enterClickAndLeave( new MouseEventData( this, f.addRoadButton ) );
		
		//f.saveTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrackButton ) );
		
		Track t = l.getTrackByName("new siding track", null);	
		Assert.assertNotNull("new siding track", t);
		Assert.assertEquals("siding track length", 1223, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
				
		t = l.getTrackByName("2nd siding track", null);	
		Assert.assertNotNull("2nd siding track", t);
		Assert.assertEquals("2nd siding track length", 9999, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
		
		t = l.getTrackByName("3rd siding track", null);	
		Assert.assertNotNull("3rd siding track", t);
		Assert.assertEquals("3rd siding track length", 1010, t.getLength());
		
		Assert.assertEquals("only south", Track.SOUTH, t.getTrainDirections());
//		Assert.assertEquals("exclude roads", Track.EXCLUDEROADS, t.getRoadOption());
//		Assert.assertFalse("only UP road", t.acceptsRoadName("UP"));
//		Assert.assertTrue("3rd siding Road2", t.acceptsRoadName("Road2"));
		
		// create the schedule edit frame
		//f.editScheduleButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.editScheduleButton ) );
		
        // confirm schedule add frame creation
        JmriJFrame sef = JmriJFrame.getFrame("Add Schedule for Spur 3rd siding track");
        Assert.assertNotNull(sef);
		
        // kill all frames
		f.dispose();
		sef.dispose();
	}
	
	/**
	 * Staging tracks needs its own location
	 */
	public void testStagingEditFrame(){		
		LocationManager lManager = LocationManager.instance();
		Location l = lManager.getLocationByName("Test Loc A");
		StagingEditFrame f = new StagingEditFrame();
		f.setTitle("Test Staging Add Frame");
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents(l, null);
		
		// create four staging tracks
		f.trackNameTextField.setText("new staging track");
		f.trackLengthTextField.setText("34");
		//f.addTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("2nd staging track");
		f.trackLengthTextField.setText("3456");
		//f.addTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("3rd staging track");
		f.trackLengthTextField.setText("1");
		//f.addTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("4th staging track");
		f.trackLengthTextField.setText("12");
		//f.addTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		// deselect east, west and south check boxes
		//f.northCheckBox.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.northCheckBox ) );
		//f.westCheckBox.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.westCheckBox ) );
		//f.southCheckBox.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.southCheckBox ) );
		
		//f.saveTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrackButton ) );
		
		sleep(1);	// for slow machines
		Track t = l.getTrackByName("new staging track", null);	
		Assert.assertNotNull("new staging track", t);
		Assert.assertEquals("staging track length", 34, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
				
		t = l.getTrackByName("2nd staging track", null);	
		Assert.assertNotNull("2nd staging track", t);
		Assert.assertEquals("2nd staging track length", 3456, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
		
		t = l.getTrackByName("3rd staging track", null);	
		Assert.assertNotNull("3rd staging track", t);
		Assert.assertEquals("3rd staging track length", 1, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());

		t = l.getTrackByName("4th staging track", null);	
		Assert.assertNotNull("4th staging track", t);
		Assert.assertEquals("4th staging track length", 12, t.getLength());		
		Assert.assertEquals("only east", Track.EAST, t.getTrainDirections());	
		
		f.dispose();
	}
	
	public void testYardEditFrame(){
		LocationManager lManager = LocationManager.instance();
		Location l = lManager.getLocationByName("Test Loc C");
		YardEditFrame f = new YardEditFrame();
		f.setTitle("Test Yard Add Frame");
		f.initComponents(l, null);
		
		// create four yard tracks
		f.trackNameTextField.setText("new yard track");
		f.trackLengthTextField.setText("43");
		//f.addTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("2nd yard track");
		f.trackLengthTextField.setText("6543");
		//f.addTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("3rd yard track");
		f.trackLengthTextField.setText("1");
		//f.addTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("4th yard track");
		f.trackLengthTextField.setText("21");
		//f.addTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		// deselect east, west and south check boxes
		//f.eastCheckBox.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.eastCheckBox ) );
		//f.westCheckBox.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.westCheckBox ) );
		//f.southCheckBox.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.southCheckBox ) );
		
		//f.saveTrackButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrackButton ) );
		
		Track t = l.getTrackByName("new yard track", null);	
		Assert.assertNotNull("new yard track", t);
		Assert.assertEquals("yard track length", 43, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
				
		t = l.getTrackByName("2nd yard track", null);	
		Assert.assertNotNull("2nd yard track", t);
		Assert.assertEquals("2nd yard track length", 6543, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());
		
		t = l.getTrackByName("3rd yard track", null);	
		Assert.assertNotNull("3rd yard track", t);
		Assert.assertEquals("3rd yard track length", 1, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALLROADS, t.getRoadOption());

		t = l.getTrackByName("4th yard track", null);	
		Assert.assertNotNull("4th yard track", t);
		Assert.assertEquals("4th yard track length", 21, t.getLength());		
		Assert.assertEquals("only north", Track.NORTH, t.getTrainDirections());
		
		f.dispose();
	}
	
	/**
	 * This test builds on the previous testInterchangeEditFrame(),
	 * testSidingEditFrame(), and testYardEditFrame() tests.
	 */
	public void testLocationEditFrameRead(){
		LocationManager lManager = LocationManager.instance();
		Location l2 = lManager.getLocationByName("Test Loc C");
		
		LocationEditFrame f = new LocationEditFrame();
		f.setTitle("Test Edit Location Frame");
		f.initComponents(l2);
		
		// check location name
		Assert.assertEquals("name", "Test Loc C", f.locationNameTextField.getText());
		
		Assert.assertEquals("number of sidings", 3, f.sidingModel.getRowCount());
		Assert.assertEquals("number of interchanges", 2, f.interchangeModel.getRowCount());
		Assert.assertEquals("number of yards", 4, f.yardModel.getRowCount());
		Assert.assertEquals("number of staging tracks", 0, f.stagingModel.getRowCount());
		
		f.dispose();
	}
	
	public void testLocationEditFrameReadStaging(){
		LocationManager lManager = LocationManager.instance();
		Location l2 = lManager.getLocationByName("Test Loc A");
		
		LocationEditFrame f = new LocationEditFrame();
		f.setTitle("Test Edit Location Frame Staging");
		f.initComponents(l2);
		
		// check location name
		Assert.assertEquals("name", "Test Loc A", f.locationNameTextField.getText());
		
		Assert.assertEquals("number of sidings", 0, f.sidingModel.getRowCount());
		Assert.assertEquals("number of interchanges", 0, f.interchangeModel.getRowCount());
		Assert.assertEquals("number of yards", 0, f.yardModel.getRowCount());
		Assert.assertEquals("number of staging tracks", 4, f.stagingModel.getRowCount());
		
		// is the staging only button selected?
		Assert.assertTrue("staging selected", f.stageRadioButton.isSelected());
		
		f.dispose();
	}
	

	public void testScheduleEditFrame(){
		LocationManager lManager = LocationManager.instance();
		Location l = lManager.getLocationByName("Test Loc C");
		Assert.assertNotNull("Location exists", l);
		Track t = l.getTrackByName("3rd siding track", null);
		Assert.assertNotNull("Track exists", t);
		ScheduleEditFrame f = new ScheduleEditFrame();
		f.setTitle("Test Schedule Frame");
		f.initComponents(null, l, t);
		f.scheduleNameTextField.setText("Test Schedule A");
		f.commentTextField.setText("Test Comment");
		//f.addScheduleButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addScheduleButton ) );
		
		// was the schedule created?
		ScheduleManager m = ScheduleManager.instance();
		Schedule s = m.getScheduleByName("Test Schedule A");	
		Assert.assertNotNull("Test Schedule A exists", s);
		
		// now add some car types to the schedule
		f.typeBox.setSelectedItem("Boxcar");
		//f.addTypeButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTypeButton ) );
		f.typeBox.setSelectedItem("Flatcar");
		//f.addTypeButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTypeButton ) );
		f.typeBox.setSelectedItem("Coilcar");
		//f.addTypeButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTypeButton ) );
		// put Tank Food at start of list
		f.typeBox.setSelectedItem("Tank Food");
		//f.addLocAtTop.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addLocAtTop ) );
		//f.addTypeButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTypeButton ) );
		//f.saveScheduleButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveScheduleButton ) );
		
		List<String> list = s.getItemsBySequenceList();
		Assert.assertEquals("number of items", 4, list.size());
		
		ScheduleItem si = s.getItemById(list.get(0));		
		Assert.assertEquals("1st type", "Tank Food", si.getType());
		si = s.getItemById(list.get(1));		
		Assert.assertEquals("2nd type", "Boxcar", si.getType());
		si = s.getItemById(list.get(2));		
		Assert.assertEquals("3rd type", "Flatcar", si.getType());
		si = s.getItemById(list.get(3));		
		Assert.assertEquals("3rd type", "Coilcar", si.getType());
		
		//f.deleteScheduleButton.doClick();
		getHelper().enterClickAndLeave( new MouseEventData( this, f.deleteScheduleButton ) );
		// Yes to pop up
		pressDialogButton(f, "Yes");
		s = m.getScheduleByName("Test Schedule A");	
		Assert.assertNull("Test Schedule A exists", s);
		
		f.dispose();
	}
	
	public void testScheduleComboBoxes(){
		LocationManager lm = LocationManager.instance();
		Location l = lm.newLocation("new test location");
		Track t = l.addTrack("track 1", Track.SIDING);
		
		ScheduleManager sm = ScheduleManager.instance();
		
		// clear out any previous schedules
		sm.dispose();
		sm = ScheduleManager.instance();
		
		Schedule s1 = sm.newSchedule("new schedule");
		Schedule s2 = sm.newSchedule("newer schedule");
		ScheduleItem i1 = s1.addItem("BoxCar");
		i1.setRoad("new road");
		i1.setLoad("new load");
		i1.setShip("new ship load");
		ScheduleItem i2 = s1.addItem("Caboose");
		i2.setRoad("road");
		i2.setLoad("load");
		i2.setShip("ship load");
		
		Assert.assertEquals("1 First schedule name", "new schedule", s1.getName());
		Assert.assertEquals("1 First schedule name", "newer schedule", s2.getName());
		
		List<String> names = sm.getSchedulesByNameList();
		Assert.assertEquals("There should be 2 schedules", 2, names.size());
		Schedule sch1 = sm.getScheduleById(names.get(0));
		Schedule sch2 = sm.getScheduleById(names.get(1));
		Assert.assertEquals("2 First schedule name", "new schedule", sch1.getName());
		Assert.assertEquals("2 First schedule name", "newer schedule", sch2.getName());
		Assert.assertEquals("Schedule 1", sch1, sm.getScheduleByName("new schedule"));
		Assert.assertEquals("Schedule 2", sch2, sm.getScheduleByName("newer schedule"));
		
		JComboBox box = sm.getComboBox();
		Assert.assertEquals("3 First schedule name", "", box.getItemAt(0));
		Assert.assertEquals("3 First schedule name", sch1, box.getItemAt(1));
		Assert.assertEquals("3 First schedule name", sch2, box.getItemAt(2));
		
		JComboBox box2 = sm.getSidingsByScheduleComboBox(s1);
		Assert.assertEquals("First siding name", null, box2.getItemAt(0));
		
		// now add a schedule to siding
		t.setScheduleId(sch1.getId());
		
		JComboBox box3 = sm.getSidingsByScheduleComboBox(s1);
		LocationTrackPair ltp = (LocationTrackPair)box3.getItemAt(0);
		
		Assert.assertEquals("Location track pair location", l, ltp.getLocation()); 
		Assert.assertEquals("Location track pair track", t, ltp.getTrack()); 
		
		// remove all schedules
		sm.dispose();
		
		names = sm.getSchedulesByNameList();
		Assert.assertEquals("There should be no schedules", 0, names.size());
		
	}

	
	
	public void testScheduleTableFrame(){
		SchedulesTableFrame f = new SchedulesTableFrame();
		f.setVisible(true);
		f.dispose();
	}
	
	@SuppressWarnings("unchecked")
	private void pressDialogButton(JmriJFrame f, String buttonName){
		//  (with JfcUnit, not pushing this off to another thread)			                                            
		// Locate resulting dialog box
        List<javax.swing.JDialog> dialogList = new DialogFinder(null).findAll(f);
        javax.swing.JDialog d = dialogList.get(0);
        // Find the button
        AbstractButtonFinder finder = new AbstractButtonFinder(buttonName);
        javax.swing.JButton button = ( javax.swing.JButton ) finder.find( d, 0);
        Assert.assertNotNull("button not found", button);   
        // Click button
        getHelper().enterClickAndLeave( new MouseEventData( this, button ) );		
	}
	
	// Ensure minimal setup for log4J
	@Override
    protected void setUp() throws Exception { 
        super.setUp();
		apps.tests.Log4JFixture.setUp();
		
		// set the locale to US English
		Locale.setDefault(Locale.ENGLISH);
		
		// Repoint OperationsSetupXml to JUnitTest subdirectory
		OperationsSetupXml.setOperationsDirectoryName("operations"+File.separator+"JUnitTest");
		// Change file names to ...Test.xml
		OperationsSetupXml.instance().setOperationsFileName("OperationsJUnitTest.xml"); 
		RouteManagerXml.instance().setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
		EngineManagerXml.instance().setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
		LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
		LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
		TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");
	}

	public OperationsLocationsGuiTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsLocationsGuiTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsLocationsGuiTest.class);
		suite.addTest(jmri.jmrit.operations.locations.PoolTrackGuiTest.suite());
		return suite;
	}

	// The minimal setup for log4J
	@Override
    protected void tearDown() throws Exception { 
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
