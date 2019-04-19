package jmri.jmrit.operations.rollingstock.cars;

import java.awt.GraphicsEnvironment;
import java.text.MessageFormat;
import jmri.InstanceManager;
import jmri.jmrit.operations.OperationsTestCase;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.locations.Track;
import jmri.jmrit.operations.rollingstock.cars.tools.CarAttributeEditFrame;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.Setup;
import jmri.util.JUnitOperationsUtil;
import jmri.util.JUnitUtil;
import jmri.util.swing.JemmyUtil;
import jmri.util.junit.rules.*;
import org.junit.*;
import org.junit.rules.*;

/**
 * Tests for the Operations Cars GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
public class CarEditFrameTest extends OperationsTestCase {
    
    @Rule
    public Timeout globalTimeout = Timeout.seconds(10); // 10 second timeout for methods in this test class.

    @Rule
    public RetryRule retryRule = new RetryRule(2); // allow 2 retries
    
    @Test
    public void testClearRoadNumber() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());

        f.roadNumberTextField.setText("123");
        JemmyUtil.enterClickAndLeave(f.clearRoadNumberButton);
        Assert.assertEquals("road number", "", f.roadNumberTextField.getText());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testRoadNumberErrorConditions() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());

        // this will load the weight fields
        f.lengthComboBox.setSelectedIndex(4);

        // "*" is not a legal character for road number
        f.roadNumberTextField.setText("6*6");

        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("roadNumNG"), Bundle.getMessage("ButtonOK"));

        // test number too long
        StringBuffer sb = new StringBuffer("A");
        for (int i = 0; i < Control.max_len_string_road_number; i++) {
            sb.append(i);
        }

        f.roadNumberTextField.setText(sb.toString());

        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("RoadNumTooLong"), Bundle.getMessage("ButtonOK"));

        // confirm that delete and save buttons are disabled
        Assert.assertFalse(f.saveButton.isEnabled());
        Assert.assertFalse(f.deleteButton.isEnabled());

        // enter a good road number
        f.roadNumberTextField.setText("123");

        JemmyUtil.enterClickAndLeave(f.addButton);
        // confirm that delete and save buttons are enabled
        Assert.assertTrue(f.saveButton.isEnabled());
        Assert.assertTrue(f.deleteButton.isEnabled());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testWeightErrorConditions() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());

        // enter a good road number
        f.roadNumberTextField.setText("123456");

        // new dialog warning car weight
        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("carActualWeight"), Bundle.getMessage("ButtonOK"));

        f.weightTextField.setText("1.5"); // good oz weight      
        f.weightTonsTextField.setText("Bogus Weight");
        // new dialog warning car weight
        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("WeightTonError"), Bundle.getMessage("ButtonOK"));

        // this will load the weight fields
        f.lengthComboBox.setSelectedIndex(4);

        // confirm that delete and save buttons are NOT enabled
        Assert.assertFalse(f.saveButton.isEnabled());
        Assert.assertFalse(f.deleteButton.isEnabled());

        JemmyUtil.enterClickAndLeave(f.addButton);

        // confirm that delete and save buttons are enabled
        Assert.assertTrue(f.saveButton.isEnabled());
        Assert.assertTrue(f.deleteButton.isEnabled());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditRoadButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editRoadButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.ROAD, f.carAttributeEditFrame._comboboxName);

        // now change to car type
        JemmyUtil.enterClickAndLeave(f.editTypeButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.TYPE, f.carAttributeEditFrame._comboboxName);

        JUnitUtil.dispose(f.carAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditTypeButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editTypeButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.TYPE, f.carAttributeEditFrame._comboboxName);

        JUnitUtil.dispose(f.carAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditColorButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editColorButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.COLOR, f.carAttributeEditFrame._comboboxName);

        JUnitUtil.dispose(f.carAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditLengthButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editLengthButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.LENGTH, f.carAttributeEditFrame._comboboxName);

        JUnitUtil.dispose(f.carAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditOwnerButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editOwnerButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.OWNER, f.carAttributeEditFrame._comboboxName);

        JUnitUtil.dispose(f.carAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditGroupButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editGroupButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.KERNEL, f.carAttributeEditFrame._comboboxName);

        JUnitUtil.dispose(f.carAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testEditLoadButton() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editLoadButton);
        Assert.assertTrue(f.carLoadEditFrame.isShowing());

        JUnitUtil.dispose(f.carLoadEditFrame);
        JUnitUtil.dispose(f);
    }

    @Test
    public void testLocationComboBox() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        LocationManager lm = InstanceManager.getDefault(LocationManager.class);
        Location loc1 = lm.getLocationByName("North End Staging");
        Location loc2 = lm.getLocationByName("North Industries");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());

        f.roadNumberTextField.setText("10345");
        f.roadComboBox.setSelectedItem("SP");

        CarManager cm = InstanceManager.getDefault(CarManager.class);
        Car car = cm.getByRoadAndNumber("SP", "10345");
        Assert.assertNull("car exists", car);

        // this will load the weight fields
        f.lengthComboBox.setSelectedIndex(4); //40 foot car

        // test no track selected error
        f.locationBox.setSelectedIndex(1);
        JemmyUtil.enterClickAndLeave(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));

        car = cm.getByRoadAndNumber("SP", "10345");
        Assert.assertNotNull("car exists", car);

        Assert.assertEquals("car location", null, car.getLocation());

        f.trackLocationBox.setSelectedIndex(1);
        JemmyUtil.enterClickAndLeave(f.addButton);

        // car already exists
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("carCanNotAdd"), Bundle.getMessage("ButtonOK"));
        Assert.assertEquals("car location", null, car.getLocation());

        JemmyUtil.enterClickAndLeave(f.saveButton);
        // car location should have been updated
        Assert.assertEquals("car location", loc1, car.getLocation());

        // now change location
        f.locationBox.setSelectedIndex(2);
        f.trackLocationBox.setSelectedIndex(1);

        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertEquals("car location", loc2, car.getLocation());

        // add a track
        Assert.assertEquals("Number of locations", 4, f.locationBox.getItemCount());
        Assert.assertEquals("Number of tracks", 2, f.trackLocationBox.getItemCount());
        Track testSpur = loc2.addTrack("Test_Spur", Track.SPUR);

        // add a location to cause update to both location and track comboboxes
        lm.newLocation("Test_Location");
        Assert.assertEquals("Number of locations", 5, f.locationBox.getItemCount());
        Assert.assertEquals("Number of tracks", 3, f.trackLocationBox.getItemCount());

        // try to set car to test spur, with a length of 0
        f.trackLocationBox.setSelectedIndex(2);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));

        // get response message
        String status = car.setLocation(loc2, testSpur);
        Assert.assertFalse(status.equals(Track.OKAY));
        JemmyUtil.pressDialogButton(f, MessageFormat
                .format(Bundle.getMessage("rsOverride"), new Object[]{status}), Bundle.getMessage("ButtonNo"));

        // confirm car location and track didn't change
        Assert.assertNotEquals("track", testSpur, car.getTrack());

        // do it again, but say yes
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.pressDialogButton(f, MessageFormat
                .format(Bundle.getMessage("rsOverride"), new Object[]{status}), Bundle.getMessage("ButtonYes"));

        // confirm car location and track changed
        Assert.assertEquals("track", testSpur, car.getTrack());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddCar() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        // increase test coverage
        Setup.setValueEnabled(true);
        Setup.setRfidEnabled(true);

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.setTitle("Test Add Car Frame");

        // this will also create property changes
        JUnitOperationsUtil.initOperationsData(); // load cars

        // confirm load
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("number of cars", 9, cManager.getNumEntries());

        // add a new car
        f.roadNumberTextField.setText("6");
        f.roadComboBox.setSelectedItem("SP");
        f.typeComboBox.setSelectedItem(Bundle.getMessage("Caboose"));
        f.lengthComboBox.setSelectedItem("38");
        f.colorComboBox.setSelectedItem("Black");
        f.loadComboBox.setSelectedItem("L");
        f.builtTextField.setText("1999");
        f.ownerComboBox.setSelectedItem("DAB");
        f.commentTextField.setText("test car comment field");

        // Save button should be disabled
        // Jemmy has no way to click a disabled button.
        //JemmyUtil.enterClickAndLeave(f.saveButton);
        Car c6 = cManager.getByRoadAndNumber("SP", "6");
        Assert.assertNull("Car should not exist", c6);

        // use add button
        JemmyUtil.enterClickAndLeave(f.addButton);

        c6 = cManager.getByRoadAndNumber("SP", "6");
        Assert.assertNotNull("Car create", c6);
        Assert.assertEquals("car type", Bundle.getMessage("Caboose"), c6.getTypeName());
        Assert.assertEquals("car length", "38", c6.getLength());
        Assert.assertEquals("car color", "Black", c6.getColor());
        Assert.assertEquals("car load", "L", c6.getLoadName());
        Assert.assertEquals("car built", "1999", c6.getBuilt());
        Assert.assertEquals("car owner", "DAB", c6.getOwner());
        Assert.assertEquals("car comment", "test car comment field", c6.getComment());

        // test type default check boxes
        Assert.assertFalse("not a caboose", c6.isCaboose());
        Assert.assertFalse("no fred", c6.hasFred());
        Assert.assertFalse("not hazardous", c6.isHazardous());

        JemmyUtil.enterClickAndLeave(f.cabooseCheckBox);
        Assert.assertFalse("still not a caboose", c6.isCaboose());
        JemmyUtil.enterClickAndLeave(f.saveButton);
        // Change all car type to caboose dialog window should appear
        // need to push the "No" button in the dialog window to close
        JemmyUtil.pressDialogButton(f, MessageFormat.format(
                Bundle.getMessage("carModifyAllType"), new Object[]{Bundle.getMessage("Caboose")}),
                Bundle.getMessage("ButtonNo"));

        Assert.assertTrue("now a caboose", c6.isCaboose());
        Assert.assertFalse("not hazardous 2", c6.isHazardous());

        JemmyUtil.enterClickAndLeave(f.fredCheckBox);
        Assert.assertTrue("still a caboose", c6.isCaboose());
        Assert.assertFalse("still no fred", c6.hasFred());
        JemmyUtil.enterClickAndLeave(f.saveButton);
        // need to push the "No" button in the dialog window to close
        JemmyUtil.pressDialogButton(f, MessageFormat.format(
                Bundle.getMessage("carModifyAllType"), new Object[]{Bundle.getMessage("Caboose")}),
                Bundle.getMessage("ButtonNo"));
        Assert.assertFalse("no longer a caboose", c6.isCaboose());
        Assert.assertTrue("now has a fred", c6.hasFred());
        Assert.assertFalse("not hazardous 3", c6.isHazardous());

        JemmyUtil.enterClickAndLeave(f.hazardousCheckBox);
        Assert.assertFalse("still not hazardous 3", c6.isHazardous());
        JemmyUtil.enterClickAndLeave(f.saveButton);
        // need to push the "No" button in the dialog window to close
        JemmyUtil.pressDialogButton(f, MessageFormat.format(
                Bundle.getMessage("carModifyAllType"), new Object[]{Bundle.getMessage("Caboose")}),
                Bundle.getMessage("ButtonNo"));
        Assert.assertFalse("still no longer a caboose", c6.isCaboose());
        Assert.assertTrue("still has a fred", c6.hasFred());
        Assert.assertTrue("now hazardous", c6.isHazardous());

        JemmyUtil.enterClickAndLeave(f.utilityCheckBox);
        Assert.assertFalse("not utility", c6.isUtility());
        JemmyUtil.enterClickAndLeave(f.saveButton);
        // need to push the "No" button in the dialog window to close
        JemmyUtil.pressDialogButton(f,MessageFormat.format(
                Bundle.getMessage("carModifyAllType"), new Object[]{Bundle.getMessage("Caboose")}),
                Bundle.getMessage("ButtonNo"));
        Assert.assertTrue("now utility", c6.isUtility());
        Assert.assertFalse("not a caboose", c6.isCaboose());
        Assert.assertTrue("still has a fred", c6.hasFred());
        Assert.assertTrue("still hazardous", c6.isHazardous());

        // should have one more car
        Assert.assertEquals("number of cars", 10, cManager.getNumEntries());

        // add another car
        f.roadNumberTextField.setText("7");
        JemmyUtil.enterClickAndLeave(f.addButton);
        Assert.assertEquals("number of cars", 11, cManager.getNumEntries());

        Car c7 = cManager.getByRoadAndNumber("SP", "6");
        Assert.assertNotNull("Car create", c7);

        // confirm that c6 still exist
        c6 = cManager.getByRoadAndNumber("SP", "6");
        Assert.assertNotNull("Car create", c6);

        JUnitUtil.dispose(f);
    }

    @Test
    public void testSaveCar() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        // confirm load
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("number of cars", 9, cManager.getNumEntries());

        Car car = cManager.getByRoadAndNumber("CP", "888");
        // confirm car id
        Assert.assertEquals("car id", "CP888", car.getId());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.load(car);

        Assert.assertEquals("car road", "CP", f.roadComboBox.getSelectedItem());
        Assert.assertEquals("car number", "888", f.roadNumberTextField.getText());

        // change road number for this car
        f.roadNumberTextField.setText("54321");
        JemmyUtil.enterClickAndLeave(f.saveButton);
        car = cManager.getByRoadAndNumber("CP", "54321");
        Assert.assertNotNull("car exists", car);
        // confirm car id was modified
        Assert.assertEquals("car id", "CP54321", car.getId());

        // close on save
        Setup.setCloseWindowOnSaveEnabled(true);

        // change road name
        f.roadComboBox.setSelectedItem("SP");
        JemmyUtil.enterClickAndLeave(f.saveButton);
        car = cManager.getByRoadAndNumber("SP", "54321");
        Assert.assertNotNull("car exists", car);
        // confirm car id was modified
        Assert.assertEquals("car id", "SP54321", car.getId());

        Assert.assertFalse("window closed", f.isVisible());
    }
    
    @Test
    public void testSaveExistingCar() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car car = cManager.getByRoadAndNumber("CP", "888");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.load(car);

        // change road number for this car to an existing car
        f.roadNumberTextField.setText("X20002");
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carCanNotUpdate"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonOK"));
 
        JUnitUtil.dispose(f);
    }

    @Test
    public void testSaveCarPassenger() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        // confirm load
        CarManager cManager = InstanceManager.getDefault(CarManager.class);
        Assert.assertEquals("number of cars", 9, cManager.getNumEntries());

        Car car = cManager.getByRoadAndNumber("CP", "888");
        Car car2 = cManager.getByRoadAndNumber("CP", "X20002");
        Assert.assertFalse(car.isPassenger());
        Assert.assertFalse(car2.isPassenger());
        Assert.assertEquals("blocking order", 0, car.getBlocking());
        Assert.assertEquals("blocking order", 0, car2.getBlocking());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.load(car);

        JemmyUtil.enterClickAndLeave(f.passengerCheckBox);
        f.blockingTextField.setText("23"); // random number for blocking order
        JemmyUtil.enterClickAndLeave(f.saveButton);

        // 1st dialog, make all Boxcar passenger?
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonNo"));
        //2nd dialog, make all Boxcar blocking order 23 
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonNo"));

        Assert.assertTrue(car.isPassenger());
        Assert.assertFalse(car2.isPassenger());
        Assert.assertEquals("blocking order", 23, car.getBlocking());
        Assert.assertEquals("blocking order", 0, car2.getBlocking());

        JemmyUtil.enterClickAndLeave(f.passengerCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonNo"));

        Assert.assertFalse(car.isPassenger());
        Assert.assertFalse(car2.isPassenger());
        Assert.assertEquals("blocking order", 23, car.getBlocking());
        Assert.assertEquals("blocking order", 0, car2.getBlocking());

        JemmyUtil.enterClickAndLeave(f.passengerCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonYes"));

        Assert.assertTrue(car.isPassenger());
        Assert.assertTrue(car2.isPassenger());
        Assert.assertEquals("blocking order", 23, car.getBlocking());
        Assert.assertEquals("blocking order", 0, car2.getBlocking());

        f.blockingTextField.setText("99"); // random number for blocking order
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonYes"));

        Assert.assertTrue(car.isPassenger());
        Assert.assertTrue(car2.isPassenger());
        Assert.assertEquals("blocking order", 99, car.getBlocking());
        Assert.assertEquals("blocking order", 99, car2.getBlocking());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testSaveCarCaboose() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car car = cManager.getByRoadAndNumber("CP", "888");
        Car car2 = cManager.getByRoadAndNumber("CP", "X20002");
        Assert.assertFalse(car.isCaboose());
        Assert.assertFalse(car2.isCaboose());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.load(car);

        JemmyUtil.enterClickAndLeave(f.cabooseCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        // dialog, make all Boxcar Caboose?
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonNo"));

        Assert.assertTrue(car.isCaboose());
        Assert.assertFalse(car2.isCaboose());

        JemmyUtil.enterClickAndLeave(f.cabooseCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonNo"));

        Assert.assertFalse(car.isCaboose());
        Assert.assertFalse(car2.isCaboose());

        JemmyUtil.enterClickAndLeave(f.cabooseCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonYes"));

        Assert.assertTrue(car.isCaboose());
        Assert.assertTrue(car2.isCaboose());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testSaveCarFred() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car car = cManager.getByRoadAndNumber("CP", "888");
        Assert.assertFalse(car.hasFred());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.load(car);

        JemmyUtil.enterClickAndLeave(f.fredCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertTrue(car.hasFred());

        JemmyUtil.enterClickAndLeave(f.fredCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertFalse(car.hasFred());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testSaveCarUtility() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car car = cManager.getByRoadAndNumber("CP", "888");
        Car car2 = cManager.getByRoadAndNumber("CP", "X20002");
        Assert.assertFalse(car.isUtility());
        Assert.assertFalse(car2.isUtility());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.load(car);

        JemmyUtil.enterClickAndLeave(f.utilityCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        // dialog, make all Boxcar utility?
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonNo"));

        Assert.assertTrue(car.isUtility());
        Assert.assertFalse(car2.isUtility());

        JemmyUtil.enterClickAndLeave(f.utilityCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonNo"));

        Assert.assertFalse(car.isUtility());
        Assert.assertFalse(car2.isUtility());

        JemmyUtil.enterClickAndLeave(f.utilityCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonYes"));

        Assert.assertTrue(car.isUtility());
        Assert.assertTrue(car2.isUtility());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testSaveCarHazardous() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car car = cManager.getByRoadAndNumber("CP", "888");
        Car car2 = cManager.getByRoadAndNumber("CP", "X20002");
        Assert.assertFalse(car.isHazardous());
        Assert.assertFalse(car2.isHazardous());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.load(car);

        JemmyUtil.enterClickAndLeave(f.hazardousCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        // dialog, make all Boxcar Caboose?
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonNo"));

        Assert.assertTrue(car.isHazardous());
        Assert.assertFalse(car2.isHazardous());

        JemmyUtil.enterClickAndLeave(f.hazardousCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonNo"));

        Assert.assertFalse(car.isHazardous());
        Assert.assertFalse(car2.isHazardous());

        JemmyUtil.enterClickAndLeave(f.hazardousCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);

        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[]{car.getTypeName()}), Bundle.getMessage("ButtonYes"));

        Assert.assertTrue(car.isHazardous());
        Assert.assertTrue(car2.isHazardous());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testSaveKernel() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());

        JUnitOperationsUtil.initOperationsData();

        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car car = cManager.getByRoadAndNumber("CP", "888");
        Car car2 = cManager.getByRoadAndNumber("CP", "X20002");
        Assert.assertNull(car.getKernel());
        Assert.assertNull(car2.getKernel());
        Assert.assertEquals("Track", "NI Yard", car.getTrackName());
        Assert.assertEquals("Track", "North End 2", car2.getTrackName());
        Assert.assertEquals("Load", "E", car.getLoadName());
        Assert.assertEquals("Load", "E", car2.getLoadName());

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.load(car);

        // create a kernel
        Kernel k = cManager.newKernel("Test_Kernel");
        car2.setKernel(k);

        f.groupComboBox.setSelectedItem("Test_Kernel");
        JemmyUtil.enterClickAndLeave(f.saveButton);

        // dialog requesting to make all cars in kernel to have the same location and track
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carPartKernel"),
                        new Object[]{car.getKernelName()}), Bundle.getMessage("ButtonNo"));

        Assert.assertEquals("kernel", k, car.getKernel());
        Assert.assertEquals("order", 2, car.getBlocking());
        Assert.assertFalse(car.getKernel().isLead(car));
        Assert.assertFalse(car.isLead());
        Assert.assertEquals("Track", "North End 2", car2.getTrackName());

        // now remove the kernel
        f.groupComboBox.setSelectedIndex(0);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        Assert.assertNull(car.getKernel());

        // If car has FRED it becomes the lead
        f.groupComboBox.setSelectedItem("Test_Kernel");
        JemmyUtil.enterClickAndLeave(f.fredCheckBox);
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        // dialog requesting to make all cars in kernel to have the same location and track
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carPartKernel"),
                        new Object[]{car.getKernelName()}), Bundle.getMessage("ButtonYes"));


        Assert.assertEquals("kernel", k, car.getKernel());
        Assert.assertEquals("order", 2, car.getBlocking());
        Assert.assertTrue(car.getKernel().isLead(car));
        Assert.assertEquals("Track", "NI Yard", car2.getTrackName());
        
        // change the car's load
        f.loadComboBox.setSelectedItem("L");
        JemmyUtil.enterClickAndLeave(f.saveButton);
        
        // dialog requesting to make all cars in kernel have the same load
        JemmyUtil.pressDialogButton(MessageFormat.format(Bundle.getMessage("carPartKernel"),
                        new Object[]{car.getKernelName()}), Bundle.getMessage("ButtonYes"));
        
        Assert.assertEquals("Load", "L", car.getLoadName());
        Assert.assertEquals("Load", "L", car2.getLoadName());

        JUnitUtil.dispose(f);
    }

    @Test
    public void testCarEditFrameRead() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Assert.assertEquals("number of cars", 9, cManager.getNumEntries());
        Car c1 = cManager.getByRoadAndNumber("CP", "C10099");

        c1.setWeight("1.4");
        c1.setWeightTons("Tons of Weight");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.load(c1);
        f.setTitle("Test Edit Car Frame");

        Assert.assertEquals("car road", "CP", f.roadComboBox.getSelectedItem());
        Assert.assertEquals("car number", "C10099", f.roadNumberTextField.getText());
        Assert.assertEquals("car type", Bundle.getMessage("Caboose"), f.typeComboBox.getSelectedItem());
        Assert.assertEquals("car length", "32", f.lengthComboBox.getSelectedItem());
        Assert.assertEquals("car weight", "1.4", f.weightTextField.getText());
        Assert.assertEquals("car weight tons", "Tons of Weight", f.weightTonsTextField.getText());
        Assert.assertEquals("car color", "Red", f.colorComboBox.getSelectedItem());
        Assert.assertEquals("car load", "E", f.loadComboBox.getSelectedItem());
        Assert.assertEquals("car built", "1980", f.builtTextField.getText());
        Assert.assertEquals("car owner", "AT", f.ownerComboBox.getSelectedItem());
        Assert.assertEquals("car comment", "Test Car CP C10099 Comment", f.commentTextField.getText());

        Assert.assertTrue("car is a caboose", f.cabooseCheckBox.isSelected());
        Assert.assertFalse("car does not have a fred", f.fredCheckBox.isSelected());
        Assert.assertFalse("car is not hazardous", f.hazardousCheckBox.isSelected());

        // test delete button
        JemmyUtil.enterClickAndLeave(f.deleteButton);

        // should have one less car
        Assert.assertEquals("number of cars", 8, cManager.getNumEntries());
        Assert.assertNull("car doesn't exist", cManager.getByRoadAndNumber("CP", "C10099"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddNewCarRoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setRoadName("TEST_ROAD");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        // should cause add road dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addRoad"), Bundle.getMessage("ButtonNo"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertFalse(InstanceManager.getDefault(CarRoads.class).containsName("TEST_ROAD"));

        // now answer yes to add road
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addRoad"), Bundle.getMessage("ButtonYes"));

        try {
            load2.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertTrue(InstanceManager.getDefault(CarRoads.class).containsName("TEST_ROAD"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddNewCarType() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setTypeName("TEST_TYPE");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        // should cause add type dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addType"), Bundle.getMessage("ButtonNo"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertFalse(InstanceManager.getDefault(CarTypes.class).containsName("TEST_TYPE"));

        // now answer yes to add type
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addType"), Bundle.getMessage("ButtonYes"));

        try {
            load2.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertTrue(InstanceManager.getDefault(CarTypes.class).containsName("TEST_TYPE"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddNewCarLength() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setLength("123");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        // should cause add length dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addLength"), Bundle.getMessage("ButtonNo"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertFalse(InstanceManager.getDefault(CarLengths.class).containsName("123"));

        // now answer yes to add length
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addLength"), Bundle.getMessage("ButtonYes"));

        try {
            load2.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertTrue(InstanceManager.getDefault(CarLengths.class).containsName("123"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddNewCarOwner() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setOwner("TEST_OWNER");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        // should cause add owner dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addOwner"), Bundle.getMessage("ButtonNo"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertFalse(InstanceManager.getDefault(CarOwners.class).containsName("TEST_OWNER"));

        // now answer yes to add owner
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addOwner"), Bundle.getMessage("ButtonYes"));

        try {
            load2.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertTrue(InstanceManager.getDefault(CarOwners.class).containsName("TEST_OWNER"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddNewCarColor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setColor("TEST_COLOR");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        // should cause add color dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("carAddColor"), Bundle.getMessage("ButtonNo"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertFalse(InstanceManager.getDefault(CarColors.class).containsName("TEST_COLOR"));

        // now answer yes to add color
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("carAddColor"), Bundle.getMessage("ButtonYes"));

        try {
            load2.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertTrue(InstanceManager.getDefault(CarColors.class).containsName("TEST_COLOR"));

        JUnitUtil.dispose(f);
    }

    @Test
    public void testAddNewCarLoad() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setLoadName("TEST_LOAD");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        // should cause add load dialog to appear
        Thread load = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load.setName("load edit frame"); // NOI18N
        load.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addLoad"), Bundle.getMessage("ButtonNo"));

        try {
            load.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertFalse(InstanceManager.getDefault(CarLoads.class).containsName(c1.getTypeName(), "TEST_LOAD"));

        // now answer yes to add load
        Thread load2 = new Thread(new Runnable() {
            @Override
            public void run() {
                f.load(c1);
            }
        });
        load2.setName("load edit frame"); // NOI18N
        load2.start();

        jmri.util.JUnitUtil.waitFor(() -> {
            return load2.getState().equals(Thread.State.WAITING);
        }, "wait for prompt");

        JemmyUtil.pressDialogButton(Bundle.getMessage("addLoad"), Bundle.getMessage("ButtonYes"));

        try {
            load2.join();
        } catch (InterruptedException e) {
            // do nothing
        }

        Assert.assertTrue(InstanceManager.getDefault(CarLoads.class).containsName(c1.getTypeName(), "TEST_LOAD"));

        JUnitUtil.dispose(f);
    }
}
