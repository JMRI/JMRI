//OperationsCarsGuiTest.java

package jmri.jmrit.operations.rollingstock.cars;

import jmri.jmrit.operations.OperationsFrame;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.TrainManagerXml;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestSuite;
import junit.extensions.jfcunit.finder.*;
import junit.extensions.jfcunit.eventdata.*;

import java.io.File;
import java.util.List;
import java.util.Locale;

/**
 * Tests for the Operations Cars GUI class
 *  
 * @author	Dan Boudreau Copyright (C) 2009
 * @version $Revision$
 */
public class OperationsCarsGuiTest extends jmri.util.SwingTestCase {

	public void testCarsTableFrame() throws Exception {
		// remove previous cars
		CarManager.instance().dispose();
		CarRoads.instance().dispose();

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
		// add locations
		LocationManager lManager = LocationManager.instance();
		Location westford = lManager.newLocation("Westford");
		Track westfordYard = westford.addTrack("Yard", Track.YARD);
		westfordYard.setLength(300);
		Track westfordSiding = westford.addTrack("Siding", Track.SPUR);
		westfordSiding.setLength(300);
		Track westfordAble = westford.addTrack("Able", Track.SPUR);
		westfordAble.setLength(300);
		Location boxford = lManager.newLocation("Boxford");
		Track boxfordYard = boxford.addTrack("Yard", Track.YARD);
		boxfordYard.setLength(300);
		Track boxfordJacobson = boxford.addTrack("Jacobson", Track.SPUR);
		boxfordJacobson.setLength(300);
		Track boxfordHood = boxford.addTrack("Hood", Track.SPUR);
		boxfordHood.setLength(300);
		
		// enable rfid field
		Setup.setRfidEnabled(true);
		
		CarsTableFrame ctf = new CarsTableFrame(true, null, null);	
		// show all cars?
		Assert.assertTrue("show all cars",ctf.showAllCars);
		// table should be empty
		Assert.assertEquals("number of cars 1", "0", ctf.numCars.getText());
		
		CarManager cManager = CarManager.instance();
		// add 5 cars to table
		loadCars();
		
		Car c1 = cManager.getByRoadAndNumber("NH", "1");
		Assert.assertNotNull(c1);

		Assert.assertEquals("c1 location", Track.OKAY, c1.setLocation(westford, westfordYard));
		Assert.assertEquals("c1 destination", Track.OKAY, c1.setDestination(boxford, boxfordJacobson));

		Car c2 = cManager.getByRoadAndNumber("UP", "2");
		
		Car c3 = cManager.getByRoadAndNumber("AA", "3");
		
		Assert.assertEquals("c3 location", Track.OKAY, c3.setLocation(boxford, boxfordHood));
		Assert.assertEquals("c3 destination", Track.OKAY, c3.setDestination(boxford, boxfordYard));
		
		Car c4 = cManager.getByRoadAndNumber("SP", "2");

		Assert.assertEquals("c4 location", Track.OKAY, c4.setLocation(westford, westfordSiding));
		Assert.assertEquals("c4 destination", Track.OKAY, c4.setDestination(boxford, boxfordHood));
		
		Car c5 = cManager.getByRoadAndNumber("NH", "5");

		Assert.assertEquals("c5 location", Track.OKAY, c5.setLocation(westford, westfordAble));
		Assert.assertEquals("c5 destination", Track.OKAY, c5.setDestination(westford, westfordAble));
		
		Assert.assertEquals("number of cars", "5", ctf.numCars.getText());
	
		// default is sort by number
		List<String> cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by number list", c1.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by number list", c4.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by number list", c2.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by number list", c3.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by number list", c5.getId(), cars.get(4));
		
		// now sort by built date
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByBuilt ) );
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by built list", c5.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by built list", c4.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by built list", c2.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by built list", c3.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by built list", c1.getId(), cars.get(4));
		
		// now sort by color
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByColor ) );
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by color list", c4.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by color list", c2.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by color list", c5.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by color list", c1.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by color list", c3.getId(), cars.get(4));

        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByDestination ) );
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by destination list", c2.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by destination list", c4.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by destination list", c1.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by destination list", c3.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by destination list", c5.getId(), cars.get(4));

        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByKernel ) );
		//TODO add kernels
		
		// now sort by load
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByLoad ) );
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by load list", c2.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by load list", c4.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by load list", c1.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by load list", c3.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by load list", c5.getId(), cars.get(4));	
		
		// now sort by location
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByLocation ) );
        cars = ctf.carsModel.getSelectedCarList();
        Assert.assertEquals("1st car in sort by location list", c2.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by location list", c3.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by location list", c5.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by location list", c4.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by location list", c1.getId(), cars.get(4));
	
		// now sort by moves
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByMoves ) );
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by move list", c5.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by move list", c4.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by move list", c3.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by move list", c2.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by move list", c1.getId(), cars.get(4));

		// test sort by number again
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByNumber ) );
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by number list 2", c1.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by number list 2", c4.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by number list 2", c2.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by number list 2", c3.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by number list 2", c5.getId(), cars.get(4));

		// test sort by owner
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByOwner ) );
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by owner list", c4.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by owner list", c3.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by owner list", c2.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by owner list", c5.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by owner list", c1.getId(), cars.get(4));

		// test sort by rfid
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByRfid ) );
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by rfid list", c5.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by rfid list", c2.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by rfid list", c1.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by rfid list", c4.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by rfid list", c3.getId(), cars.get(4));

		// test sort by road
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByRoad ) );
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by road list", c3.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by road list", c1.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by road list", c5.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by road list", c4.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by road list", c2.getId(), cars.get(4));

        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByTrain ) );
		//TODO add trains
		
		// test sort by type
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.sortByType ) );
		cars = ctf.carsModel.getSelectedCarList();
		Assert.assertEquals("1st car in sort by type list", c2.getId(), cars.get(0));
		Assert.assertEquals("2nd car in sort by type list", c1.getId(), cars.get(1));
		Assert.assertEquals("3rd car in sort by type list", c5.getId(), cars.get(2));
		Assert.assertEquals("4th car in sort by type list", c3.getId(), cars.get(3));
		Assert.assertEquals("5th car in sort by type list", c4.getId(), cars.get(4));

		// test find text field
		ctf.findCarTextBox.setText("2");
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.findButton ) );
		// table is sorted by type, cars with number 2 are in the first and last rows
		Assert.assertEquals("find car by number 1st", 0, ctf.carsTable.getSelectedRow());
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.findButton ) );
		Assert.assertEquals("find car by number 2nd", 4, ctf.carsTable.getSelectedRow());

		// create the CarEditFrame
        getHelper().enterClickAndLeave( new MouseEventData( this, ctf.addButton ) );
        
        ctf.dispose();
		
	}

	List<String> tempCars;
	
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
        getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
		
		CarManager cManager = CarManager.instance();
		Car c6 = cManager.getByRoadAndNumber("SP", "6");
		
		Assert.assertNotNull("Car did not create", c6);
		Assert.assertEquals("car type", "Caboose", c6.getTypeName());
		Assert.assertEquals("car length", "38", c6.getLength());
		Assert.assertEquals("car color", "Black", c6.getColor());
		Assert.assertEquals("car load", "L", c6.getLoadName());
		Assert.assertEquals("car built", "1999", c6.getBuilt());
		Assert.assertEquals("car owner", "Owner1", c6.getOwner());
		Assert.assertEquals("car comment", "test car comment field", c6.getComment());
		
		// test type default check boxes
		Assert.assertFalse("not a caboose", c6.isCaboose());
		Assert.assertFalse("no fred", c6.hasFred());
		Assert.assertFalse("not hazardous", c6.isHazardous());
		
        getHelper().enterClickAndLeave( new MouseEventData( this, f.cabooseCheckBox ) );
		Assert.assertFalse("still not a caboose", c6.isCaboose());
        getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
        // Change all car type to caboose dialog window should appear
	    // need to push the "No" button in the dialog window to close
	    pressDialogButton(f, "No");
        
		Assert.assertTrue("now a caboose", c6.isCaboose());
		Assert.assertFalse("not hazardous 2", c6.isHazardous());
		
        getHelper().enterClickAndLeave( new MouseEventData( this, f.fredCheckBox ) );
		Assert.assertTrue("still a caboose", c6.isCaboose());
		Assert.assertFalse("still no fred", c6.hasFred());
        getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
	    // need to push the "No" button in the dialog window to close
	    pressDialogButton(f, "No");
		Assert.assertFalse("no longer a caboose", c6.isCaboose());
		Assert.assertTrue("now has a fred", c6.hasFred());
		Assert.assertFalse("not hazardous 3", c6.isHazardous());
		
        getHelper().enterClickAndLeave( new MouseEventData( this, f.hazardousCheckBox ) );
		Assert.assertFalse("still not hazardous 3", c6.isHazardous());
        getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
	    // need to push the "No" button in the dialog window to close
	    pressDialogButton(f, "No");
		Assert.assertFalse("still no longer a caboose", c6.isCaboose());
		Assert.assertTrue("still has a fred", c6.hasFred());
		Assert.assertTrue("now hazardous", c6.isHazardous());
		
		getHelper().enterClickAndLeave( new MouseEventData( this, f.utilityCheckBox ) );
		Assert.assertFalse("not utility", c6.isUtility());
        getHelper().enterClickAndLeave( new MouseEventData( this, f.saveButton ) );
	    // need to push the "No" button in the dialog window to close
	    pressDialogButton(f, "No");
	    Assert.assertTrue("now utility", c6.isUtility());
		Assert.assertFalse("not a caboose", c6.isCaboose());
		Assert.assertTrue("still has a fred", c6.hasFred());
		Assert.assertTrue("still hazardous", c6.isHazardous());
		
		// should have 6 cars now
		Assert.assertEquals("number of cars", 6, cManager.getNumEntries());
		
		f.dispose();
	}
	
	public void testCarEditFrameRead(){
		loadCars();		// load cars
		CarManager cManager = CarManager.instance();
		Car c1 = cManager.getByRoadAndNumber("NH", "1");

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
        getHelper().enterClickAndLeave( new MouseEventData( this, f.deleteButton ) );
		
		// should have 5 cars now
		Assert.assertEquals("number of cars", 5, cManager.getNumEntries());
		
		f.dispose();
	}
	
	public void testCarAttributeEditFrameColor(){
		CarAttributeEditFrame f = new CarAttributeEditFrame();
		f.initComponents(CarEditFrame.COLOR);		
		f.addTextBox.setText("Pink");
        getHelper().enterClickAndLeave( new MouseEventData( this, f.addButton ) );
		// new color should appear at start of list
		Assert.assertEquals("new color","Pink",f.comboBox.getItemAt(0));
		 
		// test replace
		f.comboBox.setSelectedItem("Pink");
		f.addTextBox.setText("Pinker");
	    // push replace button
	    getHelper().enterClickAndLeave( new MouseEventData( this, f.replaceButton ) );
	    // need to also push the "Yes" button in the dialog window
	    pressDialogButton(f, "Yes");
	    // did the replace work?
	    Assert.assertEquals("replaced Pink with Pinker","Pinker",f.comboBox.getItemAt(0));

		getHelper().enterClickAndLeave( new MouseEventData( this, f.deleteButton ) );
		// black is the first default color
		Assert.assertEquals("old color","Black",f.comboBox.getItemAt(0));
		
		f.dispose();
	}
	
	public void testCarAttributeEditFrameKernel(){
		// remove all kernels
		CarManager cm = CarManager.instance();
		List<String> kList = cm.getKernelNameList();
		for (int i=0; i<kList.size(); i++)
			cm.deleteKernel(kList.get(i));
		// create TwoCars kernel
		cm.newKernel("TwoCars");
		
		CarAttributeEditFrame f = new CarAttributeEditFrame();
		f.initComponents(CarEditFrame.KERNEL);
		// confirm that space and TwoCar kernel exists
		Assert.assertEquals("space 1","",f.comboBox.getItemAt(0));
		Assert.assertEquals("previous kernel 1","TwoCars", f.comboBox.getItemAt(1));
		
		f.addTextBox.setText("TestKernel");
        getHelper().enterClickAndLeave( new MouseEventData( this, f.addButton ) );
		// new kernel should appear at start of list after blank
		Assert.assertEquals("new kernel","TestKernel", f.comboBox.getItemAt(1));
		
		// test replace
		f.comboBox.setSelectedItem("TestKernel");
		f.addTextBox.setText("TestKernel2");
	    // push replace button
	    getHelper().enterClickAndLeave( new MouseEventData( this, f.replaceButton ) );
	    // need to also push the "Yes" button in the dialog window
	    pressDialogButton(f, "Yes");
	    // did the replace work?
	    Assert.assertEquals("replaced TestKernel with TestKernel2","TestKernel2",f.comboBox.getItemAt(1));
		
		// now try and delete
		f.comboBox.setSelectedItem("TestKernel2");
        getHelper().enterClickAndLeave( new MouseEventData( this, f.deleteButton ) );
		// blank is the first default kernel
		Assert.assertEquals("space 2","",f.comboBox.getItemAt(0));
		Assert.assertEquals("previous kernel 2","TwoCars", f.comboBox.getItemAt(1));
		
		f.dispose();
	}
	
	public void testCarAttributeEditFrame2(){
		CarAttributeEditFrame f = new CarAttributeEditFrame();
		f.initComponents(CarEditFrame.LENGTH);
		f.dispose();
		f = new CarAttributeEditFrame();
		f.initComponents(CarEditFrame.OWNER);
		f.dispose();
		f = new CarAttributeEditFrame();
		f.initComponents(CarEditFrame.ROAD);
		f.dispose();
		f = new CarAttributeEditFrame();
		f.initComponents(CarEditFrame.TYPE);
		f.dispose();
	}
	
	public void testCarLoadEditFrame(){
		CarLoadEditFrame f = new CarLoadEditFrame();
		f.initComponents("Boxcar", "");
		f.addTextBox.setText("New Load");
        getHelper().enterClickAndLeave( new MouseEventData( this, f.addButton ) );
		// new load should appear at start of list
		Assert.assertEquals("new load","New Load",f.comboBox.getItemAt(0));
		
		f.dispose();
	}
	
	public void testCarSetFrame(){
		loadCars();		// load cars
		CarSetFrame f = new CarSetFrame();
		f.setTitle("Test Car Set Frame");
		f.initComponents();
		CarManager cManager = CarManager.instance();
		Car c3 = cManager.getByRoadAndNumber("AA", "3");
		f.loadCar(c3);
		
		f.dispose();
	}
	
	private void loadCars() {
		CarManager cManager = CarManager.instance();
		// add 5 cars to table
		Car c1 = cManager.newCar("NH", "1");
		c1.setBuilt("2009");
		c1.setColor("Red");
		c1.setLength("40");
		c1.setLoadName("L");
		c1.setMoves(55);
		c1.setOwner("Owner2");
		c1.setRfid("RFID 3");
		c1.setTypeName("Caboose");
		c1.setWeight("1.4");
		c1.setWeightTons("Tons of Weight");
		c1.setCaboose(true);
		c1.setComment("Test Car NH 1 Comment");
		
		Car c2 = cManager.newCar("UP", "2");
		c2.setBuilt("2004");
		c2.setColor("Blue");
		c2.setLength("50");
		c2.setLoadName("E");
		c2.setMoves(50);
		c2.setOwner("AT");
		c2.setRfid("RFID 2");
		c2.setTypeName("Boxcar");
		
		Car c3 = cManager.newCar("AA", "3");
		c3.setBuilt("2006");
		c3.setColor("White");
		c3.setLength("30");
		c3.setLoadName("LA");
		c3.setMoves(40);
		c3.setOwner("AB");
		c3.setRfid("RFID 5");
		c3.setTypeName("Gon");
		
		Car c4 = cManager.newCar("SP", "2");
		c4.setBuilt("1990");
		c4.setColor("Black");
		c4.setLength("45");
		c4.setLoadName("EA");
		c4.setMoves(30);
		c4.setOwner("AAA");
		c4.setRfid("RFID 4");
		c4.setTypeName("Tank Food");
		
		Car c5 = cManager.newCar("NH", "5");
		c5.setBuilt("1956");
		c5.setColor("Brown");
		c5.setLength("25");
		c5.setLoadName("LL");
		c5.setMoves(25);
		c5.setOwner("DAB");
		c5.setRfid("RFID 1");
		c5.setTypeName("Coil Car");

	}
	
	@SuppressWarnings("unchecked")
	private void pressDialogButton(OperationsFrame f, String buttonName){
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
		CarManagerXml.instance().setOperationsFileName("OperationsJUnitTestCarRoster.xml");
		LocationManagerXml.instance().setOperationsFileName("OperationsJUnitTestLocationRoster.xml");
		TrainManagerXml.instance().setOperationsFileName("OperationsJUnitTestTrainRoster.xml");
		
		CarColors.instance().dispose();	// reset colors

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
    protected void tearDown() throws Exception { 
        apps.tests.Log4JFixture.tearDown();
        super.tearDown();
    }
}
