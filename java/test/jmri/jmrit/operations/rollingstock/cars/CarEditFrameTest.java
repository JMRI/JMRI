package jmri.jmrit.operations.rollingstock.cars;

import java.text.MessageFormat;

import org.junit.Assert;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

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
import jmri.util.ThreadingUtil;
import jmri.util.swing.JemmyUtil;

/**
 * Tests for the Operations Cars GUI class
 *
 * @author Dan Boudreau Copyright (C) 2009
 */
@Timeout(10)
public class CarEditFrameTest extends OperationsTestCase {

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testClearRoadNumber() {

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());

        f.roadNumberTextField.setText("123");
        JemmyUtil.enterClickAndLeave(f.clearRoadNumberButton);
        Assert.assertEquals("road number", "", f.roadNumberTextField.getText());

        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testRoadNumberErrorConditions() {

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());

        // this will load the weight fields
        f.lengthComboBox.setSelectedIndex(4);

        // "*" is not a legal character for road number
        f.roadNumberTextField.setText("6*6");

        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("roadNumNG"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        // test number too long
        StringBuilder sb = new StringBuilder("A");
        for (int i = 0; i < Control.max_len_string_road_number; i++) {
            sb.append(i);
        }

        f.roadNumberTextField.setText(sb.toString());

        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("RoadNumTooLong"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
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

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testWeightErrorConditions() {

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        Assert.assertTrue(f.isShowing());

        // enter a good road number
        f.roadNumberTextField.setText("123456");

        // new dialog warning car weight
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("carActualWeight"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        f.weightTextField.setText("1.5"); // good oz weight
        f.weightTonsTextField.setText("Bogus Weight");
        // new dialog warning car weight
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("WeightTonError"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
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

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testEditRoadButton() {

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editRoadButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.ROAD, f.carAttributeEditFrame._attribute);

        // now change to car type
        JemmyUtil.enterClickAndLeave(f.editTypeButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.TYPE, f.carAttributeEditFrame._attribute);

        JUnitUtil.dispose(f.carAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testEditTypeButton() {

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editTypeButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.TYPE, f.carAttributeEditFrame._attribute);

        JUnitUtil.dispose(f.carAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testEditColorButton() {

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editColorButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.COLOR, f.carAttributeEditFrame._attribute);

        JUnitUtil.dispose(f.carAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testEditLengthButton() {

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editLengthButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.LENGTH, f.carAttributeEditFrame._attribute);

        JUnitUtil.dispose(f.carAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testEditOwnerButton() {

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editOwnerButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.OWNER, f.carAttributeEditFrame._attribute);

        JUnitUtil.dispose(f.carAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testEditGroupButton() {

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editGroupButton);
        Assert.assertTrue(f.carAttributeEditFrame.isShowing());
        Assert.assertEquals("Check attribute", CarAttributeEditFrame.KERNEL, f.carAttributeEditFrame._attribute);

        JUnitUtil.dispose(f.carAttributeEditFrame);
        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testEditLoadButton() {

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        JemmyUtil.enterClickAndLeave(f.editLoadButton);
        Assert.assertTrue(f.carLoadEditFrame.isShowing());

        JUnitUtil.dispose(f.carLoadEditFrame);
        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testLocationComboBox() {

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
        Assert.assertNull(car);

        // this will load the weight fields
        f.lengthComboBox.setSelectedIndex(4); // 40 foot car

        // test no track selected error
        f.locationBox.setSelectedIndex(1);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
        
        car = cm.getByRoadAndNumber("SP", "10345");
        Assert.assertNotNull(car);

        Assert.assertEquals("car location", null, car.getLocation());

        f.trackLocationBox.setSelectedIndex(1);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.addButton);
        // car already exists
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("carCanNotAdd"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);
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
        // get response message
        String status = car.setLocation(loc2, testSpur);
        Assert.assertFalse(status.equals(Track.OKAY));
        
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.pressDialogButton(f, MessageFormat.format(Bundle.getMessage("rsOverride"), new Object[] { status }),
                Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        
        // confirm car location and track didn't change
        Assert.assertNotEquals("track", testSpur, car.getTrack());

        // do it again, but say yes
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(f, Bundle.getMessage("rsCanNotLoc"), Bundle.getMessage("ButtonOK"));
        JemmyUtil.pressDialogButton(f, MessageFormat.format(Bundle.getMessage("rsOverride"), new Object[] { status }),
                Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        // confirm car location and track changed
        Assert.assertEquals("track", testSpur, car.getTrack());

        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddCar() {

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
        // JemmyUtil.enterClickAndLeave(f.saveButton);
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
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // Change all car type to caboose dialog window should appear
        // need to push the "No" button in the dialog window to close
        JemmyUtil.pressDialogButton(f, MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[] { Bundle.getMessage("Caboose") }), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        Assert.assertTrue("now a caboose", c6.isCaboose());
        Assert.assertFalse("not hazardous 2", c6.isHazardous());

        JemmyUtil.enterClickAndLeave(f.fredCheckBox);
        Assert.assertTrue("still a caboose", c6.isCaboose());
        Assert.assertFalse("still no fred", c6.hasFred());
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // need to push the "No" button in the dialog window to close
        JemmyUtil.pressDialogButton(f, MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[] { Bundle.getMessage("Caboose") }), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        Assert.assertFalse("no longer a caboose", c6.isCaboose());
        Assert.assertTrue("now has a fred", c6.hasFred());
        Assert.assertFalse("not hazardous 3", c6.isHazardous());

        JemmyUtil.enterClickAndLeave(f.hazardousCheckBox);
        Assert.assertFalse("still not hazardous 3", c6.isHazardous());
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // need to push the "No" button in the dialog window to close
        JemmyUtil.pressDialogButton(f, MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[] { Bundle.getMessage("Caboose") }), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        Assert.assertFalse("still no longer a caboose", c6.isCaboose());
        Assert.assertTrue("still has a fred", c6.hasFred());
        Assert.assertTrue("now hazardous", c6.isHazardous());

        JemmyUtil.enterClickAndLeave(f.utilityCheckBox);
        Assert.assertFalse("not utility", c6.isUtility());
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // need to push the "No" button in the dialog window to close
        JemmyUtil.pressDialogButton(f, MessageFormat.format(Bundle.getMessage("carModifyAllType"),
                new Object[] { Bundle.getMessage("Caboose") }), Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
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

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testSaveCar() {

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

    // @Ignore("AppVeyor:giving up after 3 failures. 12/31/2019")
    // please detail failure so test can be fixed
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testSaveExistingCar() {

        JUnitOperationsUtil.initOperationsData();

        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car car = cManager.getByRoadAndNumber("CP", "888");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        f.load(car);

        // change road number for this car to an existing car
        f.roadNumberTextField.setText("X20002");

        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carCanNotUpdate"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonOK"));
        JemmyUtil.waitFor(f);

        JUnitUtil.dispose(f);
    }

    // @Ignore("AppVeyor:giving up after 3 failures. 12/31/2019")
    // please detail failure so test can be fixed
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testSaveCarPassenger() {

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
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);

        // 1st dialog, make all Boxcar passenger?
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonNo"));
        // 2nd dialog, make all Boxcar blocking order 23
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        Assert.assertTrue(car.isPassenger());
        Assert.assertFalse(car2.isPassenger());
        Assert.assertEquals("blocking order", 23, car.getBlocking());
        Assert.assertEquals("blocking order", 0, car2.getBlocking());

        JemmyUtil.enterClickAndLeave(f.passengerCheckBox);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        
        Assert.assertFalse(car.isPassenger());
        Assert.assertFalse(car2.isPassenger());
        Assert.assertEquals("blocking order", 23, car.getBlocking());
        Assert.assertEquals("blocking order", 0, car2.getBlocking());

        JemmyUtil.enterClickAndLeave(f.passengerCheckBox);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        
        Assert.assertTrue(car.isPassenger());
        Assert.assertTrue(car2.isPassenger());
        Assert.assertEquals("blocking order", 23, car.getBlocking());
        Assert.assertEquals("blocking order", 0, car2.getBlocking());

        f.blockingTextField.setText("99"); // random number for blocking order
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        
        Assert.assertTrue(car.isPassenger());
        Assert.assertTrue(car2.isPassenger());
        Assert.assertEquals("blocking order", 99, car.getBlocking());
        Assert.assertEquals("blocking order", 99, car2.getBlocking());

        JUnitUtil.dispose(f);
    }

    // @Ignore("AppVeyor:giving up after 3 failures. 12/31/2019")
    // please detail failure so test can be fixed
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testSaveCarCaboose() {

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
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // dialog, make all Boxcar Caboose?
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);

        Assert.assertTrue(car.isCaboose());
        Assert.assertFalse(car2.isCaboose());

        JemmyUtil.enterClickAndLeave(f.cabooseCheckBox);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);

        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        Assert.assertFalse(car.isCaboose());
        Assert.assertFalse(car2.isCaboose());

        JemmyUtil.enterClickAndLeave(f.cabooseCheckBox);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);

        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        Assert.assertTrue(car.isCaboose());
        Assert.assertTrue(car2.isCaboose());

        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testSaveCarFred() {

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

    // @Ignore("AppVeyor:giving up after 3 failures. 12/31/2019")
    // please detail failure so test can be fixed
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testSaveCarUtility() {

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
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // dialog, make all Boxcar utility?
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);

        Assert.assertTrue(car.isUtility());
        Assert.assertFalse(car2.isUtility());

        JemmyUtil.enterClickAndLeave(f.utilityCheckBox);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);

        Assert.assertFalse(car.isUtility());
        Assert.assertFalse(car2.isUtility());

        JemmyUtil.enterClickAndLeave(f.utilityCheckBox);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);

        Assert.assertTrue(car.isUtility());
        Assert.assertTrue(car2.isUtility());

        JUnitUtil.dispose(f);
    }

    // @Ignore("AppVeyor:giving up after 3 failures. 12/31/2019")
    // please detail failure so test can be fixed
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testSaveCarHazardous() {

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
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // dialog, make all Boxcar hazardous?
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);

        Assert.assertTrue(car.isHazardous());
        Assert.assertFalse(car2.isHazardous());

        JemmyUtil.enterClickAndLeave(f.hazardousCheckBox);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);

        Assert.assertFalse(car.isHazardous());
        Assert.assertFalse(car2.isHazardous());

        JemmyUtil.enterClickAndLeave(f.hazardousCheckBox);
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);

        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carModifyAllType"), new Object[] { car.getTypeName() }),
                Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        Assert.assertTrue(car.isHazardous());
        Assert.assertTrue(car2.isHazardous());

        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testSaveKernel() {

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
        Kernel k = InstanceManager.getDefault(KernelManager.class).newKernel("Test_Kernel");
        car2.setKernel(k);

        f.groupComboBox.setSelectedItem("Test_Kernel");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // dialog requesting to make all cars in kernel to have the same location and
        // track
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carPartKernel"), new Object[] { car2.getKernelName() }),
                Bundle.getMessage("ButtonNo"));
        JemmyUtil.waitFor(f);
        
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
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // dialog requesting to make all cars in kernel to have the same location and
        // track
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carPartKernel"), new Object[] { car2.getKernelName() }),
                Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);
        
        Assert.assertEquals("kernel", k, car.getKernel());
        Assert.assertEquals("order", 2, car.getBlocking());
        Assert.assertTrue(car.getKernel().isLead(car));
        Assert.assertEquals("Track", "NI Yard", car2.getTrackName());

        // change the car's load
        f.loadComboBox.setSelectedItem("L");
        JemmyUtil.enterClickAndLeaveThreadSafe(f.saveButton);
        // dialog requesting to make all cars in kernel have the same load
        JemmyUtil.pressDialogButton(f,
                MessageFormat.format(Bundle.getMessage("carPartKernel"), new Object[] { car2.getKernelName() }),
                Bundle.getMessage("ButtonYes"));
        JemmyUtil.waitFor(f);

        Assert.assertEquals("Load", "L", car.getLoadName());
        Assert.assertEquals("Load", "L", car2.getLoadName());

        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testCarEditFrameRead() {
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

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddNewCarRoadNo() {
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setRoadName("TEST_ROAD");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        
        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(Bundle.getMessage("addRoad"), Bundle.getMessage("ButtonNo"));
        });
        t1.setName("click testAddNewCarRoadNo Thread");
        t1.start();

        ThreadingUtil.runOnGUI( ()->{
                f.load(c1);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click No Button in dialogue didn't happen");

        Assert.assertFalse(InstanceManager.getDefault(CarRoads.class).containsName("TEST_ROAD"));
        JUnitUtil.dispose(f);
    }
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddNewCarRoadYes() {
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setRoadName("TEST_ROAD");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(f, Bundle.getMessage("addRoad"), Bundle.getMessage("ButtonYes"));
        });
        t1.setName("click testAddNewCarRoadYes Thread");
        t1.start();
        
        // should cause add road dialog to appear
        ThreadingUtil.runOnGUI( ()->{
                f.load(c1);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click Yes Button in dialogue didn't happen");
        
        Assert.assertTrue(InstanceManager.getDefault(CarRoads.class).containsName("TEST_ROAD"));
        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddNewCarTypeYes() {
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setTypeName("TEST_TYPE");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        
        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(f, Bundle.getMessage("addType"), Bundle.getMessage("ButtonYes"));
        });
        t1.setName("click testAddNewCarTypeYes Thread");
        t1.start();

        // should cause add type dialog to appear
        ThreadingUtil.runOnGUI( ()->{
                f.load(c1);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click Yes Button in dialogue didn't happen");
        
        Assert.assertTrue(InstanceManager.getDefault(CarTypes.class).containsName("TEST_TYPE"));
        JUnitUtil.dispose(f);
    }
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddNewCarTypeNo() {
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setTypeName("TEST_TYPE");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(f, Bundle.getMessage("addType"), Bundle.getMessage("ButtonNo"));
        });
        t1.setName("click testAddNewCarTypeNo Thread");
        t1.start();
        
        // should cause add type dialog to appear
        ThreadingUtil.runOnGUI( ()->{
                f.load(c1);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click No Button in dialogue didn't happen");
        
        Assert.assertFalse(InstanceManager.getDefault(CarTypes.class).containsName("TEST_TYPE"));
        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddNewCarLengthNo() {
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setLength("123");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(f, Bundle.getMessage("addLength"), Bundle.getMessage("ButtonNo"));
        });
        t1.setName("click testAddNewCarLengthNo Thread");
        t1.start();

        // should cause add length dialog to appear
        ThreadingUtil.runOnGUI( ()->{
                f.load(c1);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click No Button in dialogue didn't happen");
        
        Assert.assertFalse(InstanceManager.getDefault(CarLengths.class).containsName("123"));
        JUnitUtil.dispose(f);
    }
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddNewCarLengthYes() {
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setLength("123");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(f, Bundle.getMessage("addLength"), Bundle.getMessage("ButtonYes"));
        });
        t1.setName("click testAddNewCarLengthYes Thread");
        t1.start();

        // should cause add length dialog to appear
        ThreadingUtil.runOnGUI( ()->{
            f.load(c1);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click Yes Button in dialogue didn't happen");
        
        Assert.assertTrue(InstanceManager.getDefault(CarLengths.class).containsName("123"));
        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddNewCarOwnerNo() {
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setOwner("TEST_OWNER");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        
        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(f, Bundle.getMessage("addOwner"), Bundle.getMessage("ButtonNo"));
        });
        t1.setName("click testAddNewCarOwnerNo Thread");
        t1.start();

        ThreadingUtil.runOnGUI( ()->{
            f.load(c1);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click No Button in dialogue didn't happen");

        Assert.assertFalse(InstanceManager.getDefault(CarOwners.class).containsName("TEST_OWNER"));
        JUnitUtil.dispose(f);
    }
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddNewCarOwnerYes() {
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setOwner("TEST_OWNER");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        
        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(f, Bundle.getMessage("addOwner"), Bundle.getMessage("ButtonYes"));
        });
        t1.setName("click testAddNewCarRoadNo Thread");
        t1.start();

        ThreadingUtil.runOnGUI( ()->{
                f.load(c1);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click No Button in dialogue didn't happen");
        
        Assert.assertTrue(InstanceManager.getDefault(CarOwners.class).containsName("TEST_OWNER"));
        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddNewCarColorNo() {
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setColor("TEST_COLOR");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        
        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(f, Bundle.getMessage("carAddColor"), Bundle.getMessage("ButtonNo"));
        });
        t1.setName("click testAddNewCarColorNo Thread");
        t1.start();

        // should cause add color dialog to appear
        ThreadingUtil.runOnGUI( ()->{
                f.load(c1);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click No Button in dialogue didn't happen");
        
        Assert.assertFalse(InstanceManager.getDefault(CarColors.class).containsName("TEST_COLOR"));
        JUnitUtil.dispose(f);
    }
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddNewCarColorYes() {
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setColor("TEST_COLOR");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();
        
        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(f, Bundle.getMessage("carAddColor"), Bundle.getMessage("ButtonYes"));
        });
        t1.setName("click testAddNewCarColorYes Thread");
        t1.start();

        // should cause add color dialog to appear
        ThreadingUtil.runOnGUI( ()->{
                f.load(c1);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click Yes Button in dialogue didn't happen");
        
        Assert.assertTrue(InstanceManager.getDefault(CarColors.class).containsName("TEST_COLOR"));
        JUnitUtil.dispose(f);
    }

    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddNewCarLoadNo() {
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setLoadName("TEST_LOAD");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(f, Bundle.getMessage("addLoad"), Bundle.getMessage("ButtonNo"));
        });
        t1.setName("click testAddNewCarLoadNo Thread");
        t1.start();
        
        // should cause add load dialog to appear
        ThreadingUtil.runOnGUI( ()->{
                f.load(c1);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click No Button in dialogue didn't happen");

        Assert.assertFalse(InstanceManager.getDefault(CarLoads.class).containsName(c1.getTypeName(), "TEST_LOAD"));
        JUnitUtil.dispose(f);
    }
    
    @DisabledIfSystemProperty(named = "java.awt.headless", matches = "true")
    @Test
    public void testAddNewCarLoadYes() {
        JUnitOperationsUtil.initOperationsData(); // load cars
        CarManager cManager = InstanceManager.getDefault(CarManager.class);

        Car c1 = cManager.getByRoadAndNumber("CP", "777");
        c1.setLoadName("TEST_LOAD");

        CarEditFrame f = new CarEditFrame();
        f.initComponents();

        Thread t1 = new Thread(() -> {
            JemmyUtil.pressDialogButton(f, Bundle.getMessage("addLoad"), Bundle.getMessage("ButtonYes"));
        });
        t1.setName("click testAddNewCarLoadYes Thread");
        t1.start();
        
        // should cause add load dialog to appear
        ThreadingUtil.runOnGUI( ()->{
                f.load(c1);
        });

        JUnitUtil.waitFor(() -> {
            return !t1.isAlive();
        }, "Click Yes Button in dialogue didn't happen");

        Assert.assertTrue(InstanceManager.getDefault(CarLoads.class).containsName(c1.getTypeName(), "TEST_LOAD"));
        JUnitUtil.dispose(f);
    }
}
