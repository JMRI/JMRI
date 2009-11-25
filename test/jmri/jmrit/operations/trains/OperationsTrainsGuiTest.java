//OperationsTrainsGuiTest.java

package jmri.jmrit.operations.trains;

import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.util.List;

/**
 * Tests for the Operations Trains GUI class
 *  
 * @author	Dan Boudreau Copyright (C) 2009
 * @version $Revision: 1.8 $
 */
public class OperationsTrainsGuiTest extends TestCase {

	synchronized void releaseThread() {
		try {
			Thread.sleep(20);
			// super.wait(100);
		}
		catch (InterruptedException e) {
			Assert.fail("failed due to InterruptedException");
		}
	}
	
	/**
	 * Adds some cars for the various tests in this suite
	 */
	public void testTrainsAddCars(){
		CarManager cm = CarManager.instance();
		// add caboose to the roster
		Car c = cm.newCar("NH", "687");
		c.setCaboose(true);
		c = cm.newCar("CP", "435");
		c.setCaboose(true);
		
	}
	
	public void testTrainsTableFrame(){
		TrainsTableFrame f = new TrainsTableFrame();
		f.setVisible(true);
		f.setSize(400,200);
		f.setLocation(10,20);
		f.sortByName.doClick();
		f.saveButton.doClick();
		
		// frame location can move just a bit on MacOS
		Point p = f.getLocation();
		
		TrainManager tmanager = TrainManager.instance();
		Assert.assertEquals("sort by 2", TrainsTableFrame.NAME, tmanager.getTrainFrameSortBy());
		Assert.assertEquals("location 1", p, tmanager.getTrainFramePosition());
		Assert.assertEquals("size 1", new Dimension(400,200), tmanager.getTrainFrameSize());
		Assert.assertFalse("Build Messages", tmanager.getBuildMessages());
		Assert.assertFalse("Build Report", tmanager.getBuildReport());
		Assert.assertFalse("Print Review", tmanager.getPrintPreview());
		
		f.sortByTime.doClick();
		f.buildMsgBox.doClick();
		f.buildReportBox.doClick();
		f.saveButton.doClick();
		f.setSize(610,250);
		f.setLocation(20,10);
		
		Assert.assertEquals("sort by 3", TrainsTableFrame.TIME, tmanager.getTrainFrameSortBy());
		Assert.assertTrue("Build Messages 2", tmanager.getBuildMessages());
		Assert.assertTrue("Build Report 2", tmanager.getBuildReport());
		Assert.assertFalse("Print Review 2", tmanager.getPrintPreview());

		// frame location shouldn't have moved yet
		Assert.assertEquals("location 2", p, tmanager.getTrainFramePosition());
		Assert.assertEquals("size 2", new Dimension(400,200), tmanager.getTrainFrameSize());
		
		f.sortById.doClick();
		f.buildMsgBox.doClick();
		f.printPreviewBox.doClick();
		f.saveButton.doClick();
		// frame location can move just a bit on MacOS
		p = f.getLocation();
		Assert.assertEquals("sort by 1", TrainsTableFrame.ID, tmanager.getTrainFrameSortBy());
		Assert.assertEquals("location 3", p, tmanager.getTrainFramePosition());
		Assert.assertEquals("size 3", new Dimension(610,250), tmanager.getTrainFrameSize());
		Assert.assertFalse("Build Messages 3", tmanager.getBuildMessages());
		Assert.assertTrue("Build Report 3", tmanager.getBuildReport());
		Assert.assertTrue("Print Review 3", tmanager.getPrintPreview());

		// create the TrainEditFrame
		f.addButton.doClick();
		
		// create the TrainSwichListEditFrame
		f.printSwitchButton.doClick();
	}
	
	TrainEditFrame trainEditFrame;
	/**
	 * This test relies on OperationsTrainsTest having been run to initialize
	 * the train fields.
	 */
	public void testTrainEditFrame(){
		trainEditFrame = new TrainEditFrame();
		trainEditFrame.setTitle("Test Add Train Frame");
		trainEditFrame.initComponents(null);
		// fill in name and description fields
		trainEditFrame.trainNameTextField.setText("Test Train Name");
		trainEditFrame.trainDescriptionTextField.setText("Test Train Description");
		trainEditFrame.commentTextField.setText("Test Train Comment");
		trainEditFrame.addTrainButton.doClick();
		
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");
		
		// test defaults
		Assert.assertEquals("train name", "Test Train Name", t.getName());
		Assert.assertEquals("train description", "Test Train Description", t.getDescription());
		Assert.assertEquals("train comment", "Test Train Comment", t.getComment());
		Assert.assertEquals("train depart time", "00:00", t.getDepartureTime());
		Assert.assertEquals("train route", null, t.getRoute());
		Assert.assertTrue("train accepts car type Flat", t.acceptsTypeName("Flat"));
		Assert.assertEquals("train roads", Train.ALLROADS, t.getRoadOption());
		Assert.assertEquals("train requirements", Train.NONE, t.getRequirements());
		
		// test departure time fields
		trainEditFrame.hourBox.setSelectedItem("15");
		trainEditFrame.minuteBox.setSelectedItem("45");
		// shouldn't change until Save
		Assert.assertEquals("train comment", "00:00", t.getDepartureTime());
		trainEditFrame.saveTrainButton.doClick();
		Assert.assertEquals("train comment", "15:45", t.getDepartureTime());
		
		// test route field
		trainEditFrame.routeBox.setSelectedIndex(3);	// the 3rd item should be "Test Route C"
		Assert.assertEquals("train route 2", "Test Route C", t.getRoute().getName());
		// test route edit button
		trainEditFrame.editButton.doClick();
		
		// test car types using the clear and set buttons
		trainEditFrame.clearButton.doClick();
		Assert.assertFalse("train accepts car type Flat 1", t.acceptsTypeName("Flat"));
		trainEditFrame.setButton.doClick();
		Assert.assertTrue("train accepts car type Flat 2", t.acceptsTypeName("Flat"));
		
		// test car road options
		trainEditFrame.roadNameExclude.doClick();
		Assert.assertEquals("train car road exclude", Train.EXCLUDEROADS, t.getRoadOption());
		trainEditFrame.roadNameInclude.doClick();
		Assert.assertEquals("train car road include", Train.INCLUDEROADS, t.getRoadOption());
		trainEditFrame.roadNameAll.doClick();
		Assert.assertEquals("train car road all", Train.ALLROADS, t.getRoadOption());
		
		// test engine fields
		Assert.assertEquals("number of engines", "0", t.getNumberEngines());
		Assert.assertEquals("engine model", "", t.getEngineModel());
		Assert.assertEquals("engine road", "", t.getEngineRoad());
		// now change them
		trainEditFrame.numEnginesBox.setSelectedItem("3");
		trainEditFrame.modelEngineBox.setSelectedItem("GP40");
		trainEditFrame.roadEngineBox.setSelectedItem("UP");
		// shouldn't change until Save
		Assert.assertEquals("number of engines 1", "0", t.getNumberEngines());
		Assert.assertEquals("engine model 1", "", t.getEngineModel());
		Assert.assertEquals("engine road 1", "", t.getEngineRoad());
		trainEditFrame.saveTrainButton.doClick();
		Assert.assertEquals("number of engines 2", "3", t.getNumberEngines());
		Assert.assertEquals("engine model 2", "GP40", t.getEngineModel());
		Assert.assertEquals("engine road 2", "UP", t.getEngineRoad());
		
		// test caboose and FRED buttons and fields
		// require a car with FRED
		trainEditFrame.fredRadioButton.doClick();
		// shouldn't change until Save
		Assert.assertEquals("train requirements 1", Train.NONE, t.getRequirements());
		trainEditFrame.saveTrainButton.doClick();
		Assert.assertEquals("train requirements 2", Train.FRED, t.getRequirements());
		trainEditFrame.cabooseRadioButton.doClick();
		trainEditFrame.saveTrainButton.doClick();
		Assert.assertEquals("train requirements 3", Train.CABOOSE, t.getRequirements());
		Assert.assertEquals("caboose road 1", "", t.getCabooseRoad());
		// shouldn't change until Save
		trainEditFrame.roadCabooseBox.setSelectedItem("NH");
		Assert.assertEquals("caboose road 2", "", t.getCabooseRoad());
		trainEditFrame.saveTrainButton.doClick();
		Assert.assertEquals("caboose road 3", "NH", t.getCabooseRoad());
		trainEditFrame.noneRadioButton.doClick();
		trainEditFrame.saveTrainButton.doClick();
		Assert.assertEquals("train requirements 4", Train.NONE, t.getRequirements());

		// test frame size and location
		trainEditFrame.setSize(650,600);
		trainEditFrame.setLocation(25,30);
		trainEditFrame.saveTrainButton.doClick();
		// frame location can move just a bit on MacOS
		Point p = trainEditFrame.getLocation();
		Assert.assertEquals("location 1", p, tmanager.getTrainEditFramePosition());
		Assert.assertEquals("size 1", new Dimension(650,600), tmanager.getTrainEditFrameSize());
		
		// test delete button
		// the delete opens a dialog window to confirm the delete which 
		// in turn stops the thread, so create a new thread to this this.
		Runnable r  = new Runnable() {
			public void run() {
				trainEditFrame.deleteTrainButton.doClick();
			}
		};
		new Thread(r).start();
		
		// need to wait for dialog to appear
		Window windows[] = trainEditFrame.getOwnedWindows();
		while(windows.length == 0){
			Thread.yield();
			windows = trainEditFrame.getOwnedWindows();
		}
		// There is only one dialog window so take the first
		Window w = windows[0];
		// need to wait for dialog window to be in focus
		while(!w.isFocused()){
			Thread.yield();
		}
	
		w.setVisible(false);
		w.dispose();

	}
	
	public void testTrainEditFrameRead(){
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");
		
		// change the train so it doesn't match the add test
		t.setRequirements(Train.CABOOSE);
		t.setCabooseRoad("CP");
		
		TrainEditFrame f = new TrainEditFrame();
		f.setTitle("Test Edit Train Frame");
		f.initComponents(t);
		
		Assert.assertEquals("train name", "Test Train Name", f.trainNameTextField.getText());
		Assert.assertEquals("train description", "Test Train Description", f.trainDescriptionTextField.getText());
		Assert.assertEquals("train comment", "Test Train Comment", f.commentTextField.getText());
		Assert.assertEquals("train depart hour", "15", f.hourBox.getSelectedItem());
		Assert.assertEquals("train depart minute", "45", f.minuteBox.getSelectedItem());
		Assert.assertEquals("train route", t.getRoute(), f.routeBox.getSelectedItem());
		Assert.assertEquals("number of engines", "3", f.numEnginesBox.getSelectedItem());
		Assert.assertEquals("engine model", "GP40", f.modelEngineBox.getSelectedItem());
		Assert.assertEquals("engine road", "UP", f.roadEngineBox.getSelectedItem());
		Assert.assertEquals("caboose road", "CP", f.roadCabooseBox.getSelectedItem());
		// check radio buttons	
		Assert.assertTrue("caboose selected", f.cabooseRadioButton.isSelected());
		Assert.assertFalse("none selected", f.noneRadioButton.isSelected());
		Assert.assertFalse("FRED selected", f.fredRadioButton.isSelected());
		
		// test frame size and location
		//Assert.assertEquals("location 1", new Point(25,30), tmanager.getTrainEditFramePosition());
		Assert.assertEquals("size 1", new Dimension(650,600), tmanager.getTrainEditFrameSize());
	}
	
	public void testTrainModifyFrame(){
		// confirm that train default accepts Boxcars
		TrainManager tmanager = TrainManager.instance();
		Train t = tmanager.getTrainByName("Test Train Name");
		Assert.assertTrue("accepts Boxcar 1", t.acceptsTypeName("Boxcar"));
				
		TrainsByCarTypeFrame f = new TrainsByCarTypeFrame();
		f.initComponents("Boxcar");
		
		// remove Boxcar from trains
		f.clearButton.doClick();
		f.saveButton.doClick();
		Assert.assertFalse("accepts Boxcar 2", t.acceptsTypeName("Boxcar"));

		// now add Boxcar to trains
		f.setButton.doClick();
		f.saveButton.doClick();
		Assert.assertTrue("accepts Boxcar 3", t.acceptsTypeName("Boxcar"));
	}
	
	public void testTrainSwitchListEditFrame(){
		TrainSwitchListEditFrame f = new TrainSwitchListEditFrame();
		f.initComponents();
		
		LocationManager lmanager = LocationManager.instance();
		List<String> locations = lmanager.getLocationsByNameList();
		
		// default switch list will print all locations
		for (int i=0; i<locations.size(); i++){
			Location l = lmanager.getLocationById(locations.get(i));
			Assert.assertTrue("print switchlist 1", l.getSwitchList());
		}
		// now clear all locations
		f.clearButton.doClick();
		f.saveButton.doClick();
		for (int i=0; i<locations.size(); i++){
			Location l = lmanager.getLocationById(locations.get(i));
			Assert.assertFalse("print switchlist 2", l.getSwitchList());
		}
		// now set all locations
		f.setButton.doClick();
		f.saveButton.doClick();
		for (int i=0; i<locations.size(); i++){
			Location l = lmanager.getLocationById(locations.get(i));
			Assert.assertTrue("print switchlist 3", l.getSwitchList());
		}
	}
	
	// Ensure minimal setup for log4J
	@Override
	protected void setUp() {
		apps.tests.Log4JFixture.setUp();
	}

	public OperationsTrainsGuiTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsTrainsGuiTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsTrainsGuiTest.class);
		return suite;
	}

	// The minimal setup for log4J
	@Override
	protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
