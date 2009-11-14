//OperationsCarsGuiTest.java

package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsXml;
import jmri.jmrit.operations.trains.TrainManagerXml;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.awt.Window;
import java.io.File;
import java.util.List;

/**
 * Tests for the Operations Cars GUI class
 *  
 * @author	Dan Boudreau Copyright (C) 2009
 * @version $Revision: 1.3 $
 */
public class OperationsCarsGuiTest extends TestCase {

	synchronized void releaseThread() {
		try {
			Thread.sleep(20);
			// super.wait(100);
		}
		catch (InterruptedException e) {
			Assert.fail("failed due to InterruptedException");
		}
	}
	
	public void testCarsTableFrame(){
		// remove previous cars
		CarManager.instance().dispose();
		// add Owner1 and Owner2
		CarOwners co = CarOwners.instance();
		co.addName("Owner1");
		co.addName("Owner2");
		// add road names
		CarRoads cr = CarRoads.instance();
		cr.addName("NH");
		cr.addName("UP");
		cr.addName("AA");
		cr.addName("SP");
		
		CarsTableFrame ctf = new CarsTableFrame(true, null, null);	
		// show all cars?
		Assert.assertTrue("show all cars",ctf.showAllCars);
		// table should be empty
		Assert.assertEquals("number of cars 1", "0", ctf.numCars.getText());
		
		CarManager cManager = CarManager.instance();
		// add 5 cars to table
		Car c1 = cManager.newCar("NH", "1");
		c1.setBuilt("2009");
		c1.setColor("Red");
		c1.setLength("40");
		c1.setLoad("L");
		c1.setMoves(55);
		c1.setOwner("Owner2");
		c1.setRfid("RFID 3");
		c1.setType("Caboose");
		c1.setWeight("1.4");
		c1.setWeightTons("Tons of Weight");
		c1.setCaboose(true);
		c1.setComment("Test Car NH 1 Comment");

		Car c2 = cManager.newCar("UP", "2");
		c2.setBuilt("2004");
		c2.setColor("Blue");
		c2.setLength("50");
		c2.setLoad("E");
		c2.setMoves(50);
		c2.setOwner("AT");
		c2.setRfid("RFID 2");
		c2.setType("Boxcar");
		
		Car c3 = cManager.newCar("AA", "3");
		c3.setBuilt("2006");
		c3.setColor("White");
		c3.setLoad("LA");
		c3.setMoves(40);
		c3.setOwner("AB");
		c3.setRfid("RFID 5");
		c3.setType("Gon");
		
		Car c4 = cManager.newCar("SP", "2");
		c4.setBuilt("1990");
		c4.setColor("Black");
		c4.setLoad("EA");
		c4.setMoves(30);
		c4.setOwner("AAA");
		c4.setRfid("RFID 4");
		c4.setType("Tankcar");
		
		Car c5 = cManager.newCar("NH", "5");
		c5.setBuilt("1956");
		c5.setColor("Brown");
		c5.setLoad("LL");
		c5.setMoves(25);
		c5.setOwner("DAB");
		c5.setRfid("RFID 1");
		c5.setType("Coilcar");
		
		Assert.assertEquals("number of cars", "5", ctf.numCars.getText());
	
		// default is sort by number
		List<String> cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by number list", c1.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by number list", c4.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by number list", c2.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by number list", c3.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by number list", c5.getId(), cars.get(4));
		
		// now sort by built date
		ctf.sortByBuilt.doClick();
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by built list", c5.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by built list", c4.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by built list", c2.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by built list", c3.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by built list", c1.getId(), cars.get(4));
		
		// now sort by color
		ctf.sortByColor.doClick();
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by color list", c4.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by color list", c2.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by color list", c5.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by color list", c1.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by color list", c3.getId(), cars.get(4));

		ctf.sortByDestination.doClick();
		//TODO add destinations
		ctf.sortByKernel.doClick();
		//TODO add kernels
		
		// now sort by load
		ctf.sortByLoad.doClick();
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by load list", c2.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by load list", c4.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by load list", c1.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by load list", c3.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by load list", c5.getId(), cars.get(4));	
		
		ctf.sortByLocation.doClick();
		//TODO add locations
		
		// now sort by moves
		ctf.sortByMoves.doClick();
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by move list", c5.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by move list", c4.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by move list", c3.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by move list", c2.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by move list", c1.getId(), cars.get(4));

		// test sort by number again
		ctf.sortByNumber.doClick();
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by number list 2", c1.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by number list 2", c4.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by number list 2", c2.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by number list 2", c3.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by number list 2", c5.getId(), cars.get(4));

		// test sort by owner
		ctf.sortByOwner.doClick();
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by owner list", c4.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by owner list", c3.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by owner list", c2.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by owner list", c5.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by owner list", c1.getId(), cars.get(4));

		// test sort by rfid
		ctf.sortByRfid.doClick();
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by rfid list", c5.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by rfid list", c2.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by rfid list", c1.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by rfid list", c4.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by rfid list", c3.getId(), cars.get(4));

		// test sort by road
		ctf.sortByRoad.doClick();
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by road list", c3.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by road list", c1.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by road list", c5.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by road list", c4.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by road list", c2.getId(), cars.get(4));

		ctf.sortByTrain.doClick();
		//TODO add trains
		
		// test sort by type
		ctf.sortByType.doClick();
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by type list", c2.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by type list", c1.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by type list", c5.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by type list", c3.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by type list", c4.getId(), cars.get(4));

		// test find text field
		ctf.findCarTextBox.setText("2");
		ctf.findButton.doClick();
		// table is sorted by type, cars with number 2 are in the first and last rows
		Assert.assertEquals("find car by number 1st", 0, ctf.carsTable.getSelectedRow());
		ctf.findButton.doClick();
		Assert.assertEquals("find car by number 2nd", 4, ctf.carsTable.getSelectedRow());

		// create the CarEditFrame
		ctf.addButton.doClick();
		
	}

	public void testCarEditFrame(){	
		CarEditFrame f = new CarEditFrame();
		f.setTitle("Test Add Car Frame");
		f.initComponents();
		
		// add a new car
		f.roadComboBox.setSelectedItem("SP");
		f.roadNumberTextField.setText("6");
		f.typeComboBox.setSelectedItem("Caboose");
		f.lengthComboBox.setSelectedItem("38");
		f.colorComboBox.setSelectedItem("Black");
		f.loadComboBox.setSelectedItem("L");
		f.builtTextField.setText("1999");
		f.ownerComboBox.setSelectedItem("Owner1");
		f.commentTextField.setText("test car comment field");
		f.saveButton.doClick();
		
		CarManager cManager = CarManager.instance();
		Car c6 = cManager.getCarByRoadAndNumber("SP", "6");
		
		Assert.assertNotNull("Car did not create", c6);
		Assert.assertEquals("car type", "Caboose", c6.getType());
		Assert.assertEquals("car length", "38", c6.getLength());
		Assert.assertEquals("car color", "Black", c6.getColor());
		Assert.assertEquals("car load", "L", c6.getLoad());
		Assert.assertEquals("car built", "1999", c6.getBuilt());
		Assert.assertEquals("car owner", "Owner1", c6.getOwner());
		Assert.assertEquals("car comment", "test car comment field", c6.getComment());
		
		// test type default check boxes
		Assert.assertFalse("not a caboose", c6.isCaboose());
		Assert.assertFalse("no fred", c6.hasFred());
		Assert.assertFalse("not hazardous", c6.isHazardous());
		
		f.cabooseCheckBox.doClick();
		Assert.assertFalse("still not a caboose", c6.isCaboose());
		f.saveButton.doClick();
		Assert.assertTrue("now a caboose", c6.isCaboose());
		Assert.assertFalse("not hazardous 2", c6.isHazardous());
		
		f.fredCheckBox.doClick();
		Assert.assertTrue("still a caboose", c6.isCaboose());
		Assert.assertFalse("still no fred", c6.hasFred());
		f.saveButton.doClick();
		Assert.assertFalse("no longer a caboose", c6.isCaboose());
		Assert.assertTrue("now has a fred", c6.hasFred());
		Assert.assertFalse("not hazardous 3", c6.isHazardous());
		
		f.hazardousCheckBox.doClick();
		Assert.assertFalse("still not hazardous 3", c6.isHazardous());
		f.saveButton.doClick();
		Assert.assertFalse("still no longer a caboose", c6.isCaboose());
		Assert.assertTrue("still has a fred", c6.hasFred());
		Assert.assertTrue("now hazardous", c6.isHazardous());
		
		// should have 6 cars now
		Assert.assertEquals("number of cars", 6, cManager.getNumEntries());
	}
	
	public void testCarEditFrameRead(){
		CarManager cManager = CarManager.instance();
		Car c1 = cManager.getCarByRoadAndNumber("NH", "1");
		CarEditFrame f = new CarEditFrame();
		f.initComponents();
		f.setTitle("Test Edit Car Frame");
		f.loadCar(c1);
		
		Assert.assertEquals("car road", "NH", f.roadComboBox.getSelectedItem());
		Assert.assertEquals("car number", "1", f.roadNumberTextField.getText());
		Assert.assertEquals("car type", "Caboose", f.typeComboBox.getSelectedItem());
		Assert.assertEquals("car length", "40", f.lengthComboBox.getSelectedItem());
		Assert.assertEquals("car weight", "1.4", f.weightTextField.getText());
		Assert.assertEquals("car weight tons", "Tons of Weight", f.weightTonsTextField.getText());
		Assert.assertEquals("car color", "Red", f.colorComboBox.getSelectedItem());
		Assert.assertEquals("car load", "L", f.loadComboBox.getSelectedItem());
		Assert.assertEquals("car built", "2009", f.builtTextField.getText());
		Assert.assertEquals("car owner", "Owner2", f.ownerComboBox.getSelectedItem());
		Assert.assertEquals("car comment", "Test Car NH 1 Comment", f.commentTextField.getText());
		
		Assert.assertTrue("car is a caboose", f.cabooseCheckBox.isSelected());
		Assert.assertFalse("car does not have a fred", f.fredCheckBox.isSelected());
		Assert.assertFalse("car is not hazardous", f.hazardousCheckBox.isSelected());
		
		// test delete button
		f.deleteButton.doClick();
		
		// should have 5 cars now
		Assert.assertEquals("number of cars", 5, cManager.getNumEntries());
	}
	
	public void testCarAttributeEditFrameColor(){
		CarAttributeEditFrame f = new CarAttributeEditFrame();
		f.initComponents(CarEditFrame.COLOR);		
		f.addTextBox.setText("Pink");
		f.addButton.doClick();
		// new color should appear at start of list
		Assert.assertEquals("new color","Pink",f.comboBox.getItemAt(0));
		
		f.deleteButton.doClick();
		// red is the first default color
		Assert.assertEquals("old color","Red",f.comboBox.getItemAt(0));
		
		testReplace(f);	
	}
	
	public void testCarAttributeEditFrameKernel(){
		// create TwoCars kernel
		CarManager cm = CarManager.instance();
		cm.newKernel("TwoCars");
		
		CarAttributeEditFrame f = new CarAttributeEditFrame();
		f.initComponents(CarEditFrame.KERNEL);
		// confirm that space and TwoCar kernel exists
		Assert.assertEquals("space 1","",f.comboBox.getItemAt(0));
		Assert.assertEquals("previous kernel 1","TwoCars", f.comboBox.getItemAt(1));
		
		f.addTextBox.setText("TestKernel");
		f.addButton.doClick();
		// new kernel should appear at start of list after blank
		Assert.assertEquals("new kernel","TestKernel", f.comboBox.getItemAt(1));
		
		// test replace
		f.comboBox.setSelectedItem("TestKernel");
		f.addTextBox.setText("TestKernel2");
		testReplace(f);	
		
		// now try and delete
		f.comboBox.setSelectedItem("TestKernel");
		f.deleteButton.doClick();
		// blank is the first default kernel
		Assert.assertEquals("space 2","",f.comboBox.getItemAt(0));
		Assert.assertEquals("previous kernel 2","TwoCars", f.comboBox.getItemAt(1));	
	}
	
	public void testCarAttributeEditFrame2(){
		CarAttributeEditFrame f = new CarAttributeEditFrame();
		f.initComponents(CarEditFrame.LENGTH);
		f = new CarAttributeEditFrame();
		f.initComponents(CarEditFrame.OWNER);
		f = new CarAttributeEditFrame();
		f.initComponents(CarEditFrame.ROAD);
		f = new CarAttributeEditFrame();
		f.initComponents(CarEditFrame.TYPE);
	}
	
	public void testCarLoadEditFrame(){
		CarLoadEditFrame f = new CarLoadEditFrame();
		f.initComponents("Boxcar");
		f.addTextBox.setText("New Load");
		f.addButton.doClick();
		// new load should appear at start of list
		Assert.assertEquals("new color","New Load",f.comboBox.getItemAt(0));
	}
	
	public void testCarSetFrame(){
		CarSetFrame f = new CarSetFrame();
		f.setTitle("Test Car Set Frame");
		f.initComponents();
		CarManager cManager = CarManager.instance();
		Car c3 = cManager.getCarByRoadAndNumber("AA", "3");
		f.loadCar(c3);
	}
	
	CarAttributeEditFrame caef;
	//TODO this test only closes the confirmation window, need to figure out
	// a way to press the okay button
	private void testReplace(CarAttributeEditFrame f){
		caef = f;
		// test replace button
		// the replace opens a dialog window to confirm the replace which 
		// in turn stops the thread, so create a new thread to this this.
		Runnable r  = new Runnable() {
			public void run() {
				caef.replaceButton.doClick();
			}
		};
		
		Thread t = new Thread(r);
		t.start();
		
		// need to wait for dialog to appear
		Window windows[] = f.getOwnedWindows();
		while(windows.length == 0){
			Thread.yield();
			windows = f.getOwnedWindows();
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
	
	// Ensure minimal setup for log4J
	@Override
	protected void setUp() {
		apps.tests.Log4JFixture.setUp();
		
		// Repoint OperationsXml to JUnitTest subdirectory
		OperationsXml.setOperationsDirectoryName("operations"+File.separator+"JUnitTest");
		// Change file names to ...Test.xml
		OperationsXml.setOperationsFileName("OperationsJUnitTest.xml"); 
		RouteManagerXml.setOperationsFileName("OperationsJUnitTestRouteRoster.xml");
		EngineManagerXml.setOperationsFileName("OperationsJUnitTestEngineRoster.xml");
		CarManagerXml.setOperationsFileName("OperationsJUnitTestCarRoster.xml");
		LocationManagerXml.setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
		TrainManagerXml.setOperationsFileName("OperationsJUnitTestTrainRoster.xml");

	}

	public OperationsCarsGuiTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {"-noloading", OperationsCarsGuiTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(OperationsCarsGuiTest.class);
		return suite;
	}

	// The minimal setup for log4J
	@Override
	protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
}
