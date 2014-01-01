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
	
		LocationsTableFrame f = new LocationsTableFrame();
		
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
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addYardButton ) );
		
		// add an interchange track
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addInterchangeButton ) );
		
		// add a staging track
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addStagingButton ) );
		
		// add a yard track
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addYardButton ) );
		
		f.locationNameTextField.setText("Newer Test Location");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveLocationButton ) );
		
		Assert.assertEquals("changed location name", "Newer Test Location", newLoc.getName());
		
		// test delete button
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
		Location l3 = lManager.newLocation("Test Loc C");
		l3.setLength(1003);

		Location l = lManager.getLocationByName("Test Loc C");
		Assert.assertNotNull("Test Loc C", l);
		InterchangeEditFrame f = new InterchangeEditFrame();
		f.setTitle("Test Interchange Add Frame");
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents(l, null);
		
		// create two interchange tracks
		f.trackNameTextField.setText("new interchange track");
		f.trackLengthTextField.setText("321");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("2nd interchange track");
		f.trackLengthTextField.setText("4331");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		// deselect east and south check boxes
		getHelper().enterClickAndLeave( new MouseEventData( this, f.eastCheckBox ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.southCheckBox ) );
				
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrackButton ) );
		
		Track t = l.getTrackByName("new interchange track", Track.INTERCHANGE);	
		Assert.assertNotNull("new interchange track", t);
		Assert.assertEquals("interchange track length", 321, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());
				
		t = l.getTrackByName("2nd interchange track", Track.INTERCHANGE);	
		Assert.assertNotNull("2nd interchange track", t);
		Assert.assertEquals("2nd interchange track length", 4331, t.getLength());
		Assert.assertEquals("west and north", Track.NORTH+Track.WEST, t.getTrainDirections());
		
		// check track accepts Boxcars
		Assert.assertTrue("2nd interchange track accepts Boxcars", t.acceptsTypeName("Boxcar"));
		// test clear car types button
		getHelper().enterClickAndLeave( new MouseEventData( this, f.clearButton ) );	
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrackButton ) );
		Assert.assertFalse("2nd interchange track doesn't accept Boxcars", t.acceptsTypeName("Boxcar"));
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.setButton ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrackButton ) );
		Assert.assertTrue("2nd interchange track accepts Boxcars again", t.acceptsTypeName("Boxcar"));	
		
		f.dispose();
		
		// now reload
		
		Location l2 = lManager.getLocationByName("Test Loc C");		
		Assert.assertNotNull("Location Test Loc C", l2);
		
		LocationEditFrame fl = new LocationEditFrame();
		fl.setTitle("Test Edit Location Frame");
		fl.initComponents(l2);
		
		// check location name
		Assert.assertEquals("name", "Test Loc C", fl.locationNameTextField.getText());
		
		Assert.assertEquals("number of interchanges", 2, fl.interchangeModel.getRowCount());
		Assert.assertEquals("number of staging tracks", 0, fl.stagingModel.getRowCount());
		
		fl.dispose();
	}
	
	public void testSidingEditFrame(){		
		LocationManager lManager = LocationManager.instance();
		Location l = lManager.getLocationByName("Test Loc C");
		SpurEditFrame f = new SpurEditFrame();
		f.setTitle("Test Siding Add Frame");
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents(l, null);
		
		// create three siding tracks
		f.trackNameTextField.setText("new siding track");
		f.trackLengthTextField.setText("1223");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("2nd siding track");
		f.trackLengthTextField.setText("9999");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("3rd siding track");
		f.trackLengthTextField.setText("1010");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		// deselect east, west and north check boxes
		getHelper().enterClickAndLeave( new MouseEventData( this, f.eastCheckBox ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.westCheckBox ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.northCheckBox ) );
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrackButton ) );
		
		Track t = l.getTrackByName("new siding track", null);	
		Assert.assertNotNull("new siding track", t);
		Assert.assertEquals("siding track length", 1223, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());
				
		t = l.getTrackByName("2nd siding track", null);	
		Assert.assertNotNull("2nd siding track", t);
		Assert.assertEquals("2nd siding track length", 9999, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());
		
		t = l.getTrackByName("3rd siding track", null);	
		Assert.assertNotNull("3rd siding track", t);
		Assert.assertEquals("3rd siding track length", 1010, t.getLength());
		
		Assert.assertEquals("only south", Track.SOUTH, t.getTrainDirections());
		
		// create the schedule edit frame
		getHelper().enterClickAndLeave( new MouseEventData( this, f.editScheduleButton ) );
		
        // confirm schedule add frame creation
        JmriJFrame sef = JmriJFrame.getFrame("Add Schedule for Spur 3rd siding track");
        Assert.assertNotNull(sef);
		
        // kill all frames
		f.dispose();
		sef.dispose();
		
		// now reload
		
		Location l2 = lManager.getLocationByName("Test Loc C");		
		Assert.assertNotNull("Location Test Loc C", l2);
		
		LocationEditFrame fl = new LocationEditFrame();
		fl.setTitle("Test Edit Location Frame");
		fl.initComponents(l2);
		
		// check location name
		Assert.assertEquals("name", "Test Loc C", fl.locationNameTextField.getText());
		
		Assert.assertEquals("number of sidings", 3, fl.spurModel.getRowCount());
		Assert.assertEquals("number of staging tracks", 0, fl.stagingModel.getRowCount());
		
		fl.dispose();
	}
	
	/**
	 * Staging tracks needs its own location
	 */
	public void testStagingEditFrame(){		
		LocationManager lManager = LocationManager.instance();
		Location l = lManager.getLocationByName("Test Loc A");
		Assert.assertNotNull("Test Loc A", l);
		StagingEditFrame f = new StagingEditFrame();
		f.setTitle("Test Staging Add Frame");
		f.setLocation(0, 0);	// entire panel must be visible for tests to work properly
		f.initComponents(l, null);
		
		// create four staging tracks
		f.trackNameTextField.setText("new staging track");
		f.trackLengthTextField.setText("34");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("2nd staging track");
		f.trackLengthTextField.setText("3456");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("3rd staging track");
		f.trackLengthTextField.setText("1");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("4th staging track");
		f.trackLengthTextField.setText("12");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		// deselect east, west and south check boxes
		getHelper().enterClickAndLeave( new MouseEventData( this, f.northCheckBox ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.westCheckBox ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.southCheckBox ) );
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrackButton ) );
		
		sleep(1);	// for slow machines
		Track t = l.getTrackByName("new staging track", null);	
		Assert.assertNotNull("new staging track", t);
		Assert.assertEquals("staging track length", 34, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());
				
		t = l.getTrackByName("2nd staging track", null);	
		Assert.assertNotNull("2nd staging track", t);
		Assert.assertEquals("2nd staging track length", 3456, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());
		
		t = l.getTrackByName("3rd staging track", null);	
		Assert.assertNotNull("3rd staging track", t);
		Assert.assertEquals("3rd staging track length", 1, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

		t = l.getTrackByName("4th staging track", null);	
		Assert.assertNotNull("4th staging track", t);
		Assert.assertEquals("4th staging track length", 12, t.getLength());		
		Assert.assertEquals("only east", Track.EAST, t.getTrainDirections());	
		
		f.dispose();
		
		Location l2 = lManager.getLocationByName("Test Loc A");
		Assert.assertNotNull("Test Loc A",l2);
		
		LocationEditFrame fl = new LocationEditFrame();
		fl.setTitle("Test Edit Location Frame Staging");
		fl.initComponents(l2);
		
		// check location name
		Assert.assertEquals("name", "Test Loc A", fl.locationNameTextField.getText());
		
		Assert.assertEquals("number of sidings", 0, fl.spurModel.getRowCount());
		Assert.assertEquals("number of interchanges", 0, fl.interchangeModel.getRowCount());
		Assert.assertEquals("number of yards", 0, fl.yardModel.getRowCount());
		Assert.assertEquals("number of staging tracks", 4, fl.stagingModel.getRowCount());
		
		// is the staging only button selected?
		Assert.assertTrue("staging selected", fl.stageRadioButton.isSelected());
		
		fl.dispose();
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
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("2nd yard track");
		f.trackLengthTextField.setText("6543");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("3rd yard track");
		f.trackLengthTextField.setText("1");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		f.trackNameTextField.setText("4th yard track");
		f.trackLengthTextField.setText("21");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTrackButton ) );
		
		// deselect east, west and south check boxes
		getHelper().enterClickAndLeave( new MouseEventData( this, f.eastCheckBox ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.westCheckBox ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.southCheckBox ) );
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveTrackButton ) );
		
		Track t = l.getTrackByName("new yard track", null);	
		Assert.assertNotNull("new yard track", t);
		Assert.assertEquals("yard track length", 43, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());
				
		t = l.getTrackByName("2nd yard track", null);	
		Assert.assertNotNull("2nd yard track", t);
		Assert.assertEquals("2nd yard track length", 6543, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());
		
		t = l.getTrackByName("3rd yard track", null);	
		Assert.assertNotNull("3rd yard track", t);
		Assert.assertEquals("3rd yard track length", 1, t.getLength());
		// check that the defaults are correct
		Assert.assertEquals("all directions", ALL, t.getTrainDirections());
		Assert.assertEquals("all roads", Track.ALL_ROADS, t.getRoadOption());

		t = l.getTrackByName("4th yard track", null);	
		Assert.assertNotNull("4th yard track", t);
		Assert.assertEquals("4th yard track length", 21, t.getLength());		
		Assert.assertEquals("only north", Track.NORTH, t.getTrainDirections());
		
		f.dispose();
		
		// now reload
		
		Location l2 = lManager.getLocationByName("Test Loc C");		
		Assert.assertNotNull("Location Test Loc C", l2);
		
		LocationEditFrame fl = new LocationEditFrame();
		fl.setTitle("Test Edit Location Frame");
		fl.initComponents(l2);
		
		// check location name
		Assert.assertEquals("name", "Test Loc C", fl.locationNameTextField.getText());
		
		Assert.assertEquals("number of yards", 4, fl.yardModel.getRowCount());
		Assert.assertEquals("number of staging tracks", 0, fl.stagingModel.getRowCount());
		
		fl.dispose();
	}

	public void testScheduleEditFrame(){
		LocationManager lManager = LocationManager.instance();
		Location l2 = lManager.newLocation("Test Loc C");
		l2.setLength(1003);

		Location l = lManager.getLocationByName("Test Loc C");
		Assert.assertNotNull("Location exists", l);
		Track t = l.addTrack("3rd siding track", Track.SPUR);
		Assert.assertNotNull("Track exists", t);
		ScheduleEditFrame f = new ScheduleEditFrame();
		f.setTitle("Test Schedule Frame");
		f.initComponents(null, l, t);
		f.scheduleNameTextField.setText("Test Schedule A");
		f.commentTextField.setText("Test Comment");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addScheduleButton ) );
		
		// was the schedule created?
		ScheduleManager m = ScheduleManager.instance();
		Schedule s = m.getScheduleByName("Test Schedule A");	
		Assert.assertNotNull("Test Schedule A exists", s);
		
		// now add some car types to the schedule
		f.typeBox.setSelectedItem("Boxcar");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTypeButton ) );
		f.typeBox.setSelectedItem("Flatcar");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTypeButton ) );
		f.typeBox.setSelectedItem("Coilcar");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTypeButton ) );
		// put Tank Food at start of list
		f.typeBox.setSelectedItem("Tank Food");
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addLocAtTop ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.addTypeButton ) );
		getHelper().enterClickAndLeave( new MouseEventData( this, f.saveScheduleButton ) );
		
		List<String> list = s.getItemsBySequenceList();
		Assert.assertEquals("number of items", 4, list.size());
		
		ScheduleItem si = s.getItemById(list.get(0));		
		Assert.assertEquals("1st type", "Tank Food", si.getTypeName());
		si = s.getItemById(list.get(1));		
		Assert.assertEquals("2nd type", "Boxcar", si.getTypeName());
		si = s.getItemById(list.get(2));		
		Assert.assertEquals("3rd type", "Flatcar", si.getTypeName());
		si = s.getItemById(list.get(3));		
		Assert.assertEquals("3rd type", "Coilcar", si.getTypeName());
		
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
		Track t = l.addTrack("track 1", Track.SPUR);
		
		ScheduleManager sm = ScheduleManager.instance();
		
		// clear out any previous schedules
		sm.dispose();
		sm = ScheduleManager.instance();
		
		Schedule s1 = sm.newSchedule("new schedule");
		Schedule s2 = sm.newSchedule("newer schedule");
		ScheduleItem i1 = s1.addItem("BoxCar");
		i1.setRoadName("new road");
		i1.setReceiveLoadName("new load");
		i1.setShipLoadName("new ship load");
		ScheduleItem i2 = s1.addItem("Caboose");
		i2.setRoadName("road");
		i2.setReceiveLoadName("load");
		i2.setShipLoadName("ship load");
		
		Assert.assertEquals("1 First schedule name", "new schedule", s1.getName());
		Assert.assertEquals("1 First schedule name", "newer schedule", s2.getName());
		
		List<Schedule> names = sm.getSchedulesByNameList();
		Assert.assertEquals("There should be 2 schedules", 2, names.size());
		Schedule sch1 = names.get(0);
		Schedule sch2 = names.get(1);
		Assert.assertEquals("2 First schedule name", "new schedule", sch1.getName());
		Assert.assertEquals("2 First schedule name", "newer schedule", sch2.getName());
		Assert.assertEquals("Schedule 1", sch1, sm.getScheduleByName("new schedule"));
		Assert.assertEquals("Schedule 2", sch2, sm.getScheduleByName("newer schedule"));
		
		JComboBox box = sm.getComboBox();
		Assert.assertEquals("3 First schedule name", "", box.getItemAt(0));
		Assert.assertEquals("3 First schedule name", sch1, box.getItemAt(1));
		Assert.assertEquals("3 First schedule name", sch2, box.getItemAt(2));
		
		JComboBox box2 = sm.getSpursByScheduleComboBox(s1);
		Assert.assertEquals("First siding name", null, box2.getItemAt(0));
		
		// now add a schedule to siding
		t.setScheduleId(sch1.getId());
		
		JComboBox box3 = sm.getSpursByScheduleComboBox(s1);
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
	
	private void loadLocations() {
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
		
		// clear out previous locations
		LocationManager.instance().dispose();
		
		loadLocations();
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
